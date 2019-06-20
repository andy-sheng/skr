package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomSendInviteUserMsg;

public class CombineRoomSendInviteUserNotifyEvent {
    public BaseNotiInfo mBaseNotiInfo;
    public UserInfoModel mUserInfoModel;

    public CombineRoomSendInviteUserNotifyEvent(BaseNotiInfo baseNotiInfo, CombineRoomSendInviteUserMsg combineRoomSendInviteUserMsg) {
        mBaseNotiInfo = baseNotiInfo;
        mUserInfoModel = UserInfoModel.parseFromPB(combineRoomSendInviteUserMsg.getUser());
    }

    public BaseNotiInfo getBaseNotiInfo() {
        return mBaseNotiInfo;
    }

    public UserInfoModel getUserInfoModel() {
        return mUserInfoModel;
    }
}
