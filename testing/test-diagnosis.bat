@echo off
echo ========================================
echo P2P Chat Test Diagnosis Tool
echo ========================================
echo.

echo This tool will help diagnose why the GUI test might be failing.
echo.

echo [1/6] Checking current directory...
echo Current directory: %CD%
echo.

echo [2/6] Checking if JAR file exists...
if exist "..\target\p2p-chat-1.0-SNAPSHOT.jar" (
    echo ✅ JAR file found: ..\target\p2p-chat-1.0-SNAPSHOT.jar
    dir "..\target\p2p-chat-1.0-SNAPSHOT.jar"
) else (
    echo ❌ JAR file NOT found!
    echo Expected location: ..\target\p2p-chat-1.0-SNAPSHOT.jar
    echo.
    echo This means the project hasn't been compiled yet.
    echo Please run: mvn clean package
    echo.
    goto end
)
echo.

echo [3/6] Checking Java installation...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java not found in PATH!
    echo Please install Java and add it to your PATH
    goto end
) else (
    echo ✅ Java is available
)
echo.

echo [4/6] Testing basic JAR execution...
echo Trying to run: java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar --help
java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar --help 2>&1
echo Return code: %ERRORLEVEL%
echo.

echo [5/6] Testing JavaFX availability...
echo Trying: java --module-path . --add-modules javafx.controls --version
java --module-path . --add-modules javafx.controls --version 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ JavaFX test failed!
    echo This is likely the cause of the GUI test failure.
) else (
    echo ✅ JavaFX test passed!
)
echo.

echo [6/6] Testing CLI version...
echo Trying to start CLI version for 5 seconds...
timeout /t 5 java -cp ..\target\classes com.group7.chat.Main 8080 2>&1
echo.

echo ========================================
echo Diagnosis Summary
echo ========================================
echo.

if not exist "..\target\p2p-chat-1.0-SNAPSHOT.jar" (
    echo ❌ PROBLEM: JAR file missing
    echo SOLUTION: Run 'mvn clean package' in the main project directory
    goto end
)

java --module-path . --add-modules javafx.controls --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ PROBLEM: JavaFX not available
    echo SOLUTIONS:
    echo 1. Install Java with JavaFX: https://www.azul.com/downloads/?package=jdk-fx
    echo 2. Use CLI version instead: multi-cli-test.bat
    echo 3. Run: ..\check-javafx.bat for detailed JavaFX diagnosis
) else (
    echo ✅ All checks passed! GUI test should work.
    echo If it still fails, try running: multi-gui-test-debug.bat
)

:end
echo.
echo Press any key to exit...
pause >nul
