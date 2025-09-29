@echo off
echo Starting P2P Chat...
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar
pause
