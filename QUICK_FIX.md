# 快速修复指南

## ❌ 常见错误

### 1. 如果看到编码错误或乱码
批处理脚本可能出现编码问题。

### 2. 如果看到 "Module javafx.controls not found"
您的Java环境缺少JavaFX模块。

## 🚀 立即解决方案

### 如果缺少JavaFX模块：

**最简单方法（推荐）：**
1. 下载包含JavaFX的Java：https://www.azul.com/downloads/?package=jdk-fx
2. 安装后重新运行 `start.bat`

**快速测试方法：**
```cmd
# 运行命令行版本（无需JavaFX）
start-cli.bat
```

### 如果有JavaFX但仍有问题：

**方法1：直接命令行运行**

打开命令提示符（cmd），进入项目目录，运行：

```cmd
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar
```

### 方法2：使用简化脚本

双击运行：`start.bat`

### 方法3：运行命令行版本

如果GUI版本仍有问题，运行命令行版本：

```cmd
# 首先编译（如果需要）
mvn clean compile

# 然后运行
java -cp target\classes com.group7.chat.Main
```

或双击：`start-cli.bat`

## 🔧 如果Java命令不工作

1. **检查Java是否安装**：
   ```cmd
   java -version
   ```

2. **如果没有安装Java**：
   - 下载并安装 Java 11 或更高版本
   - 推荐：Azul Zulu JDK (包含JavaFX): https://www.azul.com/downloads/?package=jdk-fx

3. **如果Java已安装但命令不工作**：
   - 检查PATH环境变量是否包含Java路径
   - 重启命令提示符

## 📋 完整步骤

1. 下载项目
2. 解压到某个目录
3. 打开命令提示符
4. 进入项目目录：`cd C:\path\to\P2pChat`
5. 运行：`java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar`

## 💡 如果仍有问题

查看详细解决方案：`JAVAFX_RUNTIME_SOLUTIONS.md`
