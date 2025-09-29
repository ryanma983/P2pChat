# P2P聊天应用安全功能使用指南

## 🔐 安全功能概述

本P2P聊天应用实现了企业级的安全通信机制，提供端到端加密、身份验证、数字签名等完整的安全保护。

### 🎯 安全特性

- **端到端加密**：使用AES-256加密所有消息内容
- **RSA密钥交换**：2048位RSA密钥确保密钥交换安全
- **数字签名**：防止消息篡改和伪造
- **身份验证**：基于公钥的节点身份验证
- **安全文件传输**：文件传输过程全程加密
- **密钥管理**：自动化的密钥生成、存储和轮换

## 🚀 快速开始

### 启用安全功能

安全功能在节点启动时自动初始化：

```java
Node node = new Node(8080);
node.start();

// 检查安全状态
if (node.isSecurityEnabled()) {
    System.out.println("安全功能已启用");
    System.out.println(node.getSecurityManager().getSecurityStatus());
}
```

### 基本安全设置

```java
SecurityManager securityManager = node.getSecurityManager();

// 启用严格模式（只允许加密通信）
securityManager.setStrictMode(true);

// 查看安全状态
String status = securityManager.getSecurityStatus();
System.out.println(status);
```

## 🔑 密钥管理

### 自动密钥生成

系统启动时自动生成节点密钥对：

```
=== 安全管理器初始化 ===
[密钥管理] 生成新的节点密钥对
[密钥管理] 密钥对已保存到: keys/
[密钥管理] 公钥指纹: ABC123...
```

### 密钥交换

与新节点建立连接时自动进行密钥交换：

```java
// 手动触发密钥交换
boolean success = securityManager.handleKeyExchange("target-node-id");
if (success) {
    System.out.println("密钥交换成功");
}
```

### 密钥状态查看

```java
KeyManager keyManager = securityManager.getKeyManager();

// 检查会话密钥
boolean hasSessionKey = keyManager.hasSessionKey("node-id");

// 检查公钥
boolean hasPublicKey = keyManager.hasPublicKey("node-id");

// 获取统计信息
int sessionKeyCount = keyManager.getSessionKeyCount();
int publicKeyCount = keyManager.getPublicKeyCount();
```

## 💬 安全消息传输

### 自动加密

发送消息时系统自动判断是否加密：

```java
// 发送普通消息（如果有密钥会自动加密）
Message message = new Message(Message.Type.CHAT, nodeId, "Hello World!");
String processedMessage = securityManager.processOutgoingMessage(message, targetNodeId);

// 接收消息时自动解密
Message receivedMessage = securityManager.processIncomingMessage(rawMessage, senderNodeId);
```

### 强制加密模式

```java
// 启用严格模式，只允许加密通信
securityManager.setStrictMode(true);

// 此时所有消息都必须加密，否则拒绝发送/接收
```

### 消息完整性验证

所有消息都包含数字签名，自动验证：

```
[安全管理器] 消息完整性验证通过: node-123
[安全管理器] 消息签名验证成功: node-456
```

## 📁 安全文件传输

### 发送加密文件

```java
SecurityManager securityManager = node.getSecurityManager();

// 发送安全文件
SecureFileTransferService.TransferResult result = 
    securityManager.sendSecureFile("target-node-id", "/path/to/file.txt", "/save/path/file.txt");

if (result.isSuccess()) {
    System.out.println("文件传输成功: " + result.getBytesTransferred() + " bytes");
} else {
    System.err.println("文件传输失败: " + result.getErrorMessage());
}
```

### 接收加密文件

文件接收过程自动处理：

```
[安全文件传输] 收到文件传输请求: document.pdf (1.2MB)
[安全文件传输] 开始解密文件...
[安全文件传输] 文件解密完成，保存到: /downloads/document.pdf
[安全文件传输] 文件完整性验证通过
```

## 🛡️ 身份验证

### 节点身份验证

```java
AuthenticationService authService = securityManager.getAuthenticationService();

// 验证节点身份
AuthenticationService.AuthenticationResult result = 
    securityManager.authenticateNode("node-id", "public-key-string");

switch (result) {
    case SUCCESS:
        System.out.println("节点身份验证成功");
        break;
    case FAILED:
        System.out.println("节点身份验证失败");
        break;
    case PENDING:
        System.out.println("身份验证进行中...");
        break;
}
```

### 信任级别管理

```java
// 查看节点信任级别
int trustLevel = authService.getTrustLevel("node-id");
System.out.println("信任级别: " + trustLevel + "/100");

// 更新信任级别
authService.updateTrustLevel("node-id", 85);
```

### 身份证书

