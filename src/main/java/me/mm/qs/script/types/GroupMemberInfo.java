package me.mm.qs.script.types;

import me.mm.qs.script.annotation.BeanShellType;

/**
 * Group member information structure with IDE hints.
 * This will be converted to Object in BeanShell.
 */
@BeanShellType
public class GroupMemberInfo {
    /** 成员账号 */
    public String UserUin;
    
    /** 群内昵称 */
    public String NickName;
    
    /** 成员名字(好友备注) */
    public String UserName;
    
    /** 成员群聊等级 */
    public int UserLevel;
    
    /** 成员加群时间 */
    public long Join_Time;
    
    /** 最后发言时间，不一定能实时刷新 */
    public long Last_AvtivityTime;
    
    /** 原对象 对应com.tencent.mobileqq.data.troop.TroopInfo */
    public Object sourceInfo;
    
    /** 是否群主 */
    public boolean IsOwner;
    
    /** 是否管理 */
    public boolean IsAdmin;
}
