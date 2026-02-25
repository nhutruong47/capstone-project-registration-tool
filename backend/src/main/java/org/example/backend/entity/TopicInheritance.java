package org.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "topic_inheritances", uniqueConstraints = @UniqueConstraint(columnNames = { "parent_topic_id",
        "child_topic_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicInheritance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_topic_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Topic parentTopic; // Đề tài gốc (đợt 1)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_topic_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Topic childTopic; // Đề tài nộp lại (đợt 2)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
