package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "semesters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // VD: SP26, FA25

    @Column(nullable = false)
    private String name; // VD: Spring 2026, Fall 2025

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private LocalDateTime topicSubmissionOpen; // Thời gian mở nộp đề tài
    private LocalDateTime topicSubmissionClose; // Thời gian đóng nộp đề tài

    private LocalDateTime registrationOpen; // Thời gian mở đăng ký
    private LocalDateTime registrationClose; // Thời gian đóng đăng ký

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
