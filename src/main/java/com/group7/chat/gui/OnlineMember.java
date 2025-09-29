package com.group7.chat.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 在线成员数据模型
 */
public class OnlineMember {
    private final StringProperty nodeId;
    private final StringProperty address;
    private final StringProperty status;
    private final StringProperty displayName;
    private long lastSeen;
    
    public OnlineMember(String nodeId, String address) {
        this.nodeId = new SimpleStringProperty(nodeId);
        this.address = new SimpleStringProperty(address);
        this.status = new SimpleStringProperty("在线");
        this.lastSeen = System.currentTimeMillis();
        
        // 从地址中提取端口号作为显示名称
        this.displayName = new SimpleStringProperty(extractDisplayName(address));
    }
    
    public OnlineMember(String nodeId, String address, String displayName) {
        this.nodeId = new SimpleStringProperty(nodeId);
        this.address = new SimpleStringProperty(address);
        this.status = new SimpleStringProperty("在线");
        this.displayName = new SimpleStringProperty(displayName);
        this.lastSeen = System.currentTimeMillis();
    }
    
    private String extractDisplayName(String address) {
        if (address != null && address.contains(":")) {
            String port = address.split(":")[1];
            return "Node_" + port;
        }
        return "Unknown";
    }
    
    // NodeId 属性
    public StringProperty nodeIdProperty() {
        return nodeId;
    }
    
    public String getNodeId() {
        return nodeId.get();
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId.set(nodeId);
    }
    
    // Address 属性
    public StringProperty addressProperty() {
        return address;
    }
    
    public String getAddress() {
        return address.get();
    }
    
    public void setAddress(String address) {
        this.address.set(address);
    }
    
    // Status 属性
    public StringProperty statusProperty() {
        return status;
    }
    
    public String getStatus() {
        return status.get();
    }
    
    public void setStatus(String status) {
        this.status.set(status);
    }
    
    // DisplayName 属性
    public StringProperty displayNameProperty() {
        return displayName;
    }
    
    public String getDisplayName() {
        return displayName.get();
    }
    
    public void setDisplayName(String displayName) {
        this.displayName.set(displayName);
    }
    
    // LastSeen
    public long getLastSeen() {
        return lastSeen;
    }
    
    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OnlineMember that = (OnlineMember) obj;
        return getNodeId().equals(that.getNodeId());
    }
    
    @Override
    public int hashCode() {
        return getNodeId().hashCode();
    }
}
