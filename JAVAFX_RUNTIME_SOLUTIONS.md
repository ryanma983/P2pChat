# JavaFX运行时问题解决方案

## ❌ 错误信息
```
错误: 缺少 JavaFX 运行时组件, 需要使用该组件来运行此应用程序
```

## 🔍 问题原因

从Java 11开始，JavaFX不再包含在JDK中，即使我们的JAR文件包含了JavaFX库，某些Java版本仍然需要特殊的启动参数。

## 🛠️ 解决方案

### 方案1：使用模块路径参数（推荐）

**Windows:**
```cmd
java --module-path . --add-modules javafx.controls,javafx.fxml -jar p2p-chat-1.0-SNAPSHOT.jar
```

**Linux/Mac:**
```bash
java --module-path . --add-modules javafx.controls,javafx.fxml -jar p2p-chat-1.0-SNAPSHOT.jar
```

### 方案2：下载并安装JavaFX SDK

1. **下载JavaFX SDK**:
   - 访问: https://openjfx.io/
   - 下载适合您系统的JavaFX SDK
   - 解压到某个目录，例如: `C:\javafx-sdk-17.0.2`

2. **使用JavaFX SDK运行**:
```cmd
java --module-path "C:\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar p2p-chat-1.0-SNAPSHOT.jar
```

### 方案3：使用命令行版本（无需JavaFX）

如果GUI版本无法运行，可以使用命令行版本：

1. **确保项目已编译**:
```cmd
mvn clean compile
```

2. **运行命令行版本**:
```cmd
java -cp target/classes com.group7.chat.Main
```

### 方案4：使用包含JavaFX的Java发行版

下载并安装包含JavaFX的Java发行版：
- **Azul Zulu FX**: https://www.azul.com/downloads/?package=jdk-fx
- **Liberica JDK Full**: https://bell-sw.com/pages/downloads/

## 🚀 快速解决脚本

我将为您创建自动化脚本来解决这个问题。

### Windows批处理脚本 (run-with-javafx.bat)
```batch
@echo off
echo Trying to run P2P Chat with JavaFX support...

REM 方法1: 尝试使用模块路径
echo Method 1: Using module path...
java --module-path . --add-modules javafx.controls,javafx.fxml -jar p2p-chat-1.0-SNAPSHOT.jar
if %ERRORLEVEL% EQU 0 goto :success

REM 方法2: 尝试直接运行
echo Method 2: Direct execution...
java -jar p2p-chat-1.0-SNAPSHOT.jar
if %ERRORLEVEL% EQU 0 goto :success

REM 方法3: 运行命令行版本
echo Method 3: Running CLI version...
java -cp target/classes com.group7.chat.Main
if %ERRORLEVEL% EQU 0 goto :success

echo All methods failed. Please check JavaFX installation.
goto :end

:success
echo Application started successfully!

:end
pause
```

### Linux/Mac脚本 (run-with-javafx.sh)
```bash
#!/bin/bash
echo "Trying to run P2P Chat with JavaFX support..."

# 方法1: 尝试使用模块路径
echo "Method 1: Using module path..."
if java --module-path . --add-modules javafx.controls,javafx.fxml -jar p2p-chat-1.0-SNAPSHOT.jar; then
    echo "Application started successfully!"
    exit 0
fi

# 方法2: 尝试直接运行
echo "Method 2: Direct execution..."
if java -jar p2p-chat-1.0-SNAPSHOT.jar; then
    echo "Application started successfully!"
    exit 0
fi

# 方法3: 运行命令行版本
echo "Method 3: Running CLI version..."
if java -cp target/classes com.group7.chat.Main; then
    echo "Application started successfully!"
    exit 0
fi

echo "All methods failed. Please check JavaFX installation."
exit 1
```

## 🔧 检查您的Java版本

运行以下命令检查Java版本：
```cmd
java -version
```

**推荐的Java版本**:
- Java 11 + 单独的JavaFX
- Java 17 + 单独的JavaFX
- 或使用包含JavaFX的发行版

## 📋 故障排除步骤

1. **检查Java版本**: `java -version`
2. **尝试方案1**: 使用模块路径参数
3. **如果失败**: 下载JavaFX SDK (方案2)
4. **最后选择**: 使用命令行版本 (方案3)

## 💡 为什么会出现这个问题？

1. **Java模块系统**: Java 9+引入了模块系统
2. **JavaFX分离**: 从Java 11开始，JavaFX不再包含在JDK中
3. **模块路径**: 需要明确指定JavaFX模块的位置

## ✅ 推荐解决方案

**最简单的方法**:
1. 下载Azul Zulu FX (包含JavaFX的Java)
2. 或者使用我提供的启动脚本

**最可靠的方法**:
1. 下载JavaFX SDK
2. 使用完整的模块路径命令
