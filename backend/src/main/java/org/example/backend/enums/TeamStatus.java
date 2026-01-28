package org.example.backend.enums;

public enum TeamStatus {
    FORMING, // Đang tìm thành viên (<4 SV)
    READY, // Đủ số lượng (4-5 SV)
    REGISTERED, // Đã đăng ký đề tài
    FINALIZED // Đã được gán đề tài chính thức
}
