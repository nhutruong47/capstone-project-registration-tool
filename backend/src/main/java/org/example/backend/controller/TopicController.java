package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Topic;
import org.example.backend.enums.TopicStatus;
import org.example.backend.service.AIService;
import org.example.backend.service.AuthService;
import org.example.backend.service.SemesterService;
import org.example.backend.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TopicController {

    private final TopicService topicService;
    private final AuthService authService;
    private final SemesterService semesterService;
    private final AIService aiService;
    private final org.example.backend.service.TopicReviewerService topicReviewerService;


    /**
     * Sinh viên tự nộp đề tài mới (không dùng file)
     */
    @Operation(summary = "Sinh viên nộp ý tưởng đề tài mới", tags = {"3. Student"})
    @PostMapping("/student-register")
    public ResponseEntity<?> createByStudent(@RequestBody Map<String, Object> request) {
        try {
            Long semesterId = Long.valueOf(request.get("semesterId").toString());
            Long registrationPhaseId = Long.valueOf(request.get("registrationPhaseId").toString());
            String titleEn = (String) request.get("titleEn");
            String titleVi = (String) request.get("titleVi");
            String description = (String) request.get("description");
            String department = (String) request.getOrDefault("department", "SE");
            String studentGroupInfo = (String) request.get("studentGroupInfo");
            Integer studentCount = request.get("studentCount") != null
                    ? Integer.valueOf(request.get("studentCount").toString())
                    : null;

            Topic topic = topicService.createByStudent(semesterId, registrationPhaseId,
                    titleEn, titleVi, description, department,
                    studentGroupInfo, studentCount);

            // Trigger AI comprehensive check (Compliance & Similarity)
            aiService.checkTopicAIAsync(topic.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Topic submitted successfully by student. AI similarity check in progress.",
                    "topic", topic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Nộp lại đề tài FAIL ở đợt 2
     */
    @Operation(summary = "Nộp lại đề tài (đối với đợt 2 hoặc khi bị từ chối)", tags = {"3. Student"})
    @PostMapping("/{parentTopicId}/resubmit")
    public ResponseEntity<?> resubmit(@PathVariable Long parentTopicId,
            @RequestBody Map<String, Object> request) {
        try {
            Long newPhaseId = Long.valueOf(request.get("registrationPhaseId").toString());
            String titleEn = (String) request.get("titleEn");
            String titleVi = (String) request.get("titleVi");
            String description = (String) request.get("description");
            String department = (String) request.get("department");
            String studentGroupInfo = (String) request.get("studentGroupInfo");

            Topic childTopic = topicService.resubmit(parentTopicId, newPhaseId, titleEn, titleVi,
                    description, department, studentGroupInfo);

            aiService.checkTopicAIAsync(childTopic.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Topic resubmitted successfully",
                    "topic", childTopic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Moderator chỉ định Reviewer 1 & 2
     */
    @Operation(summary = "Moderator phân công Reviewer 1 & 2", tags = {"5. Moderator"})
    @PutMapping("/{id}/assign-reviewers")
    public ResponseEntity<?> assignReviewers(@PathVariable Long id, @RequestBody List<Long> reviewerIds) {
        try {
            topicReviewerService.assignReviewers(id, reviewerIds);
            return ResponseEntity.ok(Map.of("message", "Reviewers assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Moderator chỉ định Reviewer 3 khi kết quả mâu thuẫn
     */
    @Operation(summary = "Moderator phân công Reviewer thứ 3 (Tie-breaker)", tags = {"5. Moderator"})
    @PutMapping("/{id}/assign-third-reviewer")
    public ResponseEntity<?> assignThirdReviewer(@PathVariable Long id, @RequestParam Long reviewerId) {
        try {
            topicReviewerService.assignThirdReviewer(id, reviewerId);
            return ResponseEntity.ok(Map.of("message", "Third reviewer assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Hoàn tất kết quả đề tài
     * Request: { "supervisorId": 1 } (optional)
     */
    @Operation(summary = "Moderator hoàn tất và chốt đề tài (Gán GVHD)", tags = {"5. Moderator"})
    @PostMapping("/{id}/finalize")
    public ResponseEntity<?> finalizeTopic(@PathVariable Long id, @RequestBody(required = false) Map<String, Long> request) {
        try {
            Long supervisorId = (request != null) ? request.get("supervisorId") : null;
            Topic topic = topicService.finalizeTopic(id, supervisorId);
            return ResponseEntity.ok(Map.of(
                    "message", "Topic finalized successfully",
                    "topic", topic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Lấy tất cả đề tài (Tất cả vai trò)", tags = {"6. General"})
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(topicService.findAll());
    }

    @Operation(summary = "Lấy chi tiết đề tài theo ID", tags = {"6. General"})
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return topicService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lấy đề tài theo mã số", tags = {"6. General"})
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        return topicService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lấy đề tài của một quản lý/giảng viên", tags = {"6. General"})
    @GetMapping("/supervisor/{supervisorId}")
    public ResponseEntity<?> getBySupervisor(@PathVariable Long supervisorId) {
        return authService.findById(supervisorId)
                .map(supervisor -> ResponseEntity.ok(topicService.findBySupervisor(supervisor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lấy đề tài theo học kỳ", tags = {"6. General"})
    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<?> getBySemester(@PathVariable Long semesterId) {
        return semesterService.findById(semesterId)
                .map(semester -> ResponseEntity.ok(topicService.findBySemester(semester)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lấy danh sách đề tài đã đạt (PASSED) theo học kỳ", tags = {"6. General"})
    @GetMapping("/semester/{semesterId}/passed")
    public ResponseEntity<?> getPassedTopics(@PathVariable Long semesterId) {
        return semesterService.findById(semesterId)
                .map(semester -> ResponseEntity.ok(topicService.findPassedTopicsBySemester(semester)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lấy đề tài theo trạng thái", tags = {"6. General"})
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Topic>> getByStatus(@PathVariable TopicStatus status) {
        return ResponseEntity.ok(topicService.findByStatus(status));
    }

    @Operation(summary = "Lấy lịch sử kế thừa/nộp lại của đề tài", tags = {"6. General"})
    @GetMapping("/{id}/inheritance")
    public ResponseEntity<?> getInheritanceHistory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(topicService.getInheritanceHistory(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Cập nhật thông tin đề tài", tags = {"3. Student"})
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String titleEn = (String) request.get("titleEn");
            String titleVi = (String) request.get("titleVi");
            String description = (String) request.get("description");
            String studentGroupInfo = (String) request.get("studentGroupInfo");

            Topic topic = topicService.update(id, titleEn, titleVi, description, studentGroupInfo);
            return ResponseEntity.ok(topic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Xóa đề tài", tags = {"2. Admin"})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            topicService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Topic deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
