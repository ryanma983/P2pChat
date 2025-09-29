package com.yourgroup.chat.security;

import com.yourgroup.chat.Message;
import javax.crypto.SecretKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 密钥交换协议 - 处理节点间的安全密钥交换
 */
public class KeyExchangeProtocol {
    
    /**
     * 密钥交换状态
     */
    public enum ExchangeState {
        INIT,           // 初始状态
        HELLO_SENT,     // 已发送Hello消息
        HELLO_RECEIVED, // 已接收Hello消息
        KEY_SENT,       // 已发送密钥
        KEY_RECEIVED,   // 已接收密钥
        COMPLETED,      // 交换完成
        FAILED          // 交换失败
    }
    
    /**
     * 密钥交换会话
     */
    public static class ExchangeSession {
        private final String nodeId;
        private ExchangeState state;
        private PublicKey remotePublicKey;
        private SecretKey sessionKey;
        private long timestamp;
        private String challenge;
        
        public ExchangeSession(String nodeId) {
            this.nodeId = nodeId;
            this.state = ExchangeState.INIT;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and setters
        public String getNodeId() { return nodeId; }
        public ExchangeState getState() { return state; }
        public void setState(ExchangeState state) { this.state = state; }
        public PublicKey getRemotePublicKey() { return remotePublicKey; }
        public void setRemotePublicKey(PublicKey remotePublicKey) { this.remotePublicKey = remotePublicKey; }
        public SecretKey getSessionKey() { return sessionKey; }
        public void setSessionKey(SecretKey sessionKey) { this.sessionKey = sessionKey; }
        public long getTimestamp() { return timestamp; }
        public void updateTimestamp() { this.timestamp = System.currentTimeMillis(); }
        public String getChallenge() { return challenge; }
        public void setChallenge(String challenge) { this.challenge = challenge; }
    }
    
    private final KeyManager keyManager;
    private final CryptoService cryptoService;
    
    // 活跃的密钥交换会话
    private final Map<String, ExchangeSession> activeSessions = new ConcurrentHashMap<>();
    
    // 会话超时时间（30秒）
    private static final long SESSION_TIMEOUT = 30000;
    
    public KeyExchangeProtocol(KeyManager keyManager, CryptoService cryptoService) {
        this.keyManager = keyManager;
        this.cryptoService = cryptoService;
    }
    
