@echo off
echo ========================================
echo P2P Chat Test Suite Launcher
echo ========================================
echo.

echo Welcome to the P2P Chat testing suite!
echo Choose your testing method:
echo.

echo 1. Multi-GUI Test (Recommended for visual testing)
echo 2. Multi-CLI Test (No JavaFX required)
echo 3. Interactive Node Launcher (Advanced)
echo 4. Quick 3-Node Test (CLI)
echo 5. Check JavaFX Installation
echo 6. View Testing Guide
echo 7. Exit
echo.

set /p choice="Enter your choice (1-7): "

if "%choice%"=="1" goto multi_gui
if "%choice%"=="2" goto multi_cli
if "%choice%"=="3" goto interactive
if "%choice%"=="4" goto quick_test
if "%choice%"=="5" goto check_javafx
if "%choice%"=="6" goto view_guide
if "%choice%"=="7" goto end
goto invalid

:multi_gui
echo.
echo Launching Multi-GUI Test...
call multi-gui-test.bat
goto end

:multi_cli
echo.
echo Launching Multi-CLI Test...
call multi-cli-test.bat
goto end

:interactive
echo.
echo Launching Interactive Node Launcher...
call test-multiple-nodes.bat
goto end

:quick_test
echo.
echo Launching Quick 3-Node Test...
call quick-test.bat
goto end

:check_javafx
echo.
echo Checking JavaFX Installation...
call ..\check-javafx.bat
goto end

:view_guide
echo.
echo Opening Testing Guide...
if exist TESTING_GUIDE.md (
    start notepad TESTING_GUIDE.md
) else (
    echo Testing guide not found!
)
pause
goto start

:invalid
echo Invalid choice. Please try again.
pause
goto start

:end
echo.
echo Thank you for using P2P Chat Test Suite!
pause
