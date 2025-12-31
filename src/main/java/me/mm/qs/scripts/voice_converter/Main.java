package me.mm.qs.scripts.voice_converter;

import me.mm.qs.scripts.voice_converter.constants.MessageType;
import me.mm.qs.script.QScriptBase;
import me.mm.qs.script.annotation.RootCode;
import me.mm.qs.script.annotation.ScriptInfo;
import me.mm.qs.script.types.MessageData;
import me.mm.qs.scripts.voice_converter.utils.MessageHandler;
import me.mm.qs.scripts.voice_converter.utils.Helper;
import me.mm.qs.scripts.voice_converter.utils.SilkAudioDecoder;
import me.mm.qs.scripts.voice_converter.utils.PcmToWavConverter;
import me.mm.qs.scripts.voice_converter.utils.PcmToMp3Converter;
import me.mm.qs.scripts.voice_converter.utils.AudioDecoderState;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static me.mm.qs.script.Globals.*;

import me.mm.qs.scripts.voice_converter.statue.Statue;
import me.mm.qs.scripts.voice_converter.web.WebDialog;

/**
 * Main script entry point.
 * This is the entry file that will be converted to main.java.
 */
@ScriptInfo(
        name = "语音转换",
        author = "木木",
        version = "1.1",
        description = "将qq语音文件转换为可以用其他软件直接播放的格式\n" +
                "加载脚本之后，聊天页面点击悬浮窗，启用功能\n" +
                "启用之后在消息界面，长按语音，点击wav即可进行转换，默认保存到download文件夹\n" +
                "以后会增加更多功能，批量转换以及转换到其他格式",
        tags = "功能拓展"
)
public class Main extends QScriptBase {
    // Utility instances - will be removed in BeanShell output
    private final MessageHandler messageHandler = new MessageHandler();
    private final Helper helper = new Helper();
    private final SilkAudioDecoder audioDecoder = new SilkAudioDecoder();
    private final PcmToWavConverter wavConverter = new PcmToWavConverter();
    private final PcmToMp3Converter mp3Converter = new PcmToMp3Converter();
    private final WebDialog dialog = new WebDialog();

    // Callback methods - will be extracted to root level in BeanShell
    @Override
    public void onMsg(MessageData msg) {
        // Now you have full IDE hints!
        String text = msg.MessageContent;
        String qq = msg.UserUin;
        String qun = msg.GroupUin;
        boolean isGroup = msg.IsGroup;
        
        // 测试 FFmpeg 版本
        if ("ffmpeg版本".equals(text) && qq.equals(myUin)) {
            String version = mp3Converter.getVersion();
            if (version != null) {
                toast("FFmpeg 已就绪");
                log("[FFmpeg Version] " + version);
            } else {
                toast("FFmpeg 未就绪，请查看日志");
            }
            return;
        }
        
        /*if ("菜单".equals(text) && qq.equals(myUin)) {
            String reply = "TG频道：https://t.me/QStoryPlugin\n交流群:979938489\n---------\n这是菜单 你可以发送下面的指令来进行测试  \n艾特我\n回复我\n私聊我";

            if (isGroup) {
                sendMsg(qun, "", reply);
            } else {
                sendMsg("", qq, reply);
            }
        }

        if ("艾特我".equals(text) && isGroup && qq.equals(myUin)) {
            sendMsg(qun, "", "[AtQQ=" + qq + "] 嗯呐");
        }

        if ("回复我".equals(text) && isGroup && qq.equals(myUin)) {
            sendReply(qun, msg, "好啦");
        }

        if ("私聊我".equals(text)) {
            sendMsg(qun, qq, "我已经私聊你咯");
        }
        if ("回表情".equals(text)) {
            replyEmoji(msg, "1");
        }
        
        // 注释掉测试代码，避免BeanShell命名空间问题
        // 测试新增的工具方法
        if (messageHandler.containsKeyword(text, "测试")) {
            toast("检测到关键词:测试");
        }

        // 使用模块方法处理禁言命令
        if (messageHandler.isMuteCommand(msg)) {
            int banTime = messageHandler.parseTimeBymessage(msg);
            if (banTime >= 60 * 60 * 24 * 30 + 1) {
                sendMsg(msg.GroupUin, "", "请控制在30天以内");
                return;
            } else {
                for (String atUin : msg.mAtList) {
                    forbidden(msg.GroupUin, atUin, banTime);
                }
            }
        }*/
    }

    @Override
    public void onCreateMenu(MessageData msg) {
        // 为语音消息添加解码菜单
        if (msg.MessageType == MessageType.VOICE) {
//            addMenuItem("PCM", "saveVoice");
            if (Statue.SHELL_OPEN) {
                addMenuItem("WAV", "saveVoiceAsWav");
                addMenuItem("MP3", "saveVoiceAsMp3");
            }

        }
    }


