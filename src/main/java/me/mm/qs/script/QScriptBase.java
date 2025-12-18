package me.mm.qs.script;

import android.content.Context;
import me.mm.qs.script.types.*;
import static me.mm.qs.script.Globals.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * QStory 脚本基类,用于 IDE 代码提示支持。
 * 此类定义了 QStory BeanShell 环境中所有可用的方法。
 * 全局变量现在在 Globals.java 中定义。
 * 实际的脚本逻辑应该写在继承此基类的类中。
 */
public abstract class QScriptBase {

    // 全局变量现在在 Globals.java 中 - 通过静态导入引入
    // 可以直接访问: myUin, context, appPath, loader, pluginID

    // --- 回调方法 (在脚本中重写) ---

    /**
     * 当收到消息时调用。
     * @param msg 消息数据对象
     */
    public void onMsg(MessageData msg) {}

    /**
     * 当成员被禁言时调用。
     * @param GroupUin 群号
     * @param UserUin  被禁言用户的QQ号
     * @param OPUin    执行禁言的管理员QQ号
     * @param time     禁言时长(秒)
     */
    public void onForbiddenEvent(String GroupUin, String UserUin, String OPUin, long time) {}

    /**
     * 当成员加入或退出群时调用。
     * @param GroupUin 群号
     * @param UserUin  用户QQ号
     * @param type     2表示加入, 1表示退出
     */
    public void onTroopEvent(String GroupUin, String UserUin, int type) {}

    /**
     * 当脚本悬浮窗被打开时调用。
     * @param type 聊天类型(1:私聊, 2:群聊)
     * @param uin  好友QQ号或群号
     */
    public void onClickFloatingWindow(int type, String uin) {}

    /**
     * 当消息即将发送时调用。
     * @param msg           要发送的消息内容
     * @param GroupUinOrFriendUin 群号或好友QQ号
     * @param type          聊天类型(2:群聊, 1或100:私聊)
     * @return 修改后的消息内容
     */
    public String getMsg(String msg, String GroupUinOrFriendUin, int type) { return msg; }

    /**
     * 当为消息创建长按菜单时调用。
     * @param msg 消息数据对象
     */
    public void onCreateMenu(MessageData msg) {}

    /**
     * 当收到未解析的原始消息时调用。
     * @param msg 原始消息对象
     */
    public void callbackOnRawMsg(Object msg) {}

    // --- API 方法 ---

    // 发送消息
    /**
     * 发送文本、图片或图文消息。
     * 图文格式: [PicUrl=图片地址], @格式: [AtQQ=QQ号]
     * @param GroupUin 群号(私聊时为空)
     * @param UserUin  用户QQ号(群聊时为空)
     * @param msg      消息内容
     */
    public native void sendMsg(String GroupUin, String UserUin, String msg);

    /**
     * 发送单张图片。
     * @param GroupUin 群号(私聊时为空)
     * @param UserUin  用户QQ号(群聊时为空)
     * @param Path     图片本地或网络路径
     */
    public native void sendPic(String GroupUin, String UserUin, String Path);

    /**
     * 发送JSON卡片消息。
     * @param GroupUin 群号(私聊时为空)
     * @param UserUin  用户QQ号(群聊时为空)
     * @param card     卡片内容
     */
    public native void sendCard(String GroupUin, String UserUin, String card);

    /**
     * 发送回复消息(仅群聊)。
     * @param GroupUin 群号
     * @param msg      要回复的消息对象
     * @param replyMsg 回复文本
     */
    public native void sendReply(String GroupUin, Object msg, String replyMsg);

    /**
     * 发送文件。
     * @param GroupUin 群号(私聊时为空)
     * @param UserUin  用户QQ号(群聊时为空)
     * @param Path     文件路径
     */
    public native void sendFile(String GroupUin, String UserUin, String Path);

    /**
     * 发送语音消息。
     * @param GroupUin 群号(私聊时为空)
     * @param UserUin  用户QQ号(群聊时为空)
     * @param Path     语音文件路径
     */
    public native void sendVoice(String GroupUin, String UserUin, String Path);

