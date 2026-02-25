package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.enums.ReviewStatus;
import org.example.backend.repository.ChecklistTemplateRepository;
import org.example.backend.service.AuthService;
import org.example.backend.service.ChecklistService;
import org.example.backend.service.TopicReviewerService;
import org.example.backend.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topic-reviewers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TopicReviewerController {

    private final TopicReviewerService topicReviewerService;
    private final TopicService topicService;
    private final AuthService authService;
    private final ChecklistService checklistService;
    private final ChecklistTemplateRepository checklistTemplateRepository;

    /**
     * Moderator phân công reviewer cho đề tài
     */
    @PostMapping("/assign/{topicId}")
    public ResponseEntity<?> assignReviewers(@PathVariable Long topicId,
            @RequestBody Map<String, Integer> request) {
        try {
            int numberOfReviewers = request.getOrDefault("numberOfReviewers", 2);
            List<TopicReviewer> reviewers = topicReviewerService.assignReviewers(topicId, numberOfReviewers);
            return ResponseEntity.ok(Map.of(
                    "message", "Reviewers assigned successfully",
                    "reviewers", reviewers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reviewer nộp đánh giá checklist cho đề tài
     * Request body: { "comment": "...", "scores": [{"checklistTemplateId": 1,
     * "score": 1}, ...] }
     */
    @PostMapping("/{topicReviewerId}/submit")
    public ResponseEntity<?> submitReview(@PathVariable Long topicReviewerId,
            @RequestBody Map<String, Object> request) {
        try {
            String comment = (String) request.get("comment");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> scores = (List<Map<String, Object>>) request.get("scores");

            List<ChecklistResult> checklistResults = new ArrayList<>();
            for (Map<String, Object> scoreEntry : scores) {
                Long templateId = Long.valueOf(scoreEntry.get("checklistTemplateId").toString());
                Integer score = Integer.valueOf(scoreEntry.get("score").toString());

                // Validate score: must be -1, 0, or 1
                if (score < -1 || score > 1) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Score must be -1, 0, or 1"));
                }

                ChecklistTemplate template = checklistTemplateRepository.findById(templateId)
                        .orElseThrow(() -> new RuntimeException("Checklist template not found: " + templateId));

                ChecklistResult result = ChecklistResult.builder()
                        .checklistTemplate(template)
                        .score(score)
                        .build();
                checklistResults.add(result);
            }

            TopicReviewer topicReviewer = topicReviewerService.submitReview(topicReviewerId, checklistResults, comment);
            return ResponseEntity.ok(topicReviewer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách reviewer của một đề tài
     */
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<?> getByTopic(@PathVariable Long topicId) {
        return topicService.findById(topicId)
                .map(topic -> ResponseEntity.ok(topicReviewerService.findByTopic(topic)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy danh sách đề tài cần reviewer đánh giá
     */
    @GetMapping("/reviewer/{reviewerId}/pending")
    public ResponseEntity<?> getPendingByReviewer(@PathVariable Long reviewerId) {
        return authService.findById(reviewerId)
                .map(reviewer -> ResponseEntity.ok(topicReviewerService.findPendingByReviewer(reviewer)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy tất cả đánh giá của một reviewer
     */
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<?> getByReviewer(@PathVariable Long reviewerId) {
        return authService.findById(reviewerId)
                .map(reviewer -> ResponseEntity.ok(topicReviewerService.findByReviewer(reviewer)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy chi tiết một topic-reviewer assignment
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return topicReviewerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy kết quả checklist của một topic-reviewer
     */
    @GetMapping("/{topicReviewerId}/checklist-results")
    public ResponseEntity<?> getChecklistResults(@PathVariable Long topicReviewerId) {
        return topicReviewerService.findById(topicReviewerId)
                .map(tr -> ResponseEntity.ok(checklistService.findResultsByTopicReviewer(tr)))
                .orElse(ResponseEntity.notFound().build());
    }
}
