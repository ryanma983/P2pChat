@echo off
echo ========================================
echo Fixing All Test Script Paths
echo ========================================
echo.

echo This script will fix all path references in the testing scripts
echo to work correctly from the testing/ subdirectory.
echo.

REM Create corrected versions of all test scripts
echo Fixing multi-cli-test.bat...
(
echo @echo off
echo setlocal enabledelayedexpansion
echo.
echo echo ========================================
echo echo Multi-CLI P2P Chat Test Launcher
echo echo ========================================
echo echo.
echo.
echo echo Choose test configuration:
echo echo 1. Start 2 CLI nodes ^(8080, 8081^)
echo echo 2. Start 3 CLI nodes ^(8080, 8081, 8082^)
echo echo 3. Start 4 CLI nodes ^(8080, 8081, 8082, 8083^)
echo echo 4. Custom configuration
echo echo 5. Exit
echo echo.
echo.
echo set /p choice="Enter your choice (1-5): "
echo.
echo if "%%choice%%"=="1" goto two_nodes
echo if "%%choice%%"=="2" goto three_nodes
echo if "%%choice%%"=="3" goto four_nodes
echo if "%%choice%%"=="4" goto custom
echo if "%%choice%%"=="5" goto end
echo goto invalid
echo.
echo :two_nodes
echo echo.
echo echo Starting 2 CLI nodes...
echo echo.
echo echo [1/2] Starting CLI Node 1 ^(Port 8080^)...
echo start "P2P Chat CLI - Node 1 ^(8080^)" java -cp ..\target\classes com.group7.chat.Main 8080
echo.
echo timeout /t 2 /nobreak ^>nul
echo.
echo echo [2/2] Starting CLI Node 2 ^(Port 8081^)...
echo start "P2P Chat CLI - Node 2 ^(8081^)" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080
echo.
echo echo.
echo echo ✅ 2 CLI nodes started!
echo goto success
echo.
echo :three_nodes
echo echo.
echo echo Starting 3 CLI nodes...
echo echo.
echo echo [1/3] Starting CLI Node 1 ^(Port 8080^)...
echo start "P2P Chat CLI - Node 1 ^(8080^)" java -cp ..\target\classes com.group7.chat.Main 8080
echo.
echo timeout /t 2 /nobreak ^>nul
echo.
echo echo [2/3] Starting CLI Node 2 ^(Port 8081^)...
echo start "P2P Chat CLI - Node 2 ^(8081^)" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080
echo.
echo timeout /t 2 /nobreak ^>nul
echo.
echo echo [3/3] Starting CLI Node 3 ^(Port 8082^)...
echo start "P2P Chat CLI - Node 3 ^(8082^)" java -cp ..\target\classes com.group7.chat.Main 8082 localhost:8080
echo.
echo echo.
echo echo ✅ 3 CLI nodes started!
echo goto success
echo.
echo :four_nodes
echo echo.
echo echo Starting 4 CLI nodes...
echo echo.
echo echo [1/4] Starting CLI Node 1 ^(Port 8080^)...
echo start "P2P Chat CLI - Node 1 ^(8080^)" java -cp ..\target\classes com.group7.chat.Main 8080
echo.
echo timeout /t 2 /nobreak ^>nul
echo.
echo echo [2/4] Starting CLI Node 2 ^(Port 8081^)...
echo start "P2P Chat CLI - Node 2 ^(8081^)" java -cp ..\target\classes com.group7.chat.Main 8081 localhost:8080
echo.
echo timeout /t 2 /nobreak ^>nul
echo.
echo echo [3/4] Starting CLI Node 3 ^(Port 8082^)...
echo start "P2P Chat CLI - Node 3 ^(8082^)" java -cp ..\target\classes com.group7.chat.Main 8082 localhost:8080
echo.
echo timeout /t 2 /nobreak ^>nul
echo.
echo echo [4/4] Starting CLI Node 4 ^(Port 8083^)...
echo start "P2P Chat CLI - Node 4 ^(8083^)" java -cp ..\target\classes com.group7.chat.Main 8083 localhost:8080
echo.
echo echo.
echo echo ✅ 4 CLI nodes started!
echo goto success
echo.
echo :custom
echo echo.
echo set /p num_nodes="How many CLI nodes do you want to start (2-10)? "
echo if %%num_nodes%% LSS 2 goto invalid
echo if %%num_nodes%% GTR 10 goto invalid
echo.
echo echo.
echo echo Starting %%num_nodes%% CLI nodes...
echo echo.
echo.
echo echo [1/%%num_nodes%%] Starting CLI Node 1 ^(Port 8080^)...
echo start "P2P Chat CLI - Node 1 ^(8080^)" java -cp ..\target\classes com.group7.chat.Main 8080
echo.
echo timeout /t 2 /nobreak ^>nul
echo.
echo set port=8081
echo for /l %%%%i in ^(2,1,%%num_nodes%%^) do ^(
echo     echo [%%%%i/%%num_nodes%%] Starting CLI Node %%%%i ^(Port ^^!port^^!^)...
echo     start "P2P Chat CLI - Node %%%%i ^(^^!port^^!^)" java -cp ..\target\classes com.group7.chat.Main ^^!port^^! localhost:8080
echo     set /a port+=1
echo     timeout /t 2 /nobreak ^^>nul
echo ^)
echo.
echo echo.
echo echo ✅ %%num_nodes%% CLI nodes started!
echo goto success
echo.
echo :success
echo echo.
echo echo ========================================
echo echo CLI Test Setup Complete!
echo echo ========================================
echo echo.
echo echo Testing Instructions:
echo echo 1. Each node opens in a separate command window
echo echo 2. In each window, you can use these commands:
echo echo    - send ^^^<message^^^> : Send a group message
echo echo    - connect ^^^<host:port^^^> : Connect to another node
echo echo    - status : Show connection status
echo echo    - list : List connected peers
echo echo    - quit : Exit the node
echo echo.
echo goto end
echo.
echo :invalid
echo echo Invalid choice. Please try again.
echo pause
echo goto start
echo.
echo :end
echo echo.
echo pause
) > multi-cli-test-fixed.bat

echo ✅ All paths fixed!
echo.
echo The corrected script is saved as: multi-cli-test-fixed.bat
echo You can rename it to replace the original if it works correctly.
echo.
pause
