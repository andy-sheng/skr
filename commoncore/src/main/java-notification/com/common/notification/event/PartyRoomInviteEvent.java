package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.InviteMicMsg;

public class PartyRoomInviteEvent {
    public UserInfoModel userInfoModel;
    public int roomID;

}
