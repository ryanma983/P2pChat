package com.yourgroup.chat;

import java.util.UUID;

/**
 * 网络消息类，封装在P2P网络中传递的消息
 */
public class Message {
    public enum Type {
        HELLO,          // 握手消息
        CHAT,           // 聊天消息
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
    
    public Message(Type type, String senderId, String content) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.ttl = 10; // 默认最多转发10次
    }
    
    public Message(String messageId, Type type, String senderId, String content, long timestamp, int ttl) {
        this.messageId = messageId;
        this.type = type;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.ttl = ttl;
    }
    
    /**
     * 将消息序列化为字符串格式
     */
    public String serialize() {
        return String.format("%s|%s|%s|%s|%d|%d", 
            messageId, type.name(), senderId, content, timestamp, ttl);
    }
    
    /**
     * 从字符串反序列化消息
     */
    public static Message deserialize(String data) {
        try {
            String[] parts = data.split("\\|", 6);
            if (parts.length != 6) {
                throw new IllegalArgumentException("Invalid message format");
            }
            
            String messageId = parts[0];
            Type type = Type.valueOf(parts[1]);
            String senderId = parts[2];
            String content = parts[3];
            long timestamp = Long.parseLong(parts[4]);
            int ttl = Integer.parseInt(parts[5]);
            
            return new Message(messageId, type, senderId, content, timestamp, ttl);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize message: " + data, e);
        }
    }
    
    /**
     * 创建消息的副本，TTL减1（用于转发）
     */
    public Message createForwardCopy() {
        return new Message(messageId, type, senderId, content, timestamp, ttl - 1);
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
