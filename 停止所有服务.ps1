# ========================================
# Stop All Microservices - Fast Version
# Version: 1.1
# Date: 2026-02-20
# ========================================

Write-Host ""
Write-Host "========== Stop All Microservices ==========" -ForegroundColor Cyan
Write-Host ""

# Stop all Java processes first (faster)
Write-Host "[1/2] Stop Java processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "  Found $($javaProcesses.Count) Java processes" -ForegroundColor Gray
    $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "  [OK] All Java processes stopped" -ForegroundColor Green
} else {
    Write-Host "  [INFO] No Java processes found" -ForegroundColor Gray
}
Write-Host ""

# Clean up background jobs
Write-Host "[2/2] Clean up background jobs..." -ForegroundColor Yellow
$jobs = Get-Job -ErrorAction SilentlyContinue
if ($jobs) {
    Write-Host "  Found $($jobs.Count) background jobs" -ForegroundColor Gray
    Get-Job | Remove-Job -Force -ErrorAction SilentlyContinue
    Write-Host "  [OK] All background jobs removed" -ForegroundColor Green
} else {
    Write-Host "  [INFO] No background jobs found" -ForegroundColor Gray
}
Write-Host ""

# Verify
Write-Host "Verify service status..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

$ports = @(9080, 8081, 8082, 8083, 8084)
$stillRunning = 0

foreach ($port in $ports) {
    $portRunning = netstat -ano | findstr ":$port" | findstr "LISTENING"
    if ($portRunning) {
        Write-Host "  [!] Port $port still running" -ForegroundColor Yellow
        $stillRunning++
    } else {
        Write-Host "  [OK] Port $port stopped" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($stillRunning -eq 0) {
    Write-Host "  All microservices stopped!" -ForegroundColor Green
} else {
    Write-Host "  Still $stillRunning services running" -ForegroundColor Yellow
    Write-Host "  You may need to stop them manually" -ForegroundColor Gray
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Tips:" -ForegroundColor Yellow
Write-Host "  - MySQL and Docker containers not stopped" -ForegroundColor Gray
Write-Host "  - To restart: .\startup-all-services.ps1" -ForegroundColor Gray
Write-Host ""
