package com.common.notification.event;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;

public class CRSyncInviteUserNotifyEvent extends BaseEnterRoomEvent {
    private long inviterId = MyUserInfoManager.getInstance().getUid();

    public long getInviterId() {
        return inviterId;
    }

    public CRSyncInviteUserNotifyEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        super(basePushInfo, combineRoomEnterMsg);
    }
}
