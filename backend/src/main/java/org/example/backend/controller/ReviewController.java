package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Review;
import org.example.backend.entity.Topic;
import org.example.backend.enums.ReviewDecision;
import org.example.backend.enums.TopicStatus;
import org.example.backend.service.AuthService;
import org.example.backend.service.ReviewService;
import org.example.backend.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;
    private final TopicService topicService;
    private final AuthService authService;

    @GetMapping("/assigned/{reviewerId}")
    public ResponseEntity<?> getAssignedReviews(@PathVariable Long reviewerId) {
        return authService.findById(reviewerId)
                .map(reviewer -> ResponseEntity.ok(reviewService.findPendingByReviewer(reviewer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<?> getReviewsByTopic(@PathVariable Long topicId) {
        return topicService.findById(topicId)
                .map(topic -> ResponseEntity.ok(reviewService.findByTopic(topic)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return reviewService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/assign/{topicId}")
    public ResponseEntity<?> assignReviewers(@PathVariable Long topicId,
            @RequestBody Map<String, Integer> request) {
        try {
            int numberOfReviewers = request.getOrDefault("numberOfReviewers", 2);
            List<Review> reviews = reviewService.assignReviewers(topicId, numberOfReviewers);
            return ResponseEntity.ok(Map.of(
                    "message", "Reviewers assigned successfully",
                    "reviews", reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{reviewId}/submit")
    public ResponseEntity<?> submitReview(@PathVariable Long reviewId,
            @RequestBody Map<String, String> request) {
        try {
            ReviewDecision decision = ReviewDecision.valueOf(request.get("decision"));
            String comment = request.get("comment");

            Review review = reviewService.submitReview(reviewId, decision, comment);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/coordinator-decision/{topicId}")
    public ResponseEntity<?> coordinatorDecision(@PathVariable Long topicId,
            @RequestBody Map<String, String> request) {
        try {
            TopicStatus decision = TopicStatus.valueOf(request.get("decision"));
            String reason = request.get("reason");

            Topic topic = reviewService.coordinatorDecision(topicId, decision, reason);
            return ResponseEntity.ok(Map.of(
                    "message", "Coordinator decision applied",
                    "topic", topic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            reviewService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
