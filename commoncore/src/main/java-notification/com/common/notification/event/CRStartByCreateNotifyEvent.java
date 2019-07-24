package com.common.notification.event;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.notification.BaseNotiInfo;
import com.component.live.proto.Notification.CombineRoomEnterMsg;

/**
 * 通过在唱聊房内邀请之后对方同意之后收到的push
 */
public class CRStartByCreateNotifyEvent {
    private long inviterId = MyUserInfoManager.getInstance().getUid();
    BaseNotiInfo basePushInfo;
    CombineRoomEnterMsg combineRoomEnterMsg;

    public BaseNotiInfo getBasePushInfo() {
        return basePushInfo;
    }

    public CombineRoomEnterMsg getCombineRoomEnterMsg() {
        return combineRoomEnterMsg;
    }

    public long getInviterId() {
        return inviterId;
    }

    public CRStartByCreateNotifyEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        this.basePushInfo = basePushInfo;
        this.combineRoomEnterMsg = combineRoomEnterMsg;
    }
}
