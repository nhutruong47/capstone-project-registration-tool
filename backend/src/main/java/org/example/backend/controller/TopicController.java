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

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        try {
            Long supervisorId = Long.valueOf(request.get("supervisorId").toString());
            Long semesterId = Long.valueOf(request.get("semesterId").toString());
            String titleEn = (String) request.get("titleEn");
            String titleVi = (String) request.get("titleVi");
            String description = (String) request.get("description");
            String requirements = (String) request.get("requirements");
            Integer maxTeams = request.get("maxTeams") != null
                    ? Integer.valueOf(request.get("maxTeams").toString())
                    : 1;
            String majorPrefix = (String) request.getOrDefault("majorPrefix", "SE");

            User supervisor = authService.findById(supervisorId)
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));

            Topic topic = topicService.create(supervisor, semesterId, titleEn, titleVi,
                    description, requirements, maxTeams, majorPrefix);
            return ResponseEntity.ok(topic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Topic>> getAll() {
        return ResponseEntity.ok(topicService.findByStatus(TopicStatus.PUBLISHED));
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

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableForRegistration() {
        return semesterService.getActiveSemester()
                .map(semester -> ResponseEntity.ok(topicService.findAvailableForRegistration(semester)))
                .orElse(ResponseEntity.ok(List.of()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Topic>> getByStatus(@PathVariable TopicStatus status) {
        return ResponseEntity.ok(topicService.findByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String titleEn = (String) request.get("titleEn");
            String titleVi = (String) request.get("titleVi");
            String description = (String) request.get("description");
            String requirements = (String) request.get("requirements");
            Integer maxTeams = request.get("maxTeams") != null
                    ? Integer.valueOf(request.get("maxTeams").toString())
                    : null;

            Topic topic = topicService.update(id, titleEn, titleVi, description, requirements, maxTeams);
            return ResponseEntity.ok(topic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id) {
        try {
            Topic topic = topicService.submit(id);
            // Trigger async AI processing
            aiService.processTopicAsync(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Topic submitted for AI processing",
                    "topic", topic));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(@PathVariable Long id) {
        try {
            Topic topic = topicService.updateStatus(id, TopicStatus.PUBLISHED);
            return ResponseEntity.ok(topic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/resubmit")
    public ResponseEntity<?> resubmit(@PathVariable Long id) {
        try {
            Topic topic = topicService.incrementVersion(id);
            // Trigger async AI processing for new version
            aiService.processTopicAsync(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Topic resubmitted for AI processing (Version " + topic.getVersion() + ")",
                    "topic", topic));
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
