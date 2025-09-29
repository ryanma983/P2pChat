@echo off
echo Starting P2P Chat (Simple Mode)...
echo.
echo Trying direct JAR execution...
java -jar target\p2p-chat-1.0-SNAPSHOT.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo GUI version failed. Trying CLI version...
    java -cp target\classes com.group7.chat.Main
)
pause
