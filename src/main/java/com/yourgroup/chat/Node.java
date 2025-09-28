package com.yourgroup.chat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * P2P聊天网络中的节点类
 * 每个节点既可以作为服务器接受连接，也可以作为客户端连接到其他节点
 */
public class Node {
    private final int port;
    private final String nodeId;
    private ServerSocket serverSocket;
    private boolean running = false;
    
    // 存储与其他节点的连接
    private final Map<String, PeerConnection> connections = new ConcurrentHashMap<>();
    
    // 已知的其他节点地址列表
    private final List<String> knownPeers = new CopyOnWriteArrayList<>();
    
    // 消息路由器
    private MessageRouter messageRouter;
    
    public Node(int port) {
        this.port = port;
        this.nodeId = generateNodeId();
        this.messageRouter = new MessageRouter(this);
        System.out.println("节点创建完成，ID: " + nodeId + ", 端口: " + port);
    }
    
    /**
     * 生成唯一的节点ID
     */
    private String generateNodeId() {
        return "Node-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    /**
     * 启动节点，开始监听连接
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("节点 " + nodeId + " 启动成功，监听端口: " + port);
            
            // 启动服务器线程，接受新连接
            new Thread(this::acceptConnections).start();
            
            // 启动心跳检测线程
            startHeartbeatTask();
            
            // 尝试连接到已知的节点
            connectToKnownPeers();
            
        } catch (IOException e) {
            System.err.println("无法启动节点: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 停止节点
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            // 关闭所有连接
            for (PeerConnection connection : connections.values()) {
                connection.close();
            }
            connections.clear();
            System.out.println("节点 " + nodeId + " 已停止");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 添加已知节点地址
     */
    public void addKnownPeer(String address) {
        if (!knownPeers.contains(address)) {
            knownPeers.add(address);
            System.out.println("添加已知节点: " + address);
        }
    }
    
    /**
     * 连接到指定的节点
     */
    public boolean connectToPeer(String address) {
        try {
            String[] parts = address.split(":");
            if (parts.length != 2) {
                System.err.println("无效的节点地址格式: " + address + " (应该是 host:port)");
                return false;
            }
            
            String host = parts[0];
            int peerPort = Integer.parseInt(parts[1]);
            
            // 避免连接到自己
            if (host.equals("localhost") && peerPort == this.port) {
                return false;
            }
            
            // 检查是否已经连接
            if (connections.containsKey(address)) {
                System.out.println("已经连接到节点: " + address);
                return true;
            }
            
            Socket socket = new Socket(host, peerPort);
            PeerConnection connection = new PeerConnection(socket, address, false);
            connections.put(address, connection);
            
            // 启动连接处理线程
            new Thread(() -> handlePeerConnection(connection)).start();
            
            System.out.println("成功连接到节点: " + address);
            
            // 发送握手消息
            Message helloMessage = new Message(Message.Type.HELLO, nodeId, nodeId + ":" + port);
            connection.sendMessage(helloMessage.serialize());
            
            // 通知GUI有新成员加入（对于出站连接）
            if (messageRouter.getMessageListener() != null) {
                // 从地址中提取节点ID（这里简化处理，实际应该等待对方的握手回复）
                String remoteNodeId = "Node-" + address.replace(":", "-");
                messageRouter.getMessageListener().onMemberJoined(remoteNodeId, address);
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("连接到节点 " + address + " 失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 连接到所有已知节点
     */
    private void connectToKnownPeers() {
        for (String peer : knownPeers) {
            connectToPeer(peer);
        }
    }
    
    /**
     * 接受新的连接
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                String remoteAddress = clientSocket.getRemoteSocketAddress().toString();
                System.out.println("接收到新连接: " + remoteAddress);
                
                PeerConnection connection = new PeerConnection(clientSocket, remoteAddress, true);
                
                // 启动连接处理线程
                new Thread(() -> handlePeerConnection(connection)).start();
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("接受连接时发生错误: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 处理与对等节点的连接
     */
    private void handlePeerConnection(PeerConnection connection) {
        try {
            // 将连接添加到连接列表（对于入站连接）
            if (connection.isInbound()) {
                connections.put(connection.getAddress(), connection);
                System.out.println("入站连接已添加: " + connection.getAddress());
            }
            
            String line;
            while ((line = connection.readMessage()) != null) {
                connection.updateLastActivity();
                try {
                    Message message = Message.deserialize(line);
                    messageRouter.handleMessage(connection, message);
                } catch (IllegalArgumentException e) {
                    System.err.println("收到无效消息格式: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("与节点 " + connection.getAddress() + " 的连接断开");
            
            // 通知GUI成员离开
            if (messageRouter.getMessageListener() != null) {
                String nodeId = "Node-" + connection.getAddress().replace(":", "-");
                messageRouter.getMessageListener().onMemberLeft(nodeId);
            }
        } finally {
            connections.remove(connection.getAddress());
            connection.close();
        }
    }
    
    /**
     * 发送群聊消息到所有连接的节点
     */
    public void sendChatMessage(String message) {
        Message chatMessage = new Message(Message.Type.CHAT, nodeId, message);
        messageRouter.broadcastMessage(chatMessage);
        System.out.println("群聊消息已广播到网络");
    }
    
    /**
     * 发送私聊消息到指定节点
     */
    public void sendPrivateMessage(String targetNodeId, String message) {
        Message privateMessage = new Message(Message.Type.PRIVATE_CHAT, nodeId, message, targetNodeId);
        
        // 直接处理消息（包括本地处理和转发）
        messageRouter.handleMessage(null, privateMessage);
        System.out.println("私聊消息已发送给: " + targetNodeId);
    }
    
    /**
     * 发送文件传输请求
     */
    public void sendFileRequest(String targetNodeId, java.io.File file) {
        String fileInfo = file.getName() + "|" + file.length();
        Message fileRequest = new Message(Message.Type.FILE_REQUEST, nodeId, fileInfo, targetNodeId);
        messageRouter.broadcastMessage(fileRequest);
        System.out.println("文件传输请求已发送给: " + targetNodeId + ", 文件: " + file.getName());
    }
    
    /**
     * 获取当前连接的节点数量
     */
    public int getConnectionCount() {
        return connections.size();
    }
    
    /**
     * 获取节点ID
     */
    public String getNodeId() {
        return nodeId;
    }
    
    /**
     * 获取监听端口
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 获取连接映射（用于MessageRouter）
     */
    public Map<String, PeerConnection> getConnections() {
        return connections;
    }
    
    /**
     * 设置消息监听器
     */
    public void setMessageListener(MessageListener listener) {
        messageRouter.setMessageListener(listener);
    }
    
    /**
     * 启动心跳检测任务
     */
    private void startHeartbeatTask() {
        Timer heartbeatTimer = new Timer("Heartbeat-" + nodeId, true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (running) {
                    // 发送心跳
                    messageRouter.sendHeartbeat();
                    
                    // 检查超时连接
                    checkTimeoutConnections();
                }
            }
        }, 30000, 30000); // 每30秒执行一次
    }
    
    /**
     * 检查并清理超时的连接
     */
    private void checkTimeoutConnections() {
        long timeoutMs = 120000; // 2分钟超时
        List<String> timeoutConnections = new ArrayList<>();
        
        for (Map.Entry<String, PeerConnection> entry : connections.entrySet()) {
            if (entry.getValue().isTimeout(timeoutMs)) {
                timeoutConnections.add(entry.getKey());
            }
        }
        
        for (String address : timeoutConnections) {
            System.out.println("连接超时，断开: " + address);
            PeerConnection connection = connections.remove(address);
            if (connection != null) {
                connection.close();
            }
        }
    }
}
