package com.yourgroup.chat.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.scene.input.KeyCode;

import java.util.*;
import java.util.function.Consumer;

/**
 * 兼容性更好的表情选择器 - 混合使用Unicode表情和文字表情
 */
public class CompatibleEmojiPicker {
    
    private Popup popup;
    private Consumer<String> onEmojiSelected;
    private TextField searchField;
    private FlowPane currentEmojiPane;
    private List<String> recentEmojis;
    private Map<String, List<EmojiItem>> emojiCategories;
    private VBox categoryButtons;
    private String currentCategory = "常用表情";
    
    // 表情项目类
    public static class EmojiItem {
        private final String emoji;
        private final String name;
        private final String fallback;
        
        public EmojiItem(String emoji, String name, String fallback) {
            this.emoji = emoji;
            this.name = name;
            this.fallback = fallback;
        }
        
        public String getEmoji() { return emoji; }
        public String getName() { return name; }
        public String getFallback() { return fallback; }
        
        public String getDisplayText() {
            // 尝试使用Unicode表情，如果不支持则使用文字替代
            return emoji != null && !emoji.isEmpty() ? emoji : fallback;
        }
    }
    
    // 兼容性表情分类
    private static final Map<String, List<EmojiItem>> EMOJI_CATEGORIES = new LinkedHashMap<>();
    
