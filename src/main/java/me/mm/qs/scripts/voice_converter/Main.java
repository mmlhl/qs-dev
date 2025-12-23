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
import me.mm.qs.scripts.voice_converter.utils.AudioDecoderState;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import static me.mm.qs.script.Globals.*;

/**
 * Main script entry point.
 * This is the entry file that will be converted to main.java.
 */
@ScriptInfo(
    name = "语音转换",
    author = "木木",
    version = "1.0",
    description ="111",
    tags = "功能拓展"
)
public class Main extends QScriptBase {
    // Utility instances - will be removed in BeanShell output
    private final MessageHandler messageHandler = new MessageHandler();
    private final Helper helper = new Helper();
    private final SilkAudioDecoder audioDecoder = new SilkAudioDecoder();
    private final PcmToWavConverter wavConverter = new PcmToWavConverter();

    // Callback methods - will be extracted to root level in BeanShell
    @Override
    public void onMsg(MessageData msg) {
        // Now you have full IDE hints!
        String text = msg.MessageContent;
        String qq = msg.UserUin;
        String qun = msg.GroupUin;
        boolean isGroup = msg.IsGroup;
        
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
            addMenuItem("WAV", "saveVoiceAsWav");
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
}

// Global initialization code - will be placed at root level
@RootCode
class Init extends QScriptBase {
    // All method bodies in this class will be extracted to root level
    public void init() {

    }
}