    /**
     * 发送视频。
     * @param group   群号
     * @param userUin 用户QQ号
     * @param path    视频文件路径
     */
    public native void sendVideo(String group, String userUin, String path);

    /**
     * 给用户点赞。
     * @param UserUin 用户QQ号
     * @param count   点赞数量
     */
    public native void sendLike(String UserUin, int count);

    /**
     * 戳一戳用户。
     * @param group 群号(私聊时为空)
     * @param uin   用户QQ号
     */
    public native void sendPai(String group, String uin);

    /**
     * 发送表情回应消息。
     * @param msg     消息对象
     * @param emojiId 表情ID
     */
    public native void replyEmoji(Object msg, String emojiId);

    /**
     * 发送ProtoBuf消息(实验性功能)。
     * @param cmd      命令
     * @param jsonBody JSON内容
     */
    public native void sendProto(String cmd, String jsonBody);

    // 群管理操作
    /**
     * 设置群成员名片(仅管理员)。
     * @param GroupUin 群号
     * @param UserUin  成员QQ号
     * @param Name     新名片
     */
    public native void setCard(String GroupUin, String UserUin, String Name);

    /**
     * 设置群成员专属头衔(仅群主)。
     * @param GroupUin 群号
     * @param UserUin  成员QQ号
     * @param title    头衔
     */
    public native void setTitle(String GroupUin, String UserUin, String title);

    /**
     * 撤回消息。
     * @param msg 消息对象
     */
    public native void revokeMsg(Object msg);

    /**
     * 删除消息。
     * @param msg 消息对象
     */
    public native void deleteMsg(Object msg);

    /**
     * 禁言成员(仅管理员)。
     * @param GroupUin 群号
     * @param UserUin  成员QQ号(全体禁言时为空)
     * @param time     时长(秒)
     */
    public native void forbidden(String GroupUin, String UserUin, int time);

    /**
     * 踢出成员(仅管理员)。
     * @param GroupUin 群号
     * @param UserUin  成员QQ号
     * @param isBlack  是否拉黑
     */
    public native void kick(String GroupUin, String UserUin, boolean isBlack);

    // 信息获取
    /**
     * 获取群成员昵称。
     * @param group 群号
     * @param uin   成员QQ号
     * @return 成员昵称
     */
    public native String getMemberName(String group, String uin);

    /**
     * 获取群列表。
     * @return GroupInfo对象的ArrayList
     */
    public native ArrayList<GroupInfo> getGroupList();

    /**
     * 获取指定群的信息。
     * @param GroupUin 群号
     * @return GroupInfo对象
     */
    public native GroupInfo getGroupInfo(String GroupUin);

    /**
     * 获取群成员列表。
     * @param GroupUin 群号
     * @return GroupMemberInfo对象的ArrayList
     */
    public native ArrayList<GroupMemberInfo> getGroupMemberList(String GroupUin);

    /**
     * 获取指定群成员的信息。
     * @param group 群号
     * @param uin   成员QQ号
     * @return GroupMemberInfo对象
     */
    public native GroupMemberInfo getMemberInfo(String group, String uin);

    /**
     * 获取群禁言列表。
     * @param GroupUin 群号
     * @return ForbiddenInfo对象的ArrayList
     */
    public native ArrayList<ForbiddenInfo> getForbiddenList(String GroupUin);

    /**
     * 获取好友列表。
     * @return FriendInfo对象的ArrayList
     */
    public native ArrayList<FriendInfo> getFriendList();

    /**
     * 检查是否为好友。
     * @param uin 用户QQ号
     * @return 是好友返回true
     */
    public native boolean isFriend(String uin);

    // 数据存储
    /**
     * 存储字符串值。
     * @param ConfigName 配置名
     * @param key        键
     * @param value      值
     */
    public native void putString(String ConfigName, String key, String value);

