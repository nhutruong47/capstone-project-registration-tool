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

                        // 1. Create Active Semester
                        Semester semester = Semester.builder()
                                        .code("SP26")
                                        .name("Spring 2026")
                                        .startDate(LocalDate.of(2026, 1, 1))
                                        .endDate(LocalDate.of(2026, 4, 30))
                                        .isActive(true)
                                        .build();
                        semesterRepository.save(semester);

                        // 2. Create Registration Phases
                        RegistrationPhase phase1 = RegistrationPhase.builder()
                                        .name("Đợt 1")
                                        .startDate(LocalDateTime.of(2025, 11, 1, 0, 0))
                                        .endDate(LocalDateTime.of(2025, 12, 1, 23, 59))
                                        .status(PhaseStatus.CLOSED)
                                        .semester(semester)
                                        .build();
                        registrationPhaseRepository.save(phase1);

                        RegistrationPhase phase2 = RegistrationPhase.builder()
                                        .name("Đợt 2")
                                        .startDate(LocalDateTime.of(2025, 12, 10, 0, 0))
                                        .endDate(LocalDateTime.of(2025, 12, 31, 23, 59))
                                        .status(PhaseStatus.OPEN)
                                        .semester(semester)
                                        .build();
                        registrationPhaseRepository.save(phase2);

                        // 3. Create Users
                        User admin = User.builder()
                                        .email("admin@fpt.edu.vn")
                                        .password("12345")
                                        .fullName("System Administrator")
                                        .role(UserRole.ADMIN)
                                        .phone("0987654321")
                                        .build();
                        userRepository.save(admin);

                        User lecturer1 = User.builder()
                                        .email("thanhnh@fpt.edu.vn")
                                        .password("12345")
                                        .fullName("Nguyen Huu Thanh")
                                        .role(UserRole.LECTURER)
                                        .department("Software Engineering")
                                        .phone("0912345678")
                                        .build();
                        userRepository.save(lecturer1);

                        User lecturer2 = User.builder()
                                        .email("tungtt@fpt.edu.vn")
                                        .password("12345")
                                        .fullName("Tran Thanh Tung")
                                        .role(UserRole.LECTURER)
                                        .department("Information Systems")
                                        .phone("0909090909")
                                        .build();
                        userRepository.save(lecturer2);

                        User lecturer3 = User.builder()
                                        .email("minhpt@fpt.edu.vn")
                                        .password("12345")
                                        .fullName("Phan Thanh Minh")
                                        .role(UserRole.LECTURER)
                                        .department("Software Engineering")
                                        .phone("0901234567")
                                        .build();
                        userRepository.save(lecturer3);

                        User moderator = User.builder()
                                        .email("hoaiphuong@fpt.edu.vn")
                                        .password("12345")
                                        .fullName("Le Hoai Phuong")
                                        .role(UserRole.MODERATOR)
                                        .department("Computer Science")
                                        .phone("0988888888")
                                        .build();
                        userRepository.save(moderator);

                        User student1 = User.builder()
                                        .email("hieunm@fpt.edu.vn")
                                        .password("12345")
                                        .fullName("Nguyen Minh Hieu")
                                        .role(UserRole.STUDENT)
                                        .studentCode("SE17001")
                                        .build();
                        userRepository.save(student1);

                        // 4. Create Checklist Templates
                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Tính thực tiễn")
                                        .description("Đề tài có tính ứng dụng thực tế, giải quyết vấn đề cụ thể")
                                        .displayOrder(1)
                                        .build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Tính khả thi")
                                        .description("Đề tài có thể hoàn thành trong thời gian quy định với nguồn lực hiện có")
                                        .displayOrder(2)
                                        .build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Tính mới")
                                        .description("Đề tài có yếu tố sáng tạo, không trùng lặp với các đề tài trước")
                                        .displayOrder(3)
                                        .build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Yêu cầu kỹ thuật")
                                        .description("Đề tài có yêu cầu kỹ thuật rõ ràng, phù hợp với trình độ sinh viên")
                                        .displayOrder(4)
                                        .build());

                        checklistTemplateRepository.save(ChecklistTemplate.builder()
                                        .name("Mô tả đầy đủ")
                                        .description("Mô tả đề tài rõ ràng, đầy đủ thông tin cần thiết")
                                        .displayOrder(5)
                                        .build());

                        // 5. Create Sample Topics
                        Topic topic1 = Topic.builder()
                                        .code("SP26-SE001")
                                        .title("Smart Capstone Project Management System")
                                        .description("A system to manage the entire lifecycle of capstone projects, including topic proposal, team formation, and evaluation.")
                                        .supervisor(lecturer1)
                                        .semester(semester)
                                        .registrationPhase(phase1)
                                        .status(TopicStatus.PASS)
                                        .submittedAt(LocalDateTime.now())
                                        .build();
                        topicRepository.save(topic1);

                        Topic topic2 = Topic.builder()
                                        .code("SP26-SE002")
                                        .title("AI-Powered Resume Screener")
                                        .description("Using NLP to analyze and rank resumes based on job descriptions. The system provides automated screening and transparency.")
                                        .supervisor(lecturer1)
                                        .semester(semester)
                                        .registrationPhase(phase1)
                                        .status(TopicStatus.PENDING)
                                        .submittedAt(LocalDateTime.now())
                                        .build();
                        topicRepository.save(topic2);

                        System.out.println("Database Initialized Successfully!");
                };
        }
}
