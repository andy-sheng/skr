package com.component.busilib.friends;


import com.common.core.userinfo.model.UserInfoModel;

import java.io.Serializable;


public class RecommendModel implements Serializable {
    public static final int TYPE_FRIEND_ROOM = 1;
    public static final int TYPE_RECOMMEND_ROOM = 2;
    /**
     * roomInfo : {"inPlayersNum":0,"isOwner":true,"roomID":0,"roomTag":"URT_UNKNOWN","roomType":"RT_UNKNOWN","tagID":0,"totalPlayersNum":0,"userID":0}
     * tagInfo : {"bgColor":"string","introduction":"string","tagID":0,"tagName":"string"}
     * userInfo : {"avatar":"string","nickname":"string","sex":"unknown","userID":0}
     * category : 1
     */

    private SimpleRoomInfo roomInfo;
    private SpecialModel tagInfo;
    private UserInfoModel userInfo;
    private int category;

    public SimpleRoomInfo getRoomInfo() {
        return roomInfo;
    }

    public void setRoomInfo(SimpleRoomInfo roomInfo) {
        this.roomInfo = roomInfo;
    }

    public SpecialModel getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(SpecialModel tagInfo) {
        this.tagInfo = tagInfo;
    }

    public UserInfoModel getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoModel userInfo) {
        this.userInfo = userInfo;
    }


    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "FriendRoomModel{" +
                "roomInfo=" + roomInfo +
                ", tagInfo=" + tagInfo +
                ", userInfo=" + userInfo +
                ", category=" + category +
                '}';
    }
}
