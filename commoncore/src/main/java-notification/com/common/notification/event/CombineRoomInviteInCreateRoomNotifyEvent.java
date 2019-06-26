package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomInviteInCreateRoomMsg;

public class CombineRoomInviteInCreateRoomNotifyEvent {
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

    public CombineRoomInviteInCreateRoomNotifyEvent(BaseNotiInfo baseNotiInfo, CombineRoomInviteInCreateRoomMsg combineRoomInviteInCreateRoomMsg) {
        mBaseNotiInfo = baseNotiInfo;
        user = UserInfoModel.parseFromPB(combineRoomInviteInCreateRoomMsg.getUser());
        inviteMsg = combineRoomInviteInCreateRoomMsg.getInviteMsg();
        roomID = combineRoomInviteInCreateRoomMsg.getRoomID();
    }
}
