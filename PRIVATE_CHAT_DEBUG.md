# 私聊消息问题修复和调试指南

## 🐛 问题描述

用户报告两个问题：
1. **显示问题**：私聊界面显示"与(一个不规则的字符串)开始私聊"
2. **功能问题**：8081发送私聊到8080，8080没有接收到

## ✅ 已修复的显示问题

### 1. 私聊窗口标题
**修复前**：`"私聊 - " + targetMember.getNodeId()`
**修复后**：`"私聊 - " + targetMember.getDisplayName()`

### 2. 欢迎消息
**修复前**：`"开始与 " + targetMember.getNodeId() + " 的私聊"`
**修复后**：`"开始与 " + targetMember.getDisplayName() + " 的私聊"`

### 3. 系统消息
**修复前**：`"已打开与 " + nodeId + " 的私聊窗口"`
**修复后**：`"已打开与 " + member.getDisplayName() + " 的私聊窗口"`

现在私聊界面应该显示：**"开始与 Node_8080 的私聊"**

## 🔍 添加的调试功能

为了诊断私聊消息无法接收的问题，我添加了详细的调试日志：

### 1. 发送端日志
```java
// 在 Node.sendPrivateMessage() 中
System.out.println("发送私聊消息: " + getDisplayName() + " -> " + targetNodeId.substring(0, 8) + "...: " + message);
```

### 2. 接收端日志
```java
// 在 MessageRouter.processLocalAppMessage() 中
System.out.println("处理本地私聊消息: " + message.getSenderId().substring(0, 8) + "... -> " + node.getDisplayName() + ": " + message.getContent());
```

### 3. 转发过程日志
```java
// 在 MessageRouter.forwardMessage() 中
System.out.println("转发私聊消息，目标: " + targetNodeId.substring(0, 8) + "..., 当前连接数: " + node.getConnections().size());
System.out.println("检查连接: " + connection.getAddress() + ", 远程ID: " + (remoteId != null ? remoteId.substring(0, 8) + "..." : "null"));
System.out.println("找到直接连接，发送私聊消息到: " + connection.getAddress());
```

## 🎯 测试和调试步骤

### 1. 启动测试
```cmd
testing\gui-test.bat
# 选择 "2. Two nodes"
```

### 2. 观察控制台输出
启动后，控制台应该显示：
- 节点启动信息
- 连接建立过程
- HELLO消息交换

### 3. 测试私聊
1. 在8081节点右键点击 "Node_8080"
2. 选择"私聊"
3. 发送消息 "Hello"

### 4. 预期的调试输出
**发送端 (8081)**：
```
发送私聊消息: Node_8081 -> 12345678...: Hello
转发私聊消息，目标: 12345678..., 当前连接数: 1
检查连接: localhost:8080, 远程ID: 12345678...
找到直接连接，发送私聊消息到: localhost:8080
```

**接收端 (8080)**：
```
处理本地私聊消息: 87654321... -> Node_8080: Hello
```

## 🔧 可能的问题和解决方案

### 问题1：连接的远程节点ID为null
**症状**：调试输出显示 "远程ID: null"
**原因**：HELLO消息处理时没有正确设置 `connection.setRemoteNodeId()`
**解决**：检查 `MessageRouter.handleHelloMessage()` 方法

### 问题2：没有找到直接连接
**症状**：调试输出显示 "当前连接数: 0" 或找不到匹配的连接
**原因**：连接建立失败或节点ID不匹配
**解决**：检查连接状态和节点ID匹配逻辑

### 问题3：消息发送但未接收
**症状**：发送端有日志，接收端没有日志
**原因**：网络传输问题或消息序列化问题
**解决**：检查网络连接和消息格式

## 📝 下一步调试

如果问题仍然存在，请：

1. **运行测试并查看控制台输出**
2. **记录完整的调试日志**
3. **检查是否有错误信息**
4. **确认连接状态和节点ID匹配**

这些调试信息将帮助我们快速定位问题的根本原因。
