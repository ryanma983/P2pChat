# P2P 去中心化聊天应用

一个使用 Java 和 JavaFX 开发的现代化去中心化聊天应用，支持群聊、私聊和文件传输功能。

## ✨ 功能特性

### 🌐 核心功能
- **群聊** - 所有节点间的群组聊天
- **私聊** - 双击成员打开独立私聊窗口
- **文件传输** - 点对点文件发送和接收
- **在线成员列表** - 实时显示网络中的所有节点

### 🎨 用户界面
- **现代化设计** - 类似 Telegram/Discord 的专业界面
- **多窗口支持** - 可同时进行多个私聊对话
- **响应式布局** - 适应不同窗口大小
- **简洁界面** - 专注于核心聊天功能

### 🔧 技术特性
- **去中心化网络** - P2P 节点通信，无需中央服务器
- **消息路由** - 洪泛算法确保消息到达所有节点
- **多线程处理** - 并发处理多个连接
- **实时状态更新** - 在线/离线状态，连接数显示

## 🚀 快速开始

### 环境要求
- Java 11 或更高版本
- Maven 3.6 或更高版本
- 支持 JavaFX 的系统环境

### 安装和运行

1. **克隆项目**
   ```bash
   git clone https://github.com/WebProjectTest114514/P2pChat.git
   cd P2pChat
   ```

2. **编译项目**
   ```bash
   mvn clean compile
   ```

3. **启动应用**
   ```bash
   # 启动第一个节点（默认端口 8080）
   mvn javafx:run
   
   # 启动第二个节点（端口 8081，连接到第一个节点）
   mvn javafx:run -Djavafx.args="8081 localhost:8080"
   
   # 启动更多节点
   mvn javafx:run -Djavafx.args="8082 localhost:8081"
   ```

## 📖 使用指南

### 基本操作

1. **发送群聊消息**
   - 在主窗口输入框中输入消息
   - 点击"发送"按钮或按 Enter 键

2. **开始私聊**
   - 在左侧成员列表中双击任意在线成员
   - 在弹出的私聊窗口中发送消息

3. **发送文件**
   - 点击 📁 文件按钮
   - 选择要发送的文件
   - 接收方会收到确认对话框

### 高级功能

- **多窗口私聊** - 可以同时与多个用户进行私聊
- **网络自组织** - 节点会自动发现和连接到网络中的其他节点
- **消息路由** - 消息会自动传播到网络中的所有节点

## 🏗️ 项目结构

```
P2pChat/
├── src/main/java/com/yourgroup/chat/
│   ├── Main.java                    # 命令行入口
│   ├── Node.java                    # 网络节点核心类
│   ├── Message.java                 # 消息数据模型
│   ├── MessageRouter.java           # 消息路由和洪泛算法
│   ├── PeerConnection.java          # 对等连接管理
│   ├── MessageListener.java         # 消息监听器接口
│   └── gui/
│       ├── ChatApplication.java     # JavaFX 应用主类
│       ├── EnhancedChatController.java # 主界面控制器
│       ├── PrivateChatWindow.java   # 私聊窗口
│       ├── ChatMessage.java         # 聊天消息模型
│       ├── OnlineMember.java        # 在线成员模型
│       └── MessageListCell.java     # 消息列表单元格
├── src/main/resources/
│   ├── fxml/                        # FXML 界面文件
│   └── css/                         # CSS 样式文件
├── docs/                            # 📚 技术文档和开发报告
│   ├── README.md                    # 文档中心索引
│   ├── FIXES_REPORT.md              # 主要修复报告
│   ├── FILE_TRANSFER_FIXES.md       # 文件传输修复详情
│   └── ...                         # 其他技术文档和历史记录
├── pom.xml                          # Maven 配置
├── README.md                        # 项目说明
└── TEST_GUIDE.md                    # 测试指南
```

## 🔧 技术架构

### 网络层
- **P2P 协议** - 基于 TCP Socket 的点对点通信
- **节点发现** - 通过引导节点和邻居列表共享
- **消息路由** - 洪泛算法 + TTL 防止循环
- **连接管理** - 心跳检测和自动重连

### 应用层
- **JavaFX** - 现代化用户界面框架
- **FXML + CSS** - 界面布局和样式分离
- **多线程** - 网络I/O和UI更新分离
- **事件驱动** - 基于监听器模式的消息处理

### 消息协议
```
HELLO:nodeId              # 节点握手
CHAT:content              # 群聊消息
PRIVATE_CHAT:targetId:content  # 私聊消息
FILE_REQUEST:targetId:fileName|size  # 文件传输请求
PEER_LIST:node1,node2,... # 邻居列表共享
PING / PONG               # 心跳检测
```

## 🧪 测试

详细的测试指南请参考 [TEST_GUIDE.md](TEST_GUIDE.md)

### 快速测试
```bash
# 终端1：启动第一个节点
mvn javafx:run

# 终端2：启动第二个节点并连接
mvn javafx:run -Djavafx.args="8081 localhost:8080"

# 在任一窗口发送消息，观察消息传播
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 开发环境设置
1. Fork 项目
2. 创建功能分支：`git checkout -b feature/new-feature`
3. 提交更改：`git commit -am 'Add new feature'`
4. 推送分支：`git push origin feature/new-feature`
5. 创建 Pull Request

## 📄 许可证

本项目仅用于教育目的。

## 🙏 致谢

- JavaFX 社区提供的优秀文档和示例
- Unicode 联盟提供的表情符号标准
- 所有为开源软件做出贡献的开发者们

## 📞 联系方式

如有问题或建议，请通过 GitHub Issues 联系。

---

**注意：** 这是一个教育项目，请在安全的网络环境中使用。
