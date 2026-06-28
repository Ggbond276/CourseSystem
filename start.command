#!/bin/bash
# 课程系统一键启动脚本
# 用法：双击本文件即可同时启动前端和后端
# 关闭：请关闭弹出的两个终端窗口

# 切到脚本所在目录（即项目根目录）
cd "$(dirname "$0")"

# --- 启动后端（新建终端窗口）---
osascript <<EOF
tell application "Terminal"
    do script "cd \"$(pwd)/server\" && ./mvnw spring-boot:run"
end tell
EOF

# --- 启动前端（新建终端窗口）---
osascript <<EOF
tell application "Terminal"
    do script "cd \"$(pwd)/course_frontend\" && npm run dev"
end tell
EOF

echo "前端、后端启动命令已发出！请在弹出的两个终端窗口中查看运行状态。"
echo "后端地址: http://localhost:8888"
echo "前端地址: http://localhost:5173"
echo "关闭服务：直接关闭两个终端窗口即可"
