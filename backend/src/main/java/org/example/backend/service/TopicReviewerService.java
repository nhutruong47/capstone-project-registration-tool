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
     * Moderator phân công reviewer cho đề tài
     * Ít nhất 2 reviewer, reviewer không được là supervisor của đề tài
     */
    public List<TopicReviewer> assignReviewers(Long topicId, int numberOfReviewers) {
        if (numberOfReviewers < 2) {
            throw new RuntimeException("Minimum 2 reviewers required");
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        // Lấy tất cả giảng viên (trừ supervisor của đề tài)
        List<User> availableLecturers = userRepository.findByRole(UserRole.LECTURER);
        availableLecturers.removeIf(l -> l.getId().equals(topic.getSupervisor().getId()));

        // Loại bỏ các reviewer đã được phân công
        List<TopicReviewer> existingReviewers = topicReviewerRepository.findByTopic(topic);
        List<Long> existingReviewerIds = existingReviewers.stream()
                .map(tr -> tr.getReviewer().getId())
                .toList();
        availableLecturers.removeIf(l -> existingReviewerIds.contains(l.getId()));

        if (availableLecturers.size() < numberOfReviewers) {
            throw new RuntimeException("Not enough lecturers available for review assignment");
        }

        // Shuffle và chọn reviewer
        Collections.shuffle(availableLecturers);
        List<User> selectedReviewers = availableLecturers.subList(0, numberOfReviewers);

        List<TopicReviewer> topicReviewers = new ArrayList<>();
        for (User reviewer : selectedReviewers) {
            TopicReviewer tr = TopicReviewer.builder()
                    .topic(topic)
                    .reviewer(reviewer)
                    .reviewStatus(ReviewStatus.NOT_STARTED)
                    .build();
            topicReviewers.add(topicReviewerRepository.save(tr));

            // Thông báo cho reviewer
            notificationService.create(reviewer,
                    "Đề tài cần đánh giá",
                    "Bạn được phân công đánh giá đề tài: " + topic.getCode() + " - " + topic.getTitle(),
                    "/reviews/" + topicId);
        }

        // Cập nhật trạng thái đề tài
        topic.setStatus(TopicStatus.IN_REVIEW);
        topicRepository.save(topic);

        return topicReviewers;
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

        // Lưu kết quả checklist
        for (ChecklistResult result : checklistResults) {
            result.setTopicReviewer(topicReviewer);
            checklistResultRepository.save(result);
        }

        // Tính tổng điểm
        Integer totalScore = checklistResultRepository.sumScoreByTopicReviewer(topicReviewer);

        topicReviewer.setTotalScore(totalScore);
        topicReviewer.setComment(comment);
        topicReviewer.setReviewStatus(ReviewStatus.COMPLETED);
        topicReviewer.setReviewedAt(LocalDateTime.now());

        TopicReviewer saved = topicReviewerRepository.save(topicReviewer);

        // Kiểm tra xem tất cả reviewer đã đánh giá chưa
        evaluateTopicStatus(topicReviewer.getTopic());

        return saved;
    }

    /**
     * Đánh giá trạng thái cuối cùng của đề tài
     * Tổng điểm > 0 → PASS
     * Tổng điểm < 0 → FAIL
     * Tổng điểm = 0 → CONSIDER
     */
    private void evaluateTopicStatus(Topic topic) {
        Long totalReviewers = topicReviewerRepository.countByTopic(topic);
        Long completedReviewers = topicReviewerRepository.countCompletedByTopic(topic);

        // Chưa đủ số reviewer hoàn thành
        if (completedReviewers < totalReviewers || totalReviewers < 2) {
            return;
        }

        // Tính tổng điểm từ tất cả reviewer
        List<TopicReviewer> reviewers = topicReviewerRepository.findByTopic(topic);
        int overallScore = reviewers.stream()
                .filter(tr -> tr.getTotalScore() != null)
                .mapToInt(TopicReviewer::getTotalScore)
                .sum();

        TopicStatus newStatus;
        if (overallScore > 0) {
            newStatus = TopicStatus.PASS;
        } else if (overallScore < 0) {
            newStatus = TopicStatus.FAIL;
        } else {
            newStatus = TopicStatus.CONSIDER;
        }

        topic.setStatus(newStatus);
        topicRepository.save(topic);

        // Thông báo cho supervisor
        notificationService.create(topic.getSupervisor(),
                "Kết quả đánh giá đề tài",
                "Đề tài " + topic.getCode() + " đã được đánh giá. Kết quả: " + newStatus,
                "/topics/" + topic.getId());
    }

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
}
