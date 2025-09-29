package com.yourgroup.chat.security;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 安全文件传输服务 - 提供加密的文件传输功能
 */
public class SecureFileTransferService {
    
    private final KeyManager keyManager;
    private final CryptoService cryptoService;
    private final int basePort;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean running = false;
    
    // 文件传输会话管理
    private final ConcurrentHashMap<String, FileTransferSession> activeSessions = new ConcurrentHashMap<>();
    
    // 文件块大小（64KB）
    private static final int CHUNK_SIZE = 65536;
    
    /**
     * 文件传输会话
     */
    public static class FileTransferSession {
        private final String sessionId;
        private final String nodeId;
        private final String fileName;
        private final long fileSize;
        private final String fileHash;
        private final SecretKey encryptionKey;
        private long transferredBytes;
        private boolean completed;
        private long startTime;
        
        public FileTransferSession(String sessionId, String nodeId, String fileName, 
                                 long fileSize, String fileHash, SecretKey encryptionKey) {
            this.sessionId = sessionId;
            this.nodeId = nodeId;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.fileHash = fileHash;
            this.encryptionKey = encryptionKey;
            this.transferredBytes = 0;
            this.completed = false;
            this.startTime = System.currentTimeMillis();
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public String getNodeId() { return nodeId; }
        public String getFileName() { return fileName; }
        public long getFileSize() { return fileSize; }
        public String getFileHash() { return fileHash; }
        public SecretKey getEncryptionKey() { return encryptionKey; }
        public long getTransferredBytes() { return transferredBytes; }
        public boolean isCompleted() { return completed; }
        public long getStartTime() { return startTime; }
        
        public void updateProgress(long bytes) {
            this.transferredBytes += bytes;
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
        
        public double getProgress() {
            return fileSize > 0 ? (double) transferredBytes / fileSize : 0.0;
        }
        
        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }
    }
    
    /**
     * 文件传输结果
     */
    public static class TransferResult {
        private final boolean success;
        private final String message;
        private final long transferredBytes;
        private final long duration;
        
        public TransferResult(boolean success, String message, long transferredBytes, long duration) {
            this.success = success;
            this.message = message;
            this.transferredBytes = transferredBytes;
            this.duration = duration;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getTransferredBytes() { return transferredBytes; }
        public long getDuration() { return duration; }
    }
    
    public SecureFileTransferService(KeyManager keyManager, CryptoService cryptoService, int basePort) {
        this.keyManager = keyManager;
        this.cryptoService = cryptoService;
        this.basePort = basePort + 2000; // 使用不同的端口范围
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * 启动安全文件传输服务
     */
    public void start() throws IOException {
        if (running) return;
        
        serverSocket = new ServerSocket(basePort);
        running = true;
        
        // 启动服务器监听线程
        executorService.submit(this::acceptConnections);
        
        System.out.println("[安全文件传输] 服务启动，端口: " + basePort);
    }
    
    /**
     * 停止安全文件传输服务
     */
    public void stop() {
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[安全文件传输] 关闭服务器套接字失败: " + e.getMessage());
        }
        
        executorService.shutdown();
        activeSessions.clear();
        
        System.out.println("[安全文件传输] 服务已停止");
    }
    
    /**
     * 接受连接
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleFileTransfer(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("[安全文件传输] 接受连接失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 发送加密文件
     */
    public TransferResult sendSecureFile(String targetNodeId, String filePath, String savePath) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 检查文件是否存在
            Path file = Paths.get(filePath);
            if (!Files.exists(file)) {
                return new TransferResult(false, "文件不存在: " + filePath, 0, 0);
            }
            
            // 检查是否有会话密钥
            SecretKey sessionKey = keyManager.getSessionKey(targetNodeId);
            if (sessionKey == null) {
                return new TransferResult(false, "未找到与目标节点的会话密钥", 0, 0);
            }
            
            // 计算文件哈希
            String fileHash = calculateFileHash(file);
            long fileSize = Files.size(file);
            String fileName = file.getFileName().toString();
            
            // 生成会话ID
            String sessionId = generateSessionId();
            
            // 创建传输会话
            FileTransferSession session = new FileTransferSession(
                sessionId, targetNodeId, fileName, fileSize, fileHash, sessionKey);
            activeSessions.put(sessionId, session);
            
            // 连接到目标节点
            String targetAddress = getNodeAddress(targetNodeId);
            if (targetAddress == null) {
                return new TransferResult(false, "无法获取目标节点地址", 0, 0);
            }
            
            String[] addressParts = targetAddress.split(":");
            String host = addressParts[0];
            int port = Integer.parseInt(addressParts[1]) + 2000; // 安全文件传输端口
            
