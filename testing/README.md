# P2P Chat 测试工具

这个目录包含简洁可靠的测试脚本，用于测试P2P聊天应用的功能。

## 🚀 快速开始

### Windows用户

1. **检查系统**：
   ```cmd
   check.bat
   ```

2. **测试CLI版本**（推荐先试这个）：
   ```cmd
   cli-test.bat
   ```

3. **测试GUI版本**（需要JavaFX）：
   ```cmd
   gui-test.bat
   ```

### Linux/Mac用户

1. **测试CLI版本**：
   ```bash
   ./cli-test.sh
   ```

2. **测试GUI版本**：
   ```bash
   ./gui-test.sh
   ```

## 📋 脚本说明

### `check.bat`
- 检查Java安装
- 检查JAR文件和class文件
- 测试CLI版本
- 检查JavaFX可用性

### `cli-test.bat/.sh`
- 启动CLI版本的P2P聊天节点
- 支持1-3个节点
- 无需JavaFX，兼容性最好
- 每个节点在独立的命令行窗口中运行

### `gui-test.bat/.sh`
- 启动GUI版本的P2P聊天节点
- 支持1-3个节点
- 需要JavaFX支持
- 每个节点在独立的GUI窗口中运行

## 🎮 使用说明

### CLI版本测试
1. 运行 `cli-test.bat`
2. 选择要启动的节点数量
3. 在每个命令行窗口中使用以下命令：
   - `send Hello!` - 发送群聊消息
   - `status` - 查看连接状态
   - `list` - 列出连接的节点
   - `quit` - 退出节点

### GUI版本测试
1. 运行 `gui-test.bat`
2. 选择要启动的节点数量
3. 在每个GUI窗口中：
   - 发送群聊消息
   - 右键用户列表进行私聊
   - 使用文件传输功能
   - 手动连接其他节点

## 🔧 故障排除

### 脚本闪退
- 运行 `check.bat` 检查系统状态
- 确保在 `testing/` 目录中运行脚本
- 确保项目已编译：`mvn clean package`

### JavaFX问题
- 如果GUI版本不工作，使用CLI版本
- 安装包含JavaFX的Java：https://www.azul.com/downloads/?package=jdk-fx

### 找不到文件
- 确保在 `testing/` 目录中运行脚本
- 运行 `mvn clean package` 重新编译项目

## 💡 测试建议

1. **首次测试**：先运行 `check.bat` 检查系统
2. **稳定测试**：使用 `cli-test.bat` 进行基本功能测试
3. **完整测试**：如果JavaFX可用，使用 `gui-test.bat` 测试完整功能
4. **网络测试**：启动多个节点测试P2P通信

## 📁 文件结构

```
testing/
├── check.bat           # 系统检查工具
├── cli-test.bat/.sh    # CLI版本测试
├── gui-test.bat/.sh    # GUI版本测试
├── README.md           # 本文档
├── TESTING_GUIDE.md    # 详细测试指南
└── TROUBLESHOOTING.md  # 故障排除指南
```

## 🎯 测试目标

这些脚本帮助您验证：
- P2P网络连接
- 群聊消息传递
- 私聊功能（GUI版本）
- 文件传输（GUI版本）
- 节点发现和路由
- 安全加密通信
- 网络故障恢复
