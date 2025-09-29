@echo off
echo ========================================
echo Quick P2P Test Setup
echo ========================================
echo.

echo Starting 3 nodes for testing...
echo.

echo [1/3] Starting Node 1 (Port 8080)...
start "Node 1 - Port 8080" java -cp ..\target\classes com.group7.chat.Main 8080

timeout /t 2 /nobreak >nul

echo [2/3] Starting Node 2 (Port 8081)...
start "Node 2 - Port 8081" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080

timeout /t 2 /nobreak >nul

echo [3/3] Starting Node 3 (Port 8082)...
start "Node 3 - Port 8082" java -cp ..\target\classes com.group7.chat.Main 8082 localhost:8080

echo.
echo ========================================
echo All nodes started!
echo ========================================
echo.
echo You now have 3 P2P chat nodes running:
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081 (connected to Node 1)
echo - Node 3: localhost:8082 (connected to Node 1)
echo.
echo Test the chat functionality between the nodes!
echo.
pause
