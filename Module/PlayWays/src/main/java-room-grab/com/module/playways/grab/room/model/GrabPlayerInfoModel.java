package com.module.playways.grab.room.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.zq.live.proto.Room.OnlineInfo;

public class GrabPlayerInfoModel extends PlayerInfoModel {
    protected int role;

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
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
}
