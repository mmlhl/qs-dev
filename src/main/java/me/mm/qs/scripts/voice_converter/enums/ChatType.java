package me.mm.qs.scripts.voice_converter.enums;

/**
 * 聊天类型枚举常量
 * 测试自动转换和加载功能
 */

import me.mm.qs.script.annotation.GlobalInstance;

@GlobalInstance
public class ChatType {
    /** 私聊 */
    public static final int PRIVATE = 1;
    
    /** 群聊 */
    public static final int GROUP = 2;
    
    /** 临时会话 */
    public static final int TEMP = 100;

}
