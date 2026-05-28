# 临时启动后端服务 (使用 JDK 21)
# 不会修改系统 JAVA_HOME,不影响其他项目

Write-Host "========== 启动后端服务 (JDK 21) ==========" -ForegroundColor Cyan
Write-Host ""

# 临时设置 JDK 21 路径
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "[INFO] 使用 JDK: $env:JAVA_HOME" -ForegroundColor Green
Write-Host ""

# 验证 Java 版本
Write-Host "[1/3] 验证 Java 版本..." -ForegroundColor Yellow
java -version
Write-Host ""

# 启动 Admin Service
Write-Host "[2/3] 启动 Admin Service (端口 8084)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", `
    "`$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot'; `
    `$env:PATH=`"`$env:JAVA_HOME\bin;`$env:PATH`"; `
    cd '$PWD\restaurant-admin-service'; `
    Write-Host '正在启动 Admin Service...' -ForegroundColor Cyan; `
    mvn spring-boot:run '-Dspring-boot.run.profiles=local' '-Dmaven.test.skip=true'"

Start-Sleep -Seconds 3

# 启动 Gateway
Write-Host "[3/3] 启动 Gateway (端口 9080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", `
    "`$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot'; `
    `$env:PATH=`"`$env:JAVA_HOME\bin;`$env:PATH`"; `
    cd '$PWD\restaurant-gateway'; `
    Write-Host '正在启动 Gateway...' -ForegroundColor Cyan; `
    mvn spring-boot:run '-Dspring-boot.run.profiles=local' '-Dmaven.test.skip=true'"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  服务启动中,请等待约 60 秒..." -ForegroundColor Green
Write-Host "  两个新窗口已打开,可以查看启动日志" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Tip: Close the new windows to stop services" -ForegroundColor Gray
Write-Host ""
