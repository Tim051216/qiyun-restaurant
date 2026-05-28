@echo off
chcp 65001 >nul
echo ========================================
echo   清理大文件 - 准备上传 GitHub
echo ========================================
echo.
echo 正在删除以下文件夹：
echo   - 所有 target/ 文件夹 (Maven 编译文件)
echo   - 所有 node_modules/ 文件夹 (Node.js 依赖)
echo   - 所有 dist/ 文件夹 (前端构建文件)
echo   - 所有 unpackage/ 文件夹 (uni-app 编译文件)
echo.
echo 开始清理...
echo.

echo [1/4] 删除 target/ 文件夹...
for /d /r . %%d in (target) do @if exist "%%d" (
    echo   删除: %%d
    rd /s /q "%%d" 2>nul
)

echo [2/4] 删除 node_modules/ 文件夹...
for /d /r . %%d in (node_modules) do @if exist "%%d" (
    echo   删除: %%d
    rd /s /q "%%d" 2>nul
)

echo [3/4] 删除 dist/ 文件夹...
for /d /r . %%d in (dist) do @if exist "%%d" (
    echo   删除: %%d
    rd /s /q "%%d" 2>nul
)

echo [4/4] 删除 unpackage/ 文件夹...
for /d /r . %%d in (unpackage) do @if exist "%%d" (
    echo   删除: %%d
    rd /s /q "%%d" 2>nul
)

echo.
echo ========================================
echo   清理完成！
echo ========================================
echo.
echo 项目已准备好上传到 GitHub！
echo.
echo 下一步：
echo   1. 在 GitHub 创建仓库
echo   2. 上传项目文件
echo.
pause
