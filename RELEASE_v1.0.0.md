# Release v1.0.0 - 语音转换脚本

## 🎉 首个正式版本发布

本次发布是 QStory 语音转换脚本的首个正式版本，支持将 QQ 语音消息的 Silk 格式解码为可播放的 WAV 格式。

## ✨ 主要功能

### 语音转换
- **Silk 解码**: 支持将 QQ 语音消息（Silk 格式）解码为 PCM 格式
- **WAV 转换**: 自动将 PCM 转换为标准 WAV 格式，可直接播放
- **自定义保存**: 弹出对话框让用户自定义文件名和保存路径
  - 默认文件名：原始语音文件名
  - 默认路径：`/storage/emulated/0/Download`
  - 支持自动处理路径末尾斜杠
  - 自动添加 `.wav` 后缀

### 用户界面
- **长按菜单**: 在语音消息上长按，选择"WAV"菜单项
- **现代化对话框**: 使用 Material Design 风格的保存对话框
- **实时反馈**: Toast 提示保存状态和路径

### 技术特性
- **多脚本架构**: 支持在 `scripts/` 目录下开发多个独立脚本
- **模块化设计**: 工具类分离（MessageHandler、Helper、AudioDecoder 等）
- **类型安全**: 使用 Java 开发，IDE 完整代码提示
- **自动转换**: Java 代码自动转换为 BeanShell 脚本
- **独立脚本 ID**: 每个脚本拥有独立的随机 ID

## 📦 项目结构

```
scripts/voice_converter/
├── Main.java                    # 主入口
├── constants/
│   └── MessageType.java        # 消息类型常量
├── enums/
│   └── ChatType.java          # 聊天类型枚举
└── utils/
    ├── AudioDecoderState.java  # 音频解码状态
    ├── Helper.java            # 辅助工具
    ├── MessageHandler.java    # 消息处理
    ├── PcmToWavConverter.java # WAV 转换器
    ├── SilkAudioDecoder.java  # Silk 解码器
    └── SilkLibraryLoader.java # Silk 库加载器
```

## 🔧 使用方法

### 开发
```bash
# 创建新脚本
gradlew createScript -PscriptName=my_script -PdisplayName=我的脚本

# 转换并部署
gradlew deploy -PscriptName=voice_converter

# 拉取错误日志
gradlew fetchError -PscriptName=voice_converter
```

### 使用脚本
1. 部署脚本到设备
2. 在 QQ 中找到语音消息
3. 长按语音消息
4. 选择"WAV"菜单
5. 在弹出的对话框中设置文件名和路径
6. 点击"保存"

## 🛠️ 技术实现

### 转换器功能
- **保留外部 import**: Android 和 Java 标准库的 import 会被保留
- **动态包名检测**: 自动从 Main.java 获取脚本根包名
- **@GlobalInstance**: 全局单例类直接内联到 main.java
- **@RootCode**: 去掉类和方法包装，代码直接放到根级别
- **构建信息**: 每个生成的文件包含源文件路径和构建时间

### 主线程处理
- 使用 `activity.runOnUiThread()` 确保 UI 操作在主线程执行
- 避免 `Can't create handler inside thread` 错误

### 样式优化
- 使用 `Theme_DeviceDefault_Light_Dialog_Alert` 主题
- 现代化的 Material Design 风格

## 🐛 已知问题

无

## 📝 更新日志

### v1.0.0 (2025-12-21)
- ✨ 首次发布
- ✨ Silk 音频解码为 WAV 格式
- ✨ 自定义保存对话框
- ✨ 多脚本开发架构
- ✨ 自动 Java 到 BeanShell 转换
- 🐛 修复子目录文件的 load 路径问题
- 🐛 修复主线程 UI 显示问题
- 🎨 使用 Material Design 对话框样式

## 🙏 致谢

感谢 QStory 框架提供的插件支持。

## 📄 许可证

本项目仅供学习交流使用。
