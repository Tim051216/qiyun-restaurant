# Startup All Microservices
# Version: 1.1
# Date: 2026-02-20

Write-Host ""
Write-Host "========== Startup All Microservices ==========" -ForegroundColor Cyan
Write-Host ""

$currentPath = Get-Location
Write-Host "[INFO] Current Path: $currentPath" -ForegroundColor Gray
Write-Host ""

# Step 1: Check Docker
Write-Host "[1/5] Check Docker..." -ForegroundColor Yellow
$dockerRunning = docker ps 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "  [X] Docker not running" -ForegroundColor Red
    Write-Host "  Please start Docker Desktop first" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "  [OK] Docker is running" -ForegroundColor Green
Write-Host ""

# Step 2: Start Docker Containers
Write-Host "[2/5] Start Docker containers..." -ForegroundColor Yellow
$dockerComposePath = Join-Path $currentPath "restaurant-server\docker-compose.yml"
if (Test-Path $dockerComposePath) {
    Write-Host "  Starting Nacos, Redis, RabbitMQ..." -ForegroundColor Gray
    Set-Location restaurant-server
    docker-compose up -d 2>&1 | Out-Null
    Set-Location ..
    Write-Host "  [OK] Containers started" -ForegroundColor Green
    Write-Host "  Waiting 15 seconds..." -ForegroundColor Gray
    Start-Sleep -Seconds 15
} else {
    Write-Host "  [!] docker-compose.yml not found, skip" -ForegroundColor Yellow
}
Write-Host ""

