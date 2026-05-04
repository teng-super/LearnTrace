param(
  [int[]]$Ports = @(5173, 8080, 3306)
)

function Info($message) { Write-Host "[LearnTrace] $message" -ForegroundColor Cyan }

foreach ($Port in $Ports) {
  $Connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq "Listen" }
  foreach ($Conn in $Connections) {
    $ProcessId = $Conn.OwningProcess
    if ($ProcessId -and $ProcessId -ne 0) {
      try {
        $Process = Get-Process -Id $ProcessId -ErrorAction Stop
        Info "Stopping port $Port process: $($Process.ProcessName) ($ProcessId)"
        Stop-Process -Id $ProcessId -Force
      } catch {
        Write-Host "[LearnTrace] Could not stop process $ProcessId on port ${Port}: $($_.Exception.Message)" -ForegroundColor Yellow
      }
    }
  }
}
