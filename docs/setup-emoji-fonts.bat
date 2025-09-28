@echo off
echo ========================================
echo P2P Chat 表情字体安装脚本
echo ========================================
echo.

echo 正在检查系统表情字体支持...
echo.

REM 检查Windows版本
ver | findstr /i "10\." >nul
if %errorlevel%==0 (
    echo 检测到 Windows 10 - 表情字体应该已内置
    goto :check_java
)

ver | findstr /i "11\." >nul
if %errorlevel%==0 (
    echo 检测到 Windows 11 - 表情字体应该已内置
    goto :check_java
)

echo 检测到较旧的Windows版本，可能需要安装表情字体
echo.

:check_java
echo 正在检查Java版本和表情支持...
java -version 2>&1 | findstr /i "version"
echo.

echo 正在创建表情测试程序...
echo import javax.swing.*; > EmojiTest.java
echo import java.awt.*; >> EmojiTest.java
echo public class EmojiTest { >> EmojiTest.java
echo     public static void main(String[] args) { >> EmojiTest.java
echo         JFrame frame = new JFrame("表情测试"); >> EmojiTest.java
echo         JLabel label = new JLabel("😀😃😄😁😆😅🤣😂🙂🙃😉😊😇🥰😍🤩😘"); >> EmojiTest.java
echo         label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24)); >> EmojiTest.java
echo         frame.add(label); >> EmojiTest.java
echo         frame.setSize(600, 100); >> EmojiTest.java
echo         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); >> EmojiTest.java
echo         frame.setVisible(true); >> EmojiTest.java
echo     } >> EmojiTest.java
echo } >> EmojiTest.java

javac EmojiTest.java
if %errorlevel%==0 (
    echo 编译成功！正在运行表情测试...
    echo 如果看到彩色表情，说明系统支持Unicode表情
    echo 如果看到方框或问号，说明需要安装表情字体
    echo.
    start java EmojiTest
) else (
    echo 编译失败，请检查Java环境
)

echo.
echo ========================================
echo 表情字体解决方案：
echo ========================================
echo.
echo 1. Windows 10/11用户：
echo    - 系统应该已内置Segoe UI Emoji字体
echo    - 如果仍有问题，请更新Windows
echo.
echo 2. 较旧Windows版本：
echo    - 下载并安装 Segoe UI Emoji 字体
echo    - 或安装 Noto Color Emoji 字体
echo.
echo 3. JavaFX表情支持：
echo    - 确保使用Java 11或更高版本
echo    - 在CSS中指定表情字体
echo.
echo 4. 如果仍有问题：
echo    - 使用我们的兼容性表情选择器
echo    - 它会自动回退到文字表情
echo.
echo ========================================

pause
del EmojiTest.java EmojiTest.class 2>nul
