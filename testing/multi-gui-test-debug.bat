@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Multi-GUI P2P Chat Test Launcher (Debug)
echo ========================================
echo.

echo [DEBUG] Current directory: %CD%
echo [DEBUG] Checking if we're in the testing folder...

if not exist "..\target\p2p-chat-1.0-SNAPSHOT.jar" (
    echo ❌ ERROR: Cannot find JAR file!
    echo Expected location: ..\target\p2p-chat-1.0-SNAPSHOT.jar
    echo Current directory: %CD%
    echo.
    echo Please make sure you're running this from the testing\ folder
    echo and that the project has been compiled with: mvn clean package
    echo.
    pause
    exit /b 1
)

echo ✅ JAR file found!
echo.

echo [DEBUG] Checking Java installation...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: Java not found!
    echo Please install Java and make sure it's in your PATH
    echo.
    pause
    exit /b 1
)

echo ✅ Java found!
echo.

echo [DEBUG] Checking JavaFX availability...
java --module-path . --add-modules javafx.controls --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ JavaFX not available!
    echo.
    echo This could mean:
    echo 1. You don't have JavaFX installed
    echo 2. Your Java version doesn't include JavaFX
    echo.
    echo Solutions:
    echo 1. Run: ..\check-javafx.bat to diagnose the issue
    echo 2. Install Java with JavaFX: https://www.azul.com/downloads/?package=jdk-fx
    echo 3. Use CLI version instead: multi-cli-test.bat
    echo.
    pause
    exit /b 1
)

echo ✅ JavaFX available!
echo.

echo Choose test configuration:
echo 1. Start 2 GUI nodes (8080, 8081)
echo 2. Start 3 GUI nodes (8080, 8081, 8082)
echo 3. Start 4 GUI nodes (8080, 8081, 8082, 8083)
echo 4. Test single GUI node (8080)
echo 5. Exit
echo.

set /p choice="Enter your choice (1-5): "

if "%choice%"=="1" goto two_nodes
if "%choice%"=="2" goto three_nodes
if "%choice%"=="3" goto four_nodes
if "%choice%"=="4" goto single_node
if "%choice%"=="5" goto end
goto invalid

:single_node
echo.
echo [DEBUG] Testing single GUI node...
echo Command: java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080
echo.
echo Starting single GUI node for testing...
start "P2P Chat GUI - Test Node (8080)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080
echo.
echo ✅ Single GUI node started!
echo If the GUI window doesn't appear, there might be a JavaFX issue.
goto success

:two_nodes
echo.
echo [DEBUG] Starting 2 GUI nodes...
echo.
echo [1/2] Starting GUI Node 1 (Port 8080)...
echo [DEBUG] Command: java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080
start "P2P Chat GUI - Node 1 (8080)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080

echo [DEBUG] Waiting 3 seconds...
timeout /t 3 /nobreak >nul

echo [2/2] Starting GUI Node 2 (Port 8081)...
echo [DEBUG] Command: java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8081
start "P2P Chat GUI - Node 2 (8081)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8081

echo.
echo ✅ 2 GUI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081
goto success

:three_nodes
echo.
echo [DEBUG] Starting 3 GUI nodes...
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
echo [DEBUG] Starting 4 GUI nodes...
echo.
for /l %%i in (1,1,4) do (
    set /a port=8079+%%i
    echo [%%i/4] Starting GUI Node %%i (Port !port!)...
    start "P2P Chat GUI - Node %%i (!port!)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar !port!
    timeout /t 3 /nobreak >nul
)

echo.
echo ✅ 4 GUI nodes started!
echo - Node 1: localhost:8080
echo - Node 2: localhost:8081
echo - Node 3: localhost:8082
echo - Node 4: localhost:8083
goto success

:success
echo.
echo ========================================
echo GUI Test Setup Complete!
echo ========================================
echo.
echo What to expect:
echo 1. Multiple GUI windows should open (one for each node)
echo 2. Each window title shows the node number and port
echo 3. Wait for all windows to fully load
echo 4. You should see user lists and chat interfaces
echo.
echo If no GUI windows appear:
echo 1. Check if JavaFX is properly installed
echo 2. Run: ..\check-javafx.bat for diagnosis
echo 3. Try the CLI version: multi-cli-test.bat
echo.
echo Testing Instructions:
echo - Send messages in any window to test group chat
echo - Right-click users in the user list for private chat
echo - Use the file transfer button to send files
echo - Try connecting nodes manually using the Connect button
echo.
goto end

:invalid
echo Invalid choice. Please try again.
pause
goto start

:end
echo.
echo Press any key to exit...
pause >nul
