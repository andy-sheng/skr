package com.common.notification.event;

import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;

public class CombineRoomSyncInviteUserNotifyEvent extends BaseEnterRoomEvent {
    public CombineRoomSyncInviteUserNotifyEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        super(basePushInfo, combineRoomEnterMsg);
    }
}
