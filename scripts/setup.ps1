<#
  Chuẩn bị máy mới (Windows):  pnpm bootstrap   hoặc   pwsh scripts/setup.ps1
  1. Kiểm tra công cụ bắt buộc (Git, JDK 21+, Node 22.12+, pnpm, Docker + compose)
  2. Tạo apps/backend/.env từ .env.example (tự sinh SIGNER_KEY) nếu chưa có
  3. Cài dependency frontend, bật git hook
#>
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [Text.Encoding]::UTF8
$Root = Split-Path -Parent $PSScriptRoot
$script:Missing = $false

function Ok([string]$Name, [string]$Text) { Write-Host ('  [OK]    {0,-8} {1}' -f $Name, $Text) -ForegroundColor Green }
function Bad([string]$Name, [string]$Text) { Write-Host ('  [THIẾU] {0,-8} {1}' -f $Name, $Text) -ForegroundColor Red; $script:Missing = $true }
function Note([string]$Name, [string]$Text) { Write-Host ('  [--]    {0,-8} {1}' -f $Name, $Text) -ForegroundColor DarkGray }
function Has([string]$Name) { [bool](Get-Command $Name -ErrorAction SilentlyContinue) }

Push-Location $Root
try {
  Write-Host '==> Kiểm tra công cụ' -ForegroundColor Cyan

  if (Has git) { Ok git ((git --version) -replace 'git version ', '') }
  else { Bad git 'cài Git: https://git-scm.com' }

  if (Has java) {
    # java -version in ra stderr; gom qua cmd để PowerShell không coi là lỗi.
    $javaText = (cmd /c 'java -version 2>&1') -join ' '
    if ($javaText -match 'version "(\d+)') {
      if ([int]$Matches[1] -ge 21) { Ok java $Matches[1] }
      else { Bad java "đang là $($Matches[1]), cần JDK 21+ (https://adoptium.net)" }
    }
    else { Bad java 'không đọc được phiên bản, cần JDK 21+' }
  }
  else { Bad java 'cài JDK 21+ (https://adoptium.net)' }

  if (Has node) {
    $nodeVersion = [version]((node -v) -replace '^v', '')
    if ($nodeVersion -ge [version]'22.12') { Ok node $nodeVersion }
    else { Bad node "đang là $nodeVersion, cần Node 22.12+ (khuyên dùng 24)" }
  }
  else { Bad node 'cài Node 22.12+ (khuyên dùng 24): https://nodejs.org' }

  if (Has pnpm) { Ok pnpm (pnpm -v) }
  else { Bad pnpm 'chạy: corepack enable   (hoặc npm i -g pnpm@10)' }

  # Docker: Docker Desktop trên Windows, hoặc docker bên trong WSL.
  $dockerOk = $false
  if (Has docker) {
    cmd /c 'docker compose version >nul 2>&1'
    if ($LASTEXITCODE -eq 0) { Ok docker 'Docker trên Windows'; $dockerOk = $true }
  }
  if (-not $dockerOk -and (Has wsl)) {
    cmd /c 'wsl -e docker compose version >nul 2>&1'
    if ($LASTEXITCODE -eq 0) { Ok docker 'Docker trong WSL (pnpm dev sẽ mở 1 cửa sổ WSL giữ Postgres)'; $dockerOk = $true }
  }
  if (-not $dockerOk) { Bad docker 'cài Docker Desktop, hoặc Docker Engine + compose trong WSL' }

  if (Has pwsh) { Note pwsh 'PowerShell 7 có (khuyên dùng)' }
  else { Note pwsh 'không có PowerShell 7, sẽ dùng Windows PowerShell' }

  if ($script:Missing) {
    Write-Host ''
    Write-Host 'Còn thiếu công cụ ở trên. Cài xong rồi chạy lại: pnpm bootstrap' -ForegroundColor Red
    exit 1
  }

  Write-Host '==> File môi trường' -ForegroundColor Cyan
  $EnvFile = Join-Path $Root 'apps/backend/.env'
  if (Test-Path $EnvFile) {
    Write-Host '  apps/backend/.env đã có, giữ nguyên.'
    if (Select-String -Path $EnvFile -Pattern '^[A-Z_]+=["'']' -Quiet) {
      Write-Host '  [LƯU Ý] .env có giá trị bọc ngoặc kép/đơn. Hãy bỏ ngoặc (KEY=value),' -ForegroundColor Yellow
      Write-Host '          nếu không backend sẽ đọc cả dấu ngoặc khi chạy mvnw trực tiếp.' -ForegroundColor Yellow
    }
  }
  else {
    $key = node -e "console.log(require('crypto').randomBytes(48).toString('hex'))"
    $lines = Get-Content (Join-Path $Root 'apps/backend/.env.example') -Encoding UTF8 |
      ForEach-Object { if ($_ -match '^SIGNER_KEY=') { "SIGNER_KEY=$key" } else { $_ } }
    # Ghi UTF-8 không BOM để Spring và docker compose đọc đúng dòng đầu.
    [IO.File]::WriteAllLines($EnvFile, [string[]]$lines, (New-Object Text.UTF8Encoding $false))
    Write-Host '  Đã tạo apps/backend/.env (SIGNER_KEY sinh ngẫu nhiên).'
    Write-Host '  Muốn gửi email OTP thì điền EMAIL_ACCOUNT và EMAIL_APP_PASSWORD trong file đó.'
  }

  Write-Host '==> Dependency frontend' -ForegroundColor Cyan
  Push-Location (Join-Path $Root 'apps/frontend')
  try {
    pnpm install --frozen-lockfile --config.confirmModulesPurge=false
    if ($LASTEXITCODE -ne 0) { throw "pnpm install thất bại (exit $LASTEXITCODE)" }
  }
  finally { Pop-Location }

  Write-Host '==> Git hook' -ForegroundColor Cyan
  git config core.hooksPath .githooks
  Write-Host '  Đã bật .githooks (pre-commit: lint file thay đổi).'

  Write-Host ''
  Write-Host 'Xong. Chạy dự án: pnpm dev   rồi mở http://localhost:3000' -ForegroundColor Green
}
finally { Pop-Location }
