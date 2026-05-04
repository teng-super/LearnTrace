param(
  [string]$ProjectRoot = "D:\LearnTrace",
  [string]$MysqlHome = $env:LEARNTRACE_MYSQL_HOME,
  [int]$Port = 3306
)

$ErrorActionPreference = "Stop"
function Info($message) { Write-Host "[LearnTrace] $message" -ForegroundColor Cyan }

$ToolsDir = Join-Path $ProjectRoot ".tools"
$BundledMysqlDir = Join-Path $ToolsDir "mysql"
$MysqlDataDir = Join-Path $ProjectRoot "data\mysql"
$LogsDir = Join-Path $ProjectRoot "logs"
$MysqlConfig = Join-Path $ToolsDir "mysql-my.ini"
$MysqlHomeFile = Join-Path $ToolsDir "mysql-home.txt"

$SavedMysqlHome = ""
if (Test-Path $MysqlHomeFile) {
  $SavedMysqlHome = (Get-Content -LiteralPath $MysqlHomeFile -Raw).Trim()
}

$KnownMysqlDirs = @(
  $MysqlHome,
  $SavedMysqlHome,
  "D:\mysql",
  "D:\MySQL",
  $BundledMysqlDir
) | Where-Object { ![string]::IsNullOrWhiteSpace($_) }

$MysqlDir = $KnownMysqlDirs |
  Where-Object { Test-Path (Join-Path $_ "bin\mysqld.exe") } |
  Select-Object -First 1

if (!$MysqlDir) { throw "MySQL is not set up. Run .\scripts\setup-mysql.ps1 first, or set LEARNTRACE_MYSQL_HOME." }
Info "Using MySQL binaries at $MysqlDir"

$Mysqld = Join-Path $MysqlDir "bin\mysqld.exe"

if (!(Test-Path (Join-Path $MysqlDataDir "mysql"))) { throw "MySQL data directory is missing. Run .\scripts\setup-mysql.ps1 first." }
if (!(Test-Path $MysqlConfig)) { throw "MySQL config is missing. Run .\scripts\setup-mysql.ps1 first." }

New-Item -ItemType Directory -Force -Path $LogsDir | Out-Null

$Existing = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq "Listen" } | Select-Object -First 1
if ($null -ne $Existing) {
  Info "MySQL appears to already be listening on port $Port"
  exit 0
}

Info "Starting MySQL on 127.0.0.1:$Port"
Start-Process -FilePath $Mysqld -ArgumentList "--defaults-file=`"$MysqlConfig`"" -WorkingDirectory $MysqlDir -WindowStyle Hidden

$MysqlAdmin = Join-Path $MysqlDir "bin\mysqladmin.exe"
for ($i = 0; $i -lt 60; $i++) {
  try {
    & $MysqlAdmin --protocol=TCP --host=127.0.0.1 --port=$Port --user=root ping 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) {
      Info "MySQL is ready"
      exit 0
    }
  } catch {}
  Start-Sleep -Seconds 1
}

throw "MySQL did not become ready. Check $LogsDir\mysql.err"