    static {
        // 常用表情 - 混合Unicode和文字
        EMOJI_CATEGORIES.put("常用表情", Arrays.asList(
            new EmojiItem("😀", "开心", ":D"),
            new EmojiItem("😃", "大笑", ":)"),
            new EmojiItem("😄", "哈哈", "^_^"),
            new EmojiItem("😁", "嘿嘿", "XD"),
            new EmojiItem("😆", "笑哭", "=D"),
            new EmojiItem("😅", "汗", "^^;"),
            new EmojiItem("😂", "笑哭", ":'D"),
            new EmojiItem("🙂", "微笑", ":)"),
            new EmojiItem("😉", "眨眼", ";)"),
            new EmojiItem("😊", "开心", "^_^"),
            new EmojiItem("😇", "天使", "O:)"),
            new EmojiItem("😍", "爱心眼", "<3"),
            new EmojiItem("😘", "飞吻", ":*"),
            new EmojiItem("😗", "亲亲", ":*"),
            new EmojiItem("😙", "亲", ":-*"),
            new EmojiItem("😚", "亲吻", ":*"),
            new EmojiItem("🙃", "倒脸", "(:"),
            new EmojiItem("😋", "美味", ":P"),
            new EmojiItem("😛", "吐舌", ":P"),
            new EmojiItem("😜", "调皮", ";P"),
            new EmojiItem("🤪", "疯狂", "XP"),
            new EmojiItem("😝", "吐舌", "XP"),
            new EmojiItem("🤑", "财迷", "$_$"),
            new EmojiItem("🤗", "拥抱", "\\o/"),
            new EmojiItem("🤭", "捂嘴", ":-X"),
            new EmojiItem("🤫", "嘘", ":-X"),
            new EmojiItem("🤔", "思考", ":-?"),
            new EmojiItem("🤐", "闭嘴", ":-X"),
            new EmojiItem("😐", "面无表情", ":|"),
            new EmojiItem("😑", "无语", "-_-")
        ));
        
        // 情绪表情
        EMOJI_CATEGORIES.put("情绪表情", Arrays.asList(
            new EmojiItem("😶", "无言", ":|"),
            new EmojiItem("😏", "得意", ":/"),
            new EmojiItem("😒", "不爽", ":/"),
            new EmojiItem("🙄", "翻白眼", "-_-"),
            new EmojiItem("😬", "尴尬", ":-S"),
            new EmojiItem("🤥", "说谎", ":-L"),
            new EmojiItem("😔", "失落", ":("),
            new EmojiItem("😪", "困", "-_-"),
            new EmojiItem("🤤", "流口水", "*_*"),
            new EmojiItem("😴", "睡觉", "zzZ"),
            new EmojiItem("😷", "口罩", ":-#"),
            new EmojiItem("🤒", "发烧", ":-#"),
            new EmojiItem("🤕", "受伤", ":-#"),
            new EmojiItem("🤢", "恶心", "X-("),
            new EmojiItem("🤮", "呕吐", "X-("),
            new EmojiItem("🤧", "打喷嚏", ":-#"),
            new EmojiItem("🥵", "热", ":-#"),
            new EmojiItem("🥶", "冷", ":-#"),
            new EmojiItem("😵", "晕", "X-("),
            new EmojiItem("🤯", "爆炸", "X-O"),
            new EmojiItem("🤠", "牛仔", "B-)"),
            new EmojiItem("🥳", "派对", "\\o/"),
            new EmojiItem("😎", "酷", "B-)"),
            new EmojiItem("🤓", "书呆子", "8-)"),
            new EmojiItem("🧐", "单片眼镜", ":-|"),
            new EmojiItem("😕", "困惑", ":/"),
            new EmojiItem("😟", "担心", ":("),
            new EmojiItem("🙁", "皱眉", ":("),
            new EmojiItem("☹️", "不开心", ":("),
            new EmojiItem("😮", "惊讶", ":O")
        ));
        
        // 手势动作
        EMOJI_CATEGORIES.put("手势动作", Arrays.asList(
            new EmojiItem("👋", "挥手", "\\o/"),
            new EmojiItem("🤚", "举手", "\\o"),
            new EmojiItem("🖐️", "手掌", "\\o"),
            new EmojiItem("✋", "停", "STOP"),
            new EmojiItem("🖖", "长寿", "\\//"),
            new EmojiItem("👌", "OK", "OK"),
            new EmojiItem("🤏", "一点点", "tiny"),
            new EmojiItem("✌️", "胜利", "V"),
            new EmojiItem("🤞", "祈祷", "pray"),
            new EmojiItem("🤟", "爱你", "ILY"),
            new EmojiItem("🤘", "摇滚", "\\m/"),
            new EmojiItem("🤙", "打电话", "call"),
            new EmojiItem("👈", "左指", "<-"),
            new EmojiItem("👉", "右指", "->"),
            new EmojiItem("👆", "上指", "^"),
            new EmojiItem("👇", "下指", "v"),
            new EmojiItem("☝️", "食指", "!"),
            new EmojiItem("👍", "赞", "+1"),
            new EmojiItem("👎", "踩", "-1"),
            new EmojiItem("👊", "拳头", "punch"),
            new EmojiItem("✊", "举拳", "fist"),
            new EmojiItem("🤛", "左拳", "punch"),
            new EmojiItem("🤜", "右拳", "punch"),
            new EmojiItem("👏", "鼓掌", "clap"),
            new EmojiItem("🙌", "举双手", "\\o/"),
            new EmojiItem("👐", "张开手", "open"),
            new EmojiItem("🤲", "捧", "hold"),
            new EmojiItem("🤝", "握手", "shake"),
            new EmojiItem("🙏", "祈祷", "pray"),
            new EmojiItem("💪", "肌肉", "strong")
        ));
        
        // 爱心符号
        EMOJI_CATEGORIES.put("爱心符号", Arrays.asList(
            new EmojiItem("❤️", "红心", "<3"),
            new EmojiItem("🧡", "橙心", "<3"),
            new EmojiItem("💛", "黄心", "<3"),
            new EmojiItem("💚", "绿心", "<3"),
            new EmojiItem("💙", "蓝心", "<3"),
            new EmojiItem("💜", "紫心", "<3"),
            new EmojiItem("🖤", "黑心", "</3"),
            new EmojiItem("🤍", "白心", "<3"),
            new EmojiItem("🤎", "棕心", "<3"),
            new EmojiItem("💔", "破碎的心", "</3"),
            new EmojiItem("❣️", "心叹号", "<3!"),
            new EmojiItem("💕", "两颗心", "<3<3"),
            new EmojiItem("💞", "旋转心", "<3"),
            new EmojiItem("💓", "跳动心", "<3"),
            new EmojiItem("💗", "成长心", "<3"),
            new EmojiItem("💖", "闪亮心", "<3"),
            new EmojiItem("💘", "箭心", "<3"),
            new EmojiItem("💝", "礼物心", "<3"),
            new EmojiItem("💟", "心装饰", "<3"),
            new EmojiItem("💋", "唇印", ":*")
        ));
        
        // 常用符号
        EMOJI_CATEGORIES.put("常用符号", Arrays.asList(
            new EmojiItem("⭐", "星星", "*"),
            new EmojiItem("🌟", "闪亮星", "*"),
            new EmojiItem("✨", "闪光", "*"),
            new EmojiItem("💫", "眩晕", "*"),
            new EmojiItem("⚡", "闪电", "!"),
            new EmojiItem("🔥", "火", "fire"),
            new EmojiItem("💯", "百分百", "100"),
            new EmojiItem("💢", "愤怒", "!!"),
            new EmojiItem("💨", "冲刺", "~~~"),
            new EmojiItem("💦", "汗滴", "..."),
            new EmojiItem("🎉", "庆祝", "party"),
            new EmojiItem("🎊", "彩带", "party"),
            new EmojiItem("🎈", "气球", "balloon"),
            new EmojiItem("🎁", "礼物", "gift"),
            new EmojiItem("🏆", "奖杯", "trophy"),
            new EmojiItem("🥇", "金牌", "1st"),
            new EmojiItem("🥈", "银牌", "2nd"),
            new EmojiItem("🥉", "铜牌", "3rd"),
            new EmojiItem("🎯", "靶心", "target"),
            new EmojiItem("🎪", "马戏团", "circus")
        ));
        
        // 文字表情（备用）
        EMOJI_CATEGORIES.put("文字表情", Arrays.asList(
            new EmojiItem("", "开心", ":)"),
            new EmojiItem("", "难过", ":("),
            new EmojiItem("", "大笑", ":D"),
            new EmojiItem("", "吐舌", ":P"),
            new EmojiItem("", "眨眼", ";)"),
            new EmojiItem("", "惊讶", ":o"),
            new EmojiItem("", "面无表情", ":|"),
            new EmojiItem("", "困惑", ":/"),
            new EmojiItem("", "亲吻", ":*"),
            new EmojiItem("", "爱心", "<3"),
            new EmojiItem("", "开心", "^_^"),
            new EmojiItem("", "无语", "-_-"),
            new EmojiItem("", "震惊", "o_O"),
            new EmojiItem("", "愤怒", ">:("),
            new EmojiItem("", "生气", ":@"),
            new EmojiItem("", "大笑", "XD"),
            new EmojiItem("", "等于开心", "=D"),
            new EmojiItem("", "等于微笑", "=)"),
            new EmojiItem("", "等于难过", "=("),
            new EmojiItem("", "等于吐舌", "=P"),
            new EmojiItem("", "破碎的心", "</3"),
            new EmojiItem("", "哭泣", "T_T"),
            new EmojiItem("", "大震惊", "O_O"),
            new EmojiItem("", "酷", "B)"),
            new EmojiItem("", "酷带横线", "B-)"),
            new EmojiItem("", "举手", "\\o/"),
            new EmojiItem("", "右手", "o/"),
            new EmojiItem("", "左手", "\\o"),
            new EmojiItem("", "摇滚", "\\m/"),
            new EmojiItem("", "金钱眼", "$_$")
        ));
    }
    
