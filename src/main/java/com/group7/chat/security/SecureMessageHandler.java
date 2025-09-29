package com.group7.chat.security;

import com.group7.chat.Message;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 安全消息处理器 - 负责消息的加密和解密
 */
public class SecureMessageHandler {
    
    private final KeyManager keyManager;
    private final CryptoService cryptoService;
    private final AuthenticationService authenticationService;
    
    public SecureMessageHandler(KeyManager keyManager, CryptoService cryptoService, AuthenticationService authenticationService) {
        this.keyManager = keyManager;
        this.cryptoService = cryptoService;
        this.authenticationService = authenticationService;
    }
    
    /**
     * 安全消息包装类
     */
    public static class SecureMessage {
        private final String senderId;
        private final String encryptedContent;
        private final String signature;
        private final long timestamp;
        private final String messageId;
        
        public SecureMessage(String senderId, String encryptedContent, String signature, long timestamp, String messageId) {
            this.senderId = senderId;
            this.encryptedContent = encryptedContent;
            this.signature = signature;
            this.timestamp = timestamp;
            this.messageId = messageId;
        }
        
        public String getSenderId() { return senderId; }
        public String getEncryptedContent() { return encryptedContent; }
        public String getSignature() { return signature; }
        public long getTimestamp() { return timestamp; }
        public String getMessageId() { return messageId; }
        
        /**
         * 转换为传输格式
         */
        public String toTransportFormat() {
            return String.join(":", 
                senderId, 
                encryptedContent, 
                signature, 
                String.valueOf(timestamp),
                messageId
            );
        }
        
        /**
         * 从传输格式创建SecureMessage
         */
        public static SecureMessage fromTransportFormat(String transportData) {
            String[] parts = transportData.split(":", 5);
            if (parts.length != 5) {
                throw new IllegalArgumentException("无效的安全消息格式");
            }
            
            return new SecureMessage(
                parts[0],  // senderId
                parts[1],  // encryptedContent
                parts[2],  // signature
                Long.parseLong(parts[3]), // timestamp
                parts[4]   // messageId
            );
        }
    }
    

    
    /**
     * 加密消息
     */
    public SecureMessage encryptMessage(String content, String targetNodeId) throws Exception {
        // 获取会话密钥
        SecretKey sessionKey = keyManager.getSessionKey(targetNodeId);
        if (sessionKey == null) {
            throw new IllegalStateException("未找到与节点 " + targetNodeId + " 的会话密钥");
        }
        
        // 创建消息元数据
        long timestamp = System.currentTimeMillis();
        String messageId = generateMessageId();
        String senderId = keyManager.getNodeId();
        
        // 构造完整消息内容（包含元数据）
        String fullContent = createFullMessageContent(content, timestamp, messageId);
        
        // 使用AES加密消息内容
        CryptoService.EncryptionResult encryptionResult = cryptoService.encryptWithAES(fullContent, sessionKey);
        String encryptedContent = encryptionResult.toBase64String();
        
        // 创建数字签名
        byte[] contentBytes = fullContent.getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = cryptoService.sign(contentBytes, keyManager.getNodePrivateKey());
        String signature = Base64.getEncoder().encodeToString(signatureBytes);
        
        System.out.println("[安全消息] 加密消息发送给: " + targetNodeId);
        
        return new SecureMessage(senderId, encryptedContent, signature, timestamp, messageId);
    }
    
    /**
     * 解密消息（从Message对象）
     */
    public Message decryptMessage(Message message, String senderNodeId) throws Exception {
        if (!message.isSecureMessage()) {
            throw new IllegalArgumentException("不是安全消息");
        }
        
        // 解析安全消息内容
        SecureMessage secureMessage = SecureMessage.fromTransportFormat(message.getContent());
        
        // 解密消息内容
        String decryptedContent = decryptMessage(secureMessage);
        
        // 创建解密后的消息
        return new Message(message.getType(), senderNodeId, decryptedContent, message.getTargetId());
    }
    
