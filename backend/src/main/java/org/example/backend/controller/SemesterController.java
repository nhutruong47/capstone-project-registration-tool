package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Semester;
import org.example.backend.service.SemesterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SemesterController {

    private final SemesterService semesterService;

    @Operation(summary = "Tạo Học kỳ mới", description = "Ví dụ: Summer 2026", tags = {"2. Admin"})
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

    @Operation(summary = "Lấy danh sách tất cả học kỳ", tags = {"2. Admin"})
    @GetMapping
    public ResponseEntity<List<Semester>> getAll() {
        return ResponseEntity.ok(semesterService.findAll());
    }

    @Operation(summary = "Lấy chi tiết học kỳ theo ID", tags = {"2. Admin"})
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return semesterService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lấy học kỳ hiện tại đang hoạt động", tags = {"2. Admin"})
    @GetMapping("/active")
    public ResponseEntity<?> getActive() {
        return semesterService.getActiveSemester()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Kích hoạt học kỳ", tags = {"2. Admin"})
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        try {
            Semester semester = semesterService.setActive(id);
            return ResponseEntity.ok(semester);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Xóa học kỳ", tags = {"2. Admin"})
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
