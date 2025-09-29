#!/bin/bash

echo "========================================"
echo "P2P Chat CLI Test"
echo "========================================"
echo

echo "Current directory: $(pwd)"
echo

# 检查classes目录
if [ ! -f "../target/classes/com/group7/chat/Main.class" ]; then
    echo "ERROR: Main.class not found!"
    echo "Expected: ../target/classes/com/group7/chat/Main.class"
    echo
    echo "Please run: mvn clean compile"
    echo
    read -p "Press Enter to exit..."
    exit 1
fi

echo "Main.class found: ../target/classes/com/group7/chat/Main.class"
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

# 询问要启动多少个CLI节点
echo "How many CLI nodes do you want to start?"
echo "1. Single node (port 8080)"
echo "2. Two nodes (ports 8080, 8081)"
echo "3. Three nodes (ports 8080, 8081, 8082)"
echo
read -p "Enter choice (1-3): " choice

case $choice in
    1)
        echo
        echo "Starting single CLI node on port 8080..."
        echo
        echo "Command: java -cp ../target/classes com.group7.chat.Main 8080"
        echo
        gnome-terminal --title="P2P Chat CLI - Port 8080" -- java -cp ../target/classes com.group7.chat.Main 8080 2>/dev/null || \
        xterm -title "P2P Chat CLI - Port 8080" -e java -cp ../target/classes com.group7.chat.Main 8080 2>/dev/null || \
        java -cp ../target/classes com.group7.chat.Main 8080 &
        echo
        echo "CLI node started in new terminal!"
        ;;
    2)
        echo
        echo "Starting two CLI nodes..."
        echo
        echo "[1/2] Starting node on port 8080..."
        gnome-terminal --title="P2P Chat CLI - Port 8080" -- java -cp ../target/classes com.group7.chat.Main 8080 2>/dev/null || \
        xterm -title "P2P Chat CLI - Port 8080" -e java -cp ../target/classes com.group7.chat.Main 8080 2>/dev/null || \
        java -cp ../target/classes com.group7.chat.Main 8080 &
        sleep 2

        echo "[2/2] Starting node on port 8081 (auto-connects to 8080)..."
        gnome-terminal --title="P2P Chat CLI - Port 8081" -- java -cp ../target/classes com.group7.chat.Main 8081 localhost:8080 2>/dev/null || \
        xterm -title "P2P Chat CLI - Port 8081" -e java -cp ../target/classes com.group7.chat.Main 8081 localhost:8080 2>/dev/null || \
        java -cp ../target/classes com.group7.chat.Main 8081 localhost:8080 &
        echo
        echo "Two CLI nodes started!"
        echo "- Node 1: localhost:8080"
        echo "- Node 2: localhost:8081 (connected to Node 1)"
        ;;
    3)
        echo
        echo "Starting three CLI nodes..."
        echo
        echo "[1/3] Starting node on port 8080..."
        gnome-terminal --title="P2P Chat CLI - Port 8080" -- java -cp ../target/classes com.group7.chat.Main 8080 2>/dev/null || \
        xterm -title "P2P Chat CLI - Port 8080" -e java -cp ../target/classes com.group7.chat.Main 8080 2>/dev/null || \
        java -cp ../target/classes com.group7.chat.Main 8080 &
        sleep 2

        echo "[2/3] Starting node on port 8081 (auto-connects to 8080)..."
        gnome-terminal --title="P2P Chat CLI - Port 8081" -- java -cp ../target/classes com.group7.chat.Main 8081 localhost:8080 2>/dev/null || \
        xterm -title "P2P Chat CLI - Port 8081" -e java -cp ../target/classes com.group7.chat.Main 8081 localhost:8080 2>/dev/null || \
        java -cp ../target/classes com.group7.chat.Main 8081 localhost:8080 &
        sleep 2

        echo "[3/3] Starting node on port 8082 (auto-connects to 8080)..."
        gnome-terminal --title="P2P Chat CLI - Port 8082" -- java -cp ../target/classes com.group7.chat.Main 8082 localhost:8080 2>/dev/null || \
        xterm -title "P2P Chat CLI - Port 8082" -e java -cp ../target/classes com.group7.chat.Main 8082 localhost:8080 2>/dev/null || \
        java -cp ../target/classes com.group7.chat.Main 8082 localhost:8080 &
        echo
        echo "Three CLI nodes started!"
        echo "- Node 1: localhost:8080"
        echo "- Node 2: localhost:8081 (connected to Node 1)"
        echo "- Node 3: localhost:8082 (connected to Node 1)"
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
echo "Each node runs in a separate terminal window."
echo "In each window, you can use these commands:"
echo
echo "  send Hello!           - Send group message"
echo "  connect localhost:8080 - Connect to another node"
echo "  status                - Show connection status"
echo "  list                  - List connected peers"
echo "  quit                  - Exit the node"
echo
echo "Example test:"
echo "1. In Node 2 window, type: send Hello from Node 2!"
echo "2. Check if Node 1 and Node 3 receive the message"
echo "3. Try: status (to see connections)"
echo