            try (Socket socket = new Socket(host, port);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 FileInputStream fileIn = new FileInputStream(file.toFile())) {
                
                // 发送传输头信息
                sendTransferHeader(out, sessionId, fileName, fileSize, fileHash, savePath);
                
                // 等待确认
                String response = in.readUTF();
                if (!"READY".equals(response)) {
                    return new TransferResult(false, "目标节点拒绝接收文件: " + response, 0, 0);
                }
                
                // 开始传输加密文件数据
                long totalSent = sendEncryptedFileData(out, fileIn, session);
                
                // 等待传输完成确认
                String finalResponse = in.readUTF();
                boolean success = "SUCCESS".equals(finalResponse);
                
                long duration = System.currentTimeMillis() - startTime;
                activeSessions.remove(sessionId);
                
                if (success) {
                    System.out.println("[安全文件传输] 文件发送成功: " + fileName + " (" + totalSent + " bytes)");
                    return new TransferResult(true, "文件发送成功", totalSent, duration);
                } else {
                    return new TransferResult(false, "文件传输验证失败: " + finalResponse, totalSent, duration);
                }
                
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("[安全文件传输] 发送文件失败: " + e.getMessage());
            return new TransferResult(false, "发送失败: " + e.getMessage(), 0, duration);
        }
    }
    