    /**
     * 解密消息
     */
    public String decryptMessage(SecureMessage secureMessage) throws Exception {
        String senderId = secureMessage.getSenderId();
        
        // 获取会话密钥
        SecretKey sessionKey = keyManager.getSessionKey(senderId);
        if (sessionKey == null) {
            throw new IllegalStateException("未找到与节点 " + senderId + " 的会话密钥");
        }
        
        // 解密消息内容
        CryptoService.EncryptionResult encryptionResult = 
            CryptoService.EncryptionResult.fromBase64String(secureMessage.getEncryptedContent());
        String fullContent = cryptoService.decryptWithAES(encryptionResult, sessionKey);
        
        // 验证数字签名
        byte[] contentBytes = fullContent.getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = Base64.getDecoder().decode(secureMessage.getSignature());
        
        // 获取发送者公钥
        java.security.PublicKey senderPublicKey = keyManager.getPublicKey(senderId);
        if (senderPublicKey == null) {
            throw new IllegalStateException("未找到发送者 " + senderId + " 的公钥");
        }
        
        // 验证签名
        boolean signatureValid = cryptoService.verifySignature(contentBytes, signatureBytes, senderPublicKey);
        if (!signatureValid) {
            throw new SecurityException("消息签名验证失败");
        }
        
        // 解析消息内容
        String actualContent = parseMessageContent(fullContent, secureMessage.getTimestamp(), secureMessage.getMessageId());
        
        System.out.println("[安全消息] 成功解密来自 " + senderId + " 的消息");
        
        return actualContent;
    }
    
    /**
     * 加密群聊消息
     */
    public Message encryptGroupMessage(String content, String groupId) throws Exception {
        // 为群聊消息创建特殊格式
        long timestamp = System.currentTimeMillis();
        String messageId = generateMessageId();
        String senderId = keyManager.getNodeId();
        
        // 群聊消息使用发送者的私钥签名，但不加密内容（因为需要广播）
        // 在实际应用中，可以为群聊实现群密钥机制
        String fullContent = createFullMessageContent(content, timestamp, messageId);
        
        // 创建数字签名
        byte[] contentBytes = fullContent.getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = cryptoService.sign(contentBytes, keyManager.getNodePrivateKey());
        String signature = Base64.getEncoder().encodeToString(signatureBytes);
        
        // 创建安全群聊消息格式
        String secureContent = "SECURE_GROUP:" + senderId + ":" + 
                              Base64.getEncoder().encodeToString(fullContent.getBytes(StandardCharsets.UTF_8)) + 
                              ":" + signature + ":" + timestamp + ":" + messageId;
        
        System.out.println("[安全消息] 创建安全群聊消息");
        
        return new Message(Message.Type.CHAT, senderId, secureContent);
    }
    
    /**
     * 解密群聊消息
     */
    public String decryptGroupMessage(Message message) throws Exception {
        String content = message.getContent();
        
        if (!content.startsWith("SECURE_GROUP:")) {
            // 非安全群聊消息，直接返回
            return content;
        }
        
        String[] parts = content.split(":", 6);
        if (parts.length != 6) {
            throw new IllegalArgumentException("无效的安全群聊消息格式");
        }
        
        String senderId = parts[1];
        String encodedContent = parts[2];
        String signature = parts[3];
        long timestamp = Long.parseLong(parts[4]);
        String messageId = parts[5];
        
        // 解码消息内容
        byte[] contentBytes = Base64.getDecoder().decode(encodedContent);
        String fullContent = new String(contentBytes, StandardCharsets.UTF_8);
        
        // 验证数字签名
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        java.security.PublicKey senderPublicKey = keyManager.getPublicKey(senderId);
        
        if (senderPublicKey == null) {
            throw new IllegalStateException("未找到发送者 " + senderId + " 的公钥");
        }
        
        boolean signatureValid = cryptoService.verifySignature(contentBytes, signatureBytes, senderPublicKey);
        if (!signatureValid) {
            throw new SecurityException("群聊消息签名验证失败");
        }
        
        // 解析消息内容
        String actualContent = parseMessageContent(fullContent, timestamp, messageId);
        
        System.out.println("[安全消息] 成功验证来自 " + senderId + " 的群聊消息");
        
        return actualContent;
    }
    
    /**
     * 创建完整消息内容（包含元数据）
     */
    private String createFullMessageContent(String content, long timestamp, String messageId) {
        return String.join("|", 
            content, 
            String.valueOf(timestamp), 
            messageId,
            keyManager.getNodeId()
        );
    }
    
