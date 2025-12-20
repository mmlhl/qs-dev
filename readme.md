# QStory BeanShell 脚本开发助手

这个项目为 QStory BeanShell 脚本开发提供完整的 IDE 代码提示支持。

## 项目结构

```
qs-dev/
├── src/main/java/me/mm/qs/
│   ├── script/                      # 框架基础类
│   │   ├── annotation/              # 注解定义
│   │   │   ├── ScriptInfo.java      # 脚本信息注解
│   │   │   ├── RootCode.java        # 根级代码注解
│   │   │   └── GlobalInstance.java  # 全局实例注解
│   │   ├── types/                   # 类型定义(带完整字段提示)
│   │   ├── QScriptBase.java         # 基类(所有API方法)
│   │   └── Globals.java             # 全局变量
│   └── scripts/                     # 所有脚本源代码
│       ├── example/                 # 示例脚本模板
│       │   └── Main.java
│       └── voice_converter/         # 语音转换脚本
│           ├── Main.java
│           ├── constants/
│           ├── enums/
│           └── utils/
├── scripts/
│   └── JavaToBeanShellConverter.java  # 转换器
├── dist/                            # 转换后的脚本输出
└── build.gradle
```

## 快速开始

### 1. 查看可用脚本

```powershell
.\gradlew.bat listScripts
```

### 2. 创建新脚本

```powershell
.\gradlew.bat createScript -PscriptName=my_script -PdisplayName="我的脚本"
```

### 3. 转换并部署脚本

```powershell
.\gradlew.bat runScript -PscriptName=my_script
```

## Gradle 命令

| 命令 | 说明 |
|------|------|
| `gradlew listScripts` | 列出所有可用脚本 |
| `gradlew listDevices` | 列出所有连接的设备 |
| `gradlew runScript -PscriptName=xxx` | 转换+部署（快捷命令） |
| `gradlew convertScript -PscriptName=xxx` | 仅转换 |
| `gradlew deployScript -PscriptName=xxx` | 仅部署 |
| `gradlew fetchError -PscriptName=xxx` | 获取错误日志 |
| `gradlew createScript -PscriptName=xxx` | 创建新脚本 |

**设备选择**: 添加 `-PdeviceId=xxx` 可指定目标设备

## 编写脚本

在 `src/main/java/me/mm/qs/scripts/<你的脚本>/Main.java` 中编写代码:

```java
package me.mm.qs.scripts.my_script;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.script.annotation.RootCode;
import me.mm.qs.script.annotation.ScriptInfo;
import me.mm.qs.script.types.MessageData;

import static me.mm.qs.script.Globals.*;

@ScriptInfo(
    name = "我的脚本",
    author = "作者",
    version = "1.0.0",
    description = "脚本描述",
    tags = "功能标签"
)
public class Main extends QScriptBase {
    
    @Override
    public void onMsg(MessageData msg) {
        // 享受完整的 IDE 代码提示!
        String text = msg.MessageContent;
        String qq = msg.UserUin;
        
        if ("你好".equals(text)) {
            sendMsg(msg.GroupUin, "", "你好!");
        }
    }
    
    @Override
    public void onCreateMenu(MessageData msg) {
        // 长按消息菜单
        addMenuItem("操作", "myAction");
    }
    
    public void myAction(MessageData msg) {
        toast("执行操作");
    }
}

@RootCode
class Init extends QScriptBase {
    public void init() {
        addItem("菜单", "showMenu");
        toast("脚本加载成功");
    }
}
```

## 注解说明

| 注解 | 说明 |
|------|------|
| `@ScriptInfo` | 脚本元信息（名称、作者、版本等） |
| `@RootCode` | 方法体直接放到根级别（用于初始化代码） |
| `@GlobalInstance` | 全局实例，在 main.java 中自动初始化 |

## 转换规则

1. **Main 类**: 去掉大括号，保留所有内容（变量+方法）到根级别
2. **@RootCode 类/方法**: 只保留方法体内容
3. **@GlobalInstance 类**: 在 main.java 中生成全局实例
4. **其他类**: 保留类定义，重命名为 `路径_类名`
5. **import**: 转换为 `load(appPath + "/path/File.java")`
6. **类型转换**: `MessageData` 等转换为 `Object`

## 技术栈

- Java 17+
- Gradle 8.x
- JavaParser 3.25.0
- Android SDK (用于类型定义)
