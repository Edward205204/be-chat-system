# Chat System

Ứng dụng chat kiểu Discord: server, channel, role/permission, chat realtime qua STOMP.

| Phần | Công nghệ | Thư mục |
|------|-----------|---------|
| Backend | Spring Boot 4, Java 21, PostgreSQL, Flyway | [`apps/backend`](apps/backend) |
| Frontend | React 19, Vite, TanStack Router/Query, pnpm | [`apps/frontend`](apps/frontend) |
| Database | Postgres 16 (Docker) | [`docker-compose.yml`](docker-compose.yml) |

## Cần cài trước

Chạy được trên Windows, Linux, macOS và WSL. `pnpm bootstrap` sẽ kiểm tra giúp từng món trong bảng bắt buộc.

### Bắt buộc

| Công cụ | Phiên bản | Dùng để | Kiểm tra |
|---------|-----------|---------|----------|
| Git | bất kỳ bản gần đây | mã nguồn, git hook | `git --version` |
| JDK | 21 trở lên (CI dùng Temurin 21) | build và chạy backend | `java -version` |
| Node.js | 22.12 trở lên, khuyên dùng 24 | frontend và các script ở thư mục gốc | `node -v` |
| pnpm | 10 (ghi ở `packageManager`) | cài dependency frontend, chạy lệnh | `pnpm -v` |
| Docker + plugin compose | bất kỳ bản gần đây | Postgres cho môi trường dev | `docker compose version` |

Ghi chú cài đặt:

- **pnpm**: cách gọn nhất là `corepack enable` (corepack đi kèm Node), nó tự lấy đúng bản ghi trong `package.json`. Hoặc `npm i -g pnpm@10`.
- **Maven**: không cần cài. `apps/backend/mvnw` tự tải Maven 3.9 ở lần chạy đầu (cần mạng).
- **Docker trên Windows**: dùng Docker Desktop, hoặc Docker Engine cài bên trong WSL. Với cách thứ hai, `pnpm dev` mở thêm một cửa sổ WSL để giữ Postgres sống, đừng đóng nó khi đang dev.
- **PowerShell 7** (`pwsh`, chỉ Windows): khuyên dùng. Không có thì script tự dùng Windows PowerShell có sẵn.

### Tuỳ chọn