    public CompatibleEmojiPicker(Consumer<String> onEmojiSelected) {
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
        container.getStyleClass().add("compatible-emoji-picker");
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
        searchField.getStyleClass().add("compatible-emoji-search");
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
        categoryButtons.getStyleClass().add("compatible-emoji-categories");
        categoryButtons.setPrefWidth(50);
        categoryButtons.setSpacing(5);
        categoryButtons.setPadding(new Insets(10, 5, 10, 5));
        
        // 分类图标映射（使用文字图标确保兼容性）
        Map<String, String> categoryIcons = new LinkedHashMap<>();
        categoryIcons.put("常用表情", ":)");
        categoryIcons.put("情绪表情", ":(");
        categoryIcons.put("手势动作", "\\o/");
        categoryIcons.put("爱心符号", "<3");
        categoryIcons.put("常用符号", "*");
        categoryIcons.put("文字表情", "^_^");
        
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
        button.getStyleClass().add("compatible-emoji-category-btn");
        button.setPrefSize(40, 40);
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }
    
    private void createEmojiArea(HBox contentArea) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("compatible-emoji-scroll");
        scrollPane.setPrefSize(340, 280);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        currentEmojiPane = new FlowPane();
        currentEmojiPane.getStyleClass().add("compatible-emoji-flow");
        currentEmojiPane.setHgap(5);
        currentEmojiPane.setVgap(5);
        currentEmojiPane.setPadding(new Insets(10));
        
        scrollPane.setContent(currentEmojiPane);
        contentArea.getChildren().add(scrollPane);
    }
    
    private void showCategory(String category) {
        currentEmojiPane.getChildren().clear();
        List<EmojiItem> emojis = emojiCategories.get(category);
        if (emojis != null) {
            for (EmojiItem emojiItem : emojis) {
                Button emojiButton = createEmojiButton(emojiItem);
                currentEmojiPane.getChildren().add(emojiButton);
            }
        }
    }
    
    private void showRecentEmojis() {
        currentEmojiPane.getChildren().clear();
        for (String emoji : recentEmojis) {
            EmojiItem item = new EmojiItem(emoji, "最近使用", emoji);
            Button emojiButton = createEmojiButton(item);
            currentEmojiPane.getChildren().add(emojiButton);
        }
    }
    
    private void searchEmojis(String query) {
        currentEmojiPane.getChildren().clear();
        String lowerQuery = query.toLowerCase();
        
        for (List<EmojiItem> emojis : emojiCategories.values()) {
            for (EmojiItem emojiItem : emojis) {
                if (emojiItem.getName().toLowerCase().contains(lowerQuery) ||
                    emojiItem.getFallback().toLowerCase().contains(lowerQuery)) {
                    Button emojiButton = createEmojiButton(emojiItem);
                    currentEmojiPane.getChildren().add(emojiButton);
                }
            }
        }
    }
    
    private Button createEmojiButton(EmojiItem emojiItem) {
        String displayText = emojiItem.getDisplayText();
        Button button = new Button(displayText);
        button.getStyleClass().add("compatible-emoji-btn");
        button.setPrefSize(35, 35);
        button.setTooltip(new Tooltip(emojiItem.getName()));
        
        button.setOnAction(e -> {
            if (onEmojiSelected != null) {
                // 优先使用Unicode表情，如果显示有问题则使用文字替代
                String selectedEmoji = emojiItem.getEmoji() != null && !emojiItem.getEmoji().isEmpty() 
                    ? emojiItem.getEmoji() : emojiItem.getFallback();
                onEmojiSelected.accept(selectedEmoji);
                addToRecent(selectedEmoji);
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
