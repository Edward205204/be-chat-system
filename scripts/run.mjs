// Chọn script theo hệ điều hành, để các lệnh `pnpm ...` ở thư mục gốc chạy được mọi nơi.
//   node scripts/run.mjs <tên> [tham số]   -> scripts/<tên>.ps1 (Windows) hoặc scripts/<tên>.sh (Linux/macOS/WSL)
//   node scripts/run.mjs mvnw [tham số]    -> Maven wrapper trong apps/backend
import { spawnSync } from 'node:child_process'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const [name, ...args] = process.argv.slice(2)
if (!name) {
  console.error('Cách dùng: node scripts/run.mjs <setup|dev|build|mvnw> [tham số]')
  process.exit(2)
}

const scriptsDir = path.dirname(fileURLToPath(import.meta.url))
const isWindows = process.platform === 'win32'
let command
let commandArgs
let cwd = path.dirname(scriptsDir)

if (name === 'mvnw') {
  cwd = path.join(cwd, 'apps', 'backend')
  command = isWindows ? 'cmd' : './mvnw'
  commandArgs = isWindows ? ['/c', 'mvnw.cmd', ...args] : args
} else if (isWindows) {
  // Ưu tiên PowerShell 7 (pwsh); không có thì dùng Windows PowerShell có sẵn.
  command = spawnSync('pwsh', ['-NoProfile', '-Command', 'exit 0']).status === 0 ? 'pwsh' : 'powershell'
  commandArgs = ['-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', path.join(scriptsDir, `${name}.ps1`), ...args]
} else {
  command = 'bash'
  commandArgs = [path.join(scriptsDir, `${name}.sh`), ...args]
}

const result = spawnSync(command, commandArgs, { stdio: 'inherit', cwd })
if (result.error) {
  console.error(`Không chạy được "${command}": ${result.error.message}`)
  process.exit(1)
}
process.exit(result.status ?? 1)
