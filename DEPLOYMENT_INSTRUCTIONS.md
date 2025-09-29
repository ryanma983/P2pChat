# P2P Chat 部署说明

## 🚀 最新更新 (2025-09-29)

已推送包含所有文件传输修复的最新代码到GitHub。

## 📋 部署步骤

### 1. 下载最新代码
```bash
git clone https://github.com/WebProjectTest114514/P2pChat.git
cd P2pChat
```

或者如果已有代码：
```bash
git pull origin main
```

### 2. 编译项目
```bash
mvn clean package -DskipTests
```

### 3. 验证文件存在
确保以下文件存在：
- `target/p2p-chat-1.0-SNAPSHOT.jar` (主要JAR文件)
- `src/main/java/com/group7/chat/FileTransferService.java` (包含修复)
- `src/main/java/com/group7/chat/AddressParsingTest.java` (测试工具)

### 4. 启动节点

**节点1 (端口8080):**
```bash
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar 8080
```

**节点2 (端口8081):**
```bash
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar 8081
```

## 🔍 验证修复成功

### 预期的调试输出

当您尝试发送文件时，应该看到详细的调试信息：

```
[文件传输] 开始处理文件发送请求
[文件传输] 目标节点ID: broadcast
[文件传输] 文件: filename.png (12345 bytes)
[文件传输] 保存路径: filename.png
[文件传输] 广播模式，当前连接数: 1
[文件传输] 广播模式，选择连接: localhost/127.0.0.1:9081
[文件传输] 连接的远程节点ID: e5ec6c83...
[文件传输] 原始地址: localhost/127.0.0.1:9081, 标准化地址: 127.0.0.1:9081
[文件传输] 解析结果 - 主机: 127.0.0.1, 基础端口: 9081, 文件传输端口: 10081
[文件传输] 准备连接到: 127.0.0.1:10081
[文件传输] 发送头信息: SEND:transfer_xxx:filename.png:12345:filename.png
[文件传输] 发送进度: 100% (12345/12345 bytes)
[文件传输] 文件发送完成: filename.png (12345 bytes)
```

### 如果仍然只看到旧格式

如果您仍然只看到：
```
[文件传输] 开始发送文件到 broadcast (localhost/127.0.0.1:9081)
```

这说明您使用的仍然是旧版本。请：

1. **确认下载了最新代码**
2. **重新编译项目**
3. **使用新生成的JAR文件**

## 🐛 故障排除

### 问题1：私聊文件没有落地
- 检查程序运行目录
- 查看是否有权限问题
- 确认接收方显示了完整的传输日志

### 问题2：群聊文件传输失败
- 确保两个节点都已连接
- 检查防火墙设置
- 观察详细的调试输出

### 问题3：仍然看不到详细日志
- 重新下载代码：`git clone https://github.com/WebProjectTest114514/P2pChat.git`
- 删除旧的target目录：`rm -rf target`
- 重新编译：`mvn clean package -DskipTests`

## 📞 支持

如果按照以上步骤操作后仍有问题，请提供：
1. 完整的启动日志
2. 文件传输尝试的完整日志
3. 使用的操作系统和Java版本

最新代码已包含所有修复，应该能解决文件传输问题。