    /**
     * 解析消息内容
     */
    private String parseMessageContent(String fullContent, long expectedTimestamp, String expectedMessageId) {
        String[] parts = fullContent.split("\\|", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("无效的消息内容格式");
        }
        
        String content = parts[0];
        long timestamp = Long.parseLong(parts[1]);
        String messageId = parts[2];
        String senderId = parts[3];
        
        // 验证时间戳（防止重放攻击）
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - timestamp) > 300000) { // 5分钟容差
            throw new SecurityException("消息时间戳无效，可能是重放攻击");
        }
        
        // 验证消息ID
        if (!messageId.equals(expectedMessageId)) {
            throw new SecurityException("消息ID不匹配");
        }
        
        return content;
    }
    
    /**
     * 生成唯一消息ID
     */
    private String generateMessageId() {
        try {
            byte[] randomBytes = cryptoService.generateRandomBytes(8);
            long timestamp = System.currentTimeMillis();
            String combined = timestamp + ":" + Base64.getEncoder().encodeToString(randomBytes);
            return cryptoService.hashString(combined).substring(0, 16);
        } catch (Exception e) {
            // 降级方案
            return String.valueOf(System.currentTimeMillis()) + "_" + 
                   String.valueOf(System.nanoTime()).substring(8);
        }
    }
    
    /**
     * 验证消息完整性
     */
    public boolean verifyMessageIntegrity(SecureMessage secureMessage) {
        try {
            decryptMessage(secureMessage);
            return true;
        } catch (Exception e) {
            System.err.println("[安全消息] 消息完整性验证失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查是否可以发送安全消息
     */
    public boolean canSendSecureMessage(String targetNodeId) {
        return keyManager.getSessionKey(targetNodeId) != null && 
               keyManager.getPublicKey(targetNodeId) != null;
    }
    
    /**
     * 检查是否可以接收安全消息
     */
    public boolean canReceiveSecureMessage(String senderNodeId) {
        return keyManager.getSessionKey(senderNodeId) != null && 
               keyManager.getPublicKey(senderNodeId) != null;
    }
    
    /**
     * 获取消息加密状态信息
     */
    public String getEncryptionStatus(String nodeId) {
        boolean hasSessionKey = keyManager.getSessionKey(nodeId) != null;
        boolean hasPublicKey = keyManager.getPublicKey(nodeId) != null;
        
        if (hasSessionKey && hasPublicKey) {
            return "完全加密";
        } else if (hasPublicKey) {
            return "需要密钥交换";
        } else {
            return "未加密";
        }
    }
    
    /**
     * 启动密钥交换
     */
    public boolean initiateKeyExchange(String targetNodeId) {
        try {
            // 检查是否有目标节点的公钥
            if (!keyManager.hasPublicKey(targetNodeId)) {
                System.err.println("[密钥交换] 缺少节点公钥: " + targetNodeId);
                return false;
            }
            
            // 生成会话密钥
            SecretKey sessionKey = keyManager.generateSessionKey();
            
            // 存储会话密钥
            keyManager.storeSessionKey(targetNodeId, sessionKey);
            
            System.out.println("[密钥交换] 成功与节点 " + targetNodeId + " 建立会话密钥");
            return true;
            
        } catch (Exception e) {
            System.err.println("[密钥交换] 失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 加密消息（从Message对象）
     */
    public Message encryptMessage(Message message, String targetNodeId) {
        try {
            // 检查是否可以发送安全消息
            if (!canSendSecureMessage(targetNodeId)) {
                System.err.println("[安全消息] 无法向 " + targetNodeId + " 发送安全消息");
                return null;
            }
            
            // 加密消息内容
            SecureMessage secureMessage = encryptMessage(message.getContent(), targetNodeId);
            
            // 创建安全消息
            Message encryptedMessage = new Message(
                Message.Type.SECURE_MESSAGE,
                message.getSenderId(),
                secureMessage.toTransportFormat(),
                message.getTargetId()
            );
            
            return encryptedMessage;
            
        } catch (Exception e) {
            System.err.println("[安全消息] 加密失败: " + e.getMessage());
            return null;
        }
    }
}