| Công cụ | Khi nào cần |
|---------|-------------|
| [direnv](https://direnv.net) | Muốn biến trong `.env` có sẵn trong shell khi `cd apps/backend`. Backend tự đọc `.env` nên không bắt buộc. Nếu dùng: `direnv allow apps/backend`. |
| [Bruno](https://www.usebruno.com) | Mở bộ request API có sẵn ở `apps/backend/api`. |
| Plugin Lombok cho IDE | IntelliJ / VS Code hiểu code sinh bởi Lombok và MapStruct. Build bằng Maven thì không cần. |
| Gmail App Password | Tính năng gửi OTP và quên mật khẩu. Thiếu thì các luồng khác vẫn chạy. |

## Bắt đầu trên máy mới

```bash
git clone git@github.com:Edward205204/be-chat-system.git chat_system
cd chat_system
pnpm bootstrap   # kiểm tra công cụ, tạo apps/backend/.env, cài dependency FE, bật git hook
pnpm dev         # Postgres + Backend (:8080) + Frontend (:3000)
```

Mở http://localhost:3000.

`pnpm bootstrap` tạo `apps/backend/.env` từ [`.env.example`](apps/backend/.env.example) với `SIGNER_KEY` sinh ngẫu nhiên và tài khoản Postgres mặc định, nên chạy được ngay. Chỉ cần sửa file này khi muốn gửi email OTP (`EMAIL_ACCOUNT`, `EMAIL_APP_PASSWORD`).

## Lệnh thường dùng

Tất cả chạy từ thư mục gốc, giống nhau trên mọi hệ điều hành.

| Lệnh | Việc |
|------|------|
| `pnpm bootstrap` | chuẩn bị máy (chạy lại bao nhiêu lần cũng được) |
| `pnpm dev` | DB + BE + FE |
| `pnpm dev:db` / `dev:be` / `dev:fe` | chỉ chạy 1 phần |
| `pnpm db:stop` | tắt Postgres |
| `pnpm build` | BE verify (compile + unit test) và FE lint + build, giống CI |
| `pnpm test:be` | unit test backend (không cần Docker hay `.env`) |
| `pnpm format:be` | format code Java bằng Spotless |

Trên Windows, `pnpm dev` mở cửa sổ riêng cho BE và FE. Trên Linux/macOS/WSL, BE và FE chạy chung terminal, Ctrl+C để tắt cả hai.

## Biến môi trường

- Chỉ backend cần file env: `apps/backend/.env` (không commit, mẫu ở `.env.example`). Frontend không cần biến nào, Vite proxy `/rchat` sang `:8080`.
- Backend tự nạp `.env` khi khởi động từ `apps/backend` (`spring.config.import` trong `application.yaml`), nên chạy bằng `pnpm dev`, `./mvnw spring-boot:run` hay IDE đều giống nhau. Biến môi trường thật của hệ thống được ưu tiên hơn giá trị trong `.env`.
- Viết `.env` dạng `KEY=value`, không bọc ngoặc kép và không có `export`, vì Spring đọc nguyên văn phần sau dấu `=`.
- `docker-compose.yml` đọc `DB_USERNAME` / `DB_PASSWORD` từ cùng file đó.

## Chạy bằng Docker (production)

Dùng khi muốn chạy thử hoặc triển khai mà máy chỉ có Docker. Không dùng cho dev hằng ngày, vì mỗi lần đổi code hay dependency phải build lại image.

```bash
docker compose -f docker-compose.prod.yml --env-file apps/backend/.env up -d --build
```

Mở http://localhost:8088. Image backend chỉ có JRE 21 và file jar, image frontend chỉ có nginx và file tĩnh; JDK, Maven, Node, pnpm chỉ tồn tại ở stage build. Chỉ nginx mở cổng ra ngoài, nó proxy `/rchat` (REST và WebSocket) sang backend.

| Biến (đặt trong file env hoặc shell) | Mặc định | Ý nghĩa |
|------|----------|---------|
| `APP_PORT` | `8088` | cổng mở trên máy host |
| `APP_FRONTEND_URL` | `http://localhost:8088` | origin người dùng truy cập, backend dùng cho WebSocket và link mời |

Khi triển khai thật, hãy dùng file env riêng với `SIGNER_KEY` và mật khẩu DB mạnh, và đặt HTTPS ở phía trước.

## Cấu trúc

```
.
├── apps/
│   ├── backend/          Spring Boot API  (http://localhost:8080/rchat)
│   └── frontend/         React SPA        (http://localhost:3000)
├── scripts/              setup / dev / build (.ps1 cho Windows, .sh cho Linux/macOS/WSL), run.mjs chọn theo hệ điều hành
├── .githooks/            pre-commit: ESLint và Spotless cho file đang commit
├── .github/              CI, PR template, CODEOWNERS
├── docker-compose.yml    Postgres cho môi trường dev
├── docker-compose.prod.yml  DB + BE + FE bằng image production
└── package.json          lối tắt lệnh: pnpm dev, pnpm build, ...
```

## Quy ước làm việc

- **Nhánh**: không commit thẳng vào `main`. Tạo nhánh `feat/...`, `fix/...`, `chore/...` rồi mở Pull Request.
- **Commit**: theo [Conventional Commits](https://www.conventionalcommits.org), ví dụ `fix(chat): ...`, `feat(server): ...`, `chore(monorepo): ...`.
- **Trước khi mở PR**: `pnpm build` phải sạch. CI chạy đúng những bước đó.
- **Pre-commit hook**: bật sẵn sau `pnpm bootstrap`. Nó chạy ESLint cho file FE và Spotless cho file Java đang commit. Lỗi format Java sửa bằng `pnpm format:be`.
- **Database**: thay đổi schema bằng file Flyway mới `V<n>__mo_ta.sql`, không sửa migration đã có.
- **Secret**: không commit `.env`, key hay mật khẩu. Thêm biến mới thì cập nhật `.env.example`.

## Tài liệu

- Backend: [`apps/backend/README.md`](apps/backend/README.md).
- Frontend: [`apps/frontend/CONVENTIONS.md`](apps/frontend/CONVENTIONS.md).
