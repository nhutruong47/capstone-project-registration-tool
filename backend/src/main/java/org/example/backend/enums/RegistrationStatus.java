package org.example.backend.enums;

public enum RegistrationStatus {
    PENDING, // Chờ Supervisor duyệt
    APPROVED, // Supervisor đã duyệt
    REJECTED, // Supervisor từ chối
    FINALIZED // Hoàn tất, đề tài được gán
}
