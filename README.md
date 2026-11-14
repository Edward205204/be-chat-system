# Chat System

Ứng dụng chat kiểu Discord: server, channel, role/permission, chat realtime qua STOMP.

| Phần | Công nghệ | Thư mục |
|------|-----------|---------|
| Backend | Spring Boot 4, Java 21, PostgreSQL, Flyway | [`apps/backend`](apps/backend) |
| Frontend | React 19, Vite, TanStack Router/Query, pnpm | [`apps/frontend`](apps/frontend) |
| Database | Postgres 16 (Docker) | [`docker-compose.yml`](docker-compose.yml) |

## Cấu trúc

```
.
├── apps/
│   ├── backend/          Spring Boot API  (http://localhost:8080/rchat)
│   └── frontend/         React SPA        (http://localhost:3000)
├── scripts/              dev.ps1 (chạy cả dự án), build.ps1 (build như CI)
├── .github/workflows/    CI: build + test BE, lint + build FE
├── docker-compose.yml    Postgres cho môi trường dev
└── package.json          lối tắt lệnh: pnpm dev, pnpm build, ...
```

## Chạy nhanh (Windows)

Yêu cầu: Java 21, pnpm, Docker (Docker Desktop hoặc Docker trong WSL), PowerShell 7 (`pwsh`).

1. Tạo `apps/backend/.env` (xem [README backend](apps/backend/README.md#biến-môi-trường-env)).
2. Từ thư mục gốc:

```bash
pnpm dev
```

Lệnh này bật Postgres, rồi mở cửa sổ riêng cho Backend (`:8080`) và Frontend (`:3000`). Nếu Docker chạy trong WSL thì có thêm 1 cửa sổ WSL giữ Postgres sống, đừng đóng nó khi đang dev. Mở http://localhost:3000.

| Lệnh | Việc |
|------|------|
| `pnpm dev` | DB + BE + FE |
| `pnpm dev:db` / `dev:be` / `dev:fe` | chỉ chạy 1 phần |
| `pnpm db:stop` | tắt Postgres |
| `pnpm build` | build BE + lint/build FE (như CI) |
| `pnpm test:be` | test backend (cần Docker cho Testcontainers) |

## Tài liệu

- Backend: [`apps/backend/README.md`](apps/backend/README.md), đặc tả API cho FE ở `apps/backend/docs/api/`.
- Frontend: [`apps/frontend/CONVENTIONS.md`](apps/frontend/CONVENTIONS.md).
