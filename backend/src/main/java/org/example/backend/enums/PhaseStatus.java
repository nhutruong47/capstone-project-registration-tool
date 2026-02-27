package org.example.backend.enums;

public enum PhaseStatus {
    OPEN, // Đợt đăng ký đang mở (nhận đề tài)
    CLOSED, // Đợt đăng ký đã đóng (hết hạn nộp)
    REVIEWING // Đang chấm điểm (sau khi đóng nộp, trước khi công bố)
}
