# QStory BeanShell 脚本开发助手

这个项目为 QStory BeanShell 脚本开发提供完整的 IDE 代码提示支持。

## 项目结构

```
qs-dev/
├── src/main/java/me/mm/qs/
│   ├── script/
│   │   ├── annotation/          # 注解定义
│   │   │   ├── BeanShellType.java    # 标记需要转换为 Object 的类型
│   │   │   ├── ScriptMethods.java    # 标记方法需要提取到根级别的类
│   │   │   └── RootCode.java         # 标记方法体直接作为根级代码
│   │   ├── types/               # 类型定义(带完整字段提示)
│   │   │   ├── MessageData.java      # 消息数据类型
│   │   │   ├── GroupInfo.java        # 群组信息类型
│   │   │   ├── GroupMemberInfo.java  # 群成员信息类型
│   │   │   ├── ForbiddenInfo.java    # 禁言信息类型
│   │   │   └── FriendInfo.java       # 好友信息类型
│   │   ├── QScriptBase.java     # 基类(定义所有全局变量和API方法)
│   │   └── MyScript.java        # 你的脚本实现(在这里编写代码)
│   └── api.md                   # QStory API 文档
├── scripts/
│   └── JavaToBeanShellConverter.java  # 转换器
├── dist/
│   └── main.java                # 转换后的 BeanShell 脚本
└── build.gradle                 # Gradle 配置
```

## 使用方法

### 1. 编写脚本

在 `MyScript.java` 中编写你的脚本代码:

```java
package me.mm.qs.script;

import me.mm.qs.script.annotation.RootCode;
import me.mm.qs.script.annotation.ScriptMethods;
import me.mm.qs.script.types.MessageData;

@ScriptMethods  // 标记这个类的方法会被提取到 BeanShell 根级别
public class MyScript extends QScriptBase {

    @Override
    public void onMsg(MessageData msg) {
        // 享受完整的 IDE 代码提示!
        String text = msg.MessageContent;  // IDE 会自动提示所有字段
        String qq = msg.UserUin;
        
        if ("你好".equals(text)) {
            sendMsg(msg.GroupUin, "", "你好!");
        }
    }
}

// 初始化代码
@ScriptMethods
class Init extends QScriptBase {
    @RootCode  // 方法体会直接放到根级别,不保留方法签名
    public void init() {
        addItem("菜单", "showMenu");
        toast("脚本加载成功");
    }
}
```

### 2. 编译并转换

运行以下命令:

```powershell
.\gradlew.bat compileJava
```

这会自动:
1. 编译你的 Java 代码
2. 运行转换器
3. 在 `dist/main.java` 生成 BeanShell 脚本

### 3. 使用生成的脚本

将 `dist/main.java` 复制到你的 QStory 脚本目录中使用。

## 核心特性

### 1. 完整的类型提示

所有 QStory 的数据类型都有对应的 Java 类:
- `MessageData` - 消息数据
- `GroupInfo` - 群组信息
- `GroupMemberInfo` - 群成员信息
- `ForbiddenInfo` - 禁言信息
- `FriendInfo` - 好友信息

这些类型在编写时提供完整的字段提示,转换时会自动变为 `Object`。

### 2. 注解系统

- `@BeanShellType` - 标记类型在转换时会变为 `Object`
- `@ScriptMethods` - 标记类,其中的方法会被提取到 BeanShell 根级别
- `@RootCode` - 标记方法,其方法体会直接放到根级别(不保留方法签名)

### 3. 自动类型转换

转换器会自动处理:
- 将 `MessageData` 等类型转换为 `Object`
- 移除所有注解(BeanShell 不支持注解)
- 提取方法到根级别
- 保留注释和代码逻辑

## 示例

### 输入 (MyScript.java)

```java
@ScriptMethods
public class MyScript extends QScriptBase {
    @Override
    public void onMsg(MessageData msg) {
        toast(msg.MessageContent);
    }
}
```

### 输出 (dist/main.java)

```java
public void onMsg(Object msg) {
    toast(msg.MessageContent);
}
```

## 注意事项

1. 不要直接编辑 `dist/main.java`,它会被自动覆盖
2. 所有脚本代码应在 `MyScript.java` 中编写
3. 继承 `QScriptBase` 以获得所有 API 方法的提示
4. 使用 `@ScriptMethods` 标记你的类
5. 使用 `@RootCode` 标记初始化代码

## 技术栈

- Java 17+
- Gradle
- JavaParser 3.25.0
- Android SDK (用于类型定义)
