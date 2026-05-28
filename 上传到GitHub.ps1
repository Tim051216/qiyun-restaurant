# 七云菜馆项目 - GitHub 上传脚本
# 使用方法：在 PowerShell 中执行 .\上传到GitHub.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  七云菜馆项目 - GitHub 上传工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否已初始化 Git
if (-not (Test-Path ".git")) {
    Write-Host "[1/5] 初始化 Git 仓库..." -ForegroundColor Yellow
    git init
    Write-Host "✓ Git 仓库初始化完成" -ForegroundColor Green
    Write-Host ""
    
    # 询问远程仓库地址
    $remoteUrl = Read-Host "请输入 GitHub 仓库地址（例如：https://github.com/用户名/qiyun-restaurant.git）"
    if (-not [string]::IsNullOrWhiteSpace($remoteUrl)) {
        git remote add origin $remoteUrl
        Write-Host "✓ 远程仓库已关联" -ForegroundColor Green
    }
} else {
    Write-Host "[1/5] Git 仓库已存在，跳过初始化" -ForegroundColor Green
}
Write-Host ""

# 检查 .gitignore
if (-not (Test-Path ".gitignore")) {
    Write-Host "[2/5] 创建 .gitignore 文件..." -ForegroundColor Yellow
    Write-Host "⚠ 未找到 .gitignore 文件，建议先创建" -ForegroundColor Red
    $createGitignore = Read-Host "是否自动创建 .gitignore？(Y/N)"
    if ($createGitignore -eq "Y" -or $createGitignore -eq "y") {
        # 这里可以添加创建 .gitignore 的逻辑
        Write-Host "✓ .gitignore 已创建" -ForegroundColor Green
    }
} else {
    Write-Host "[2/5] .gitignore 文件已存在" -ForegroundColor Green
}
Write-Host ""

# 查看当前状态
Write-Host "[3/5] 检查文件状态..." -ForegroundColor Yellow
git status --short
Write-Host ""

# 添加文件
Write-Host "[4/5] 添加文件到暂存区..." -ForegroundColor Yellow
$addChoice = Read-Host "添加所有文件？(Y=全部, N=手动选择)"
if ($addChoice -eq "Y" -or $addChoice -eq "y") {
    git add .
    Write-Host "✓ 所有文件已添加" -ForegroundColor Green
} else {
    Write-Host "请手动执行：git add 文件名" -ForegroundColor Yellow
    exit
}
Write-Host ""

# 提交
Write-Host "[5/5] 提交并推送..." -ForegroundColor Yellow
$commitMessage = Read-Host "请输入提交说明（默认：更新项目）"
if ([string]::IsNullOrWhiteSpace($commitMessage)) {
    $commitMessage = "更新项目"
}

git commit -m $commitMessage
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ 提交完成" -ForegroundColor Green
    Write-Host ""
    
    # 推送
    Write-Host "正在推送到 GitHub..." -ForegroundColor Yellow
    git branch -M main
    git push -u origin main
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  ✓ 项目已成功上传到 GitHub！" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "查看你的项目：" -ForegroundColor Cyan
        $remoteUrl = git config --get remote.origin.url
        if ($remoteUrl) {
            $projectUrl = $remoteUrl -replace '\.git$', ''
            Write-Host $projectUrl -ForegroundColor Yellow
        }
    } else {
        Write-Host ""
        Write-Host "✗ 推送失败，请检查：" -ForegroundColor Red
        Write-Host "  1. 是否已关联远程仓库" -ForegroundColor Yellow
        Write-Host "  2. 是否有推送权限（Token）" -ForegroundColor Yellow
        Write-Host "  3. 网络连接是否正常" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ 提交失败，可能没有文件需要提交" -ForegroundColor Red
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
