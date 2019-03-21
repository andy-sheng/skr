package com.module.playways.grab.songselect.model;

import com.common.core.userinfo.model.UserInfoModel;

public class FriendRoomModel {
    /**
     * currNum : 0
     * info : {"avatar":"string","nickName":"string","sex":"unknown","userID":0}
     * isOwner : true
     * playsNum : 0
     * roomID : 0
     * tagName : string
     */

    private int currNum;
    private UserInfoModel info;
    private boolean isOwner;
    private int playsNum;
    private int roomID;
    private String tagName;

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

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

}
