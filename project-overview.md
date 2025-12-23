# QStory BeanShell 脚本开发框架

## 核心理念

**开发时写 Java，运行时是 BeanShell** - 享受完整 IDE 支持，自动转换为 BeanShell 脚本

## 快速开始

### Gradle 命令

| 命令 | 说明 |
|------|------|
| `gradlew listScripts` | 列出所有可用脚本 |
| `gradlew listDevices` | 列出所有连接的设备 |
| `gradlew runScript -PscriptName=xxx` | 转换+部署（快捷命令） |
| `gradlew convertScript -PscriptName=xxx` | 仅转换 |
| `gradlew deployScript -PscriptName=xxx` | 仅部署 |
| `gradlew fetchError -PscriptName=xxx` | 获取错误日志 |
| `gradlew createScript -PscriptName=xxx -PdisplayName="显示名"` | 创建新脚本 |

**设备选择**: 添加 `-PdeviceId=xxx` 可指定目标设备

## 项目结构

```
src/main/java/me/mm/qs/
├── script/                        # 框架基础类（不要修改）
│   ├── Globals.java               # 全局变量定义
│   ├── QScriptBase.java           # 基类（所有API方法）
│   ├── annotation/                # 注解定义
│   └── types/                     # 类型定义（带IDE提示）
└── scripts/                       # 所有脚本源代码
    ├── example/                   # 示例脚本（模板）
    │   └── Main.java
    └── voice_converter/           # 实际脚本
        ├── .script-id             # 脚本ID（自动生成，固定不变）
        ├── Main.java              # 主脚本
        ├── constants/             # 常量类
        ├── enums/                 # 枚举类
        └── utils/                 # 工具类

scripts/
└── JavaToBeanShellConverter.java  # 转换器

dist/                              # 转换后的脚本输出
├── .script-mapping            # 源目录名 → 显示名映射
└── 语音转换/                   # 按显示名命名的目录
    ├── main.java
    ├── desc.txt
    ├── info.prop
    └── utils/
```

## 核心注解

| 注解 | 作用 |
|------|------|
| `@ScriptInfo` | 脚本元数据(name, author, version, description, tags) |
| `@RootCode` | 去掉类和方法包装，代码直接放到脚本根级别执行 |
| `@GlobalInstance` | 纯数据类，在 main.java 中自动生成全局实例 |
| `@BeanShellType` | 类型转换为 Object |

## 转换规则

### 1. Main 类
去掉大括号，保留所有内容（变量+方法）到根级别

### 2. @RootCode 类/方法
仅保留方法体内容，直接放到脚本根级别
```java
// Java 源码
@RootCode
class Init extends QScriptBase {
    public void init() {
        addItem("开关", "toggle");
        toast("脚本已加载");
    }
}

// 转换后（自动去掉类和方法包装）
addItem("开关", "toggle");
toast("脚本已加载");
```

### 3. @GlobalInstance 类
在 main.java 中生成类定义 + 全局实例
```java
// Java 源码
@GlobalInstance
public class AudioDecoderState {
    public static int lastSampleRate = 16000;
}

// 转换后的 main.java
class Utils_AudioDecoderState {
    lastSampleRate = 16000;
}
Utils_AudioDecoderState = new Utils_AudioDecoderState();

// 使用
Utils_AudioDecoderState.lastSampleRate = 24000;
```

### 4. 其他类
保留类定义，按路径重命名：`MessageType` → `Constants_MessageType`

### 5. import
转换为 `load(appPath + "/path/File.java")`

### 6. 类型
`MessageData` 等转换为 `Object`

## 全局变量

**`Globals.java`** 定义了所有 BeanShell 全局变量：
```java
public class Globals {
    public static String myUin;      // 当前用户 QQ
    public static Context context;   // QQ 上下文
    public static String appPath;    // 脚本目录
    public static ClassLoader loader;
    public static String pluginID;
}
```

**使用方式**:
- 在任何类中: `import static me.mm.qs.script.Globals.*;`
- 转换后自动移除 `Globals.` 前缀

## 开发规范

### 工具类
- 放在 `utils/`、`constants/`、`enums/` 等子目录
- 转换后自动重命名为 `路径_类名`

### 全局变量使用
- 必须 `import static me.mm.qs.script.Globals.*;`
- static 方法中用 `Globals.myUin`

