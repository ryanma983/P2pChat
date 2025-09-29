#!/bin/bash

echo "========================================"
echo "P2P Chat Multi-Node Test Launcher"
echo "========================================"
echo

echo "This script will help you start multiple P2P chat instances"
echo "for testing the peer-to-peer functionality."
echo

echo "Available options:"
echo "1. Start Node 1 (Port 8080) - CLI"
echo "2. Start Node 2 (Port 8081) - CLI"  
echo "3. Start Node 3 (Port 8082) - CLI"
echo "4. Start Node 1 (Port 8080) - GUI"
echo "5. Start Node 2 (Port 8081) - GUI"
echo "6. Start Node 3 (Port 8082) - GUI"
echo "7. Custom port - CLI"
echo "8. Custom port - GUI"
echo "9. Exit"
echo

read -p "Enter your choice (1-9): " choice

case $choice in
    1)
        echo "Starting Node 1 on port 8080 (CLI)..."
        gnome-terminal --title="P2P Chat Node 1 (8080)" -- java -cp target/classes com.group7.chat.Main 8080 2>/dev/null || \
        xterm -title "P2P Chat Node 1 (8080)" -e java -cp target/classes com.group7.chat.Main 8080 2>/dev/null || \
        java -cp target/classes com.group7.chat.Main 8080 &
        ;;
    2)
        echo "Starting Node 2 on port 8081 (CLI)..."
        gnome-terminal --title="P2P Chat Node 2 (8081)" -- java -cp target/classes com.group7.chat.Main 8081 localhost:8080 2>/dev/null || \
        xterm -title "P2P Chat Node 2 (8081)" -e java -cp target/classes com.group7.chat.Main 8081 localhost:8080 2>/dev/null || \
        java -cp target/classes com.group7.chat.Main 8081 localhost:8080 &
        ;;
    3)
        echo "Starting Node 3 on port 8082 (CLI)..."
        gnome-terminal --title="P2P Chat Node 3 (8082)" -- java -cp target/classes com.group7.chat.Main 8082 localhost:8080 2>/dev/null || \
        xterm -title "P2P Chat Node 3 (8082)" -e java -cp target/classes com.group7.chat.Main 8082 localhost:8080 2>/dev/null || \
        java -cp target/classes com.group7.chat.Main 8082 localhost:8080 &
        ;;
    4)
        echo "Starting Node 1 on port 8080 (GUI)..."
        java --module-path . --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar 8080 &
        ;;
    5)
        echo "Starting Node 2 on port 8081 (GUI)..."
        java --module-path . --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar 8081 &
        ;;
    6)
        echo "Starting Node 3 on port 8082 (GUI)..."
        java --module-path . --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar 8082 &
        ;;
    7)
        read -p "Enter port number: " port
        echo "Starting custom node on port $port (CLI)..."
        gnome-terminal --title="P2P Chat Node ($port)" -- java -cp target/classes com.group7.chat.Main $port 2>/dev/null || \
        xterm -title "P2P Chat Node ($port)" -e java -cp target/classes com.group7.chat.Main $port 2>/dev/null || \
        java -cp target/classes com.group7.chat.Main $port &
        ;;
    8)
        read -p "Enter port number: " port
        echo "Starting custom node on port $port (GUI)..."
        java --module-path . --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar $port &
        ;;
    9)
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo "Invalid choice. Please try again."
        exit 1
        ;;
esac

echo
echo "Node started in background/new terminal."
echo
echo "Testing Tips:"
echo "1. Start Node 1 first (port 8080)"
echo "2. Then start Node 2 (port 8081) - it will auto-connect to Node 1"
echo "3. Use 'connect localhost:8080' in CLI to manually connect nodes"
echo "4. Use the GUI connection feature to connect nodes"
echo
