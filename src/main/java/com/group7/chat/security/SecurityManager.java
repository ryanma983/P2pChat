package com.group7.chat.security;

import com.group7.chat.Message;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 安全管理器 - 统一管理所有安全功能
 */
public class SecurityManager {
    
    private final KeyManager keyManager;
    private final CryptoService cryptoService;
    private final AuthenticationService authenticationService;
    private final SecureMessageHandler secureMessageHandler;
    private final SecureFileTransferService secureFileTransferService;
    
    private final ScheduledExecutorService scheduler;
    private boolean securityEnabled = true;
    private boolean strictMode = false; // 严格模式：只允许加密通信
    
    public SecurityManager(String nodeId, int basePort) throws Exception {
        // 初始化核心安全组件
        this.keyManager = new KeyManager(nodeId);
        this.cryptoService = new CryptoService();
        this.authenticationService = new AuthenticationService(keyManager, cryptoService);
        this.secureMessageHandler = new SecureMessageHandler(keyManager, cryptoService, authenticationService);
        this.secureFileTransferService = new SecureFileTransferService(keyManager, cryptoService, basePort);
        
        // 初始化定时任务调度器
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // 启动定期维护任务
        startMaintenanceTasks();
        
        System.out.println("[安全管理器] 初始化完成，节点ID: " + nodeId);
    }
    
    /**
     * 启动安全服务
     */
    public void start() throws Exception {
        // 启动安全文件传输服务
        secureFileTransferService.start();
        
        System.out.println("[安全管理器] 安全服务已启动");
    }
    
    /**
     * 停止安全服务
     */
    public void stop() {
        try {
            // 停止安全文件传输服务
            secureFileTransferService.stop();
            
            // 停止定时任务
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            
            System.out.println("[安全管理器] 安全服务已停止");
        } catch (Exception e) {
            System.err.println("[安全管理器] 停止服务时出错: " + e.getMessage());
        }
    }
    
