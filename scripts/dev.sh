#!/usr/bin/env bash
# Chạy cả dự án bằng 1 lệnh (Linux/macOS/WSL):  pnpm dev   hoặc   bash scripts/dev.sh [all|db|be|fe|stop]
#   db   : bật Postgres (docker compose)
#   be   : Backend  (apps/backend, ./mvnw spring-boot:run, port 8080)
#   fe   : Frontend (apps/frontend, pnpm dev, port 3000)
#   all  : cả ba, BE và FE chạy chung terminal này, Ctrl+C để tắt cả hai
#   stop : tắt Postgres
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ONLY="${1:-all}"
cd "$ROOT"

case "$ONLY" in
  all | db | be | fe | stop) ;;
  *) echo "Tham số không hợp lệ: $ONLY (dùng: all|db|be|fe|stop)" >&2; exit 2 ;;
esac

if [ ! -f apps/backend/.env ]; then
  echo 'Thiếu apps/backend/.env. Chạy trước: pnpm bootstrap' >&2
  exit 1
fi

compose() { docker compose --env-file apps/backend/.env "$@"; }

if [ "$ONLY" = stop ]; then
  compose stop
  echo 'Đã tắt Postgres.'
  exit 0
fi

if [ "$ONLY" = all ] || [ "$ONLY" = db ]; then
  echo '==> Postgres'
  compose up -d
  printf 'Chờ Postgres sẵn sàng (localhost:5432)'
  ready=0
  for _ in $(seq 1 60); do
    if (exec 3<>/dev/tcp/127.0.0.1/5432) 2>/dev/null; then ready=1; break; fi
    printf '.'
    sleep 1
  done
  if [ "$ready" -ne 1 ]; then
    echo
    echo 'Postgres không lên sau 60s. Xem: docker logs chat_system_db' >&2
    exit 1
  fi
  echo ' OK'
fi

run_be() { cd "$ROOT/apps/backend" && exec ./mvnw spring-boot:run; }
run_fe() {
  cd "$ROOT/apps/frontend"
  [ -d node_modules ] || pnpm install --frozen-lockfile
  exec pnpm dev
}

case "$ONLY" in
  be) echo '==> Backend  http://localhost:8080/rchat'; run_be ;;
  fe) echo '==> Frontend http://localhost:3000'; run_fe ;;
  all)
    echo '==> Backend  http://localhost:8080/rchat'
    echo '==> Frontend http://localhost:3000   (Ctrl+C để tắt cả hai, rồi pnpm db:stop)'
    trap 'trap - INT TERM EXIT; kill 0' INT TERM EXIT
    (run_be) &
    (run_fe) &
    wait
    ;;
esac
