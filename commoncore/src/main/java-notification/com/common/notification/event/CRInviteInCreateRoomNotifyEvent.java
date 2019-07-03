package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomInviteV2Msg;

public class CRInviteInCreateRoomNotifyEvent {
    public BaseNotiInfo mBaseNotiInfo;
    private UserInfoModel user;
    private String inviteMsg;
    private Integer roomID;

    public UserInfoModel getUser() {
        return user;
    }

    public String getInviteMsg() {
        return inviteMsg;
    }

    public Integer getRoomID() {
        return roomID;
    }

    public CRInviteInCreateRoomNotifyEvent(BaseNotiInfo baseNotiInfo, CombineRoomInviteV2Msg combineRoomInviteMsg) {
        mBaseNotiInfo = baseNotiInfo;
        user = UserInfoModel.parseFromPB(combineRoomInviteMsg.getUser());
        inviteMsg = combineRoomInviteMsg.getInviteMsg();
        roomID = combineRoomInviteMsg.getRoomID();
    }
}
