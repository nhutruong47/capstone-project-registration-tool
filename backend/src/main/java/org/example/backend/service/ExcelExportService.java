package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.backend.entity.*;
import org.example.backend.enums.ReviewStatus;
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
     * Xuất file Excel theo semester gồm 4 sheets:
     * 1. Danh sách đăng ký
     * 2. Chấm điểm từng reviewer
     * 3. Tổng hợp kết quả
     * 4. Thống kê
     */
    public byte[] exportBySemester(Long semesterId) throws IOException {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        List<Topic> topics = topicRepository.findBySemester(semester);
        List<ChecklistTemplate> templates = checklistTemplateRepository.findAllByOrderByDisplayOrderAsc();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Style cho header
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle passStyle = createStatusStyle(workbook, IndexedColors.GREEN);
            CellStyle failStyle = createStatusStyle(workbook, IndexedColors.RED);
            CellStyle considerStyle = createStatusStyle(workbook, IndexedColors.ORANGE);

            // Sheet 1: Danh sách đăng ký
            createRegistrationSheet(workbook, topics, headerStyle);

            // Sheet 2: Chấm điểm từng reviewer
            createReviewerScoreSheet(workbook, topics, templates, headerStyle);

            // Sheet 3: Tổng hợp kết quả
            createSummarySheet(workbook, topics, headerStyle, passStyle, failStyle, considerStyle);

            // Sheet 4: Thống kê
            createStatisticsSheet(workbook, topics, semester, headerStyle);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createRegistrationSheet(Workbook workbook, List<Topic> topics, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Đăng ký đề tài");

        // Header
        Row headerRow = sheet.createRow(0);
        String[] headers = { "STT", "Mã đề tài", "Tên đề tài", "Mô tả", "Giảng viên HD",
                "Đợt đăng ký", "Nhóm SV", "Ngày nộp", "Trạng thái" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        int rowNum = 1;
        for (Topic topic : topics) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(topic.getCode());
            row.createCell(2).setCellValue(topic.getTitle());
            row.createCell(3).setCellValue(topic.getDescription() != null ? topic.getDescription() : "");
            row.createCell(4).setCellValue(topic.getSupervisor().getFullName());
            row.createCell(5).setCellValue(topic.getRegistrationPhase().getName());
            row.createCell(6).setCellValue(topic.getStudentGroupInfo() != null ? topic.getStudentGroupInfo() : "");
            row.createCell(7).setCellValue(
                    topic.getSubmittedAt() != null ? topic.getSubmittedAt().toString() : "");
            row.createCell(8).setCellValue(topic.getStatus().name());
            rowNum++;
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createReviewerScoreSheet(Workbook workbook, List<Topic> topics,
            List<ChecklistTemplate> templates, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Chấm điểm Reviewer");

        // Header
        Row headerRow = sheet.createRow(0);
        int col = 0;
        headerRow.createCell(col++).setCellValue("Mã đề tài");
        headerRow.createCell(col++).setCellValue("Tên đề tài");
        headerRow.createCell(col++).setCellValue("Reviewer");
        headerRow.createCell(col++).setCellValue("Thứ tự");

        for (ChecklistTemplate t : templates) {
            headerRow.createCell(col++).setCellValue(t.getName());
        }
        headerRow.createCell(col++).setCellValue("Tổng điểm");
        headerRow.createCell(col++).setCellValue("Kết quả");
        headerRow.createCell(col).setCellValue("Ghi chú");

        // Apply header style
        for (int i = 0; i <= col; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }

        // Data
        int rowNum = 1;
        for (Topic topic : topics) {
            List<TopicReviewer> reviewers = topicReviewerRepository.findByTopic(topic);
            for (TopicReviewer reviewer : reviewers) {
                Row row = sheet.createRow(rowNum++);
                int c = 0;
                row.createCell(c++).setCellValue(topic.getCode());
                row.createCell(c++).setCellValue(topic.getTitle());
                row.createCell(c++).setCellValue(reviewer.getReviewer().getFullName());
                row.createCell(c++).setCellValue("R" + reviewer.getReviewerOrder());

                // Scores for each checklist item
                List<ChecklistResult> results = checklistResultRepository.findByTopicReviewer(reviewer);
                for (ChecklistTemplate template : templates) {
                    int score = results.stream()
                            .filter(r -> r.getChecklistTemplate().getId().equals(template.getId()))
                            .map(ChecklistResult::getScore)
                            .findFirst().orElse(0);
                    row.createCell(c++).setCellValue(score);
                }

                row.createCell(c++).setCellValue(
                        reviewer.getTotalScore() != null ? reviewer.getTotalScore() : 0);
                row.createCell(c++).setCellValue(
                        reviewer.getDecision() != null ? reviewer.getDecision().name() : "N/A");
                row.createCell(c).setCellValue(
                        reviewer.getComment() != null ? reviewer.getComment() : "");
            }
        }
    }

    private void createSummarySheet(Workbook workbook, List<Topic> topics, CellStyle headerStyle,
            CellStyle passStyle, CellStyle failStyle, CellStyle considerStyle) {
        Sheet sheet = workbook.createSheet("Tổng hợp kết quả");

        // Header
        Row headerRow = sheet.createRow(0);
        String[] headers = { "STT", "Mã đề tài", "Tên đề tài", "GV Hướng dẫn", "Đợt",
                "R1", "Kết quả R1", "R2", "Kết quả R2", "R3", "Kết quả R3",
                "Điểm tổng", "Kết quả cuối", "Ghi chú" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data
        int rowNum = 1;
        for (Topic topic : topics) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(topic.getCode());
            row.createCell(2).setCellValue(topic.getTitle());
            row.createCell(3).setCellValue(topic.getSupervisor().getFullName());
            row.createCell(4).setCellValue(topic.getRegistrationPhase().getName());

            List<TopicReviewer> reviewers = topicReviewerRepository.findByTopic(topic);
            TopicReviewer r1 = reviewers.stream().filter(r -> r.getReviewerOrder() == 1).findFirst().orElse(null);
            TopicReviewer r2 = reviewers.stream().filter(r -> r.getReviewerOrder() == 2).findFirst().orElse(null);
            TopicReviewer r3 = reviewers.stream().filter(r -> r.getReviewerOrder() == 3).findFirst().orElse(null);

            if (r1 != null) {
                row.createCell(5).setCellValue(r1.getReviewer().getFullName());
                row.createCell(6).setCellValue(
                        r1.getDecision() != null ? r1.getDecision().name() : "Chưa chấm");
            }
            if (r2 != null) {
                row.createCell(7).setCellValue(r2.getReviewer().getFullName());
                row.createCell(8).setCellValue(
                        r2.getDecision() != null ? r2.getDecision().name() : "Chưa chấm");
            }
            if (r3 != null) {
                row.createCell(9).setCellValue(r3.getReviewer().getFullName());
                row.createCell(10).setCellValue(
                        r3.getDecision() != null ? r3.getDecision().name() : "Chưa chấm");
            }

            row.createCell(11).setCellValue(topic.getTotalScore() != null ? topic.getTotalScore() : 0);

            Cell resultCell = row.createCell(12);
            resultCell.setCellValue(topic.getStatus().name());
            switch (topic.getStatus()) {
                case PASS -> resultCell.setCellStyle(passStyle);
                case FAIL -> resultCell.setCellStyle(failStyle);
                case CONSIDER -> resultCell.setCellStyle(considerStyle);
                default -> {
                }
            }

            row.createCell(13).setCellValue(topic.getFinalNote() != null ? topic.getFinalNote() : "");
            rowNum++;
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createStatisticsSheet(Workbook workbook, List<Topic> topics,
            Semester semester, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Thống kê");

        long totalPass = topics.stream().filter(t -> t.getStatus().name().equals("PASS")).count();
        long totalFail = topics.stream().filter(t -> t.getStatus().name().equals("FAIL")).count();
        long totalConsider = topics.stream().filter(t -> t.getStatus().name().equals("CONSIDER")).count();
        long totalPending = topics.stream().filter(t -> t.getStatus().name().equals("PENDING")).count();
        long totalInReview = topics.stream().filter(t -> t.getStatus().name().equals("IN_REVIEW")).count();
        long totalNeedR3 = topics.stream().filter(t -> t.getStatus().name().equals("NEED_THIRD_REVIEWER")).count();

        // Title
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Thống kê kết quả - " + semester.getName() + " (" + semester.getCode() + ")");
        titleCell.setCellStyle(headerStyle);

        // Stats
        int row = 2;
        createStatRow(sheet, row++, "Tổng số đề tài", topics.size(), headerStyle);
        createStatRow(sheet, row++, "PASS", (int) totalPass, headerStyle);
        createStatRow(sheet, row++, "FAIL", (int) totalFail, headerStyle);
        createStatRow(sheet, row++, "CONSIDER", (int) totalConsider, headerStyle);
        createStatRow(sheet, row++, "PENDING", (int) totalPending, headerStyle);
        createStatRow(sheet, row++, "IN_REVIEW", (int) totalInReview, headerStyle);
        createStatRow(sheet, row, "NEED_THIRD_REVIEWER", (int) totalNeedR3, headerStyle);

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
