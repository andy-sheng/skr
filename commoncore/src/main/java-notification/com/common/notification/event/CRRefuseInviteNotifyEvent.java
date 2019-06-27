package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomRefuseMsg;

public class CRRefuseInviteNotifyEvent {

    public UserInfoModel mUserInfoModel;
    public BaseNotiInfo mBaseNotiInfo;
    public String refuseMsg;

    public CRRefuseInviteNotifyEvent(BaseNotiInfo info, CombineRoomRefuseMsg msg) {
        this.mBaseNotiInfo = info;
        this.mUserInfoModel = UserInfoModel.parseFromPB(msg.getUser());
        this.refuseMsg = msg.getRefuseMsg();
    }
}
