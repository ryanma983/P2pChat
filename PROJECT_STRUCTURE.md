# 项目结构说明

## 📁 目录结构

```
P2pChat/
├── src/                                    # 源代码目录
│   └── main/
│       ├── java/com/group7/chat/          # Java源文件
│       │   ├── gui/                       # GUI界面相关
│       │   ├── security/                  # 安全模块
│       │   ├── Node.java                  # P2P节点实现
│       │   ├── Message.java               # 消息处理
│       │   └── ...                        # 其他核心文件
│       └── resources/                      # 资源文件
│           ├── fxml/                      # JavaFX界面文件
│           └── css/                       # 样式文件
├── target/                                 # Maven编译输出
│   ├── classes/                           # 编译后的class文件
│   ├── p2p-chat-1.0-SNAPSHOT.jar         # 可执行JAR（包含依赖）
│   └── decentralized-chat-1.0-SNAPSHOT.jar # 项目JAR（仅代码）
├── scripts/                               # 启动脚本集合
│   ├── start-cli.bat/sh                  # 命令行版本启动脚本
│   ├── start-gui.bat/sh                  # GUI版本启动脚本
│   ├── start-simple.bat/sh              # 简化启动脚本
│   └── run-with-javafx.sh                # JavaFX专用脚本
├── documentation/                         # 项目文档
│   ├── INSTALL_JAVAFX.md                 # JavaFX安装指南
│   ├── QUICK_FIX.md                      # 快速问题解决
│   ├── SECURITY_ARCHITECTURE.md          # 安全架构文档
│   ├── SECURITY_VULNERABILITIES_ANALYSIS.md # 漏洞分析
│   ├── PROJECT_COMPLETION_REPORT.md      # 项目完成报告
│   ├── SECURE_COMMUNICATION_PROTOCOL.md  # 安全通信协议
│   ├── DISTRIBUTED_OVERLAY_PROTOCOL.md   # 分布式覆盖网络协议
│   ├── RUNNING_METHODS_COMPARISON.md     # 运行方式对比
│   ├── JAR_FILES_EXPLANATION.md          # JAR文件说明
│   └── ...                               # 其他技术文档
├── keys/                                  # 密钥存储目录（运行时生成）
├── pom.xml                               # Maven配置文件
├── start.bat                             # Windows主启动脚本
├── start.sh                              # Linux/Mac主启动脚本
├── README.md                             # 项目说明（本文件）
└── PROJECT_STRUCTURE.md                  # 项目结构说明
```

## 🎯 文件用途说明

### 启动脚本
- **`start.bat/sh`** - 主启动脚本，自动尝试多种运行方式
- **`scripts/start-cli.*`** - 命令行版本，无需JavaFX
- **`scripts/start-gui.*`** - GUI版本，需要JavaFX支持
- **`scripts/start-simple.*`** - 简化版本，自动回退

### 核心文档
- **`README.md`** - 项目概览和快速开始指南
- **`documentation/INSTALL_JAVAFX.md`** - 解决JavaFX相关问题
- **`documentation/QUICK_FIX.md`** - 常见问题快速解决

### 技术文档
- **安全相关：** `SECURITY_*.md` - 安全架构、协议、漏洞分析
- **网络协议：** `DISTRIBUTED_OVERLAY_PROTOCOL.md` - P2P网络实现
- **运行指南：** `RUNNING_METHODS_COMPARISON.md` - 不同运行方式对比

### 编译产物
- **`target/p2p-chat-1.0-SNAPSHOT.jar`** - 包含所有依赖的可执行JAR（8.4MB）
- **`target/decentralized-chat-1.0-SNAPSHOT.jar`** - 仅项目代码的JAR（113KB）

## 🚀 推荐使用方式

### 普通用户
1. 双击 `start.bat` (Windows) 或运行 `./start.sh` (Linux/Mac)
2. 如有问题，查看 `documentation/QUICK_FIX.md`

### 开发者
1. 查看 `documentation/` 目录下的技术文档
2. 使用 `scripts/` 目录下的专用启动脚本
3. 参考 `documentation/RUNNING_METHODS_COMPARISON.md` 了解不同运行方式

### 安全研究者
1. 阅读 `documentation/SECURITY_VULNERABILITIES_ANALYSIS.md`
2. 查看 `documentation/SECURE_COMMUNICATION_PROTOCOL.md`
3. 分析 `src/main/java/com/group7/chat/security/` 目录下的代码

## 🧹 清理说明

项目已进行结构优化：
- ✅ 移除了过时的表情符号相关文档
- ✅ 整合了分散的启动脚本到 `scripts/` 目录
- ✅ 统一了技术文档到 `documentation/` 目录
- ✅ 简化了主目录结构
- ✅ 保留了核心功能和重要文档
