package com.yourgroup.chat.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.scene.input.KeyCode;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Discord风格的表情选择器
 */
public class DiscordEmojiPicker {
    
    private Popup popup;
    private Consumer<String> onEmojiSelected;
    private TextField searchField;
    private FlowPane currentEmojiPane;
    private List<String> recentEmojis;
    private Map<String, List<String>> emojiCategories;
    private VBox categoryButtons;
    private String currentCategory = "笑脸与情感";
    
    // Discord风格的表情分类
    private static final Map<String, List<String>> EMOJI_CATEGORIES = new LinkedHashMap<>();
    
    static {
        // 笑脸与情感
        EMOJI_CATEGORIES.put("笑脸与情感", Arrays.asList(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
            "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "☺️", "😚",
            "😙", "🥲", "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭",
            "🤫", "🤔", "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄",
            "😬", "🤥", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢",
            "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "🥸",
            "😎", "🤓", "🧐", "😕", "😟", "🙁", "☹️", "😮", "😯", "😲",
            "😳", "🥺", "😦", "😧", "😨", "😰", "😥", "😢", "😭", "😱",
            "😖", "😣", "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠",
            "🤬", "😈", "👿", "💀", "☠️", "💩", "🤡", "👹", "👺", "👻"
        ));
        
        // 人物与身体
        EMOJI_CATEGORIES.put("人物与身体", Arrays.asList(
            "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
            "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍",
            "👎", "👊", "✊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝",
            "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂",
            "🦻", "👃", "🧠", "🫀", "🫁", "🦷", "🦴", "👀", "👁️", "👅",
            "👄", "💋", "🩸", "👶", "🧒", "👦", "👧", "🧑", "👱", "👨",
            "🧔", "👩", "🧓", "👴", "👵", "🙍", "🙎", "🙅", "🙆", "💁",
            "🙋", "🧏", "🙇", "🤦", "🤷", "👮", "🕵️", "💂", "🥷", "👷"
        ));
        
        // 动物与自然
        EMOJI_CATEGORIES.put("动物与自然", Arrays.asList(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨",
            "🐯", "🦁", "🐮", "🐷", "🐽", "🐸", "🐵", "🙈", "🙉", "🙊",
            "🐒", "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉",
            "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🪱", "🐛", "🦋", "🐌",
            "🐞", "🐜", "🪰", "🪲", "🪳", "🦟", "🦗", "🕷️", "🕸️", "🦂",
            "🐢", "🐍", "🦎", "🦖", "🦕", "🐙", "🦑", "🦐", "🦞", "🦀",
            "🐡", "🐠", "🐟", "🐬", "🐳", "🐋", "🦈", "🐊", "🐅", "🐆",
            "🦓", "🦍", "🦧", "🐘", "🦛", "🦏", "🐪", "🐫", "🦒", "🦘"
        ));
        
        // 食物与饮料
        EMOJI_CATEGORIES.put("食物与饮料", Arrays.asList(
            "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈",
            "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦",
            "🥬", "🥒", "🌶️", "🫑", "🌽", "🥕", "🫒", "🧄", "🧅", "🥔",
            "🍠", "🥐", "🥖", "🍞", "🥨", "🥯", "🥞", "🧇", "🧀", "🍖",
            "🍗", "🥩", "🥓", "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯",
            "🫔", "🥙", "🧆", "🥚", "🍳", "🥘", "🍲", "🫕", "🥣", "🥗",
            "🍿", "🧈", "🧂", "🥫", "🍱", "🍘", "🍙", "🍚", "🍛", "🍜",
            "🍝", "🍠", "🍢", "🍣", "🍤", "🍥", "🥮", "🍡", "🥟", "🥠"
        ));
        
        // 活动
        EMOJI_CATEGORIES.put("活动", Arrays.asList(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
            "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥅", "⛳",
            "🪁", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛼", "🛷",
            "⛸️", "🥌", "🎿", "⛷️", "🏂", "🪂", "🏋️", "🤼", "🤸", "⛹️",
            "🤺", "🏇", "🧘", "🏄", "🏊", "🤽", "🚣", "🧗", "🚵", "🚴",
            "🏆", "🥇", "🥈", "🥉", "🏅", "🎖️", "🏵️", "🎗️", "🎫", "🎟️",
            "🎪", "🤹", "🎭", "🩰", "🎨", "🎬", "🎤", "🎧", "🎼", "🎵",
            "🎶", "🥁", "🪘", "🎹", "🎷", "🎺", "🪗", "🎸", "🪕", "🎻"
        ));
        
        // 旅行与地点
        EMOJI_CATEGORIES.put("旅行与地点", Arrays.asList(
            "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
            "🛻", "🚚", "🚛", "🚜", "🏍️", "🛵", "🚲", "🛴", "🛹", "🛼",
            "🚁", "🛸", "✈️", "🛩️", "🪂", "💺", "🚀", "🛰️", "🚉", "🚊",
            "🚝", "🚞", "🚋", "🚃", "🚋", "🚞", "🚝", "🚄", "🚅", "🚈",
            "🚂", "🚆", "🚇", "🚊", "🚉", "🚁", "🚟", "🚠", "🚡", "🛶",
            "⛵", "🚤", "🛥️", "🛳️", "⛴️", "🚢", "⚓", "🪝", "⛽", "🚧",
            "🚨", "🚥", "🚦", "🛑", "🚏", "🗺️", "🗿", "🗽", "🗼", "🏰",
            "🏯", "🏟️", "🎡", "🎢", "🎠", "⛲", "⛱️", "🏖️", "🏝️", "🏜️"
        ));
        
        // 物品
        EMOJI_CATEGORIES.put("物品", Arrays.asList(
            "⌚", "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "🖲️", "🕹️",
            "🗜️", "💽", "💾", "💿", "📀", "📼", "📷", "📸", "📹", "🎥",
            "📽️", "🎞️", "📞", "☎️", "📟", "📠", "📺", "📻", "🎙️", "🎚️",
            "🎛️", "🧭", "⏱️", "⏲️", "⏰", "🕰️", "⌛", "⏳", "📡", "🔋",
            "🔌", "💡", "🔦", "🕯️", "🪔", "🧯", "🛢️", "💸", "💵", "💴",
            "💶", "💷", "🪙", "💰", "💳", "💎", "⚖️", "🪜", "🧰", "🔧",
            "🔨", "⚒️", "🛠️", "⛏️", "🪓", "🪚", "🔩", "⚙️", "🪤", "🧲",
            "🔫", "💣", "🧨", "🪓", "🔪", "🗡️", "⚔️", "🛡️", "🚬", "⚰️"
        ));
        
        // 符号
        EMOJI_CATEGORIES.put("符号", Arrays.asList(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
            "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "☮️",
            "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐",
            "⛎", "♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐",
            "♑", "♒", "♓", "🆔", "⚛️", "🉑", "☢️", "☣️", "📴", "📳",
            "🈶", "🈚", "🈸", "🈺", "🈷️", "✴️", "🆚", "💮", "🉐", "㊙️",
            "㊗️", "🈴", "🈵", "🈹", "🈲", "🅰️", "🅱️", "🆎", "🆑", "🅾️",
            "🆘", "❌", "⭕", "🛑", "⛔", "📛", "🚫", "💯", "💢", "♨️"
        ));
        
        // 旗帜
        EMOJI_CATEGORIES.put("旗帜", Arrays.asList(
            "🏁", "🚩", "🎌", "🏴", "🏳️", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️", "🇦🇫", "🇦🇽",
            "🇦🇱", "🇩🇿", "🇦🇸", "🇦🇩", "🇦🇴", "🇦🇮", "🇦🇶", "🇦🇬", "🇦🇷", "🇦🇲",
            "🇦🇼", "🇦🇺", "🇦🇹", "🇦🇿", "🇧🇸", "🇧🇭", "🇧🇩", "🇧🇧", "🇧🇾", "🇧🇪",
            "🇧🇿", "🇧🇯", "🇧🇲", "🇧🇹", "🇧🇴", "🇧🇦", "🇧🇼", "🇧🇷", "🇮🇴", "🇻🇬",
            "🇧🇳", "🇧🇬", "🇧🇫", "🇧🇮", "🇰🇭", "🇨🇲", "🇨🇦", "🇮🇨", "🇨🇻", "🇧🇶",
            "🇰🇾", "🇨🇫", "🇹🇩", "🇨🇱", "🇨🇳", "🇨🇽", "🇨🇨", "🇨🇴", "🇰🇲", "🇨🇬",
            "🇨🇩", "🇨🇰", "🇨🇷", "🇨🇮", "🇭🇷", "🇨🇺", "🇨🇼", "🇨🇾", "🇨🇿", "🇩🇰"
        ));
    }
    
