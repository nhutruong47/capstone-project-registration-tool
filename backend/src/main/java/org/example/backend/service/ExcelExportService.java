package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final TopicRepository topicRepository;
    private final TopicReviewerRepository topicReviewerRepository;
    private final ChecklistResultRepository checklistResultRepository;
    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final SemesterRepository semesterRepository;

    /**
     * Xuất file Excel theo semester gồm các sheet:
     * 1. Registration - Danh sách đề tài
     * 2. Reviewer1 - Checklist Reviewer 1
     * 3. Reviewer2 - Checklist Reviewer 2
     * 4. Reviewer3 - Checklist Reviewer 3
     * 5. Reviewer4 - Checklist Reviewer 4 (nếu có)
     * 6. Tổng hợp kết quả
     * 7. Thống kê
     */
    public byte[] exportBySemester(Long semesterId) throws IOException {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        List<Topic> topics = topicRepository.findBySemester(semester);
        List<ChecklistTemplate> templates = checklistTemplateRepository.findAllByOrderByDisplayOrderAsc();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle passStyle = createStatusStyle(workbook, IndexedColors.GREEN);
            CellStyle failStyle = createStatusStyle(workbook, IndexedColors.RED);
            CellStyle considerStyle = createStatusStyle(workbook, IndexedColors.ORANGE);

            // Sheet 1: Registration
            createRegistrationSheet(workbook, topics, headerStyle, passStyle, failStyle, considerStyle);

            // Sheet 2-5: Reviewer1, Reviewer2, Reviewer3, Reviewer4
            for (int reviewerOrder = 1; reviewerOrder <= 4; reviewerOrder++) {
                createReviewerSheet(workbook, topics, templates, reviewerOrder, headerStyle);
            }

            // Sheet 6: Thống kê
            createStatisticsSheet(workbook, topics, semester, headerStyle);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Sheet Registration - giống cấu trúc Excel mẫu
     */
    private void createRegistrationSheet(Workbook workbook, List<Topic> topics,
            CellStyle headerStyle, CellStyle passStyle, CellStyle failStyle, CellStyle considerStyle) {
        Sheet sheet = workbook.createSheet("Registration");

        Row headerRow = sheet.createRow(0);
        String[] headers = { "STT", "Mã đề tài", "Tên đề tài (EN)", "Tên đề tài (VI)",
                "Department", "GVHD", "GVHD2", "Số SV", "Đợt",
                "Reviewer1", "Result R1", "Score R1",
                "Reviewer2", "Result R2", "Score R2",
                "Reviewer3", "Result R3", "Score R3",
                "Conflict", "Final Score", "Final Result", "Ghi chú" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Topic topic : topics) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(topic.getCode());
            row.createCell(2).setCellValue(topic.getTitleEn());
            row.createCell(3).setCellValue(topic.getTitleVi() != null ? topic.getTitleVi() : "");
            row.createCell(4).setCellValue(topic.getDepartment() != null ? topic.getDepartment() : "");
            row.createCell(5).setCellValue(topic.getSupervisor().getFullName());
            row.createCell(6).setCellValue(
                    topic.getSupervisor2() != null ? topic.getSupervisor2().getFullName() : "");
            row.createCell(7).setCellValue(topic.getStudentCount() != null ? topic.getStudentCount() : 0);
            row.createCell(8).setCellValue(topic.getRegistrationPhase().getName());

            // Reviewer data
            List<TopicReviewer> reviewers = topicReviewerRepository.findByTopic(topic);
            for (int r = 1; r <= 3; r++) {
                final int order = r;
                TopicReviewer reviewer = reviewers.stream()
                        .filter(tr -> tr.getReviewerOrder() == order)
                        .findFirst().orElse(null);
                int baseCol = 9 + (r - 1) * 3;
                if (reviewer != null) {
                    row.createCell(baseCol).setCellValue(reviewer.getReviewer().getFullName());
                    row.createCell(baseCol + 1).setCellValue(
                            reviewer.getDecision() != null ? reviewer.getDecision().name() : "");
                    row.createCell(baseCol + 2).setCellValue(
                            reviewer.getTotalScore() != null ? reviewer.getTotalScore() : 0);
                }
            }

            row.createCell(18).setCellValue(topic.getConflict() ? "TRUE" : "FALSE");
            row.createCell(19).setCellValue(topic.getTotalScore() != null ? topic.getTotalScore() : 0);

            Cell resultCell = row.createCell(20);
            resultCell.setCellValue(topic.getStatus().name());
            switch (topic.getStatus()) {
                case PASS, LOCKED -> resultCell.setCellStyle(passStyle);
                case FAIL -> resultCell.setCellStyle(failStyle);
                case CONSIDER -> resultCell.setCellStyle(considerStyle);
                default -> {
                }
            }

            row.createCell(21).setCellValue(topic.getFinalNote() != null ? topic.getFinalNote() : "");
            rowNum++;
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Sheet Reviewer N - dạng ma trận checklist (giống Excel mẫu)
     * Hàng = tiêu chí, Cột = đề tài
     */
    private void createReviewerSheet(Workbook workbook, List<Topic> topics,
            List<ChecklistTemplate> templates, int reviewerOrder, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Reviewer" + reviewerOrder);

        // Header row: "Tiêu chí" | FA25SE001 | FA25SE002 | ...
        Row headerRow = sheet.createRow(0);
        Cell criteriaHeader = headerRow.createCell(0);
        criteriaHeader.setCellValue("Tiêu chí đánh giá");
        criteriaHeader.setCellStyle(headerStyle);

        for (int i = 0; i < topics.size(); i++) {
            Cell cell = headerRow.createCell(i + 1);
            cell.setCellValue(topics.get(i).getCode());
            cell.setCellStyle(headerStyle);
        }

        // Rows = checklist criteria
        for (int t = 0; t < templates.size(); t++) {
            Row row = sheet.createRow(t + 1);
            row.createCell(0).setCellValue(templates.get(t).getName());

            for (int i = 0; i < topics.size(); i++) {
                Topic topic = topics.get(i);
                List<TopicReviewer> reviewers = topicReviewerRepository.findByTopic(topic);
                TopicReviewer reviewer = reviewers.stream()
                        .filter(tr -> tr.getReviewerOrder() == reviewerOrder)
                        .findFirst().orElse(null);

                if (reviewer != null) {
                    List<ChecklistResult> results = checklistResultRepository.findByTopicReviewer(reviewer);
                    ChecklistTemplate template = templates.get(t);
                    int score = results.stream()
                            .filter(r -> r.getChecklistTemplate().getId().equals(template.getId()))
                            .map(ChecklistResult::getScore)
                            .findFirst().orElse(0);

                    // X = đạt, (trống) = không đạt, N/A = cân nhắc
                    String display = score == 1 ? "X" : (score == 0 ? "N/A" : "");
                    row.createCell(i + 1).setCellValue(display);
                }
            }
        }

        // Kết quả row
        int resultRow = templates.size() + 1;
        Row resRow = sheet.createRow(resultRow);
        resRow.createCell(0).setCellValue("Kết quả");
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            List<TopicReviewer> reviewers = topicReviewerRepository.findByTopic(topic);
            final int order = reviewerOrder;
            TopicReviewer reviewer = reviewers.stream()
                    .filter(tr -> tr.getReviewerOrder() == order)
                    .findFirst().orElse(null);
            if (reviewer != null && reviewer.getDecision() != null) {
                resRow.createCell(i + 1).setCellValue(reviewer.getDecision().name());
            }
        }

        // Comment row
        Row commentRow = sheet.createRow(resultRow + 1);
        commentRow.createCell(0).setCellValue("Nhận xét");
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            List<TopicReviewer> reviewers = topicReviewerRepository.findByTopic(topic);
            final int order = reviewerOrder;
            TopicReviewer reviewer = reviewers.stream()
                    .filter(tr -> tr.getReviewerOrder() == order)
                    .findFirst().orElse(null);
            if (reviewer != null && reviewer.getComment() != null) {
                commentRow.createCell(i + 1).setCellValue(reviewer.getComment());
            }
        }

        sheet.autoSizeColumn(0);
    }

    private void createStatisticsSheet(Workbook workbook, List<Topic> topics,
            Semester semester, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Thống kê");

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Thống kê - " + semester.getName() + " (" + semester.getCode() + ")");
        titleCell.setCellStyle(headerStyle);

        long totalPass = topics.stream().filter(t -> t.getStatus().name().equals("PASS")
                || t.getStatus().name().equals("LOCKED")).count();
        long totalFail = topics.stream().filter(t -> t.getStatus().name().equals("FAIL")).count();
        long totalConsider = topics.stream().filter(t -> t.getStatus().name().equals("CONSIDER")).count();

        int row = 2;
        createStatRow(sheet, row++, "Tổng số đề tài", topics.size(), headerStyle);
        createStatRow(sheet, row++, "PASS", (int) totalPass, headerStyle);
        createStatRow(sheet, row++, "FAIL", (int) totalFail, headerStyle);
        createStatRow(sheet, row, "CONSIDER", (int) totalConsider, headerStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createStatRow(Sheet sheet, int rowNum, String label, int value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        row.createCell(1).setCellValue(value);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStatusStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(color.getIndex());
        style.setFont(font);
        return style;
    }
}
