package org.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.backend.enums.ReviewStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "topic_reviewers", uniqueConstraints = @UniqueConstraint(columnNames = { "topic_id", "reviewer_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicReviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User reviewer;

    private Integer totalScore; // Tổng điểm từ checklist

    @Column(columnDefinition = "TEXT")
    private String comment; // Nhận xét tổng kết

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus reviewStatus = ReviewStatus.NOT_STARTED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }
}
