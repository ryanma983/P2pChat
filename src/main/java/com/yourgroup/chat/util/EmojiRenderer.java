package com.yourgroup.chat.util;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表情渲染器 - 将表情代码转换为图片显示
 */
public class EmojiRenderer {
    
    // 表情代码到图片路径的映射
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    
    static {
        // 常用表情
        EMOJI_MAP.put(":happy:", "/images/emojis/happy.png");
        EMOJI_MAP.put(":sad:", "/images/emojis/sad.png");
        EMOJI_MAP.put(":love:", "/images/emojis/love.png");
        EMOJI_MAP.put(":laugh:", "/images/emojis/laugh.png");
        EMOJI_MAP.put(":wink:", "/images/emojis/wink.png");
        EMOJI_MAP.put(":angry:", "/images/emojis/angry.png");
        EMOJI_MAP.put(":surprised:", "/images/emojis/surprised.png");
        EMOJI_MAP.put(":cool:", "/images/emojis/cool.png");
        EMOJI_MAP.put(":thinking:", "/images/emojis/thinking.png");
        EMOJI_MAP.put(":kiss:", "/images/emojis/kiss.png");
        
        // 表情符号
        EMOJI_MAP.put(":smile:", "/images/emojis/happy.png");
        EMOJI_MAP.put(":cry:", "/images/emojis/sad.png");
        EMOJI_MAP.put(":heart_eyes:", "/images/emojis/love.png");
        EMOJI_MAP.put(":joy:", "/images/emojis/laugh.png");
        EMOJI_MAP.put(":winking:", "/images/emojis/wink.png");
        EMOJI_MAP.put(":rage:", "/images/emojis/angry.png");
        EMOJI_MAP.put(":shocked:", "/images/emojis/surprised.png");
        EMOJI_MAP.put(":sunglasses:", "/images/emojis/cool.png");
        EMOJI_MAP.put(":hmm:", "/images/emojis/thinking.png");
        EMOJI_MAP.put(":kiss_heart:", "/images/emojis/kiss.png");
        
        // 动作表情
        EMOJI_MAP.put(":thumbs_up:", "/images/emojis/happy.png");
        EMOJI_MAP.put(":thumbs_down:", "/images/emojis/sad.png");
        EMOJI_MAP.put(":clap:", "/images/emojis/laugh.png");
        EMOJI_MAP.put(":wave:", "/images/emojis/wink.png");
        EMOJI_MAP.put(":peace:", "/images/emojis/cool.png");
        EMOJI_MAP.put(":fist:", "/images/emojis/angry.png");
        EMOJI_MAP.put(":ok:", "/images/emojis/surprised.png");
        EMOJI_MAP.put(":point:", "/images/emojis/thinking.png");
        EMOJI_MAP.put(":hug:", "/images/emojis/love.png");
        EMOJI_MAP.put(":high_five:", "/images/emojis/kiss.png");
        
        // 文字表情 - 映射到对应图片
        EMOJI_MAP.put(":)", "/images/emojis/happy.png");
        EMOJI_MAP.put(":(", "/images/emojis/sad.png");
        EMOJI_MAP.put(":D", "/images/emojis/laugh.png");
        EMOJI_MAP.put(";)", "/images/emojis/wink.png");
        EMOJI_MAP.put(":P", "/images/emojis/wink.png");
        EMOJI_MAP.put(">:(", "/images/emojis/angry.png");
        EMOJI_MAP.put(":o", "/images/emojis/surprised.png");
        EMOJI_MAP.put("8)", "/images/emojis/cool.png");
        EMOJI_MAP.put(":/", "/images/emojis/thinking.png");
        EMOJI_MAP.put(":*", "/images/emojis/kiss.png");
        EMOJI_MAP.put("<3", "/images/emojis/love.png");
        EMOJI_MAP.put("^_^", "/images/emojis/happy.png");
        EMOJI_MAP.put("-_-", "/images/emojis/sad.png");
        EMOJI_MAP.put("XD", "/images/emojis/laugh.png");
        EMOJI_MAP.put("o_O", "/images/emojis/surprised.png");
    }
    
