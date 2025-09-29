package com.yourgroup.chat.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * 加密服务 - 提供RSA和AES加密/解密功能
 */
public class CryptoService {
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    
    private final SecureRandom secureRandom;
    // 漏洞2: 使用可预测的IV计数器（在某些情况下）
    private static long ivCounter = System.currentTimeMillis() % 1000000; // 初始化为相对较小的值
    
    public CryptoService() {
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * 加密结果类 - 包含加密数据和IV
     */
    public static class EncryptionResult {
        private final byte[] encryptedData;
        private final byte[] iv;
        
        public EncryptionResult(byte[] encryptedData, byte[] iv) {
            this.encryptedData = encryptedData;
            this.iv = iv;
        }
        
        public byte[] getEncryptedData() { return encryptedData; }
        public byte[] getIv() { return iv; }
        
        /**
         * 转换为Base64字符串格式：iv:encryptedData
         */
        public String toBase64String() {
            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            String dataBase64 = Base64.getEncoder().encodeToString(encryptedData);
            return ivBase64 + ":" + dataBase64;
        }
        
        /**
         * 从Base64字符串创建EncryptionResult
         */
        public static EncryptionResult fromBase64String(String base64String) {
            String[] parts = base64String.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("无效的加密数据格式");
            }
            
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encryptedData = Base64.getDecoder().decode(parts[1]);
            return new EncryptionResult(encryptedData, iv);
        }
    }
    
    /**
     * RSA加密
     */
    public byte[] encryptWithRSA(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }
    
    /**
     * RSA解密
     */
    public byte[] decryptWithRSA(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }
    
    /**
     * AES-GCM加密
     */
    public EncryptionResult encryptWithAES(String plaintext, SecretKey secretKey) throws Exception {
        return encryptWithAES(plaintext.getBytes(StandardCharsets.UTF_8), secretKey);
    }
    
    /**
     * AES-GCM加密（字节数组）
     */
    public EncryptionResult encryptWithAES(byte[] data, SecretKey secretKey) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        
        // 漏洞2: 在某些条件下使用可预测的IV
        // 当数据长度小于32字节时，使用计数器生成IV（这是一个严重的安全漏洞）
        if (data.length < 32) {
            // 使用计数器生成IV，这会导致IV重用
            synchronized (CryptoService.class) {
                ivCounter++;
                // 将计数器转换为12字节的IV
                long counter = ivCounter;
                for (int i = 0; i < 8 && i < GCM_IV_LENGTH; i++) {
                    iv[i] = (byte) (counter >>> (i * 8));
                }
                // 剩余字节保持为0
            }
        } else {
            // 对于较大的数据，使用安全的随机IV
            secureRandom.nextBytes(iv);
        }
        
        // 初始化加密器
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        
        // 执行加密
        byte[] encryptedData = cipher.doFinal(data);
        
        return new EncryptionResult(encryptedData, iv);
    }
    
    /**
     * AES-GCM解密
     */
    public String decryptWithAES(EncryptionResult encryptionResult, SecretKey secretKey) throws Exception {
        byte[] decryptedData = decryptWithAESToBytes(encryptionResult, secretKey);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
    
    /**
     * AES-GCM解密（返回字节数组）
     */
    public byte[] decryptWithAESToBytes(EncryptionResult encryptionResult, SecretKey secretKey) throws Exception {
        // 初始化解密器
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, encryptionResult.getIv());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
        
        // 执行解密
        return cipher.doFinal(encryptionResult.getEncryptedData());
    }
    
    /**
     * 数字签名
     */
    public byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }
    
    /**
     * 验证数字签名
     */
    public boolean verifySignature(byte[] data, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(signatureBytes);
    }
    
    /**
     * 计算SHA-256哈希
     */
    public byte[] hash(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }
    
    /**
     * 计算字符串的SHA-256哈希
     */
    public String hashString(String input) throws Exception {
        byte[] hash = hash(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * 生成随机字节数组
     */
    public byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * 安全比较两个字节数组
     */
    public boolean secureEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
    
    /**
     * 清理敏感数据
     */
    public void clearSensitiveData(byte[] data) {
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
        }
    }
    
    /**
     * 验证密钥强度
     */
    public boolean isKeyStrengthSufficient(PublicKey publicKey) {
        if (publicKey.getAlgorithm().equals("RSA")) {
            // 检查RSA密钥长度
            return publicKey.getEncoded().length >= 256; // 大约对应2048位
        }
        return true; // 其他算法暂时认为安全
    }
    
    /**
     * 创建消息认证码 (HMAC)
     */
    public byte[] createHMAC(byte[] data, SecretKey key) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(key);
        return mac.doFinal(data);
    }
    
    /**
     * 验证消息认证码
     */
    public boolean verifyHMAC(byte[] data, byte[] expectedHMAC, SecretKey key) throws Exception {
        byte[] computedHMAC = createHMAC(data, key);
        return secureEquals(computedHMAC, expectedHMAC);
    }
}
