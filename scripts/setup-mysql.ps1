param(
  [string]$ProjectRoot = "D:\LearnTrace",
  [string]$MysqlVersion = "8.4.8",
  [string]$MysqlZipUrl = "",
  [string]$MysqlHome = $env:LEARNTRACE_MYSQL_HOME,
  [int]$Port = 3306,
  [string]$DbName = "learntrace",
  [string]$DbUser = "learntrace",
  [string]$DbPassword = "learntrace123"
)

$ErrorActionPreference = "Stop"

function Info($message) { Write-Host "[LearnTrace] $message" -ForegroundColor Cyan }
function Warn($message) { Write-Host "[LearnTrace] $message" -ForegroundColor Yellow }

$ToolsDir = Join-Path $ProjectRoot ".tools"
$DownloadsDir = Join-Path $ToolsDir "downloads"
$BundledMysqlDir = Join-Path $ToolsDir "mysql"
$MysqlDataDir = Join-Path $ProjectRoot "data\mysql"
$LogsDir = Join-Path $ProjectRoot "logs"
$MysqlConfig = Join-Path $ToolsDir "mysql-my.ini"
$MysqlHomeFile = Join-Path $ToolsDir "mysql-home.txt"

New-Item -ItemType Directory -Force -Path $ToolsDir, $DownloadsDir, $MysqlDataDir, $LogsDir | Out-Null

if ([string]::IsNullOrWhiteSpace($MysqlZipUrl)) {
  $MysqlZipUrl = "https://cdn.mysql.com/Downloads/MySQL-8.4/mysql-$MysqlVersion-winx64.zip"
}

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

if ($MysqlDir -and ($MysqlDir -ne $BundledMysqlDir)) {
  Info "Using existing MySQL binaries at $MysqlDir"
}

$MysqlZip = Join-Path $DownloadsDir "mysql-$MysqlVersion-winx64.zip"

if (!$MysqlDir) {
  $MysqlDir = $BundledMysqlDir
  if (!(Test-Path $MysqlZip)) {
    Info "Downloading MySQL $MysqlVersion portable ZIP to $MysqlZip"
    Invoke-WebRequest -Uri $MysqlZipUrl -OutFile $MysqlZip -UseBasicParsing
  } else {
    Info "Using existing MySQL ZIP: $MysqlZip"
  }

  $ExtractRoot = Join-Path $DownloadsDir "mysql-extract"
  if (Test-Path $ExtractRoot) { Remove-Item -LiteralPath $ExtractRoot -Recurse -Force }
  New-Item -ItemType Directory -Force -Path $ExtractRoot | Out-Null

  Info "Extracting MySQL into D drive tools directory"
  Expand-Archive -LiteralPath $MysqlZip -DestinationPath $ExtractRoot -Force
  $Extracted = Get-ChildItem -LiteralPath $ExtractRoot -Directory | Select-Object -First 1
  if ($null -eq $Extracted) { throw "MySQL ZIP extraction did not produce a directory." }
  if (Test-Path $BundledMysqlDir) { Remove-Item -LiteralPath $BundledMysqlDir -Recurse -Force }
  Move-Item -LiteralPath $Extracted.FullName -Destination $BundledMysqlDir
  Remove-Item -LiteralPath $ExtractRoot -Recurse -Force
} else {
  Info "MySQL files are ready at $MysqlDir"
}

$MysqlDir | Set-Content -LiteralPath $MysqlHomeFile -Encoding ASCII

$BaseDirEscaped = $MysqlDir -replace '\\', '/'
$DataDirEscaped = $MysqlDataDir -replace '\\', '/'
$LogDirEscaped = $LogsDir -replace '\\', '/'

@"
[mysqld]
basedir=$BaseDirEscaped
datadir=$DataDirEscaped
port=$Port
character-set-server=utf8mb4
collation-server=utf8mb4_0900_ai_ci
default-time-zone=+08:00
bind-address=127.0.0.1
log-error=$LogDirEscaped/mysql.err
pid-file=$LogDirEscaped/mysql.pid

[client]
default-character-set=utf8mb4
port=$Port
"@ | Set-Content -LiteralPath $MysqlConfig -Encoding ASCII

$Mysqld = Join-Path $MysqlDir "bin\mysqld.exe"
$Mysql = Join-Path $MysqlDir "bin\mysql.exe"
$MysqlAdmin = Join-Path $MysqlDir "bin\mysqladmin.exe"

if (!(Test-Path (Join-Path $MysqlDataDir "mysql"))) {
  Info "Initializing MySQL data directory at $MysqlDataDir"
  & $Mysqld --defaults-file="$MysqlConfig" --initialize-insecure --console
} else {
  Info "MySQL data directory already initialized"
}

$StartedHere = $false
$Existing = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq "Listen" } | Select-Object -First 1
if ($null -eq $Existing) {
  Info "Starting MySQL temporarily to create database/user"
  Start-Process -FilePath $Mysqld -ArgumentList "--defaults-file=`"$MysqlConfig`"" -WorkingDirectory $MysqlDir -WindowStyle Hidden
  $StartedHere = $true
} else {
  Warn "Port $Port is already listening; assuming MySQL is running"
}

$Ready = $false
for ($i = 0; $i -lt 60; $i++) {
  try {
    & $MysqlAdmin --protocol=TCP --host=127.0.0.1 --port=$Port --user=root ping 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $Ready = $true; break }
  } catch {}
  Start-Sleep -Seconds 1
}
if (!$Ready) {
  throw "MySQL did not become ready. Check $LogsDir\mysql.err"
}

$InitSql = Join-Path $LogsDir "mysql-bootstrap.sql"
@"
CREATE DATABASE IF NOT EXISTS $DbName DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS '$DbUser'@'localhost' IDENTIFIED BY '$DbPassword';
CREATE USER IF NOT EXISTS '$DbUser'@'127.0.0.1' IDENTIFIED BY '$DbPassword';
GRANT ALL PRIVILEGES ON $DbName.* TO '$DbUser'@'localhost';
GRANT ALL PRIVILEGES ON $DbName.* TO '$DbUser'@'127.0.0.1';
FLUSH PRIVILEGES;
"@ | Set-Content -LiteralPath $InitSql -Encoding UTF8

Info "Creating LearnTrace database and user"
Get-Content -LiteralPath $InitSql -Raw | & $Mysql --protocol=TCP --host=127.0.0.1 --port=$Port --user=root

if ($StartedHere) {
  Info "Stopping temporary MySQL process"
  & $MysqlAdmin --protocol=TCP --host=127.0.0.1 --port=$Port --user=root shutdown
}

Info "MySQL portable setup complete"
Info "Next: .\scripts\start-mysql.ps1"
