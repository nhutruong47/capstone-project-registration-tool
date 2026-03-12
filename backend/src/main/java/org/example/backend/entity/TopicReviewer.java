package org.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.example.backend.enums.ReviewStatus;
import org.example.backend.enums.TopicStatus;

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

    @Column(nullable = false)
    @Builder.Default
    private Integer reviewerOrder = 1; // 1 = Reviewer 1, 2 = Reviewer 2, 3 = Reviewer thứ 3

    private Integer totalScore; // Tổng điểm từ checklist

    // Kết quả quyết định của reviewer dựa trên totalScore
    @Enumerated(EnumType.STRING)
    private TopicStatus decision; // PASS / FAIL / CONSIDER

    @Column(columnDefinition = "TEXT")
    private String checklistDetails; // Dữ liệu chi tiết checklist (JSON)

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
        if (reviewerOrder == null) {
            reviewerOrder = 1;
        }
    }
}
