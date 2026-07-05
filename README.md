<div align="center">

# 💬 be-chat-system

Backend của một ứng dụng chat thời gian thực lấy cảm hứng từ Discord.

[![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

[![GitHub commit activity](https://img.shields.io/github/commit-activity/m/Edward205204/be-chat-system?style=flat-square&label=commits%2Fmonth&color=302b63)](https://github.com/Edward205204/be-chat-system/commits/main)
[![GitHub last commit](https://img.shields.io/github/last-commit/Edward205204/be-chat-system?style=flat-square&color=6c63ff)](https://github.com/Edward205204/be-chat-system/commits/main)
[![Java 98.5%](https://img.shields.io/badge/Java-98.5%25-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://github.com/Edward205204/be-chat-system)

</div>

---

> Dự án được xây dựng với Java / Spring Boot, tập trung vào các khía cạnh của một backend enterprise: security, realtime communication, RBAC và data modeling.

---

## 📌 Tổng quan

`be-chat-system` là backend của một ứng dụng chat theo phong cách **Discord**, bao gồm các tính năng:

- Đăng ký / đăng nhập với xác thực email OTP
- JWT stateless authentication (Access token + Refresh token)
- Quản lý Server (guild), Channel, thành viên
- Phân quyền RBAC theo Server và Channel
- Chat realtime qua **WebSocket (STOMP)**
- Gửi email (OTP, thông báo) qua SMTP + Thymeleaf templates

---

## 🛠 Tech Stack

| Layer         | Technology                                                                 |
|---------------|----------------------------------------------------------------------------|
| Language      | Java 21                                                                    |
| Framework     | Spring Boot 4.1                                                            |
| Web           | Spring MVC (REST) + Spring WebSocket (STOMP)                               |
| Security      | Spring Security · OAuth2 Resource Server · JWT (`nimbus-jose-jwt` HS512)  |
| ORM           | Spring Data JPA · Hibernate · PostgreSQL dialect                           |
| Database      | PostgreSQL 16 (prod) · H2 (dev/test)                                       |
| Mapping       | MapStruct 1.5.5 + Lombok                                                   |
| Mail          | Spring Mail (SMTP Gmail) + Thymeleaf email templates                       |
| Testing       | JUnit 5 · Testcontainers · JaCoCo · Spring Boot Test slices                |
| Code style    | Spotless — Google Java Format AOSP 1.25.2                                  |
| Container     | Docker Compose (PostgreSQL 16-alpine)                                      |
| Build tool    | Maven (mvnw wrapper)                                                       |

---

## 📁 Cấu trúc dự án

```
src/main/java/com/edward/chat_system/
│
├── ChatSystemApplication.java
│
├── features/                      # Tổ chức theo feature (vertical slice)
│   ├── auth/                      # Xác thực & phân quyền truy cập
│   │   ├── controller/            # AuthController
│   │   ├── dto/                   # Request / Response DTOs
│   │   ├── entity/                # RefreshToken, VerificationCode
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/               # AuthService
│   │
│   ├── user/                      # Quản lý người dùng
│   │   ├── controller/            # UserController
│   │   ├── dto/
│   │   ├── entity/                # User
│   │   ├── mapper/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── server/                    # Server (Discord-style guild)
│   │   ├── controller/            # ServerController
│   │   ├── dto/
│   │   ├── entity/                # Server, ServerMember, ServerBan, InviteLink, ServerInvitation
│   │   ├── enums/
│   │   ├── mapper/
│   │   ├── projection/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── channel/                   # Kênh chat trong Server
│   │   ├── constant/
│   │   ├── dto/
│   │   ├── entity/                # Channel
│   │   ├── enums/
│   │   ├── mapper/
│   │   ├── projection/
│   │   ├── repository/
│   │   └── service/               # ChannelService
│   │
│   ├── permission/                # Hệ thống phân quyền RBAC
│   │   ├── constant/              # RoleConstants
│   │   ├── controller/            # RoleController, ServerPermissionController, ChannelPermissionController
│   │   ├── dto/
│   │   ├── entity/                # Role, RoleMember, ServerRolePermission, ChannelRolePermission, ChannelUserPermission
│   │   ├── mapper/
│   │   ├── projection/
│   │   ├── repository/
│   │   └── service/
│   │
│   └── chat/                      # Nhắn tin realtime
│       ├── controller/            # ChatController (WebSocket STOMP)
│       ├── dto/
│       ├── entity/                # Message
│       ├── event/
│       ├── listener/
│       ├── repository/
│       ├── service/
│       └── websocket/             # ChannelDestination
│
├── infrastructure/                # Cơ sở hạ tầng kỹ thuật
│   ├── aop/                       # Aspect-Oriented Programming
│   │   ├── annotation/
│   │   ├── aspect/
│   │   └── validator/
│   ├── configuration/             # SecurityConfig, WebConfig, WebSocketConfig
│   ├── mail/                      # Mail service
│   └── security/
│       ├── jwt/                   # JwtSigner, JwtDecoderConfig, JwtChannelInterceptor
│       └── permission/            # Permission checkers
│
└── shared/                        # Dùng chung toàn dự án
    ├── aop/
    ├── dto/                       # ApiResponse<T>, CursorPageResponse<T>
    ├── exception/                 # AppException, ErrorCode, GlobalExceptionHandler
    └── utils/
```

---

## 🔐 Authentication Flow

```
POST /rchat/auth/register
  └─► Tạo User (isVerified=false)
  └─► Trả về temp token (JWT ngắn hạn)

POST /rchat/auth/send/email-otp   [cần temp token]
  └─► Gửi OTP qua Gmail SMTP (Thymeleaf template)

POST /rchat/auth/verify/email-otp [cần temp token]
  └─► Xác thực OTP → isVerified=true
  └─► Trả về { accessToken, refreshToken }

POST /rchat/auth/login
  └─► Email + Password → { accessToken, refreshToken }

POST /rchat/auth/refresh
  └─► refreshToken → { accessToken mới, refreshToken mới }

POST /rchat/auth/logout
  └─► Xoá refreshToken khỏi DB

POST /rchat/auth/send/forgot-password
POST /rchat/auth/reset-password
  └─► OTP qua email → đặt lại mật khẩu
```

**JWT**: ký bằng `HS512`, payload chứa `sub` (userId), `email`, `username`.  
**Token TTL**:
- Access token: `600s` (10 phút)
- Temp token: `86400s` (1 ngày)
- Refresh token: `864000s` (10 ngày)

**OTP**:
- Cooldown: `120s` giữa 2 lần gửi
- Hết hạn sau: `600s`
- Tối đa `5` lần nhập sai → OTP bị thu hồi

---

## 🌐 API Endpoints

> Base path: `http://localhost:8080/rchat`  
> Tất cả response bọc trong `ApiResponse<T>` với cấu trúc `{ message, result }`.

### Auth — `/auth`

| Method | Path                       | Mô tả                             |
|--------|----------------------------|------------------------------------|
| POST   | `/auth/register`           | Đăng ký tài khoản                  |
| POST   | `/auth/login`              | Đăng nhập                          |
| POST   | `/auth/send/email-otp`     | Gửi OTP xác thực email             |
| POST   | `/auth/verify/email-otp`   | Xác thực email bằng OTP            |
| POST   | `/auth/send/forgot-password` | Gửi OTP quên mật khẩu           |
| POST   | `/auth/reset-password`     | Đặt lại mật khẩu                  |
| POST   | `/auth/refresh`            | Làm mới Access Token               |
| POST   | `/auth/logout`             | Đăng xuất                          |

### Server — `/servers`

| Method | Path                                       | Mô tả                          |
|--------|--------------------------------------------|--------------------------------|
| GET    | `/servers`                                 | Lấy danh sách server của tôi  |
| POST   | `/servers`                                 | Tạo server mới                 |
| GET    | `/servers/{serverId}`                      | Chi tiết server                |
| PATCH  | `/servers/{serverId}`                      | Cập nhật server                |
| DELETE | `/servers/{serverId}`                      | Xoá server                     |
| GET    | `/servers/{serverId}/members`              | Danh sách thành viên           |
| DELETE | `/servers/{serverId}/members/{memberId}`   | Kick thành viên                |
| DELETE | `/servers/{serverId}/members/me`           | Rời server                     |
| PATCH  | `/servers/{serverId}/owner/{memberId}`     | Chuyển quyền chủ               |
| PATCH  | `/servers/{serverId}/members/{memberId}/mute` | Mute thành viên             |
| POST   | `/servers/{serverId}/bans/{userId}`        | Ban người dùng                 |
| DELETE | `/servers/{serverId}/bans/{userId}`        | Unban người dùng               |
| GET    | `/servers/{serverId}/bans`                 | Danh sách bị ban               |
| POST   | `/servers/{serverId}/links`                | Tạo invite link                |
| GET    | `/servers/{serverId}/links`                | Lấy danh sách invite link      |
| PATCH  | `/servers/{serverId}/links/{linkId}/revoke` | Thu hồi invite link           |
| POST   | `/servers/join/{token}`                    | Tham gia server qua invite link |

### Channel — `/servers/{serverId}/channels`

| Method | Path                                                  | Mô tả                      |
|--------|-------------------------------------------------------|----------------------------|
| GET    | `/servers/{serverId}/channels`                        | Danh sách channel          |
| POST   | `/servers/{serverId}/channels`                        | Tạo channel mới            |
| PATCH  | `/servers/{serverId}/channels/{channelId}`            | Cập nhật channel           |
| DELETE | `/servers/{serverId}/channels/{channelId}`            | Xoá channel                |
| POST   | `/servers/{serverId}/channels/{channelId}/invite`     | Thêm thành viên vào channel private |

### Permission — `/roles`, `/servers/{id}/permissions`, `/channels/{id}/permissions`

| Controller                    | Mô tả                                   |
|-------------------------------|-----------------------------------------|
| `RoleController`              | CRUD Role trong server                  |
| `ServerPermissionController`  | Gán/thu hồi permission cấp Server      |
| `ChannelPermissionController` | Gán/thu hồi permission cấp Channel     |

### Chat — WebSocket (STOMP)

| Destination         | Mô tả                        |
|---------------------|------------------------------|
| `/app/chat.send`    | Gửi tin nhắn vào channel     |
| `/topic/channel.*`  | Subscribe nhận tin nhắn      |

---

## 🗄 Domain Model

```
User
 ├─── Server (owner)
 │     ├─── ServerMember (User N:N)
 │     ├─── ServerBan
 │     ├─── InviteLink
 │     ├─── ServerInvitation
 │     └─── Channel
 │            ├─── Message (sender: User)
 │            ├─── ChannelRolePermission
 │            └─── ChannelUserPermission
 │
 ├─── Role (per Server)
 │     ├─── RoleMember
 │     └─── ServerRolePermission
 │
 ├─── RefreshToken
 └─── VerificationCode (OTP)
```

---

## ⚙️ Cấu hình

### Biến môi trường (`.env`)

```env
# JWT
SIGNER_KEY=<chuỗi >= 64 bytes cho HS512>

# Database
DB_URL=jdbc:postgresql://localhost:5432/chat_system
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Gmail SMTP
EMAIL_ACCOUNT=your@gmail.com
EMAIL_APP_PASSWORD_NAME=RChat
EMAIL_APP_PASSWORD=your_app_password
```

### `application.yaml` (tóm tắt)

```yaml
server:
  port: 8080
  servlet.context-path: /rchat

jwt:
  access-token-duration: 600       # giây
  tmp-token-duration: 86400
  refresh-token-duration: 864000

otp:
  cooldown: 120                    # giây
  valid_duration: 600
```

---

## 🚀 Chạy local

**Yêu cầu**: Docker, Java 21, Maven (hoặc dùng `mvnw`)

```bash
# 1. Khởi động PostgreSQL
docker compose up -d

# 2. Chạy ứng dụng
./mvnw spring-boot:run
```

**H2 Console** (dev): `http://localhost:8080/rchat/h2-console`  
**Actuator**: `http://localhost:8080/rchat/actuator`

---

## 🧪 Testing

```bash
# Chạy toàn bộ test
./mvnw test

# Báo cáo JaCoCo coverage
./mvnw verify
# Xem tại: target/site/jacoco/index.html
```

> Test dùng **Testcontainers** để spin up PostgreSQL thật trong Docker khi chạy integration test.  
> JaCoCo loại trừ `dto/`, `entity/`, `mapper/` khỏi báo cáo coverage.

---

## 🎨 Code Style

Dự án dùng **Spotless** với Google Java Format (style AOSP):

```bash
# Kiểm tra format
./mvnw spotless:check

# Tự động format
./mvnw spotless:apply
```

---

## 📈 Tiến độ

```
[████████░░░░░░░░░░░░] ~40%
```

- [x] Project setup — Spring Boot 4.1, Maven, Docker Compose
- [x] Entity design — domain model, relationships, DB indexes
- [x] Authentication — JWT (HS512), Spring Security, OAuth2 Resource Server
- [x] Email OTP — đăng ký, forgot password (SMTP Gmail + Thymeleaf)
- [x] Server CRUD — tạo, sửa, xoá, xem
- [x] Server Members — join, kick, leave, mute, transfer owner
- [x] Ban system — ban / unban, danh sách ban
- [x] Invite Link — tạo link, revoke, join qua token
- [x] Channel CRUD — tạo, sửa, xoá, danh sách (cursor pagination)
- [x] RBAC — Role, ServerRolePermission, ChannelRolePermission, ChannelUserPermission
- [x] WebSocket setup — STOMP config, JWT channel interceptor
- [x] Chat — gửi & nhận tin nhắn realtime qua WebSocket
- [ ] Direct Invitation (server invite by user ID)
- [ ] File upload (avatar, banner, attachment)
- [ ] Direct Message (DM giữa 2 người dùng)
- [ ] Read receipts / Typing indicator
- [ ] Notification service
- [ ] ...

---

## 💡 Mục tiêu

Chat system là một bài toán phù hợp để thực hành đồng thời nhiều khía cạnh của backend: **authentication**, **realtime communication**, **RBAC**, và **relational data modeling** — thay vì một CRUD app đơn thuần.

Stack Java / Spring Boot được chọn vì tính phổ biến trong môi trường enterprise và hệ sinh thái trưởng thành xung quanh (Security, JPA, WebSocket, Testing).

---

## 🔗 Liên quan

- 👤 [GitHub Profile](https://github.com/Edward205204)
- 📦 [BE_Learn_Proof](https://github.com/Edward205204/BE_Learn_Proof) — Dự án capstone NestJS trước đó
