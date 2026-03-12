# HỆ THỐNG QUẢN LÝ QUY TRÌNH ĐỀ XUẤT & ĐĂNG KÝ ĐỒ ÁN TỐT NGHIỆP
## SMART CAPSTONE MANAGEMENT SYSTEM

---

## 📋 MỤC LỤC

1. [Tổng Quan](#1-tổng-quan)
2. [Kiến Trúc Hệ Thống](#2-kiến-trúc-hệ-thống)
3. [Các Tác Nhân Hệ Thống](#3-các-tác-nhân-hệ-thống)
4. [Phân Hệ Chức Năng](#4-phân-hệ-chức-năng)
5. [Mô Hình Dữ Liệu](#5-mô-hình-dữ-liệu)
6. [Quy Trình Nghiệp Vụ](#6-quy-trình-nghiệp-vụ)
7. [API Endpoints](#7-api-endpoints)
8. [Công Nghệ Sử Dụng](#8-công-nghệ-sử-dụng)

---

## 1. TỔNG QUAN

### 1.1 Mục Tiêu
Hệ thống được xây dựng nhằm giải quyết bài toán **"nút thắt cổ chai"** trong việc quản lý hàng trăm đề tài Capstone mỗi kỳ.

### 1.2 Vấn Đề Hiện Tại
- Xử lý thủ công qua Excel
- Khó kiểm soát chất lượng đề tài
- Thiếu minh bạch trong quy trình duyệt
- Sinh viên khó đăng ký và ghép nhóm

### 1.3 Giải Pháp
- **Số hóa toàn bộ quy trình** từ lúc Giảng viên nộp đề tài
- **Sử dụng AI (OpenAI)** để sàng lọc chất lượng bước đầu
- **Hỗ trợ Hội đồng chuyên môn** duyệt chéo
- **Cho phép Sinh viên** đăng ký/ghép nhóm minh bạch

---

## 2. KIẾN TRÚC HỆ THỐNG

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │   Admin     │  │  Supervisor │  │   Student   │  │  Reviewer   │ │
│  │   Portal    │  │   Portal    │  │   Portal    │  │   Portal    │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY / REST API                       │
│                    (Spring Boot + Spring Security)                   │
└─────────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│    Topic      │     │  Registration │     │     User      │
│   Service     │     │    Service    │     │   Service     │
└───────────────┘     └───────────────┘     └───────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         DATA ACCESS LAYER                            │
│                    (Spring Data JPA + Repositories)                  │
└─────────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│   SQL Server  │     │  OpenAI API   │     │ Notification  │
│   Database    │     │  (Embeddings) │     │   Service     │
└───────────────┘     └───────────────┘     └───────────────┘
```

### 2.2 Package Structure

```
src/main/java/org/example/backend/
├── config/                     # Cấu hình Spring, Security, OpenAI
│   ├── SecurityConfig.java
│   ├── OpenAIConfig.java
│   └── AsyncConfig.java
├── controller/                 # REST Controllers
│   ├── AuthController.java
│   ├── TopicController.java
│   ├── ReviewController.java
│   ├── TeamController.java
│   ├── RegistrationController.java
│   └── AdminController.java
├── entity/                     # JPA Entities
│   ├── User.java
│   ├── Topic.java
│   ├── TopicVersion.java
│   ├── Review.java
│   ├── Registration.java
│   ├── Semester.java
│   └── Notification.java
├── repository/                 # Spring Data Repositories
│   ├── UserRepository.java
│   ├── TopicRepository.java
│   ├── ReviewRepository.java
│   ├── TeamRepository.java
│   └── RegistrationRepository.java
├── service/                    # Business Logic
│   ├── AuthService.java
│   ├── TopicService.java
│   ├── AIService.java
│   ├── ReviewService.java
│   ├── TeamService.java
│   ├── RegistrationService.java
│   └── NotificationService.java
├── dto/                        # Data Transfer Objects
│   ├── request/
│   └── response/
├── enums/                      # Enumerations
│   ├── TopicStatus.java
│   ├── ReviewDecision.java
│   ├── UserRole.java
│   └── RegistrationStatus.java
├── exception/                  # Custom Exceptions
│   └── GlobalExceptionHandler.java
└── util/                       # Utility Classes
    └── TopicCodeGenerator.java
```

---

## 3. CÁC TÁC NHÂN HỆ THỐNG (ACTORS)

| Actor | Vai Trò | Quyền Hạn Chính |
|-------|---------|-----------------|
| **Admin** | Quản trị viên hệ thống | Quản lý cấu hình học kỳ, tài khoản, thiết lập thời gian biểu |
| **Moderator** | Điều phối viên | Gán Reviewer 1 & 2, quyết định gán Reviewer thứ 3 |
| **Reviewer** | Giảng viên phản biện | Thẩm định đề cương của sinh viên |
| **Coordinator** | Giảng viên thứ 3 | Phân xử khi 2 Reviewer bất đồng ý kiến |
| **Supervisor** | Giảng viên hướng dẫn | Hướng dẫn sinh viên sau khi đề tài được duyệt (phân bổ ở bước cuối) |
| **Student (Leader)**| Trưởng nhóm SV | Nộp ý tưởng đề tài đại diện cho nhóm (không cần tạo nhóm trước) |

### 3.1 User Role Enum

```java
public enum UserRole {
    ADMIN,
    SUPERVISOR,
    REVIEWER,
    COORDINATOR,
    STUDENT
}
```

---

## 4. QUY TRÌNH NGHIỆP VỤ CỐT LÕI (7 BƯỚC)

Hệ thống hoạt động theo một luồng duy nhất từ lúc thiết lập đến khi sinh viên nộp và chốt đề tài:

**Bước 1: ADMIN (Quản trị viên) - Chuẩn bị Học kỳ & Hệ thống**
- **Tạo Học kỳ mới:** Admin đăng nhập vào hệ thống, tạo một Học kỳ (Semester) mới (Ví dụ: Spring 2026).
- **Thiết lập Thời gian biểu:** Admin cấu hình ngày bắt đầu / kết thúc Học kỳ, cũng như ngày mở/đóng đợt nộp đề xuất đề tài cho sinh viên.
- **Quản lý Tài khoản:** Đảm bảo danh sách Giảng viên (Reviewer), Moderator và Sinh viên (Student) đã được import đầy đủ và hợp lệ.

**Bước 2: Sinh viên (Leader) - Nộp Ý tưởng (Không cần file, Không cần tạo nhóm)**
- Khi thời gian "Mở đăng ký" bắt đầu, Leader (Tài khoản đại diện nhóm) đăng nhập vào hệ thống.
- Leader truy cập form "Đề tài Sinh viên tự đề xuất" và điền trực tiếp:
  - Tên đề tài (Tiếng Việt & Anh).
  - `department` (Mã ngành: SE, AI, IA...).
  - `description` (Mô tả chi tiết ý định xây dựng hệ thống, module...).
  - Tên/Mã SV của các thành viên trong nhóm (Ví dụ: Nhập đủ 4 mã SV).
- Bấm "Submit". Đề tài được lưu vào DB với trạng thái ban đầu là `PENDING`.

**Bước 3: AI - Lọc đề tài & Tự động Đánh Mã (Auto ID Generation)**
- Ngay lập tức, hệ thống gửi ngầm nội dung (đặc biệt là description) qua OpenAI để chạy 2 bài test:
  - **Compliance Check:** Kiểm tra chất lượng mô tả (nội dung có nghiêm túc, đủ ý chính không).
  - **Similarity Check:** Chống đạo nhái, so sánh với các khóa trước để ra "Điểm trùng lắp" (vd: 5%).
- Nếu AI chấm đạt (hoặc điểm rủi ro thấp), Hệ thống tự động sinh Mã Đề tài theo format ấn định: `[Mã Ngành] + [STT] + [Kỳ] + [Năm]`. 👉 Ví dụ: Nhóm nộp sản phẩm ngành AI đầu tiên của kỳ Spring 2026 sẽ được AI cấp liền mã: `A1Spring26`.
- Đề tài lúc này sẽ đính kèm "Điểm AI", "Mã đề tài" và tự động chuyển trạng thái chờ: `WAITING_MODERATOR`.

**Bước 4: MODERATOR (Điều phối viên) - Chọn 2 Giảng viên chấm thi**
- Moderator đăng nhập, xem danh sách các đề tài đã có mã (vd: A1Spring26) và đã pass qua vòng lọc AI.
- Moderator phân tích nội dung đề tài và chuyên môn của các giảng viên trong khoa.
- **Thao tác thủ công:** Moderator tự tay gán 2 Thầy/Cô phù hợp nhất vào ô Reviewer 1 và Reviewer 2 để phụ trách chấm Đề cương này.
- Hệ thống gửi chuông báo/email cho 2 thầy cô vừa được phân công. Trạng thái đề tài cập nhật thành `IN_REVIEW`.

**Bước 5: 2 Giảng viên REVIEWER - Thẩm định độc lập**
- 2 Giảng viên vào hệ thống, mở Đề cương của bạn Leader ra đọc và đưa ra phán quyết:
  - TH1: Cả hai cùng vote `APPROVED` ➡️ Đề tài Pass (Chuyển sang Bước 7).
  - TH2: Cả hai cùng báo Hủy/Sửa ➡️ Trả lại cho Leader sửa description để nộp lại vòng sau.
  - TH3: 1 người vote `APPROVED`, 1 người vote `REJECTED` (Lệch ý kiến) ➡️ Hệ thống đẩy trạng thái báo động `NEED_THIRD_REVIEWER`.

**Bước 6: MODERATOR - Chọn Người thứ 3 (Tie-breaker)**
- Nhận được cảnh báo "Lệch ý kiến" của mã A1Spring26, Moderator đăng nhập lại.
- Moderator tiếp tục chọn thủ công thêm 1 Giám khảo chung thẩm (Coordinator / Giảng viên thứ 3) dạn dày kinh nghiệm vào hệ thống.
- Vị Giám khảo thứ 3 này sẽ vào đọc, cân nhắc nhận xét của 2 người đi trước, và đưa ra 1 phiếu Vote chốt hạ cuối cùng: `APPROVED` hoặc `REJECTED`.

**Bước 7: FINALIZED - Hoàn tất (Phân bổ Giảng viên hướng dẫn)**
- Khi Đề tài nhận được phán quyết `APPROVED` cuối cùng, hệ thống chớp nhoáng đóng đinh trạng thái: `FINALIZED`.
- Lúc này, hệ thống/nhà trường sẽ phân bổ Giảng viên hướng dẫn. Mọi thông tin gắn liền với đề tài khóa lại.

---

## 5. MÔ HÌNH DỮ LIỆU

### 5.1 Entity Relationship Diagram

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   SEMESTER   │       │     USER     │       │    TOPIC     │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id           │       │ id           │       │ id           │
│ code         │       │ email        │       │ code         │
│ name         │       │ password     │──────▶│ titleEn      │
│ startDate    │◀──────│ fullName     │       │ titleVi      │
│ endDate      │       │ role         │       │ description  │
│ registOpen   │       │ department   │       │ requirements │
│ registClose  │       └──────────────┘       │ status       │
└──────────────┘              │               │ supervisorId │
                              │               │ semesterId   │
                              ▼               │ version      │
                    ┌──────────────┐          └──────────────┘
                    │    REVIEW    │                 │
                    ├──────────────┤                 │
                    │ id           │                 │
                    │ topicId      │◀────────────────┘
                    │ reviewerId   │
                    │ decision     │
                    │ comment      │
                    │ createdAt    │
                    └──────────────┘

┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│     USER     │       │    TOPIC     │       │ REGISTRATION │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id           │       │ id           │       │ id           │
│ email        │       │ code         │       │ leaderId     │
│ password     │──────▶│ titleEn      │◀──────│ topicId      │
│ fullName     │       │ titleVi      │       │ status       │
│ role         │       │ description  │       │ registeredAt │
│ department   │       │ status       │       │ approvedAt   │
└──────────────┘       │ leaderId     │       └──────────────┘
                       │ semesterId   │
                       └──────────────┘
```

### 5.2 Topic Status Enum

```java
public enum TopicStatus {
    PENDING,             // Sinh viên vừa nộp đề xuất
    WAITING_MODERATOR,   // Đã qua AI lọc, chờ Moderator phân công người chấm
    IN_REVIEW,           // Đang được 2 giảng viên Reviewer chấm
    NEED_THIRD_REVIEWER, // Lệch ý kiến, chờ Moderator gán Giám khảo 3
    REJECTED,            // Đề tài bị từ chối/Bắt sửa lại
    APPROVED,            // Reviewer duyệt thành công (bước đệm)
    FINALIZED            // Hoàn tất (Sẵn sàng thực hiện)
}
```

---

## 6. QUY TRÌNH NGHIỆP VỤ

## 6. SƠ ĐỒ TRẠNG THÁI (STATUS FLOW)

```
┌─────────┐   ┌───────────────────┐   ┌───────────┐
│ PENDING │──▶│ WAITING_MODERATOR │──▶│ IN_REVIEW │
└─────────┘   └───────────────────┘   └───────────┘
                                            │
               ┌────────────────────────────┴──────────────────────────┐
               │                            │                          │
               ▼                            ▼                          ▼
         ┌──────────┐              ┌─────────────────┐           ┌──────────┐
         │ REJECTED │              │NEED_THIRD_REVIEW│           │ APPROVED │
         └──────────┘              └─────────────────┘           └──────────┘
                                            │                          │
                                            ▼                          │
                                     ┌──────────┐                      │
                                     │ FINALIZED│◀─────────────────────┘
                                     └──────────┘
```

---

## 7. API ENDPOINTS

### 7.1 Authentication APIs

| Method | Endpoint | Mô Tả |
|--------|----------|-------|
| POST | `/api/auth/register` | Đăng ký tài khoản |
| POST | `/api/auth/login` | Đăng nhập |
| POST | `/api/auth/logout` | Đăng xuất |
| GET | `/api/auth/me` | Lấy thông tin user hiện tại |

### 7.2 Topic APIs

| Method | Endpoint | Mô Tả | Role |
|--------|----------|-------|------|
| POST | `/api/topics` | Tạo đề tài mới | Supervisor |
| GET | `/api/topics` | Lấy danh sách đề tài | All |
| GET | `/api/topics/{id}` | Chi tiết đề tài | All |
| PUT | `/api/topics/{id}` | Cập nhật đề tài | Supervisor |
| DELETE | `/api/topics/{id}` | Xóa đề tài | Supervisor |
| POST | `/api/topics/{id}/submit` | Nộp đề tài để duyệt | Supervisor |
| POST | `/api/topics/{id}/publish` | Công bố đề tài | Coordinator |

### 7.3 Review APIs

| Method | Endpoint | Mô Tả | Role |
|--------|----------|-------|------|
| GET | `/api/reviews/assigned` | Đề tài được phân công review | Reviewer |
| POST | `/api/reviews/{topicId}` | Gửi đánh giá | Reviewer |
| PUT | `/api/reviews/{id}` | Cập nhật đánh giá | Reviewer |
| POST | `/api/reviews/{topicId}/coordinator-decision` | Quyết định Coordinator | Coordinator |

### 7.4 Team APIs

(Removed: No longer needed. Leader uses Topic APIs to register topics with group info.)

### 7.5 Registration APIs

| Method | Endpoint | Mô Tả | Role |
|--------|----------|-------|------|
| POST | `/api/registrations` | Đăng ký đề tài | Student (Leader) |
| GET | `/api/registrations/topic/{topicId}` | DS đăng ký của đề tài | Supervisor |
| PUT | `/api/registrations/{id}/approve` | Duyệt đăng ký | Supervisor |
| PUT | `/api/registrations/{id}/reject` | Từ chối đăng ký | Supervisor |

### 7.6 Admin APIs

| Method | Endpoint | Mô Tả | Role |
|--------|----------|-------|------|
| GET | `/api/admin/users` | Quản lý người dùng | Admin |
| PUT | `/api/admin/users/{id}/role` | Phân quyền | Admin |
| GET | `/api/admin/semesters` | Quản lý học kỳ | Admin |
| POST | `/api/admin/semesters` | Tạo học kỳ mới | Admin |

---

## 8. CÔNG NGHỆ SỬ DỤNG

### 8.1 Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend Framework** | Spring Boot 3.4.x |
| **Security** | Spring Security + JWT |
| **Database** | SQL Server / PostgreSQL |
| **ORM** | Spring Data JPA + Hibernate |
| **AI Integration** | OpenAI API (GPT-4, Embeddings) |
| **Async Processing** | Spring @Async + CompletableFuture |
| **Build Tool** | Maven |
| **Documentation** | Swagger/OpenAPI 3.0 |

### 8.2 Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
    </dependency>
    
    <!-- OpenAI -->
    <dependency>
        <groupId>com.theokanning.openai-gpt3-java</groupId>
        <artifactId>service</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
</dependencies>
```

### 8.3 Configuration Properties

```properties
# Application
spring.application.name=smart-capstone-management

# Database
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=capstone_db
spring.datasource.username=sa
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# OpenAI
openai.api.key=${OPENAI_API_KEY}
openai.model=gpt-4
openai.embedding-model=text-embedding-ada-002

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Async
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
```

---

## 📊 DASHBOARD METRICS (Future Enhancement)

| Metric | Mô Tả |
|--------|-------|
| Total Topics | Tổng số đề tài trong kỳ |
| Pending Reviews | Số đề tài đang chờ review |
| Approved Rate | Tỷ lệ đề tài được duyệt |
| Registration Rate | Tỷ lệ đề tài có SV đăng ký |
| AI Pass Rate | Tỷ lệ đề tài pass AI screening |

---

## 🚀 DEVELOPMENT ROADMAP

### Phase 1: Core Infrastructure
- [ ] User authentication & authorization
- [ ] Basic CRUD for Topics
- [ ] Database schema setup

### Phase 2: Topic Lifecycle
- [ ] AI Pre-screening integration
- [ ] Peer Review system
- [ ] Coordinator decision flow

### Phase 3: Student Features
- [ ] Team formation
- [ ] Topic registration
- [ ] FCFS mechanism

### Phase 4: Enhancement
- [ ] Notification system
- [ ] Dashboard & Analytics
- [ ] Export reports

---

**Document Version:** 1.0
**Last Updated:** 2026-01-28
**Author:** Smart Capstone Management Team
