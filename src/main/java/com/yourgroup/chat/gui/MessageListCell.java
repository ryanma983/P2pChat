package com.yourgroup.chat.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import com.yourgroup.chat.util.EmojiRenderer;

/**
 * 自定义的消息列表单元格，用于显示聊天消息
 */
public class MessageListCell extends ListCell<ChatMessage> {
    
    private HBox container;
    private VBox messageBox;
    private Label senderLabel;
    private TextFlow contentFlow;  // 改为TextFlow以支持表情
    private Label timeLabel;
    private Region spacer;
    
    public MessageListCell() {
        createComponents();
        setupLayout();
    }
    
    private void createComponents() {
        container = new HBox();
        messageBox = new VBox();
        senderLabel = new Label();
        contentFlow = new TextFlow();  // 使用TextFlow支持表情
        timeLabel = new Label();
        spacer = new Region();
        
        // 设置TextFlow属性
        contentFlow.setMaxWidth(400);
        contentFlow.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        senderLabel.getStyleClass().add("sender-label");
        contentFlow.getStyleClass().add("content-flow");
        timeLabel.getStyleClass().add("time-label");
    }
    
    private void setupLayout() {
        // 设置消息框布局
        messageBox.getChildren().addAll(senderLabel, contentFlow, timeLabel);
        messageBox.setSpacing(2);
        messageBox.setPadding(new Insets(8, 12, 8, 12));
        
        // 设置容器布局
        container.getChildren().addAll(spacer, messageBox);
        container.setSpacing(10);
        container.setPadding(new Insets(5, 10, 5, 10));
        
        HBox.setHgrow(spacer, Priority.ALWAYS);
    }
    
    @Override
    protected void updateItem(ChatMessage message, boolean empty) {
        super.updateItem(message, empty);
        
        if (empty || message == null) {
            setGraphic(null);
            return;
        }
        
        // 更新消息内容
        senderLabel.setText(message.getSenderId());
        
        // 使用表情渲染器处理消息内容
        TextFlow renderedContent = EmojiRenderer.renderEmojis(message.getContent());
        contentFlow.getChildren().clear();
        contentFlow.getChildren().addAll(renderedContent.getChildren());
        
        timeLabel.setText(message.getFormattedTime());
        
        // 根据消息类型设置样式和对齐方式
        messageBox.getStyleClass().clear();
        container.getChildren().clear();
        
        switch (message.getType()) {
            case SENT:
                messageBox.getStyleClass().add("sent-message");
                container.getChildren().addAll(spacer, messageBox);
                container.setAlignment(Pos.CENTER_RIGHT);
                senderLabel.setVisible(false);
                senderLabel.setManaged(false);
                break;
                
            case RECEIVED:
                messageBox.getStyleClass().add("received-message");
                container.getChildren().addAll(messageBox, spacer);
                container.setAlignment(Pos.CENTER_LEFT);
                senderLabel.setVisible(true);
                senderLabel.setManaged(true);
                break;
                
            case SYSTEM:
                messageBox.getStyleClass().add("system-message");
                container.getChildren().clear();
                container.getChildren().add(messageBox);
                container.setAlignment(Pos.CENTER);
                senderLabel.setVisible(false);
                senderLabel.setManaged(false);
                break;
        }
        
        setGraphic(container);
    }
}
