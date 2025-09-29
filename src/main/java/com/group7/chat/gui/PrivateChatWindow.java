package com.group7.chat.gui;

import com.group7.chat.Node;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 私聊窗口类
 */
public class PrivateChatWindow {
    
    private Stage stage;
    private Node chatNode;
    private OnlineMember targetMember;
    private ObservableList<ChatMessage> messages;
    private ListView<ChatMessage> messageListView;
    private TextArea messageInput;
    private Button sendButton;
    private Button fileButton;

    private Label statusLabel;

    
    public PrivateChatWindow(Node chatNode, OnlineMember targetMember) {
        this.chatNode = chatNode;
        this.targetMember = targetMember;
        this.messages = FXCollections.observableArrayList();
        
        initializeWindow();
    }
    
    private void initializeWindow() {
        stage = new Stage();
        stage.setTitle("私聊 - " + targetMember.getNodeId());
        stage.setWidth(500);
        stage.setHeight(600);
        
        // 创建主布局
        BorderPane root = new BorderPane();
        root.getStyleClass().add("private-chat-window");
        
        // 顶部状态栏
        createTopBar(root);
        
        // 中间消息列表
        createMessageArea(root);
        
        // 底部输入区域
        createInputArea(root);
        
        // 创建场景并应用样式
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/private-chat-style.css").toExternalForm());

        
        stage.setScene(scene);
        

        
        // 设置窗口关闭事件
        stage.setOnCloseRequest(e -> {
            // 可以在这里添加清理逻辑
        });
        
        // 添加欢迎消息
        addSystemMessage("开始与 " + targetMember.getNodeId() + " 的私聊");
    }
    
    private void createTopBar(BorderPane root) {
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.getStyleClass().add("top-bar");
        
        Label titleLabel = new Label("私聊 - " + targetMember.getNodeId());
        titleLabel.getStyleClass().add("chat-title");
        
        statusLabel = new Label("在线");
        statusLabel.getStyleClass().add("status-label");
        
        // 添加文件传输按钮到顶部
        Button topFileButton = new Button("发送文件");
        topFileButton.getStyleClass().add("top-file-button");
        topFileButton.setOnAction(e -> handleSendFile());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        topBar.getChildren().addAll(titleLabel, statusLabel, spacer, topFileButton);
        root.setTop(topBar);
    }
    
    private void createMessageArea(BorderPane root) {
        messageListView = new ListView<>(messages);
        messageListView.setCellFactory(listView -> new MessageListCell());
        messageListView.getStyleClass().add("private-message-list");
        
        // 设置消息列表不可选中
        messageListView.setFocusTraversable(false);
        
        root.setCenter(messageListView);
    }
    
    private void createInputArea(BorderPane root) {
        VBox inputArea = new VBox(10);
        inputArea.setPadding(new Insets(10));
        inputArea.getStyleClass().add("input-area");
        
        // 消息输入框
        messageInput = new TextArea();
        messageInput.setPromptText("输入私聊消息... (Enter发送, Ctrl+Enter换行)");
        messageInput.setPrefRowCount(2);
        messageInput.setWrapText(true);
        messageInput.getStyleClass().add("message-input");
        
        // 按钮区域
        HBox buttonArea = new HBox(10);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);
        
        fileButton = new Button("文件");
        fileButton.getStyleClass().add("file-button");
        fileButton.setOnAction(e -> handleSendFile());
        
        sendButton = new Button("发送");
        sendButton.getStyleClass().add("send-button");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(e -> handleSendMessage());
        
        // 绑定发送按钮状态
        sendButton.disableProperty().bind(messageInput.textProperty().isEmpty());
        
        buttonArea.getChildren().addAll(fileButton, sendButton);
        
        inputArea.getChildren().addAll(messageInput, buttonArea);
        root.setBottom(inputArea);
        
        // 设置键盘事件
        messageInput.setOnKeyPressed(this::handleKeyPressed);
    }
    
    private void handleKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (event.isShiftDown()) {
                // Shift+Enter 换行
                messageInput.appendText("\n");
                event.consume();
            } else {
                // Enter 发送消息
                event.consume();
                handleSendMessage();
            }
        }
    }
    

    
    private void handleSendMessage() {
        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty()) {
            // 添加到界面
            addSentMessage(messageText);
            
            // 发送到网络
            chatNode.sendPrivateMessage(targetMember.getNodeId(), messageText);
            
            // 清空输入框
            messageInput.clear();
        }
    }
    
    private void handleSendFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择要发送的文件");
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            addSystemMessage("正在发送文件: " + selectedFile.getName());
            chatNode.sendFileRequest(targetMember.getNodeId(), selectedFile);
        }
    }
    
    /**
     * 添加发送的消息
     */
    public void addSentMessage(String content) {
        Platform.runLater(() -> {
            ChatMessage message = new ChatMessage(chatNode.getNodeId().toString(), content, ChatMessage.MessageType.SENT);
            messages.add(message);
            scrollToBottom();
        });
    }
    
    /**
     * 添加接收的消息
     */
    public void addReceivedMessage(String senderId, String content) {
        Platform.runLater(() -> {
            // 只显示来自目标用户的消息
            if (senderId.equals(targetMember.getNodeId())) {
                ChatMessage message = new ChatMessage(senderId, content, ChatMessage.MessageType.RECEIVED);
                messages.add(message);
                scrollToBottom();
                
                // 如果窗口不在前台，可以添加通知效果
                if (!stage.isFocused()) {
                    stage.setTitle("私聊 - " + targetMember.getNodeId() + " (新消息)");
                }
            }
        });
    }
    
    /**
     * 添加系统消息
     */
    public void addSystemMessage(String content) {
        Platform.runLater(() -> {
            ChatMessage message = new ChatMessage("系统", content, ChatMessage.MessageType.SYSTEM);
            messages.add(message);
            scrollToBottom();
        });
    }
    
    /**
     * 添加文件传输消息
     */
    public void addFileTransferMessage(String senderId, String fileName, boolean isRequest) {
        Platform.runLater(() -> {
            String content = isRequest ? 
                "收到文件传输请求: " + fileName : 
                "文件传输: " + fileName;
            ChatMessage message = new ChatMessage(senderId, content, ChatMessage.MessageType.SYSTEM);
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
     * 更新在线状态
     */
    public void updateOnlineStatus(boolean isOnline) {
        Platform.runLater(() -> {
            statusLabel.setText(isOnline ? "在线" : "离线");
            statusLabel.getStyleClass().removeAll("online-status", "offline-status");
            statusLabel.getStyleClass().add(isOnline ? "online-status" : "offline-status");
        });
    }
    
    /**
     * 显示窗口
     */
    public void show() {
        stage.show();
        stage.toFront();
        messageInput.requestFocus();
    }
    
    /**
     * 关闭窗口
     */
    public void close() {
        stage.close();
    }
    
    /**
     * 检查窗口是否显示
     */
    public boolean isShowing() {
        return stage.isShowing();
    }
    
    /**
     * 获取目标成员
     */
    public OnlineMember getTargetMember() {
        return targetMember;
    }
    
    /**
     * 窗口获得焦点时重置标题
     */
    public void onWindowFocused() {
        Platform.runLater(() -> {
            stage.setTitle("私聊 - " + targetMember.getNodeId());
        });
    }
}
