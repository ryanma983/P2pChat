@echo off
echo ========================================
echo P2P Chat 更新和测试脚本
echo ========================================

echo 1. 更新代码...
git pull origin main
if %errorlevel% neq 0 (
    echo 更新失败，请检查网络连接
    pause
    exit /b 1
)

echo 2. 清理并重新编译...
mvn clean compile
if %errorlevel% neq 0 (
    echo 编译失败，请检查代码
    pause
    exit /b 1
)

echo 3. 检查关键文件是否存在...
if exist "src\main\java\com\group7\chat\FileTransferService.java" (
    echo ✓ FileTransferService.java 存在
) else (
    echo ✗ FileTransferService.java 不存在
)

if exist "src\main\java\com\group7\chat\AddressParsingTest.java" (
    echo ✓ AddressParsingTest.java 存在
) else (
    echo ✗ AddressParsingTest.java 不存在
)

echo 4. 运行地址解析测试...
java -cp target\classes com.group7.chat.AddressParsingTest

echo ========================================
echo 更新完成！现在可以测试文件传输功能
echo 
echo 启动命令：
echo 节点1: java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar 8080
echo 节点2: java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar 8081
echo ========================================
pause
