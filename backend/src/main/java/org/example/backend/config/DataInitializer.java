package org.example.backend.config;

import org.example.backend.entity.Semester;
import org.example.backend.entity.Topic;
import org.example.backend.entity.User;
import org.example.backend.enums.TopicStatus;
import org.example.backend.enums.UserRole;
import org.example.backend.repository.SemesterRepository;
import org.example.backend.repository.TopicRepository;
import org.example.backend.repository.UserRepository;
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
            TopicRepository topicRepository) {
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
                    .topicSubmissionOpen(LocalDateTime.of(2025, 11, 1, 0, 0))
                    .topicSubmissionClose(LocalDateTime.of(2025, 12, 1, 23, 59))
                    .registrationOpen(LocalDateTime.of(2025, 12, 15, 0, 0))
                    .registrationClose(LocalDateTime.of(2025, 12, 31, 23, 59))
                    .isActive(true)
                    .build();
            semesterRepository.save(semester);

            // 2. Create Users
            // Admin
            User admin = User.builder()
                    .email("admin@fpt.edu.vn")
                    .password("12345") // Trong thực tế nên mã hóa BCrypt
                    .fullName("System Administrator")
                    .role(UserRole.ADMIN)
                    .phone("0987654321")
                    .build();
            userRepository.save(admin);

            // Supervisor (Lecturer)
            User supervisor = User.builder()
                    .email("thanhnh@fpt.edu.vn")
                    .password("12345")
                    .fullName("Nguyen Huu Thanh")
                    .role(UserRole.SUPERVISOR)
                    .department("Software Engineering")
                    .phone("0912345678")
                    .build();
            userRepository.save(supervisor);

            // Reviewer (Lecturer)
            User reviewer = User.builder()
                    .email("tungtt@fpt.edu.vn")
                    .password("12345")
                    .fullName("Tran Thanh Tung")
                    .role(UserRole.REVIEWER)
                    .department("Information Systems")
                    .phone("0909090909")
                    .build();
            userRepository.save(reviewer);

            // Coordinator
            User coordinator = User.builder()
                    .email("hoaiphuong@fpt.edu.vn")
                    .password("12345")
                    .fullName("Le Hoai Phuong")
                    .role(UserRole.COORDINATOR)
                    .department("Computer Science")
                    .phone("0988888888")
                    .build();
            userRepository.save(coordinator);

            // Students
            User student1 = User.builder()
                    .email("hieunm@fpt.edu.vn")
                    .password("12345")
                    .fullName("Nguyen Minh Hieu")
                    .role(UserRole.STUDENT)
                    .studentCode("SE17001")
                    .build();
            userRepository.save(student1);

            User student2 = User.builder()
                    .email("namtv@fpt.edu.vn")
                    .password("12345")
                    .fullName("Tran Van Nam")
                    .role(UserRole.STUDENT)
                    .studentCode("SE17002")
                    .build();
            userRepository.save(student2);

            // 3. Create Sample Topics
            Topic topic1 = Topic.builder()
                    .code("SP26-SE01")
                    .titleEn("Smart Capstone Project Management System")
                    .titleVi("Hệ thống quản lý đồ án tốt nghiệp thông minh")
                    .description(
                            "A system to manage the entire lifecycle of capstone projects, including topic proposal, team formation, and evaluation.")
                    .requirements("Java Spring Boot, ReactJS, SQL Server")
                    .supervisor(supervisor)
                    .semester(semester)
                    .status(TopicStatus.APPROVED)
                    .publishedAt(LocalDateTime.now())
                    .maxTeams(2)
                    .build();
            topicRepository.save(topic1);

            Topic topic2 = Topic.builder()
                    .code("SP26-SE02")
                    .titleEn("AI-Powered Resume Screener")
                    .titleVi("Hệ thống lọc CV sử dụng AI")
                    .description("Using NLP to analyze and rank resumes based on job descriptions.")
                    .requirements("Python, FastAPI, ReactJS, PostgreSQL")
                    .supervisor(supervisor)
                    .semester(semester)
                    .status(TopicStatus.PENDING_REVIEW)
                    .maxTeams(1)
                    .build();
            topicRepository.save(topic2);

            System.out.println("Database Initialized Successfully!");
        };
    }
}