### 常量类处理（自动化）
- 常量类放在 `constants/` 或 `enums/` 目录
- 转换后按路径重命名: `MessageType` → `Constants_MessageType`
- **转换器自动处理所有引用**，你无需手动修改

### 构建特性
- 每次构建前自动删除旧的脚本目录
- 每个脚本有独立的 `.script-id`，创建时生成，之后固定
- 编码已配置 UTF-8 (gradlew.bat 已设置 chcp 65001)

## BeanShell 限制与解决方案

### 1. 类字段访问问题

**问题**: BeanShell 中类字段需要实例化才能访问
```javascript
// ✖️ 错误 - 直接访问会报错
class MyClass {
    CONSTANT = 1;
}
MyClass.CONSTANT  // 错误: undefined variable

// ✔️ 正确 - 必须实例化
obj = new MyClass();
obj.CONSTANT  // 正确
```

**解决方案**: 
- 常量类保留类定义,不自动实例化
- 用户在需要时手动实例化: `MyMessageType = new Constants_MyMessageType();`

### 2. load() 命名空间隔离

**问题**: `load()` 在独立的命名空间中执行,变量不会暴露到全局
```javascript
// file1.java
MyVar = 123;

// main.java
load("file1.java");
toast(MyVar);  // 错误: MyVar 找不到
```

**尝试过的方案**:
- ✖️ `source()` - BeanShell 不支持
- ✖️ 别名赋值 - 局部作用域问题
- ✖️ 内联到 main.java - 会重复加载

**最终方案**: 
- 常量类通过 `load()` 加载
- 用户自己实例化后使用

### 3. 类名冲突

**问题**: 不同路径下可能有相同类名
```
constants/MessageType.java
enums/MessageType.java
```

**解决方案**: 按路径重命名
- `constants/MessageType` → `Constants_MessageType`
- `enums/MessageType` → `Enums_MessageType`

## 常见错误

| 错误 | 原因 | 解决 |
|------|------|------|
| `illegal use of undefined variable` | 常量类没实例化 | `type = new Constants_MessageType();` |
| `Class or variable not found` | load() 命名空间隔离 | 确保使用前实例化 |
| `Cannot re-assign final variable` | 重复加载 | 已通过类定义解决 |
| `File contains a path separator` | context.deleteFile() 不支持路径 | 用 `new File(path).delete()` |
| `Method xxx not found in class Yyy` | 局部变量覆盖全局变量 | 检查变量名是否重复 |

## 最佳实践

### 1. 根级别代码
```java
@RootCode
class Init extends QScriptBase {
    public void init() {
        addItem("开关", "toggle");
        toast("脚本已加载");
    }
}
// 转换后自动提取到根级别
```

### 2. 共享数据存储
```java
@GlobalInstance
class AppState extends QScriptBase {
    public static String currentUser = "";
    public static int messageCount = 0;
}
// 任何地方都可访问: AppState.currentUser
```

### 3. 文件删除
```java
// 始终使用 File 而不是 context.deleteFile()
new File(filePath).delete();
```

### 4. 变量命名
```java
// 避免局部变量覆盖全局变量（如 loader）
SilkLibraryLoader silkLoader = new SilkLibraryLoader();  // ✅ 用独特名称
SilkLibraryLoader loader = new SilkLibraryLoader();      // ✖ 会覆盖全局 loader
```

## 踩坑经验

### 变量覆盖问题
**现象**: `Method loadClass(String) not found in class 'Utils_SilkLibraryLoader'`
**原因**: 局部变量 `loader` 覆盖了全局的 `Globals.loader`，导致反射调用失败
**解决**: 将局部变量改名为 `silkLoader`

### PowerShell 编码问题
**现象**: 批量替换文件内容后中文变乱码
**原因**: PowerShell 的 `Get-Content | Set-Content` 破坏 UTF-8 编码
**解决**: 用 `git stash` 恢复，改用工具逐个文件替换

### 部署目录名映射
**现象**: 部署时使用源目录名 `voice_converter` 而不是显示名 `语音转换`
**解决**: 添加 `dist/.script-mapping` 文件记录 `voice_converter=语音转换` 映射

### 版本保存
**最佳实践**: 重要版本用 Git tag 保存
```bash
git tag v1.0.0
git push origin v1.0.0
```