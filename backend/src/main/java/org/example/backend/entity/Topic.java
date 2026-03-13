package org.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String code; // Mã đề tài: FA25SE001, SP26-SE005

    @Column(nullable = false)
    private String titleEn; // Tên đề tài tiếng Anh (hoặc Nhật)

    private String titleVi; // Tên đề tài tiếng Việt

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết

    private String department; // Bộ môn (SE, IS, IA...)

    @Column(columnDefinition = "TEXT")
    private String studentGroupInfo; // Thông tin nhóm sinh viên (JSON)

    private Integer studentCount; // Số lượng sinh viên

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TopicStatus status = TopicStatus.PENDING;

    private Integer totalScore; // Điểm tổng cuối cùng (Final Score)

    @Column(columnDefinition = "TEXT")
    private String finalNote; // Ghi chú cuối cùng

    @Builder.Default
    @Column(nullable = false)
    private Boolean conflict = false; // Có mâu thuẫn giữa reviewer không

    @Builder.Default
    @Column(nullable = false)
    private Boolean isLocked = false; // Moderator khóa kết quả

    // AI Similarity Check
    private Double aiSimilarityScore;
    @Column(columnDefinition = "TEXT")
    private String aiSimilarityDetails;

    // === Quan hệ ===

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer1_id", nullable = true)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User reviewer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer2_id", nullable = true)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User reviewer2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer3_id", nullable = true)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User reviewer3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = true)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User supervisor; // GVHD chính (có thể chưa có nếu sinh viên tự đề xuất)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor2_id", nullable = true)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User supervisor2; // GVHD phụ (nếu có)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "registrationPhases" })
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_phase_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "semester" })
    private RegistrationPhase registrationPhase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_topic_id", nullable = true)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Topic parentTopic; // Đề tài cha (nếu nộp lại đợt 2)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null)
            status = TopicStatus.PENDING;
        if (isLocked == null)
            isLocked = false;
        if (conflict == null)
            conflict = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
