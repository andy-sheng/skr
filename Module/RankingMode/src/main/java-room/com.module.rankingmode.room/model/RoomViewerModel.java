package com.module.rankingmode.room.model;

public class RoomViewerModel {
    String userId;
    String avatar;

    public RoomViewerModel(String userId, String avatar) {
        this.userId = userId;
        this.avatar = avatar;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
