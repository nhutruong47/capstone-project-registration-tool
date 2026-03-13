package org.example.backend.config;

import org.example.backend.entity.*;
import org.example.backend.enums.PhaseStatus;
import org.example.backend.enums.ReviewStatus;
import org.example.backend.enums.TopicStatus;
import org.example.backend.enums.UserRole;
import org.example.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
            SemesterRepository semesterRepository,
            RegistrationPhaseRepository registrationPhaseRepository,
            TopicRepository topicRepository,
            TopicReviewerRepository topicReviewerRepository,
            JdbcTemplate jdbcTemplate) {
        return args -> {
            // Force drop NOT NULL constraints for PostgreSQL (Render)
            try {
                System.out.println("Forcing database column nullability update...");
                jdbcTemplate.execute("ALTER TABLE topics ALTER COLUMN supervisor_id DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE topics ALTER COLUMN reviewer1_id DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE topics ALTER COLUMN reviewer2_id DROP NOT NULL");
                jdbcTemplate.execute("ALTER TABLE topics ALTER COLUMN reviewer3_id DROP NOT NULL");
                System.out.println("Database column nullability update successful.");
            } catch (Exception e) {
                System.out.println("Schema update via SQL failed (might be SQL Server or already updated): " + e.getMessage());
                // If it fails, we try SQL Server syntax just in case
                try {
                    jdbcTemplate.execute("ALTER TABLE topics ALTER COLUMN supervisor_id BIGINT NULL");
                    jdbcTemplate.execute("ALTER TABLE topics ALTER COLUMN reviewer1_id BIGINT NULL");
                    jdbcTemplate.execute("ALTER TABLE topics ALTER COLUMN reviewer2_id BIGINT NULL");
                    jdbcTemplate.execute("ALTER TABLE topics ALTER COLUMN reviewer3_id BIGINT NULL");
                    System.out.println("Database column nullability update successful (SQL Server syntax).");
                } catch (Exception e2) {
                    System.out.println("SQL Server schema update also failed: " + e2.getMessage());
                }
            }

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
                    .status(TopicStatus.APPROVED)
                    .totalScore(7)
                    .finalNote("R1 và R2 đồng ý: APPROVED")
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

            // 6. Topics for the new 7-Step Flow
            // Step 3: Waiting Moderator (Passed AI)
            Topic topic4 = Topic.builder()
                    .code("AI1FA25")
                    .titleEn("Blockchain for Supply Chain Transparency")
                    .titleVi("Ứng dụng Blockchain cho truy xuất nguồn gốc chuỗi cung ứng")
                    .description("Detailed description about a high-quality blockchain system for tracking goods.")
                    .department("IA")
                    .studentCount(4)
                    .semester(semester)
                    .registrationPhase(phase2)
                    .status(TopicStatus.WAITING_MODERATOR)
                    .aiSimilarityScore(5.0)
                    .finalNote("AI Check Result: PASSED (Compliant & Unique)")
                    .submittedAt(LocalDateTime.now())
                    .build();
            topicRepository.save(topic4);

            // Step 4/5: In Review (Assigned to R1 and R2)
            Topic topic5 = Topic.builder()
                    .code("SE2FA25")
                    .titleEn("Microservices with Spring Cloud and K8s")
                    .titleVi("Hệ thống Microservices với Spring Cloud và Kubernetes")
                    .description("Building a scalable microservices architecture.")
                    .department("SE")
                    .studentCount(3)
                    .semester(semester)
                    .registrationPhase(phase2)
                    .reviewer1(lecturer1)
                    .reviewer2(lecturer2)
                    .status(TopicStatus.IN_REVIEW)
                    .submittedAt(LocalDateTime.now())
                    .build();
            topicRepository.save(topic5);

            topicReviewerRepository.save(TopicReviewer.builder()
                    .topic(topic5).reviewer(lecturer1).reviewerOrder(1)
                    .reviewStatus(ReviewStatus.NOT_STARTED).build());
            topicReviewerRepository.save(TopicReviewer.builder()
                    .topic(topic5).reviewer(lecturer2).reviewerOrder(2)
                    .reviewStatus(ReviewStatus.NOT_STARTED).build());

            // Step 6: Need Third Reviewer (Disagreement: R1=Approved, R2=Rejected)
            Topic topic6 = Topic.builder()
                    .code("SE3FA25")
                    .titleEn("IoT Smart Home Security")
                    .titleVi("Bảo mật nhà thông minh IoT")
                    .description("Securing IoT devices in a smart home environment.")
                    .department("IA")
                    .studentCount(3)
                    .semester(semester)
                    .registrationPhase(phase2)
                    .reviewer1(lecturer3)
                    .reviewer2(lecturer4)
                    .status(TopicStatus.NEED_THIRD_REVIEWER)
                    .finalNote("Mâu thuẫn: R1=APPROVED, R2=REJECTED. Cần Reviewer thứ 3.")
                    .submittedAt(LocalDateTime.now())
                    .build();
            topicRepository.save(topic6);

            topicReviewerRepository.save(TopicReviewer.builder()
                    .topic(topic6).reviewer(lecturer3).reviewerOrder(1)
                    .decision(TopicStatus.APPROVED).totalScore(1)
                    .comment("Good idea.").reviewStatus(ReviewStatus.COMPLETED)
                    .reviewedAt(LocalDateTime.now()).build());

            topicReviewerRepository.save(TopicReviewer.builder()
                    .topic(topic6).reviewer(lecturer4).reviewerOrder(2)
                    .decision(TopicStatus.REJECTED).totalScore(0)
                    .comment("Description is too vague.").reviewStatus(ReviewStatus.COMPLETED)
                    .reviewedAt(LocalDateTime.now()).build());

            // Step 7: Finalized (Fully Approved)
            Topic topic7 = Topic.builder()
                    .code("SE4FA25")
                    .titleEn("Mobile App for Mental Health")
                    .titleVi("Ứng dụng di động hỗ trợ sức khỏe tinh thần")
                    .description("A mobile application providing mental health support.")
                    .department("SE")
                    .studentCount(3)
                    .semester(semester)
                    .registrationPhase(phase2)
                    .status(TopicStatus.FINALIZED)
                    .isLocked(true)
                    .finalNote("R1 và R2 đồng ý: APPROVED")
                    .submittedAt(LocalDateTime.now())
                    .build();
            topicRepository.save(topic7);

            System.out.println("Database Initialized Successfully!");
        };
    }
}
