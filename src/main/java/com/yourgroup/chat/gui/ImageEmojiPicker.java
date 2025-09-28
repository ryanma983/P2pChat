package com.yourgroup.chat.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.scene.input.KeyCode;

import java.util.*;
import java.util.function.Consumer;

/**
 * 基于图片的表情选择器 - 彻底解决表情显示问题
 */
public class ImageEmojiPicker {
    
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
        private final String code;
        private final String name;
        private final String imagePath;
        private final String description;
        
        public EmojiData(String code, String name, String imagePath, String description) {
            this.code = code;
            this.name = name;
            this.imagePath = imagePath;
            this.description = description;
        }
        
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getImagePath() { return imagePath; }
        public String getDescription() { return description; }
    }
    
    // 基于图片的表情数据
    private static final Map<String, List<EmojiData>> EMOJI_CATEGORIES = new LinkedHashMap<>();
    
    static {
        // 常用表情 - 使用生成的PNG图片
        EMOJI_CATEGORIES.put("常用表情", Arrays.asList(
            new EmojiData(":happy:", "开心", "/images/emojis/happy.png", "开心的笑脸"),
            new EmojiData(":sad:", "难过", "/images/emojis/sad.png", "难过的脸"),
            new EmojiData(":love:", "爱心眼", "/images/emojis/love.png", "爱心眼表情"),
            new EmojiData(":laugh:", "大笑", "/images/emojis/laugh.png", "笑出眼泪"),
            new EmojiData(":wink:", "眨眼", "/images/emojis/wink.png", "眨眼吐舌"),
            new EmojiData(":angry:", "愤怒", "/images/emojis/angry.png", "愤怒的脸"),
            new EmojiData(":surprised:", "惊讶", "/images/emojis/surprised.png", "惊讶表情"),
            new EmojiData(":cool:", "酷", "/images/emojis/cool.png", "戴墨镜很酷"),
            new EmojiData(":thinking:", "思考", "/images/emojis/thinking.png", "思考表情"),
            new EmojiData(":kiss:", "飞吻", "/images/emojis/kiss.png", "飞吻表情")
        ));
        
        // 表情符号 - 更多表情
        EMOJI_CATEGORIES.put("表情符号", Arrays.asList(
            new EmojiData(":smile:", "微笑", "/images/emojis/happy.png", "微笑"),
            new EmojiData(":cry:", "哭泣", "/images/emojis/sad.png", "哭泣"),
            new EmojiData(":heart_eyes:", "爱心", "/images/emojis/love.png", "爱心眼"),
            new EmojiData(":joy:", "喜悦", "/images/emojis/laugh.png", "喜极而泣"),
            new EmojiData(":winking:", "眨眼", "/images/emojis/wink.png", "调皮眨眼"),
            new EmojiData(":rage:", "暴怒", "/images/emojis/angry.png", "非常愤怒"),
            new EmojiData(":shocked:", "震惊", "/images/emojis/surprised.png", "非常震惊"),
            new EmojiData(":sunglasses:", "墨镜", "/images/emojis/cool.png", "戴墨镜"),
            new EmojiData(":hmm:", "嗯", "/images/emojis/thinking.png", "思考中"),
            new EmojiData(":kiss_heart:", "亲亲", "/images/emojis/kiss.png", "亲吻")
        ));
        
        // 动作表情
        EMOJI_CATEGORIES.put("动作表情", Arrays.asList(
            new EmojiData(":thumbs_up:", "点赞", "/images/emojis/happy.png", "点赞"),
            new EmojiData(":thumbs_down:", "点踩", "/images/emojis/sad.png", "点踩"),
            new EmojiData(":clap:", "鼓掌", "/images/emojis/laugh.png", "鼓掌"),
            new EmojiData(":wave:", "挥手", "/images/emojis/wink.png", "挥手"),
            new EmojiData(":peace:", "胜利", "/images/emojis/cool.png", "胜利手势"),
            new EmojiData(":fist:", "拳头", "/images/emojis/angry.png", "握拳"),
            new EmojiData(":ok:", "OK", "/images/emojis/surprised.png", "OK手势"),
            new EmojiData(":point:", "指向", "/images/emojis/thinking.png", "指向"),
            new EmojiData(":hug:", "拥抱", "/images/emojis/love.png", "拥抱"),
            new EmojiData(":high_five:", "击掌", "/images/emojis/kiss.png", "击掌")
        ));
        
        // 文字表情 - 经典ASCII
        EMOJI_CATEGORIES.put("文字表情", Arrays.asList(
            new EmojiData(":)", "开心", "/images/emojis/happy.png", "开心 :)"),
            new EmojiData(":(", "难过", "/images/emojis/sad.png", "难过 :("),
            new EmojiData(":D", "大笑", "/images/emojis/laugh.png", "大笑 :D"),
            new EmojiData(";)", "眨眼", "/images/emojis/wink.png", "眨眼 ;)"),
            new EmojiData(":P", "吐舌", "/images/emojis/wink.png", "吐舌 :P"),
            new EmojiData(">:(", "愤怒", "/images/emojis/angry.png", "愤怒 >:("),
            new EmojiData(":o", "惊讶", "/images/emojis/surprised.png", "惊讶 :o"),
            new EmojiData("8)", "酷", "/images/emojis/cool.png", "酷 8)"),
            new EmojiData(":/", "困惑", "/images/emojis/thinking.png", "困惑 :/"),
            new EmojiData(":*", "亲吻", "/images/emojis/kiss.png", "亲吻 :*"),
            new EmojiData("<3", "爱心", "/images/emojis/love.png", "爱心 <3"),
            new EmojiData("^_^", "开心", "/images/emojis/happy.png", "开心 ^_^"),
            new EmojiData("-_-", "无语", "/images/emojis/sad.png", "无语 -_-"),
            new EmojiData("XD", "哈哈", "/images/emojis/laugh.png", "哈哈 XD"),
            new EmojiData("o_O", "震惊", "/images/emojis/surprised.png", "震惊 o_O")
        ));
    }
    
    public ImageEmojiPicker(Consumer<String> onEmojiSelected) {
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
        container.getStyleClass().add("image-emoji-picker");
        container.setPrefSize(420, 380);
        
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
        searchField.getStyleClass().add("image-emoji-search");
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
        categoryButtons.getStyleClass().add("image-emoji-categories");
        categoryButtons.setPrefWidth(60);
        categoryButtons.setSpacing(8);
        categoryButtons.setPadding(new Insets(10, 8, 10, 8));
        
        // 分类图标映射 - 使用表情图片作为分类图标
        Map<String, String> categoryIcons = new LinkedHashMap<>();
        categoryIcons.put("常用表情", "/images/emojis/happy.png");
        categoryIcons.put("表情符号", "/images/emojis/love.png");
        categoryIcons.put("动作表情", "/images/emojis/cool.png");
        categoryIcons.put("文字表情", "/images/emojis/wink.png");
        
        // 最近使用按钮
        if (!recentEmojis.isEmpty()) {
            Button recentButton = createCategoryButton("/images/emojis/thinking.png", "最近使用");
            recentButton.setOnAction(e -> showRecentEmojis());
            categoryButtons.getChildren().add(recentButton);
        }
        
        // 分类按钮
        for (Map.Entry<String, String> entry : categoryIcons.entrySet()) {
            String category = entry.getKey();
            String iconPath = entry.getValue();
            Button button = createCategoryButton(iconPath, category);
            button.setOnAction(e -> {
                currentCategory = category;
                showCategory(category);
                updateCategorySelection(button);
            });
            categoryButtons.getChildren().add(button);
        }
        
        contentArea.getChildren().add(categoryButtons);
    }
    
    private Button createCategoryButton(String iconPath, String tooltip) {
        Button button = new Button();
        button.getStyleClass().add("image-emoji-category-btn");
        button.setPrefSize(44, 44);
        button.setTooltip(new Tooltip(tooltip));
        
        // 加载图标
        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(28);
            iconView.setFitHeight(28);
            iconView.setPreserveRatio(true);
            iconView.setSmooth(true);
            button.setGraphic(iconView);
        } catch (Exception e) {
            // 如果图片加载失败，显示文字
            button.setText("😀");
        }
        
        return button;
    }
    
    private void createEmojiArea(HBox contentArea) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("image-emoji-scroll");
        scrollPane.setPrefSize(350, 310);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        currentEmojiPane = new FlowPane();
        currentEmojiPane.getStyleClass().add("image-emoji-flow");
        currentEmojiPane.setHgap(6);
        currentEmojiPane.setVgap(6);
        currentEmojiPane.setPadding(new Insets(12));
        
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
        for (String emojiCode : recentEmojis) {
            // 查找对应的表情数据
            EmojiData emojiData = findEmojiByCode(emojiCode);
            if (emojiData != null) {
                Button emojiButton = createEmojiButton(emojiData);
                currentEmojiPane.getChildren().add(emojiButton);
            }
        }
    }
    
    private EmojiData findEmojiByCode(String code) {
        for (List<EmojiData> emojis : emojiCategories.values()) {
            for (EmojiData emoji : emojis) {
                if (emoji.getCode().equals(code)) {
                    return emoji;
                }
            }
        }
        return null;
    }
    
    private void searchEmojis(String query) {
        currentEmojiPane.getChildren().clear();
        String lowerQuery = query.toLowerCase();
        
        for (List<EmojiData> emojis : emojiCategories.values()) {
            for (EmojiData emojiData : emojis) {
                if (emojiData.getName().toLowerCase().contains(lowerQuery) ||
                    emojiData.getDescription().toLowerCase().contains(lowerQuery) ||
                    emojiData.getCode().toLowerCase().contains(lowerQuery)) {
                    Button emojiButton = createEmojiButton(emojiData);
                    currentEmojiPane.getChildren().add(emojiButton);
                }
            }
        }
    }
    
    private Button createEmojiButton(EmojiData emojiData) {
        Button button = new Button();
        button.getStyleClass().add("image-emoji-btn");
        button.setPrefSize(40, 40);
        button.setTooltip(new Tooltip(emojiData.getDescription()));
        
        // 加载表情图片
        try {
            Image emojiImage = new Image(getClass().getResourceAsStream(emojiData.getImagePath()));
            ImageView imageView = new ImageView(emojiImage);
            imageView.setFitWidth(32);
            imageView.setFitHeight(32);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            button.setGraphic(imageView);
        } catch (Exception e) {
            // 如果图片加载失败，显示代码
            button.setText(emojiData.getCode());
        }
        
        button.setOnAction(e -> {
            if (onEmojiSelected != null) {
                // 发送表情代码而不是Unicode字符
                onEmojiSelected.accept(emojiData.getCode());
                addToRecent(emojiData.getCode());
            }
            popup.hide();
        });
        
        return button;
    }
    
    private void addToRecent(String emojiCode) {
        recentEmojis.remove(emojiCode); // 移除已存在的
        recentEmojis.add(0, emojiCode); // 添加到开头
        if (recentEmojis.size() > 20) { // 限制最近使用的数量
            recentEmojis = recentEmojis.subList(0, 20);
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
