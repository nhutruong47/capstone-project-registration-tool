package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.service.AuthService;
import org.example.backend.service.TopicReviewerService;
import org.example.backend.service.TopicService;
import org.example.backend.enums.TopicStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Moderator phân công reviewer cho đề tài (chỉ định cụ thể)
     * Request: { "reviewerIds": [1, 2] }
     */
    @Operation(summary = "Moderator phân công Reviewer", tags = {"5. Moderator"})
    @PostMapping("/assign/{topicId}")
    public ResponseEntity<?> assignReviewers(@PathVariable Long topicId,
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> rawIds = (List<Integer>) request.get("reviewerIds");
            List<Long> reviewerIds = rawIds.stream().map(Integer::longValue).toList();

            List<TopicReviewer> reviewers = topicReviewerService.assignReviewers(topicId, reviewerIds);
            return ResponseEntity.ok(Map.of(
                    "message", "Reviewers assigned successfully",
                    "reviewers", reviewers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Moderator chỉ định Reviewer thứ 3 khi mâu thuẫn
     * Request: { "reviewerId": 5 }
     */
    @Operation(summary = "Moderator phân công Reviewer thứ 3", tags = {"5. Moderator"})
    @PostMapping("/assign-third/{topicId}")
    public ResponseEntity<?> assignThirdReviewer(@PathVariable Long topicId,
            @RequestBody Map<String, Long> request) {
        try {
            Long reviewerId = request.get("reviewerId");
            TopicReviewer thirdReviewer = topicReviewerService.assignThirdReviewer(topicId, reviewerId);
            return ResponseEntity.ok(Map.of(
                    "message", "Third reviewer assigned successfully",
                    "reviewer", thirdReviewer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reviewer nộp đánh giá cho đề tài kèm Checklist
     * Request: { "comment": "...", "decision": "APPROVED", "totalScore": 8, "checklistDetails": "{...}" }
     */
    @Operation(summary = "Reviewer nộp kết quả đánh giá", tags = {"4. Reviewer"})
    @PostMapping("/{topicReviewerId}/submit")
    public ResponseEntity<?> submitReview(@PathVariable Long topicReviewerId,
            @RequestBody Map<String, Object> request) {
        try {
            String comment = (String) request.get("comment");
            String decisionStr = (String) request.get("decision");
            TopicStatus decision = TopicStatus.valueOf(decisionStr);
            Integer totalScore = (Integer) request.get("totalScore");
            String checklistDetails = (String) request.get("checklistDetails");

            TopicReviewer topicReviewer = topicReviewerService.submitReview(topicReviewerId, decision, comment, totalScore, checklistDetails);
            return ResponseEntity.ok(topicReviewer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách đề tài cần phân công Reviewer thứ 3
     */
    @Operation(summary = "Lấy danh sách đề tài cần phân công Reviewer thứ 3", tags = {"5. Moderator"})
    @GetMapping("/need-third-reviewer")
    public ResponseEntity<?> getTopicsNeedingThirdReviewer() {
        return ResponseEntity.ok(topicReviewerService.findTopicsNeedingThirdReviewer());
    }

    /**
     * Lấy danh sách reviewer của một đề tài
     */
    @Operation(summary = "Lấy danh sách Reviewer của một đề tài", tags = {"5. Moderator"})
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<?> getByTopic(@PathVariable Long topicId) {
        return topicService.findById(topicId)
                .map(topic -> ResponseEntity.ok(topicReviewerService.findByTopic(topic)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy danh sách đề tài cần reviewer đánh giá
     */
    @Operation(summary = "Lấy danh sách đề tài đang chờ Reviewer chấm", tags = {"4. Reviewer"})
    @GetMapping("/reviewer/{reviewerId}/pending")
    public ResponseEntity<?> getPendingByReviewer(@PathVariable Long reviewerId) {
        return authService.findById(reviewerId)
                .map(reviewer -> ResponseEntity.ok(topicReviewerService.findPendingByReviewer(reviewer)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy tất cả đánh giá của một reviewer
     */
    @Operation(summary = "Lấy tất cả lịch sử chấm của một Reviewer", tags = {"4. Reviewer"})
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<?> getByReviewer(@PathVariable Long reviewerId) {
        return authService.findById(reviewerId)
                .map(reviewer -> ResponseEntity.ok(topicReviewerService.findByReviewer(reviewer)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy chi tiết một topic-reviewer assignment
     */
    @Operation(summary = "Lấy thông tin chi tiết một lượt phân công chấm", tags = {"5. Moderator"})
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return topicReviewerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy thống kê số lượt chấm của tất cả giảng viên trong một học kỳ
     */
    @Operation(summary = "Thống kê lượt chấm của Reviewer theo kỳ", tags = {"5. Moderator"})
    @GetMapping("/stats/{semesterId}")
    public ResponseEntity<?> getReviewerStats(@PathVariable Long semesterId) {
        try {
            return ResponseEntity.ok(topicReviewerService.getReviewerStats(semesterId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
