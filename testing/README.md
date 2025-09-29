# P2P Chat 测试工具集

这个文件夹包含了所有用于测试P2P聊天应用的工具和脚本。

## 🚀 快速开始

### Windows用户
```cmd
# 启动测试套件主菜单
test-launcher.bat

# 或直接启动多GUI测试
multi-gui-test.bat
```

### Linux/Mac用户
```bash
# 启动多GUI测试
./multi-gui-test.sh

# 或启动多CLI测试
./multi-cli-test.sh
```

## 📁 文件说明

### 主要测试脚本
- **`test-launcher.bat`** - 测试套件主菜单（Windows）
- **`multi-gui-test.bat/sh`** - 多GUI实例测试（推荐）
- **`multi-cli-test.bat/sh`** - 多CLI实例测试（无需JavaFX）

### 其他测试工具
- **`test-multiple-nodes.bat/sh`** - 交互式节点启动器
- **`quick-test.bat/sh`** - 快速3节点测试

### 文档
- **`TESTING_GUIDE.md`** - 详细测试指南
- **`README.md`** - 本文件

## 🎯 推荐测试流程

### 1. 首次测试
```cmd
# Windows
test-launcher.bat
# 选择 "1. Multi-GUI Test"

# Linux/Mac
./multi-gui-test.sh
```

### 2. 功能验证
1. **启动多个GUI实例**
2. **测试群聊功能** - 在一个窗口发送消息，其他窗口应该收到
3. **测试私聊功能** - 右键用户列表中的用户进行私聊
4. **测试文件传输** - 使用文件传输按钮发送文件
5. **测试网络发现** - 观察节点是否自动发现彼此

### 3. 高级测试
- 使用 `TESTING_GUIDE.md` 中的详细测试场景
- 测试网络故障恢复
- 性能和扩展性测试

## 🔧 测试配置

### GUI测试选项
- **2个节点** - 基本P2P通信测试
- **3个节点** - 网络路由测试
- **4个节点** - 扩展性测试
- **自定义** - 最多10个节点

### CLI测试选项
- 相同的节点数量选项
- 无需JavaFX支持
- 适合服务器环境测试

## 💡 测试技巧

1. **GUI测试**：
   - 等待所有窗口完全加载
   - 观察用户列表的变化
   - 尝试不同的连接组合

2. **CLI测试**：
   - 使用 `status` 命令查看连接状态
   - 使用 `send <message>` 发送群聊消息
   - 使用 `connect <host:port>` 手动连接节点

3. **网络测试**：
   - 关闭一个节点，观察其他节点的反应
   - 重新启动节点，测试重连功能
   - 测试不同的网络拓扑

## 🐛 故障排除

### JavaFX问题
```cmd
# 检查JavaFX安装
..\check-javafx.bat

# 如果GUI失败，使用CLI版本
multi-cli-test.bat
```

### 端口冲突
- 脚本会自动使用不同端口（8080, 8081, 8082...）
- 如果仍有冲突，请关闭其他占用端口的程序

### 连接问题
- 确保防火墙允许Java程序
- 检查是否有其他程序占用相同端口
- 尝试重新启动测试

## 📊 测试结果验证

### 成功指标
- ✅ 所有节点成功启动
- ✅ 节点能够相互发现和连接
- ✅ 消息能够在节点间传递
- ✅ 文件传输功能正常
- ✅ 网络具有故障恢复能力

### 常见问题
- ❌ GUI窗口无法启动 → 检查JavaFX安装
- ❌ 节点无法连接 → 检查端口和防火墙
- ❌ 消息传递失败 → 检查网络连接状态

## 🎮 高级测试场景

详细的测试场景和步骤请参考 `TESTING_GUIDE.md` 文件。

---

**快速开始：** 双击 `test-launcher.bat` (Windows) 或运行 `./multi-gui-test.sh` (Linux/Mac)
