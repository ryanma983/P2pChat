@echo off
echo Starting P2P Chat Application...
echo.

REM Try GUI version first
echo [1/3] Trying GUI version...
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar 2>nul
if %ERRORLEVEL% EQU 0 goto success

echo [2/3] Trying simple JAR...
java -jar target\p2p-chat-1.0-SNAPSHOT.jar 2>nul
if %ERRORLEVEL% EQU 0 goto success

echo [3/3] Trying CLI version...
if exist "target\classes\com\group7\chat\Main.class" (
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
