package com.module.playways.grab.room.model;

import com.module.playways.rank.prepare.model.PlayerInfoModel;

public class GrabPlayerInfoModel extends PlayerInfoModel {
    protected int role;

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
