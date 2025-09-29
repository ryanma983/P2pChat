@echo off
echo ========================================
echo Multi-GUI P2P Chat Test Launcher
echo ========================================
echo.

echo This will start multiple GUI instances for P2P testing.
echo Make sure you have JavaFX support installed!
echo.

echo Checking JavaFX availability...
java --module-path . --add-modules javafx.controls --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ JavaFX not available! 
    echo Please install Java with JavaFX support or run ..\check-javafx.bat
    echo.
    echo Alternative: Use multi-cli-test.bat for command line version
    pause
    exit /b 1
)

echo ✅ JavaFX available!
echo.

echo Choose test configuration:
echo 1. Start 2 GUI nodes (8080, 8081)
echo 2. Start 3 GUI nodes (8080, 8081, 8082)
echo 3. Start 4 GUI nodes (8080, 8081, 8082, 8083)
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
echo Starting 2 GUI nodes...
echo.
echo [1/2] Starting GUI Node 1 (Port 8080)...
start "P2P Chat GUI - Node 1 (8080)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080

timeout /t 3 /nobreak >nul

echo [2/2] Starting GUI Node 2 (Port 8081)...
start "P2P Chat GUI - Node 2 (8081)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8081

echo.
echo ✅ 2 GUI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081
goto success

:three_nodes
echo.
echo Starting 3 GUI nodes...
echo.
echo [1/3] Starting GUI Node 1 (Port 8080)...
start "P2P Chat GUI - Node 1 (8080)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080

timeout /t 3 /nobreak >nul

echo [2/3] Starting GUI Node 2 (Port 8081)...
start "P2P Chat GUI - Node 2 (8081)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8081

timeout /t 3 /nobreak >nul

echo [3/3] Starting GUI Node 3 (Port 8082)...
start "P2P Chat GUI - Node 3 (8082)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8082

echo.
echo ✅ 3 GUI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081
echo - Node 3: localhost:8082
goto success

:four_nodes
echo.
echo Starting 4 GUI nodes...
echo.
echo [1/4] Starting GUI Node 1 (Port 8080)...
start "P2P Chat GUI - Node 1 (8080)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080

timeout /t 3 /nobreak >nul

echo [2/4] Starting GUI Node 2 (Port 8081)...
start "P2P Chat GUI - Node 2 (8081)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8081

timeout /t 3 /nobreak >nul

echo [3/4] Starting GUI Node 3 (Port 8082)...
start "P2P Chat GUI - Node 3 (8082)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8082

timeout /t 3 /nobreak >nul

echo [4/4] Starting GUI Node 4 (Port 8083)...
start "P2P Chat GUI - Node 4 (8083)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8083

echo.
echo ✅ 4 GUI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081
echo - Node 3: localhost:8082
echo - Node 4: localhost:8083
goto success

:custom
echo.
set /p num_nodes="How many GUI nodes do you want to start (2-10)? "
if %num_nodes% LSS 2 goto invalid
if %num_nodes% GTR 10 goto invalid

echo.
echo Starting %num_nodes% GUI nodes...
echo.

set port=8080
for /l %%i in (1,1,%num_nodes%) do (
    echo [%%i/%num_nodes%] Starting GUI Node %%i (Port !port!)...
    start "P2P Chat GUI - Node %%i (!port!)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar !port!
    set /a port+=1
    timeout /t 3 /nobreak >nul
)

echo.
echo ✅ %num_nodes% GUI nodes started!
goto success

:success
echo.
echo ========================================
echo GUI Test Setup Complete!
echo ========================================
echo.
echo Testing Instructions:
echo 1. Wait for all GUI windows to fully load
echo 2. In each window, you can:
echo    - See other connected nodes in the user list
echo    - Send group messages in the main chat area
echo    - Right-click users for private chat
echo    - Use the file transfer button to send files
echo 3. Test network connectivity between nodes
echo 4. Try disconnecting/reconnecting nodes
echo.
echo To connect nodes manually:
echo - Use the "Connect" button in the GUI
echo - Enter: localhost:8080 (or other port numbers)
echo.
goto end

:invalid
echo Invalid choice. Please try again.
pause
goto start

:end
echo.
pause
