package com.group7.chat;

import com.group7.chat.security.SecurityManager;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * P2P聊天网络中的节点类 (已更新为支持Kademlia风格的路由)
 */
public class Node {
    // --- Kademlia 常量 ---
    public static final int K_VALUE = 20; // 每个K-桶的大小
    private static final int ID_LENGTH = 256; // 节点ID的位数 (SHA-256)

    // --- 节点核心属性 ---
    private final int port;
    private final BigInteger nodeId;
    private ServerSocket serverSocket;
    private boolean running = false;

    // --- Kademlia 路由表 ---
    private final List<Map<BigInteger, NodeInfo>> routingTable;

    // --- 连接和状态管理 ---
    private final Map<String, PeerConnection> connections = new ConcurrentHashMap<>();
    private final List<String> bootstrapPeers = new CopyOnWriteArrayList<>(); // 引导节点列表
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // --- 服务组件 ---
    private MessageRouter messageRouter;
    private FileTransferService fileTransferService;
    private SecurityManager securityManager;

    private final Map<String, File> pendingFiles = new ConcurrentHashMap<>();

    public static class NodeInfo {
        private final BigInteger nodeId;
        private final String host;
        private final int port;
        private long lastSeen;

        public NodeInfo(BigInteger nodeId, String host, int port) {
            this.nodeId = nodeId;
            this.host = host;
            this.port = port;
            this.lastSeen = System.currentTimeMillis();
        }

        public void updateLastSeen() {
            this.lastSeen = System.currentTimeMillis();
        }

        public BigInteger getNodeId() { return nodeId; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getAddress() { return host + ":" + port; }
        public long getLastSeen() { return lastSeen; }
    }

    public Node(int port) {
        this.port = port;
        this.nodeId = generateNodeId();
        this.messageRouter = new MessageRouter(this);
        this.fileTransferService = new FileTransferService(this);

        this.routingTable = new ArrayList<>(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            routingTable.add(new ConcurrentHashMap<>());
        }

        try {
            this.securityManager = new SecurityManager(nodeId.toString(16), port);
        } catch (Exception e) {
            System.err.println("安全管理器初始化失败: " + e.getMessage());
            this.securityManager = null;
        }

        System.out.println("节点创建完成，ID: " + nodeId.toString(16).substring(0, 12) + "..., 端口: " + port);
    }

    private BigInteger generateNodeId() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String randomData = System.currentTimeMillis() + "-" + UUID.randomUUID().toString();
            byte[] hash = md.digest(randomData.getBytes());
            return new BigInteger(1, hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法生成节点ID", e);
        }
    }

    private BigInteger getDistance(BigInteger id1, BigInteger id2) {
        return id1.xor(id2);
    }

