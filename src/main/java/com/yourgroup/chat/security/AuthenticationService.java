package com.yourgroup.chat.security;

import java.security.PublicKey;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 身份验证服务 - 处理节点身份验证和数字签名
 */
public class AuthenticationService {
    
    private final KeyManager keyManager;
    private final CryptoService cryptoService;
    
    // 信任的节点列表
    private final Map<String, TrustedNode> trustedNodes = new ConcurrentHashMap<>();
    
    // 挑战-响应缓存（防止重放攻击）
    private final Map<String, ChallengeData> activeChallenges = new ConcurrentHashMap<>();
    
    // 挑战超时时间（5分钟）
    private static final long CHALLENGE_TIMEOUT = 300000;
    
    /**
     * 可信节点信息
     */
    public static class TrustedNode {
        private final String nodeId;
        private final PublicKey publicKey;
        private final String fingerprint;
        private final long firstSeen;
        private long lastSeen;
        private boolean verified;
        private int trustLevel; // 0-100，信任级别
        
        public TrustedNode(String nodeId, PublicKey publicKey, String fingerprint) {
            this.nodeId = nodeId;
            this.publicKey = publicKey;
            this.fingerprint = fingerprint;
            this.firstSeen = System.currentTimeMillis();
            this.lastSeen = firstSeen;
            this.verified = false;
            this.trustLevel = 0;
        }
        
        // Getters and setters
        public String getNodeId() { return nodeId; }
        public PublicKey getPublicKey() { return publicKey; }
        public String getFingerprint() { return fingerprint; }
        public long getFirstSeen() { return firstSeen; }
        public long getLastSeen() { return lastSeen; }
        public boolean isVerified() { return verified; }
        public int getTrustLevel() { return trustLevel; }
        
        public void updateLastSeen() { this.lastSeen = System.currentTimeMillis(); }
        public void setVerified(boolean verified) { this.verified = verified; }
        public void setTrustLevel(int trustLevel) { 
            this.trustLevel = Math.max(0, Math.min(100, trustLevel)); 
        }
        
        public void increaseTrust(int amount) {
            setTrustLevel(trustLevel + amount);
        }
        
        public void decreaseTrust(int amount) {
            setTrustLevel(trustLevel - amount);
        }
    }
    
    /**
     * 挑战数据
     */
    private static class ChallengeData {
        private final String challenge;
        private final long timestamp;
        private final String nodeId;
        
        public ChallengeData(String challenge, String nodeId) {
            this.challenge = challenge;
            this.nodeId = nodeId;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getChallenge() { return challenge; }
        public long getTimestamp() { return timestamp; }
        public String getNodeId() { return nodeId; }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CHALLENGE_TIMEOUT;
        }
    }
    
    /**
     * 身份验证结果
     */
    public enum AuthenticationResult {
        SUCCESS,            // 验证成功
        FAILED,             // 验证失败
        INVALID_SIGNATURE,  // 签名无效
        EXPIRED_CHALLENGE,  // 挑战过期
        UNKNOWN_NODE,       // 未知节点
        TRUST_INSUFFICIENT  // 信任级别不足
    }
    
    public AuthenticationService(KeyManager keyManager, CryptoService cryptoService) {
        this.keyManager = keyManager;
        this.cryptoService = cryptoService;
    }
    