    // Custom menu callback - 解码语音为PCM
    public void saveVoice(MessageData msg) {
        if (msg.MessageType == MessageType.VOICE) {
            String pcmPath = audioDecoder.decodeVoiceMessage(msg.LocalPath);
        } else {
            toast("这不是语音消息");
        }
    }

    // Custom menu callback - 解码语音为 MP3
    public void saveVoiceAsMp3(MessageData msg) {
        if (msg.MessageType != MessageType.VOICE) {
            toast("这不是语音消息");
            return;
        }

        String pcmPath = null;
        try {
            // 获取原始文件名（不含路径和后缀）
            String localPath = msg.LocalPath;
            String originalName = localPath.substring(localPath.lastIndexOf("/") + 1);
            if (originalName.contains(".")) {
                originalName = originalName.substring(0, originalName.lastIndexOf("."));
            }

            // 1. 先将 slk 转换为 pcm（保存在脚本目录下）
            pcmPath = audioDecoder.decodeVoiceMessage(localPath);
            if (pcmPath == null) {
                toast("解码失败");
                return;
            }
            log("pcmPath: " + pcmPath);

            // 2. 将 pcm 转换为 mp3，保存到脚本目录
            String mp3Path = appPath + "/" + originalName + ".mp3";
            String result = mp3Converter.convertToMp3(pcmPath, mp3Path);

            if (result != null) {
                toast("已保存到: " + result);
            } else {
                toast("MP3 转换失败");
            }
        } catch (Exception e) {
            error(e);
            toast("转换失败: " + e.getMessage());
        } finally {
            // 删除临时 pcm 文件
            /*if (pcmPath != null) {
                new File(pcmPath).delete();
            }*/
        }
    }

    // Custom menu callback - 解码语音为WAV（可直接播放）
    public void saveVoiceAsWav(MessageData msg) {
        if (msg.MessageType != MessageType.VOICE) {
            toast("这不是语音消息");
            return;
        }

        // 获取原始文件名（不含路径和后缀）
        String localPath = msg.LocalPath;
        String originalName = localPath.substring(localPath.lastIndexOf("/") + 1);
        if (originalName.contains(".")) {
            originalName = originalName.substring(0, originalName.lastIndexOf("."));
        }
        String defaultFileName = originalName + ".wav";

        // 默认下载路径
        String defaultPath = "/storage/emulated/0/Download";

        // 显示保存对话框
        showSaveDialog(msg, defaultFileName, defaultPath);
    }

