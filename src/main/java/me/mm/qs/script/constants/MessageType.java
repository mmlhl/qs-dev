package me.mm.qs.script.constants;

/**
 * 消息类型常量
 * 此类会被单独转换为 BeanShell 脚本,可在运行时加载
 */
public class MessageType {
    /** 文字/图片消息 */
    public static final int TEXT_OR_IMAGE = 1;
    
    /** 卡片消息 */
    public static final int CARD = 2;
    
    /** 图文消息 */
    public static final int MIXED_MEDIA = 3;
    
    /** 语音消息 */
    public static final int VOICE = 4;
    
    /** 文件消息 */
    public static final int FILE = 5;
    
    /** 回复消息 */
    public static final int REPLY = 6;
    
    // 防止实例化
    private MessageType() {}
}
