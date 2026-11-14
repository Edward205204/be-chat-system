<#
  Chạy cả dự án bằng 1 lệnh (Windows):  pnpm dev   hoặc   pwsh scripts/dev.ps1

  1. Bật Postgres (docker compose, dùng docker trong WSL nếu Windows không có docker)
  2. Mở 1 cửa sổ chạy Backend  (apps/backend, mvnw spring-boot:run, port 8080)
  3. Mở 1 cửa sổ chạy Frontend (apps/frontend, pnpm dev, port 3000)

  Tuỳ chọn:
    db|be|fe        chỉ chạy 1 phần (vd: pwsh scripts/dev.ps1 be)
    stop            tắt Postgres (BE/FE: đóng cửa sổ của chúng)
#>
param(
  [ValidateSet('all', 'db', 'be', 'fe', 'stop')]
  [string]$Only = 'all',
  [switch]$Stop
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [Text.Encoding]::UTF8
$Root = Split-Path -Parent $PSScriptRoot
$Backend = Join-Path $Root 'apps/backend'
$Frontend = Join-Path $Root 'apps/frontend'
$EnvFile = Join-Path $Backend '.env'

function Invoke-Docker {
  # Ưu tiên docker của Windows (Docker Desktop), không có thì dùng docker trong WSL.
  if (Get-Command docker -ErrorAction SilentlyContinue) { & docker @args }
  else { & wsl -e docker @args }
  if ($LASTEXITCODE -ne 0) { throw "docker $($args -join ' ') thất bại (exit $LASTEXITCODE)" }
}

function Import-DotEnv([string]$Path) {
  if (-not (Test-Path $Path)) { throw "Thiếu file $Path. Chạy trước: pnpm bootstrap" }
  foreach ($line in Get-Content $Path) {
    $l = $line.Trim()
    if ($l -eq '' -or $l.StartsWith('#') -or -not $l.Contains('=')) { continue }
    $key, $value = $l.Split('=', 2)
    $value = $value.Trim().Trim('"').Trim("'")
    [Environment]::SetEnvironmentVariable($key.Trim(), $value, 'Process')
  }
}

Push-Location $Root
try {
  if ($Stop -or $Only -eq 'stop') {
    Invoke-Docker compose --env-file apps/backend/.env stop
    Write-Host 'Đã tắt Postgres. Đóng cửa sổ BE/FE để tắt chúng.' -ForegroundColor Yellow
    return
  }

  Import-DotEnv $EnvFile

  if ($Only -in 'all', 'db') {
    Write-Host '==> Postgres' -ForegroundColor Cyan
    # Đường dẫn tương đối để chạy được cả qua WSL.
    if (Get-Command docker -ErrorAction SilentlyContinue) {
      Invoke-Docker compose --env-file apps/backend/.env up -d
    }
    else {
      # WSL tự tắt khi không còn tiến trình nào gắn vào => Postgres chết theo.
      # Nên giữ 1 cửa sổ "chat-system DB" chạy compose ở chế độ attached (xem log DB ở đó).
      Write-Host '    (Docker trong WSL: mở 1 cửa sổ WSL chạy Postgres, đừng đóng khi đang dev)'
      Start-Process wsl -WorkingDirectory $Root -ArgumentList '-e', 'docker', 'compose',
        '--env-file', 'apps/backend/.env', 'up'
    }
    Write-Host -NoNewline 'Chờ Postgres sẵn sàng (localhost:5432)'
    $ready = $false
    for ($i = 0; $i -lt 60 -and -not $ready; $i++) {
      try { $c = [Net.Sockets.TcpClient]::new('localhost', 5432); $c.Close(); $ready = $true }
      catch { Write-Host -NoNewline '.'; Start-Sleep -Seconds 1 }
    }
    if (-not $ready) { throw 'Postgres không lên sau 60s. Xem cửa sổ DB / docker logs chat_system_db' }
    Write-Host ' OK'
  }

  $shell = if (Get-Command pwsh -ErrorAction SilentlyContinue) { 'pwsh' } else { 'powershell' }

  if ($Only -in 'all', 'be') {
    Write-Host '==> Backend  http://localhost:8080/rchat  (cửa sổ mới)' -ForegroundColor Cyan
    # Cửa sổ con thừa hưởng biến môi trường vừa nạp từ .env
    Start-Process $shell -WorkingDirectory $Backend -ArgumentList '-NoExit', '-Command',
      "`$host.UI.RawUI.WindowTitle='chat-system BE'; .\mvnw.cmd spring-boot:run"
  }

  if ($Only -in 'all', 'fe') {
    Write-Host '==> Frontend http://localhost:3000  (cửa sổ mới)' -ForegroundColor Cyan
    $install = if (Test-Path (Join-Path $Frontend 'node_modules')) { '' } else { 'pnpm install --config.confirmModulesPurge=false; ' }
    Start-Process $shell -WorkingDirectory $Frontend -ArgumentList '-NoExit', '-Command',
      "`$host.UI.RawUI.WindowTitle='chat-system FE'; ${install}pnpm dev"
  }

  Write-Host 'Xong. Tắt: đóng các cửa sổ BE/FE (và DB nếu có), rồi chạy  pnpm db:stop' -ForegroundColor Green
}
finally {
  Pop-Location
}
