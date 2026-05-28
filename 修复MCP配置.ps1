# 修复 MCP 配置文件中的 JSON 语法错误
# 问题：在 "disabled": true 后面有多余的 ",m"

$mcpConfigPath = "$env:USERPROFILE\.kiro\settings\mcp.json"

Write-Host "正在修复 MCP 配置文件..." -ForegroundColor Yellow
Write-Host "文件路径: $mcpConfigPath" -ForegroundColor Cyan

# 检查文件是否存在
if (-not (Test-Path $mcpConfigPath)) {
    Write-Host "错误: 找不到 MCP 配置文件" -ForegroundColor Red
    Write-Host "路径: $mcpConfigPath" -ForegroundColor Red
    exit 1
}

# 备份原文件
$backupPath = "$mcpConfigPath.backup"
Copy-Item $mcpConfigPath $backupPath -Force
Write-Host "已创建备份: $backupPath" -ForegroundColor Green

# 读取文件内容
$content = Get-Content $mcpConfigPath -Raw

# 显示原内容
Write-Host "`n原文件内容:" -ForegroundColor Yellow
Write-Host $content -ForegroundColor Gray

# 修复 JSON 错误：删除 ",m"
$fixedContent = $content -replace '"disabled":\s*true,m', '"disabled": true,'

# 写入修复后的内容
$fixedContent | Set-Content $mcpConfigPath -NoNewline

Write-Host "`n修复后的内容:" -ForegroundColor Yellow
Write-Host $fixedContent -ForegroundColor Green

Write-Host "`n✅ 修复完成！" -ForegroundColor Green
Write-Host "原文件已备份到: $backupPath" -ForegroundColor Cyan
Write-Host "`n请重启 Kiro 或重新加载配置以使更改生效。" -ForegroundColor Yellow
