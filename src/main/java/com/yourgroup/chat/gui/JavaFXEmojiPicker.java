package com.yourgroup.chat.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.scene.input.KeyCode;

import java.util.*;
import java.util.function.Consumer;

/**
 * JavaFX专用表情选择器 - 解决JavaFX表情渲染问题
 */
public class JavaFXEmojiPicker {
    
    private Popup popup;
    private Consumer<String> onEmojiSelected;
    private TextField searchField;
    private FlowPane currentEmojiPane;
    private List<String> recentEmojis;
    private Map<String, List<EmojiData>> emojiCategories;
    private VBox categoryButtons;
    private String currentCategory = "常用表情";
    
    // 表情数据类
    public static class EmojiData {
        private final String unicode;
        private final String name;
        private final String textFallback;
        private final String description;
        
        public EmojiData(String unicode, String name, String textFallback, String description) {
            this.unicode = unicode;
            this.name = name;
            this.textFallback = textFallback;
            this.description = description;
        }
        
        public String getUnicode() { return unicode; }
        public String getName() { return name; }
        public String getTextFallback() { return textFallback; }
        public String getDescription() { return description; }
    }
    
    // JavaFX兼容的表情数据
    private static final Map<String, List<EmojiData>> EMOJI_CATEGORIES = new LinkedHashMap<>();
    
    static {
        // 常用表情 - 使用简单易显示的表情
        EMOJI_CATEGORIES.put("常用表情", Arrays.asList(
            new EmojiData("☺", "微笑", ":)", "开心的笑脸"),
            new EmojiData("☹", "难过", ":(", "不开心的脸"),
            new EmojiData("♥", "爱心", "<3", "红色爱心"),
            new EmojiData("♡", "空心", "<3", "空心爱心"),
            new EmojiData("★", "实心星", "*", "实心星星"),
            new EmojiData("☆", "空心星", "*", "空心星星"),
            new EmojiData("♪", "音符", "♪", "音乐符号"),
            new EmojiData("♫", "双音符", "♫", "双音乐符号"),
            new EmojiData("☀", "太阳", "sun", "太阳符号"),
            new EmojiData("☁", "云朵", "cloud", "云朵符号"),
            new EmojiData("☂", "雨伞", "umbrella", "雨伞符号"),
            new EmojiData("☃", "雪人", "snowman", "雪人符号"),
            new EmojiData("✓", "对勾", "✓", "正确标记"),
            new EmojiData("✗", "错号", "✗", "错误标记"),
            new EmojiData("✉", "信封", "mail", "邮件符号"),
            new EmojiData("✈", "飞机", "plane", "飞机符号"),
            new EmojiData("♠", "黑桃", "♠", "黑桃符号"),
            new EmojiData("♣", "梅花", "♣", "梅花符号"),
            new EmojiData("♦", "方块", "♦", "方块符号"),
            new EmojiData("♥", "红桃", "♥", "红桃符号")
        ));
        
        // 表情符号
        EMOJI_CATEGORIES.put("表情符号", Arrays.asList(
            new EmojiData("☺", "开心", ":)", "开心笑脸"),
            new EmojiData("☹", "难过", ":(", "难过脸"),
            new EmojiData("☻", "黑笑脸", ":D", "黑色笑脸"),
            new EmojiData("♥", "爱心", "<3", "爱心符号"),
            new EmojiData("♡", "空心", "</3", "空心爱心"),
            new EmojiData("♢", "菱形", "<>", "菱形符号"),
            new EmojiData("♠", "黑桃", "♠", "黑桃"),
            new EmojiData("♣", "梅花", "♣", "梅花"),
            new EmojiData("♦", "方块", "♦", "方块"),
            new EmojiData("♧", "三叶草", "♧", "三叶草")
        ));
        
        // 符号标记
        EMOJI_CATEGORIES.put("符号标记", Arrays.asList(
            new EmojiData("★", "实心星", "*", "实心星星"),
            new EmojiData("☆", "空心星", "*", "空心星星"),
            new EmojiData("✓", "对勾", "✓", "正确"),
            new EmojiData("✗", "错号", "✗", "错误"),
            new EmojiData("✉", "信封", "mail", "邮件"),
            new EmojiData("✈", "飞机", "plane", "飞机"),
            new EmojiData("✎", "铅笔", "edit", "编辑"),
            new EmojiData("✏", "铅笔2", "pencil", "铅笔"),
            new EmojiData("✂", "剪刀", "cut", "剪切"),
            new EmojiData("✆", "电话", "phone", "电话"),
            new EmojiData("✇", "磁带", "tape", "磁带"),
            new EmojiData("✌", "胜利", "V", "胜利手势"),
            new EmojiData("✍", "写字", "write", "写字"),
            new EmojiData("✊", "拳头", "fist", "拳头"),
            new EmojiData("✋", "手掌", "hand", "手掌")
        ));
        
        // 天气符号
        EMOJI_CATEGORIES.put("天气符号", Arrays.asList(
            new EmojiData("☀", "太阳", "sun", "晴天"),
            new EmojiData("☁", "云朵", "cloud", "多云"),
            new EmojiData("☂", "雨伞", "rain", "下雨"),
            new EmojiData("☃", "雪人", "snow", "下雪"),
            new EmojiData("☄", "彗星", "comet", "彗星"),
            new EmojiData("★", "星星", "star", "星星"),
            new EmojiData("☆", "空星", "star", "空心星"),
            new EmojiData("☽", "月亮", "moon", "月亮"),
            new EmojiData("☾", "月牙", "moon", "月牙"),
            new EmojiData("⚡", "闪电", "lightning", "闪电")
        ));
        
        // 音乐符号
        EMOJI_CATEGORIES.put("音乐符号", Arrays.asList(
            new EmojiData("♪", "音符", "♪", "单音符"),
            new EmojiData("♫", "双音符", "♫", "双音符"),
            new EmojiData("♬", "三音符", "♬", "三音符"),
            new EmojiData("♭", "降号", "♭", "降音符"),
            new EmojiData("♮", "还原", "♮", "还原符"),
            new EmojiData("♯", "升号", "♯", "升音符")
        ));
        
        // 文字表情
        EMOJI_CATEGORIES.put("文字表情", Arrays.asList(
            new EmojiData(":)", "开心", ":)", "开心笑脸"),
            new EmojiData(":(", "难过", ":(", "难过脸"),
            new EmojiData(":D", "大笑", ":D", "大笑"),
            new EmojiData(":P", "吐舌", ":P", "吐舌头"),
            new EmojiData(";)", "眨眼", ";)", "眨眼"),
            new EmojiData(":o", "惊讶", ":o", "惊讶"),
            new EmojiData(":|", "无表情", ":|", "面无表情"),
            new EmojiData(":/", "困惑", ":/", "困惑"),
            new EmojiData(":*", "亲吻", ":*", "飞吻"),
            new EmojiData("<3", "爱心", "<3", "爱心"),
            new EmojiData("^_^", "开心", "^_^", "开心"),
            new EmojiData("-_-", "无语", "-_-", "无语"),
            new EmojiData("o_O", "震惊", "o_O", "震惊"),
            new EmojiData(">:(", "愤怒", ">:(", "愤怒"),
            new EmojiData("XD", "大笑", "XD", "哈哈大笑"),
            new EmojiData("=D", "开心", "=D", "很开心"),
            new EmojiData("</3", "破心", "</3", "破碎的心"),
            new EmojiData("T_T", "哭泣", "T_T", "哭泣"),
            new EmojiData("\\o/", "举手", "\\o/", "举双手"),
            new EmojiData("$_$", "金钱", "$_$", "金钱眼")
        ));
    }
    
