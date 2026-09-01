@echo off
echo ========================================
echo   支付演示项目 - 一键启动脚本
echo ========================================
echo.

echo [1/3] 启动 MySQL (Docker)...
cd /d "%~dp0"
docker-compose up -d
if %ERRORLEVEL% neq 0 (
    echo [警告] Docker 启动失败，请确保 MySQL 已手动启动
) else (
    echo [✓] MySQL 容器已启动
    echo     等待数据库初始化...
    timeout /t 10 /nobreak > nul
)

echo.
echo [2/3] 启动后端 Spring Boot...
cd /d "%~dp0backend"
start "Payment Backend" cmd /k "mvn spring-boot:run"

echo     等待后端启动 (30秒)...
timeout /t 30 /nobreak > nul

echo.
echo [3/3] 启动前端 Vue3...
cd /d "%~dp0frontend"
if not exist "node_modules" (
    echo     安装前端依赖...
    call npm install
)
start "Payment Frontend" cmd /k "npm run dev"

echo.
echo ========================================
echo   启动完成！
echo ========================================
echo   前端: http://localhost:5173
echo   后端: http://localhost:8080
echo.
echo   支付宝沙箱配置:
echo   1. 访问 https://open.alipay.com 注册沙箱环境
echo   2. 修改 backend/src/main/resources/application.yml
echo   3. 填入你的沙箱 AppID、私钥、支付宝公钥
echo ========================================
echo.
pause
