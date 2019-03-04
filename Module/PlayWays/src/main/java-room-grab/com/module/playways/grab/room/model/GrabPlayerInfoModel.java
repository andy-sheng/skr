package com.module.playways.grab.room.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.zq.live.proto.Room.OnlineInfo;
import com.zq.live.proto.Room.QJoinNoticeMsg;

public class GrabPlayerInfoModel extends PlayerInfoModel {
    public static final int ROLE_PLAY = 1;//在玩的
    public static final int ROLE_WAIT = 2;//等待的
    protected int role;

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "userInfo=" + (userInfo==null?"":userInfo.toSimpleString()) +
                ", isSkrer=" + isSkrer +
                ", role=" + role +
                '}';
    }

    public static GrabPlayerInfoModel parse(OnlineInfo info) {
        GrabPlayerInfoModel grabPlayerInfoModel = new GrabPlayerInfoModel();
        grabPlayerInfoModel.setRole(info.getRole().getValue());
        grabPlayerInfoModel.setOnline(info.getIsOnline());
        grabPlayerInfoModel.setSkrer(info.getIsSkrer());
        grabPlayerInfoModel.setUserID(info.getUserID());
        grabPlayerInfoModel.setUserInfo(UserInfoModel.parseFromPB(info.getUserInfo()));
        return grabPlayerInfoModel;
    }

    public static GrabPlayerInfoModel parse(QJoinNoticeMsg msg) {
        GrabPlayerInfoModel grabPlayerInfoModel = new GrabPlayerInfoModel();
        grabPlayerInfoModel.setRole(msg.getRole().getValue());
        grabPlayerInfoModel.setOnline(true);
        grabPlayerInfoModel.setSkrer(false);
        grabPlayerInfoModel.setUserID(msg.getUser().getUserID());
        grabPlayerInfoModel.setUserInfo(UserInfoModel.parseFromPB(msg.getUser()));
        return grabPlayerInfoModel;
    }
}
