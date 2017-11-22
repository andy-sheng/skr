package com.wali.live.watchsdk.fans.push.type;

/**
 * Created by zjn on 16-10-26.
 */
public class GroupNotifyType {
    public static final int APPLY_JOIN_GROUP_NOTIFY = 100;//申请加群
    public static final int AGREE_JOIN_GROUP_NOTIFY = 101;//同意加群
    public static final int REJECT_JOIN_GROUP_NOTIFY = 102;//拒绝加群
    public static final int BE_GROUP_MANAGER_NOTIFY = 103;//成为管理员
    public static final int CANCEL_GROUP_MANAGER_NOTIFY = 104;//取消管理员
    public static final int INVITE_JOIN_GROUP_NOTIFY = 105;//邀请入群   粉丝团没有
    public static final int BE_GROUP_MEM_NOTIFY = 106;//成为群成员
    public static final int REMOVE_GROUP_MEM_NOTIFY = 107;//被移出群
    public static final int FORBID_GROUP_MEM_NOTIFY = 108;//禁言
    public static final int CANCEL_FORBID_GROUP_MEM_NOTIFY = 109;//取消禁言
    public static final int GROUP_MEM_QUIT_GROUP_NOTIFY = 110;//退群通知
    public static final int GROUP_MEM_DISBAND_GROUP_NOTIFY = 111;//解散群通知  粉丝团没有
}
