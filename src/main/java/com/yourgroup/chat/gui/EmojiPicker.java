package com.yourgroup.chat.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 表情选择器组件
 */
public class EmojiPicker {
    
    private Popup popup;
    private Consumer<String> onEmojiSelected;
    
    // 表情分类
    private static final List<String> SMILEYS = Arrays.asList(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
        "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
        "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬"
    );
    
    private static final List<String> GESTURES = Arrays.asList(
        "👍", "👎", "👌", "🤌", "🤏", "✌️", "🤞", "🤟", "🤘", "🤙",
        "👈", "👉", "👆", "🖕", "👇", "☝️", "👋", "🤚", "🖐️", "✋",
        "🖖", "👏", "🙌", "🤲", "🤝", "🙏", "✍️", "💪", "🦾", "🦿",
        "🦵", "🦶", "👂", "🦻", "👃", "🧠", "🫀", "🫁", "🦷", "🦴"
    );
    
    private static final List<String> OBJECTS = Arrays.asList(
        "💻", "🖥️", "🖨️", "⌨️", "🖱️", "🖲️", "💽", "💾", "💿", "📀",
        "📱", "📞", "☎️", "📟", "📠", "📺", "📻", "🎙️", "🎚️", "🎛️",
        "🧭", "⏱️", "⏲️", "⏰", "🕰️", "⌛", "⏳", "📡", "🔋", "🔌",
        "💡", "🔦", "🕯️", "🪔", "🧯", "🛢️", "💸", "💵", "💴", "💶"
    );
    
    private static final List<String> SYMBOLS = Arrays.asList(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "☮️",
        "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐",
        "⭐", "🌟", "✨", "⚡", "☄️", "💫", "🔥", "💥", "💢", "💨"
    );
    
    public EmojiPicker(Consumer<String> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
        createPopup();
    }
    
    private void createPopup() {
        popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        
        VBox content = new VBox();
        content.getStyleClass().add("emoji-picker");
        content.setPrefSize(300, 250);
        
        // 创建标题
        Label titleLabel = new Label("选择表情");
        titleLabel.getStyleClass().add("emoji-picker-title");
        titleLabel.setPadding(new Insets(10));
        
        // 创建标签页
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("emoji-tab-pane");
        
        // 笑脸标签页
        Tab smileyTab = createEmojiTab("😊", "笑脸", SMILEYS);
        
        // 手势标签页
        Tab gestureTab = createEmojiTab("👍", "手势", GESTURES);
        
        // 物品标签页
        Tab objectTab = createEmojiTab("💻", "物品", OBJECTS);
        
        // 符号标签页
        Tab symbolTab = createEmojiTab("❤️", "符号", SYMBOLS);
        
        tabPane.getTabs().addAll(smileyTab, gestureTab, objectTab, symbolTab);
        
        content.getChildren().addAll(titleLabel, tabPane);
        popup.getContent().add(content);
    }
    
    private Tab createEmojiTab(String tabEmoji, String tabName, List<String> emojis) {
        Tab tab = new Tab(tabEmoji);
        tab.setClosable(false);
        
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(5);
        flowPane.setVgap(5);
        flowPane.setPadding(new Insets(10));
        flowPane.getStyleClass().add("emoji-flow-pane");
        
        for (String emoji : emojis) {
            Button emojiButton = new Button(emoji);
            emojiButton.getStyleClass().add("emoji-button");
            emojiButton.setPrefSize(30, 30);
            emojiButton.setOnAction(e -> {
                if (onEmojiSelected != null) {
                    onEmojiSelected.accept(emoji);
                }
                popup.hide();
            });
            flowPane.getChildren().add(emojiButton);
        }
        
        ScrollPane scrollPane = new ScrollPane(flowPane);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("emoji-scroll-pane");
        
        tab.setContent(scrollPane);
        return tab;
    }
    
    /**
     * 显示表情选择器
     */
    public void show(javafx.scene.Node owner, double x, double y) {
        popup.show(owner, x, y);
    }
    
    /**
     * 隐藏表情选择器
     */
    public void hide() {
        popup.hide();
    }
    
    /**
     * 检查是否显示中
     */
    public boolean isShowing() {
        return popup.isShowing();
    }
}
