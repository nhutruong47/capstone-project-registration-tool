package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Registration;
import org.example.backend.entity.Team;
import org.example.backend.entity.User;
import org.example.backend.service.AuthService;
import org.example.backend.service.RegistrationService;
import org.example.backend.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final TeamService teamService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody Map<String, Long> request) {
        try {
            Long teamId = request.get("teamId");
            Long topicId = request.get("topicId");

            Team team = teamService.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found"));

            Registration registration = registrationService.register(team, topicId);
            return ResponseEntity.ok(Map.of(
                    "message", "Registration submitted successfully",
                    "registration", registration));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return registrationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getByTeam(@PathVariable Long teamId) {
        return teamService.findById(teamId)
                .map(team -> ResponseEntity.ok(registrationService.findByTeam(team)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/supervisor/{supervisorId}")
    public ResponseEntity<?> getBySupervisor(@PathVariable Long supervisorId) {
        return authService.findById(supervisorId)
                .map(supervisor -> ResponseEntity.ok(registrationService.findBySupervisor(supervisor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/supervisor/{supervisorId}/pending")
    public ResponseEntity<?> getPendingBySupervisor(@PathVariable Long supervisorId) {
        return authService.findById(supervisorId)
                .map(supervisor -> ResponseEntity.ok(registrationService.findPendingBySupervisor(supervisor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        try {
            Long supervisorId = request.get("supervisorId");
            User supervisor = authService.findById(supervisorId)
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));

            Registration registration = registrationService.approve(id, supervisor);
            return ResponseEntity.ok(Map.of(
                    "message", "Registration approved",
                    "registration", registration));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Long supervisorId = Long.valueOf(request.get("supervisorId").toString());
            String reason = (String) request.get("reason");

            User supervisor = authService.findById(supervisorId)
                    .orElseThrow(() -> new RuntimeException("Supervisor not found"));

            Registration registration = registrationService.reject(id, supervisor, reason);
            return ResponseEntity.ok(Map.of(
                    "message", "Registration rejected",
                    "registration", registration));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/finalize")
    public ResponseEntity<?> finalize(@PathVariable Long id) {
        try {
            Registration registration = registrationService.finalize(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Registration finalized",
                    "registration", registration));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            registrationService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Registration deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
