package com.group7.chat;

import com.group7.chat.Node.NodeInfo;
import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 消息路由器，负责处理DOP协议消息和应用层消息的路由
 */
public class MessageRouter {
    private final Node node;
    private final Set<String> processedMessages = new CopyOnWriteArraySet<>();
    private final Map<String, Long> messageTimestamps = new ConcurrentHashMap<>();
    private MessageListener messageListener;

    private static final long CLEANUP_INTERVAL = 300000; // 5分钟
    private static final long MESSAGE_EXPIRE_TIME = 600000; // 10分钟

    public MessageRouter(Node node) {
        this.node = node;
        startCleanupTask();
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    /**
     * 处理接收到的所有消息
     */
    public void handleMessage(PeerConnection source, Message message) {
        if (isMessageProcessed(message)) {
            return; // 忽略重复消息
        }
        markMessageAsProcessed(message);

        // 更新发送方节点的路由信息
        updateSenderNodeInfo(message);

        switch (message.getType()) {
            // --- DOP 协议消息 ---
            case PING:
                handlePingMessage(source, message);
                break;
            case PONG:
                handlePongMessage(source, message);
                break;
            case FIND_NODE:
                handleFindNodeMessage(source, message);
                break;
            case NEIGHBORS:
                handleNeighborsMessage(source, message);
                break;

            // --- 应用层消息 ---
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
                // 文件请求也需要路由
                routeAppMessage(source, message);
                break;
            case FILE_TRANSFER:
                // 文件传输响应也需要路由
                routeAppMessage(source, message);
                break;

            default:
                // 对于其他应用层消息，统一进行路由
                routeAppMessage(source, message);
        }
    }

    /**
     * 路由应用层消息 (如 CHAT, PRIVATE_CHAT)
     */
    private void routeAppMessage(PeerConnection source, Message message) {
        // 检查消息是否是发给自己的
        if (message.getTargetId() != null && message.getTargetId().equals(node.getNodeIdString())) {
            // 是发给我的，本地处理
            processLocalAppMessage(message);
        } else {
            // 不是发给我的，或者需要广播，进行转发
            if (message.canForward()) {
                forwardMessage(source, message);
            }
        }
    }

    /**
     * 本地处理应用层消息
     */
    private void processLocalAppMessage(Message message) {
        if (messageListener == null) return;

        switch (message.getType()) {
            case PRIVATE_CHAT:
                messageListener.onPrivateChatMessageReceived(message.getSenderId(), message.getContent());
                break;
            case FILE_REQUEST:
                handleFileTransferRequest(null, message);
                break;
            case FILE_TRANSFER:
                handleFileTransferResponse(null, message);
                break;
            // 其他需要本地处理的消息
        }
    }

    /**
     * 广播消息到网络（用于自己发起的群聊等）
     */
    public void broadcastMessage(Message message) {
        markMessageAsProcessed(message);
        forwardMessage(null, message);
    }

    /**
     * 转发消息 (Kademlia风格)
     */
    private void forwardMessage(PeerConnection source, Message message) {
        Message forwardMessage = message.createForwardCopy();
        String serialized = forwardMessage.serialize();

        // 如果是广播或群聊消息，发送给所有邻居
        if (message.getTargetId() == null) {
            for (PeerConnection connection : node.getConnections().values()) {
                if (connection != source && connection.isConnected()) {
                    connection.sendMessage(serialized);
                }
            }
        } else {
            // 如果是私聊或单播消息，查找最近的节点进行转发
            BigInteger targetNodeId = new BigInteger(message.getTargetId(), 16);
            List<NodeInfo> closestNodes = node.findClosestNodes(targetNodeId, 3); // 转发给最近的3个节点

            for (NodeInfo info : closestNodes) {
                PeerConnection connection = node.getConnections().get(info.getAddress());
                if (connection != null && connection != source && connection.isConnected()) {
                    connection.sendMessage(serialized);
                }
            }
        }
    }

    // --- DOP 消息处理器 ---

    private void handlePingMessage(PeerConnection source, Message message) {
        // 回复PONG消息
        Message pongMessage = new Message(Message.Type.PONG, node.getNodeIdString(), "pong");
        source.sendMessage(pongMessage.serialize());
    }

    private void handlePongMessage(PeerConnection source, Message message) {
        // PONG消息确认对方在线，其信息已在 handleMessage 开始时通过 updateSenderNodeInfo 更新
        System.out.println("收到来自 " + message.getSenderId().substring(0, 8) + " 的 PONG");
    }

    private void handleFindNodeMessage(PeerConnection source, Message message) {
        String targetIdStr = message.getTargetId();
        if (targetIdStr == null) return;

        System.out.println("收到来自 " + message.getSenderId().substring(0, 8) + " 的 FIND_NODE 请求");

        BigInteger targetId = new BigInteger(targetIdStr, 16);
        List<NodeInfo> closestNodes = node.findClosestNodes(targetId, Node.K_VALUE);

        // 将节点信息序列化为 Content
        StringBuilder contentBuilder = new StringBuilder();
        for (NodeInfo info : closestNodes) {
            contentBuilder.append(info.getNodeId().toString(16)).append(",")
                          .append(info.getHost()).append(",")
                          .append(info.getPort()).append(";");
        }

        Message neighborsMessage = new Message(Message.Type.NEIGHBORS, node.getNodeIdString(), contentBuilder.toString());
        source.sendMessage(neighborsMessage.serialize());
    }

    private void handleNeighborsMessage(PeerConnection source, Message message) {
        System.out.println("收到来自 " + message.getSenderId().substring(0, 8) + " 的 NEIGHBORS 列表");
        String content = message.getContent();
        if (content.isEmpty()) return;

        String[] nodesStr = content.split(";");
        for (String nodeStr : nodesStr) {
            String[] parts = nodeStr.split(",");
            if (parts.length == 3) {
                try {
                    BigInteger nodeId = new BigInteger(parts[0], 16);
                    String host = parts[1];
                    int port = Integer.parseInt(parts[2]);

                    NodeInfo newNode = new NodeInfo(nodeId, host, port);
                    node.updateRoutingTable(newNode);

                    // 尝试连接到新发现的节点以丰富连接
                    if (!node.getConnections().containsKey(newNode.getAddress())) {
                        node.connectToPeer(newNode.getAddress());
                    }
                } catch (NumberFormatException e) {
                    System.err.println("解析NEIGHBORS消息失败: " + nodeStr);
                }
            }
        }
    }

    // --- 应用层消息处理器 (部分保留) ---

    private void handleHelloMessage(PeerConnection source, Message message) {
        // HELLO 消息在DOP中主要用于初始连接和信息交换
        // 节点信息已在 handleMessage 开始时更新
        System.out.println("收到来自 " + message.getSenderId().substring(0, 8) + " 的 HELLO");

        // 回复一个HELLO，确认连接
        if (source.isInbound()) {
            Message replyHello = new Message(Message.Type.HELLO, node.getNodeIdString(), node.getAddress());
            source.sendMessage(replyHello.serialize());
        }

        // 向新节点发起节点发现请求，以获取其邻居
        Message findNodeMessage = new Message(Message.Type.FIND_NODE, node.getNodeIdString(), "", message.getSenderId());
        source.sendMessage(findNodeMessage.serialize());
    }

    private void handleChatMessage(PeerConnection source, Message message) {
        if (messageListener != null) {
            messageListener.onChatMessageReceived(message.getSenderId(), message.getContent());
        }
        // 转发逻辑由 routeAppMessage 和 forwardMessage 处理
        if (message.canForward()) {
            forwardMessage(source, message);
        }
    }

    private void handlePrivateChatMessage(PeerConnection source, Message message) {
        // 路由和本地处理已在 routeAppMessage 中完成
        // 这里不需要额外操作，因为如果消息是给我的，processLocalAppMessage会处理
        // 如果不是给我的，forwardMessage会处理
    }

    private void handleFileTransferRequest(PeerConnection source, Message message) {
        if (messageListener == null) return;
        String[] parts = message.getContent().split(":");
        if (parts.length >= 2) {
            try {
                String fileName = parts[0];
                long fileSize = Long.parseLong(parts[1]);
                messageListener.onFileTransferRequest(message.getSenderId(), fileName, fileSize);
            } catch (NumberFormatException e) {
                System.err.println("解析文件大小失败: " + parts[1]);
            }
        }
    }

    private void handleFileTransferResponse(PeerConnection source, Message message) {
        if (messageListener == null) return;
        String content = message.getContent();
        String senderId = message.getSenderId();

        if (content.startsWith("ACCEPT:")) {
            String fileInfo = content.substring(7);
            String[] parts = fileInfo.split(":", 2);
            if (parts.length >= 2) {
                String fileName = parts[0];
                String savePath = parts[1];
                messageListener.onSystemMessage("用户 " + senderId + " 接受了文件传输: " + fileName);
                startFileTransfer(senderId, fileName, savePath);
            }
        } else if (content.startsWith("REJECT:")) {
            String fileName = content.substring(7);
            messageListener.onSystemMessage("用户 " + senderId + " 拒绝了文件传输: " + fileName);
        }
    }

    // --- 辅助方法 ---

    private void updateSenderNodeInfo(Message message) {
        try {
            BigInteger senderNodeId = new BigInteger(message.getSenderId(), 16);
            // 假设消息内容中包含地址信息，或者从连接中获取
            // 在我们的新设计中，HELLO消息应包含地址
            String content = message.getContent();
            if (message.getType() == Message.Type.HELLO && content.contains(":")) {
                String[] parts = content.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                NodeInfo senderInfo = new NodeInfo(senderNodeId, host, port);
                node.updateRoutingTable(senderInfo);
            }
        } catch (Exception e) {
            // System.err.println("更新发送者节点信息失败: " + e.getMessage());
        }
    }

    private boolean isMessageProcessed(Message message) {
        return processedMessages.contains(message.getMessageId());
    }

    private void markMessageAsProcessed(Message message) {
        processedMessages.add(message.getMessageId());
        messageTimestamps.put(message.getMessageId(), System.currentTimeMillis());
    }

    private void startCleanupTask() {
        Timer cleanupTimer = new Timer("MessageRouter-Cleanup", true);
        cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                messageTimestamps.entrySet().removeIf(entry -> currentTime - entry.getValue() > MESSAGE_EXPIRE_TIME);
                processedMessages.retainAll(messageTimestamps.keySet());
            }
        }, CLEANUP_INTERVAL, CLEANUP_INTERVAL);
    }

    private void startFileTransfer(String targetNodeId, String fileName, String savePath) {
        File fileToSend = node.getPendingFile(fileName);
        if (fileToSend != null && fileToSend.exists()) {
            node.getFileTransferService().sendFile(targetNodeId, fileToSend, savePath);
            node.removePendingFile(fileName);
        } else {
            System.err.println("找不到要发送的文件: " + fileName);
        }
    }
    
    public MessageListener getMessageListener() {
        return messageListener;
    }
}

