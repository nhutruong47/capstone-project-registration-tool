package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.entity.User;
import org.example.backend.enums.UserRole;
import org.example.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "1. Authentication")
@Tag(name = "2. Admin")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng ký tài khoản mới", tags = {"1. Authentication"})
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String fullName = request.get("fullName");
            UserRole role = UserRole.valueOf(request.getOrDefault("role", "STUDENT"));

            User user = authService.register(email, password, fullName, role);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Đăng nhập hệ thống", tags = {"1. Authentication"})
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            return authService.login(email, password)
                    .map(user -> ResponseEntity.ok(Map.of(
                            "message", "Login successful",
                            "user", user)))
                    .orElse(ResponseEntity.status(401)
                            .body(Map.of("error", "Invalid credentials")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Lấy danh sách tất cả người dùng", tags = {"2. Admin"})
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.findAll());
    }

    @Operation(summary = "Lấy thông tin người dùng theo ID", tags = {"2. Admin"})
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return authService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lấy danh sách người dùng theo vai trò", tags = {"2. Admin"})
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(authService.findByRole(role));
    }

    @Operation(summary = "Cập nhật vai trò người dùng", tags = {"2. Admin"})
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            UserRole role = UserRole.valueOf(request.get("role"));
            User user = authService.updateRole(id, role);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Xóa người dùng", tags = {"2. Admin"})
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            authService.delete(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}