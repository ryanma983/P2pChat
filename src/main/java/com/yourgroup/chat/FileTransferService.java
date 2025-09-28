package com.yourgroup.chat;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件传输服务类，负责实际的文件数据传输
 */
public class FileTransferService {
    private final Node node;
    private final ExecutorService transferExecutor;
    private final ConcurrentHashMap<String, FileTransferSession> activeSessions;
    private ServerSocket fileTransferServer;
    private final int fileTransferPort;
    private boolean running = false;
    
    public FileTransferService(Node node) {
        this.node = node;
        this.transferExecutor = Executors.newCachedThreadPool();
        this.activeSessions = new ConcurrentHashMap<>();
        this.fileTransferPort = node.getPort() + 1000; // 使用主端口+1000作为文件传输端口
    }
    
    /**
     * 启动文件传输服务
     */
    public void start() {
        try {
            fileTransferServer = new ServerSocket(fileTransferPort);
            running = true;
            
            // 启动文件传输服务器线程
            transferExecutor.submit(this::acceptFileTransferConnections);
            
            System.out.println("文件传输服务启动，端口: " + fileTransferPort);
        } catch (IOException e) {
            System.err.println("无法启动文件传输服务: " + e.getMessage());
        }
    }
    
    /**
     * 停止文件传输服务
     */
    public void stop() {
        running = false;
        try {
            if (fileTransferServer != null && !fileTransferServer.isClosed()) {
                fileTransferServer.close();
            }
            transferExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 接受文件传输连接
     */
    private void acceptFileTransferConnections() {
        while (running) {
            try {
                Socket clientSocket = fileTransferServer.accept();
                transferExecutor.submit(() -> handleFileTransferConnection(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("接受文件传输连接时发生错误: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 处理文件传输连接
     */
    private void handleFileTransferConnection(Socket socket) {
        try {
            System.out.println("[文件传输] 接受新的文件传输连接");
            
            // 获取输入流
            InputStream inputStream = socket.getInputStream();
            
            // 读取4字节的头长度
            byte[] lengthBytes = new byte[4];
            int bytesRead = 0;
            while (bytesRead < 4) {
                int read = inputStream.read(lengthBytes, bytesRead, 4 - bytesRead);
                if (read == -1) {
                    throw new IOException("连接意外关闭");
                }
                bytesRead += read;
            }
            
            // 解析头长度
            int headerLength = ((lengthBytes[0] & 0xFF) << 24) |
                              ((lengthBytes[1] & 0xFF) << 16) |
                              ((lengthBytes[2] & 0xFF) << 8) |
                              (lengthBytes[3] & 0xFF);
            
            System.out.println("[文件传输] 头信息长度: " + headerLength);
            
            // 读取头信息
            byte[] headerBytes = new byte[headerLength];
            bytesRead = 0;
            while (bytesRead < headerLength) {
                int read = inputStream.read(headerBytes, bytesRead, headerLength - bytesRead);
                if (read == -1) {
                    throw new IOException("连接意外关闭");
                }
                bytesRead += read;
            }
            
            String header = new String(headerBytes, "UTF-8");
            System.out.println("[文件传输] 收到传输头: " + header);
            
            String[] parts = header.split(":");
            if (parts.length >= 5) {
                String action = parts[0];
                String sessionId = parts[1];
                String fileName = parts[2];
                long fileSize = Long.parseLong(parts[3]);
                String savePath = parts[4];
                
                if ("SEND".equals(action)) {
                    // 接收文件，使用传输头中的保存路径
                    receiveFileWithBinaryProtocol(inputStream, sessionId, fileName, fileSize, savePath);
                }
            }
            
        } catch (Exception e) {
            System.err.println("处理文件传输连接时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // 忽略关闭异常
            }
        }
    }
    
    /**
     * 发送文件到指定节点
     */
    public void sendFile(String targetNodeId, File file, String savePath) {
        transferExecutor.submit(() -> {
            try {
                // 获取目标节点的地址
                String targetAddress = node.getNodeAddresses().get(targetNodeId);
                if (targetAddress == null) {
                    System.err.println("找不到目标节点地址: " + targetNodeId);
                    return;
                }
                
                String[] addressParts = targetAddress.split(":");
                String host = addressParts[0];
                int targetPort = Integer.parseInt(addressParts[1]) + 1000; // 文件传输端口
                
                String sessionId = generateSessionId();
                
                System.out.println("[文件传输] 开始发送文件到 " + targetNodeId + " (" + host + ":" + targetPort + ")");
                
                try (Socket socket = new Socket(host, targetPort);
                     FileInputStream fileInput = new FileInputStream(file)) {
                    
                    // 获取输出流
                    OutputStream outputStream = socket.getOutputStream();
                    
                    // 准备传输头
                    String header = String.format("SEND:%s:%s:%d:%s", sessionId, file.getName(), file.length(), savePath);
                    byte[] headerBytes = header.getBytes("UTF-8");
                    
                    // 发送头长度（4字节）
                    outputStream.write((headerBytes.length >> 24) & 0xFF);
                    outputStream.write((headerBytes.length >> 16) & 0xFF);
                    outputStream.write((headerBytes.length >> 8) & 0xFF);
                    outputStream.write(headerBytes.length & 0xFF);
                    
                    // 发送头信息
                    outputStream.write(headerBytes);
                    outputStream.flush();
                    
                    System.out.println("[文件传输] 发送头信息: " + header);
                    System.out.println("[文件传输] 头信息长度: " + headerBytes.length);
                    
                    // 发送文件数据
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalSent = 0;
                    
                    while ((bytesRead = fileInput.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalSent += bytesRead;
                        
                        // 显示进度
                        int progress = (int) ((totalSent * 100) / file.length());
                        if (totalSent % (8192 * 10) == 0 || totalSent == file.length()) { // 每传输约80KB或完成时显示进度
                            System.out.println("[文件传输] 发送进度: " + progress + "% (" + totalSent + "/" + file.length() + " bytes)");
                        }
                    }
                    
                    outputStream.flush();
                    System.out.println("[文件传输] 文件发送完成: " + file.getName() + " (" + totalSent + " bytes)");
                    
                    // 通知GUI
                    if (node.getMessageRouter().getMessageListener() != null) {
                        node.getMessageRouter().getMessageListener().onSystemMessage(
                            "文件发送完成: " + file.getName() + " → " + targetNodeId);
                    }
                    
                } catch (IOException e) {
                    System.err.println("发送文件失败: " + e.getMessage());
                    if (node.getMessageRouter().getMessageListener() != null) {
                        node.getMessageRouter().getMessageListener().onSystemMessage(
                            "文件发送失败: " + file.getName() + " - " + e.getMessage());
                    }
                }
                
            } catch (Exception e) {
                System.err.println("文件传输异常: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 使用二进制协议接收文件数据
     */
    private void receiveFileWithBinaryProtocol(InputStream inputStream, String sessionId, String fileName, long fileSize, String savePath) {
        try {
            System.out.println("[文件传输] 开始接收文件: " + fileName + " → " + savePath);
            System.out.println("[文件传输] 期望文件大小: " + fileSize + " bytes");
            
            // 创建目标文件
            File targetFile = new File(savePath);
            if (targetFile.getParentFile() != null) {
                targetFile.getParentFile().mkdirs();
            }
            
            try (FileOutputStream fileOutput = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalReceived = 0;
                
                // 直接从输入流读取文件数据
                while (totalReceived < fileSize) {
                    int remainingBytes = (int) Math.min(buffer.length, fileSize - totalReceived);
                    bytesRead = inputStream.read(buffer, 0, remainingBytes);
                    
                    if (bytesRead == -1) {
                        System.err.println("[文件传输] 连接意外关闭，已接收: " + totalReceived + "/" + fileSize + " bytes");
                        break;
                    }
                    
                    fileOutput.write(buffer, 0, bytesRead);
                    totalReceived += bytesRead;
                    
                    // 显示进度
                    int progress = (int) ((totalReceived * 100) / fileSize);
                    if (totalReceived % (8192 * 5) == 0 || totalReceived == fileSize) { // 每接收约40KB或完成时显示进度
                        System.out.println("[文件传输] 接收进度: " + progress + "% (" + totalReceived + "/" + fileSize + " bytes)");
                    }
                }
                
                fileOutput.flush();
                System.out.println("[文件传输] 文件接收完成: " + fileName + " (" + totalReceived + " bytes)");
                System.out.println("[文件传输] 保存位置: " + savePath);
                
                // 验证文件大小
                if (totalReceived != fileSize) {
                    System.err.println("[文件传输] 警告：接收的文件大小不匹配！期望: " + fileSize + ", 实际: " + totalReceived);
                } else {
                    System.out.println("[文件传输] 文件大小验证通过");
                }
                
                // 验证文件是否真的存在
                if (targetFile.exists() && targetFile.length() == totalReceived) {
                    System.out.println("[文件传输] 文件成功保存，大小: " + targetFile.length() + " bytes");
                } else {
                    System.err.println("[文件传输] 文件保存失败或大小不匹配");
                }
                
                // 通知GUI
                if (node.getMessageRouter().getMessageListener() != null) {
                    node.getMessageRouter().getMessageListener().onSystemMessage(
                        "文件接收完成: " + fileName + " (保存到: " + savePath + ")");
                }
                
                // 清理会话
                activeSessions.remove(sessionId);
                
            }
            
        } catch (Exception e) {
            System.err.println("接收文件失败: " + e.getMessage());
            e.printStackTrace();
            if (node.getMessageRouter().getMessageListener() != null) {
                node.getMessageRouter().getMessageListener().onSystemMessage(
                    "文件接收失败: " + fileName + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * 创建文件传输会话
     */
    public void createTransferSession(String sessionId, String fileName, String savePath) {
        FileTransferSession session = new FileTransferSession(sessionId, fileName, savePath);
        activeSessions.put(sessionId, session);
        System.out.println("[文件传输] 创建传输会话: " + sessionId + " → " + savePath);
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "transfer_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 获取文件传输端口
     */
    public int getFileTransferPort() {
        return fileTransferPort;
    }
    
    /**
     * 文件传输会话类
     */
    private static class FileTransferSession {
        private final String sessionId;
        private final String fileName;
        private final String savePath;
        
        public FileTransferSession(String sessionId, String fileName, String savePath) {
            this.sessionId = sessionId;
            this.fileName = fileName;
            this.savePath = savePath;
        }
        
        public String getSessionId() { return sessionId; }
        public String getFileName() { return fileName; }
        public String getSavePath() { return savePath; }
    }
}
