package me.mm.qs.script.types;

import me.mm.qs.script.annotation.BeanShellType;

/**
 * Forbidden member information structure with IDE hints.
 * This will be converted to Object in BeanShell.
 */
@BeanShellType
public class ForbiddenInfo {
    /** 成员号码 */
    public String UserUin;
    
    /** 成员名字 */
    public String UserName;
    
    /** 禁言结束的时间戳 */
    public long Endtime;
}
