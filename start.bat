@echo off
chcp 65001 >nul
REM 课程系统一键启动脚本 (Windows 版)
REM 用法：双击本文件即可同时启动前端和后端
REM 关闭：直接关闭弹出的两个命令行窗口

echo ========================================
echo   课程系统一键启动脚本
echo ========================================
echo.

REM 切到脚本所在目录（即项目根目录）
cd /d "%~dp0"

REM --- 启动后端（最小化窗口）---
start "CourseSystem-Backend" /MIN cmd /k "cd /d %CD%\server\server && mvnw spring-boot:run"

REM --- 启动前端（最小化窗口）---
start "CourseSystem-Frontend" /MIN cmd /k "cd /d %CD%\course_frontend\course_frontend && npm run dev"

echo 前端、后端启动命令已发出！
echo 后端地址: http://localhost:8888
echo 前端地址: http://localhost:5173
echo 关闭服务：直接关闭两个命令行窗口即可
echo.
pause
