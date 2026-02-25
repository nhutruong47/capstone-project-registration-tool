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
    private String code; // Mã đề tài duy nhất trong học kỳ, VD: SP26-SE005

    @Column(nullable = false)
    private String title; // Tên đề tài

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TopicStatus status = TopicStatus.PENDING;

    // AI Similarity Check
    private Double aiSimilarityScore; // Điểm tương đồng từ AI
    @Column(columnDefinition = "TEXT")
    private String aiSimilarityDetails; // Chi tiết kết quả kiểm tra trùng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User supervisor; // Giảng viên nộp đề tài

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "registrationPhases" })
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_phase_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "semester" })
    private RegistrationPhase registrationPhase; // Đợt đăng ký

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime submittedAt; // Ngày nộp

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TopicStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
