# P2P聊天测试指南

## 🚀 快速测试

### 方法1：自动启动3个节点（推荐）
```cmd
# Windows
quick-test.bat

# Linux/Mac
./quick-test.sh
```

### 方法2：交互式启动
```cmd
# Windows
test-multiple-nodes.bat

# Linux/Mac  
./test-multiple-nodes.sh
```

## 📋 手动测试步骤

### 启动多个节点

**命令行版本：**
```cmd
# 节点1（主节点）
java -cp target\classes com.group7.chat.Main 8080

# 节点2（连接到节点1）
java -cp target\classes com.group7.chat.Main 8081 localhost:8080

# 节点3（连接到节点1）
java -cp target\classes com.group7.chat.Main 8082 localhost:8080
```

**GUI版本：**
```cmd
# 节点1
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar 8080

# 节点2
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar 8081

# 节点3
java --module-path . --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar 8082
```

## 🎮 测试功能

### 1. 基本连接测试
- 启动节点1（端口8080）
- 启动节点2（端口8081）
- 在节点2中运行：`connect localhost:8080`
- 检查连接状态：`status`

### 2. 群聊测试
- 在任一节点发送消息：`send Hello everyone!`
- 检查其他节点是否收到消息

### 3. 私聊测试（GUI版本）
- 右键点击在线用户列表中的用户
- 选择"私聊"
- 发送私人消息

### 4. 文件传输测试
- 在GUI中点击"发送文件"按钮
- 选择要发送的文件
- 检查其他节点是否收到文件

### 5. 网络鲁棒性测试
- 启动3个节点
- 关闭中间节点
- 检查其他节点是否仍能通信

## 🔧 命令行界面命令

### 基本命令
- `connect <host:port>` - 连接到指定节点
- `send <message>` - 发送群聊消息
- `status` - 显示节点状态和连接信息
- `list` - 列出已连接的节点
- `quit` - 退出程序

### 高级命令
- `ping <host:port>` - 测试与指定节点的连接
- `info` - 显示详细的节点信息
- `debug` - 切换调试模式

## 📊 测试场景

### 场景1：基本P2P通信
1. 启动2个节点
2. 建立连接
3. 双向发送消息
4. 验证消息传递

### 场景2：多节点网络
1. 启动3-5个节点
2. 形成网状连接
3. 测试消息广播
4. 验证网络发现

### 场景3：节点故障恢复
1. 启动3个节点
2. 断开一个节点
3. 重新连接
4. 验证网络自愈

### 场景4：文件传输
1. 启动2个节点
2. 发送不同大小的文件
3. 验证文件完整性
4. 测试传输速度

## 🐛 故障排除

### 连接失败
- 检查端口是否被占用
- 确认防火墙设置
- 验证IP地址和端口号

### 消息不同步
- 检查网络连接
- 验证节点状态
- 重启相关节点

### GUI无法启动
- 运行 `check-javafx.bat` 检查JavaFX
- 使用命令行版本作为备选

## 📈 性能测试

### 消息吞吐量
- 发送大量消息
- 测量传输延迟
- 监控内存使用

### 网络扩展性
- 逐步增加节点数量
- 测试最大连接数
- 观察性能变化

### 文件传输性能
- 传输不同大小的文件
- 测量传输速度
- 验证并发传输

## 💡 测试技巧

1. **使用不同端口**：避免端口冲突
2. **监控日志**：观察详细的运行信息
3. **网络工具**：使用netstat检查连接状态
4. **分步测试**：逐个验证功能模块
5. **记录结果**：保存测试数据用于分析

## 🎯 预期结果

- ✅ 节点能够成功连接
- ✅ 消息能够实时传递
- ✅ 文件能够完整传输
- ✅ 网络具有故障恢复能力
- ✅ GUI和CLI版本功能一致
