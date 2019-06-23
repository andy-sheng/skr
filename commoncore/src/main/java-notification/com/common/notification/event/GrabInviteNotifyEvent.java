package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.InviteStandMsg;

// 一唱到底邀请
public class GrabInviteNotifyEvent {
    public BaseNotiInfo mBaseNotiInfo;

    public UserInfoModel mUserInfoModel;
    public int roomID;
    public int mediaType;


    public GrabInviteNotifyEvent(BaseNotiInfo baseNotiInfo, InviteStandMsg inviteStandMsg) {
        this.mBaseNotiInfo = baseNotiInfo;
        this.roomID = inviteStandMsg.getRoomID();
        this.mUserInfoModel = UserInfoModel.parseFromPB(inviteStandMsg.getUser());
        this.mediaType = inviteStandMsg.getMediaType().getValue();
    }
}
