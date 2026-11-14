#!/usr/bin/env bash
# Chuẩn bị máy mới (Linux/macOS/WSL):  pnpm bootstrap   hoặc   bash scripts/setup.sh
#   1. Kiểm tra công cụ bắt buộc (Git, JDK 21+, Node 22.12+, pnpm, Docker + compose)
#   2. Tạo apps/backend/.env từ .env.example (tự sinh SIGNER_KEY) nếu chưa có
#   3. Cài dependency frontend, bật git hook
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

missing=0
ok() { printf '  [OK]    %-8s %s\n' "$1" "$2"; }
bad() { printf '  [THIẾU] %-8s %s\n' "$1" "$2"; missing=1; }
note() { printf '  [--]    %-8s %s\n' "$1" "$2"; }
# version_ge A B: A >= B (so sánh kiểu phiên bản)
version_ge() { [ "$(printf '%s\n%s\n' "$2" "$1" | sort -V | head -n1)" = "$2" ]; }

echo '==> Kiểm tra công cụ'

if command -v git >/dev/null 2>&1; then ok git "$(git --version | awk '{print $3}')"
else bad git 'cài Git: https://git-scm.com'; fi

if command -v java >/dev/null 2>&1; then
  java_version="$(java -version 2>&1 | sed -n 's/.*version "\([0-9][0-9.]*\).*/\1/p' | head -n1)"
  if version_ge "${java_version:-0}" 21; then ok java "$java_version"
  else bad java "đang là ${java_version:-?}, cần JDK 21+ (https://adoptium.net)"; fi
else bad java 'cài JDK 21+ (https://adoptium.net)'; fi

if command -v node >/dev/null 2>&1; then
  node_version="$(node -v | sed 's/^v//')"
  if version_ge "$node_version" 22.12; then ok node "$node_version"
  else bad node "đang là $node_version, cần Node 22.12+ (khuyên dùng 24)"; fi
else bad node 'cài Node 22.12+ (khuyên dùng 24): https://nodejs.org'; fi

if pnpm_version="$(pnpm -v 2>/dev/null)"; then ok pnpm "$pnpm_version"
else bad pnpm 'chạy: corepack enable   (hoặc npm i -g pnpm@10)'; fi

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  ok docker "$(docker compose version --short 2>/dev/null || echo compose)"
else bad docker 'cài Docker Engine/Desktop kèm plugin compose: https://docs.docker.com/get-docker/'; fi

if command -v direnv >/dev/null 2>&1; then note direnv 'có (tuỳ chọn): chạy "direnv allow apps/backend" nếu muốn dùng'
else note direnv 'không có (tuỳ chọn, backend tự đọc .env)'; fi

if [ "$missing" -ne 0 ]; then
  echo
  echo 'Còn thiếu công cụ ở trên. Cài xong rồi chạy lại: pnpm bootstrap' >&2
  exit 1
fi

echo '==> File môi trường'
ENV_FILE=apps/backend/.env
if [ -f "$ENV_FILE" ]; then
  echo "  $ENV_FILE đã có, giữ nguyên."
  if grep -qE "^[A-Z_]+=[\"']" "$ENV_FILE"; then
    echo '  [LƯU Ý] .env có giá trị bọc ngoặc kép/đơn. Hãy bỏ ngoặc (KEY=value),'
    echo '          nếu không backend sẽ đọc cả dấu ngoặc khi chạy ./mvnw trực tiếp.'
  fi
else
  key="$(node -e "console.log(require('crypto').randomBytes(48).toString('hex'))")"
  sed "s/^SIGNER_KEY=.*/SIGNER_KEY=$key/" apps/backend/.env.example > "$ENV_FILE"
  echo "  Đã tạo $ENV_FILE (SIGNER_KEY sinh ngẫu nhiên)."
  echo '  Muốn gửi email OTP thì điền EMAIL_ACCOUNT và EMAIL_APP_PASSWORD trong file đó.'
fi

echo '==> Dependency frontend'
(cd apps/frontend && pnpm install --frozen-lockfile)

echo '==> Git hook'
git config core.hooksPath .githooks
chmod +x .githooks/* scripts/*.sh apps/backend/mvnw 2>/dev/null || true
echo '  Đã bật .githooks (pre-commit: lint file thay đổi).'

echo
echo 'Xong. Chạy dự án: pnpm dev   rồi mở http://localhost:3000'