    // 显示保存对话框
    private void showSaveDialog(final MessageData msg, final String defaultFileName, final String defaultPath) {
        final android.app.Activity activity = getActivity();
        if (activity == null) {
            toast("无法获取Activity");
            return;
        }

        // 必须在主线程显示对话框
        activity.runOnUiThread(new Runnable() {
            public void run() {
                // 创建垂直布局
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 30, 50, 10);

                // 文件名标签和输入框
                TextView label1 = new TextView(context);
                label1.setText("文件名:");
                label1.setPadding(0, 20, 0, 5);

                final EditText fileNameEdit = new EditText(context);
                fileNameEdit.setHint("文件名");
                fileNameEdit.setText(defaultFileName);
                fileNameEdit.setSingleLine(true);

                // 路径标签和输入框
                TextView label2 = new TextView(context);
                label2.setText("保存路径:");
                label2.setPadding(0, 20, 0, 5);

                final EditText pathEdit = new EditText(context);
                pathEdit.setHint("保存路径");
                pathEdit.setText(defaultPath);
                pathEdit.setSingleLine(true);

                // 添加到布局
                layout.addView(label1);
                layout.addView(fileNameEdit);
                layout.addView(label2);
                layout.addView(pathEdit);

                // 创建并显示对话框 (使用 Material 风格)
                new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                        .setTitle("保存WAV文件")
                        .setView(layout)
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String fileName = fileNameEdit.getText().toString().trim();
                                String savePath = pathEdit.getText().toString().trim();

                                // 处理路径末尾的斜杠
                                if (savePath.endsWith("/") || savePath.endsWith("\\")) {
                                    savePath = savePath.substring(0, savePath.length() - 1);
                                }

                                // 确保文件名有 .wav 后缀
                                if (!fileName.toLowerCase().endsWith(".wav")) {
                                    fileName = fileName + ".wav";
                                }

                                String fullPath = savePath + "/" + fileName;
                                doSaveWav(msg, fullPath);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
    }

    private void loadSo(String groupUin, String uin, int chatType) {
        try {
            Class loadClass = loader.loadClass("com.tencent.liteav.base.util.SoLoader");
            String soPath = "/data/user/0/com.tencent.mobileqq/app_libs/";
            String soName = "decoder";
            
            // 先设置库路径
            Method setLibraryPath = loadClass.getMethod("setLibraryPath", String.class);
            setLibraryPath.invoke(null, soPath);
            log("setLibraryPath: " + soPath);
            
            // 再调用 loadLibrary(String) 静态方法
            Method loadLibraryMethod = loadClass.getMethod("loadLibrary", String.class);
            boolean result = (boolean) loadLibraryMethod.invoke(null, soName);
            log("loadLibrary result: " + result);
            
            if (result) {
                // 使用 DexClassLoader 加载包含 DecodeNative 类的 DEX 文件
                String dexPath = appPath + "decoder.dex";
                String optimizedDir = context.getDir("dex", 0).getAbsolutePath();
                
                dalvik.system.DexClassLoader dexLoader = new dalvik.system.DexClassLoader(
                        dexPath, optimizedDir, null, loader);
                
                // 加载 DecodeNative 类并调用 silk2mp3 方法
                Class<?> decodeNativeClass = dexLoader.loadClass("xyz.xxin.silkdecoder.DecodeNative");
                Method silk2mp3Method = decodeNativeClass.getMethod("silk2mp3", String.class, String.class);
                
                String inputPcmPath = appPath + "23e84ac26c0297cd0e687b23bdf8bc73.pcm";
                String outputMp3Path = appPath + "1.mp3";
                
                int decodeResult = (int) silk2mp3Method.invoke(null, inputPcmPath, outputMp3Path);
                log("silk2mp3 decode result: " + decodeResult);
                
                if (decodeResult == 0) {
                    toast("已保存到: " + outputMp3Path);
                    log("MP3 转换成功");
                } else {
                    toast("MP3 转换失败，错误码: " + decodeResult);
                    log("MP3 转换失败，错误码: " + decodeResult);
                }
            } else {
                toast("加载 decoder 库失败");
                log("加载 decoder 库失败");
            }
        } catch (ClassNotFoundException e) {
            error(e);
            log("类加载失败: " + e.getMessage());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            error(e);
            log("方法调用失败: " + e.getMessage());
        } catch (Exception e) {
            error(e);
            log("loadSo 异常: " + e.getMessage());
        }
    }

    // 实际执行WAV保存
    private void doSaveWav(MessageData msg, String wavPath) {

        String pcmPath = null;
        try {
            pcmPath = audioDecoder.decodeVoiceMessage(msg.LocalPath);
            if (pcmPath == null) {
                toast("解码失败");
                return;
            }

            boolean success = wavConverter.convertPcmToWav(
                    pcmPath,
                    wavPath,
                    AudioDecoderState.lastSampleRate,
                    AudioDecoderState.lastChannels,
                    AudioDecoderState.lastBitDepth
            );

            if (success) {
                toast("已保存到: " + wavPath);
            } else {
                toast("保存失败");
            }
        } catch (Exception e) {
            error(e);
            toast("保存失败: " + e.getMessage());
        } finally {
            if (pcmPath != null) {
                new File(pcmPath).delete();
            }
        }
    }



    private void onShellStatueClick(String groupUin, String uin, int chatType) {
        if (getString("脚本启用状态", "开关").equals("关")) {
            putString("脚本启用状态", "开关", "开");
            Statue.SHELL_OPEN = true;
            toast("已全局启用功能");
        } else if (getString("脚本启用状态", "开关").equals("开")) {
            putString("脚本启用状态", "开关", "关");
            Statue.SHELL_OPEN = false;
            toast("已全局关闭功能");
        }
    }

    public void alertWebDialog(String groupUin, String uin, int chatType) {
        try {
            // 使用 appPath 获取脚本目录
            String filePath = appPath + "/web/test.html";
            java.io.File file = new java.io.File(filePath);
            
            if (!file.exists()) {
                toast("文件不存在: " + filePath);
                return;
            }
            
            // 读取文件内容
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String htmlContent = new String(data, "UTF-8");
            
            dialog.showWebDialogWithHtml("BeanShell 测试页面", htmlContent);
        } catch (Exception e) {
            error(e);
            toast("加载页面失败: " + e.getMessage());
        }
    }
}

// Global initialization code - will be placed at root level
@RootCode
class Init extends QScriptBase {
    // All method bodies in this class will be extracted to root level
    public void init() {
        if (getString("脚本启用状态", "开关") == null) {
            putString("脚本启用状态", "开关", "关");
            Statue.SHELL_OPEN = false;
        }
        if (getString("脚本启用状态", "开关").equals("开")) {
            Statue.SHELL_OPEN = true;
        }
        addItem("全局启停转换功能", "onShellStatueClick");
        addItem("弹出web弹窗", "alertWebDialog");
        addItem("加载so文件", "loadSo");
    }
}