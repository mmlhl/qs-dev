package me.mm.qs.myscript.constants;

import me.mm.qs.script.annotation.GlobalInstance;

/**
 * 消息类型常量
 * 此类会在转换时自动输出到 constants 目录,可在运行时通过 load() 加载
 */
@GlobalInstance
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

}
