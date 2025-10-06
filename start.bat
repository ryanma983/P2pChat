@echo off
echo ========================================
echo P2P Chat Application Launcher
echo ========================================
echo.

echo [1/3] Trying GUI version with JavaFX...
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar 2>nul
if %ERRORLEVEL% EQU 0 goto success

echo GUI failed. [2/3] Trying simple JAR...
java -jar target\p2p-chat-1.0-SNAPSHOT.jar 2>nul
if %ERRORLEVEL% EQU 0 goto success

echo JAR failed. [3/3] Starting CLI version...
if exist "target\classes\com\group7\chat\Main.class" (
    echo.
    echo ========================================
    echo Starting Command Line Interface
    echo ========================================
    echo Note: GUI version requires JavaFX support
    echo For GUI version, see: start-gui-only.bat
    echo ========================================
    echo.
    java -cp target\classes com.group7.chat.Main
    if %ERRORLEVEL% EQU 0 goto success
)

echo.
echo ========================================
echo Unable to start application!
echo ========================================
echo.
echo Solutions:
echo 1. Install Java with JavaFX: https://www.azul.com/downloads/?package=jdk-fx
echo 2. See documentation\INSTALL_JAVAFX.md for detailed help
echo 3. Use scripts\start-cli.bat for command line version
goto end

:success
echo Application started successfully!

:end
echo.
pause