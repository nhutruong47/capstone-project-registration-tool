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
│   ├── Team.java
│   ├── TeamMember.java
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
| **Admin** | Quản trị viên hệ thống | Quản lý cấu hình học kỳ, tài khoản, phân quyền |
| **Supervisor** | Giảng viên hướng dẫn | Đề xuất đề tài, hướng dẫn sinh viên, duyệt đăng ký |
| **Reviewer** | Giảng viên phản biện | Thẩm định đề tài của Supervisor khác |
| **Coordinator** | Điều phối viên/Chủ nhiệm BM | Quyết định cuối cùng với đề tài tranh cãi |
| **Student** | Sinh viên | Tạo nhóm, tìm thành viên, đăng ký đề tài |

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

## 4. PHÂN HỆ CHỨC NĂNG

### 4.1 PHÂN HỆ 1: QUẢN LÝ VÒNG ĐỜI ĐỀ TÀI (Topic Lifecycle Management)

#### 4.1.1 Đề Xuất & Mã Hóa (Submission & Auto-Coding)

**Chức năng:**
- Supervisor nộp đề tài mới với đầy đủ thông tin
- Hệ thống tự động sinh mã đề tài duy nhất

**Thông tin đề tài:**
| Field | Mô Tả | Bắt Buộc |
|-------|-------|----------|
| `titleEn` | Tiêu đề Tiếng Anh | ✅ |
| `titleVi` | Tiêu đề Tiếng Việt | ✅ |
| `description` | Mô tả chi tiết | ✅ |
| `requirements` | Yêu cầu kỹ thuật | ✅ |
| `maxTeams` | Số nhóm tối đa | ✅ |
| `semesterId` | Học kỳ | ✅ |

**Cơ chế sinh mã (Race Condition Prevention):**
```java
// Sử dụng Database Sequence hoặc Distributed Lock
// Format: [Kỳ]-[Ngành][Sequence]
// Ví dụ: SP26-SE005, FA25-IT012

@Entity
public class TopicSequence {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "topic_seq")
    @SequenceGenerator(name = "topic_seq", sequenceName = "TOPIC_SEQUENCE", allocationSize = 1)
    private Long id;
}
```

#### 4.1.2 Sàng Lọc AI (AI Pre-screening)

**Quy trình xử lý bất đồng bộ:**

```
┌──────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│  Submit  │────▶│   PROCESSING │────▶│  AI Check   │────▶│ Update Topic │
│  Topic   │     │   (Status)   │     │  (Async)    │     │   Status     │
└──────────┘     └──────────────┘     └─────────────┘     └──────────────┘
                                             │
                        ┌────────────────────┴────────────────────┐
                        ▼                                         ▼
                ┌───────────────┐                         ┌───────────────┐
                │  AI Check 1   │                         │  AI Check 2   │
                │  Compliance   │                         │  Similarity   │
                └───────────────┘                         └───────────────┘
```

**AI Check 1 - Kiểm tra hình thức (Compliance Check):**
```json
{
  "check_type": "compliance",
  "criteria": {
    "description_min_length": 200,
    "description_max_length": 2000,
    "has_clear_problem_statement": true,
    "has_technology_stack": true,
    "has_deliverables": true
  },
  "result": {
    "passed": true,
    "score": 85,
    "feedback": ["Mô tả rõ ràng", "Đề xuất công nghệ hợp lý"]
  }
}
```

**AI Check 2 - Kiểm tra trùng lặp (Similarity Check):**
```json
{
  "check_type": "similarity",
  "method": "openai_embeddings",
  "threshold": 80,
  "result": {
    "similarity_score": 45,
    "similar_topics": [
      {
        "topic_code": "SP25-SE003",
        "title": "E-commerce Platform",
        "similarity": 45
      }
    ],
    "passed": true
  }
}
```

#### 4.1.3 Thẩm Định Chuyên Môn (Peer Review)

**Phân công Reviewer:**
- Mỗi đề tài được gán **2 Reviewers**
- **Constraint:** Reviewer ≠ Supervisor (không được review đề tài của chính mình)

**Thang đánh giá:**
| Decision | Giá Trị | Mô Tả |
|----------|---------|-------|
| `APPROVED` | +1 | Đồng ý duyệt |
| `REJECTED` | -1 | Từ chối |
| `CONSIDER` | 0 | Cần xem xét/sửa đổi |

**Quy tắc quyết định (Decision Matrix):**

| Reviewer 1 | Reviewer 2 | Kết Quả |
|------------|------------|---------|
| APPROVED | APPROVED | ✅ `APPROVED` (Tự động) |
| REJECTED | REJECTED | ❌ `REJECTED` (Tự động) |
| APPROVED | REJECTED | ⏳ `WAITING_COORDINATOR` |
| APPROVED | CONSIDER | ⏳ `WAITING_COORDINATOR` |
| REJECTED | CONSIDER | ⏳ `WAITING_COORDINATOR` |
| CONSIDER | CONSIDER | ⏳ `WAITING_COORDINATOR` |

**Vòng lặp chỉnh sửa (Feedback Loop):**
```
┌──────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│ CONSIDER │────▶│  Supervisor  │────▶│  Create V2  │────▶│  Reset AI &  │
│          │     │   Updates    │     │             │     │    Review    │
└──────────┘     └──────────────┘     └─────────────┘     └──────────────┘
```

---

### 4.2 PHÂN HỆ 2: ĐĂNG KÝ CỦA SINH VIÊN (Student Registration)

#### 4.2.1 Quản Lý Nhóm (Team Formation)

