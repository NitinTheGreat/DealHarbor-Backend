# Kill all Java processes related to Spring Boot application
Write-Host "Searching for Java processes..." -ForegroundColor Yellow

$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue

if ($javaProcesses) {
    Write-Host "Found $($javaProcesses.Count) Java process(es):" -ForegroundColor Cyan
    
    foreach ($process in $javaProcesses) {
        Write-Host "  PID: $($process.Id) - Started: $($process.StartTime)" -ForegroundColor White
        try {
            Stop-Process -Id $process.Id -Force
            Write-Host "  Killed PID $($process.Id)" -ForegroundColor Green
        }
        catch {
            Write-Host "  Failed to kill PID $($process.Id): $_" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "All Java processes terminated." -ForegroundColor Green
    Write-Host "Wait 30 seconds for database connections to close, then try starting the app again." -ForegroundColor Yellow
}
else {
    Write-Host "No Java processes found running." -ForegroundColor Green
}
