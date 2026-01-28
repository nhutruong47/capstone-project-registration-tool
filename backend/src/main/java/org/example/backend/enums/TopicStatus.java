package org.example.backend.enums;

public enum TopicStatus {
    DRAFT, // Bản nháp
    PROCESSING, // Đang xử lý AI
    AI_PASSED, // AI đã duyệt
    AI_FAILED, // AI từ chối
    PENDING_REVIEW, // Chờ peer review
    WAITING_COORDINATOR, // Chờ Coordinator quyết định
    APPROVED, // Đã duyệt
    REJECTED, // Bị từ chối
    PUBLISHED, // Đã công bố cho SV
    REGISTERED, // Đã có SV đăng ký
    FINALIZED // Hoàn tất
}
