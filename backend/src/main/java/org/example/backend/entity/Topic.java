package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.enums.TopicStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // VD: SP26-SE005

    @Column(nullable = false)
    private String titleEn; // Tiêu đề tiếng Anh

    @Column(nullable = false)
    private String titleVi; // Tiêu đề tiếng Việt

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description; // Mô tả chi tiết

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String requirements; // Yêu cầu kỹ thuật

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicStatus status;

    @Builder.Default
    @Column(nullable = false)
    private Integer maxTeams = 1; // Số nhóm tối đa có thể đăng ký

    @Builder.Default
    private Integer version = 1; // Phiên bản (tăng khi sửa đổi)

    // AI Screening Results
    private Boolean aiCompliancePass;
    private String aiComplianceFeedback;
    private Double aiSimilarityScore;
    private String aiSimilarityDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private User supervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime submittedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TopicStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
