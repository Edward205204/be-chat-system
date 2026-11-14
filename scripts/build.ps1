<#
  Build cả dự án (giống những gì CI sẽ chạy):  pnpm build   hoặc   pwsh scripts/build.ps1
  - Backend : mvnw verify (compile + unit test, không cần Docker hay .env)
  - Frontend: pnpm install --frozen-lockfile, lint, build
#>
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [Text.Encoding]::UTF8
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

Step 'Backend verify' 'apps/backend' { .\mvnw.cmd -B -q verify }
Step 'Frontend install' 'apps/frontend' { pnpm install --frozen-lockfile --config.confirmModulesPurge=false }
Step 'Frontend lint' 'apps/frontend' { pnpm lint }
Step 'Frontend build' 'apps/frontend' { pnpm build }
Write-Host 'Build OK' -ForegroundColor Green
