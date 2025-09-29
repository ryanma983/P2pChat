@echo off
title P2P Chat CLI Test

echo ========================================
echo P2P Chat CLI Test
echo ========================================
echo.

REM 检查当前目录
echo Current directory: %CD%
echo.

REM 检查classes目录
if not exist "..\target\classes\com\group7\chat\Main.class" (
    echo ERROR: Main.class not found!
    echo Expected: ..\target\classes\com\group7\chat\Main.class
    echo.
    echo Please run: mvn clean compile
    echo.
    pause
    exit /b 1
)

echo Main.class found: ..\target\classes\com\group7\chat\Main.class
echo.

REM 检查Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found!
    echo Please install Java and add it to PATH
    echo.
    pause
    exit /b 1
)

echo Java is available.
echo.

REM 询问要启动多少个CLI节点
echo How many CLI nodes do you want to start?
echo 1. Single node (port 8080)
echo 2. Two nodes (ports 8080, 8081)
echo 3. Three nodes (ports 8080, 8081, 8082)
echo.
set /p choice="Enter choice (1-3): "

if "%choice%"=="1" goto single
if "%choice%"=="2" goto two
if "%choice%"=="3" goto three
goto invalid

:single
echo.
echo Starting single CLI node on port 8080...
echo.
echo Command: java -cp ..\target\classes com.group7.chat.Main 8080
echo.
start "P2P Chat CLI - Port 8080" java -cp ..\target\classes com.group7.chat.Main 8080
echo.
echo CLI node started in new window!
goto end

:two
echo.
echo Starting two CLI nodes...
echo.
echo [1/2] Starting node on port 8080...
start "P2P Chat CLI - Port 8080" java -cp ..\target\classes com.group7.chat.Main 8080
timeout /t 2 /nobreak >nul

echo [2/2] Starting node on port 8081 (auto-connects to 8080)...
start "P2P Chat CLI - Port 8081" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080
echo.
echo Two CLI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081 (connected to Node 1)
goto end

:three
echo.
echo Starting three CLI nodes...
echo.
echo [1/3] Starting node on port 8080...
start "P2P Chat CLI - Port 8080" java -cp ..\target\classes com.group7.chat.Main 8080
timeout /t 2 /nobreak >nul

echo [2/3] Starting node on port 8081 (auto-connects to 8080)...
start "P2P Chat CLI - Port 8081" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080
timeout /t 2 /nobreak >nul

echo [3/3] Starting node on port 8082 (auto-connects to 8080)...
start "P2P Chat CLI - Port 8082" java -cp ..\target\classes com.group7.chat.Main 8082 localhost:8080
echo.
echo Three CLI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081 (connected to Node 1)
echo - Node 3: localhost:8082 (connected to Node 1)
goto end

:invalid
echo Invalid choice. Please try again.
pause
goto start

:end
echo.
echo ========================================
echo Test Instructions:
echo ========================================
echo.
echo Each node runs in a separate command window.
echo In each window, you can use these commands:
echo.
echo   send Hello!           - Send group message
echo   connect localhost:8080 - Connect to another node
echo   status                - Show connection status
echo   list                  - List connected peers
echo   quit                  - Exit the node
echo.
echo Example test:
echo 1. In Node 2 window, type: send Hello from Node 2!
echo 2. Check if Node 1 and Node 3 receive the message
echo 3. Try: status (to see connections)
echo.
pause
