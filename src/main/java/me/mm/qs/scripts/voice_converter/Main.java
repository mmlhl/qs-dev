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
import java.net.URI;

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
    private final WebDialog dialog = new WebDialog();

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
            if (Statue.SHELL_OPEN) {
                addMenuItem("WAV", "saveVoiceAsWav");
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
        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>测试</title><style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);min-height:100vh;display:flex;align-items:center;justify-content:center;padding:20px;margin:0}.container{background:white;border-radius:12px;padding:30px;max-width:500px;width:100%;box-shadow:0 20px 60px rgba(0,0,0,.3)}h1{color:#333;margin-bottom:20px;font-size:24px}.section{margin-bottom:25px;padding-bottom:20px;border-bottom:1px solid #eee}.section:last-child{border-bottom:none}label{display:block;color:#555;margin-bottom:8px;font-weight:500}input[type=text]{width:100%;padding:10px;border:2px solid #ddd;border-radius:6px;font-size:14px}button{width:100%;padding:12px;margin-top:10px;border:none;border-radius:6px;font-size:14px;font-weight:600;cursor:pointer}.btn-primary{background:#667eea;color:white}.btn-success{background:#48bb78;color:white}.btn-danger{background:#f56565;color:white}.output{background:#f7f7f7;border:1px solid #ddd;border-radius:6px;padding:15px;margin-top:15px;max-height:200px;overflow-y:auto;font-family:'Courier New',monospace;font-size:12px;color:#333;white-space:pre-wrap;word-break:break-all}.info{background:#e6f3ff;border-left:4px solid #667eea;padding:12px;border-radius:4px;font-size:12px;color:#333;margin-bottom:20px}</style></head><body><div class='container'><h1>BeanShell 测试</h1><div class='info'>这个页面可以通过 JavaScript 执行 BeanShell 代码</div><div class='section'><label>文件路径：</label><input type='text' id='filePath' value='/storage/emulated/0/Download'><button class='btn-danger' onclick='deleteFile()'>删除文件</button></div><div class='section'><label>Toast消息：</label><input type='text' id='toastMsg' value='Hello!'><button class='btn-primary' onclick='showToast()'>显示Toast</button></div><div class='section'><label>代码：</label><input type='text' id='testCode' value='log(\"test\")'><button class='btn-success' onclick='executeCode()'>执行代码</button></div><div class='output' id='output'>准备就绪</div></div><script>function addOutput(text){var output=document.getElementById('output');var timestamp=new Date().toLocaleTimeString();output.textContent+='['+timestamp+'] '+text+'\\n';output.scrollTop=output.scrollHeight}function clearOutput(){document.getElementById('output').textContent=''}function deleteFile(){var filePath=document.getElementById('filePath').value.trim();if(!filePath){addOutput('错误：请输入路径');return}clearOutput();addOutput('删除文件: '+filePath);var code='try{new java.io.File(\"'+filePath+'\").delete();log(\"删除成功\");}catch(e){log(\"失败: \"+e);}';executeInBsh(code)}function showToast(){var msg=document.getElementById('toastMsg').value.trim();if(!msg){addOutput('错误：请输入消息');return}clearOutput();addOutput('显示: '+msg);executeInBsh('toast(\"'+msg+'\");')}function executeCode(){var code=document.getElementById('testCode').value.trim();if(!code){addOutput('错误：请输入代码');return}clearOutput();addOutput('执行: '+code);executeInBsh(code)}function executeInBsh(code){try{window.location='bsh://run?'+encodeURIComponent(code);addOutput('执行完成')}catch(e){addOutput('错误: '+e)}}</script></body></html>";
        dialog.showWebDialogWithHtml("BeanShell 测试页面", html);
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
    }
}