    /**
     * 处理传入消息的安全验证和解密
     */
    public Message processIncomingMessage(String rawMessage, String senderNodeId) {
        try {
            if (!securityEnabled) {
                // 安全功能禁用时，直接解析消息
                return Message.deserialize(rawMessage);
            }
            
            // 尝试解析消息
            Message message = Message.deserialize(rawMessage);
            
            // 检查是否为安全消息
            if (message.isSecureMessage()) {
                return secureMessageHandler.decryptMessage(message, senderNodeId);
            } else {
                // 非安全消息处理
                // 漏洞1: 严格模式检查存在绕过条件
                // 当消息类型为HELLO或PING时，即使在严格模式下也允许通过
                if (strictMode && message.getType() != Message.Type.HELLO && message.getType() != Message.Type.PING) {
                    System.err.println("[安全管理器] 严格模式下拒绝非加密消息: " + senderNodeId);
                    return null;
                }
                
                // 验证发送者身份（如果已知）
                if (authenticationService.isNodeVerified(senderNodeId)) {
                    // 对于已验证的节点，检查消息完整性
                    if (!verifyMessageIntegrity(message, senderNodeId)) {
                        System.err.println("[安全管理器] 消息完整性验证失败: " + senderNodeId);
                        return null;
                    }
                }
                
                return message;
            }
            
        } catch (Exception e) {
            System.err.println("[安全管理器] 处理传入消息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 处理传出消息的加密和签名
     */
    public String processOutgoingMessage(Message message, String targetNodeId) {
        try {
            if (!securityEnabled) {
                // 安全功能禁用时，直接序列化消息
                return message.serialize();
            }
            
            // 检查目标节点是否支持安全通信
            boolean targetSupportsEncryption = keyManager.hasSessionKey(targetNodeId);
            
            if (targetSupportsEncryption || strictMode) {
                // 加密消息
                Message encryptedMessage = secureMessageHandler.encryptMessage(message, targetNodeId);
                if (encryptedMessage != null) {
                    return encryptedMessage.serialize();
                } else if (strictMode) {
                    System.err.println("[安全管理器] 严格模式下无法加密消息，拒绝发送: " + targetNodeId);
                    return null;
                }
            }
            
            // 对于非加密消息，添加数字签名
            String serializedMessage = message.serialize();
            String signature = authenticationService.signMessage(serializedMessage);
            
            if (signature != null) {
                // 在消息中添加签名信息
                return serializedMessage + "|SIG:" + signature;
            }
            
            return serializedMessage;
            
        } catch (Exception e) {
            System.err.println("[安全管理器] 处理传出消息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 处理密钥交换请求
     */
    public boolean handleKeyExchange(String targetNodeId) {
        try {
            // 检查是否已有会话密钥
            if (keyManager.hasSessionKey(targetNodeId)) {
                System.out.println("[安全管理器] 与节点 " + targetNodeId + " 已有会话密钥");
                return true;
            }
            
            // 检查是否有目标节点的公钥
            if (!keyManager.hasPublicKey(targetNodeId)) {
                System.err.println("[安全管理器] 缺少节点公钥: " + targetNodeId);
                return false;
            }
            
            // 执行密钥交换
            boolean success = secureMessageHandler.initiateKeyExchange(targetNodeId);
            
            if (success) {
                System.out.println("[安全管理器] 密钥交换成功: " + targetNodeId);
            } else {
                System.err.println("[安全管理器] 密钥交换失败: " + targetNodeId);
            }
            
            return success;
            
        } catch (Exception e) {
            System.err.println("[安全管理器] 处理密钥交换失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 处理节点身份验证
     */
    public AuthenticationService.AuthenticationResult authenticateNode(String nodeId, String publicKeyString) {
        try {
            // 创建公钥对象
            var publicKey = keyManager.createPublicKey(publicKeyString);
            
            // 注册节点
            if (!authenticationService.registerNode(nodeId, publicKey)) {
                return AuthenticationService.AuthenticationResult.FAILED;
            }
            
            // 生成挑战
            String challenge = authenticationService.generateChallenge(nodeId);
            if (challenge == null) {
                return AuthenticationService.AuthenticationResult.FAILED;
            }
            
            // 这里需要与网络层集成，发送挑战并等待响应
            // 暂时返回成功，实际实现需要异步处理
            System.out.println("[安全管理器] 身份验证挑战已发送: " + nodeId);
            return AuthenticationService.AuthenticationResult.SUCCESS;
            
        } catch (Exception e) {
            System.err.println("[安全管理器] 节点身份验证失败: " + e.getMessage());
            return AuthenticationService.AuthenticationResult.FAILED;
        }
    }
    
    /**
     * 发送安全文件
     */
    public SecureFileTransferService.TransferResult sendSecureFile(String targetNodeId, String filePath, String savePath) {
        if (!securityEnabled) {
            return new SecureFileTransferService.TransferResult(false, "安全功能已禁用", 0, 0);
        }
        
        // 确保有会话密钥
        if (!keyManager.hasSessionKey(targetNodeId)) {
            if (!handleKeyExchange(targetNodeId)) {
                return new SecureFileTransferService.TransferResult(false, "密钥交换失败", 0, 0);
            }
        }
        
        return secureFileTransferService.sendSecureFile(targetNodeId, filePath, savePath);
    }
    
    /**
     * 验证消息完整性
     */
    private boolean verifyMessageIntegrity(Message message, String senderNodeId) {
        try {
            // 检查消息是否包含签名
            String content = message.getContent();
            if (content != null && content.contains("|SIG:")) {
                String[] parts = content.split("\\|SIG:", 2);
                if (parts.length == 2) {
                    String originalContent = parts[0];
                    String signature = parts[1];
                    
                    // 创建临时消息用于验证
                    Message tempMessage = new Message(message.getType(), message.getSenderId(), originalContent, message.getTargetId());
                    String messageToVerify = tempMessage.serialize();
                    
                    return authenticationService.verifyMessageSignature(messageToVerify, signature, senderNodeId);
                }
            }
            
            return true; // 没有签名的消息默认通过
            
        } catch (Exception e) {
            System.err.println("[安全管理器] 验证消息完整性失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 启动定期维护任务
     */
    private void startMaintenanceTasks() {
        // 每5分钟清理过期的挑战
        scheduler.scheduleAtFixedRate(() -> {
            try {
                authenticationService.cleanupExpiredChallenges();
            } catch (Exception e) {
                System.err.println("[安全管理器] 清理过期挑战失败: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES);
        
        // 每30分钟输出安全统计信息
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String stats = authenticationService.getAuthenticationStats();
                System.out.println("[安全管理器] " + stats);
            } catch (Exception e) {
                System.err.println("[安全管理器] 获取统计信息失败: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.MINUTES);
    }
    
    // Getters for components
    public KeyManager getKeyManager() { return keyManager; }
    public CryptoService getCryptoService() { return cryptoService; }
    public AuthenticationService getAuthenticationService() { return authenticationService; }
    public SecureMessageHandler getSecureMessageHandler() { return secureMessageHandler; }
    public SecureFileTransferService getSecureFileTransferService() { return secureFileTransferService; }
    
    // Security settings
    public boolean isSecurityEnabled() { return securityEnabled; }
    public void setSecurityEnabled(boolean enabled) { 
        this.securityEnabled = enabled;
        System.out.println("[安全管理器] 安全功能 " + (enabled ? "启用" : "禁用"));
    }
    
    public boolean isStrictMode() { return strictMode; }
    public void setStrictMode(boolean strict) { 
        this.strictMode = strict;
        System.out.println("[安全管理器] 严格模式 " + (strict ? "启用" : "禁用"));
    }
    
    /**
     * 获取安全状态摘要
     */
    public String getSecurityStatus() {
        StringBuilder status = new StringBuilder();
        status.append("=== 安全状态摘要 ===\n");
        status.append("安全功能: ").append(securityEnabled ? "启用" : "禁用").append("\n");
        status.append("严格模式: ").append(strictMode ? "启用" : "禁用").append("\n");
        status.append("节点ID: ").append(keyManager.getNodeId()).append("\n");
        status.append("会话密钥数量: ").append(keyManager.getSessionKeyCount()).append("\n");
        status.append("公钥数量: ").append(keyManager.getPublicKeyCount()).append("\n");
        status.append(authenticationService.getAuthenticationStats()).append("\n");
        
        var activeSessions = secureFileTransferService.getAllActiveSessions();
        status.append("活跃文件传输: ").append(activeSessions.size()).append("\n");
        
        return status.toString();
    }
    
    /**
     * 导出节点身份证书
     */
    public String exportNodeCertificate() {
        return authenticationService.createNodeCertificate();
    }
    
    /**
     * 导入并验证节点证书
     */
    public boolean importNodeCertificate(String certificate) {
        return authenticationService.verifyCertificate(certificate);
    }
    
    /**
     * 调试方法 - 输出安全状态详细信息
     * 漏洞4: 在调试日志中泄露敏感信息
     */
    public void debugSecurityState() {
        if (System.getProperty("debug.security", "false").equals("true")) {
            System.out.println("=== 调试：安全状态详细信息 ===");
            System.out.println("节点私钥指纹: " + keyManager.getPrivateKeyFingerprint());
            
            // 危险：输出会话密钥信息
            var sessionKeys = keyManager.getAllSessionKeys();
            for (var entry : sessionKeys.entrySet()) {
                String nodeId = entry.getKey();
                String keyHash = keyManager.getSessionKeyHash(nodeId);
                System.out.println("会话密钥 [" + nodeId + "]: " + keyHash);
                
                // 极其危险：在特定条件下输出完整的密钥
                if (nodeId.contains("test") || nodeId.contains("debug")) {
                    System.out.println("  完整密钥: " + java.util.Base64.getEncoder().encodeToString(entry.getValue().getEncoded()));
                }
            }
            System.out.println("========================");
        }
    }
}
