package com.yourgroup.chat;

import java.util.UUID;

/**
 * 网络消息类，封装在P2P网络中传递的消息
 */
public class Message {
    public enum Type {
        HELLO,          // 握手消息
        CHAT,           // 群聊消息
        PRIVATE_CHAT,   // 私聊消息
        FILE_TRANSFER,  // 文件传输
        FILE_REQUEST,   // 文件传输请求
        PEER_LIST,      // 邻居列表共享
        PING,           // 心跳检测
        PONG            // 心跳响应
    }
    
    private final String messageId;
    private final Type type;
    private final String senderId;
    private final String content;
    private final long timestamp;
    private int ttl; // Time To Live，防止消息无限传播
    private String targetId; // 目标节点ID，用于私聊
    
    public Message(Type type, String senderId, String content) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.ttl = 10; // 默认最多转发10次
        this.targetId = null; // 群聊消息无目标
    }
    
    public Message(Type type, String senderId, String content, String targetId) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.ttl = 10; // 默认最多转发10次
        this.targetId = targetId; // 私聊目标
    }
    
    public Message(String messageId, Type type, String senderId, String content, long timestamp, int ttl) {
        this.messageId = messageId;
        this.type = type;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.ttl = ttl;
        this.targetId = null;
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
     * 将消息序列化为字符串格式
     */
    public String serialize() {
        // 对内容进行编码，处理换行符和特殊字符
        String encodedContent = encodeContent(content);
        String encodedTargetId = targetId != null ? encodeContent(targetId) : "";
        
        return String.format("%s|%s|%s|%s|%d|%d|%s", 
            messageId, type.name(), senderId, encodedContent, timestamp, ttl, encodedTargetId);
    }
    
    /**
     * 从字符串反序列化消息
     */
    public static Message deserialize(String data) {
        try {
            String[] parts = data.split("\\|", 7);
            if (parts.length < 6) {
                throw new IllegalArgumentException("Invalid message format: expected at least 6 parts, got " + parts.length);
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
    
    /**
     * 编码内容，处理特殊字符
     */
    private static String encodeContent(String content) {
        if (content == null) return "";
        return content.replace("\\", "\\\\")
                     .replace("|", "\\|")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r");
    }
    
    /**
     * 解码内容，恢复特殊字符
     */
    private static String decodeContent(String encodedContent) {
        if (encodedContent == null || encodedContent.isEmpty()) return "";
        return encodedContent.replace("\\r", "\r")
                            .replace("\\n", "\n")
                            .replace("\\|", "|")
                            .replace("\\\\", "\\");
    }
    
    /**
     * 创建消息的副本，TTL减1（用于转发）
     */
    public Message createForwardCopy() {
        return new Message(messageId, type, senderId, content, timestamp, ttl - 1, targetId);
    }
    
    /**
     * 检查消息是否还可以继续转发
     */
    public boolean canForward() {
        return ttl > 0;
    }
    
    // Getters
    public String getMessageId() { return messageId; }
    public Type getType() { return type; }
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public int getTtl() { return ttl; }
    public String getTargetId() { return targetId; }
    
    @Override
    public String toString() {
        return String.format("Message{id='%s', type=%s, sender='%s', content='%s', ttl=%d}", 
            messageId, type, senderId, content, ttl);
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
