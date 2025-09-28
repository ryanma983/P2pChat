package com.yourgroup.chat.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 在线成员数据模型
 */
public class OnlineMember {
    private final StringProperty nodeId;
    private final StringProperty address;
    private final StringProperty status;
    private long lastSeen;
    
    public OnlineMember(String nodeId, String address) {
        this.nodeId = new SimpleStringProperty(nodeId);
        this.address = new SimpleStringProperty(address);
        this.status = new SimpleStringProperty("在线");
        this.lastSeen = System.currentTimeMillis();
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
    
    // LastSeen
    public long getLastSeen() {
        return lastSeen;
    }
    
    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return getNodeId();
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
