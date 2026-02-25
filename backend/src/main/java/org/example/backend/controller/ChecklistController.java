package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.ChecklistTemplate;
import org.example.backend.service.ChecklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChecklistController {

    private final ChecklistService checklistService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Integer displayOrder = request.get("displayOrder") != null
                    ? Integer.valueOf(request.get("displayOrder").toString())
                    : 0;

            ChecklistTemplate template = checklistService.createTemplate(name, description, displayOrder);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(checklistService.findAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return checklistService.findTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Integer displayOrder = request.get("displayOrder") != null
                    ? Integer.valueOf(request.get("displayOrder").toString())
                    : null;

            ChecklistTemplate template = checklistService.updateTemplate(id, name, description, displayOrder);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            checklistService.deleteTemplate(id);
            return ResponseEntity.ok(Map.of("message", "Checklist template deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