    public JavaFXEmojiPicker(Consumer<String> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
        this.recentEmojis = new ArrayList<>();
        this.emojiCategories = new LinkedHashMap<>(EMOJI_CATEGORIES);
        createPopup();
    }
    
    private void createPopup() {
        popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        
        // 主容器
        VBox container = new VBox();
        container.getStyleClass().add("javafx-emoji-picker");
        container.setPrefSize(400, 350);
        
        // 搜索框
        createSearchBox(container);
        
        // 内容区域
        HBox contentArea = new HBox();
        contentArea.setSpacing(0);
        
        // 左侧分类按钮
        createCategoryButtons(contentArea);
        
        // 右侧表情区域
        createEmojiArea(contentArea);
        
        container.getChildren().add(contentArea);
        popup.getContent().add(container);
        
        // 默认显示第一个分类
        showCategory(currentCategory);
    }
    
    private void createSearchBox(VBox container) {
        searchField = new TextField();
        searchField.setPromptText("搜索表情...");
        searchField.getStyleClass().add("javafx-emoji-search");
        searchField.setPrefHeight(35);
        
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.trim().isEmpty()) {
                showCategory(currentCategory);
            } else {
                searchEmojis(newText.trim());
            }
        });
        
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                popup.hide();
            }
        });
        
        container.getChildren().add(searchField);
    }
    
    private void createCategoryButtons(HBox contentArea) {
        categoryButtons = new VBox();
        categoryButtons.getStyleClass().add("javafx-emoji-categories");
        categoryButtons.setPrefWidth(50);
        categoryButtons.setSpacing(5);
        categoryButtons.setPadding(new Insets(10, 5, 10, 5));
        
        // 分类图标映射
        Map<String, String> categoryIcons = new LinkedHashMap<>();
        categoryIcons.put("常用表情", "☺");
        categoryIcons.put("表情符号", "♥");
        categoryIcons.put("符号标记", "★");
        categoryIcons.put("天气符号", "☀");
        categoryIcons.put("音乐符号", "♪");
        categoryIcons.put("文字表情", ":)");
        
        // 最近使用按钮
        if (!recentEmojis.isEmpty()) {
            Button recentButton = createCategoryButton("⏰", "最近使用");
            recentButton.setOnAction(e -> showRecentEmojis());
            categoryButtons.getChildren().add(recentButton);
        }
        
        // 分类按钮
        for (Map.Entry<String, String> entry : categoryIcons.entrySet()) {
            String category = entry.getKey();
            String icon = entry.getValue();
            Button button = createCategoryButton(icon, category);
            button.setOnAction(e -> {
                currentCategory = category;
                showCategory(category);
                updateCategorySelection(button);
            });
            categoryButtons.getChildren().add(button);
        }
        
        contentArea.getChildren().add(categoryButtons);
    }
    
    private Button createCategoryButton(String icon, String tooltip) {
        Button button = new Button(icon);
        button.getStyleClass().add("javafx-emoji-category-btn");
        button.setPrefSize(40, 40);
        button.setTooltip(new Tooltip(tooltip));
        
        // 设置字体确保符号正确显示
        button.setFont(Font.font("Arial Unicode MS", 16));
        
        return button;
    }
    
    private void createEmojiArea(HBox contentArea) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("javafx-emoji-scroll");
        scrollPane.setPrefSize(340, 280);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        currentEmojiPane = new FlowPane();
        currentEmojiPane.getStyleClass().add("javafx-emoji-flow");
        currentEmojiPane.setHgap(5);
        currentEmojiPane.setVgap(5);
        currentEmojiPane.setPadding(new Insets(10));
        
        scrollPane.setContent(currentEmojiPane);
        contentArea.getChildren().add(scrollPane);
    }
    
    private void showCategory(String category) {
        currentEmojiPane.getChildren().clear();
        List<EmojiData> emojis = emojiCategories.get(category);
        if (emojis != null) {
            for (EmojiData emojiData : emojis) {
                Button emojiButton = createEmojiButton(emojiData);
                currentEmojiPane.getChildren().add(emojiButton);
            }
        }
    }
    
    private void showRecentEmojis() {
        currentEmojiPane.getChildren().clear();
        for (String emoji : recentEmojis) {
            EmojiData data = new EmojiData(emoji, "最近使用", emoji, "最近使用的表情");
            Button emojiButton = createEmojiButton(data);
            currentEmojiPane.getChildren().add(emojiButton);
        }
    }
    
    private void searchEmojis(String query) {
        currentEmojiPane.getChildren().clear();
        String lowerQuery = query.toLowerCase();
        
        for (List<EmojiData> emojis : emojiCategories.values()) {
            for (EmojiData emojiData : emojis) {
                if (emojiData.getName().toLowerCase().contains(lowerQuery) ||
                    emojiData.getDescription().toLowerCase().contains(lowerQuery) ||
                    emojiData.getTextFallback().toLowerCase().contains(lowerQuery)) {
                    Button emojiButton = createEmojiButton(emojiData);
                    currentEmojiPane.getChildren().add(emojiButton);
                }
            }
        }
    }
    
    private Button createEmojiButton(EmojiData emojiData) {
        Button button = new Button(emojiData.getUnicode());
        button.getStyleClass().add("javafx-emoji-btn");
        button.setPrefSize(35, 35);
        button.setTooltip(new Tooltip(emojiData.getDescription()));
        
        // 设置字体确保符号正确显示
        button.setFont(Font.font("Arial Unicode MS", 18));
        
        button.setOnAction(e -> {
            if (onEmojiSelected != null) {
                // 使用Unicode符号
                onEmojiSelected.accept(emojiData.getUnicode());
                addToRecent(emojiData.getUnicode());
            }
            popup.hide();
        });
        return button;
    }
    
    private void addToRecent(String emoji) {
        recentEmojis.remove(emoji); // 移除已存在的
        recentEmojis.add(0, emoji); // 添加到开头
        if (recentEmojis.size() > 30) { // 限制最近使用的数量
            recentEmojis = recentEmojis.subList(0, 30);
        }
    }
    
    private void updateCategorySelection(Button selectedButton) {
        // 重置所有按钮样式
        for (javafx.scene.Node node : categoryButtons.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("selected");
            }
        }
        // 设置选中样式
        selectedButton.getStyleClass().add("selected");
    }
    
    public void show(javafx.scene.Node owner, double x, double y) {
        popup.show(owner, x, y);
        searchField.requestFocus();
    }
    
    public void hide() {
        popup.hide();
    }
    
    public boolean isShowing() {
        return popup.isShowing();
    }
}
