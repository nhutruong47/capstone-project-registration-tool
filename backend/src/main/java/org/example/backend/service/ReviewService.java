package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.enums.ReviewDecision;
import org.example.backend.enums.TopicStatus;
import org.example.backend.enums.UserRole;
import org.example.backend.repository.ReviewRepository;
import org.example.backend.repository.TopicRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Assign reviewers to a topic (called after AI screening passes)
     * Constraint: Reviewer ≠ Supervisor
     */
    public List<Review> assignReviewers(Long topicId, int numberOfReviewers) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        // Get all reviewers except the supervisor
        List<User> availableReviewers = userRepository.findByRole(UserRole.REVIEWER);
        availableReviewers.removeIf(r -> r.getId().equals(topic.getSupervisor().getId()));

        if (availableReviewers.size() < numberOfReviewers) {
            throw new RuntimeException("Not enough reviewers available");
        }

        // Shuffle and pick reviewers
        Collections.shuffle(availableReviewers);
        List<User> selectedReviewers = availableReviewers.subList(0, numberOfReviewers);

        List<Review> reviews = new ArrayList<>();
        for (User reviewer : selectedReviewers) {
            Review review = Review.builder()
                    .topic(topic)
                    .reviewer(reviewer)
                    .topicVersion(topic.getVersion())
                    .build();
            reviews.add(reviewRepository.save(review));

            // Notify reviewer
            notificationService.create(reviewer,
                    "New Topic Assigned for Review",
                    "You have been assigned to review topic: " + topic.getCode(),
                    "/reviews/" + topicId);
        }

        // Update topic status
        topic.setStatus(TopicStatus.PENDING_REVIEW);
        topicRepository.save(topic);

        return reviews;
    }

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> findByTopic(Topic topic) {
        return reviewRepository.findByTopic(topic);
    }

    public List<Review> findPendingByReviewer(User reviewer) {
        return reviewRepository.findPendingByReviewer(reviewer);
    }

    public Review submitReview(Long reviewId, ReviewDecision decision, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setDecision(decision);
        review.setComment(comment);
        review.setReviewedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        // Check if all reviews are completed and determine topic status
        evaluateTopicStatus(review.getTopic());

        return savedReview;
    }

    /**
     * Evaluate topic status based on review decisions
     * Decision Matrix:
     * - 2 APPROVED → APPROVED
     * - 2 REJECTED → REJECTED
     * - Otherwise → WAITING_COORDINATOR
     */
    private void evaluateTopicStatus(Topic topic) {
        List<Review> reviews = reviewRepository.findByTopicAndTopicVersion(topic, topic.getVersion());

        // Check if all reviews are completed
        boolean allCompleted = reviews.stream().allMatch(r -> r.getDecision() != null);
        if (!allCompleted || reviews.size() < 2) {
            return;
        }

        long approvedCount = reviews.stream()
                .filter(r -> r.getDecision() == ReviewDecision.APPROVED)
                .count();
        long rejectedCount = reviews.stream()
                .filter(r -> r.getDecision() == ReviewDecision.REJECTED)
                .count();

        TopicStatus newStatus;
        if (approvedCount == reviews.size()) {
            newStatus = TopicStatus.APPROVED;
        } else if (rejectedCount == reviews.size()) {
            newStatus = TopicStatus.REJECTED;
        } else {
            newStatus = TopicStatus.WAITING_COORDINATOR;
        }

        topic.setStatus(newStatus);
        if (newStatus == TopicStatus.APPROVED) {
            topic.setApprovedAt(LocalDateTime.now());
        }
        topicRepository.save(topic);

        // Notify supervisor
        notificationService.create(topic.getSupervisor(),
                "Topic Review Completed",
                "Your topic " + topic.getCode() + " review is completed. Status: " + newStatus,
                "/topics/" + topic.getId());
    }

    /**
     * Coordinator makes final decision for disputed topics
     */
    public Topic coordinatorDecision(Long topicId, TopicStatus decision, String reason) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (topic.getStatus() != TopicStatus.WAITING_COORDINATOR) {
            throw new RuntimeException("Topic is not waiting for coordinator decision");
        }

        if (decision != TopicStatus.APPROVED && decision != TopicStatus.REJECTED) {
            throw new RuntimeException("Invalid decision. Must be APPROVED or REJECTED");
        }

        topic.setStatus(decision);
        if (decision == TopicStatus.APPROVED) {
            topic.setApprovedAt(LocalDateTime.now());
        }

        // Notify supervisor
        notificationService.create(topic.getSupervisor(),
                "Coordinator Decision",
                "Your topic " + topic.getCode() + " has been " + decision + ". Reason: " + reason,
                "/topics/" + topic.getId());

        return topicRepository.save(topic);
    }

    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }
}
