package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Topic;
import org.example.backend.entity.User;
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

    /**
     * Giảng viên nộp đề tài mới
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        try {
            Long supervisorId = Long.valueOf(request.get("supervisorId").toString());
            Long semesterId = Long.valueOf(request.get("semesterId").toString());
            Long registrationPhaseId = Long.valueOf(request.get("registrationPhaseId").toString());
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String majorPrefix = (String) request.getOrDefault("majorPrefix", "SE");
            String studentGroupInfo = (String) request.get("studentGroupInfo");

            User supervisor = authService.findById(supervisorId)
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));

            Topic topic = topicService.create(supervisor, semesterId, registrationPhaseId,
                    title, description, majorPrefix, studentGroupInfo);

            // Trigger AI similarity check
            aiService.checkSimilarityAsync(topic.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Topic submitted. AI similarity check in progress.",
                    "topic", topic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Nộp lại đề tài FAIL ở đợt 2
     */
    @PostMapping("/{parentTopicId}/resubmit")
    public ResponseEntity<?> resubmit(@PathVariable Long parentTopicId,
            @RequestBody Map<String, Object> request) {
        try {
            Long newPhaseId = Long.valueOf(request.get("registrationPhaseId").toString());
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String majorPrefix = (String) request.getOrDefault("majorPrefix", "SE");
            String studentGroupInfo = (String) request.get("studentGroupInfo");

            Topic childTopic = topicService.resubmit(parentTopicId, newPhaseId, title, description,
                    majorPrefix, studentGroupInfo);

            // Trigger AI similarity check for new version
            aiService.checkSimilarityAsync(childTopic.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Topic resubmitted successfully",
                    "topic", childTopic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Moderator khóa kết quả đề tài
     */
    @PostMapping("/{id}/lock")
    public ResponseEntity<?> lockTopic(@PathVariable Long id) {
        try {
            Topic topic = topicService.lockTopic(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Topic locked successfully",
                    "topic", topic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy tất cả đề tài
     */
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(topicService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return topicService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        return topicService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/supervisor/{supervisorId}")
    public ResponseEntity<?> getBySupervisor(@PathVariable Long supervisorId) {
        return authService.findById(supervisorId)
                .map(supervisor -> ResponseEntity.ok(topicService.findBySupervisor(supervisor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<?> getBySemester(@PathVariable Long semesterId) {
        return semesterService.findById(semesterId)
                .map(semester -> ResponseEntity.ok(topicService.findBySemester(semester)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy danh sách đề tài PASS để công bố cho sinh viên
     */
    @GetMapping("/semester/{semesterId}/passed")
    public ResponseEntity<?> getPassedTopics(@PathVariable Long semesterId) {
        return semesterService.findById(semesterId)
                .map(semester -> ResponseEntity.ok(topicService.findPassedTopicsBySemester(semester)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Topic>> getByStatus(@PathVariable TopicStatus status) {
        return ResponseEntity.ok(topicService.findByStatus(status));
    }

    @GetMapping("/{id}/inheritance")
    public ResponseEntity<?> getInheritanceHistory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(topicService.getInheritanceHistory(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String studentGroupInfo = (String) request.get("studentGroupInfo");

            Topic topic = topicService.update(id, title, description, studentGroupInfo);
            return ResponseEntity.ok(topic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

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
