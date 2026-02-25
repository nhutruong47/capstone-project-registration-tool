package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checklist_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Tên mục checklist

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết mục checklist

    @Builder.Default
    @Column(nullable = false)
    private Integer displayOrder = 0; // Thứ tự hiển thị
}
