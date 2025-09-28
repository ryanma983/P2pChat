#!/bin/bash

echo "========================================"
echo "P2P Chat 表情字体安装脚本"
echo "========================================"
echo

# 检测操作系统
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="Linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macOS"
else
    OS="Unknown"
fi

echo "检测到操作系统: $OS"
echo

# 检查Java版本
echo "正在检查Java版本..."
java -version
echo

echo "正在检查表情字体支持..."

# 创建表情测试程序
cat > EmojiTest.java << 'EOF'
import javax.swing.*;
import java.awt.*;

public class EmojiTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("表情测试");
            JLabel label = new JLabel("<html><div style='font-size:24px'>😀😃😄😁😆😅🤣😂🙂🙃😉😊😇🥰😍🤩😘</div></html>");
            
            // 尝试设置表情字体
            Font emojiFont = null;
            String[] fontNames = {"Apple Color Emoji", "Noto Color Emoji", "Segoe UI Emoji", "EmojiOne Color"};
            
            for (String fontName : fontNames) {
                Font testFont = new Font(fontName, Font.PLAIN, 24);
                if (!testFont.getFamily().equals(Font.DIALOG)) {
                    emojiFont = testFont;
                    System.out.println("找到表情字体: " + fontName);
                    break;
                }
            }
            
            if (emojiFont != null) {
                label.setFont(emojiFont);
            } else {
                System.out.println("未找到表情字体，使用默认字体");
            }
            
            frame.add(label);
            frame.setSize(600, 100);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            System.out.println("如果看到彩色表情，说明系统支持Unicode表情");
            System.out.println("如果看到方框或问号，说明需要安装表情字体");
        });
    }
}
EOF

# 编译和运行测试
javac EmojiTest.java
if [ $? -eq 0 ]; then
    echo "编译成功！正在运行表情测试..."
    java EmojiTest &
else
    echo "编译失败，请检查Java环境"
fi

echo
echo "========================================"
echo "表情字体安装指南："
echo "========================================"
echo

if [ "$OS" = "Linux" ]; then
    echo "Linux用户："
    echo "1. Ubuntu/Debian系统："
    echo "   sudo apt update"
    echo "   sudo apt install fonts-noto-color-emoji"
    echo
    echo "2. CentOS/RHEL系统："
    echo "   sudo yum install google-noto-emoji-color-fonts"
    echo
    echo "3. Arch Linux系统："
    echo "   sudo pacman -S noto-fonts-emoji"
    echo
    echo "4. 手动安装："
    echo "   - 下载 Noto Color Emoji 字体"
    echo "   - 复制到 ~/.fonts/ 目录"
    echo "   - 运行 fc-cache -f -v"
    
elif [ "$OS" = "macOS" ]; then
    echo "macOS用户："
    echo "1. macOS 10.7+已内置Apple Color Emoji字体"
    echo "2. 如果仍有问题，请更新macOS"
    echo "3. 或者安装Noto Color Emoji："
    echo "   brew install --cask font-noto-color-emoji"
    
else
    echo "未知操作系统，请手动安装表情字体"
fi

echo
echo "========================================"
echo "JavaFX表情支持优化："
echo "========================================"
echo
echo "1. 确保使用Java 11或更高版本"
echo "2. 在JavaFX应用中设置字体："
echo "   -fx-font-family: \"Apple Color Emoji\", \"Noto Color Emoji\", \"Segoe UI Emoji\";"
echo
echo "3. 如果仍有问题，使用我们的兼容性表情选择器"
echo "   它会自动回退到文字表情"
echo

# 清理临时文件
sleep 2
rm -f EmojiTest.java EmojiTest.class

echo "脚本执行完成！"
