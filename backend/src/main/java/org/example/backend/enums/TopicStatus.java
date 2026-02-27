package org.example.backend.enums;

public enum TopicStatus {
    PENDING, // Chờ duyệt
    IN_REVIEW, // Đang review
    PASS, // Đạt
    FAIL, // Không đạt
    CONSIDER, // Cân nhắc
    NEED_THIRD_REVIEWER, // Cần giảng viên thứ 3 giải quyết mâu thuẫn
    LOCKED // Moderator đã khóa kết quả
}
