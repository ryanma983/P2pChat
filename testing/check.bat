@echo off
title P2P Chat System Check

echo ========================================
echo P2P Chat System Check
echo ========================================
echo.

echo Current directory: %CD%
echo.

echo [1/5] Checking Java...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java not found!
    goto end
) else (
    echo ✅ Java is available
)
echo.

echo [2/5] Checking JAR file...
if exist "..\target\p2p-chat-1.0-SNAPSHOT.jar" (
    echo ✅ JAR file found
    dir "..\target\p2p-chat-1.0-SNAPSHOT.jar"
) else (
    echo ❌ JAR file not found
    echo Please run: mvn clean package
)
echo.

echo [3/5] Checking classes...
if exist "..\target\classes\com\group7\chat\Main.class" (
    echo ✅ Main.class found
) else (
    echo ❌ Main.class not found
    echo Please run: mvn clean compile
)
echo.

echo [4/5] Testing CLI version...
echo Trying to start CLI for 3 seconds...
timeout /t 3 java -cp ..\target\classes com.group7.chat.Main 8080 2>&1
echo.

echo [5/5] Testing JavaFX...
echo Trying JavaFX test...
java --module-path . --add-modules javafx.controls --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ JavaFX not available
    echo For GUI: Install Java with JavaFX
    echo For CLI: Use cli-test.bat
) else (
    echo ✅ JavaFX is available
    echo You can use both gui-test.bat and cli-test.bat
)
echo.

echo ========================================
echo Check Complete
echo ========================================
echo.

:end
pause
