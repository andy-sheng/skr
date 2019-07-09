package com.common.notification.event;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;

/**
 * 通过在唱聊房内邀请之后对方同意之后收到的push
 */
public class CRStartByCreateNotifyEvent extends BaseEnterRoomEvent {
    private long inviterId = MyUserInfoManager.getInstance().getUid();

    public long getInviterId() {
        return inviterId;
    }

    public CRStartByCreateNotifyEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        super(basePushInfo, combineRoomEnterMsg);
    }
}
