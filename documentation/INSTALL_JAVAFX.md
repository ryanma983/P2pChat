# JavaFX 安装指南

## ❌ 错误信息
```
java.lang.module.FindException: Module javafx.controls not found
```

## 🔍 问题原因
您的Java环境中没有JavaFX模块。从Java 11开始，JavaFX不再包含在标准JDK中。

## 🚀 解决方案

### 方案1：下载包含JavaFX的Java发行版（推荐）

**Azul Zulu JDK with JavaFX（最简单）:**
1. 访问: https://www.azul.com/downloads/?package=jdk-fx
2. 选择您的操作系统（Windows）
3. 下载并安装 Zulu JDK FX
4. 重新运行应用

**Liberica JDK Full:**
1. 访问: https://bell-sw.com/pages/downloads/
2. 选择 "Full" 版本（包含JavaFX）
3. 下载并安装
4. 重新运行应用

### 方案2：下载JavaFX SDK

1. **下载JavaFX SDK:**
   - 访问: https://openjfx.io/
   - 下载适合Windows的JavaFX SDK
   - 解压到某个目录，例如: `C:\javafx-sdk-17.0.2`

2. **使用JavaFX SDK运行:**
```cmd
java --module-path "C:\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar
```

### 方案3：使用命令行版本（无需JavaFX）

如果您只想快速测试应用功能：

```cmd
# 运行命令行版本
java -cp target\classes com.group7.chat.Main

# 或双击
start-cli.bat
```

### 方案4：使用Maven运行（如果已安装Maven）

```cmd
mvn clean compile
mvn javafx:run
```

## 🎯 推荐步骤

### 最简单的方法：
1. **卸载当前Java**
2. **下载并安装 Azul Zulu JDK FX**: https://www.azul.com/downloads/?package=jdk-fx
3. **重新运行**: `start-simple.bat`

### 快速测试方法：
1. **双击运行**: `start-cli.bat`
2. **使用命令行界面测试应用功能**

## 📋 验证安装

安装完成后，运行以下命令验证：

```cmd
java -version
java --list-modules | findstr javafx
```

如果看到javafx相关模块，说明安装成功。

## 🔧 创建自定义启动脚本

如果您下载了JavaFX SDK到 `C:\javafx-sdk-17.0.2`，创建一个批处理文件：

```batch
@echo off
java --module-path "C:\javafx-sdk-17.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar target\p2p-chat-1.0-SNAPSHOT.jar
pause
```

## 💡 注意事项

1. **路径中不要有中文字符**
2. **确保Java版本是11或更高**
3. **JavaFX版本要与Java版本兼容**

选择最适合您的方案，推荐使用方案1（Azul Zulu JDK FX）最简单！
