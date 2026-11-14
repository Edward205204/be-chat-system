#!/usr/bin/env bash
# Build cả dự án (giống CI, Linux/macOS/WSL):  pnpm build   hoặc   bash scripts/build.sh
#   Backend : ./mvnw verify (compile + unit test, không cần Docker hay .env)
#   Frontend: pnpm install --frozen-lockfile, lint, build
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

step() { echo "==> $1"; }

step 'Backend verify'
(cd "$ROOT/apps/backend" && ./mvnw -B -q verify)
step 'Frontend install'
(cd "$ROOT/apps/frontend" && pnpm install --frozen-lockfile)
step 'Frontend lint'
(cd "$ROOT/apps/frontend" && pnpm lint)
step 'Frontend build'
(cd "$ROOT/apps/frontend" && pnpm build)
echo 'Build OK'
