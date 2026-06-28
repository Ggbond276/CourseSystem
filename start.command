#!/bin/bash
# 课程系统一键启动脚本
# 用法：双击本文件即可同时启动前端和后端
# 关闭：请关闭弹出的两个终端窗口

# 切到脚本所在目录（即项目根目录）
cd "$(dirname "$0")"

# --- 启动前自检：避免目录层级写错时悄无声息地失败 ---
echo "正在自检项目目录结构..."
BACKEND_DIR="$(pwd)/server/server"
FRONTEND_DIR="$(pwd)/course_frontend/course_frontend"

if [ ! -x "$BACKEND_DIR/mvnw" ]; then
  echo "[错误] 找不到后端启动文件：$BACKEND_DIR/mvnw"
  echo "请确认 start.command 放在项目根目录，且 server/server/mvnw 存在。"
  exit 1
fi
if [ ! -f "$FRONTEND_DIR/package.json" ]; then
  echo "[错误] 找不到前端 package.json：$FRONTEND_DIR/package.json"
  echo "请确认 start.command 放在项目根目录，且 course_frontend/course_frontend/package.json 存在。"
  exit 1
fi
echo "自检通过 ✓"
echo

# --- 启动后端（新建终端窗口）---
osascript <<EOF
tell application "Terminal"
    do script "cd \"$BACKEND_DIR\" && ./mvnw spring-boot:run"
end tell
EOF

# --- 启动前端（新建终端窗口）---
osascript <<EOF
tell application "Terminal"
    do script "cd \"$FRONTEND_DIR\" && npm run dev"
end tell
EOF

echo "前端、后端启动命令已发出！请在弹出的两个终端窗口中查看运行状态。"
echo "后端地址: http://localhost:8888"
echo "前端地址: http://localhost:5173"
echo "关闭服务：直接关闭两个终端窗口即可"
