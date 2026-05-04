param(
  [string]$ProjectRoot = "D:\LearnTrace",
  [int]$Port = 8080
)

$ErrorActionPreference = "Stop"
function Info($message) { Write-Host "[LearnTrace] $message" -ForegroundColor Cyan }

$BackendDir = Join-Path $ProjectRoot "backend"
$Maven = Join-Path $ProjectRoot ".tools\apache-maven-3.9.9\bin\mvn.cmd"
$LogsDir = Join-Path $ProjectRoot "logs"
$Jar = Join-Path $BackendDir "target\learntrace-backend-0.1.0.jar"

New-Item -ItemType Directory -Force -Path $LogsDir, (Join-Path $ProjectRoot "data\uploads"), (Join-Path $ProjectRoot "data\exports") | Out-Null

if (!(Test-Path $Jar)) {
  if (!(Test-Path $Maven)) { throw "Maven is missing at $Maven" }
  Info "Backend jar missing; building backend first"
  Push-Location $BackendDir
  try {
    & $Maven -q -DskipTests package
  } finally {
    Pop-Location
  }
}

$Existing = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq "Listen" } | Select-Object -First 1
if ($null -ne $Existing) {
  Info "Backend port $Port is already listening"
  exit 0
}

$env:SERVER_PORT = "$Port"
$env:MYSQL_URL = "jdbc:mysql://localhost:3306/learntrace?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
$env:MYSQL_USER = "learntrace"
$env:MYSQL_PASSWORD = "learntrace123"
$env:UPLOADS_DIR = Join-Path $ProjectRoot "data\uploads"
$env:EXPORTS_DIR = Join-Path $ProjectRoot "data\exports"
$env:PUBLIC_BASE_URL = "http://localhost:$Port/api/files/public"
if ([string]::IsNullOrWhiteSpace($env:ONLYOFFICE_URL)) {
  $env:ONLYOFFICE_URL = "http://localhost:8088"
}

Info "Starting backend on http://localhost:$Port"
Start-Process -FilePath "java" `
  -ArgumentList "-jar `"$Jar`"" `
  -WorkingDirectory $BackendDir `
  -WindowStyle Hidden `
  -RedirectStandardOutput (Join-Path $LogsDir "backend.out.log") `
  -RedirectStandardError (Join-Path $LogsDir "backend.err.log")

for ($i = 0; $i -lt 90; $i++) {
  $Conn = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq "Listen" } | Select-Object -First 1
  if ($null -ne $Conn) {
    Info "Backend is listening on http://localhost:$Port"
    exit 0
  }
  Start-Sleep -Seconds 1
}

throw "Backend did not start. Check $LogsDir\backend.err.log"