    /**
     * 获取字符串值。
     * @param ConfigName 配置名
     * @param key        键
     * @return 值
     */
    public native String getString(String ConfigName, String key);

    /**
     * 获取字符串值(带默认值)。
     * @param ConfigName 配置名
     * @param key        键
     * @param def        默认值
     * @return 值或默认值
     */
    public native String getString(String ConfigName, String key, String def);

    /**
     * 存储整数值。
     * @param ConfigName 配置名
     * @param key        键
     * @param value      值
     */
    public native void putInt(String ConfigName, String key, int value);

    /**
     * 获取整数值(带默认值)。
     * @param ConfigName 配置名
     * @param key        键
     * @param def        默认值
     * @return 值或默认值
     */
    public native int getInt(String ConfigName, String key, int def);

    /**
     * 存储长整数值。
     * @param ConfigName 配置名
     * @param key        键
     * @param value      值
     */
    public native void putLong(String ConfigName, String key, long value);

    /**
     * 获取长整数值(带默认值)。
     * @param ConfigName 配置名
     * @param key        键
     * @param def        默认值
     * @return 值或默认值
     */
    public native long getLong(String ConfigName, String key, long def);

    /**
     * 存储布尔值。
     * @param ConfigName 配置名
     * @param key        键
     * @param value      值
     */
    public native void putBoolean(String ConfigName, String key, boolean value);

    /**
     * 获取布尔值(带默认值)。
     * @param ConfigName 配置名
     * @param key        键
     * @param def        默认值
     * @return 值或默认值
     */
    public native boolean getBoolean(String ConfigName, String key, boolean def);

    /**
     * 存储浮点数值。
     * @param ConfigName 配置名
     * @param key        键
     * @param value      值
     */
    public native void putFloat(String ConfigName, String key, float value);

    /**
     * 获取浮点数值(带默认值)。
     * @param ConfigName 配置名
     * @param key        键
     * @param def        默认值
     * @return 值或默认值
     */
    public native float getFloat(String ConfigName, String key, float def);

    /**
     * 存储双精度浮点数值。
     * @param ConfigName 配置名
     * @param key        键
     * @param value      值
     */
    public native void putDouble(String ConfigName, String key, double value);

    /**
     * 获取双精度浮点数值(带默认值)。
     * @param ConfigName 配置名
     * @param key        键
     * @param def        默认值
     * @return 值或默认值
     */
    public native double getDouble(String ConfigName, String key, double def);

    // Skey相关方法
    /**
     * 获取群组RKey。
     * @return RKey
     */
    public native String getGroupRKey();

    /**
     * 获取好友RKey。
     * @return RKey
     */
    public native String getFriendRKey();

    /**
     * 获取标准skey。
     * @return Skey
     */
    public native String getSkey();

    /**
     * 获取真实skey。
     * @return Skey
     */
    public native String getRealSkey();

    /**
     * 获取pskey。
     * @param url 网址
     * @return Pskey
     */
    public native String getPskey(String url);

    /**
     * 获取PT4Token。
     * @param str 输入字符串
     * @return PT4Token
     */
    public native String getPT4Token(String str);

    /**
     * 获取GTK。
     * @param str 输入字符串
     * @return GTK
     */
    public native String getGTK(String str);

    /**
     * 获取BKN。
     * @param pskey Pskey值
     * @return BKN
     */
    public native long getBKN(String pskey);

    // 其他方法
    /**
     * 获取当前顶层Activity。
     * @return Activity对象或null
     */
    public native android.app.Activity getActivity();

    /**
     * 显示Toast消息。
     * @param content 要显示的内容
     */
    public native void toast(Object content);

    /**
     * 加载另一个Java脚本文件。
     * @param Path 脚本文件路径
     */
    public native void load(String Path);

    /**
     * 加载JAR文件。
     * @param JarPath JAR文件路径
     */
    public native void loadJar(String JarPath);

    /**
     * 动态执行Java代码。
     * @param code Java代码字符串
     */
    public native void eval(String code);

