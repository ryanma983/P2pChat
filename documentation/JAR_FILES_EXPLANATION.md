# JAR文件说明

## 📦 为什么有两个JAR文件？

在 `target/` 目录下有两个JAR文件，这是由Maven的不同插件生成的：

### 1. `decentralized-chat-1.0-SNAPSHOT.jar` (113KB)
- **生成插件**: Maven JAR Plugin (默认)
- **内容**: 仅包含项目自己的代码
- **大小**: 113KB
- **依赖**: 不包含外部依赖（JavaFX、Gson等）
- **运行方式**: 需要手动指定classpath
- **用途**: 用于开发和调试，或作为库文件

**运行命令**:
```bash
# 无法直接运行，因为缺少依赖
java -jar target/decentralized-chat-1.0-SNAPSHOT.jar  # ❌ 会报错

# 需要指定classpath
java -cp target/classes:依赖路径 com.group7.chat.gui.ChatApplication
```

### 2. `p2p-chat-1.0-SNAPSHOT.jar` (8.4MB)
- **生成插件**: Maven Shade Plugin
- **内容**: 包含项目代码 + 所有依赖库
- **大小**: 8.4MB
- **依赖**: 包含JavaFX、Gson等所有依赖
- **运行方式**: 可以直接运行
- **用途**: 最终发布版本，用户友好

**运行命令**:
```bash
# 可以直接运行 ✅
java -jar target/p2p-chat-1.0-SNAPSHOT.jar
```

## 🔍 详细对比

| 特性 | 小JAR (113KB) | 大JAR (8.4MB) |
|------|---------------|---------------|
| **包含内容** | 仅项目代码 | 项目代码 + 所有依赖 |
| **JavaFX** | ❌ 不包含 | ✅ 包含 |
| **Gson** | ❌ 不包含 | ✅ 包含 |
| **直接运行** | ❌ 不可以 | ✅ 可以 |
| **文件大小** | 小 | 大 |
| **用途** | 开发/库文件 | 最终发布 |

## 🎯 推荐使用

**对于最终用户**: 使用 `p2p-chat-1.0-SNAPSHOT.jar`
```bash
java -jar target/p2p-chat-1.0-SNAPSHOT.jar
```

**对于开发者**: 两个都有用
- 小JAR用于开发和调试
- 大JAR用于测试最终发布版本

## 🛠️ 技术原理

### Maven JAR Plugin (默认)
```xml
<!-- 自动包含在所有Maven项目中 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
</plugin>
```
- 只打包 `src/main/java` 和 `src/main/resources` 中的内容
- 不包含依赖库
- 生成 `${artifactId}-${version}.jar`

### Maven Shade Plugin (我们添加的)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <configuration>
        <finalName>p2p-chat-1.0-SNAPSHOT</finalName>
        <!-- 包含所有依赖 -->
    </configuration>
</plugin>
```
- 将所有依赖库"合并"到一个JAR中
- 创建"fat JAR"或"uber JAR"
- 可以独立运行，无需额外依赖

## 🗂️ 文件结构对比

**小JAR内容**:
```
META-INF/
css/
fxml/
com/group7/chat/  (仅项目代码)
```

**大JAR内容**:
```
META-INF/
css/
fxml/
com/group7/chat/     (项目代码)
com/sun/javafx/      (JavaFX库)
com/google/gson/     (Gson库)
javafx/              (JavaFX模块)
... (其他依赖库)
```

## 💡 最佳实践

1. **发布给用户**: 只提供大JAR文件
2. **版本控制**: 通常不提交JAR文件到Git
3. **CI/CD**: 构建时生成，发布到仓库
4. **文档**: 在README中说明使用大JAR文件
