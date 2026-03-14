package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.enums.ReviewStatus;
import org.example.backend.enums.TopicStatus;
import org.example.backend.enums.UserRole;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TopicReviewerService {

    private final TopicReviewerRepository topicReviewerRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SemesterRepository semesterRepository;

    /**
     * Moderator phân công 2 reviewer cho đề tài
     * Reviewer không được là supervisor của đề tài
     */
    public List<TopicReviewer> assignReviewers(Long topicId, List<Long> reviewerIds) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (reviewerIds == null || reviewerIds.size() < 2) {
            throw new RuntimeException("Minimum 2 reviewers required");
        }

        // Kiểm tra không assign supervisor làm reviewer (nếu đã có)
        for (Long reviewerId : reviewerIds) {
            if (topic.getSupervisor() != null && reviewerId.equals(topic.getSupervisor().getId())) {
                throw new RuntimeException("Supervisor cannot be assigned as reviewer for their own topic");
            }
        }

        // Loại bỏ reviewer đã được phân công
        List<TopicReviewer> existingReviewers = topicReviewerRepository.findByTopic(topic);
        List<Long> existingReviewerIds = existingReviewers.stream()
                .map(tr -> tr.getReviewer().getId())
                .toList();

        List<TopicReviewer> topicReviewers = new ArrayList<>();
        int order = existingReviewers.size() + 1;

        for (Long reviewerId : reviewerIds) {
            if (existingReviewerIds.contains(reviewerId)) {
                continue; // Đã assign rồi, bỏ qua
            }

            User reviewer = userRepository.findById(reviewerId)
                    .orElseThrow(() -> new RuntimeException("Reviewer not found: " + reviewerId));

            TopicReviewer tr = TopicReviewer.builder()
                    .topic(topic)
                    .reviewer(reviewer)
                    .reviewerOrder(order)
                    .reviewStatus(ReviewStatus.NOT_STARTED)
                    .build();
            topicReviewers.add(topicReviewerRepository.save(tr));

            // Set explicitly in Topic entity
            if (order == 1) {
                topic.setReviewer1(reviewer);
            } else if (order == 2) {
                topic.setReviewer2(reviewer);
            } else if (order == 3) {
                topic.setReviewer3(reviewer);
            }
            order++;

            // Thông báo cho reviewer
            notificationService.create(reviewer,
                    "Đề tài cần đánh giá",
                    "Bạn được phân công đánh giá đề tài: " + topic.getCode() + " - " + topic.getTitleEn(),
                    "/reviews/" + topicId);
        }

        // Cập nhật trạng thái đề tài
        topic.setStatus(TopicStatus.IN_REVIEW);
        topicRepository.save(topic);

        return topicReviewers;
    }

    /**
     * Moderator chỉ định Reviewer thứ 3 khi R1 và R2 mâu thuẫn
     */
    public TopicReviewer assignThirdReviewer(Long topicId, Long reviewerId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (topic.getStatus() != TopicStatus.NEED_THIRD_REVIEWER) {
            throw new RuntimeException("Topic does not need a third reviewer (status: " + topic.getStatus() + ")");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        // Kiểm tra reviewer không trùng với supervisor hoặc reviewer hiện tại
        if (topic.getSupervisor() != null && reviewer.getId().equals(topic.getSupervisor().getId())) {
            throw new RuntimeException("Supervisor cannot be a reviewer");
        }

        List<TopicReviewer> existingReviewers = topicReviewerRepository.findByTopic(topic);
        for (TopicReviewer tr : existingReviewers) {
            if (tr.getReviewer().getId().equals(reviewerId)) {
                throw new RuntimeException("This reviewer is already assigned to this topic");
            }
        }

        TopicReviewer thirdReviewer = TopicReviewer.builder()
                .topic(topic)
                .reviewer(reviewer)
                .reviewerOrder(3)
                .reviewStatus(ReviewStatus.NOT_STARTED)
                .build();
        thirdReviewer = topicReviewerRepository.save(thirdReviewer);

        topic.setReviewer3(reviewer);
        topic.setStatus(TopicStatus.IN_REVIEW); // Change back to IN_REVIEW from NEED_THIRD_REVIEWER
        topicRepository.save(topic);

        // Thông báo
        notificationService.create(reviewer,
                "Phân công đánh giá (Reviewer thứ 3)",
                "Bạn được chỉ định là reviewer thứ 3 cho đề tài: " + topic.getCode() + " - " + topic.getTitleEn() +
                        ". Kết quả đánh giá của bạn sẽ là quyết định cuối cùng.",
                "/reviews/" + topicId);

        return thirdReviewer;
    }

    /**
     * Reviewer nộp đánh giá cho đề tài kèm Checklist
     */
    public TopicReviewer submitReview(Long topicReviewerId, TopicStatus decision, String comment, Double totalScore, String checklistDetails) {
        TopicReviewer topicReviewer = topicReviewerRepository.findById(topicReviewerId)
                .orElseThrow(() -> new RuntimeException("TopicReviewer not found"));

        return processReview(topicReviewer, decision, comment, totalScore, checklistDetails);
    }

    /**
     * Nộp đánh giá dựa trên topicId và reviewerId (Khớp spec của user)
     */
    public TopicReviewer submitReviewByTopicAndReviewer(Long topicId, Long reviewerId, TopicStatus decision, String comment, Double totalScore, String checklistDetails) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        TopicReviewer topicReviewer = topicReviewerRepository.findByTopicAndReviewer(topic, reviewer)
                .orElseThrow(() -> new RuntimeException("No review assignment found for this reviewer on this topic"));

        return processReview(topicReviewer, decision, comment, totalScore, checklistDetails);
    }

    private TopicReviewer processReview(TopicReviewer topicReviewer, TopicStatus decision, String comment, Double totalScore, String checklistDetails) {
        if (decision != TopicStatus.APPROVED && decision != TopicStatus.REJECTED) {
            throw new RuntimeException("Decision must be either APPROVED or REJECTED");
        }

        // Kiểm tra topic chưa bị khóa
        if (topicReviewer.getTopic().getIsLocked()) {
            throw new RuntimeException("Topic results are locked. Cannot submit review.");
        }

        topicReviewer.setTotalScore(totalScore);
        topicReviewer.setDecision(decision);
        topicReviewer.setComment(comment);
        topicReviewer.setChecklistDetails(checklistDetails);
        topicReviewer.setReviewStatus(ReviewStatus.COMPLETED);
        topicReviewer.setReviewedAt(LocalDateTime.now());

        TopicReviewer saved = topicReviewerRepository.save(topicReviewer);

        // Kiểm tra và quyết định trạng thái đề tài
        evaluateTopicStatus(topicReviewer.getTopic());

        return saved;
    }

    /**
     * Logic quyết định trạng thái đề tài:
     * 1. Chờ tất cả reviewer (ít nhất 2) hoàn thành
     * 2. Nếu R1 == R2 → kết quả cuối = kết quả đó
     * 3. Nếu R1 ≠ R2 → NEED_THIRD_REVIEWER
     * 4. Khi R3 chấm → kết quả R3 = quyết định cuối cùng
     */
    private void evaluateTopicStatus(Topic topic) {
        List<TopicReviewer> allReviewers = topicReviewerRepository.findByTopic(topic);

        // Tìm R1, R2, R3
        TopicReviewer r1 = allReviewers.stream().filter(tr -> tr.getReviewerOrder() == 1).findFirst().orElse(null);
        TopicReviewer r2 = allReviewers.stream().filter(tr -> tr.getReviewerOrder() == 2).findFirst().orElse(null);
        TopicReviewer r3 = allReviewers.stream().filter(tr -> tr.getReviewerOrder() == 3).findFirst().orElse(null);

        // Cần ít nhất R1 và R2 hoàn thành
        if (r1 == null || r2 == null)
            return;
        if (r1.getReviewStatus() != ReviewStatus.COMPLETED || r2.getReviewStatus() != ReviewStatus.COMPLETED)
            return;

        // Trường hợp đã có R3
        if (r3 != null && r3.getReviewStatus() == ReviewStatus.COMPLETED) {
            // Kết quả R3 là quyết định cuối cùng
            TopicStatus finalStatus = r3.getDecision();
            topic.setStatus(finalStatus == TopicStatus.APPROVED ? TopicStatus.FINALIZED : TopicStatus.REJECTED);
            topic.setTotalScore(r3.getTotalScore());
            topic.setFinalNote("Quyết định bởi Reviewer thứ 3: " + r3.getReviewer().getFullName());
            if (finalStatus == TopicStatus.APPROVED) {
                topic.setIsLocked(true);
            }
            topicRepository.save(topic);

            notifyResult(topic, finalStatus);
            return;
        }

        // So sánh R1 và R2
        if (r1.getDecision() == r2.getDecision()) {
            // R1 == R2 → kết quả cuối
            TopicStatus finalStatus = r1.getDecision();
            Double scoreR1 = r1.getTotalScore() != null ? r1.getTotalScore() : 0.0;
            Double scoreR2 = r2.getTotalScore() != null ? r2.getTotalScore() : 0.0;
            Double avgScore = (scoreR1 + scoreR2) / 2.0;

            // Nếu đồng thuận Approve -> Chuyển thẳng sang FINALIZED (Step 7)
            topic.setStatus(finalStatus == TopicStatus.APPROVED ? TopicStatus.FINALIZED : TopicStatus.REJECTED);
            topic.setTotalScore(avgScore);
            topic.setFinalNote("R1 và R2 đồng ý: " + finalStatus);
            if (finalStatus == TopicStatus.APPROVED) {
                topic.setIsLocked(true);
            }
            topicRepository.save(topic);

            notifyResult(topic, finalStatus);
        } else {
            // R1 ≠ R2 → cần R3
            topic.setStatus(TopicStatus.NEED_THIRD_REVIEWER);
            topic.setFinalNote("Mâu thuẫn: R1=" + r1.getDecision() + ", R2=" + r2.getDecision() +
                    ". Cần Reviewer thứ 3.");
            topicRepository.save(topic);

            // Thông báo
            if (topic.getSupervisor() != null) {
                notificationService.create(topic.getSupervisor(),
                        "Đề tài cần Reviewer thứ 3",
                        "Đề tài " + topic.getCode() + " có kết quả mâu thuẫn giữa 2 reviewer. " +
                                "Moderator sẽ chỉ định reviewer thứ 3.",
                        "/topics/" + topic.getId());
            }
        }
    }

    private void notifyResult(Topic topic, TopicStatus finalStatus) {
        if (topic.getSupervisor() != null) {
            notificationService.create(topic.getSupervisor(),
                    "Kết quả đánh giá đề tài",
                    "Đề tài " + topic.getCode() + " đã được đánh giá. Kết quả: " + finalStatus,
                    "/topics/" + topic.getId());
        }
    }

    // === Query methods ===

    public List<TopicReviewer> findByTopic(Topic topic) {
        return topicReviewerRepository.findByTopic(topic);
    }

    public List<TopicReviewer> findPendingByReviewer(User reviewer) {
        return topicReviewerRepository.findPendingByReviewer(reviewer);
    }

    public List<TopicReviewer> findByReviewer(User reviewer) {
        return topicReviewerRepository.findByReviewer(reviewer);
    }

    public java.util.Optional<TopicReviewer> findById(Long id) {
        return topicReviewerRepository.findById(id);
    }

    /**
     * Lấy danh sách đề tài cần phân công Reviewer thứ 3
     */
    public List<Topic> findTopicsNeedingThirdReviewer() {
        return topicRepository.findByStatus(TopicStatus.NEED_THIRD_REVIEWER);
    }

    /**
     * Lấy thống kê số lượt chấm của tất cả giảng viên trong một học kỳ
     */
    public List<java.util.Map<String, Object>> getReviewerStats(Long semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        List<User> lecturers = userRepository.findByRole(UserRole.LECTURER);
        List<java.util.Map<String, Object>> stats = new ArrayList<>();

        for (User lecturer : lecturers) {
            Long count = topicReviewerRepository.countByReviewerAndTopicSemester(lecturer, semester);
            java.util.Map<String, Object> stat = new java.util.HashMap<>();
            stat.put("reviewer", lecturer);
            stat.put("assignmentCount", count);
            stats.add(stat);
        }

        return stats;
    }
}
