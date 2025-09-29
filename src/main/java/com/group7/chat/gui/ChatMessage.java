package com.group7.chat.gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 聊天消息的GUI数据模型
 */
public class ChatMessage {
    
    public enum MessageType {
        SENT,       // 自己发送的消息
        RECEIVED,   // 接收到的消息
        SYSTEM      // 系统消息
    }
    
    private final String senderId;
    private final String content;
    private final LocalDateTime timestamp;
    private final MessageType type;
    
    public ChatMessage(String senderId, String content, MessageType type) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.type = type;
    }
    
    public ChatMessage(String senderId, String content, LocalDateTime timestamp, MessageType type) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public String getContent() {
        return content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public String getFormattedDateTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getFormattedTime(), senderId, content);
    }
}
