package com.yourgroup.chat.util;

import javafx.scene.text.Font;
import java.util.Arrays;
import java.util.List;

/**
 * 表情字体支持工具类
 */
public class EmojiSupport {
    
    // 按优先级排序的表情字体列表
    private static final List<String> EMOJI_FONTS = Arrays.asList(
        "Apple Color Emoji",      // macOS
        "Segoe UI Emoji",         // Windows 10/11
        "Noto Color Emoji",       // Linux
        "EmojiOne Color",         // 第三方
        "Twemoji Mozilla",        // Firefox
        "Segoe UI Symbol",        // Windows 8/8.1
        "Symbola",                // 通用Unicode字体
        "DejaVu Sans"             // 备用字体
    );
    
    private static String detectedEmojiFont = null;
    private static boolean fontDetected = false;
    
    /**
     * 检测系统中可用的表情字体
     */
    public static String detectEmojiFont() {
        if (fontDetected) {
            return detectedEmojiFont;
        }
        
        System.out.println("[表情字体] 开始检测系统表情字体支持...");
        
        // 获取系统所有字体
        List<String> systemFonts = Font.getFamilies();
        
        // 按优先级查找表情字体
        for (String emojiFont : EMOJI_FONTS) {
            if (systemFonts.contains(emojiFont)) {
                detectedEmojiFont = emojiFont;
                System.out.println("[表情字体] 找到表情字体: " + emojiFont);
                break;
            }
        }
        
        if (detectedEmojiFont == null) {
            System.out.println("[表情字体] 未找到专用表情字体，使用默认字体");
            detectedEmojiFont = Font.getDefault().getFamily();
        }
        
        fontDetected = true;
        return detectedEmojiFont;
    }
    
    /**
     * 获取表情字体CSS样式
     */
    public static String getEmojiFontCSS() {
        String emojiFont = detectEmojiFont();
        return String.format(
            "-fx-font-family: \"%s\", \"Apple Color Emoji\", \"Segoe UI Emoji\", " +
            "\"Noto Color Emoji\", \"EmojiOne Color\", \"Segoe UI Symbol\", " +
            "\"Symbola\", \"DejaVu Sans\", sans-serif;",
            emojiFont
        );
    }
    
    /**
     * 测试表情字符是否能正确显示
     */
    public static boolean testEmojiSupport(String emoji) {
        try {
            // 简单的字符长度测试
            // Unicode表情通常占用2-4个字符位置
            return emoji.length() >= 1 && emoji.length() <= 8;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取系统表情支持信息
     */
    public static String getEmojiSupportInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== 表情字体支持信息 ===\n");
        
        String os = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        
        info.append("操作系统: ").append(os).append("\n");
        info.append("Java版本: ").append(javaVersion).append("\n");
        
        String emojiFont = detectEmojiFont();
        info.append("检测到的表情字体: ").append(emojiFont).append("\n");
        
        info.append("\n可用字体列表:\n");
        List<String> systemFonts = Font.getFamilies();
        for (String font : EMOJI_FONTS) {
            boolean available = systemFonts.contains(font);
            info.append("  ").append(font).append(": ")
                .append(available ? "✓ 可用" : "✗ 不可用").append("\n");
        }
        
        info.append("\n表情测试:\n");
        String[] testEmojis = {"😀", "😃", "❤️", "👍", "🎉"};
        for (String emoji : testEmojis) {
            boolean supported = testEmojiSupport(emoji);
            info.append("  ").append(emoji).append(": ")
                .append(supported ? "✓ 支持" : "✗ 不支持").append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * 打印表情支持信息到控制台
     */
    public static void printEmojiSupportInfo() {
        System.out.println(getEmojiSupportInfo());
    }
    
    /**
     * 获取推荐的字体安装指南
     */
    public static String getFontInstallationGuide() {
        String os = System.getProperty("os.name").toLowerCase();
        StringBuilder guide = new StringBuilder();
        
        guide.append("=== 表情字体安装指南 ===\n\n");
        
        if (os.contains("windows")) {
            guide.append("Windows系统:\n");
            guide.append("1. Windows 10/11: 系统已内置Segoe UI Emoji字体\n");
            guide.append("2. 较旧版本: 请更新到Windows 10或手动安装表情字体\n");
            guide.append("3. 运行 setup-emoji-fonts.bat 进行自动检测\n");
            
        } else if (os.contains("mac")) {
            guide.append("macOS系统:\n");
            guide.append("1. macOS 10.7+: 系统已内置Apple Color Emoji字体\n");
            guide.append("2. 如有问题请更新macOS到最新版本\n");
            guide.append("3. 或安装Noto Color Emoji: brew install --cask font-noto-color-emoji\n");
            
        } else if (os.contains("linux")) {
            guide.append("Linux系统:\n");
            guide.append("1. Ubuntu/Debian: sudo apt install fonts-noto-color-emoji\n");
            guide.append("2. CentOS/RHEL: sudo yum install google-noto-emoji-color-fonts\n");
            guide.append("3. Arch Linux: sudo pacman -S noto-fonts-emoji\n");
            guide.append("4. 安装后运行: fc-cache -f -v\n");
            
        } else {
            guide.append("未知系统: 请手动下载并安装Noto Color Emoji字体\n");
        }
        
        guide.append("\nJavaFX优化:\n");
        guide.append("1. 使用Java 11或更高版本\n");
        guide.append("2. 在CSS中指定表情字体\n");
        guide.append("3. 如仍有问题，使用兼容性表情选择器\n");
        
        return guide.toString();
    }
}
