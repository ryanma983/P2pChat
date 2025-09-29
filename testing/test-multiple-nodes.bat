@echo off
echo ========================================
echo P2P Chat Multi-Node Test Launcher
echo ========================================
echo.

echo This script will help you start multiple P2P chat instances
echo for testing the peer-to-peer functionality.
echo.

echo Available options:
echo 1. Start Node 1 (Port 8080) - CLI
echo 2. Start Node 2 (Port 8081) - CLI  
echo 3. Start Node 3 (Port 8082) - CLI
echo 4. Start Node 1 (Port 8080) - GUI
echo 5. Start Node 2 (Port 8081) - GUI
echo 6. Start Node 3 (Port 8082) - GUI
echo 7. Custom port - CLI
echo 8. Custom port - GUI
echo 9. Exit
echo.

set /p choice="Enter your choice (1-9): "

if "%choice%"=="1" goto node1_cli
if "%choice%"=="2" goto node2_cli
if "%choice%"=="3" goto node3_cli
if "%choice%"=="4" goto node1_gui
if "%choice%"=="5" goto node2_gui
if "%choice%"=="6" goto node3_gui
if "%choice%"=="7" goto custom_cli
if "%choice%"=="8" goto custom_gui
if "%choice%"=="9" goto end
goto invalid

:node1_cli
echo Starting Node 1 on port 8080 (CLI)...
start "P2P Chat Node 1 (8080)" java -cp ..\target\classes com.group7.chat.Main 8080
goto end

:node2_cli
echo Starting Node 2 on port 8081 (CLI)...
start "P2P Chat Node 2 (8081)" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080
goto end

:node3_cli
echo Starting Node 3 on port 8082 (CLI)...
start "P2P Chat Node 3 (8082)" java -cp ..\target\classes com.group7.chat.Main 8082 localhost:8080
goto end

:node1_gui
echo Starting Node 1 on port 8080 (GUI)...
start "P2P Chat GUI Node 1 (8080)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8080
goto end

:node2_gui
echo Starting Node 2 on port 8081 (GUI)...
start "P2P Chat GUI Node 2 (8081)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8081
goto end

:node3_gui
echo Starting Node 3 on port 8082 (GUI)...
start "P2P Chat GUI Node 3 (8082)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar 8082
goto end

:custom_cli
set /p port="Enter port number: "
echo Starting custom node on port %port% (CLI)...
start "P2P Chat Node (%port%)" java -cp ..\target\classes com.group7.chat.Main %port%
goto end

:custom_gui
set /p port="Enter port number: "
echo Starting custom node on port %port% (GUI)...
start "P2P Chat GUI Node (%port%)" java --module-path . --add-modules javafx.controls,javafx.fxml -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar %port%
goto end

:invalid
echo Invalid choice. Please try again.
pause
goto start

:end
echo.
echo Node started in a new window.
echo.
echo Testing Tips:
echo 1. Start Node 1 first (port 8080)
echo 2. Then start Node 2 (port 8081) - it will auto-connect to Node 1
echo 3. Use 'connect localhost:8080' in CLI to manually connect nodes
echo 4. Use the GUI connection feature to connect nodes
echo.
pause
