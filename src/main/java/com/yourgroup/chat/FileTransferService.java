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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             InputStream fileInput = socket.getInputStream();
             OutputStream fileOutput = socket.getOutputStream()) {
            
            // 读取传输头信息
            String header = reader.readLine();
            System.out.println("[文件传输] 收到传输头: " + header);
            
            String[] parts = header.split(":");
            if (parts.length >= 4) {
                String action = parts[0];
                String sessionId = parts[1];
                String fileName = parts[2];
                long fileSize = Long.parseLong(parts[3]);
                
                if ("SEND".equals(action)) {
                    // 接收文件
                    receiveFile(socket, sessionId, fileName, fileSize);
                }
            }
            
        } catch (IOException e) {
            System.err.println("处理文件传输连接时发生错误: " + e.getMessage());
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
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                     FileInputStream fileInput = new FileInputStream(file)) {
                    
                    // 发送传输头
                    String header = String.format("SEND:%s:%s:%d:%s", sessionId, file.getName(), file.length(), savePath);
                    writer.write(header);
                    writer.newLine();
                    writer.flush();
                    
                    // 发送文件数据
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalSent = 0;
                    
                    while ((bytesRead = fileInput.read(buffer)) != -1) {
                        socket.getOutputStream().write(buffer, 0, bytesRead);
                        totalSent += bytesRead;
                        
                        // 显示进度
                        int progress = (int) ((totalSent * 100) / file.length());
                        if (totalSent % (8192 * 10) == 0) { // 每传输约80KB显示一次进度
                            System.out.println("[文件传输] 进度: " + progress + "% (" + totalSent + "/" + file.length() + " bytes)");
                        }
                    }
                    
                    socket.getOutputStream().flush();
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
     * 接收文件
     */
    private void receiveFile(Socket socket, String sessionId, String fileName, long fileSize) {
        try {
            // 从会话中获取保存路径
            FileTransferSession session = activeSessions.get(sessionId);
            String savePath;
            if (session != null) {
                savePath = session.getSavePath();
            } else {
                // 如果没有会话信息，使用默认路径
                String userHome = System.getProperty("user.home");
                String defaultDownloadDir = userHome + File.separator + "P2PChat_Downloads";
                new File(defaultDownloadDir).mkdirs();
                savePath = defaultDownloadDir + File.separator + fileName;
            }
            
            System.out.println("[文件传输] 开始接收文件: " + fileName + " → " + savePath);
            
            // 跳过HTTP头部分，直接读取文件数据
            InputStream inputStream = socket.getInputStream();
            
            // 先读取并跳过头信息行
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // 跳过头信息
            
            // 创建目标文件
            File targetFile = new File(savePath);
            targetFile.getParentFile().mkdirs();
            
            try (FileOutputStream fileOutput = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalReceived = 0;
                
                while (totalReceived < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {
                    int bytesToWrite = (int) Math.min(bytesRead, fileSize - totalReceived);
                    fileOutput.write(buffer, 0, bytesToWrite);
                    totalReceived += bytesToWrite;
                    
                    // 显示进度
                    int progress = (int) ((totalReceived * 100) / fileSize);
                    if (totalReceived % (8192 * 10) == 0) { // 每接收约80KB显示一次进度
                        System.out.println("[文件传输] 接收进度: " + progress + "% (" + totalReceived + "/" + fileSize + " bytes)");
                    }
                }
                
                fileOutput.flush();
                System.out.println("[文件传输] 文件接收完成: " + fileName + " (" + totalReceived + " bytes)");
                System.out.println("[文件传输] 保存位置: " + savePath);
                
                // 通知GUI
                if (node.getMessageRouter().getMessageListener() != null) {
                    node.getMessageRouter().getMessageListener().onSystemMessage(
                        "文件接收完成: " + fileName + " (保存到: " + savePath + ")");
                }
                
                // 清理会话
                activeSessions.remove(sessionId);
                
            }
            
        } catch (IOException e) {
            System.err.println("接收文件失败: " + e.getMessage());
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
