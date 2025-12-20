# QStory BeanShell 脚本开发框架

## 核心理念

**开发时写 Java,运行时是 BeanShell** - 享受完整 IDE 支持,自动转换为 BeanShell 脚本

## 关键架构

### 1. 全局变量管理

**`Globals.java`** - 所有 BeanShell 全局变量的定义
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

### 2. 模块化开发

**Main.java** - 主脚本
```java
import me.mm.qs.myscript.utils.MessageHandler;

@ScriptMethods
public class Main extends QScriptBase {
    // 创建工具类实例
    private final MessageHandler messageHandler = new MessageHandler();
    
    public void onMsg(MessageData msg) {
        // 直接调用,有完整提示
        if (messageHandler.isMuteCommand(msg)) {
            // ...
        }
    }
}
```

**工具类** (utils/)
```java
@ScriptMethods
public class MessageHandler extends QScriptBase {
    // 实例方法 - 可调用基类方法
    public boolean isMuteCommand(MessageData msg) {
        return msg.IsSend && ...
    }
    
    // static 方法 - 只用全局变量
    public static boolean isAtMe(MessageData msg) {
        return msg.MessageContent.contains("@" + Globals.myUin);
    }
}
```

### 3. 自动转换

转换器 (`scripts/JavaToBeanShellConverter.java`) 自动处理:
- ✅ `import utils.XXX` → `load(appPath + "/utils/XXX.java")`
- ✅ `messageHandler.xxx()` → `xxx()`
- ✅ `Globals.myUin` → `myUin`
- ✅ `MessageData` → `Object`
- ✅ 移除 `static` 修饰符和注解
- ✅ **自动处理常量类引用**：
  - `new MyMessageType()` → `new Constants_MyMessageType()`
  - `MyMessageType type =` → `type =` (移除类型声明)
  - `ArrayList<MyMessageType>` → `ArrayList` (移除泛型)

## 核心注解

- `@ScriptInfo` - 脚本元数据(name, author, version, description, tags)
- `@ScriptMethods` - 标记类的方法需要提取
- `@RootCode` - 提取方法体到根级别（支持类和方法两个级别）
  - **类级别**: `@RootCode class Init { ... }` - 提取类中所有方法体到根级别
  - **方法级别**: `@RootCode void init() { ... }` - 只提取该方法体到根级别
- `@GlobalInstance` - 标记纯数据类，在 main.java 中自动生成全局实例
- `@BeanShellType` - 类型转换为 Object

## 编译和部署

```bash
# 编译 (自动转换)
gradlew compileJava

# 部署到设备
gradlew deployScript

# 拉取错误日志
gradlew fetchError
```

## 目录结构

```
src/main/java/me/mm/qs/
├── script/
│   ├── Globals.java          # 全局变量定义
│   ├── QScriptBase.java      # 基类(只含方法)
│   ├── annotation/           # 注解定义
│   └── types/                # 类型定义
└── myscript/
    ├── Main.java             # 主脚本
    ├── constants/            # 常量类目录
    │   └── MyMessageType.java
    ├── enums/                # 枚举类目录
    │   └── ChatType.java
    └── utils/                # 工具模块
        ├── MessageHandler.java
        └── Helper.java

dist/脚本名称/                 # 生成的 BeanShell 脚本
├── .script-id               # 脚本 ID (只生成一次)
├── main.java
├── desc.txt
├── info.prop
├── constants/
│   └── MyMessageType.java
├── enums/
│   └── ChatType.java
└── utils/
    ├── MessageHandler.java
    └── Helper.java
```

## 重要说明

1. **工具类必须**:
   - 继承 `QScriptBase`
   - 添加 `@ScriptMethods` 注解

2. **全局变量使用**:
   - 必须 `import static me.mm.qs.script.Globals.*;`
   - static 方法中用 `Globals.myUin`

