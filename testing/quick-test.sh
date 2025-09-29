#!/bin/bash

echo "========================================"
echo "Quick P2P Test Setup"
echo "========================================"
echo

echo "Starting 3 nodes for testing..."
echo

echo "[1/3] Starting Node 1 (Port 8080)..."
gnome-terminal --title="Node 1 - Port 8080" -- java -cp ../target/classes com.group7.chat.Main 8080 2>/dev/null || \
xterm -title "Node 1 - Port 8080" -e java -cp ../target/classes com.group7.chat.Main 8080 2>/dev/null || \
java -cp ../target/classes com.group7.chat.Main 8080 &

sleep 2

echo "[2/3] Starting Node 2 (Port 8081)..."
gnome-terminal --title="Node 2 - Port 8081" -- java -cp ../target/classes com.group7.chat.Main 8081 localhost:8080 2>/dev/null || \
xterm -title "Node 2 - Port 8081" -e java -cp ../target/classes com.group7.chat.Main 8081 localhost:8080 2>/dev/null || \
java -cp ../target/classes com.group7.chat.Main 8081 localhost:8080 &

sleep 2

echo "[3/3] Starting Node 3 (Port 8082)..."
gnome-terminal --title="Node 3 - Port 8082" -- java -cp ../target/classes com.group7.chat.Main 8082 localhost:8080 2>/dev/null || \
xterm -title "Node 3 - Port 8082" -e java -cp ../target/classes com.group7.chat.Main 8082 localhost:8080 2>/dev/null || \
java -cp ../target/classes com.group7.chat.Main 8082 localhost:8080 &

echo
echo "========================================"
echo "All nodes started!"
echo "========================================"
echo
echo "You now have 3 P2P chat nodes running:"
echo "- Node 1: localhost:8080"
echo "- Node 2: localhost:8081 (connected to Node 1)"
echo "- Node 3: localhost:8082 (connected to Node 1)"
echo
echo "Test the chat functionality between the nodes!"
echo
