package me.mm.qs.script;

import android.widget.TextView;
import me.mm.qs.script.annotation.RootCode;
import me.mm.qs.script.annotation.ScriptMethods;
import me.mm.qs.script.types.MessageData;

/**
 * Example script implementation with full IDE hint support.
 * Use @ScriptMethods to mark classes whose methods will be extracted to root level.
 */
@ScriptMethods
public class MyScript extends QScriptBase {

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
    }

    @Override
    public void onCreateMenu(MessageData msg) {
        if (msg.IsGroup) {
            addMenuItem("仅群", "showGroup");
        }
    }

    // Custom menu callback
    public void showGroup(MessageData msg) {
        toast("提示在" + msg.GroupUin);
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
class GlobalInit extends QScriptBase {
    // This code will run at the root level when the script loads
    @RootCode
    public void init() {
        addItem("开关加载提示", "加载提示");
        if (getString("加载提示", "开关") == null) {
            toast("发送 \"菜单\" 查看使用说明");
        }
    }
}