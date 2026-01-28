package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Semester;
import org.example.backend.service.SemesterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SemesterController {

    private final SemesterService semesterService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            String name = request.get("name");
            LocalDate startDate = LocalDate.parse(request.get("startDate"));
            LocalDate endDate = LocalDate.parse(request.get("endDate"));

            Semester semester = semesterService.create(code, name, startDate, endDate);
            return ResponseEntity.ok(semester);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Semester>> getAll() {
        return ResponseEntity.ok(semesterService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return semesterService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActive() {
        return semesterService.getActiveSemester()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        try {
            Semester semester = semesterService.setActive(id);
            return ResponseEntity.ok(semester);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/registration-period")
    public ResponseEntity<?> updateRegistrationPeriod(@PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            LocalDateTime open = LocalDateTime.parse(request.get("open"));
            LocalDateTime close = LocalDateTime.parse(request.get("close"));
            Semester semester = semesterService.updateRegistrationPeriod(id, open, close);
            return ResponseEntity.ok(semester);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/topic-submission-period")
    public ResponseEntity<?> updateTopicSubmissionPeriod(@PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            LocalDateTime open = LocalDateTime.parse(request.get("open"));
            LocalDateTime close = LocalDateTime.parse(request.get("close"));
            Semester semester = semesterService.updateTopicSubmissionPeriod(id, open, close);
            return ResponseEntity.ok(semester);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            semesterService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Semester deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
