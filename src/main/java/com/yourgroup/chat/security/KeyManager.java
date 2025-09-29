package com.yourgroup.chat.security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 密钥管理器 - 负责生成、存储和管理所有加密密钥
 */
public class KeyManager {
    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";
    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;
    
    private static final String KEYS_DIR = "keys";
    private static final String PRIVATE_KEY_FILE = "private_key.pem";
    private static final String PUBLIC_KEY_FILE = "public_key.pem";
    
    // 节点的长期密钥对
    private KeyPair nodeKeyPair;
    
    // 会话密钥缓存 - 存储与其他节点的会话密钥
    private final Map<String, SecretKey> sessionKeys = new ConcurrentHashMap<>();
    
    // 公钥缓存 - 存储其他节点的公钥
    private final Map<String, PublicKey> publicKeyCache = new ConcurrentHashMap<>();
    
    private final SecureRandom secureRandom;
    private final String nodeId;
    
    public KeyManager(String nodeId) {
        this.nodeId = nodeId;
        this.secureRandom = new SecureRandom();
        initializeNodeKeys();
    }
    
    /**
     * 初始化节点密钥对
     */
    private void initializeNodeKeys() {
        try {
            // 尝试加载现有密钥
            if (loadExistingKeys()) {
                System.out.println("[安全] 成功加载现有密钥对");
                return;
            }
            
            // 生成新的密钥对
            System.out.println("[安全] 生成新的RSA密钥对...");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyGen.initialize(RSA_KEY_SIZE, secureRandom);
            nodeKeyPair = keyGen.generateKeyPair();
            
            // 保存密钥到文件
            saveKeysToFile();
            System.out.println("[安全] RSA密钥对生成并保存完成");
            
        } catch (Exception e) {
            throw new RuntimeException("密钥初始化失败", e);
        }
    }
    
    /**
     * 加载现有的密钥对
     */
    private boolean loadExistingKeys() {
        try {
            Path keysDir = Paths.get(KEYS_DIR);
            Path privateKeyPath = keysDir.resolve(PRIVATE_KEY_FILE);
            Path publicKeyPath = keysDir.resolve(PUBLIC_KEY_FILE);
            
            if (!Files.exists(privateKeyPath) || !Files.exists(publicKeyPath)) {
                return false;
            }
            
            // 读取私钥
            String privateKeyPEM = Files.readString(privateKeyPath);
            privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                                       .replace("-----END PRIVATE KEY-----", "")
                                       .replaceAll("\\s", "");
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            
            // 读取公钥
            String publicKeyPEM = Files.readString(publicKeyPath);
            publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "")
                                      .replace("-----END PUBLIC KEY-----", "")
                                      .replaceAll("\\s", "");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            
            nodeKeyPair = new KeyPair(publicKey, privateKey);
            return true;
            
        } catch (Exception e) {
            System.err.println("[安全] 加载现有密钥失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 保存密钥对到文件
     */
    private void saveKeysToFile() throws IOException {
        Path keysDir = Paths.get(KEYS_DIR);
        Files.createDirectories(keysDir);
        
        // 保存私钥
        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
                              Base64.getEncoder().encodeToString(nodeKeyPair.getPrivate().getEncoded()) +
                              "\n-----END PRIVATE KEY-----";
        Files.writeString(keysDir.resolve(PRIVATE_KEY_FILE), privateKeyPEM);
        
        // 保存公钥
        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" +
                             Base64.getEncoder().encodeToString(nodeKeyPair.getPublic().getEncoded()) +
                             "\n-----END PUBLIC KEY-----";
        Files.writeString(keysDir.resolve(PUBLIC_KEY_FILE), publicKeyPEM);
    }
    
    /**
     * 生成AES会话密钥
     */
    public SecretKey generateSessionKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(AES_KEY_SIZE, secureRandom);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES密钥生成失败", e);
        }
    }
    
    /**
     * 存储会话密钥
     */
    public void storeSessionKey(String nodeId, SecretKey sessionKey) {
        sessionKeys.put(nodeId, sessionKey);
        System.out.println("[安全] 存储会话密钥: " + nodeId);
    }
    
    /**
     * 获取会话密钥
     */
    public SecretKey getSessionKey(String nodeId) {
        return sessionKeys.get(nodeId);
    }
    
    /**
     * 移除会话密钥
     */
    public void removeSessionKey(String nodeId) {
        SecretKey removed = sessionKeys.remove(nodeId);
        if (removed != null) {
            System.out.println("[安全] 移除会话密钥: " + nodeId);
        }
    }
    
    /**
     * 存储其他节点的公钥
     */
    public void storePublicKey(String nodeId, PublicKey publicKey) {
        publicKeyCache.put(nodeId, publicKey);
        System.out.println("[安全] 存储公钥: " + nodeId);
    }
    
    /**
     * 获取其他节点的公钥
     */
    public PublicKey getPublicKey(String nodeId) {
        return publicKeyCache.get(nodeId);
    }
    
    /**
     * 获取本节点的公钥
     */
    public PublicKey getNodePublicKey() {
        return nodeKeyPair.getPublic();
    }
    
    /**
     * 获取本节点的私钥
     */
    public PrivateKey getNodePrivateKey() {
        return nodeKeyPair.getPrivate();
    }
    
    /**
     * 获取节点ID（基于公钥指纹）
     */
    public String getNodeId() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(nodeKeyPair.getPublic().getEncoded());
            return Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成节点ID失败", e);
        }
    }
    
    /**
     * 获取公钥的指纹
     */
    public String getPublicKeyFingerprint(PublicKey publicKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKey.getEncoded());
            return Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成公钥指纹失败", e);
        }
    }
    
    /**
     * 清理所有会话密钥
     */
    public void clearAllSessionKeys() {
        sessionKeys.clear();
        System.out.println("[安全] 清理所有会话密钥");
    }
    
    /**
     * 获取会话密钥数量
     */
    public int getSessionKeyCount() {
        return sessionKeys.size();
    }
    
    /**
     * 获取公钥缓存数量
     */
    public int getPublicKeyCount() {
        return publicKeyCache.size();
    }
    
    /**
     * 从字节数组创建SecretKey
     */
    public SecretKey createSecretKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }
    
    /**
     * 从Base64字符串创建PublicKey
     */
    public PublicKey createPublicKey(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }
    
    /**
     * 将PublicKey转换为Base64字符串
     */
    public String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    
    /**
     * 检查是否有与指定节点的会话密钥
     */
    public boolean hasSessionKey(String nodeId) {
        return sessionKeys.containsKey(nodeId);
    }
    
    /**
     * 检查是否有指定节点的公钥
     */
    public boolean hasPublicKey(String nodeId) {
        return publicKeyCache.containsKey(nodeId);
    }
}
