@echo off
echo ========================================
echo Multi-CLI P2P Chat Test Launcher
echo ========================================
echo.

echo This will start multiple CLI instances for P2P testing.
echo No JavaFX required!
echo.

echo Choose test configuration:
echo 1. Start 2 CLI nodes (8080, 8081)
echo 2. Start 3 CLI nodes (8080, 8081, 8082)
echo 3. Start 4 CLI nodes (8080, 8081, 8082, 8083)
echo 4. Custom configuration
echo 5. Exit
echo.

set /p choice="Enter your choice (1-5): "

if "%choice%"=="1" goto two_nodes
if "%choice%"=="2" goto three_nodes
if "%choice%"=="3" goto four_nodes
if "%choice%"=="4" goto custom
if "%choice%"=="5" goto end
goto invalid

:two_nodes
echo.
echo Starting 2 CLI nodes...
echo.
echo [1/2] Starting CLI Node 1 (Port 8080)...
start "P2P Chat CLI - Node 1 (8080)" java -cp ..\target\classes com.group7.chat.Main 8080

timeout /t 2 /nobreak >nul

echo [2/2] Starting CLI Node 2 (Port 8081)...
start "P2P Chat CLI - Node 2 (8081)" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080

echo.
echo ✅ 2 CLI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081 (auto-connected to Node 1)
goto success

:three_nodes
echo.
echo Starting 3 CLI nodes...
echo.
echo [1/3] Starting CLI Node 1 (Port 8080)...
start "P2P Chat CLI - Node 1 (8080)" java -cp ..\target\classes com.group7.chat.Main 8080

timeout /t 2 /nobreak >nul

echo [2/3] Starting CLI Node 2 (Port 8081)...
start "P2P Chat CLI - Node 2 (8081)" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080

timeout /t 2 /nobreak >nul

echo [3/3] Starting CLI Node 3 (Port 8082)...
start "P2P Chat CLI - Node 3 (8082)" java -cp ..\target\classes com.group7.chat.Main 8082 localhost:8080

echo.
echo ✅ 3 CLI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081 (auto-connected to Node 1)
echo - Node 3: localhost:8082 (auto-connected to Node 1)
goto success

:four_nodes
echo.
echo Starting 4 CLI nodes...
echo.
echo [1/4] Starting CLI Node 1 (Port 8080)...
start "P2P Chat CLI - Node 1 (8080)" java -cp ..\target\classes com.group7.chat.Main 8080

timeout /t 2 /nobreak >nul

echo [2/4] Starting CLI Node 2 (Port 8081)...
start "P2P Chat CLI - Node 2 (8081)" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080

timeout /t 2 /nobreak >nul

echo [3/4] Starting CLI Node 3 (Port 8082)...
start "P2P Chat CLI - Node 3 (8082)" java -cp ..\target\classes com.group7.chat.Main 8082 localhost:8080

timeout /t 2 /nobreak >nul

echo [4/4] Starting CLI Node 4 (Port 8083)...
start "P2P Chat CLI - Node 4 (8083)" java -cp ..\target\classes com.group7.chat.Main 8083 localhost:8080

echo.
echo ✅ 4 CLI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081 (auto-connected to Node 1)
echo - Node 3: localhost:8082 (auto-connected to Node 1)
echo - Node 4: localhost:8083 (auto-connected to Node 1)
goto success

:custom
echo.
set /p num_nodes="How many CLI nodes do you want to start (2-10)? "
if %num_nodes% LSS 2 goto invalid
if %num_nodes% GTR 10 goto invalid

echo.
echo Starting %num_nodes% CLI nodes...
echo.

echo [1/%num_nodes%] Starting CLI Node 1 (Port 8080)...
start "P2P Chat CLI - Node 1 (8080)" java -cp ..\target\classes com.group7.chat.Main 8080

timeout /t 2 /nobreak >nul

set port=8081
for /l %%i in (2,1,%num_nodes%) do (
    echo [%%i/%num_nodes%] Starting CLI Node %%i (Port !port!)...
    start "P2P Chat CLI - Node %%i (!port!)" java -cp ..\target\classes com.group7.chat.Main !port! localhost:8080
    set /a port+=1
    timeout /t 2 /nobreak >nul
)

echo.
echo ✅ %num_nodes% CLI nodes started!
goto success

:success
echo.
echo ========================================
echo CLI Test Setup Complete!
echo ========================================
echo.
echo Testing Instructions:
echo 1. Each node opens in a separate command window
echo 2. In each window, you can use these commands:
echo    - send ^<message^> : Send a group message
echo    - connect ^<host:port^> : Connect to another node
echo    - status : Show connection status
echo    - list : List connected peers
echo    - quit : Exit the node
echo.
echo Example test sequence:
echo 1. In Node 2 window: send Hello from Node 2!
echo 2. Check if Node 1 and Node 3 receive the message
echo 3. Try: status (to see connections)
echo 4. Try: list (to see peer list)
echo.
goto end

:invalid
echo Invalid choice. Please try again.
pause
goto start

:end
echo.
pause
