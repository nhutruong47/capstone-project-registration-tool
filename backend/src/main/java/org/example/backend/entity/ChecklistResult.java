package org.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checklist_results", uniqueConstraints = @UniqueConstraint(columnNames = { "topic_reviewer_id",
        "checklist_template_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_reviewer_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private TopicReviewer topicReviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_template_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private ChecklistTemplate checklistTemplate;

    @Column(nullable = false)
    private Integer score; // 1 = Đạt, 0 = Cân nhắc, -1 = Không đạt
}
