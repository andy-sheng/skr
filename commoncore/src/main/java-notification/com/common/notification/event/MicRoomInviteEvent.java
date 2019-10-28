package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.InviteMicMsg;

public class MicRoomInviteEvent {
    public BaseNotiInfo mBaseNotiInfo;
    public InviteMicMsg mInviteMicMsg;
    public UserInfoModel mUserInfoModel;

    public MicRoomInviteEvent(BaseNotiInfo baseNotiInfo, InviteMicMsg inviteMicMsg) {
        mBaseNotiInfo = baseNotiInfo;
        mInviteMicMsg = inviteMicMsg;
        mUserInfoModel = UserInfoModel.parseFromPB(inviteMicMsg.getUser());
    }

    public BaseNotiInfo getBaseNotiInfo() {
        return mBaseNotiInfo;
    }

    public InviteMicMsg getInviteMicMsg() {
        return mInviteMicMsg;
    }

    public UserInfoModel getUserInfoModel() {
        return mUserInfoModel;
    }
}
