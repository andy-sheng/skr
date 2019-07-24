package com.common.notification.event;

import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;

public class CRStartByMatchPushEvent {
    BaseNotiInfo basePushInfo;
    CombineRoomEnterMsg combineRoomEnterMsg;

    public BaseNotiInfo getBasePushInfo() {
        return basePushInfo;
    }

    public CombineRoomEnterMsg getCombineRoomEnterMsg() {
        return combineRoomEnterMsg;
    }

    public CRStartByMatchPushEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        this.basePushInfo = basePushInfo;
        this.combineRoomEnterMsg = combineRoomEnterMsg;
    }
}
