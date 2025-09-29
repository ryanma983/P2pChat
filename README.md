# 安全 P2P 聊天应用

一个功能完整的、采用Java和JavaFX开发的**安全P2P聊天应用**。支持群聊、私聊、文件传输，具有强大的安全架构和真正的分布式网络设计。

## 🚀 快速开始

### 方法1：一键启动（推荐）

**Windows用户：**
```cmd
# 智能启动（自动选择GUI或CLI）
start.bat

# 仅启动GUI版本
start-gui-only.bat
```

**Linux/Mac用户：**
```bash
# 智能启动（自动选择GUI或CLI）
./start.sh

# 仅启动GUI版本
scripts/start-gui.sh
```

> **注意：** 如果看到命令行界面而不是GUI，说明您的Java环境缺少JavaFX支持。请查看下面的JavaFX问题解决方案。

### 方法2：手动运行

```bash
# 编译项目
mvn clean package

# 运行GUI版本
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target/p2p-chat-1.0-SNAPSHOT.jar

# 或运行命令行版本
java -cp target/classes com.group7.chat.Main
```

## ❌ 常见问题解决

### 问题1：启动后看到命令行界面而不是GUI

**现象：** 运行 `start.bat` 后看到黑色命令行窗口，而不是图形界面

**原因：** 您的Java环境缺少JavaFX支持，脚本自动回退到命令行版本

**解决方案：** 参考下面的"问题2：缺少 JavaFX 运行时组件"

### 问题2：缺少 JavaFX 运行时组件

**错误信息：** `错误: 缺少 JavaFX 运行时组件` 或 `Module javafx.controls not found`

**解决方案：**

**首先检查JavaFX是否已安装：**
```cmd
# Windows
check-javafx.bat

# Linux/Mac  
./check-javafx.sh
```

1. **最简单方法（推荐）：**
   - 下载包含JavaFX的Java：https://www.azul.com/downloads/?package=jdk-fx
   - 选择 "Azul Zulu JDK FX" for your OS
   - 安装后重新运行 `start.bat`

2. **快速测试方法：**
   ```cmd
   # Windows
   scripts\start-cli.bat
   
   # Linux/Mac
   scripts/start-cli.sh
   ```

3. **手动安装JavaFX：**
   - 下载JavaFX SDK：https://openjfx.io/
   - 解压到某个目录（如：`C:\javafx-sdk-17.0.2`）
   - 运行：
   ```cmd
   java --module-path "C:\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar
   ```

### 问题2：找不到JAR文件

**错误信息：** `Unable to access jarfile`

**解决方案：**
```bash
# 首先编译项目
mvn clean package

# 然后运行
start.bat  # Windows
./start.sh # Linux/Mac
```

### 问题3：编译失败

**解决方案：**
```bash
# 确保Java版本正确
java -version  # 需要Java 11+

# 清理并重新编译
mvn clean compile package
```

## 📁 项目结构

```
P2pChat/
├── src/                          # 源代码
├── target/                       # 编译输出
├── start.bat / start.sh         # 主启动脚本
├── scripts/                     # 其他启动脚本
│   ├── start-cli.bat/sh        # 命令行版本
│   ├── start-gui.bat/sh        # GUI版本
│   └── start-simple.bat/sh     # 简化版本
├── documentation/               # 详细文档
│   ├── INSTALL_JAVAFX.md       # JavaFX安装指南
│   ├── SECURITY_*.md           # 安全相关文档
│   └── PROJECT_*.md            # 项目文档
└── README.md                   # 本文件
```

## 🎮 使用说明

### GUI模式
启动后您将看到：
- 现代化的聊天界面
- 在线成员列表
- 群聊和私聊功能
- 文件传输功能
- 安全加密状态显示

### 命令行模式
可用命令：
- `connect <host:port>` - 连接到指定节点
- `send <message>` - 发送消息
- `status` - 显示状态
- `quit` - 退出

## 🔒 安全特性

- **端到端加密 (E2EE)：** RSA-2048 + AES-256-GCM
- **分布式网络：** 基于Kademlia的覆盖网络协议
- **安全文件传输：** 加密通道中的文件传输
- **前向保密：** 动态密钥交换
- **完整性校验：** 防篡改和重放攻击

## 🛠️ 开发者信息

### 编译要求
- Java 11 或更高版本
- Maven 3.6+
- JavaFX（GUI模式）

### 运行测试
```bash
mvn test
```

### 创建发布版本
```bash
mvn clean package
```

## 📚 详细文档

- **安装问题：** `documentation/INSTALL_JAVAFX.md`
- **运行指南：** `documentation/RUN_GUIDE.md`
- **安全架构：** `documentation/SECURITY_ARCHITECTURE.md`
- **项目完成报告：** `documentation/PROJECT_COMPLETION_REPORT.md`

## 🔍 安全评审

本项目包含**故意植入的安全漏洞**供学术研究：
- 详细分析：`documentation/SECURITY_VULNERABILITIES_ANALYSIS.md`
- 安全协议：`documentation/SECURE_COMMUNICATION_PROTOCOL.md`

## 📞 支持

如果遇到问题：
1. 查看 `documentation/QUICK_FIX.md`
2. 尝试不同的启动脚本
3. 检查Java和JavaFX安装

---

**快速开始：** 双击 `start.bat` (Windows) 或运行 `./start.sh` (Linux/Mac)