```java
// 导出节点证书
String certificate = securityManager.exportNodeCertificate();
System.out.println("节点证书: " + certificate);

// 导入并验证证书
boolean valid = securityManager.importNodeCertificate(certificate);
if (valid) {
    System.out.println("证书验证成功");
}
```

## 📊 安全监控

### 实时安全状态

```java
// 获取完整安全状态
String securityStatus = securityManager.getSecurityStatus();
System.out.println(securityStatus);
```

输出示例：
```
=== 安全状态摘要 ===
安全功能: 启用
严格模式: 启用
节点ID: Node-1234567890
会话密钥数量: 3
公钥数量: 5
节点统计: 总计=5, 已验证=3, 可信=2, 活跃挑战=1
活跃文件传输: 0
```

### 加密状态检查

```java
SecureMessageHandler messageHandler = securityManager.getSecureMessageHandler();

// 检查与特定节点的加密状态
String encryptionStatus = messageHandler.getEncryptionStatus("node-id");
System.out.println("加密状态: " + encryptionStatus);
// 输出: "完全加密" / "需要密钥交换" / "未加密"
```

## ⚙️ 高级配置

### 安全参数调整

```java
// 禁用安全功能（仅用于测试）
securityManager.setSecurityEnabled(false);

// 重新启用安全功能
securityManager.setSecurityEnabled(true);

// 检查安全功能状态
boolean isEnabled = securityManager.isSecurityEnabled();
```

### 密钥轮换

```java
KeyManager keyManager = securityManager.getKeyManager();

// 移除过期的会话密钥
keyManager.removeSessionKey("old-node-id");

// 重新生成会话密钥
SecretKey newSessionKey = keyManager.generateSessionKey();
keyManager.storeSessionKey("node-id", newSessionKey);
```

### 清理和维护

系统自动执行定期维护任务：

- **每5分钟**：清理过期的身份验证挑战
- **每30分钟**：输出安全统计信息
- **启动时**：验证密钥文件完整性

## 🔧 故障排除

### 常见问题

**1. 密钥交换失败**
```
[安全管理器] 密钥交换失败: target-node-id
```
解决方案：
- 检查目标节点是否在线
- 确认目标节点的公钥是否正确
- 重新尝试连接

**2. 消息解密失败**
```
[安全管理器] 处理传入消息失败: 解密错误
```
解决方案：
- 检查会话密钥是否存在
- 验证消息格式是否正确
- 重新进行密钥交换

**3. 文件传输加密失败**
```
[安全文件传输] 文件加密失败: 缺少会话密钥
```
解决方案：
- 确保与目标节点已建立会话密钥
- 手动触发密钥交换
- 检查文件权限

### 调试模式

启用详细的安全日志：

```java
// 在启动时添加JVM参数
-Djava.util.logging.level=FINE
```

这将输出详细的安全操作日志，帮助诊断问题。

## 🛡️ 安全最佳实践

### 1. 密钥管理
- 定期备份密钥文件
- 不要在不安全的网络上传输私钥
- 定期轮换会话密钥

### 2. 网络安全
- 在生产环境中始终启用严格模式
- 定期验证节点身份
- 监控异常的连接行为

### 3. 文件传输
- 大文件传输前先验证目标节点身份
- 传输敏感文件时使用额外的访问控制
- 定期清理临时文件

### 4. 监控和审计
- 定期检查安全状态
- 记录所有安全相关事件
- 监控信任级别变化

## 📚 API参考

### SecurityManager主要方法

| 方法 | 描述 |
|------|------|
| `isSecurityEnabled()` | 检查安全功能是否启用 |
| `setStrictMode(boolean)` | 设置严格模式 |
| `getSecurityStatus()` | 获取安全状态摘要 |
| `handleKeyExchange(String)` | 处理密钥交换 |
| `sendSecureFile(...)` | 发送安全文件 |
| `exportNodeCertificate()` | 导出节点证书 |

### KeyManager主要方法

| 方法 | 描述 |
|------|------|
| `hasSessionKey(String)` | 检查会话密钥 |
| `hasPublicKey(String)` | 检查公钥 |
| `getSessionKeyCount()` | 获取会话密钥数量 |
| `getPublicKeyCount()` | 获取公钥数量 |

### AuthenticationService主要方法

| 方法 | 描述 |
|------|------|
| `authenticateNode(...)` | 验证节点身份 |
| `getTrustLevel(String)` | 获取信任级别 |
| `updateTrustLevel(...)` | 更新信任级别 |
| `getAuthenticationStats()` | 获取验证统计 |

---

## 📞 技术支持

如果您在使用安全功能时遇到问题，请：

1. 查看控制台输出的安全日志
2. 检查密钥文件是否完整
3. 验证网络连接状态
4. 参考本指南的故障排除部分

安全功能为您的P2P聊天提供了企业级的保护，确保通信内容的机密性、完整性和可用性。
