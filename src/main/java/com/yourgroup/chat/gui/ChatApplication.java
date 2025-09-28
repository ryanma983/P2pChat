package com.yourgroup.chat.gui;

import com.yourgroup.chat.Node;
import com.yourgroup.chat.util.EmojiSupport;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX 聊天应用程序主类
 */
public class ChatApplication extends Application {
    
    private Node chatNode;
    private EnhancedChatController controller;
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        // 检测表情字体支持
        System.out.println("=== P2P Chat 启动 ===");
        EmojiSupport.printEmojiSupportInfo();
        
        // 加载 FXML 文件
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/enhanced-chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        
        // 加载 CSS 样式
        scene.getStylesheets().add(getClass().getResource("/css/enhanced-chat-style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/javafx-emoji-style.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/dynamic-emoji-fonts.css").toExternalForm());
        
        // 获取控制器并设置节点
        controller = fxmlLoader.getController();
        
        // 创建聊天节点（默认端口8080，可以通过参数修改）
        int port = getPortFromParameters();
        chatNode = new Node(port);
        controller.setNode(chatNode);
        
        // 设置窗口属性
        primaryStage.setTitle("P2P Chat - " + chatNode.getNodeId());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        
        // 设置应用程序图标（如果有的话）
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/chat-icon.png")));
        } catch (Exception e) {
            // 图标文件不存在时忽略
        }
        
        // 设置关闭事件处理
        primaryStage.setOnCloseRequest(event -> {
            if (chatNode != null) {
                chatNode.stop();
            }
            Platform.exit();
            System.exit(0);
        });
        
        // 启动聊天节点
        chatNode.start();
        
        // 处理命令行参数中的已知节点
        addKnownPeersFromParameters();
        
        // 显示窗口
        primaryStage.show();
    }
    
    /**
     * 从命令行参数获取端口号
     */
    private int getPortFromParameters() {
        try {
            Parameters params = getParameters();
            if (!params.getRaw().isEmpty()) {
                return Integer.parseInt(params.getRaw().get(0));
            }
        } catch (NumberFormatException e) {
            System.err.println("无效的端口号参数，使用默认端口 8080");
        }
        return 8080;
    }
    
    /**
     * 从命令行参数添加已知节点
     */
    private void addKnownPeersFromParameters() {
        Parameters params = getParameters();
        for (int i = 1; i < params.getRaw().size(); i++) {
            String peer = params.getRaw().get(i);
            chatNode.addKnownPeer(peer);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
