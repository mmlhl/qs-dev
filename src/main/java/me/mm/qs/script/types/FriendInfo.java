package me.mm.qs.script.types;

import me.mm.qs.script.annotation.BeanShellType;

/**
 * Friend information structure with IDE hints.
 * This will be converted to Object in BeanShell.
 */
@BeanShellType
public class FriendInfo {
    /** QQ号 */
    public String uin;
    
    /** QQ昵称 */
    public String name;
    
    /** 备注 */
    public String remark;
    
    /** 是否会员 */
    public boolean isVip = false;
    
    /** 会员等级 */
    public int vipLevel = 0;
}
