package com.common.notification.event;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.notification.BaseNotiInfo;
import com.component.live.proto.Notification.CombineRoomEnterMsg;

public class CRSyncInviteUserNotifyEvent {
    private long inviterId = MyUserInfoManager.getInstance().getUid();

    public long getInviterId() {
        return inviterId;
    }

    BaseNotiInfo basePushInfo;

    CombineRoomEnterMsg combineRoomEnterMsg;

    public BaseNotiInfo getBasePushInfo() {
        return basePushInfo;
    }

    public CombineRoomEnterMsg getCombineRoomEnterMsg() {
        return combineRoomEnterMsg;
    }

    public CRSyncInviteUserNotifyEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        this.basePushInfo = basePushInfo;
        this.combineRoomEnterMsg = combineRoomEnterMsg;
    }
}
