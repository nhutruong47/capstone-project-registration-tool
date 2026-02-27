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
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TopicReviewerService {

    private final TopicReviewerRepository topicReviewerRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ChecklistResultRepository checklistResultRepository;
    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final NotificationService notificationService;

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

        // Kiểm tra không assign supervisor làm reviewer
        for (Long reviewerId : reviewerIds) {
            if (reviewerId.equals(topic.getSupervisor().getId())) {
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
                    .reviewerOrder(order++)
                    .reviewStatus(ReviewStatus.NOT_STARTED)
                    .build();
            topicReviewers.add(topicReviewerRepository.save(tr));

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
     * Phân công tự động (random) 2 reviewer cho đề tài
     */
    public List<TopicReviewer> autoAssignReviewers(Long topicId, int numberOfReviewers) {
        if (numberOfReviewers < 2) {
            throw new RuntimeException("Minimum 2 reviewers required");
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        // Lấy tất cả giảng viên (trừ supervisor)
        List<User> availableLecturers = new ArrayList<>(userRepository.findByRole(UserRole.LECTURER));
        availableLecturers.removeIf(l -> l.getId().equals(topic.getSupervisor().getId()));

        // Loại bỏ reviewer đã phân công
        List<TopicReviewer> existingReviewers = topicReviewerRepository.findByTopic(topic);
        List<Long> existingIds = existingReviewers.stream()
                .map(tr -> tr.getReviewer().getId())
                .toList();
        availableLecturers.removeIf(l -> existingIds.contains(l.getId()));

        if (availableLecturers.size() < numberOfReviewers) {
            throw new RuntimeException("Not enough lecturers available for review assignment");
        }

        Collections.shuffle(availableLecturers);
        List<Long> selectedIds = availableLecturers.subList(0, numberOfReviewers).stream()
                .map(User::getId)
                .toList();

        return assignReviewers(topicId, selectedIds);
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
        if (reviewer.getId().equals(topic.getSupervisor().getId())) {
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

        // Thông báo
        notificationService.create(reviewer,
                "Phân công đánh giá (Reviewer thứ 3)",
                "Bạn được chỉ định là reviewer thứ 3 cho đề tài: " + topic.getCode() + " - " + topic.getTitleEn() +
                        ". Kết quả đánh giá của bạn sẽ là quyết định cuối cùng.",
                "/reviews/" + topicId);

        return thirdReviewer;
    }

    /**
     * Reviewer nộp đánh giá checklist cho đề tài
     */
    public TopicReviewer submitReview(Long topicReviewerId, List<ChecklistResult> checklistResults, String comment) {
        TopicReviewer topicReviewer = topicReviewerRepository.findById(topicReviewerId)
                .orElseThrow(() -> new RuntimeException("TopicReviewer not found"));

        if (comment == null || comment.trim().isEmpty()) {
            throw new RuntimeException("Comment is required");
        }

        // Kiểm tra topic chưa bị khóa
        if (topicReviewer.getTopic().getIsLocked()) {
            throw new RuntimeException("Topic results are locked. Cannot submit review.");
        }

        // Lưu kết quả checklist
        for (ChecklistResult result : checklistResults) {
            result.setTopicReviewer(topicReviewer);
            checklistResultRepository.save(result);
        }

        // Tính tổng điểm
        Integer totalScore = checklistResultRepository.sumScoreByTopicReviewer(topicReviewer);

        // Xác định decision dựa trên totalScore
        TopicStatus decision;
        if (totalScore > 0) {
            decision = TopicStatus.PASS;
        } else if (totalScore < 0) {
            decision = TopicStatus.FAIL;
        } else {
            decision = TopicStatus.CONSIDER;
        }

        topicReviewer.setTotalScore(totalScore);
        topicReviewer.setDecision(decision);
        topicReviewer.setComment(comment);
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
        List<TopicReviewer> completedReviewers = allReviewers.stream()
                .filter(tr -> tr.getReviewStatus() == ReviewStatus.COMPLETED)
                .toList();

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
            topic.setStatus(finalStatus);
            topic.setTotalScore(r3.getTotalScore());
            topic.setFinalNote("Quyết định bởi Reviewer thứ 3: " + r3.getReviewer().getFullName());
            topicRepository.save(topic);

            notifyResult(topic, finalStatus);
            return;
        }

        // So sánh R1 và R2
        if (r1.getDecision() == r2.getDecision()) {
            // R1 == R2 → kết quả cuối
            TopicStatus finalStatus = r1.getDecision();
            int avgScore = (r1.getTotalScore() + r2.getTotalScore()) / 2;
            topic.setStatus(finalStatus);
            topic.setTotalScore(avgScore);
            topic.setFinalNote("R1 và R2 đồng ý: " + finalStatus);
            topicRepository.save(topic);

            notifyResult(topic, finalStatus);
        } else {
            // R1 ≠ R2 → cần R3
            topic.setStatus(TopicStatus.NEED_THIRD_REVIEWER);
            topic.setFinalNote("Mâu thuẫn: R1=" + r1.getDecision() + ", R2=" + r2.getDecision() +
                    ". Cần Reviewer thứ 3.");
            topicRepository.save(topic);

            // Thông báo cho supervisor
            notificationService.create(topic.getSupervisor(),
                    "Đề tài cần Reviewer thứ 3",
                    "Đề tài " + topic.getCode() + " có kết quả mâu thuẫn giữa 2 reviewer. " +
                            "Moderator sẽ chỉ định reviewer thứ 3.",
                    "/topics/" + topic.getId());
        }
    }

    private void notifyResult(Topic topic, TopicStatus finalStatus) {
        notificationService.create(topic.getSupervisor(),
                "Kết quả đánh giá đề tài",
                "Đề tài " + topic.getCode() + " đã được đánh giá. Kết quả: " + finalStatus,
                "/topics/" + topic.getId());
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
}
