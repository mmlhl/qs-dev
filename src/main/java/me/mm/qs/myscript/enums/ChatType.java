package me.mm.qs.myscript.enums;

/**
 * 聊天类型枚举常量
 * 测试自动转换和加载功能
 */
public class ChatType {
    /** 私聊 */
    public static final int PRIVATE = 1;
    
    /** 群聊 */
    public static final int GROUP = 2;
    
    /** 临时会话 */
    public static final int TEMP = 100;
    
    // 防止实例化
    private ChatType() {}
}
