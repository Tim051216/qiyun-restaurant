# 容器化部署验证脚本 (PowerShell版本)
# 用于验证Docker环境和配置的正确性

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "容器化部署验证" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

$PASSED = 0
$FAILED = 0

function Check-Result {
    param($Message)
    if ($LASTEXITCODE -eq 0 -or $?) {
        Write-Host "✓ $Message" -ForegroundColor Green
        $script:PASSED++
    } else {
        Write-Host "✗ $Message" -ForegroundColor Red
        $script:FAILED++
    }
}

# 1. 检查Docker是否安装
Write-Host "1. 检查Docker环境..."
try {
    docker --version | Out-Null
    Check-Result "Docker已安装"
} catch {
    Write-Host "✗ Docker未安装" -ForegroundColor Red
    $FAILED++
}

try {
    docker-compose --version | Out-Null
    Check-Result "Docker Compose已安装"
} catch {
    Write-Host "✗ Docker Compose未安装" -ForegroundColor Red
    $FAILED++
}

# 2. 检查Docker是否运行
try {
    docker ps | Out-Null
    Check-Result "Docker守护进程正在运行"
} catch {
    Write-Host "✗ Docker守护进程未运行" -ForegroundColor Red
    $FAILED++
}

# 3. 检查Dockerfile文件
Write-Host ""
Write-Host "2. 检查Dockerfile文件..."

if (Test-Path "..\restaurant-gateway\Dockerfile") {
    Write-Host "✓ Gateway Dockerfile存在" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "✗ Gateway Dockerfile不存在" -ForegroundColor Red
    $FAILED++
}

if (Test-Path "..\restaurant-order\Dockerfile") {
    Write-Host "✓ Order Service Dockerfile存在" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "✗ Order Service Dockerfile不存在" -ForegroundColor Red
    $FAILED++
}

if (Test-Path "..\restaurant-dish\Dockerfile") {
    Write-Host "✓ Dish Service Dockerfile存在" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "✗ Dish Service Dockerfile不存在" -ForegroundColor Red
    $FAILED++
}

if (Test-Path "..\restaurant-member\Dockerfile") {
    Write-Host "✓ Member Service Dockerfile存在" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "✗ Member Service Dockerfile不存在" -ForegroundColor Red
    $FAILED++
}

if (Test-Path "..\restaurant-admin-service\Dockerfile") {
    Write-Host "✓ Admin Service Dockerfile存在" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "✗ Admin Service Dockerfile不存在" -ForegroundColor Red
    $FAILED++
}

# 4. 检查docker-compose文件
Write-Host ""
Write-Host "3. 检查docker-compose配置..."

if (Test-Path "docker-compose-full.yml") {
    Write-Host "✓ docker-compose-full.yml存在" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "✗ docker-compose-full.yml不存在" -ForegroundColor Red
    $FAILED++
}

# 验证docker-compose配置语法
try {
    docker-compose -f docker-compose-full.yml config --quiet 2>&1 | Out-Null
    Check-Result "docker-compose配置语法正确"
} catch {
    Write-Host "✗ docker-compose配置语法错误" -ForegroundColor Red
    $FAILED++
}

# 5. 检查必要的配置文件
Write-Host ""
Write-Host "4. 检查配置文件..."

if (Test-Path "monitoring\prometheus.yml") {
    Write-Host "✓ Prometheus配置文件存在" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "✗ Prometheus配置文件不存在" -ForegroundColor Red
    $FAILED++
}

if (Test-Path "DOCKER_DEPLOYMENT.md") {
    Write-Host "✓ 部署文档存在" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "✗ 部署文档不存在" -ForegroundColor Red
    $FAILED++
}

# 6. 检查网络
Write-Host ""
Write-Host "5. 检查Docker网络..."
$networks = docker network ls | Select-String "restaurant-network"
if ($networks) {
    Write-Host "! restaurant-network网络已存在（将在docker-compose up时使用）" -ForegroundColor Yellow
} else {
    Write-Host "✓ restaurant-network网络将在启动时创建" -ForegroundColor Green
}

# 7. 检查数据卷
Write-Host ""
Write-Host "6. 检查Docker数据卷..."
$volumes = docker volume ls -q | Select-String -Pattern "mysql-master-data|redis-data|rabbitmq-data|nacos-data"
if ($volumes) {
    $volumeCount = ($volumes | Measure-Object).Count
    Write-Host "! 发现 $volumeCount 个已存在的数据卷" -ForegroundColor Yellow
    Write-Host "   如需全新部署，请先运行: docker-compose -f docker-compose-full.yml down -v" -ForegroundColor Yellow
} else {
    Write-Host "✓ 数据卷将在启动时创建" -ForegroundColor Green
}

# 8. 检查端口占用
Write-Host ""
Write-Host "7. 检查端口占用..."
$ports = @(3306, 6379, 5672, 8080, 8081, 8082, 8083, 8084, 8848, 9090, 3000)
$portConflicts = 0

foreach ($port in $ports) {
    $connection = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($connection) {
        Write-Host "! 端口 $port 已被占用" -ForegroundColor Yellow
        $portConflicts++
    }
}

if ($portConflicts -eq 0) {
    Write-Host "✓ 所有必需端口都可用" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "✗ 发现 $portConflicts 个端口冲突" -ForegroundColor Red
    $FAILED++
}

# 9. 检查磁盘空间
Write-Host ""
Write-Host "8. 检查系统资源..."
$drive = Get-PSDrive -Name (Get-Location).Drive.Name
$freeSpaceGB = [math]::Round($drive.Free / 1GB, 2)

if ($freeSpaceGB -gt 20) {
    Write-Host "✓ 磁盘空间充足 (${freeSpaceGB}GB可用)" -ForegroundColor Green
    $PASSED++
} else {
    Write-Host "! 磁盘空间较少 (${freeSpaceGB}GB可用)，建议至少20GB" -ForegroundColor Yellow
    $PASSED++
}

# 总结
Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "验证结果" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "通过: $PASSED" -ForegroundColor Green
Write-Host "失败: $FAILED" -ForegroundColor Red
Write-Host ""

if ($FAILED -eq 0) {
    Write-Host "✓ 所有检查通过！可以开始部署。" -ForegroundColor Green
    Write-Host ""
    Write-Host "启动命令:"
    Write-Host "  docker-compose -f docker-compose-full.yml up -d"
    Write-Host ""
    Write-Host "查看日志:"
    Write-Host "  docker-compose -f docker-compose-full.yml logs -f"
    Write-Host ""
    exit 0
} else {
    Write-Host "✗ 发现 $FAILED 个问题，请先解决后再部署。" -ForegroundColor Red
    Write-Host ""
    exit 1
}
