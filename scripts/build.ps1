<#
  Build cả dự án (giống những gì CI sẽ chạy):  pnpm build   hoặc   pwsh scripts/build.ps1
  - Backend : mvnw package (bỏ test vì test cần Docker/Testcontainers; chạy riêng: pnpm test:be)
  - Frontend: pnpm install --frozen-lockfile, lint, build
#>
$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot

function Step([string]$Name, [string]$Dir, [scriptblock]$Cmd) {
  Write-Host "==> $Name" -ForegroundColor Cyan
  Push-Location (Join-Path $Root $Dir)
  try {
    & $Cmd
    if ($LASTEXITCODE -ne 0) { throw "$Name thất bại (exit $LASTEXITCODE)" }
  }
  finally { Pop-Location }
}

Step 'Backend build' 'apps/backend' { .\mvnw.cmd -B -q package -DskipTests }
Step 'Frontend install' 'apps/frontend' { pnpm install --frozen-lockfile --config.confirmModulesPurge=false }
Step 'Frontend lint' 'apps/frontend' { pnpm lint }
Step 'Frontend build' 'apps/frontend' { pnpm build }
Write-Host 'Build OK' -ForegroundColor Green
