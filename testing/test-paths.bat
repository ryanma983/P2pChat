@echo off
echo ========================================
echo Testing File Paths
echo ========================================
echo.

echo Current directory: %CD%
echo.

echo [1/4] Checking JAR file...
if exist "..\target\p2p-chat-1.0-SNAPSHOT.jar" (
    echo ✅ JAR file found: ..\target\p2p-chat-1.0-SNAPSHOT.jar
    dir "..\target\p2p-chat-1.0-SNAPSHOT.jar"
) else (
    echo ❌ JAR file NOT found at: ..\target\p2p-chat-1.0-SNAPSHOT.jar
)
echo.

echo [2/4] Checking classes directory...
if exist "..\target\classes" (
    echo ✅ Classes directory found: ..\target\classes
    dir "..\target\classes\com\group7\chat\Main.class" 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo ✅ Main.class found
    ) else (
        echo ❌ Main.class NOT found
    )
) else (
    echo ❌ Classes directory NOT found at: ..\target\classes
)
echo.

echo [3/4] Testing Java classpath...
echo Trying: java -cp ..\target\classes com.group7.chat.Main --help
java -cp ..\target\classes com.group7.chat.Main --help 2>&1
echo Return code: %ERRORLEVEL%
echo.

echo [4/4] Testing JAR execution...
echo Trying: java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar --help
java -jar ..\target\p2p-chat-1.0-SNAPSHOT.jar --help 2>&1
echo Return code: %ERRORLEVEL%
echo.

echo ========================================
echo Path Test Complete
echo ========================================
echo.
pause
