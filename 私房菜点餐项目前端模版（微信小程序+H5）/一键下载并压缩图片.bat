@echo off
chcp 65001 >nul
echo ========================================
echo    菜品图片批量下载和压缩工具
echo ========================================
echo.

REM 检查 Python 是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未检测到 Python
    echo 📝 请先安装 Python 3.6 或更高版本
    echo 下载地址：https://www.python.org/downloads/
    pause
    exit /b 1
)

echo ✅ Python 已安装
echo.

REM 检查并安装依赖
echo 📦 检查依赖包...
pip show requests >nul 2>&1
if errorlevel 1 (
    echo 正在安装 requests...
    pip install requests
)

pip show pillow >nul 2>&1
if errorlevel 1 (
    echo 正在安装 pillow...
    pip install pillow
)

echo ✅ 依赖包已就绪
echo.

REM 显示菜单
:menu
echo ========================================
echo 请选择操作：
echo ========================================
echo 1. 下载所有菜品图片（推荐）
echo 2. 仅压缩现有图片
echo 3. 下载并压缩（一键完成）
echo 4. 退出
echo ========================================
echo.

set /p choice=请输入选项 (1-4): 

if "%choice%"=="1" goto download
if "%choice%"=="2" goto compress
if "%choice%"=="3" goto both
if "%choice%"=="4" goto end

echo ❌ 无效选项，请重新选择
echo.
goto menu

:download
echo.
echo 🚀 开始下载图片...
echo.
python download_images_simple.py
echo.
echo ✨ 下载完成！
echo.
pause
goto menu

:compress
echo.
echo 🚀 开始压缩图片...
echo.
python compress_images.py
echo.
echo ✨ 压缩完成！
echo.
pause
goto menu

:both
echo.
echo 🚀 步骤 1/2: 下载图片...
echo.
python download_images_simple.py
echo.
echo 🚀 步骤 2/2: 压缩图片...
echo.
python compress_images.py
echo.
echo ✨ 全部完成！
echo.
pause
goto menu

:end
echo.
echo 👋 再见！
echo.
pause