3. **`@GlobalInstance` 纯数据类** (新功能!):
   - **用途**: 存放需要共享的静态数据（只有静态字段，无方法）
   - **处理方式**:
     - 在 main.java 中生成类定义 + 全局实例（load 语句之前）
     - 常量类：变量名 = 重命名后的类名（如 `Constants_MessageType`）
     - 数据类：变量名 = 原类名（如 `AudioDecoderState`）
     - 原文件不生成（仅剩注释的文件被跳过）
     - 不生成 load 语句（避免加载不存在的文件）
   - **示例**:
     ```java
     // AudioDecoderState.java - 纯数据类
     @ScriptMethods
     @GlobalInstance
     public class AudioDecoderState extends QScriptBase {
         public static int lastSampleRate = 16000;
         public static int lastBitDepth = 16;
         public static int lastChannels = 1;
     }
     
     // 转换后的 main.java（自动生成）
     class AudioDecoderState {
         lastSampleRate = 16000;
         lastBitDepth = 16;
         lastChannels = 1;
     }
     AudioDecoderState = new AudioDecoderState();
     
     // 使用（在任何文件中）
     AudioDecoderState.lastSampleRate = 24000;
     ```

4. **常量类处理** (重要! - 已自动化):
   - 常量类放在 `constants/` 或 `enums/` 目录
   - 转换后会按路径重命名: `MessageType` → `Constants_MessageType`
   - **转换器自动处理所有引用**，你无需手动修改：
     ```java
     // Java 代码 (Main.java) - 正常写，享受 IDE 提示
     MyMessageType type = new MyMessageType();
     if (msg.MessageType == type.VOICE) { ... }
     
     // 转换后的 BeanShell (自动处理)
     type = new Constants_MyMessageType();
     if (msg.MessageType == type.VOICE) { ... }
     ```
   - **支持的转换**：
     - `new ClassName()` → `new Prefix_ClassName()`
     - `ClassName var =` → `var =` (移除类型)
     - `ArrayList<ClassName>` → `ArrayList` (移除泛型)
   - 每个常量类会生成独立文件，通过 `load()` 加载

5. **构建清理**:
   - 每次构建前自动删除旧的脚本目录
   - 避免残留旧文件（如删除了类但文件还在）

6. **编码问题**:
   - 已配置 UTF-8 (gradle.properties)
   - 中文正常显示

7. **Git 集成**:
   - 如果 IDEA 提示创建仓库,检查 `.idea/vcs.xml` 是否存在

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

1. **`illegal use of undefined variable, class, or 'void' literal`**
   - 原因: 常量类没有实例化
   - 解决: `MyMessageType = new Constants_MyMessageType();`

2. **`Class or variable not found: MessageType.VOICE`**
   - 原因: load() 命名空间隔离
   - 解决: 确保在使用前实例化

3. **`Cannot re-assign final variable`**
   - 原因: 重复加载导致常量重复定义
   - 解决: 已通过类定义解决(可重复加载)

4. **`File ... contains a path separator` (删除文件错误)**
   - 原因: `context.deleteFile()` 只接受文件名，不接受路径
   - 错误: `context.deleteFile("/path/to/file.pcm");`
   - 正确: `new File("/path/to/file.pcm").delete();`

## 最佳实践

1. **根级别代码初始化**:
   ```java
   // 只需一个注解！
   @RootCode
   class Init extends QScriptBase {
       public void init() {
           addItem("开关加载提示", "加载提示");
           toast("脚本已加载");
       }
   }
   // 转换后自动提取到根级别
   ```

2. **共享数据存储**:
   ```java
   // 使用 @GlobalInstance 而不是 static 字段
   @GlobalInstance
   class AppState extends QScriptBase {
       public static String currentUser = "";
       public static int messageCount = 0;
   }
   // 任何地方都可以访问: AppState.currentUser
   ```

3. **文件删除**:
   ```java
   // 始终使用 File 而不是 context.deleteFile()
   new File(filePath).delete();
   ```