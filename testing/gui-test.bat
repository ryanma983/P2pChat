@echo off
title P2P Chat GUI Test

echo ========================================
echo P2P Chat GUI Test
echo ========================================
echo.

REM 检查当前目录
echo Current directory: %CD%
echo.

REM 检查JAR文件
if not exist "..\target\p2p-chat-1.0-SNAPSHOT.jar" (
    echo ERROR: JAR file not found!
    echo Expected: ..\target\p2p-chat-1.0-SNAPSHOT.jar
    echo.
    echo Please run: mvn clean package
    echo.
    pause
    exit /b 1
)

echo JAR file found: ..\target\p2p-chat-1.0-SNAPSHOT.jar
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

REM 询问要启动多少个GUI节点
echo How many GUI nodes do you want to start?
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
echo Starting single GUI node on port 8080...
echo.
echo Command: java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080
echo.
start "P2P Chat GUI - Port 8080" java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080
echo.
echo GUI node started!
echo If no window appears, JavaFX might not be available.
echo Try running: ..\check-javafx.bat
goto end

:two
echo.
echo Starting two GUI nodes...
echo.
echo [1/2] Starting node on port 8080...
start "P2P Chat GUI - Port 8080" java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080
timeout /t 3 /nobreak >nul

echo [2/2] Starting node on port 8081...
start "P2P Chat GUI - Port 8081" java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8081
echo.
echo Two GUI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081
goto end

:three
echo.
echo Starting three GUI nodes...
echo.
echo [1/3] Starting node on port 8080...
start "P2P Chat GUI - Port 8080" java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080
timeout /t 3 /nobreak >nul

echo [2/3] Starting node on port 8081...
start "P2P Chat GUI - Port 8081" java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8081
timeout /t 3 /nobreak >nul

echo [3/3] Starting node on port 8082...
start "P2P Chat GUI - Port 8082" java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8082
echo.
echo Three GUI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081
echo - Node 3: localhost:8082
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
echo 1. Wait for GUI windows to appear
echo 2. In each window, you can:
echo    - Send group messages
echo    - Right-click users for private chat
echo    - Use file transfer
echo    - Connect to other nodes manually
echo.
echo If GUI doesn't work, try: cli-test.bat
echo.
pause