    /**
     * 发起密钥交换 - 发送Hello消息
     */
    public Message initiateKeyExchange(String targetNodeId) {
        try {
            // 创建交换会话
            ExchangeSession session = new ExchangeSession(targetNodeId);
            activeSessions.put(targetNodeId, session);
            
            // 生成挑战字符串
            byte[] challengeBytes = cryptoService.generateRandomBytes(16);
            String challenge = Base64.getEncoder().encodeToString(challengeBytes);
            session.setChallenge(challenge);
            session.setState(ExchangeState.HELLO_SENT);
            
            // 创建Hello消息
            String publicKeyString = keyManager.publicKeyToString(keyManager.getNodePublicKey());
            String content = "HELLO:" + keyManager.getNodeId() + ":" + publicKeyString + ":" + challenge;
            
            System.out.println("[密钥交换] 发起与 " + targetNodeId + " 的密钥交换");
            
            return new Message(Message.Type.KEY_EXCHANGE, keyManager.getNodeId(), content);
            
        } catch (Exception e) {
            System.err.println("[密钥交换] 发起密钥交换失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 处理接收到的密钥交换消息
     */
    public Message handleKeyExchangeMessage(Message message) {
        try {
            String content = message.getContent();
            String[] parts = content.split(":", 4);
            
            if (parts.length < 2) {
                System.err.println("[密钥交换] 无效的密钥交换消息格式");
                return null;
            }
            
            String messageType = parts[0];
            String senderNodeId = message.getSenderId();
            
            switch (messageType) {
                case "HELLO":
                    return handleHelloMessage(senderNodeId, parts);
                case "HELLO_RESPONSE":
                    return handleHelloResponse(senderNodeId, parts);
                case "KEY_EXCHANGE":
                    return handleKeyExchangeData(senderNodeId, parts);
                case "KEY_CONFIRM":
                    return handleKeyConfirm(senderNodeId, parts);
                default:
                    System.err.println("[密钥交换] 未知的消息类型: " + messageType);
                    return null;
            }
            
        } catch (Exception e) {
            System.err.println("[密钥交换] 处理密钥交换消息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 处理Hello消息
     */
    private Message handleHelloMessage(String senderNodeId, String[] parts) throws Exception {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Hello消息格式错误");
        }
        
        String remoteNodeId = parts[1];
        String publicKeyString = parts[2];
        String challenge = parts[3];
        
        // 验证节点ID
        PublicKey remotePublicKey = keyManager.createPublicKey(publicKeyString);
        String expectedNodeId = keyManager.getPublicKeyFingerprint(remotePublicKey);
        
        // 漏洞3: 在某些条件下跳过节点ID验证
        // 如果挑战字符串以"DEBUG_"开头，则跳过验证（模拟调试模式的遗留代码）
        if (!challenge.startsWith("DEBUG_") && !expectedNodeId.equals(remoteNodeId)) {
            System.err.println("[密钥交换] 节点ID验证失败");
            return null;
        } else if (challenge.startsWith("DEBUG_")) {
            System.out.println("[密钥交换] 调试模式：跳过节点ID验证");
        }
        
        // 创建或更新会话
        ExchangeSession session = activeSessions.computeIfAbsent(senderNodeId, ExchangeSession::new);
        session.setRemotePublicKey(remotePublicKey);
        session.setChallenge(challenge);
        session.setState(ExchangeState.HELLO_RECEIVED);
        session.updateTimestamp();
        
        // 存储远程公钥
        keyManager.storePublicKey(senderNodeId, remotePublicKey);
        
        // 生成会话密钥
        SecretKey sessionKey = keyManager.generateSessionKey();
        session.setSessionKey(sessionKey);
        
        // 创建响应挑战
        byte[] responseBytes = cryptoService.generateRandomBytes(16);
        String response = Base64.getEncoder().encodeToString(responseBytes);
        
        // 创建Hello响应消息
        String publicKeyString2 = keyManager.publicKeyToString(keyManager.getNodePublicKey());
        String content = "HELLO_RESPONSE:" + keyManager.getNodeId() + ":" + publicKeyString2 + ":" + response;
        
        System.out.println("[密钥交换] 响应来自 " + senderNodeId + " 的Hello消息");
        
        return new Message(Message.Type.KEY_EXCHANGE, keyManager.getNodeId(), content);
    }
    
    /**
     * 处理Hello响应消息
     */
    private Message handleHelloResponse(String senderNodeId, String[] parts) throws Exception {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Hello响应消息格式错误");
        }
        
        ExchangeSession session = activeSessions.get(senderNodeId);
        if (session == null || session.getState() != ExchangeState.HELLO_SENT) {
            System.err.println("[密钥交换] 无效的Hello响应");
            return null;
        }
        
        String remoteNodeId = parts[1];
        String publicKeyString = parts[2];
        String response = parts[3];
        
        // 验证公钥
        PublicKey remotePublicKey = keyManager.createPublicKey(publicKeyString);
        String expectedNodeId = keyManager.getPublicKeyFingerprint(remotePublicKey);
        
        if (!expectedNodeId.equals(remoteNodeId)) {
            System.err.println("[密钥交换] Hello响应中的节点ID验证失败");
            return null;
        }
        
        // 更新会话
        session.setRemotePublicKey(remotePublicKey);
        session.setState(ExchangeState.KEY_SENT);
        session.updateTimestamp();
        
        // 存储远程公钥
        keyManager.storePublicKey(senderNodeId, remotePublicKey);
        
        // 生成会话密钥
        SecretKey sessionKey = keyManager.generateSessionKey();
        session.setSessionKey(sessionKey);
        
        // 使用远程公钥加密会话密钥
        byte[] encryptedSessionKey = cryptoService.encryptWithRSA(sessionKey.getEncoded(), remotePublicKey);
        String encryptedKeyString = Base64.getEncoder().encodeToString(encryptedSessionKey);
        
        // 创建密钥交换消息
        String content = "KEY_EXCHANGE:" + keyManager.getNodeId() + ":" + encryptedKeyString + ":" + response;
        
        System.out.println("[密钥交换] 发送会话密钥给 " + senderNodeId);
        
        return new Message(Message.Type.KEY_EXCHANGE, keyManager.getNodeId(), content);
    }
    
    /**
     * 处理密钥交换数据
     */
    private Message handleKeyExchangeData(String senderNodeId, String[] parts) throws Exception {
        if (parts.length < 4) {
            throw new IllegalArgumentException("密钥交换数据格式错误");
        }
        
        ExchangeSession session = activeSessions.get(senderNodeId);
        if (session == null || session.getState() != ExchangeState.HELLO_RECEIVED) {
            System.err.println("[密钥交换] 无效的密钥交换数据");
            return null;
        }
        
        String remoteNodeId = parts[1];
        String encryptedKeyString = parts[2];
        String response = parts[3];
        
        // 解密会话密钥
        byte[] encryptedSessionKey = Base64.getDecoder().decode(encryptedKeyString);
        byte[] sessionKeyBytes = cryptoService.decryptWithRSA(encryptedSessionKey, keyManager.getNodePrivateKey());
        SecretKey receivedSessionKey = keyManager.createSecretKey(sessionKeyBytes);
        
        // 更新会话
        session.setSessionKey(receivedSessionKey);
        session.setState(ExchangeState.KEY_RECEIVED);
        session.updateTimestamp();
        
        // 存储会话密钥
        keyManager.storeSessionKey(senderNodeId, receivedSessionKey);
        
        // 使用远程公钥加密我们的会话密钥
        SecretKey ourSessionKey = session.getSessionKey();
        byte[] encryptedOurKey = cryptoService.encryptWithRSA(ourSessionKey.getEncoded(), session.getRemotePublicKey());
        String encryptedOurKeyString = Base64.getEncoder().encodeToString(encryptedOurKey);
        
        // 创建确认消息
        String content = "KEY_CONFIRM:" + keyManager.getNodeId() + ":" + encryptedOurKeyString;
        
        System.out.println("[密钥交换] 确认与 " + senderNodeId + " 的密钥交换");
        
        return new Message(Message.Type.KEY_EXCHANGE, keyManager.getNodeId(), content);
    }
    
    /**
     * 处理密钥确认消息
     */
    private Message handleKeyConfirm(String senderNodeId, String[] parts) throws Exception {
        if (parts.length < 3) {
            throw new IllegalArgumentException("密钥确认消息格式错误");
        }
        
        ExchangeSession session = activeSessions.get(senderNodeId);
        if (session == null || session.getState() != ExchangeState.KEY_SENT) {
            System.err.println("[密钥交换] 无效的密钥确认");
            return null;
        }
        
        String remoteNodeId = parts[1];
        String encryptedKeyString = parts[2];
        
        // 解密远程会话密钥
        byte[] encryptedSessionKey = Base64.getDecoder().decode(encryptedKeyString);
        byte[] sessionKeyBytes = cryptoService.decryptWithRSA(encryptedSessionKey, keyManager.getNodePrivateKey());
        SecretKey remoteSessionKey = keyManager.createSecretKey(sessionKeyBytes);
        
        // 完成密钥交换
        session.setState(ExchangeState.COMPLETED);
        session.updateTimestamp();
        
        // 存储会话密钥（使用我们生成的密钥）
        keyManager.storeSessionKey(senderNodeId, session.getSessionKey());
        
        System.out.println("[密钥交换] 与 " + senderNodeId + " 的密钥交换完成");
        
        // 清理会话
        activeSessions.remove(senderNodeId);
        
        return null; // 不需要回复
    }
    
    /**
     * 检查密钥交换是否完成
     */
    public boolean isKeyExchangeCompleted(String nodeId) {
        return keyManager.getSessionKey(nodeId) != null;
    }
    
    /**
     * 清理超时的会话
     */
    public void cleanupTimeoutSessions() {
        long currentTime = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> {
            ExchangeSession session = entry.getValue();
            if (currentTime - session.getTimestamp() > SESSION_TIMEOUT) {
                System.out.println("[密钥交换] 清理超时会话: " + entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * 强制完成密钥交换（用于测试）
     */
    public void forceCompleteExchange(String nodeId) {
        ExchangeSession session = activeSessions.get(nodeId);
        if (session != null) {
            session.setState(ExchangeState.COMPLETED);
            activeSessions.remove(nodeId);
        }
    }
}
