# 文件传输问题快速修复指南

## 🐛 问题症状

您遇到的问题：
```
[文件传输] 开始发送文件到 broadcast (localhost/127.0.0.1:9081)
发送文件 没有反应
```

## 🔧 问题原因

地址格式复杂化：`localhost/127.0.0.1:9081` 这种格式导致地址解析失败。

## ✅ 解决方案

### 方法1：使用最新修复版本

1. **下载最新代码**：
   ```bash
   git pull origin main
   mvn clean compile
   ```

2. **重新启动节点**：
   ```bash
   java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar 8080
   java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar 8081
   ```

### 方法2：手动修复（如果无法更新）

如果您无法更新代码，可以手动修改 `FileTransferService.java` 第188-194行：

```java
// 解析地址和端口
String normalizedAddress = targetAddress.replace("localhost", "127.0.0.1");

// 处理可能的复杂地址格式，如 "localhost/127.0.0.1:9081"
if (normalizedAddress.contains("/")) {
    // 取斜杠后面的部分
    normalizedAddress = normalizedAddress.substring(normalizedAddress.lastIndexOf("/") + 1);
}
```

## 🚀 验证修复

修复后，您应该看到类似这样的日志：
```
[文件传输] 广播模式，发送给: localhost/127.0.0.1:9081
[文件传输] 开始发送文件到 broadcast (127.0.0.1:9081)
[文件传输] 原始地址: localhost/127.0.0.1:9081, 标准化地址: 127.0.0.1:9081
[文件传输] 解析结果 - 主机: 127.0.0.1, 基础端口: 9081, 文件传输端口: 10081
[文件传输] 文件发送完成: filename.txt (XXX bytes)
```

## 📋 测试步骤

1. 启动两个节点
2. 确保节点连接成功
3. 尝试发送文件
4. 观察控制台输出，确认文件传输完成

修复已提交到GitHub，您可以直接拉取最新版本使用。
