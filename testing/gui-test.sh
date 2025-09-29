#!/bin/bash

echo "========================================"
echo "P2P Chat GUI Test"
echo "========================================"
echo

echo "Current directory: $(pwd)"
echo

# 检查JAR文件
if [ ! -f "../target/p2p-chat-1.0-SNAPSHOT.jar" ]; then
    echo "ERROR: JAR file not found!"
    echo "Expected: ../target/p2p-chat-1.0-SNAPSHOT.jar"
    echo
    echo "Please run: mvn clean package"
    echo
    read -p "Press Enter to exit..."
    exit 1
fi

echo "JAR file found: ../target/p2p-chat-1.0-SNAPSHOT.jar"
echo

# 检查Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found!"
    echo "Please install Java"
    echo
    read -p "Press Enter to exit..."
    exit 1
fi

echo "Java is available."
echo

# 询问要启动多少个GUI节点
echo "How many GUI nodes do you want to start?"
echo "1. Single node (port 8080)"
echo "2. Two nodes (ports 8080, 8081)"
echo "3. Three nodes (ports 8080, 8081, 8082)"
echo
read -p "Enter choice (1-3): " choice

case $choice in
    1)
        echo
        echo "Starting single GUI node on port 8080..."
        echo
        echo "Command: java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080"
        echo
        gnome-terminal --title="P2P Chat GUI - Port 8080" -- java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080 2>/dev/null || \
        xterm -title "P2P Chat GUI - Port 8080" -e java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080 2>/dev/null || \
        java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080 &
        echo
        echo "GUI node started!"
        echo "If no window appears, JavaFX might not be available."
        ;;
    2)
        echo
        echo "Starting two GUI nodes..."
        echo
        echo "[1/2] Starting node on port 8080..."
        gnome-terminal --title="P2P Chat GUI - Port 8080" -- java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080 2>/dev/null || \
        xterm -title "P2P Chat GUI - Port 8080" -e java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080 2>/dev/null || \
        java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080 &
        sleep 3

        echo "[2/2] Starting node on port 8081..."
        gnome-terminal --title="P2P Chat GUI - Port 8081" -- java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8081 2>/dev/null || \
        xterm -title "P2P Chat GUI - Port 8081" -e java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8081 2>/dev/null || \
        java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8081 &
        echo
        echo "Two GUI nodes started!"
        echo "- Node 1: localhost:8080"
        echo "- Node 2: localhost:8081"
        ;;
    3)
        echo
        echo "Starting three GUI nodes..."
        echo
        echo "[1/3] Starting node on port 8080..."
        gnome-terminal --title="P2P Chat GUI - Port 8080" -- java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080 2>/dev/null || \
        xterm -title "P2P Chat GUI - Port 8080" -e java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080 2>/dev/null || \
        java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8080 &
        sleep 3

        echo "[2/3] Starting node on port 8081..."
        gnome-terminal --title="P2P Chat GUI - Port 8081" -- java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8081 2>/dev/null || \
        xterm -title "P2P Chat GUI - Port 8081" -e java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8081 2>/dev/null || \
        java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8081 &
        sleep 3

        echo "[3/3] Starting node on port 8082..."
        gnome-terminal --title="P2P Chat GUI - Port 8082" -- java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8082 2>/dev/null || \
        xterm -title "P2P Chat GUI - Port 8082" -e java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8082 2>/dev/null || \
        java -jar ../target/p2p-chat-1.0-SNAPSHOT.jar 8082 &
        echo
        echo "Three GUI nodes started!"
        echo "- Node 1: localhost:8080"
        echo "- Node 2: localhost:8081"
        echo "- Node 3: localhost:8082"
        ;;
    *)
        echo "Invalid choice."
        exit 1
        ;;
esac

echo
echo "========================================"
echo "Test Instructions:"
echo "========================================"
echo
echo "1. Wait for GUI windows to appear"
echo "2. In each window, you can:"
echo "   - Send group messages"
echo "   - Right-click users for private chat"
echo "   - Use file transfer"
echo "   - Connect to other nodes manually"
echo
echo "If GUI doesn't work, try: ./cli-test.sh"
echo
