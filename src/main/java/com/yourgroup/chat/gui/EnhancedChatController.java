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
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 增强版聊天界面控制器，支持群聊、私聊、文件传输和成员列表
 */
public class EnhancedChatController implements Initializable, com.yourgroup.chat.MessageListener {
    
    // 左侧成员面板
    @FXML private ListView<OnlineMember> memberListView;
    @FXML private Label memberCountLabel;
    // 移除聊天模式选择相关控件
    
    // 右侧聊天面板
    @FXML private Label chatTitleLabel;
    @FXML private Label nodeIdLabel;
    @FXML private Label connectionCountLabel;
    @FXML private ListView<ChatMessage> messageListView;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private Button fileButton;
    @FXML private Button emojiButton;
    @FXML private Button connectButton;
    
    // 菜单项
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem settingsMenuItem;
    
    private Node chatNode;
    private ObservableList<ChatMessage> messages;
    private ObservableList<OnlineMember> onlineMembers;
    private Timer statusUpdateTimer;
    private Map<String, PrivateChatWindow> privateChatWindows = new HashMap<>();
    private EmojiPicker emojiPicker;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化数据
        messages = FXCollections.observableArrayList();
        onlineMembers = FXCollections.observableArrayList();
        
        // 设置列表视图
        messageListView.setItems(messages);
        messageListView.setCellFactory(listView -> new MessageListCell());
        memberListView.setItems(onlineMembers);
        
        // 默认群聊模式，无需单选按钮
        
        // 设置消息输入框
        messageInput.setWrapText(true);
        messageInput.setPrefRowCount(2);
        
        // 绑定发送按钮状态
        sendButton.disableProperty().bind(messageInput.textProperty().isEmpty());
        
        // 设置键盘快捷键
        messageInput.setOnKeyPressed(this::handleKeyPressed);
        
        // 设置成员列表点击事件
        memberListView.setOnMouseClicked(this::handleMemberClick);
        
        // 移除聊天模式切换，默认群聊
        
        // 设置菜单项事件
        aboutMenuItem.setOnAction(e -> showAboutDialog());
        settingsMenuItem.setOnAction(e -> showSettingsDialog());
        
        // 初始化表情选择器
        emojiPicker = new EmojiPicker(this::insertEmoji);
        emojiButton.setOnAction(e -> handleEmojiButton());
        
        // 添加欢迎消息
        addSystemMessage("欢迎使用 P2P 聊天应用！支持群聊、私聊、文件传输和表情。");
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
            
