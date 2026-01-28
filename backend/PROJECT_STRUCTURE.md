# CẤU TRÚC CÂY DỰ ÁN - SMART CAPSTONE MANAGEMENT SYSTEM
## 3-Layer Architecture (Controller - Service - Repository)

```
d:\sab\BOOK\backend\
├── pom.xml                                    # Maven configuration
├── SMART_CAPSTONE_MANAGEMENT_SYSTEM.md        # Project documentation
│
└── src/main/java/org/example/backend/
    │
    ├── BackendApplication.java                # Main application entry point
    │
    ├── config/                                # ⚙️ CONFIGURATION LAYER
    │   ├── AsyncConfig.java                   # Async thread pool config
    │   └── WebConfig.java                     # CORS configuration
    │
    ├── enums/                                 # 📊 ENUMERATIONS
    │   ├── UserRole.java                      # ADMIN, SUPERVISOR, REVIEWER, COORDINATOR, STUDENT
    │   ├── TopicStatus.java                   # DRAFT → PROCESSING → AI_PASSED → PENDING_REVIEW → APPROVED → PUBLISHED → FINALIZED
    │   ├── ReviewDecision.java                # APPROVED (+1), REJECTED (-1), CONSIDER (0)
    │   ├── TeamStatus.java                    # FORMING, READY, REGISTERED, FINALIZED
    │   ├── TeamMemberRole.java                # LEADER, MEMBER
    │   └── RegistrationStatus.java            # PENDING, APPROVED, REJECTED, FINALIZED
    │
    ├── entity/                                # 📦 DATA LAYER (JPA Entities)
    │   ├── User.java                          # User account (all roles)
    │   ├── Semester.java                      # Academic semester
    │   ├── Topic.java                         # Capstone topic with AI screening fields
    │   ├── Review.java                        # Peer review record
    │   ├── Team.java                          # Student team
    │   ├── TeamMember.java                    # Team membership (join table)
    │   ├── Registration.java                  # Topic registration by team
    │   └── Notification.java                  # User notifications
    │
    ├── repository/                            # 💾 DATA ACCESS LAYER (Spring Data JPA)
    │   ├── UserRepository.java                # User CRUD + findByEmail, findByRole
    │   ├── SemesterRepository.java            # Semester CRUD + findActive
    │   ├── TopicRepository.java               # Topic CRUD + complex queries
    │   ├── ReviewRepository.java              # Review CRUD + findPending
    │   ├── TeamRepository.java                # Team CRUD + findByInviteCode
    │   ├── TeamMemberRepository.java          # TeamMember CRUD
    │   ├── RegistrationRepository.java        # Registration CRUD + FCFS queries
    │   └── NotificationRepository.java        # Notification CRUD
    │
    ├── service/                               # 🧠 BUSINESS LOGIC LAYER
    │   ├── AuthService.java                   # Authentication & user management
    │   ├── SemesterService.java               # Semester management
    │   ├── TopicService.java                  # Topic lifecycle management
    │   ├── AIService.java                     # AI screening (compliance + similarity)
    │   ├── ReviewService.java                 # Peer review process + decision matrix
    │   ├── TeamService.java                   # Team formation & membership
    │   ├── RegistrationService.java           # FCFS topic registration
    │   └── NotificationService.java           # User notifications
    │
    ├── controller/                            # 🌐 PRESENTATION LAYER (REST API)
    │   ├── AuthController.java                # /api/auth/*
    │   ├── SemesterController.java            # /api/semesters/*
    │   ├── TopicController.java               # /api/topics/*
    │   ├── ReviewController.java              # /api/reviews/*
    │   ├── TeamController.java                # /api/teams/*
    │   ├── RegistrationController.java        # /api/registrations/*
    │   └── NotificationController.java        # /api/notifications/*
    │
    └── exception/                             # ⚠️ EXCEPTION HANDLING
        └── GlobalExceptionHandler.java        # Centralized error handling
```

---

## 📊 THỐNG KÊ DỰ ÁN

| Layer | Count | Files |
|-------|-------|-------|
| **Enums** | 6 | UserRole, TopicStatus, ReviewDecision, TeamStatus, TeamMemberRole, RegistrationStatus |
| **Entities** | 8 | User, Semester, Topic, Review, Team, TeamMember, Registration, Notification |
| **Repositories** | 8 | Corresponding to each entity |
| **Services** | 8 | AuthService, SemesterService, TopicService, AIService, ReviewService, TeamService, RegistrationService, NotificationService |
| **Controllers** | 7 | Auth, Semester, Topic, Review, Team, Registration, Notification |
| **Config** | 2 | AsyncConfig, WebConfig |
| **Exception** | 1 | GlobalExceptionHandler |
| **Total** | **41** | Java files |

---

## 🔄 DATA FLOW (3-Layer)

```
┌─────────────────────────────────────────────────────────────────┐
│                      CLIENT (Browser/App)                        │
└─────────────────────────────────────────────────────────────────┘
                               │
                               ▼ HTTP Request
┌─────────────────────────────────────────────────────────────────┐
│  CONTROLLER LAYER (Presentation)                                │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │
│  │ TopicController  │ │ TeamController  │ │ RegistrationController  │               │
│  └─────────────┘ └─────────────┘ └─────────────┘               │
│  - Receive HTTP requests                                        │
│  - Validate input                                               │
│  - Return HTTP responses                                        │
└─────────────────────────────────────────────────────────────────┘
                               │
                               ▼ Method Call
┌─────────────────────────────────────────────────────────────────┐
│  SERVICE LAYER (Business Logic)                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │
│  │ TopicService │ │ AIService   │ │ ReviewService│               │
│  └─────────────┘ └─────────────┘ └─────────────┘               │
│  - Business rules                                               │
│  - Transaction management                                       │
│  - Orchestrate operations                                       │
└─────────────────────────────────────────────────────────────────┘
                               │
                               ▼ Method Call
┌─────────────────────────────────────────────────────────────────┐
│  REPOSITORY LAYER (Data Access)                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │
│  │ TopicRepository │ │ TeamRepository │ │ RegistrationRepository │               │
│  └─────────────┘ └─────────────┘ └─────────────┘               │
│  - CRUD operations                                              │
│  - Custom queries                                               │
│  - JPA/Hibernate                                                │
└─────────────────────────────────────────────────────────────────┘
                               │
                               ▼ SQL
┌─────────────────────────────────────────────────────────────────┐
│                       DATABASE (SQL Server)                      │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐        │
│  │ users  │ │ topics │ │ reviews│ │ teams  │ │registrations│        │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 API ENDPOINTS SUMMARY

| Controller | Base Path | Key Endpoints |
|------------|-----------|---------------|
| **Auth** | `/api/auth` | POST /register, POST /login, GET /users |
| **Semester** | `/api/semesters` | CRUD + PUT /{id}/activate |
| **Topic** | `/api/topics` | CRUD + POST /{id}/submit, POST /{id}/publish |
| **Review** | `/api/reviews` | POST /assign/{topicId}, POST /{id}/submit, POST /coordinator-decision/{topicId} |
| **Team** | `/api/teams` | CRUD + POST /join, POST /{id}/kick/{userId}, POST /{id}/leave |
| **Registration** | `/api/registrations` | POST, POST /{id}/approve, POST /{id}/reject, POST /{id}/finalize |
| **Notification** | `/api/notifications` | GET /user/{userId}, PUT /{id}/read |

---

*Generated: 2026-01-28 | Smart Capstone Management System v1.0*
