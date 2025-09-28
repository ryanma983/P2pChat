package com.yourgroup.chat.gui;

import com.yourgroup.chat.Message;
import com.yourgroup.chat.Node;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 聊天界面控制器
 */
public class ChatController implements Initializable, com.yourgroup.chat.MessageListener {
    
    @FXML private Label nodeIdLabel;
    @FXML private Label connectionCountLabel;
    @FXML private ListView<ChatMessage> messageListView;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private Button connectButton;
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem settingsMenuItem;
    
    private Node chatNode;
    private ObservableList<ChatMessage> messages;
    private Timer statusUpdateTimer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messages = FXCollections.observableArrayList();
        messageListView.setItems(messages);
        messageListView.setCellFactory(listView -> new MessageListCell());
        
        // 设置消息输入框
        messageInput.setWrapText(true);
        messageInput.setPrefRowCount(2);
        
        // 绑定发送按钮状态
        sendButton.disableProperty().bind(messageInput.textProperty().isEmpty());
        
        // 设置键盘快捷键
        messageInput.setOnKeyPressed(this::handleKeyPressed);
        
        // 设置菜单项事件
        aboutMenuItem.setOnAction(e -> showAboutDialog());
        settingsMenuItem.setOnAction(e -> showSettingsDialog());
        
        // 添加欢迎消息
        addSystemMessage("欢迎使用 P2P 聊天应用！");
    }
    
    /**
     * 设置聊天节点
     */
    public void setNode(Node node) {
        this.chatNode = node;
        
        // 更新界面信息
        Platform.runLater(() -> {
            nodeIdLabel.setText("节点ID: " + node.getNodeId());
            updateConnectionCount();
        });
        
        // 启动状态更新定时器
        startStatusUpdateTimer();
        
        // 设置消息监听器
        node.setMessageListener(this);
        
        addSystemMessage("节点已启动，端口: " + node.getPort());
    }
    
    /**
     * 处理发送按钮点击
     */
    @FXML
    private void handleSendMessage() {
        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty() && chatNode != null) {
            // 添加到界面
            addSentMessage(messageText);
            
            // 发送到网络
            chatNode.sendChatMessage(messageText);
            
            // 清空输入框
            messageInput.clear();
        }
    }
    
    /**
     * 处理连接按钮点击
     */
    @FXML
    private void handleConnect() {
        TextInputDialog dialog = new TextInputDialog("localhost:8081");
        dialog.setTitle("连接到节点");
        dialog.setHeaderText("请输入要连接的节点地址");
        dialog.setContentText("地址 (host:port):");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(address -> {
            if (chatNode.connectToPeer(address)) {
                addSystemMessage("成功连接到: " + address);
            } else {
                addSystemMessage("连接失败: " + address);
            }
        });
    }
    
    /**
     * 处理键盘事件
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (event.isControlDown()) {
                // Ctrl+Enter 换行
                messageInput.appendText("\n");
            } else {
                // Enter 发送消息
                event.consume();
                handleSendMessage();
            }
        }
    }
    
    /**
     * 添加发送的消息
     */
    private void addSentMessage(String content) {
        Platform.runLater(() -> {
            ChatMessage message = new ChatMessage(chatNode.getNodeId(), content, ChatMessage.MessageType.SENT);
            messages.add(message);
            scrollToBottom();
        });
    }
    
    /**
     * 添加接收的消息
     */
    public void addReceivedMessage(String senderId, String content) {
        Platform.runLater(() -> {
            ChatMessage message = new ChatMessage(senderId, content, ChatMessage.MessageType.RECEIVED);
            messages.add(message);
            scrollToBottom();
        });
    }
    
    /**
     * 添加系统消息
     */
    private void addSystemMessage(String content) {
        Platform.runLater(() -> {
            ChatMessage message = new ChatMessage("系统", content, ChatMessage.MessageType.SYSTEM);
            messages.add(message);
            scrollToBottom();
        });
    }
    
    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (!messages.isEmpty()) {
                messageListView.scrollTo(messages.size() - 1);
            }
        });
    }
    
    /**
     * 更新连接数量显示
     */
    private void updateConnectionCount() {
        if (chatNode != null) {
            Platform.runLater(() -> {
                int count = chatNode.getConnectionCount();
                connectionCountLabel.setText("连接数: " + count);
            });
        }
    }
    
    /**
     * 启动状态更新定时器
     */
    private void startStatusUpdateTimer() {
        statusUpdateTimer = new Timer("StatusUpdate", true);
        statusUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateConnectionCount();
            }
        }, 1000, 5000); // 每5秒更新一次
    }
    
    // MessageListener 接口实现
    @Override
    public void onChatMessageReceived(String senderId, String content) {
        addReceivedMessage(senderId, content);
    }
    
    @Override
    public void onConnectionStatusChanged(int connectionCount) {
        updateConnectionCount();
    }
    
    @Override
    public void onSystemMessage(String message) {
        addSystemMessage(message);
    }
    
    /**
     * 显示关于对话框
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("P2P 聊天应用");
        alert.setContentText("一个基于 Java 的去中心化聊天应用\n\n" +
                "特性:\n" +
                "• 去中心化网络架构\n" +
                "• 消息洪泛传播\n" +
                "• 自动节点发现\n" +
                "• 现代化用户界面");
        alert.showAndWait();
    }
    
    /**
     * 显示设置对话框
     */
    private void showSettingsDialog() {
        // TODO: 实现设置对话框
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("设置");
        alert.setHeaderText("设置功能");
        alert.setContentText("设置功能正在开发中...");
        alert.showAndWait();
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (statusUpdateTimer != null) {
            statusUpdateTimer.cancel();
        }
    }
}
