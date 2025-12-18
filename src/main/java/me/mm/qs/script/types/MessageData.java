package me.mm.qs.script.types;

import me.mm.qs.script.annotation.BeanShellType;

import java.util.ArrayList;

/**
 * Message data structure with IDE hints.
 * This will be converted to Object in BeanShell.
 */
@BeanShellType
public class MessageData {
    /** 消息内容（文本、图片下载地址、语音MD5、卡片代码） */
    public String MessageContent;
    
    /** 群号（仅在群消息、私聊消息和频道消息时有效） */
    public String GroupUin;
    
    /** 在私聊可以使用此参数 总是为对方的QQ号 */
    public String PeerUin;
    
    /** 发送者QQ号 */
    public String UserUin;
    
    /** 消息类型（1: 文字/图片；2: 卡片；3: 图文；4: 语音；5: 文件；6: 回复） */
    public int MessageType;
    
    /** 是否群组消息（仅在群聊消息和频道消息时为 true） */
    public boolean IsGroup;
    
    /** 是否频道消息（仅在频道消息时为 true） */
    public boolean IsChannel;
    
    /** 发送者昵称 */
    public String SenderNickName;
    
    /** 消息时间戳（单位：毫秒） */
    public long MessageTime;
    
    /** 艾特列表 */
    public ArrayList<String> mAtList;
    
    /** 是否为自己发送的消息 */
    public boolean IsSend;
    
    /** 文件名（仅在群文件消息时有效） */
    public String FileName;
    
    /** 文件大小（仅在群文件消息时有效） */
    public long FileSize;
    
    /** 本地文件路径（仅在语音文件消息时有效） */
    public String LocalPath;
    
    /** 回复的用户账号（仅在回复消息时有效） */
    public String ReplyTo;
    
    /** 回复的消息 */
    public MessageData RecordMsg;
    
    /** 频道ID（仅在频道消息时有效） */
    public String GuildID;
    
    /** 子频道ID（仅在频道消息时有效） */
    public String ChannelID;
    
    /** 消息中存在的图片MD5列表 */
    public String[] PicList;
    
    /** 消息中存在的图片链接列表 */
    public ArrayList<String> PicUrlList;
    
    /** 未经过解析的原消息 */
    public Object msg;
}
