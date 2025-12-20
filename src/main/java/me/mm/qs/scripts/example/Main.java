package me.mm.qs.scripts.example;

import me.mm.qs.script.QScriptBase;
import me.mm.qs.script.annotation.RootCode;
import me.mm.qs.script.annotation.ScriptInfo;
import me.mm.qs.script.types.MessageData;

import static me.mm.qs.script.Globals.*;

/**
 * 示例脚本模板
 * 复制此目录创建新脚本
 */
@ScriptInfo(
    name = "示例脚本",
    author = "开发者",
    version = "1.0.0",
    description = "这是一个示例脚本模板",
    tags = "示例,模板"
)
public class Main extends QScriptBase {
    
    /**
     * 消息回调 - 收到消息时触发
     */
    @Override
    public void onMsg(MessageData msg) {
        String text = msg.MessageContent;
        String qq = msg.UserUin;
        String qun = msg.GroupUin;
        boolean isGroup = msg.IsGroup;
        
        // 示例：响应"菜单"命令
        if ("菜单".equals(text) && qq.equals(myUin)) {
            String reply = "这是示例脚本\n发送 '帮助' 查看更多";
            if (isGroup) {
                sendMsg(qun, "", reply);
            } else {
                sendMsg("", qq, reply);
            }
        }
        
        // 示例：响应"帮助"命令
        if ("帮助".equals(text)) {
            toast("收到帮助请求");
        }
    }
    
    /**
     * 菜单创建回调 - 长按消息时触发
     */
    @Override
    public void onCreateMenu(MessageData msg) {
        // 示例：为所有消息添加菜单项
        addMenuItem("示例操作", "exampleAction");
    }
    
    /**
     * 自定义菜单回调
     */
    public void exampleAction(MessageData msg) {
        toast("执行了示例操作");
    }
    
    /**
     * 悬浮窗菜单回调 - 参数: groupUin, uin, chatType
     */
    public void 示例功能开关(String groupUin, String uin, int chatType) {
        if (getString("示例功能", "开关") == null) {
            putString("示例功能", "开关", "关");
            toast("已关闭示例功能");
        } else {
            putString("示例功能", "开关", null);
            toast("已开启示例功能");
        }
    }
}

/**
 * 初始化代码 - 脚本加载时执行
 */
@RootCode
class Init extends QScriptBase {
    public void init() {
        // 添加悬浮窗菜单项
        addItem("示例功能开关", "示例功能开关");
        
        // 加载提示
        if (getString("示例功能", "开关") == null) {
            toast("示例脚本已加载");
        }
    }
}
