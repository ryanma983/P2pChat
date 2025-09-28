package com.yourgroup.chat;

/**
 * 消息监听器接口，用于GUI与网络层的通信
 */
public interface MessageListener {
    
    /**
     * 当收到群聊消息时调用
     */
    void onChatMessageReceived(String senderId, String content);
    
    /**
     * 当收到私聊消息时调用
     */
    void onPrivateChatMessageReceived(String senderId, String content);
    
    /**
     * 当收到文件传输请求时调用
     */
    void onFileTransferRequest(String senderId, String fileName, long fileSize);
    
    /**
     * 当节点连接状态改变时调用
     */
    void onConnectionStatusChanged(int connectionCount);
    
    /**
     * 当收到系统消息时调用
     */
    void onSystemMessage(String message);
    
    /**
     * 当新成员加入时调用
     */
    void onMemberJoined(String nodeId, String address);
    
    /**
     * 当成员离开时调用
     */
    void onMemberLeft(String nodeId);
}