    public DiscordEmojiPicker(Consumer<String> onEmojiSelected) {
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
        container.getStyleClass().add("discord-emoji-picker");
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
        searchField.getStyleClass().add("discord-emoji-search");
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
        categoryButtons.getStyleClass().add("discord-emoji-categories");
        categoryButtons.setPrefWidth(50);
        categoryButtons.setSpacing(5);
        categoryButtons.setPadding(new Insets(10, 5, 10, 5));
        
        // 分类图标映射
        Map<String, String> categoryIcons = new LinkedHashMap<>();
        categoryIcons.put("笑脸与情感", "😀");
        categoryIcons.put("人物与身体", "👋");
        categoryIcons.put("动物与自然", "🐶");
        categoryIcons.put("食物与饮料", "🍎");
        categoryIcons.put("活动", "⚽");
        categoryIcons.put("旅行与地点", "🚗");
        categoryIcons.put("物品", "💻");
        categoryIcons.put("符号", "❤️");
        categoryIcons.put("旗帜", "🏁");
        
        // 最近使用按钮
        if (!recentEmojis.isEmpty()) {
            Button recentButton = createCategoryButton("🕒", "最近使用");
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
        button.getStyleClass().add("discord-emoji-category-btn");
        button.setPrefSize(40, 40);
        button.setTooltip(new Tooltip(tooltip));
        return button;
    }
    
    private void createEmojiArea(HBox contentArea) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("discord-emoji-scroll");
        scrollPane.setPrefSize(340, 280);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        currentEmojiPane = new FlowPane();
        currentEmojiPane.getStyleClass().add("discord-emoji-flow");
        currentEmojiPane.setHgap(5);
        currentEmojiPane.setVgap(5);
        currentEmojiPane.setPadding(new Insets(10));
        
        scrollPane.setContent(currentEmojiPane);
        contentArea.getChildren().add(scrollPane);
    }
    
