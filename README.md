<div align="center">

# 💬 be-chat-system

**Dự án học Java đầu tiên** — Xây dựng hệ thống chat thời gian thực lấy cảm hứng từ Discord.

[![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

[![GitHub commit activity](https://img.shields.io/github/commit-activity/m/Edward205204/be-chat-system?style=flat-square&label=commits%2Fmonth&color=302b63)](https://github.com/Edward205204/be-chat-system/commits/main)
[![GitHub last commit](https://img.shields.io/github/last-commit/Edward205204/be-chat-system?style=flat-square&color=6c63ff)](https://github.com/Edward205204/be-chat-system/commits/main)
[![Java 98.5%](https://img.shields.io/badge/Java-98.5%25-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://github.com/Edward205204/be-chat-system)

</div>

---

> 🌱 **Đây là dự án Java đầu tiên của mình.** Trước đó toàn làm Node.js / NestJS — qua Spring Boot để hiểu sâu hơn về cách một backend "enterprise-style" thực sự vận hành: dependency injection, bean lifecycle, JPA, Spring Security... Vừa học vừa build.

---

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 |
| Security | Spring Security + OAuth2 Resource Server + JWT (nimbus-jose-jwt) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL (prod) · H2 (dev/test) |
| Mapping | MapStruct + Lombok |
| Mail | Spring Mail + Thymeleaf templates |
| Testing | JUnit 5 · Testcontainers · JaCoCo |
| Code style | Spotless (Google Java Format AOSP) |
| Container | Docker Compose |

---

## Tiến độ

```
[██████░░░░░░░░░░░░░░] ~30%
```

- [x] Project setup — Spring Boot 4.1, Maven, Docker Compose
- [x] Entity design — domain model, relationships
- [x] Authentication — JWT, Spring Security, OAuth2 Resource Server
- [ ] WebSocket / realtime messaging
- [ ] Channels & Servers (Discord-style)
- [ ] File upload
- [ ] Email notification
- [ ] ...

---

## Tại sao dự án này?

Mình chọn build một chat system vì nó buộc phải đụng vào nhiều thứ cùng lúc:
**auth**, **realtime**, **data modeling**, **rbac** — không phải CRUD tutorial.  
Và chọn Java/Spring Boot vì muốn hiểu tại sao nó vẫn là backbone của phần lớn backend enterprise sau bao nhiêu năm.

---

## Chạy local

```bash
# Start database
docker compose up -d

# Run app
./mvnw spring-boot:run
```

---

## Liên quan

- 👤 [GitHub Profile](https://github.com/Edward205204)
- 📦 [BE_Learn_Proof](https://github.com/Edward205204/BE_Learn_Proof) — Dự án capstone NestJS trước đó