    /**
     * 处理文件传输连接
     */
    private void handleFileTransfer(Socket clientSocket) {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            
            // 接收传输头信息
            String headerData = in.readUTF();
            FileTransferHeader header = parseTransferHeader(headerData);
            
            if (header == null) {
                out.writeUTF("ERROR:Invalid header");
                return;
            }
            
            // 验证会话密钥
            SecretKey sessionKey = keyManager.getSessionKey(header.senderId);
            if (sessionKey == null) {
                out.writeUTF("ERROR:No session key");
                return;
            }
            
            // 创建接收会话
            FileTransferSession session = new FileTransferSession(
                header.sessionId, header.senderId, header.fileName, 
                header.fileSize, header.fileHash, sessionKey);
            activeSessions.put(header.sessionId, session);
            
            // 确认准备接收
            out.writeUTF("READY");
            
            // 接收加密文件数据
            TransferResult result = receiveEncryptedFileData(in, header, session);
            
            // 发送最终结果
            out.writeUTF(result.isSuccess() ? "SUCCESS" : "ERROR:" + result.getMessage());
            
            activeSessions.remove(header.sessionId);
            
            if (result.isSuccess()) {
                System.out.println("[安全文件传输] 文件接收成功: " + header.fileName);
            } else {
                System.err.println("[安全文件传输] 文件接收失败: " + result.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("[安全文件传输] 处理文件传输失败: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // 忽略关闭异常
            }
        }
    }
    
    /**
     * 发送传输头信息
     */
    private void sendTransferHeader(DataOutputStream out, String sessionId, String fileName, 
                                  long fileSize, String fileHash, String savePath) throws Exception {
        String headerData = String.join(":", 
            sessionId, 
            keyManager.getNodeId(), 
            fileName, 
            String.valueOf(fileSize), 
            fileHash, 
            savePath
        );
        
        out.writeUTF(headerData);
    }
    
    /**
     * 传输头信息类
     */
    private static class FileTransferHeader {
        final String sessionId;
        final String senderId;
        final String fileName;
        final long fileSize;
        final String fileHash;
        final String savePath;
        
        FileTransferHeader(String sessionId, String senderId, String fileName, 
                          long fileSize, String fileHash, String savePath) {
            this.sessionId = sessionId;
            this.senderId = senderId;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.fileHash = fileHash;
            this.savePath = savePath;
        }
    }
    
    /**
     * 解析传输头信息
     */
    private FileTransferHeader parseTransferHeader(String headerData) {
        try {
            String[] parts = headerData.split(":", 6);
            if (parts.length != 6) {
                return null;
            }
            
            return new FileTransferHeader(
                parts[0], // sessionId
                parts[1], // senderId
                parts[2], // fileName
                Long.parseLong(parts[3]), // fileSize
                parts[4], // fileHash
                parts[5]  // savePath
            );
        } catch (Exception e) {
            System.err.println("[安全文件传输] 解析传输头失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 发送加密文件数据
     */
    private long sendEncryptedFileData(DataOutputStream out, FileInputStream fileIn, 
                                     FileTransferSession session) throws Exception {
        byte[] buffer = new byte[CHUNK_SIZE];
        long totalSent = 0;
        int bytesRead;
        
        while ((bytesRead = fileIn.read(buffer)) != -1) {
            // 创建实际数据块（可能小于缓冲区大小）
            byte[] chunk = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunk, 0, bytesRead);
            
            // 加密数据块
            CryptoService.EncryptionResult encryptedChunk = 
                cryptoService.encryptWithAES(chunk, session.getEncryptionKey());
            
            // 发送加密数据长度和数据
            byte[] encryptedData = encryptedChunk.getEncryptedData();
            byte[] iv = encryptedChunk.getIv();
            
            out.writeInt(iv.length);
            out.write(iv);
            out.writeInt(encryptedData.length);
            out.write(encryptedData);
            
            totalSent += bytesRead;
            session.updateProgress(bytesRead);
            
            // 输出进度
            if (totalSent % (CHUNK_SIZE * 10) == 0 || totalSent == session.getFileSize()) {
                double progress = session.getProgress() * 100;
                System.out.printf("[安全文件传输] 发送进度: %.1f%% (%d/%d bytes)%n", 
                    progress, totalSent, session.getFileSize());
            }
        }
        
        return totalSent;
    }
    
    /**
     * 接收加密文件数据
     */
    private TransferResult receiveEncryptedFileData(DataInputStream in, FileTransferHeader header, 
                                                  FileTransferSession session) {
        try {
            // 创建保存目录
            Path savePath = Paths.get(header.savePath);
            Files.createDirectories(savePath.getParent());
            
            try (FileOutputStream fileOut = new FileOutputStream(savePath.toFile())) {
                long totalReceived = 0;
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                
                while (totalReceived < header.fileSize) {
                    // 接收IV
                    int ivLength = in.readInt();
                    byte[] iv = new byte[ivLength];
                    in.readFully(iv);
                    
                    // 接收加密数据
                    int dataLength = in.readInt();
                    byte[] encryptedData = new byte[dataLength];
                    in.readFully(encryptedData);
                    
                    // 解密数据块
                    CryptoService.EncryptionResult encryptionResult = 
                        new CryptoService.EncryptionResult(encryptedData, iv);
                    byte[] decryptedChunk = cryptoService.decryptWithAESToBytes(
                        encryptionResult, session.getEncryptionKey());
                    
                    // 写入文件并更新哈希
                    fileOut.write(decryptedChunk);
                    digest.update(decryptedChunk);
                    
                    totalReceived += decryptedChunk.length;
                    session.updateProgress(decryptedChunk.length);
                    
                    // 输出进度
                    if (totalReceived % (CHUNK_SIZE * 10) == 0 || totalReceived == header.fileSize) {
                        double progress = session.getProgress() * 100;
                        System.out.printf("[安全文件传输] 接收进度: %.1f%% (%d/%d bytes)%n", 
                            progress, totalReceived, header.fileSize);
                    }
                }
                
                // 验证文件完整性
                String receivedHash = Base64.getEncoder().encodeToString(digest.digest());
                if (!receivedHash.equals(header.fileHash)) {
                    Files.deleteIfExists(savePath); // 删除损坏的文件
                    return new TransferResult(false, "文件哈希验证失败", totalReceived, 
                        System.currentTimeMillis() - session.getStartTime());
                }
                
                session.setCompleted(true);
                return new TransferResult(true, "文件接收成功", totalReceived, 
                    System.currentTimeMillis() - session.getStartTime());
            }
            
        } catch (Exception e) {
            return new TransferResult(false, "接收失败: " + e.getMessage(), 
                session.getTransferredBytes(), System.currentTimeMillis() - session.getStartTime());
        }
    }
    
    /**
     * 计算文件哈希
     */
    private String calculateFileHash(Path filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        
        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        
        return Base64.getEncoder().encodeToString(digest.digest());
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        try {
            byte[] randomBytes = cryptoService.generateRandomBytes(16);
            return Base64.getEncoder().encodeToString(randomBytes).substring(0, 22);
        } catch (Exception e) {
            return "session_" + System.currentTimeMillis();
        }
    }
    
    /**
     * 获取节点地址（这里需要与现有系统集成）
     */
    private String getNodeAddress(String nodeId) {
        // 这里需要与现有的节点地址解析系统集成
        // 暂时返回localhost地址用于测试
        return "localhost:8080"; // 需要实际实现
    }
    
    /**
     * 获取活跃传输会话
     */
    public FileTransferSession getActiveSession(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    /**
     * 获取所有活跃会话
     */
    public java.util.Collection<FileTransferSession> getAllActiveSessions() {
        return activeSessions.values();
    }
    
    /**
     * 取消传输会话
     */
    public boolean cancelTransfer(String sessionId) {
        FileTransferSession session = activeSessions.remove(sessionId);
        if (session != null) {
            System.out.println("[安全文件传输] 取消传输会话: " + sessionId);
            return true;
        }
        return false;
    }
}
