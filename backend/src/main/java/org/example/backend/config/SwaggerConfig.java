package org.example.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Capstone Management System API")
                        .version("1.0.0")
                        .description("API documentation for Smart Capstone Management System - "
                                + "Hệ thống quản lý quy trình đề xuất và đăng ký đồ án tốt nghiệp")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("support@capstone.edu.vn"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .tags(List.of(
                        new io.swagger.v3.oas.models.tags.Tag().name("1. Authentication").description("Đăng nhập và Đăng ký tài khoản"),
                        new io.swagger.v3.oas.models.tags.Tag().name("2. Admin").description("Quản trị hệ thống: Quản lý người dùng, học kỳ, thời gian biểu"),
                        new io.swagger.v3.oas.models.tags.Tag().name("3. Student").description("Dành cho Sinh viên: Nộp đề tài, sửa đổi, theo dõi"),
                        new io.swagger.v3.oas.models.tags.Tag().name("4. Reviewer").description("Dành cho Giảng viên phản biện: Đánh giá đề tài"),
                        new io.swagger.v3.oas.models.tags.Tag().name("5. Moderator").description("Dành cho Moderator: Phân công, duyệt kết quả, thống kê"),
                        new io.swagger.v3.oas.models.tags.Tag().name("6. General").description("Thông tin chung: Tra cứu, thông báo")
                ))
                .servers(List.of(
                        new Server().url("https://capstone-project-registration-tool.onrender.com")
                                .description("Production Server (Render)"),
                        new Server().url("http://localhost:8080")
                                .description("Development Server (Local)")));
    }
}
