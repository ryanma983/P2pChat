@echo off
echo ========================================
echo P2P Chat Application Launcher
echo ========================================
echo.

echo Checking Java version...
java -version
echo.

echo Trying to run P2P Chat with JavaFX support...
echo.

REM 方法1: 尝试使用模块路径参数
echo [Method 1] Using JavaFX module path...
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar 2>nul
if %ERRORLEVEL% EQU 0 goto :success

REM 方法2: 尝试直接运行JAR
echo [Method 2] Direct JAR execution...
java -jar target/p2p-chat-1.0-SNAPSHOT.jar 2>nul
if %ERRORLEVEL% EQU 0 goto :success

REM 方法3: 检查是否有编译的类文件，运行命令行版本
echo [Method 3] Trying CLI version...
if exist "target\classes\com\group7\chat\Main.class" (
    echo Running command line version...
    java -cp target/classes com.group7.chat.Main
    if %ERRORLEVEL% EQU 0 goto :success
) else (
    echo Classes not found. Please run: mvn clean compile
)

REM 所有方法都失败了
echo.
echo ========================================
echo All methods failed!
echo ========================================
echo.
echo Possible solutions:
echo 1. Download JavaFX SDK from: https://openjfx.io/
echo 2. Install Java with JavaFX (Azul Zulu FX): https://www.azul.com/downloads/?package=jdk-fx
echo 3. Use the command: java --module-path "path\to\javafx\lib" --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar
echo.
echo For more help, see: JAVAFX_RUNTIME_SOLUTIONS.md
goto :end

:success
echo.
echo ========================================
echo Application started successfully!
echo ========================================

:end
echo.
echo Press any key to exit...
pause >nul
