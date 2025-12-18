package me.mm.qs.script.types;

import me.mm.qs.script.annotation.BeanShellType;

/**
 * Group information structure with IDE hints.
 * This will be converted to Object in BeanShell.
 */
@BeanShellType
public class GroupInfo {
    /** 群号 */
    public String GroupUin;
    
    /** 群名 */
    public String GroupName;
    
    /** 群主账号 */
    public String GroupOwner;
    
    /** 管理员列表 包括群主,不一定总是最新,通常30分钟刷新一次 */
    public String[] AdminList;
    
    /** 我在此群是否是群主或者管理 */
    public boolean IsOwnerOrAdmin;
    
    /** 原对象 对应com.tencent.mobileqq.data.troop.TroopMemberInfo */
    public Object sourceInfo;
}
