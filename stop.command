#!/bin/bash
# 课程系统一键停止脚本
# 用法：双击本文件即可同时关闭前端（Vite）和后端（Spring Boot）

echo "正在关闭前端（Vite）进程..."
pkill -f "vite" 2>/dev/null

echo "正在关闭后端（Spring Boot / mvnw）进程..."
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "mvnw" 2>/dev/null
lsof -ti:8888 | xargs kill -9 2>/dev/null
lsof -ti:5173 | xargs kill -9 2>/dev/null

echo "已全部关闭。"
