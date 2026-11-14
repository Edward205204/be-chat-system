# fe_chat_system

FE demo cho backend `chat_system` (Spring Boot, `localhost:8080/rchat`).

- Luật code: đọc `CONVENTIONS.md` trước khi sửa gì (stack 1-lib-1-việc, feature-based, naming).
- Đặc tả API: `../backend/docs/api/` (README, 01..07). Type: `src/types/api.ts`.
- Chạy: `pnpm install` → `pnpm dev` (port 3000, proxy `/rchat` + WS sang `:8080`). Cả dự án: `pnpm dev` ở thư mục gốc monorepo.
- Trước khi xong: `pnpm lint` và `pnpm build` phải sạch.