    private void showCategory(String category) {
        currentEmojiPane.getChildren().clear();
        List<String> emojis = emojiCategories.get(category);
        if (emojis != null) {
            for (String emoji : emojis) {
                Button emojiButton = createEmojiButton(emoji);
                currentEmojiPane.getChildren().add(emojiButton);
            }
        }
    }
    
    private void showRecentEmojis() {
        currentEmojiPane.getChildren().clear();
        for (String emoji : recentEmojis) {
            Button emojiButton = createEmojiButton(emoji);
            currentEmojiPane.getChildren().add(emojiButton);
        }
    }
    
    private void searchEmojis(String query) {
        currentEmojiPane.getChildren().clear();
        String lowerQuery = query.toLowerCase();
        
        for (List<String> emojis : emojiCategories.values()) {
            for (String emoji : emojis) {
                // 简单的搜索逻辑，可以根据需要扩展
                if (emoji.contains(lowerQuery)) {
                    Button emojiButton = createEmojiButton(emoji);
                    currentEmojiPane.getChildren().add(emojiButton);
                }
            }
        }
    }
    
    private Button createEmojiButton(String emoji) {
        Button button = new Button(emoji);
        button.getStyleClass().add("discord-emoji-btn");
        button.setPrefSize(35, 35);
        button.setOnAction(e -> {
            if (onEmojiSelected != null) {
                onEmojiSelected.accept(emoji);
                addToRecent(emoji);
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
