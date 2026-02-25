package org.example.backend.enums;

public enum UserRole {
    ADMIN, // Quản trị viên hệ thống
    LECTURER, // Giảng viên (có thể vừa là supervisor vừa là reviewer)
    MODERATOR, // Điều phối viên (phân công reviewer, quản lý đợt đăng ký)
    STUDENT // Sinh viên
}
