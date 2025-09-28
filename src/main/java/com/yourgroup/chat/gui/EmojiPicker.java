package com.yourgroup.chat.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 表情选择器组件 - 使用简单的文字表情
 */
public class EmojiPicker {
    
    private Popup popup;
    private Consumer<String> onEmojiSelected;
    
    // 表情分类 - 使用简单的ASCII表情
    private static final List<String> SMILEYS = Arrays.asList(
        ":)", ":(", ":D", ":P", ";)", ":o", ":|", ":/", ":*", "<3",
        "^_^", "-_-", "o_O", ">:(", ":@", "XD", "=D", "=)", "=(", "=P",
        ":-)", ":-(", ":-D", ":-P", ";-)", ":-o", ":-|", ":-/", ":-*", "</3",
        "^o^", "T_T", "O_O", ">:-(", ":-@", "X-D", "8)", "8-)", "B)", "B-)"
    );
    
    private static final List<String> GESTURES = Arrays.asList(
        "\\o/", "o/", "\\o", "_o_", "^5", "m(", ")m", "\\m/", "m/", "\\m",
        "->", "<-", "^", "v", "<", ">", "~", "*", "+", "-",
        "o", "O", "x", "X", "?", "!", "@", "#", "$", "%"
    );
    
    private static final List<String> OBJECTS = Arrays.asList(
        "[PC]", "[TV]", "[TEL]", "[CAR]", "[BUS]", "[BIKE]", "[BOOK]", "[PEN]", "[CUP]", "[FOOD]",
        "[HOME]", "[WORK]", "[SHOP]", "[BANK]", "[MAIL]", "[GIFT]", "[STAR]", "[SUN]", "[MOON]", "[CLOUD]",
        "[RAIN]", "[SNOW]", "[FIRE]", "[TREE]", "[FLOWER]", "[MUSIC]", "[GAME]", "[BALL]", "[KEY]", "[LOCK]"
    );
    
    private static final List<String> SYMBOLS = Arrays.asList(
        "♥", "♦", "♣", "♠", "♪", "♫", "☀", "☁", "☂", "☃",
        "★", "☆", "♀", "♂", "©", "®", "™", "§", "¶", "†",
        "‡", "•", "‰", "′", "″", "‴", "※", "‼", "⁇", "⁈"
    );
    
    public EmojiPicker(Consumer<String> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
        createPopup();
    }
    
    private void createPopup() {
        popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        
        // 创建主容器
        VBox container = new VBox();
        container.getStyleClass().add("emoji-picker");
        container.setPrefSize(300, 250);
        
        // 创建标签页
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("emoji-tab-pane");
        
        // 添加各个分类标签页
        tabPane.getTabs().addAll(
            createEmojiTab("笑脸", SMILEYS),
            createEmojiTab("手势", GESTURES),
            createEmojiTab("物品", OBJECTS),
            createEmojiTab("符号", SYMBOLS)
        );
        
        container.getChildren().add(tabPane);
        popup.getContent().add(container);
    }
    
    private Tab createEmojiTab(String title, List<String> emojis) {
        Tab tab = new Tab(title);
        tab.setClosable(false);
        
        FlowPane flowPane = new FlowPane();
        flowPane.getStyleClass().add("emoji-flow-pane");
        flowPane.setHgap(5);
        flowPane.setVgap(5);
        flowPane.setPadding(new Insets(10));
        
        for (String emoji : emojis) {
            Button emojiButton = new Button(emoji);
            emojiButton.getStyleClass().add("emoji-button");
            emojiButton.setPrefSize(40, 30);
            emojiButton.setOnAction(e -> {
                if (onEmojiSelected != null) {
                    onEmojiSelected.accept(emoji);
                }
                popup.hide();
            });
            flowPane.getChildren().add(emojiButton);
        }
        
        tab.setContent(flowPane);
        return tab;
    }
    
    public void show(javafx.scene.Node owner, double x, double y) {
        popup.show(owner, x, y);
    }
    
    public void hide() {
        popup.hide();
    }
    
    public boolean isShowing() {
        return popup.isShowing();
    }
}
