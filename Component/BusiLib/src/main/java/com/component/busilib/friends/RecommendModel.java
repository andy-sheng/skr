package com.component.busilib.friends;


import com.common.core.userinfo.model.UserInfoModel;

import java.io.Serializable;
import java.util.List;


public class RecommendModel implements Serializable {
    public static final int TYPE_FRIEND_ROOM = 1;
    public static final int TYPE_RECOMMEND_ROOM = 2;
    public static final int TYPE_FOLLOW_ROOM = 3;
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
    private String displayName;
    private String displayURL;
    private String displayAvatar;
    List<PlayUser> playUsers;

    public List<PlayUser> getPlayUsers() {
        return playUsers;
    }

    public void setPlayUsers(List<PlayUser> playUsers) {
        this.playUsers = playUsers;
    }

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


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayURL() {
        return displayURL;
    }

    public void setDisplayURL(String displayURL) {
        this.displayURL = displayURL;
    }

    public String getDisplayAvatar() {
        return displayAvatar;
    }

    public void setDisplayAvatar(String displayAvatar) {
        this.displayAvatar = displayAvatar;
    }

    @Override
    public String toString() {
        return "RecommendModel{" +
                "roomInfo=" + roomInfo +
                ", tagInfo=" + tagInfo +
                ", userInfo=" + userInfo +
                ", category=" + category +
                ", displayName='" + displayName + '\'' +
                ", displayURL='" + displayURL + '\'' +
                ", displayAvatar='" + displayAvatar + '\'' +
                '}';
    }

    public static class PlayUser implements Serializable {
        private int userID;
        private String avatar;

        public int getUserID() {
            return userID;
        }

        public void setUserID(int userID) {
            this.userID = userID;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }

}
