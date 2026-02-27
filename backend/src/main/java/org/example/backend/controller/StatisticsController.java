package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Dashboard thống kê theo semester
     * GET /api/statistics/semester/{semesterId}
     */
    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<?> getStatsBySemester(@PathVariable Long semesterId) {
        try {
            Map<String, Object> stats = statisticsService.getStatsBySemester(semesterId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
