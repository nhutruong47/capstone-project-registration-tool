package org.example.backend.enums;

public enum TopicStatus {
    PENDING,             // Sinh viên vừa nộp đề xuất
    WAITING_MODERATOR,   // Đã qua AI lọc, chờ Moderator phân công người chấm
    IN_REVIEW,           // Đang được 2 giảng viên Reviewer chấm
    NEED_THIRD_REVIEWER, // Lệch ý kiến, chờ Moderator gán Giám khảo 3
    APPROVED,            // Mức độ duyệt thành công sau review
    REJECTED,            // Đề tài bị từ chối/Bắt sửa lại
    FINALIZED,           // Hoàn tất (Sẵn sàng thực hiện/phân công GVHD)
    PASS                 // Legacy status for compatibility
}
