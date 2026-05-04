param(
  [string]$ProjectRoot = "D:\LearnTrace",
  [int]$Port = 5173
)

$ErrorActionPreference = "Stop"
function Info($message) { Write-Host "[LearnTrace] $message" -ForegroundColor Cyan }

$FrontendDir = Join-Path $ProjectRoot "frontend"
$LogsDir = Join-Path $ProjectRoot "logs"
New-Item -ItemType Directory -Force -Path $LogsDir | Out-Null

if (!(Test-Path (Join-Path $FrontendDir "node_modules"))) {
  Info "Installing frontend dependencies"
  Push-Location $FrontendDir
  try {
    & npm.cmd install
  } finally {
    Pop-Location
  }
}

$Existing = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq "Listen" } | Select-Object -First 1
if ($null -ne $Existing) {
  Info "Frontend port $Port is already listening"
  exit 0
}

Info "Starting frontend on http://localhost:$Port"
Start-Process -FilePath "npm.cmd" `
  -ArgumentList "run","dev","--","--port","$Port" `
  -WorkingDirectory $FrontendDir `
  -WindowStyle Hidden `
  -RedirectStandardOutput (Join-Path $LogsDir "frontend.out.log") `
  -RedirectStandardError (Join-Path $LogsDir "frontend.err.log")

for ($i = 0; $i -lt 45; $i++) {
  $Conn = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq "Listen" } | Select-Object -First 1
  if ($null -ne $Conn) {
    Info "Frontend is ready at http://localhost:$Port"
    exit 0
  }
  Start-Sleep -Seconds 1
}

throw "Frontend did not start. Check $LogsDir\frontend.err.log"