    // 表情代码的正则表达式模式
    private static final Pattern EMOJI_PATTERN = Pattern.compile("(:[a-zA-Z_]+:|:\\)|:\\(|:D|;\\)|:P|>:\\(|:o|8\\)|:/|:\\*|<3|\\^_\\^|-_-|XD|o_O)");
    
    /**
     * 将包含表情代码的文本转换为TextFlow，其中表情代码被替换为图片
     * 
     * @param text 包含表情代码的文本
     * @return 包含文本和表情图片的TextFlow
     */
    public static TextFlow renderEmojis(String text) {
        TextFlow textFlow = new TextFlow();
        
        if (text == null || text.isEmpty()) {
            return textFlow;
        }
        
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        int lastEnd = 0;
        
        while (matcher.find()) {
            // 添加表情前的文本
            if (matcher.start() > lastEnd) {
                String beforeText = text.substring(lastEnd, matcher.start());
                if (!beforeText.isEmpty()) {
                    Text textNode = new Text(beforeText);
                    textNode.getStyleClass().add("chat-text");
                    textFlow.getChildren().add(textNode);
                }
            }
            
            // 添加表情图片
            String emojiCode = matcher.group();
            ImageView emojiImage = createEmojiImage(emojiCode);
            if (emojiImage != null) {
                textFlow.getChildren().add(emojiImage);
            } else {
                // 如果图片加载失败，显示原始代码
                Text fallbackText = new Text(emojiCode);
                fallbackText.getStyleClass().add("emoji-fallback");
                textFlow.getChildren().add(fallbackText);
            }
            
            lastEnd = matcher.end();
        }
        
        // 添加剩余的文本
        if (lastEnd < text.length()) {
            String remainingText = text.substring(lastEnd);
            if (!remainingText.isEmpty()) {
                Text textNode = new Text(remainingText);
                textNode.getStyleClass().add("chat-text");
                textFlow.getChildren().add(textNode);
            }
        }
        
        // 如果没有找到表情，直接添加整个文本
        if (textFlow.getChildren().isEmpty()) {
            Text textNode = new Text(text);
            textNode.getStyleClass().add("chat-text");
            textFlow.getChildren().add(textNode);
        }
        
        return textFlow;
    }
    
    /**
     * 创建表情图片
     * 
     * @param emojiCode 表情代码
     * @return 表情ImageView，如果加载失败返回null
     */
    private static ImageView createEmojiImage(String emojiCode) {
        String imagePath = EMOJI_MAP.get(emojiCode);
        if (imagePath == null) {
            return null;
        }
        
        try {
            Image image = new Image(EmojiRenderer.class.getResourceAsStream(imagePath));
            if (image.isError()) {
                return null;
            }
            
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(20);  // 聊天中的表情大小
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.getStyleClass().add("chat-emoji");
            
            return imageView;
        } catch (Exception e) {
            System.err.println("加载表情图片失败: " + imagePath + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查文本是否包含表情代码
     * 
     * @param text 要检查的文本
     * @return 如果包含表情代码返回true
     */
    public static boolean containsEmojis(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return EMOJI_PATTERN.matcher(text).find();
    }
    
    /**
     * 获取文本中的所有表情代码
     * 
     * @param text 要分析的文本
     * @return 表情代码列表
     */
    public static java.util.List<String> extractEmojis(String text) {
        java.util.List<String> emojis = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            return emojis;
        }
        
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        while (matcher.find()) {
            emojis.add(matcher.group());
        }
        
        return emojis;
    }
    
    /**
     * 将表情代码转换为纯文本（移除表情）
     * 
     * @param text 包含表情代码的文本
     * @return 移除表情后的纯文本
     */
    public static String stripEmojis(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return EMOJI_PATTERN.matcher(text).replaceAll("");
    }
    
    /**
     * 获取支持的表情代码列表
     * 
     * @return 所有支持的表情代码
     */
    public static java.util.Set<String> getSupportedEmojis() {
        return EMOJI_MAP.keySet();
    }
    
    /**
     * 检查表情代码是否被支持
     * 
     * @param emojiCode 表情代码
     * @return 如果支持返回true
     */
    public static boolean isEmojiSupported(String emojiCode) {
        return EMOJI_MAP.containsKey(emojiCode);
    }
}
