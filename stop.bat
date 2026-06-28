@echo off
chcp 65001 >nul
REM 课程系统一键停止脚本 (Windows 版)
REM 用法：双击本文件即可同时关闭前端（Vite）和后端（Spring Boot）

echo 正在关闭前端（Vite）进程...
taskkill /FI "WINDOWTITLE eq CourseSystem-Frontend*" /T /F >nul 2>&1

echo 正在关闭后端（Spring Boot / mvnw）进程...
taskkill /FI "WINDOWTITLE eq CourseSystem-Backend*" /T /F >nul 2>&1

echo 兜底清理：按端口杀掉占用进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8888 ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
taskkill /IM java.exe /F >nul 2>&1
taskkill /IM node.exe /F >nul 2>&1

echo 已全部关闭。
pause
