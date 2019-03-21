package com.module.playways.grab.songselect.friends;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.songselect.model.SpecialModel;

public class FriendRoomModel {

    /**
     * currNum : 0
     * info : {"avatar":"string","nickName":"string","sex":"unknown","userID":0}
     * isOwner : true
     * playTag : {"bgColor":"string","introduction":"string","tagID":0,"tagName":"string"}
     * playsNum : 0
     * roomID : 0
     */

    private int currNum;
    private UserInfoModel info;
    private boolean isOwner;
    private SpecialModel playTag;
    private int playsNum;
    private int roomID;

    public int getCurrNum() {
        return currNum;
    }

    public void setCurrNum(int currNum) {
        this.currNum = currNum;
    }

    public UserInfoModel getInfo() {
        return info;
    }

    public void setInfo(UserInfoModel info) {
        this.info = info;
    }

    public boolean isIsOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public SpecialModel getPlayTag() {
        return playTag;
    }

    public void setPlayTag(SpecialModel playTag) {
        this.playTag = playTag;
    }

    public int getPlaysNum() {
        return playsNum;
    }

    public void setPlaysNum(int playsNum) {
        this.playsNum = playsNum;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }
}
