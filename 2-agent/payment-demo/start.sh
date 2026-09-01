#!/bin/bash

echo "========================================"
echo "  支付演示项目 - 一键启动脚本"
echo "========================================"
echo ""

# 项目根目录
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "[1/3] 启动 MySQL (Docker)..."
cd "$ROOT_DIR"
docker-compose up -d
if [ $? -ne 0 ]; then
    echo "[警告] Docker 启动失败，请确保 MySQL 已手动启动"
else
    echo "[✓] MySQL 容器已启动"
    echo "    等待数据库初始化..."
    sleep 10
fi

echo ""
echo "[2/3] 启动后端 Spring Boot..."
cd "$ROOT_DIR/backend"
mvn spring-boot:run &
BACKEND_PID=$!
echo "    后端 PID: $BACKEND_PID"
echo "    等待后端启动 (30秒)..."
sleep 30

echo ""
echo "[3/3] 启动前端 Vue3..."
cd "$ROOT_DIR/frontend"
if [ ! -d "node_modules" ]; then
    echo "    安装前端依赖..."
    npm install
fi
npm run dev &
FRONTEND_PID=$!

echo ""
echo "========================================"
echo "  启动完成！"
echo "========================================"
echo "  前端: http://localhost:5173"
echo "  后端: http://localhost:8080"
echo ""
echo "  支付宝沙箱配置:"
echo "  1. 访问 https://open.alipay.com 注册沙箱"
echo "  2. 修改 backend/src/main/resources/application.yml"
echo "  3. 填入沙箱 AppID、私钥、支付宝公钥"
echo ""
echo "  按 Ctrl+C 停止所有服务"
echo "========================================"

# 等待用户中断
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT
wait
