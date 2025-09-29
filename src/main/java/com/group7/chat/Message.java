package com.group7.chat;

import java.util.UUID;

/**
 * 网络消息类，封装在P2P网络中传递的消息
 */
public class Message {
    public enum Type {
        // --- 原有消息类型 ---
        HELLO,          // 握手消息
        CHAT,           // 群聊消息
        PRIVATE_CHAT,   // 私聊消息
        FILE_TRANSFER,  // 文件传输
        FILE_REQUEST,   // 文件传输请求
        PEER_LIST,      // 邻居列表共享 (将被NEIGHBORS替代)
        KEY_EXCHANGE,   // 密钥交换消息
        SECURE_CHAT,    // 安全群聊消息
        SECURE_PRIVATE_CHAT, // 安全私聊消息

        // --- 分布式覆盖网络 (DOP) 消息类型 ---
        PING,           // 检查节点是否在线
        PONG,           // 对PING的响应
        FIND_NODE,      // 请求获取离目标ID最近的节点列表
        NEIGHBORS,      // 对FIND_NODE的响应，包含节点列表
        SECURE_MESSAGE  // 安全消息类型
    }

    private final String messageId;
    private final Type type;
    private final String senderId;
    private final String content;
    private final long timestamp;
    private int ttl; // Time To Live
    private String targetId; // 目标ID (可用于私聊、FIND_NODE等)

    // 构造函数保持不变
    public Message(Type type, String senderId, String content) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.ttl = 10; // 默认TTL
        this.targetId = null;
    }

    public Message(Type type, String senderId, String content, String targetId) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.ttl = 10;
        this.targetId = targetId;
    }

    public Message(String messageId, Type type, String senderId, String content, long timestamp, int ttl, String targetId) {
        this.messageId = messageId;
        this.type = type;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.ttl = ttl;
        this.targetId = targetId;
    }

    /**
     * 序列化与反序列化 (保持不变)
     */
    public String serialize() {
        String encodedContent = encodeContent(content);
        String encodedTargetId = targetId != null ? encodeContent(targetId) : "";
        return String.format("%s|%s|%s|%s|%d|%d|%s",
            messageId, type.name(), senderId, encodedContent, timestamp, ttl, encodedTargetId);
    }

    public static Message deserialize(String data) {
        try {
            String[] parts = data.split("\\|", 7);
            if (parts.length < 6) {
                throw new IllegalArgumentException("Invalid message format: " + data);
            }
            String messageId = parts[0];
            Type type = Type.valueOf(parts[1]);
            String senderId = parts[2];
            String content = decodeContent(parts[3]);
            long timestamp = Long.parseLong(parts[4]);
            int ttl = Integer.parseInt(parts[5]);
            String targetId = parts.length > 6 && !parts[6].isEmpty() ? decodeContent(parts[6]) : null;
            return new Message(messageId, type, senderId, content, timestamp, ttl, targetId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize message: " + data, e);
        }
    }

    // 内容编解码 (保持不变)
    private static String encodeContent(String content) {
        if (content == null) return "";
        return content.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String decodeContent(String encodedContent) {
        if (encodedContent == null || encodedContent.isEmpty()) return "";
        return encodedContent.replace("\\r", "\r").replace("\\n", "\n").replace("\\|", "\\|").replace("\\\\", "\\");
    }

    // Getters
    public String getMessageId() { return messageId; }
    public Type getType() { return type; }
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public int getTtl() { return ttl; }
    public String getTargetId() { return targetId; }

    // 辅助方法
    public Message createForwardCopy() {
        return new Message(messageId, type, senderId, content, timestamp, ttl - 1, targetId);
    }

    public boolean canForward() {
        return ttl > 0;
    }
    
    public boolean isSecureMessage() {
        return type == Type.SECURE_MESSAGE || type == Type.SECURE_CHAT || type == Type.SECURE_PRIVATE_CHAT;
    }

    @Override
    public String toString() {
        return String.format("Message{id=%s, type=%s, sender=%s, target=%s, content='%s'}",
            messageId.substring(0, 8), type, senderId.substring(0, 8), targetId != null ? targetId.substring(0, 8) : "N/A", content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Message message = (Message) obj;
        return messageId.equals(message.messageId);
    }

    @Override
    public int hashCode() {
        return messageId.hashCode();
    }
}

