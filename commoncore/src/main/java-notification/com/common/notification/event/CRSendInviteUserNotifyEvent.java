package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomInviteMsg;

public class CRSendInviteUserNotifyEvent {
    public BaseNotiInfo mBaseNotiInfo;
    public UserInfoModel mUserInfoModel;
    public String msg;

    public CRSendInviteUserNotifyEvent(BaseNotiInfo baseNotiInfo, CombineRoomInviteMsg combineRoomInviteMsg) {
        mBaseNotiInfo = baseNotiInfo;
        mUserInfoModel = UserInfoModel.parseFromPB(combineRoomInviteMsg.getUser());
        msg = combineRoomInviteMsg.getInviteMsg();
    }

    public BaseNotiInfo getBaseNotiInfo() {
        return mBaseNotiInfo;
    }

    public UserInfoModel getUserInfoModel() {
        return mUserInfoModel;
    }
}
