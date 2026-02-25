package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.ChecklistResult;
import org.example.backend.entity.ChecklistTemplate;
import org.example.backend.entity.TopicReviewer;
import org.example.backend.repository.ChecklistResultRepository;
import org.example.backend.repository.ChecklistTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChecklistService {

    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final ChecklistResultRepository checklistResultRepository;

    // === ChecklistTemplate CRUD ===

    public ChecklistTemplate createTemplate(String name, String description, Integer displayOrder) {
        ChecklistTemplate template = ChecklistTemplate.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .build();
        return checklistTemplateRepository.save(template);
    }

    public List<ChecklistTemplate> findAllTemplates() {
        return checklistTemplateRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Optional<ChecklistTemplate> findTemplateById(Long id) {
        return checklistTemplateRepository.findById(id);
    }

    public ChecklistTemplate updateTemplate(Long id, String name, String description, Integer displayOrder) {
        ChecklistTemplate template = checklistTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Checklist template not found"));
        template.setName(name);
        template.setDescription(description);
        if (displayOrder != null) {
            template.setDisplayOrder(displayOrder);
        }
        return checklistTemplateRepository.save(template);
    }

    public void deleteTemplate(Long id) {
        checklistTemplateRepository.deleteById(id);
    }

    // === ChecklistResult queries ===

    public List<ChecklistResult> findResultsByTopicReviewer(TopicReviewer topicReviewer) {
        return checklistResultRepository.findByTopicReviewer(topicReviewer);
    }

    public Integer calculateTotalScore(TopicReviewer topicReviewer) {
        return checklistResultRepository.sumScoreByTopicReviewer(topicReviewer);
    }
}
