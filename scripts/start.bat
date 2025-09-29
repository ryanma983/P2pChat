@echo off
echo Starting P2P Chat...
echo.
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo JavaFX module not found!
    echo ========================================
    echo.
    echo Please see INSTALL_JAVAFX.md for solutions:
    echo 1. Install Azul Zulu JDK FX
    echo 2. Download JavaFX SDK
    echo 3. Or use CLI version: start-cli.bat
    echo.
)
pause