    private int getBucketIndex(BigInteger targetNodeId) {
        BigInteger distance = getDistance(this.nodeId, targetNodeId);
        if (distance.equals(BigInteger.ZERO)) return 0;
        return ID_LENGTH - distance.bitLength();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("节点 " + getNodeIdString().substring(0, 8) + " 启动成功，监听端口: " + port);

            new Thread(this::acceptConnections).start();
            startMaintenanceTasks();
            fileTransferService.start();

            if (securityManager != null) {
                securityManager.start();
            }

            bootstrap();

        } catch (Exception e) {
            System.err.println("无法启动节点: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        scheduler.shutdownNow();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            if (fileTransferService != null) fileTransferService.stop();
            if (securityManager != null) securityManager.stop();
            for (PeerConnection connection : connections.values()) {
                connection.close();
            }
            connections.clear();
            System.out.println("节点 " + getNodeIdString().substring(0, 8) + " 已停止");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addBootstrapPeer(String address) {
        if (!bootstrapPeers.contains(address)) {
            bootstrapPeers.add(address);
        }
    }

    private void bootstrap() {
        scheduler.schedule(() -> {
            System.out.println("开始引导过程...");
            for (String peerAddress : bootstrapPeers) {
                connectToPeer(peerAddress);
            }
            // 发起对自己的FIND_NODE请求，以填充邻近的K-桶
            lookupNodes(this.nodeId);
        }, 1, TimeUnit.SECONDS);
    }

    public boolean connectToPeer(String address) {
        if (connections.containsKey(address)) return true;
        try {
            String[] parts = address.split(":");
            String host = parts[0];
            int peerPort = Integer.parseInt(parts[1]);

            if (host.equals("localhost") && peerPort == this.port) return false;

            Socket socket = new Socket(host, peerPort);
            PeerConnection connection = new PeerConnection(socket, address, false);
            connections.put(address, connection);

            new Thread(() -> handlePeerConnection(connection)).start();
            System.out.println("成功连接到节点: " + address);

            Message helloMessage = new Message(Message.Type.HELLO, getNodeIdString(), getAddress());
            connection.sendMessage(helloMessage.serialize());

            return true;
        } catch (Exception e) {
            System.err.println("连接到节点 " + address + " 失败: " + e.getMessage());
            connections.remove(address);
            return false;
        }
    }

    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                String remoteAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                PeerConnection connection = new PeerConnection(clientSocket, remoteAddress, true);
                connections.put(remoteAddress, connection);
                new Thread(() -> handlePeerConnection(connection)).start();
            } catch (IOException e) {
                if (running) System.err.println("接受连接时发生错误: " + e.getMessage());
            }
        }
    }

    private void handlePeerConnection(PeerConnection connection) {
        try {
            String line;
            while ((line = connection.readMessage()) != null) {
                try {
                    Message message = Message.deserialize(line);
                    messageRouter.handleMessage(connection, message);
                } catch (IllegalArgumentException e) {
                    System.err.println("收到无效消息格式: " + line);
                }
            }
        } catch (IOException e) {
            // 连接断开
        } finally {
            System.out.println("与节点 " + connection.getAddress() + " 的连接断开");
            connections.remove(connection.getAddress());
            // 注意：不在这里从路由表移除节点，而是通过PING失败来确认节点下线
        }
    }

    public void updateRoutingTable(NodeInfo nodeInfo) {
        if (nodeInfo.getNodeId().equals(this.nodeId)) return;

        int bucketIndex = getBucketIndex(nodeInfo.getNodeId());
        Map<BigInteger, NodeInfo> bucket = routingTable.get(bucketIndex);

        if (bucket.containsKey(nodeInfo.getNodeId())) {
            bucket.get(nodeInfo.getNodeId()).updateLastSeen();
        } else if (bucket.size() < K_VALUE) {
            bucket.put(nodeInfo.getNodeId(), nodeInfo);
        } else {
            // 桶已满，尝试PING最旧的节点
            NodeInfo oldestNode = bucket.values().stream().min(Comparator.comparingLong(NodeInfo::getLastSeen)).orElse(null);
            if (oldestNode != null) {
                sendPing(oldestNode, (isAlive) -> {
                    if (!isAlive) {
                        bucket.remove(oldestNode.getNodeId());
                        bucket.put(nodeInfo.getNodeId(), nodeInfo);
                    }
                });
            }
        }
    }

    public void lookupNodes(BigInteger targetId) {
        System.out.println("开始为目标 " + targetId.toString(16).substring(0, 8) + " 查找节点...");
        List<NodeInfo> closest = findClosestNodes(targetId, K_VALUE);
        for (NodeInfo info : closest) {
            PeerConnection conn = getOrCreateConnection(info);
            if (conn != null) {
                Message findNodeMsg = new Message(Message.Type.FIND_NODE, getNodeIdString(), "", info.getNodeId().toString(16));
                conn.sendMessage(findNodeMsg.serialize());
            }
        }
    }

    private void startMaintenanceTasks() {
        // 定期PING路由表中的节点以检查其健康状况
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("执行路由表维护任务...");
            for (Map<BigInteger, NodeInfo> bucket : routingTable) {
                for (NodeInfo info : bucket.values()) {
                    sendPing(info, (isAlive) -> {
                        if (!isAlive) {
                            bucket.remove(info.getNodeId());
                            System.out.println("节点 " + info.getNodeId().toString(16).substring(0, 8) + " 无响应，已从路由表移除");
                        }
                    });
                }
            }
        }, 1, 5, TimeUnit.MINUTES); // 每5分钟执行一次

        // 定期刷新K-桶，特别是那些很久没有变化的
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("执行K-桶刷新任务...");
            for (int i = 0; i < ID_LENGTH; i++) {
                // 对每个桶生成一个随机ID并发起查找
                BigInteger randomIdInBucket = generateRandomIdInBucket(i);
                lookupNodes(randomIdInBucket);
            }
        }, 10, 15, TimeUnit.MINUTES); // 每15分钟执行一次
    }

    private void sendPing(NodeInfo target, java.util.function.Consumer<Boolean> callback) {
        PeerConnection conn = getOrCreateConnection(target);
        if (conn != null) {
            Message pingMsg = new Message(Message.Type.PING, getNodeIdString(), "ping", target.getNodeId().toString(16));
            conn.sendMessage(pingMsg.serialize());
            // 简单实现：假设如果在一定时间内没有收到PONG，则认为节点离线
            // 一个更健壮的实现需要一个回调管理器
            scheduler.schedule(() -> callback.accept(false), 5, TimeUnit.SECONDS); // 5秒超时
        } else {
            callback.accept(false);
        }
    }

    private PeerConnection getOrCreateConnection(NodeInfo info) {
        if (connections.containsKey(info.getAddress())) {
            return connections.get(info.getAddress());
        } else {
            if (connectToPeer(info.getAddress())) {
                return connections.get(info.getAddress());
            }
        }
        return null;
    }

    private BigInteger generateRandomIdInBucket(int bucketIndex) {
        BigInteger base = BigInteger.ONE.shiftLeft(ID_LENGTH - 1 - bucketIndex);
        BigInteger randomComponent = new BigInteger(ID_LENGTH - 1 - bucketIndex, new Random());
        return this.nodeId.xor(base.or(randomComponent));
    }

    // --- 原有功能 (适配后) ---
    public void sendChatMessage(String message) {
        Message chatMessage = new Message(Message.Type.CHAT, getNodeIdString(), message);
        messageRouter.broadcastMessage(chatMessage);
    }

    public void sendPrivateMessage(String targetNodeId, String message) {
        Message privateMessage = new Message(Message.Type.PRIVATE_CHAT, getNodeIdString(), message, targetNodeId);
        messageRouter.handleMessage(null, privateMessage);
    }

    public void sendFileRequest(String targetNodeId, File file) {
        pendingFiles.put(file.getName(), file);
        String fileInfo = file.getName() + ":" + file.length();
        Message fileRequest = new Message(Message.Type.FILE_REQUEST, getNodeIdString(), fileInfo, targetNodeId);
        messageRouter.broadcastMessage(fileRequest);
    }

    // --- Getters and Setters ---
    public String getNodeIdString() { return nodeId.toString(16); }
    public BigInteger getNodeId() { return nodeId; }
    public String getAddress() { return "localhost:" + port; }
    public Map<String, PeerConnection> getConnections() { return connections; }
    public void setMessageListener(MessageListener listener) { messageRouter.setMessageListener(listener); }

    public List<NodeInfo> findClosestNodes(BigInteger targetId, int count) {
        List<NodeInfo> allNodes = new ArrayList<>();
        for (Map<BigInteger, NodeInfo> bucket : routingTable) {
            allNodes.addAll(bucket.values());
        }
        allNodes.sort(Comparator.comparing(node -> getDistance(node.getNodeId(), targetId)));
        return allNodes.subList(0, Math.min(allNodes.size(), count));
    }

    public int getPort() { return port; }
    public MessageRouter getMessageRouter() { return messageRouter; }
    public FileTransferService getFileTransferService() { return fileTransferService; }
    public File getPendingFile(String fileName) { return pendingFiles.get(fileName); }
    public void removePendingFile(String fileName) { pendingFiles.remove(fileName); }
    public SecurityManager getSecurityManager() { return securityManager; }
    public boolean isSecurityEnabled() { return securityManager != null && securityManager.isSecurityEnabled(); }
    
    // 兼容性方法 - 为了保持与旧代码的兼容性
    public void addKnownPeer(String peerAddress) {
        bootstrapPeers.add(peerAddress);
        connectToPeer(peerAddress);
    }
    
    public int getConnectionCount() {
        return connections.size();
    }
    
    public void sendGroupFileRequest(File file) {
        if (fileTransferService != null) {
            fileTransferService.sendFileToAll(file);
        }
    }
    
    public void acceptFileTransfer(String senderId, String fileName, String savePath) {
        if (fileTransferService != null) {
            fileTransferService.acceptFileTransfer(senderId, fileName, savePath);
        }
    }
    
    public void rejectFileTransfer(String senderId, String fileName) {
        if (fileTransferService != null) {
            fileTransferService.rejectFileTransfer(senderId, fileName);
        }
    }
    
    public List<String> getNodeAddresses() {
        List<String> addresses = new ArrayList<>();
        for (PeerConnection conn : connections.values()) {
            addresses.add(conn.getRemoteAddress());
        }
        return addresses;
    }
}