    /**
     * 注册新节点
     */
    public boolean registerNode(String nodeId, PublicKey publicKey) {
        try {
            // 验证节点ID与公钥的一致性
            String expectedNodeId = keyManager.getPublicKeyFingerprint(publicKey);
            if (!expectedNodeId.equals(nodeId)) {
                System.err.println("[身份验证] 节点ID与公钥不匹配: " + nodeId);
                return false;
            }
            
            // 检查密钥强度
            if (!cryptoService.isKeyStrengthSufficient(publicKey)) {
                System.err.println("[身份验证] 公钥强度不足: " + nodeId);
                return false;
            }
            
            String fingerprint = keyManager.getPublicKeyFingerprint(publicKey);
            TrustedNode trustedNode = new TrustedNode(nodeId, publicKey, fingerprint);
            trustedNodes.put(nodeId, trustedNode);
            
            // 存储公钥到密钥管理器
            keyManager.storePublicKey(nodeId, publicKey);
            
            System.out.println("[身份验证] 注册新节点: " + nodeId + " (指纹: " + fingerprint + ")");
            return true;
            
        } catch (Exception e) {
            System.err.println("[身份验证] 注册节点失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成身份验证挑战
     */
    public String generateChallenge(String targetNodeId) {
        try {
            // 生成随机挑战字符串
            byte[] challengeBytes = cryptoService.generateRandomBytes(32);
            String challenge = Base64.getEncoder().encodeToString(challengeBytes);
            
            // 存储挑战数据
            activeChallenges.put(challenge, new ChallengeData(challenge, targetNodeId));
            
            System.out.println("[身份验证] 生成挑战给节点: " + targetNodeId);
            return challenge;
            
        } catch (Exception e) {
            System.err.println("[身份验证] 生成挑战失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 响应身份验证挑战
     */
    public String respondToChallenge(String challenge) {
        try {
            // 使用私钥签名挑战
            byte[] challengeBytes = challenge.getBytes();
            byte[] signature = cryptoService.sign(challengeBytes, keyManager.getNodePrivateKey());
            
            // 创建响应（包含签名和节点信息）
            String nodeId = keyManager.getNodeId();
            String publicKeyString = keyManager.publicKeyToString(keyManager.getNodePublicKey());
            String signatureString = Base64.getEncoder().encodeToString(signature);
            
            String response = String.join(":", nodeId, publicKeyString, signatureString);
            
            System.out.println("[身份验证] 响应挑战: " + challenge.substring(0, 8) + "...");
            return response;
            
        } catch (Exception e) {
            System.err.println("[身份验证] 响应挑战失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证挑战响应
     */
    public AuthenticationResult verifyResponse(String challenge, String response) {
        try {
            // 检查挑战是否存在且未过期
            ChallengeData challengeData = activeChallenges.get(challenge);
            if (challengeData == null) {
                return AuthenticationResult.EXPIRED_CHALLENGE;
            }
            
            if (challengeData.isExpired()) {
                activeChallenges.remove(challenge);
                return AuthenticationResult.EXPIRED_CHALLENGE;
            }
            
            // 解析响应
            String[] parts = response.split(":", 3);
            if (parts.length != 3) {
                return AuthenticationResult.FAILED;
            }
            
            String nodeId = parts[0];
            String publicKeyString = parts[1];
            String signatureString = parts[2];
            
            // 验证节点是否已注册
            TrustedNode trustedNode = trustedNodes.get(nodeId);
            if (trustedNode == null) {
                // 尝试注册新节点
                try {
                    PublicKey publicKey = keyManager.createPublicKey(publicKeyString);
                    if (!registerNode(nodeId, publicKey)) {
                        return AuthenticationResult.UNKNOWN_NODE;
                    }
                    trustedNode = trustedNodes.get(nodeId);
                } catch (Exception e) {
                    return AuthenticationResult.UNKNOWN_NODE;
                }
            }
            
            // 验证签名
            byte[] challengeBytes = challenge.getBytes();
            byte[] signature = Base64.getDecoder().decode(signatureString);
            
            boolean signatureValid = cryptoService.verifySignature(
                challengeBytes, signature, trustedNode.getPublicKey());
            
            if (!signatureValid) {
                trustedNode.decreaseTrust(10);
                return AuthenticationResult.INVALID_SIGNATURE;
            }
            
            // 验证成功，更新节点信息
            trustedNode.setVerified(true);
            trustedNode.updateLastSeen();
            trustedNode.increaseTrust(5);
            
            // 清理挑战
            activeChallenges.remove(challenge);
            
            System.out.println("[身份验证] 验证成功: " + nodeId + " (信任级别: " + trustedNode.getTrustLevel() + ")");
            return AuthenticationResult.SUCCESS;
            
        } catch (Exception e) {
            System.err.println("[身份验证] 验证响应失败: " + e.getMessage());
            return AuthenticationResult.FAILED;
        }
    }
    
    /**
     * 签名消息
     */
    public String signMessage(String message) {
        try {
            byte[] messageBytes = message.getBytes();
            byte[] signature = cryptoService.sign(messageBytes, keyManager.getNodePrivateKey());
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            System.err.println("[身份验证] 签名消息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证消息签名
     */
    public boolean verifyMessageSignature(String message, String signature, String senderNodeId) {
        try {
            TrustedNode trustedNode = trustedNodes.get(senderNodeId);
            if (trustedNode == null) {
                System.err.println("[身份验证] 未知发送者: " + senderNodeId);
                return false;
            }
            
            byte[] messageBytes = message.getBytes();
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            
            boolean valid = cryptoService.verifySignature(
                messageBytes, signatureBytes, trustedNode.getPublicKey());
            
            if (valid) {
                trustedNode.updateLastSeen();
                trustedNode.increaseTrust(1);
            } else {
                trustedNode.decreaseTrust(5);
            }
            
            return valid;
            
        } catch (Exception e) {
            System.err.println("[身份验证] 验证消息签名失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查节点是否可信
     */
    public boolean isNodeTrusted(String nodeId) {
        TrustedNode node = trustedNodes.get(nodeId);
        return node != null && node.isVerified() && node.getTrustLevel() >= 50;
    }
    
    /**
     * 检查节点是否已验证
     */
    public boolean isNodeVerified(String nodeId) {
        TrustedNode node = trustedNodes.get(nodeId);
        return node != null && node.isVerified();
    }
    
    /**
     * 获取节点信任级别
     */
    public int getNodeTrustLevel(String nodeId) {
        TrustedNode node = trustedNodes.get(nodeId);
        return node != null ? node.getTrustLevel() : 0;
    }
    
    /**
     * 获取可信节点信息
     */
    public TrustedNode getTrustedNode(String nodeId) {
        return trustedNodes.get(nodeId);
    }
    
    /**
     * 获取所有可信节点
     */
    public Map<String, TrustedNode> getAllTrustedNodes() {
        return new ConcurrentHashMap<>(trustedNodes);
    }
    
    /**
     * 移除节点
     */
    public boolean removeNode(String nodeId) {
        TrustedNode removed = trustedNodes.remove(nodeId);
        if (removed != null) {
            System.out.println("[身份验证] 移除节点: " + nodeId);
            return true;
        }
        return false;
    }
    
    /**
     * 手动设置节点信任级别
     */
    public boolean setNodeTrustLevel(String nodeId, int trustLevel) {
        TrustedNode node = trustedNodes.get(nodeId);
        if (node != null) {
            node.setTrustLevel(trustLevel);
            System.out.println("[身份验证] 设置节点信任级别: " + nodeId + " -> " + trustLevel);
            return true;
        }
        return false;
    }
    
    /**
     * 清理过期的挑战
     */
    public void cleanupExpiredChallenges() {
        activeChallenges.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                System.out.println("[身份验证] 清理过期挑战: " + entry.getKey().substring(0, 8) + "...");
                return true;
            }
            return false;
        });
    }
    
    /**
     * 获取身份验证统计信息
     */
    public String getAuthenticationStats() {
        int totalNodes = trustedNodes.size();
        int verifiedNodes = (int) trustedNodes.values().stream().filter(TrustedNode::isVerified).count();
        int trustedNodesCount = (int) this.trustedNodes.values().stream().filter(node -> node.getTrustLevel() >= 50).count();
        int activeChallenges = this.activeChallenges.size();
        
        return String.format("节点统计: 总计=%d, 已验证=%d, 可信=%d, 活跃挑战=%d", 
            totalNodes, verifiedNodes, trustedNodesCount, activeChallenges);
    }
    
    /**
     * 创建节点身份证书（简化版）
     */
    public String createNodeCertificate() {
        try {
            String nodeId = keyManager.getNodeId();
            String publicKeyString = keyManager.publicKeyToString(keyManager.getNodePublicKey());
            long timestamp = System.currentTimeMillis();
            
            String certificateData = String.join(":", nodeId, publicKeyString, String.valueOf(timestamp));
            String signature = signMessage(certificateData);
            
            return certificateData + ":" + signature;
            
        } catch (Exception e) {
            System.err.println("[身份验证] 创建节点证书失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证节点身份证书
     */
    public boolean verifyCertificate(String certificate) {
        try {
            String[] parts = certificate.split(":", 4);
            if (parts.length != 4) {
                return false;
            }
            
            String nodeId = parts[0];
            String publicKeyString = parts[1];
            String timestamp = parts[2];
            String signature = parts[3];
            
            String certificateData = String.join(":", nodeId, publicKeyString, timestamp);
            
            // 验证时间戳（证书有效期1年）
            long certTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            if (currentTime - certTime > 365L * 24 * 60 * 60 * 1000) {
                return false; // 证书过期
            }
            
            return verifyMessageSignature(certificateData, signature, nodeId);
            
        } catch (Exception e) {
            System.err.println("[身份验证] 验证证书失败: " + e.getMessage());
            return false;
        }
    }
}
