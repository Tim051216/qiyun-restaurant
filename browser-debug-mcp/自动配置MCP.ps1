# 自动配置浏览器调试 MCP 服务器
# 使用方法: 右键此文件 -> 使用 PowerShell 运行

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "浏览器调试 MCP 自动配置脚本" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

$mcpConfigPath = "C:/Users/Administrator/.kiro/settings/mcp.json"
$backupPath = "C:/Users/Administrator/.kiro/settings/mcp.json.backup"

# 检查配置文件是否存在
if (Test-Path $mcpConfigPath) {
    Write-Host "✓ 找到 MCP 配置文件" -ForegroundColor Green
    
    # 备份原配置
    Write-Host "正在备份原配置..." -ForegroundColor Yellow
    Copy-Item $mcpConfigPath $backupPath -Force
    Write-Host "✓ 备份完成: $backupPath" -ForegroundColor Green
} else {
    Write-Host "! 配置文件不存在,将创建新文件" -ForegroundColor Yellow
    
    # 确保目录存在
    $configDir = Split-Path $mcpConfigPath
    if (-not (Test-Path $configDir)) {
        New-Item -ItemType Directory -Path $configDir -Force | Out-Null
    }
}

# 新配置内容
$newConfig = @"
{
  "mcpServers": {
    "fetch": {
      "command": "uvx",
      "args": ["mcp-server-fetch"],
      "env": {},
      "disabled": true,
      "autoApprove": []
    },
    "browser-debug": {
      "command": "node",
      "args": ["C:/Users/Administrator/七云菜馆/browser-debug-mcp/server.js"],
      "disabled": false,
      "autoApprove": ["get_console_logs", "get_network_logs", "take_screenshot"]
    }
  }
}
"@

# 写入新配置
Write-Host "正在写入新配置..." -ForegroundColor Yellow
$newConfig | Out-File -FilePath $mcpConfigPath -Encoding UTF8 -Force

Write-Host "✓ 配置写入完成!" -ForegroundColor Green
Write-Host ""

# 验证配置
Write-Host "验证配置文件..." -ForegroundColor Yellow
if (Test-Path $mcpConfigPath) {
    $content = Get-Content $mcpConfigPath -Raw
    if ($content -match "browser-debug") {
        Write-Host "✓ 配置验证成功!" -ForegroundColor Green
    } else {
        Write-Host "✗ 配置验证失败,请检查文件内容" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "✗ 配置文件未找到" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "配置完成!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "1. 在 Kiro 中按 Ctrl+Shift+P" -ForegroundColor White
Write-Host "2. 输入 'MCP' 并选择 'Reconnect MCP Servers'" -ForegroundColor White
Write-Host "3. 对 AI 说: '启动浏览器调试 http://localhost:8090'" -ForegroundColor White
Write-Host ""
Write-Host "如需恢复原配置,运行:" -ForegroundColor Yellow
Write-Host "Copy-Item '$backupPath' '$mcpConfigPath' -Force" -ForegroundColor Gray
Write-Host ""

# 询问是否立即打开配置文件
$openFile = Read-Host "是否打开配置文件查看? (Y/N)"
if ($openFile -eq "Y" -or $openFile -eq "y") {
    notepad $mcpConfigPath
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