# Step 3: Check MySQL
Write-Host "[3/5] Check MySQL..." -ForegroundColor Yellow
$mysqlRunning = netstat -ano | findstr ":3306" | findstr "LISTENING"
if ($mysqlRunning) {
    Write-Host "  [OK] MySQL is running (port 3306)" -ForegroundColor Green
} else {
    Write-Host "  [X] MySQL not running" -ForegroundColor Red
    Write-Host "  Please start MySQL first" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host ""

# Step 4: Start Microservices
Write-Host "[4/5] Start microservices..." -ForegroundColor Yellow
Write-Host "  Services will run in background" -ForegroundColor Gray
Write-Host ""

# Gateway
Write-Host "  [1/5] Start Gateway (port 9080)..." -ForegroundColor Cyan
$gatewayPath = Join-Path $currentPath "restaurant-gateway"
if (Test-Path $gatewayPath) {
    $portInUse = netstat -ano | findstr ":9080" | findstr "LISTENING"
    if (-not $portInUse) {
        $job1 = Start-Job -ScriptBlock {
            param($path)
            Set-Location $path
            mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dmaven.test.skip=true" 2>&1
        } -ArgumentList $gatewayPath
        Write-Host "      Job ID: $($job1.Id)" -ForegroundColor Gray
        Write-Host "      Waiting 35 seconds..." -ForegroundColor Gray
        Start-Sleep -Seconds 35
    } else {
        Write-Host "      [!] Port 9080 already in use" -ForegroundColor Yellow
    }
} else {
    Write-Host "      [X] Directory not found" -ForegroundColor Red
}
Write-Host ""

# Order Service
Write-Host "  [2/5] Start Order Service (port 8081)..." -ForegroundColor Cyan
$orderPath = Join-Path $currentPath "restaurant-order"
if (Test-Path $orderPath) {
    $portInUse = netstat -ano | findstr ":8081" | findstr "LISTENING"
    if (-not $portInUse) {
        $job2 = Start-Job -ScriptBlock {
            param($path)
            Set-Location $path
            mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dmaven.test.skip=true" 2>&1
        } -ArgumentList $orderPath
        Write-Host "      Job ID: $($job2.Id)" -ForegroundColor Gray
        Write-Host "      Waiting 15 seconds..." -ForegroundColor Gray
        Start-Sleep -Seconds 15
    } else {
        Write-Host "      [!] Port 8081 already in use" -ForegroundColor Yellow
    }
} else {
    Write-Host "      [X] Directory not found" -ForegroundColor Red
}
Write-Host ""

# Dish Service
Write-Host "  [3/5] Start Dish Service (port 8082)..." -ForegroundColor Cyan
$dishPath = Join-Path $currentPath "restaurant-dish"
if (Test-Path $dishPath) {
    $portInUse = netstat -ano | findstr ":8082" | findstr "LISTENING"
    if (-not $portInUse) {
        $job3 = Start-Job -ScriptBlock {
            param($path)
            Set-Location $path
            mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dmaven.test.skip=true" 2>&1
        } -ArgumentList $dishPath
        Write-Host "      Job ID: $($job3.Id)" -ForegroundColor Gray
        Write-Host "      Waiting 15 seconds..." -ForegroundColor Gray
        Start-Sleep -Seconds 15
    } else {
        Write-Host "      [!] Port 8082 already in use" -ForegroundColor Yellow
    }
} else {
    Write-Host "      [X] Directory not found" -ForegroundColor Red
}
Write-Host ""

# Member Service
Write-Host "  [4/5] Start Member Service (port 8083)..." -ForegroundColor Cyan
$memberPath = Join-Path $currentPath "restaurant-member"
if (Test-Path $memberPath) {
    $portInUse = netstat -ano | findstr ":8083" | findstr "LISTENING"
    if (-not $portInUse) {
        $job4 = Start-Job -ScriptBlock {
            param($path)
            Set-Location $path
            mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dmaven.test.skip=true" 2>&1
        } -ArgumentList $memberPath
        Write-Host "      Job ID: $($job4.Id)" -ForegroundColor Gray
        Write-Host "      Waiting 15 seconds..." -ForegroundColor Gray
        Start-Sleep -Seconds 15
    } else {
        Write-Host "      [!] Port 8083 already in use" -ForegroundColor Yellow
    }
} else {
    Write-Host "      [X] Directory not found" -ForegroundColor Red
}
Write-Host ""

# Admin Service
Write-Host "  [5/5] Start Admin Service (port 8084)..." -ForegroundColor Cyan
$adminPath = Join-Path $currentPath "restaurant-admin-service"
if (Test-Path $adminPath) {
    $portInUse = netstat -ano | findstr ":8084" | findstr "LISTENING"
    if (-not $portInUse) {
        $job5 = Start-Job -ScriptBlock {
            param($path)
            Set-Location $path
            mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dmaven.test.skip=true" 2>&1
        } -ArgumentList $adminPath
        Write-Host "      Job ID: $($job5.Id)" -ForegroundColor Gray
        Write-Host "      Waiting 10 seconds..." -ForegroundColor Gray
        Start-Sleep -Seconds 10
    } else {
        Write-Host "      [!] Port 8084 already in use" -ForegroundColor Yellow
    }
} else {
    Write-Host "      [X] Directory not found" -ForegroundColor Red
}
Write-Host ""

# Step 5: Verify Services
Write-Host "[5/5] Verify services..." -ForegroundColor Yellow
Write-Host "  Waiting 10 seconds for services to start..." -ForegroundColor Gray
Start-Sleep -Seconds 10
Write-Host ""

$runningCount = 0

$port9080 = netstat -ano | findstr ":9080" | findstr "LISTENING"
if ($port9080) {
    Write-Host "  [OK] Gateway (port 9080)" -ForegroundColor Green
    $runningCount++
} else {
    Write-Host "  [X] Gateway (port 9080)" -ForegroundColor Red
}

$port8081 = netstat -ano | findstr ":8081" | findstr "LISTENING"
if ($port8081) {
    Write-Host "  [OK] Order Service (port 8081)" -ForegroundColor Green
    $runningCount++
} else {
    Write-Host "  [X] Order Service (port 8081)" -ForegroundColor Red
}

$port8082 = netstat -ano | findstr ":8082" | findstr "LISTENING"
if ($port8082) {
    Write-Host "  [OK] Dish Service (port 8082)" -ForegroundColor Green
    $runningCount++
} else {
    Write-Host "  [X] Dish Service (port 8082)" -ForegroundColor Red
}

$port8083 = netstat -ano | findstr ":8083" | findstr "LISTENING"
if ($port8083) {
    Write-Host "  [OK] Member Service (port 8083)" -ForegroundColor Green
    $runningCount++
} else {
    Write-Host "  [X] Member Service (port 8083)" -ForegroundColor Red
}

$port8084 = netstat -ano | findstr ":8084" | findstr "LISTENING"
if ($port8084) {
    Write-Host "  [OK] Admin Service (port 8084)" -ForegroundColor Green
    $runningCount++
} else {
    Write-Host "  [X] Admin Service (port 8084)" -ForegroundColor Red
}

Write-Host ""
if ($runningCount -eq 5) {
    Write-Host "  Running services: $runningCount/5" -ForegroundColor Green
} elseif ($runningCount -ge 3) {
    Write-Host "  Running services: $runningCount/5" -ForegroundColor Yellow
} else {
    Write-Host "  Running services: $runningCount/5" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Startup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Access URLs:" -ForegroundColor Yellow
Write-Host "  Gateway: http://localhost:9080" -ForegroundColor White
Write-Host "  Nacos: http://localhost:8848/nacos" -ForegroundColor White
Write-Host ""

Write-Host "Management Commands:" -ForegroundColor Yellow
Write-Host "  View jobs: Get-Job" -ForegroundColor White
Write-Host "  View logs: Receive-Job -Id JobID -Keep" -ForegroundColor White
Write-Host "  Stop all: .\stop-all-services.ps1" -ForegroundColor White
Write-Host ""

Write-Host "Press Enter to exit..." -ForegroundColor Gray
Read-Host
