package org.example.backend.enums;

public enum ReviewDecision {
    APPROVED(1), // Đồng ý duyệt
    REJECTED(-1), // Từ chối
    CONSIDER(0); // Cần xem xét/sửa đổi

    private final int value;

    ReviewDecision(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
