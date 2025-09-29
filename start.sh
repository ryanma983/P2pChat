#!/bin/bash
echo "Starting P2P Chat Application..."
echo

# Try GUI version first
echo "[1/3] Trying GUI version..."
if java --module-path . --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar 2>/dev/null; then
    echo "Application started successfully!"
    exit 0
fi

echo "[2/3] Trying simple JAR..."
if java -jar target/p2p-chat-1.0-SNAPSHOT.jar 2>/dev/null; then
    echo "Application started successfully!"
    exit 0
fi

echo "[3/3] Trying CLI version..."
if [ -f "target/classes/com/group7/chat/Main.class" ]; then
    if java -cp target/classes com.group7.chat.Main; then
        echo "Application started successfully!"
        exit 0
    fi
fi

echo
echo "========================================"
echo "Unable to start application!"
echo "========================================"
echo
echo "Solutions:"
echo "1. Install Java with JavaFX: https://www.azul.com/downloads/?package=jdk-fx"
echo "2. See documentation/INSTALL_JAVAFX.md for detailed help"
echo "3. Use scripts/start-cli.sh for command line version"
echo

exit 1
