package com.common.notification.event;

import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;

public class CRSyncInviteUserNotifyEvent extends BaseEnterRoomEvent {
    public CRSyncInviteUserNotifyEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        super(basePushInfo, combineRoomEnterMsg);
    }
}
