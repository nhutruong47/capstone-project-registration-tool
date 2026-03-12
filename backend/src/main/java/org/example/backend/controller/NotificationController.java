package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Notification;
import org.example.backend.entity.User;
import org.example.backend.service.AuthService;
import org.example.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    @Operation(summary = "Lấy danh sách thông báo của người dùng", tags = {"6. General"})
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable Long userId) {
        return authService.findById(userId)
                .map(user -> ResponseEntity.ok(notificationService.findByUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lấy danh sách thông báo chưa đọc", tags = {"6. General"})
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<?> getUnreadByUser(@PathVariable Long userId) {
        return authService.findById(userId)
                .map(user -> ResponseEntity.ok(notificationService.findUnreadByUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lấy số lượng thông báo chưa đọc", tags = {"6. General"})
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long userId) {
        return authService.findById(userId)
                .map(user -> ResponseEntity.ok(Map.of("count", notificationService.countUnread(user))))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Đánh dấu một thông báo là đã đọc", tags = {"6. General"})
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            Notification notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Đánh dấu tất cả thông báo là đã đọc", tags = {"6. General"})
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
        try {
            User user = authService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            notificationService.markAllAsRead(user);
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Xóa thông báo", tags = {"6. General"})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            notificationService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
