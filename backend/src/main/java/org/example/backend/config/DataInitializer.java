package org.example.backend.config;

import org.example.backend.entity.*;
import org.example.backend.enums.PhaseStatus;
import org.example.backend.enums.TopicStatus;
import org.example.backend.enums.UserRole;
import org.example.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

        @Bean
        CommandLineRunner initDatabase(UserRepository userRepository,
                        SemesterRepository semesterRepository,
                        RegistrationPhaseRepository registrationPhaseRepository,
                        TopicRepository topicRepository,
                        ChecklistTemplateRepository checklistTemplateRepository) {
                return args -> {
                        if (userRepository.count() > 0) {
                                System.out.println("Database already initialized. Skipping seed data.");
                                return;
                        }

                        System.out.println("Initializing Database with Seed Data...");

                        // 1. Semester
                        Semester semester = Semester.builder()
                                        .code("FA25")
                                        .name("Fall 2025")
                                        .startDate(LocalDate.of(2025, 9, 1))
                                        .endDate(LocalDate.of(2025, 12, 31))
                                        .isActive(true)
                                        .build();
                        semesterRepository.save(semester);

                        // 2. Registration Phases
                        RegistrationPhase phase1 = RegistrationPhase.builder()
                                        .name("Đợt 1")
                                        .startDate(LocalDateTime.of(2025, 9, 1, 0, 0))
                                        .endDate(LocalDateTime.of(2025, 10, 1, 23, 59))
                                        .status(PhaseStatus.CLOSED)
                                        .semester(semester)
                                        .build();
                        registrationPhaseRepository.save(phase1);

                        RegistrationPhase phase2 = RegistrationPhase.builder()
                                        .name("Đợt 2")
                                        .startDate(LocalDateTime.of(2025, 10, 15, 0, 0))
                                        .endDate(LocalDateTime.of(2025, 11, 15, 23, 59))
                                        .status(PhaseStatus.OPEN)
                                        .semester(semester)
                                        .build();
                        registrationPhaseRepository.save(phase2);

                        // 3. Users
                        userRepository.save(User.builder()
                                        .email("admin@fpt.edu.vn").password("12345")
                                        .fullName("System Administrator").role(UserRole.ADMIN)
                                        .phone("0987654321").build());

                        User lecturer1 = User.builder()
                                        .email("thanhnh@fpt.edu.vn").password("12345")
                                        .fullName("Nguyen Huu Thanh").role(UserRole.LECTURER)
                                        .department("Software Engineering").phone("0912345678").build();
                        userRepository.save(lecturer1);

                        User lecturer2 = User.builder()
                                        .email("tungtt@fpt.edu.vn").password("12345")
                                        .fullName("Tran Thanh Tung").role(UserRole.LECTURER)
                                        .department("Information Systems").phone("0909090909").build();
                        userRepository.save(lecturer2);

                        User lecturer3 = User.builder()
                                        .email("minhpt@fpt.edu.vn").password("12345")
                                        .fullName("Phan Thanh Minh").role(UserRole.LECTURER)
                                        .department("Software Engineering").phone("0901234567").build();
                        userRepository.save(lecturer3);

                        User lecturer4 = User.builder()
                                        .email("huyennt@fpt.edu.vn").password("12345")
                                        .fullName("Nguyen Thi Huyen").role(UserRole.LECTURER)
                                        .department("Information Assurance").phone("0907654321").build();
                        userRepository.save(lecturer4);

                        User moderator = User.builder()
                                        .email("hoaiphuong@fpt.edu.vn").password("12345")
                                        .fullName("Le Hoai Phuong").role(UserRole.MODERATOR)
                                        .department("Computer Science").phone("0988888888").build();
                        userRepository.save(moderator);

                        userRepository.save(User.builder()
                                        .email("hieunm@fpt.edu.vn").password("12345")
                                        .fullName("Nguyen Minh Hieu").role(UserRole.STUDENT)
                                        .studentCode("SE17001").build());

                        userRepository.save(User.builder()
                                        .email("longdh@fpt.edu.vn").password("12345")
                                        .fullName("Duong Hoang Long").role(UserRole.STUDENT)
                                        .studentCode("SE17002").build());

                        // 4. Checklist Templates (10 tiêu chí từ Excel mẫu)
                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Tên đề tài phản ánh đúng định hướng")
                                        .description("Tên đề tài có phản ánh đúng nội dung và định hướng nghiên cứu không")
                                        .displayOrder(1).build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Ngữ cảnh sản phẩm (Product Context)")
                                        .description("Bối cảnh và môi trường sử dụng sản phẩm có được mô tả rõ ràng không")
                                        .displayOrder(2).build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Problem Statement")
                                        .description("Vấn đề cần giải quyết có được trình bày rõ ràng không")
                                        .displayOrder(3).build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Main Actors")
                                        .description("Các tác nhân chính (actors) của hệ thống có được xác định rõ không")
                                        .displayOrder(4).build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Main Flows")
                                        .description("Các luồng xử lý chính của hệ thống có được mô tả không")
                                        .displayOrder(5).build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Customers / Sponsors")
                                        .description("Khách hàng hoặc nhà tài trợ có được xác định không")
                                        .displayOrder(6).build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Approach / Applied Technology")
                                        .description("Công nghệ áp dụng và phương pháp tiếp cận có phù hợp không")
                                        .displayOrder(7).build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Scope phù hợp")
                                        .description("Phạm vi đề tài có phù hợp với thời gian và nguồn lực không")
                                        .displayOrder(8).build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Complexity phù hợp Capstone")
                                        .description("Độ phức tạp có đủ mức độ cho đồ án tốt nghiệp không")
                                        .displayOrder(9).build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Applicability & Feasibility")
                                        .description("Tính ứng dụng và khả thi của đề tài")
                                        .displayOrder(10).build());

                        // 5. Sample Topics
                        topicRepository.save(Topic.builder()
                                        .code("FA25SE001")
                                        .titleEn("Smart Capstone Project Management System")
                                        .titleVi("Hệ thống quản lý đồ án tốt nghiệp thông minh")
                                        .description("A system to manage the entire lifecycle of capstone projects.")
                                        .department("SE")
                                        .studentGroupInfo("[{\"name\":\"Nguyen Minh Hieu\",\"code\":\"SE17001\"}]")
                                        .studentCount(3)
                                        .supervisor(lecturer1)
                                        .semester(semester)
                                        .registrationPhase(phase1)
                                        .status(TopicStatus.PASS)
                                        .totalScore(7)
                                        .finalNote("R1 và R2 đồng ý: PASS")
                                        .submittedAt(LocalDateTime.now())
                                        .build());

                        topicRepository.save(Topic.builder()
                                        .code("FA25SE002")
                                        .titleEn("AI-Powered Resume Screener")
                                        .titleVi("Hệ thống sàng lọc CV bằng AI")
                                        .description("Using NLP to analyze and rank resumes based on job descriptions.")
                                        .department("SE")
                                        .studentCount(2)
                                        .supervisor(lecturer1)
                                        .semester(semester)
                                        .registrationPhase(phase1)
                                        .status(TopicStatus.PENDING)
                                        .submittedAt(LocalDateTime.now())
                                        .build());

                        topicRepository.save(Topic.builder()
                                        .code("FA25SE003")
                                        .titleEn("E-Commerce Platform with AI Recommendations")
                                        .titleVi("Nền tảng thương mại điện tử với gợi ý AI")
                                        .description("Build a modern e-commerce platform with AI-powered product recommendations.")
                                        .department("SE")
                                        .studentCount(4)
                                        .supervisor(lecturer2)
                                        .semester(semester)
                                        .registrationPhase(phase1)
                                        .status(TopicStatus.FAIL)
                                        .totalScore(-3)
                                        .finalNote("R1 và R2 đồng ý: FAIL")
                                        .submittedAt(LocalDateTime.now())
                                        .build());

                        System.out.println("Database Initialized Successfully!");
                };
        }
}
