package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.service.ExcelExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExcelExportController {

    private final ExcelExportService excelExportService;

    /**
     * Xuất file Excel kết quả theo semester
     * GET /api/export/semester/{semesterId}
     */
    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<?> exportBySemester(@PathVariable Long semesterId) {
        try {
            byte[] excelData = excelExportService.exportBySemester(semesterId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment",
                    "CapstoneResult_Semester_" + semesterId + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
