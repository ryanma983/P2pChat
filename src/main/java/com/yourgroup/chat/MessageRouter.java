package com.yourgroup.chat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 消息路由器，负责处理消息的转发和洪泛传播
 */
public class MessageRouter {
    private final Node node;
    private final Set<String> processedMessages = new CopyOnWriteArraySet<>();
    private final Map<String, Long> messageTimestamps = new ConcurrentHashMap<>();
    private MessageListener messageListener;
    
    // 清理过期消息记录的时间间隔（毫秒）
    private static final long CLEANUP_INTERVAL = 300000; // 5分钟
    private static final long MESSAGE_EXPIRE_TIME = 600000; // 10分钟
    
    public MessageRouter(Node node) {
        this.node = node;
        // 启动定期清理线程
        startCleanupTask();
    }
    
    /**
     * 设置消息监听器
     */
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }
    
    /**
     * 处理接收到的消息
     */
    public void handleMessage(PeerConnection source, Message message) {
        // 检查是否已经处理过这条消息
        if (isMessageProcessed(message)) {
            return; // 忽略重复消息
        }
        
        // 标记消息为已处理
        markMessageAsProcessed(message);
        
        // 根据消息类型进行处理
        switch (message.getType()) {
            case HELLO:
                handleHelloMessage(source, message);
                break;
            case CHAT:
                handleChatMessage(source, message);
                break;
            case PRIVATE_CHAT:
                handlePrivateChatMessage(source, message);
                break;
            case FILE_REQUEST:
                handleFileTransferRequest(source, message);
                break;
            case PEER_LIST:
                handlePeerListMessage(source, message);
                break;
            case PING:
                handlePingMessage(source, message);
                break;
            case PONG:
                handlePongMessage(source, message);
                break;
        }
        
        // 如果消息还可以转发，则转发给其他节点
        if (message.canForward()) {
            forwardMessage(source, message);
        }
    }
    
    /**
     * 广播消息到网络（仅转发，不本地处理）
     */
    public void broadcastMessage(Message message) {
        // 标记消息为已处理
        markMessageAsProcessed(message);
        
        // 转发给其他节点
        forwardMessage(null, message);
    }
    
    /**
     * 转发消息给其他节点（除了源节点）
     */
    private void forwardMessage(PeerConnection source, Message message) {
        Message forwardMessage = message.createForwardCopy();
        String serialized = forwardMessage.serialize();
        
        for (PeerConnection connection : node.getConnections().values()) {
            // 不要将消息发回给发送者
            if (connection != source && connection.isConnected()) {
                connection.sendMessage(serialized);
            }
        }
    }
    
    /**
     * 处理握手消息
     */
    private void handleHelloMessage(PeerConnection source, Message message) {
        System.out.println("[调试] 收到握手消息，来自节点: " + message.getSenderId() + ", 地址: " + source.getAddress());
        
        // 解析握手消息中的节点地址信息
        String content = message.getContent();
        if (content.contains(":")) {
            String[] parts = content.split(":");
            if (parts.length >= 2) {
                String nodeAddress = "localhost:" + parts[parts.length - 1]; // 提取端口号
                node.addNodeAddress(message.getSenderId(), nodeAddress);
                System.out.println("[调试] 解析节点地址: " + message.getSenderId() + " -> " + nodeAddress);
            }
        }
        
        // 通知GUI有新成员加入
        if (messageListener != null) {
            System.out.println("[调试] 通知GUI添加成员: " + message.getSenderId());
            messageListener.onMemberJoined(message.getSenderId(), source.getAddress());
        } else {
            System.out.println("[调试] messageListener 为 null，无法通知GUI");
        }
        
        // 如果这是入站连接，回复握手消息
        if (source.isInbound()) {
            System.out.println("[调试] 这是入站连接，回复握手消息");
            Message replyHello = new Message(Message.Type.HELLO, node.getNodeId(), node.getNodeId() + ":" + node.getPort());
            source.sendMessage(replyHello.serialize());
        }
        
        // 发送自己的邻居列表给新连接的节点
        sendPeerList(source);
    }
    
    /**
     * 处理聊天消息
     */
    private void handleChatMessage(PeerConnection source, Message message) {
        System.out.println(String.format("[群聊] %s: %s", 
            message.getSenderId(), message.getContent()));
        
        // 通知GUI界面
        if (messageListener != null) {
            messageListener.onChatMessageReceived(message.getSenderId(), message.getContent());
        }
    }
    
    /**
     * 处理私聊消息
     */
    private void handlePrivateChatMessage(PeerConnection source, Message message) {
        System.out.println(String.format("[调试] 收到私聊消息 - 发送者: %s, 目标: %s, 自己: %s", 
            message.getSenderId(), message.getTargetId(), node.getNodeId()));
        
        // 检查消息是否是发给自己的
        if (message.getTargetId() != null && message.getTargetId().equals(node.getNodeId())) {
            System.out.println(String.format("[私聊] %s: %s", 
                message.getSenderId(), message.getContent()));
            
            // 通知GUI界面
            if (messageListener != null) {
                messageListener.onPrivateChatMessageReceived(message.getSenderId(), message.getContent());
            }
        } else {
            System.out.println("[调试] 私聊消息不是发给自己的，将转发");
        }
        // 如果不是发给自己的，继续转发（在forwardMessage中处理）
    }
    
    /**
     * 处理文件传输请求
     */
    private void handleFileTransferRequest(PeerConnection source, Message message) {
        // 解析文件信息
        String[] parts = message.getContent().split("\\|");
        if (parts.length >= 2) {
            String fileName = parts[0];
            long fileSize = Long.parseLong(parts[1]);
            
            System.out.println(String.format("[文件传输] %s 请求发送文件: %s (%d bytes)", 
                message.getSenderId(), fileName, fileSize));
            
            // 通知GUI界面
            if (messageListener != null) {
                messageListener.onFileTransferRequest(message.getSenderId(), fileName, fileSize);
            }
        }
    }
    
    /**
     * 处理邻居列表消息
     */
    private void handlePeerListMessage(PeerConnection source, Message message) {
        String[] peers = message.getContent().split(",");
        System.out.println("[调试] 收到邻居列表，包含 " + peers.length + " 个节点: " + message.getContent());
        
        for (String peer : peers) {
            String trimmedPeer = peer.trim();
            if (!trimmedPeer.isEmpty() && !trimmedPeer.equals("localhost:" + node.getPort())) {
                // 尝试连接到新发现的节点
                if (!node.getConnections().containsKey(trimmedPeer)) {
                    System.out.println("[调试] 发现新节点，尝试连接: " + trimmedPeer);
                    // 延迟一点时间再连接，避免连接冲突
                    final String finalPeer = trimmedPeer;
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000); // 等待1秒
                            node.connectToPeer(finalPeer);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                } else {
                    System.out.println("[调试] 节点 " + trimmedPeer + " 已经连接");
                }
            } else {
                System.out.println("[调试] 跳过节点: " + trimmedPeer + " (空或自己)");
            }
        }
    }
    
    /**
     * 处理心跳检测消息
     */
    private void handlePingMessage(PeerConnection source, Message message) {
        // 回复PONG消息
        Message pongMessage = new Message(Message.Type.PONG, node.getNodeId(), "pong");
        source.sendMessage(pongMessage.serialize());
    }
    
    /**
     * 处理心跳响应消息
     */
    private void handlePongMessage(PeerConnection source, Message message) {
        // 更新连接的最后活跃时间
        source.updateLastActivity();
    }
    
    /**
     * 发送邻居列表给指定连接
     */
    private void sendPeerList(PeerConnection target) {
        // 使用节点地址映射而不是连接地址
        Collection<String> nodeAddresses = node.getNodeAddresses().values();
        String peerList = String.join(",", nodeAddresses);
        
        System.out.println("[调试] 发送邻居列表给 " + target.getAddress() + ": " + peerList);
        
        Message peerListMessage = new Message(Message.Type.PEER_LIST, node.getNodeId(), peerList);
        target.sendMessage(peerListMessage.serialize());
    }
    
    /**
     * 检查消息是否已经处理过
     */
    private boolean isMessageProcessed(Message message) {
        return processedMessages.contains(message.getMessageId());
    }
    
    /**
     * 标记消息为已处理
     */
    private void markMessageAsProcessed(Message message) {
        processedMessages.add(message.getMessageId());
        messageTimestamps.put(message.getMessageId(), System.currentTimeMillis());
    }
    
    /**
     * 启动定期清理任务
     */
    private void startCleanupTask() {
        Timer cleanupTimer = new Timer("MessageRouter-Cleanup", true);
        cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredMessages();
            }
        }, CLEANUP_INTERVAL, CLEANUP_INTERVAL);
    }
    
    /**
     * 清理过期的消息记录
     */
    private void cleanupExpiredMessages() {
        long currentTime = System.currentTimeMillis();
        Set<String> expiredMessages = new HashSet<>();
        
        for (Map.Entry<String, Long> entry : messageTimestamps.entrySet()) {
            if (currentTime - entry.getValue() > MESSAGE_EXPIRE_TIME) {
                expiredMessages.add(entry.getKey());
            }
        }
        
        for (String messageId : expiredMessages) {
            processedMessages.remove(messageId);
            messageTimestamps.remove(messageId);
        }
        
        if (!expiredMessages.isEmpty()) {
            System.out.println("清理了 " + expiredMessages.size() + " 条过期消息记录");
        }
    }
    
    /**
     * 获取消息监听器
     */
    public MessageListener getMessageListener() {
        return messageListener;
    }
    
    /**
     * 发送心跳检测到所有连接
     */
    public void sendHeartbeat() {
        Message pingMessage = new Message(Message.Type.PING, node.getNodeId(), "ping");
        broadcastMessage(pingMessage);
    }
}
