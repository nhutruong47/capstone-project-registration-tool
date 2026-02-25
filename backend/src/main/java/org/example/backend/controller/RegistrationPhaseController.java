package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.RegistrationPhase;
import org.example.backend.service.RegistrationPhaseService;
import org.example.backend.service.SemesterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/registration-phases")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RegistrationPhaseController {

    private final RegistrationPhaseService registrationPhaseService;
    private final SemesterService semesterService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        try {
            Long semesterId = Long.valueOf(request.get("semesterId").toString());
            String name = (String) request.get("name");
            LocalDateTime startDate = LocalDateTime.parse((String) request.get("startDate"));
            LocalDateTime endDate = LocalDateTime.parse((String) request.get("endDate"));

            RegistrationPhase phase = registrationPhaseService.create(semesterId, name, startDate, endDate);
            return ResponseEntity.ok(phase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return registrationPhaseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<?> getBySemester(@PathVariable Long semesterId) {
        return semesterService.findById(semesterId)
                .map(semester -> ResponseEntity.ok(registrationPhaseService.findBySemester(semester)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/semester/{semesterId}/open")
    public ResponseEntity<?> getOpenPhases(@PathVariable Long semesterId) {
        return semesterService.findById(semesterId)
                .map(semester -> ResponseEntity.ok(registrationPhaseService.findOpenPhasesBySemester(semester)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable Long id) {
        try {
            RegistrationPhase phase = registrationPhaseService.closePhase(id);
            return ResponseEntity.ok(phase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<?> open(@PathVariable Long id) {
        try {
            RegistrationPhase phase = registrationPhaseService.openPhase(id);
            return ResponseEntity.ok(phase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            registrationPhaseService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Registration phase deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
