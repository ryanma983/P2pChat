package com.group7.chat;

import java.io.*;
import java.net.Socket;

/**
 * 表示与一个对等节点的连接
 */
public class PeerConnection {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private String address;
    private final boolean inbound; // true表示入站连接，false表示出站连接
    private long lastActivity; // 最后活跃时间
    private String remoteNodeId; // 存储远程节点的ID
    
    public PeerConnection(Socket socket, String address, boolean inbound) throws IOException {
        this.socket = socket;
        this.address = address;
        this.inbound = inbound;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.lastActivity = System.currentTimeMillis();
    }
    
    /**
     * 发送消息到对等节点
     */
    public void sendMessage(String message) {
        writer.println(message);
    }
    
    /**
     * 从对等节点读取消息
     */
    public String readMessage() throws IOException {
        return reader.readLine();
    }
    
    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取连接地址
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * 设置连接地址
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * 检查是否为入站连接
     */
    public boolean isInbound() {
        return inbound;
    }
    
    /**
     * 获取底层Socket对象
     */
    public Socket getSocket() {
        return socket;
    }
    
    /**
     * 检查连接是否仍然活跃
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
    
    /**
     * 更新最后活跃时间
     */
    public void updateLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
    
    /**
     * 获取最后活跃时间
     */
    public long getLastActivity() {
        return lastActivity;
    }
    
    /**
     * 检查连接是否超时
     */
    public boolean isTimeout(long timeoutMs) {
        return System.currentTimeMillis() - lastActivity > timeoutMs;
    }
    
    /**
     * 获取远程地址
     */
    public String getRemoteAddress() {
        return socket.getRemoteSocketAddress().toString();
    }
    
    /**
     * 设置远程节点ID
     */
    public void setRemoteNodeId(String nodeId) {
        this.remoteNodeId = nodeId;
    }
    
    /**
     * 获取远程节点ID
     */
    public String getRemoteNodeId() {
        return remoteNodeId;
    }
}