@echo off
echo ========================================
echo P2P Chat GUI Launcher
echo ========================================
echo.

echo Checking Java version...
java -version
echo.

echo [Step 1] Trying GUI with JavaFX modules...
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar
if %ERRORLEVEL% EQU 0 goto success

echo.
echo [Step 2] Trying direct JAR execution...
java -jar target\p2p-chat-1.0-SNAPSHOT.jar
if %ERRORLEVEL% EQU 0 goto success

echo.
echo ========================================
echo GUI version failed to start!
echo ========================================
echo.
echo Your Java environment does not have JavaFX support.
echo.
echo SOLUTIONS:
echo.
echo Option 1 - Install Java with JavaFX (RECOMMENDED):
echo   1. Download: https://www.azul.com/downloads/?package=jdk-fx
echo   2. Choose "Azul Zulu JDK FX" for Windows
echo   3. Install and restart this script
echo.
echo Option 2 - Download JavaFX SDK:
echo   1. Download: https://openjfx.io/
echo   2. Extract to C:\javafx-sdk-17.0.2
echo   3. Run: java --module-path "C:\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar
echo.
echo Option 3 - Use command line version:
echo   Run: scripts\start-cli.bat
echo.
goto end

:success
echo.
echo ========================================
echo GUI Application started successfully!
echo ========================================

:end
echo.
pause
