package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Team;
import org.example.backend.entity.User;
import org.example.backend.service.AuthService;
import org.example.backend.service.SemesterService;
import org.example.backend.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamService teamService;
    private final AuthService authService;
    private final SemesterService semesterService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        try {
            Long leaderId = Long.valueOf(request.get("leaderId").toString());
            Long semesterId = Long.valueOf(request.get("semesterId").toString());
            String name = (String) request.get("name");

            User leader = authService.findById(leaderId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Team team = teamService.create(leader, semesterId, name);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllForActiveSemester() {
        return semesterService.getActiveSemester()
                .map(semester -> ResponseEntity.ok(teamService.findBySemester(semester)))
                .orElse(ResponseEntity.ok(List.of()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return teamService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invite/{inviteCode}")
    public ResponseEntity<?> getByInviteCode(@PathVariable String inviteCode) {
        return teamService.findByInviteCode(inviteCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-team/{userId}")
    public ResponseEntity<?> getMyTeam(@PathVariable Long userId) {
        return authService.findById(userId)
                .flatMap(user -> semesterService.getActiveSemester()
                        .flatMap(semester -> teamService.findByUserAndSemester(user, semester)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinByInviteCode(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String inviteCode = (String) request.get("inviteCode");

            User user = authService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Team team = teamService.joinByInviteCode(user, inviteCode);
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully joined team",
                    "team", team));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{teamId}/kick/{userId}")
    public ResponseEntity<?> kickMember(@PathVariable Long teamId, @PathVariable Long userId,
            @RequestBody Map<String, Long> request) {
        try {
            Long requestingUserId = request.get("requestingUserId");
            User requestingUser = authService.findById(requestingUserId)
                    .orElseThrow(() -> new RuntimeException("Requesting user not found"));

            teamService.kickMember(teamId, userId, requestingUser);
            return ResponseEntity.ok(Map.of("message", "Member kicked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{teamId}/leave")
    public ResponseEntity<?> leaveTeam(@PathVariable Long teamId, @RequestBody Map<String, Long> request) {
        try {
            Long userId = request.get("userId");
            User user = authService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            teamService.leaveTeam(teamId, user);
            return ResponseEntity.ok(Map.of("message", "Left team successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{teamId}/transfer-leadership")
    public ResponseEntity<?> transferLeadership(@PathVariable Long teamId,
            @RequestBody Map<String, Long> request) {
        try {
            Long currentLeaderId = request.get("currentLeaderId");
            Long newLeaderId = request.get("newLeaderId");

            User currentLeader = authService.findById(currentLeaderId)
                    .orElseThrow(() -> new RuntimeException("Current leader not found"));

            Team team = teamService.transferLeadership(teamId, newLeaderId, currentLeader);
            return ResponseEntity.ok(Map.of(
                    "message", "Leadership transferred successfully",
                    "team", team));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{teamId}/regenerate-invite-code")
    public ResponseEntity<?> regenerateInviteCode(@PathVariable Long teamId,
            @RequestBody Map<String, Long> request) {
        try {
            Long leaderId = request.get("leaderId");
            User leader = authService.findById(leaderId)
                    .orElseThrow(() -> new RuntimeException("Leader not found"));

            String newCode = teamService.regenerateInviteCode(teamId, leader);
            return ResponseEntity.ok(Map.of("inviteCode", newCode));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            teamService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Team deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
