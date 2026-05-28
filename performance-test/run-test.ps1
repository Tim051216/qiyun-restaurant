# JMeter 压测自动化脚本
# 用于七云菜馆项目性能测试

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  七云菜馆 - 性能压测工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 JMeter 是否已安装
$jmeterPaths = @(
    "C:\apache-jmeter-5.6.3",
    "C:\apache-jmeter-5.6.2",
    "C:\apache-jmeter-5.6.1",
    "C:\apache-jmeter-5.6",
    "C:\apache-jmeter-5.5",
    "C:\Program Files\apache-jmeter-5.6.3",
    "$env:JMETER_HOME"
)

$JMETER_HOME = $null
foreach ($path in $jmeterPaths) {
    if ($path -and (Test-Path "$path\bin\jmeter.bat")) {
        $JMETER_HOME = $path
        break
    }
}

if (-not $JMETER_HOME) {
    Write-Host "❌ 未找到 JMeter 安装目录" -ForegroundColor Red
    Write-Host ""
    Write-Host "请按以下步骤安装 JMeter:" -ForegroundColor Yellow
    Write-Host "1. 访问: https://jmeter.apache.org/download_jmeter.cgi" -ForegroundColor Yellow
    Write-Host "2. 下载 apache-jmeter-5.6.3.zip" -ForegroundColor Yellow
    Write-Host "3. 解压到 C:\ 目录" -ForegroundColor Yellow
    Write-Host "4. 重新运行此脚本" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "✓ 找到 JMeter: $JMETER_HOME" -ForegroundColor Green
Write-Host ""

# 检查服务是否运行
Write-Host "正在检查服务状态..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9080/api/dishes" -Method GET -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✓ 服务正常运行 (状态码: $($response.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "❌ 服务未运行或无法访问" -ForegroundColor Red
    Write-Host ""
    Write-Host "请先启动后端服务:" -ForegroundColor Yellow
    Write-Host ".\启动所有服务.ps1" -ForegroundColor Yellow
    Write-Host ""
    $continue = Read-Host "是否继续压测? (y/n)"
    if ($continue -ne "y") {
        exit 1
    }
}

Write-Host ""

# 配置
$TEST_PLAN = "dish-list-test.jmx"
$RESULT_DIR = "results"
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"
$RESULT_FILE = "$RESULT_DIR\result-$TIMESTAMP.jtl"
$REPORT_DIR = "$RESULT_DIR\report-$TIMESTAMP"

# 创建结果目录
if (-not (Test-Path $RESULT_DIR)) {
    New-Item -ItemType Directory -Force -Path $RESULT_DIR | Out-Null
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  压测配置" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试计划: $TEST_PLAN" -ForegroundColor White
Write-Host "并发用户: 50" -ForegroundColor White
Write-Host "持续时间: 60 秒" -ForegroundColor White
Write-Host "目标接口: http://localhost:9080/api/dishes" -ForegroundColor White
Write-Host "结果文件: $RESULT_FILE" -ForegroundColor White
Write-Host "报告目录: $REPORT_DIR" -ForegroundColor White
Write-Host ""

# 确认执行
$confirm = Read-Host "是否开始压测? (y/n)"
if ($confirm -ne "y") {
    Write-Host "已取消压测" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  开始压测..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 执行压测
$startTime = Get-Date
& "$JMETER_HOME\bin\jmeter.bat" `
    -n `
    -t $TEST_PLAN `
    -l $RESULT_FILE `
    -e `
    -o $REPORT_DIR

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  压测完成!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "总耗时: $([math]::Round($duration, 2)) 秒" -ForegroundColor Green
Write-Host ""

# 检查是否生成了报告
if (Test-Path "$REPORT_DIR\index.html") {
    Write-Host "✓ HTML 报告已生成" -ForegroundColor Green
    Write-Host ""
    Write-Host "正在打开报告..." -ForegroundColor Yellow
    Start-Sleep -Seconds 2
    Start-Process "$REPORT_DIR\index.html"
} else {
    Write-Host "❌ 报告生成失败" -ForegroundColor Red
    Write-Host "请检查 JTL 文件: $RESULT_FILE" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "结果文件位置:" -ForegroundColor Cyan
Write-Host "  - JTL 文件: $RESULT_FILE" -ForegroundColor White
Write-Host "  - HTML 报告: $REPORT_DIR\index.html" -ForegroundColor White
Write-Host ""
