@echo off
chcp 65001 >nul
echo ========================================
echo P2P Chat Application Launcher
echo ========================================
echo.

echo Checking Java version...
java -version
echo.

echo Trying to run P2P Chat GUI...
echo.

echo [Method 1] Using JavaFX module path...
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar
if %ERRORLEVEL% EQU 0 goto success

echo [Method 2] Direct JAR execution...
java -jar target\p2p-chat-1.0-SNAPSHOT.jar
if %ERRORLEVEL% EQU 0 goto success

echo [Method 3] Trying CLI version...
if exist "target\classes\com\group7\chat\Main.class" (
    echo Running command line version...
    java -cp target\classes com.group7.chat.Main
    if %ERRORLEVEL% EQU 0 goto success
) else (
    echo Classes not found. Please run: mvn clean compile
)

echo.
echo ========================================
echo All methods failed!
echo ========================================
echo.
echo Possible solutions:
echo 1. Download JavaFX SDK from: https://openjfx.io/
echo 2. Install Java with JavaFX from: https://www.azul.com/downloads/?package=jdk-fx
echo 3. Check JAVAFX_RUNTIME_SOLUTIONS.md for detailed help
goto end

:success
echo.
echo ========================================
echo Application started successfully!
echo ========================================

:end
echo.
pause
