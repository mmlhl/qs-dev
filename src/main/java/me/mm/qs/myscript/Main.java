package me.mm.qs.myscript;

import me.mm.qs.myscript.constants.MessageType;
import me.mm.qs.script.QScriptBase;
import me.mm.qs.script.annotation.RootCode;
import me.mm.qs.script.annotation.ScriptMethods;
import me.mm.qs.script.annotation.ScriptInfo;
import me.mm.qs.script.types.MessageData;
import me.mm.qs.myscript.utils.MessageHandler;
import me.mm.qs.myscript.utils.Helper;
import me.mm.qs.myscript.utils.SilkAudioDecoder;
import me.mm.qs.myscript.utils.PcmToWavConverter;

import static me.mm.qs.script.Globals.*;

/**
 * Main script entry point.
 * This is the entry file that will be converted to main.java.
 */
@ScriptInfo(
    name = "示例脚本",
    author = "Your Name",
    version = "1.0",
    description = "这是一个示例脚本，演示如何使用 QStory BeanShell 脚本开发框架。\n" +
                  "支持的功能：\n" +
                  "- 菜单指令\n" +
                  "- @提醒\n" +
                  "- 回复消息\n" +
                  "- 私聊功能\n" +
                  "\n发送 '菜单' 查看使用说明",
    tags = "群聊辅助,娱乐功能"
)
@ScriptMethods
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
        
        if ("菜单".equals(text) && qq.equals(myUin)) {
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
        /*
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
        }
        */
    }

    @Override
    public void onCreateMenu(MessageData msg) {
        MessageType type = new MessageType();
        if (msg.IsGroup) {
            addMenuItem("仅群", "showGroup");
        }
        // 为语音消息添加解码菜单
        if (msg.MessageType == type.VOICE) {
            addMenuItem("解码为PCM", "saveVoice");
        }
    }

    // Custom menu callback
    public void showGroup(MessageData msg) {
        toast("提示在" + msg.MessageType);
    }

    // Custom menu callback - 解码语音为PCM
    public void saveVoice(MessageData msg) {
        MessageType type = new MessageType();
        if (msg.MessageType == type.VOICE) {
            String pcmPath = audioDecoder.decodeVoiceMessage(msg.LocalPath);
        } else {
            toast("这不是语音消息");
        }
    }


    // Floating window menu callback - parameters: groupUin, uin, chatType
    public void 加载提示(String groupUin, String uin, int chatType) {
        if (getString("加载提示", "开关") == null) {
            putString("加载提示", "开关", "关");
            toast("已关闭加载提示");
        } else {
            putString("加载提示", "开关", null);
            toast("已开启加载提示");
        }
    }
}

// Global initialization code - will be placed at root level
@ScriptMethods
class Init extends QScriptBase {
    // This code will run at the root level when the script loads
    @RootCode
    public void init() {
        addItem("开关加载提示", "加载提示");
        if (getString("加载提示", "开关") == null) {
            toast("发送 \"菜单\" 查看使用说明");
        }
    }
}