    /**
     * 记录异常到脚本目录。
     * @param throwable 异常对象
     */
    public native void error(Throwable throwable);

    /**
     * 输出日志到脚本目录。
     * @param content 日志内容
     */
    public native void log(Object content);

    /**
     * 执行HTTP GET请求。
     * @param url 网址
     * @return 响应内容
     */
    public native String httpGet(String url);

    /**
     * 执行HTTP GET请求(带请求头)。
     * @param url     网址
     * @param headers 请求头
     * @return 响应内容
     */
    public native String httpGet(String url, Map<String, String> headers);

    /**
     * 执行HTTP POST请求(表单数据)。
     * @param url  网址
     * @param data 表单数据
     * @return 响应内容
     */
    public native String httpPost(String url, Map<String, String> data);

    /**
     * 执行HTTP POST请求(表单数据+请求头)。
     * @param url     网址
     * @param headers 请求头
     * @param data    表单数据
     * @return 响应内容
     */
    public native String httpPost(String url, Map<String, String> headers, Map<String, String> data);

    /**
     * 执行HTTP POST请求(JSON数据)。
     * @param url  网址
     * @param data JSON数据
     * @return 响应内容
     */
    public native String httpPostJson(String url, String data);

    /**
     * 执行HTTP POST请求(JSON数据+请求头)。
     * @param url     网址
     * @param headers 请求头
     * @param data    JSON数据
     * @return 响应内容
     */
    public native String httpPostJson(String url, Map<String, String> headers, String data);

    /**
     * 通过HTTP下载文件。
     * @param url  文件网址
     * @param path 保存路径(相对于脚本目录)
     */
    public native void httpDownload(String url, String path);

    /**
     * 通过HTTP下载文件(带请求头)。
     * @param url     文件网址
     * @param path    保存路径(相对于脚本目录)
     * @param headers 请求头
     */
    public native void httpDownload(String url, String path, Map<String, String> headers);

    // 文件操作
    /**
     * 从文件读取文本。
     * @param path 文件路径
     * @return 文件内容
     */
    public native String readFileText(String path);

    /**
     * 写入文本到文件(覆盖模式)。
     * @param path 文本文件路径
     * @param text 文本内容
     */
    public native void writeTextToFile(String path, String text);

    /**
     * 追加文本到文件。
     * @param path 文本文件路径
     * @param text 文本内容
     */
    public native void writeTextAppendToFile(String path, String text);

    /**
     * 从文件读取字节。
     * @param path 文件路径
     * @return 字节数组
     */
    public native byte[] readFileBytes(String path);

    /**
     * 写入字节到文件。
     * @param path  文件路径
     * @param bytes 字节数组
     */
    public native void writeBytesToFile(String path, byte[] bytes);

    // 悬浮窗菜单方法
    /**
     * 添加永久菜单项。
     * @param Name          显示名称
     * @param CallbackName  回调方法名
     * @return 菜单项ID
     */
    public native String addItem(String Name, String CallbackName);

    /**
     * 添加临时菜单项。
     * @param ItemName      显示名称
     * @param CallbackName  回调方法名
     */
    public native void addTemporaryItem(String ItemName, String CallbackName);

    /**
     * 移除菜单项。
     * @param ItemID 菜单项ID
     */
    public native void removeItem(String ItemID);

    // 长按消息菜单方法
    /**
     * 添加长按消息的菜单项。
     * @param Name          显示名称
     * @param CallbackName  回调方法名
     * @return 菜单项ID
     */
    public native String addMenuItem(String Name, String CallbackName);

    // 当前窗口方法
    /**
     * 获取当前聊天类型。
     * @return 1为私聊, 2为群聊
     */
    public native int getChatType();

    /**
     * 获取当前群号。
     * @return 群号或空字符串
     */
    public native String getCurrentGroupUin();

    /**
     * 获取当前好友的QQ号。
     * @return 好友QQ号或空字符串
     */
    public native String getCurrentFriendUin();
}