            // 添加自己到成员列表
            OnlineMember selfMember = new OnlineMember(node.getNodeId(), "localhost:" + node.getPort());
            selfMember.setStatus("本机");
            onlineMembers.add(selfMember);
            updateMemberCount();
        });
        
        // 启动状态更新定时器
        startStatusUpdateTimer();
        
        // 设置消息监听器
        node.setMessageListener(this);
        
        addSystemMessage("节点已启动，端口: " + node.getPort());
    }
    
    /**
     * 处理成员列表点击事件
     */
    private void handleMemberClick(MouseEvent event) {
        OnlineMember clickedMember = memberListView.getSelectionModel().getSelectedItem();
        if (clickedMember != null && !clickedMember.getNodeId().equals(chatNode.getNodeId())) {
            // 双击打开私聊窗口
            if (event.getClickCount() == 2) {
                openPrivateChatWindow(clickedMember);
            }
        }
    }
    
    /**
     * 打开私聊窗口
     */
    private void openPrivateChatWindow(OnlineMember member) {
        String nodeId = member.getNodeId();
        
        // 检查是否已经有该成员的私聊窗口
        PrivateChatWindow existingWindow = privateChatWindows.get(nodeId);
        if (existingWindow != null && existingWindow.isShowing()) {
            // 如果窗口已存在，直接显示
            existingWindow.show();
            return;
        }
        
        // 创建新的私聊窗口
        PrivateChatWindow privateChatWindow = new PrivateChatWindow(chatNode, member);
        privateChatWindows.put(nodeId, privateChatWindow);
        
        // 显示窗口
        privateChatWindow.show();
        
        addSystemMessage("已打开与 " + nodeId + " 的私聊窗口");
    }
    
    /**
     * 处理表情按钮点击
     */
    @FXML
    private void handleEmojiButton() {
        if (emojiPicker.isShowing()) {
            emojiPicker.hide();
        } else {
            // 计算表情选择器的显示位置
            double x = emojiButton.localToScreen(emojiButton.getBoundsInLocal()).getMinX();
            double y = emojiButton.localToScreen(emojiButton.getBoundsInLocal()).getMinY() - 260;
            emojiPicker.show(emojiButton, x, y);
        }
    }
    
    /**
     * 插入表情到输入框
     */
    private void insertEmoji(String emoji) {
        int caretPosition = messageInput.getCaretPosition();
        String currentText = messageInput.getText();
        String newText = currentText.substring(0, caretPosition) + emoji + currentText.substring(caretPosition);
        messageInput.setText(newText);
        messageInput.positionCaret(caretPosition + emoji.length());
        messageInput.requestFocus();
    }
    
    // updateChatMode方法已移除，默认群聊模式
    
    /**
     * 处理发送按钮点击
     */
    @FXML
    private void handleSendMessage() {
        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty() && chatNode != null) {
            // 发送群聊消息
            addSentMessage(messageText, ChatMessage.MessageType.SENT);
            chatNode.sendChatMessage(messageText);
            
            // 清空输入框
            messageInput.clear();
        }
    }
    
    /**
     * 处理文件发送按钮点击
     */
    @FXML
    private void handleSendFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择要发送的文件");
        File selectedFile = fileChooser.showOpenDialog(sendButton.getScene().getWindow());
        
        if (selectedFile != null) {
            // 文件传输功能暂时禁用，需要在私聊窗口中使用
            addSystemMessage("文件传输请在私聊窗口中使用（双击成员名称打开私聊窗口）");
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
    private void addSentMessage(String content, ChatMessage.MessageType type) {
        Platform.runLater(() -> {
            ChatMessage message = new ChatMessage(chatNode.getNodeId(), content, type);
            messages.add(message);
            scrollToBottom();
        });
    }
    
    /**
     * 添加接收的消息
     */
    public void addReceivedMessage(String senderId, String content, ChatMessage.MessageType type) {
        Platform.runLater(() -> {
            ChatMessage message = new ChatMessage(senderId, content, type);
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
     * 更新成员数量显示
     */
    private void updateMemberCount() {
        Platform.runLater(() -> {
            memberCountLabel.setText("(" + onlineMembers.size() + ")");
        });
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
        addReceivedMessage(senderId, content, ChatMessage.MessageType.RECEIVED);
    }
    
    @Override
    public void onPrivateChatMessageReceived(String senderId, String content) {
        // 在主窗口显示私聊消息
        addReceivedMessage(senderId, "[私聊] " + content, ChatMessage.MessageType.RECEIVED);
        
        // 如果有对应的私聊窗口，也在私聊窗口中显示
        PrivateChatWindow privateChatWindow = privateChatWindows.get(senderId);
        if (privateChatWindow != null && privateChatWindow.isShowing()) {
            privateChatWindow.addReceivedMessage(senderId, content);
        }
    }
    
    @Override
    public void onFileTransferRequest(String senderId, String fileName, long fileSize) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("文件传输请求");
            alert.setHeaderText(senderId + " 想要发送文件给你");
            alert.setContentText("文件名: " + fileName + "\n文件大小: " + fileSize + " bytes\n\n是否接受?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                addSystemMessage("已接受来自 " + senderId + " 的文件: " + fileName);
                
                // 如果有对应的私聊窗口，也在私聊窗口中显示
                PrivateChatWindow privateChatWindow = privateChatWindows.get(senderId);
                if (privateChatWindow != null && privateChatWindow.isShowing()) {
                    privateChatWindow.addFileTransferMessage(senderId, fileName, true);
                }
                
                // TODO: 实现实际的文件接收逻辑
            } else {
                addSystemMessage("已拒绝来自 " + senderId + " 的文件: " + fileName);
            }
        });
    }
    
    @Override
    public void onConnectionStatusChanged(int connectionCount) {
        updateConnectionCount();
    }
    
    @Override
    public void onSystemMessage(String message) {
        addSystemMessage(message);
    }
    
    @Override
    public void onMemberJoined(String nodeId, String address) {
        Platform.runLater(() -> {
            // 检查是否已存在
            boolean exists = onlineMembers.stream()
                .anyMatch(member -> member.getNodeId().equals(nodeId));
            
            if (!exists) {
                OnlineMember newMember = new OnlineMember(nodeId, address);
                onlineMembers.add(newMember);
                updateMemberCount();
                addSystemMessage("成员 " + nodeId + " 加入了聊天");
            }
        });
    }
    
    @Override
    public void onMemberLeft(String nodeId) {
        Platform.runLater(() -> {
            onlineMembers.removeIf(member -> member.getNodeId().equals(nodeId));
            updateMemberCount();
            addSystemMessage("成员 " + nodeId + " 离开了聊天");
            
            // 成员离开时的处理（已简化）
            
            // 更新对应私聊窗口的状态
            PrivateChatWindow privateChatWindow = privateChatWindows.get(nodeId);
            if (privateChatWindow != null && privateChatWindow.isShowing()) {
                privateChatWindow.updateOnlineStatus(false);
                privateChatWindow.addSystemMessage("用户已离线");
            }
        });
    }
    
    /**
     * 显示关于对话框
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("P2P 聊天应用");
        alert.setContentText("一个功能完整的去中心化聊天应用\n\n" +
                "特性:\n" +
                "• 群聊和私聊\n" +
                "• 点对点文件传输\n" +
                "• 在线成员列表\n" +
                "• 去中心化网络架构\n" +
                "• 消息洪泛传播\n" +
                "• 自动节点发现");
        alert.showAndWait();
    }
    
    /**
     * 显示设置对话框
     */
    private void showSettingsDialog() {
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
        
        // 关闭所有私聊窗口
        for (PrivateChatWindow window : privateChatWindows.values()) {
            if (window.isShowing()) {
                window.close();
            }
        }
        privateChatWindows.clear();
    }
}
