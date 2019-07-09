package com.module.playways.grab.room.songmanager.model;

import com.common.core.userinfo.model.UserInfoModel;

import java.io.Serializable;

public class GrabWishSongModel extends GrabRoomSongModel implements Serializable {

    private String pID;

    private UserInfoModel suggester;

    public UserInfoModel getSuggester() {
        return suggester;
    }

    public void setSuggester(UserInfoModel suggester) {
        this.suggester = suggester;
    }

    public String getpID() {
        return pID;
    }

    public void setpID(String pID) {
        this.pID = pID;
    }

    @Override
    public String toString() {
        return "GrabRoomSongModel{" +
                "itemName='" + itemName + '\'' +
                ", owner='" + owner + '\'' +
                ", roundSeq=" + roundSeq +
                ", itemID=" + itemID +
                ", playType=" + playType +
                ", challengeAvailable=" + challengeAvailable +
                ", pID=" + pID +
                ", suggester=" + suggester +
                '}';
    }
}
