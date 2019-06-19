package com.module.playways.grab.room.songmanager.model;

import com.common.core.userinfo.model.UserInfoModel;

import java.io.Serializable;

public class GrabWishSongModel extends GrabRoomSongModel implements Serializable {

    private UserInfoModel suggester;

    public UserInfoModel getSuggester() {
        return suggester;
    }

    public void setSuggester(UserInfoModel suggester) {
        this.suggester = suggester;
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
                ", suggester=" + suggester +
                '}';
    }
}