**Quy tắc:**
- Sinh viên **không đăng ký lẻ**, phải theo nhóm
- Số lượng thành viên: **4-5 người**
- Chỉ nhóm đủ số lượng tối thiểu mới được đăng ký đề tài

**Chức năng:**
| Chức Năng | Mô Tả |
|-----------|-------|
| Tạo nhóm | Leader tạo nhóm mới |
| Mời thành viên | Gửi mã mời (Invite Code) |
| Kick thành viên | Leader loại thành viên |
| Rời nhóm | Thành viên tự rời nhóm |

**Team Status Flow:**
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   FORMING   │────▶│    READY    │────▶│  REGISTERED │
│  (<4 SV)    │     │  (4-5 SV)   │     │  (Có đề tài)│
└─────────────┘     └─────────────┘     └─────────────┘
```

#### 4.2.2 Đăng Ký Đề Tài (Topic Registration)

**Hiển thị:** Sinh viên chỉ thấy đề tài có status `APPROVED` và `PUBLISHED`

**Quy tắc FCFS (First Come First Served):**
```
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Team A     │────▶│ PENDING_APPROVAL│────▶│    FINALIZED    │
│  Registers  │     │   (Chờ GV)      │     │   (GV duyệt)    │
└─────────────┘     └─────────────────┘     └─────────────────┘

  Team B, C...      "Đã có người đăng ký"
     ❌ Blocked
```

**Registration Status:**
| Status | Mô Tả |
|--------|-------|
| `PENDING` | Chờ Supervisor duyệt |
| `APPROVED` | Supervisor đã duyệt |
| `REJECTED` | Supervisor từ chối |
| `FINALIZED` | Hoàn tất, đề tài được gán |

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
│     TEAM     │       │ TEAM_MEMBER  │       │ REGISTRATION │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id           │       │ id           │       │ id           │
│ name         │◀──────│ teamId       │       │ teamId       │
│ inviteCode   │       │ userId       │──────▶│ topicId      │
│ leaderId     │       │ role         │       │ status       │
│ status       │       │ joinedAt     │       │ registeredAt │
│ createdAt    │       └──────────────┘       │ approvedAt   │
└──────────────┘                              └──────────────┘
```

### 5.2 Topic Status Enum

```java
public enum TopicStatus {
    DRAFT,              // Bản nháp
    PROCESSING,         // Đang xử lý AI
    AI_PASSED,          // AI đã duyệt
    AI_FAILED,          // AI từ chối
    PENDING_REVIEW,     // Chờ peer review
    WAITING_COORDINATOR,// Chờ Coordinator quyết định
    APPROVED,           // Đã duyệt
    REJECTED,           // Bị từ chối
    PUBLISHED,          // Đã công bố cho SV
    REGISTERED,         // Đã có SV đăng ký
    FINALIZED           // Hoàn tất
}
```

---

## 6. QUY TRÌNH NGHIỆP VỤ

### 6.1 Topic Lifecycle Flow

```
┌─────────┐    ┌────────────┐    ┌───────────┐    ┌─────────────┐
│  DRAFT  │───▶│ PROCESSING │───▶│ AI_PASSED │───▶│PENDING_REVIEW│
└─────────┘    └────────────┘    └───────────┘    └─────────────┘
                    │                                    │
                    ▼                                    ▼
              ┌───────────┐                    ┌─────────────────┐
              │ AI_FAILED │                    │    2 Reviews    │
              └───────────┘                    └─────────────────┘
                    │                          ╱       │       ╲
                    ▼                         ▼        ▼        ▼
              ┌───────────┐            ┌─────────┐ ┌───────┐ ┌─────────────────┐
              │  Resubmit │            │APPROVED │ │REJECTED│ │WAITING_COORDINATOR│
              └───────────┘            └─────────┘ └───────┘ └─────────────────┘
                                            │                        │
                                            ▼                        ▼
                                      ┌───────────┐           ┌─────────────┐
                                      │ PUBLISHED │           │ Coordinator │
                                      └───────────┘           │  Decision   │
                                            │                 └─────────────┘
                                            ▼
                                      ┌───────────┐
                                      │REGISTERED │
                                      └───────────┘
                                            │
                                            ▼
                                      ┌───────────┐
                                      │ FINALIZED │
                                      └───────────┘
```

### 6.2 Student Registration Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    STUDENT REGISTRATION FLOW                 │
└─────────────────────────────────────────────────────────────┘

Step 1: Team Formation
┌──────────┐    ┌───────────┐    ┌───────────┐    ┌─────────┐
│  Create  │───▶│   Invite  │───▶│  Members  │───▶│  READY  │
│   Team   │    │  Members  │    │   Join    │    │  (4-5)  │
└──────────┘    └───────────┘    └───────────┘    └─────────┘

Step 2: Topic Registration
┌─────────┐    ┌───────────┐    ┌───────────┐    ┌───────────┐
│  Browse │───▶│  Select   │───▶│  PENDING  │───▶│ FINALIZED │
│  Topics │    │   Topic   │    │  APPROVAL │    │           │
└─────────┘    └───────────┘    └───────────┘    └───────────┘
                                      │
                                      ▼
                                ┌───────────┐
                                │ Supervisor│
                                │  Approves │
                                └───────────┘
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

| Method | Endpoint | Mô Tả | Role |
|--------|----------|-------|------|
| POST | `/api/teams` | Tạo nhóm mới | Student |
| GET | `/api/teams/my-team` | Lấy thông tin nhóm | Student |
| POST | `/api/teams/join` | Tham gia nhóm bằng code | Student |
| DELETE | `/api/teams/members/{userId}` | Kick thành viên | Leader |
| POST | `/api/teams/leave` | Rời nhóm | Student |

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
