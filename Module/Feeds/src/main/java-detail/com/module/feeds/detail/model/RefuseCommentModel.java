package com.module.feeds.detail.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.VipInfo;

import java.io.Serializable;

public class RefuseCommentModel implements Serializable {

    /**
     * actionDesc : string
     * avatar : string
     * content : string
     * nickname : string
     * timeMs : 0
     * userID : 0
     */

    private String actionDesc;
    private String avatar;
    private String content;
    private String nickname;
    private VipInfo vipInfo;
    private int feedID;
    private long timeMs;
    private int userID;

    public String getActionDesc() {
        return actionDesc;
    }

    public void setActionDesc(String actionDesc) {
        this.actionDesc = actionDesc;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public VipInfo getVipInfo() {
        return vipInfo;
    }

    public void setVipInfo(VipInfo vipInfo) {
        this.vipInfo = vipInfo;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(long timeMs) {
        this.timeMs = timeMs;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getFeedID() {
        return feedID;
    }

    public void setFeedID(int feedID) {
        this.feedID = feedID;
    }

    public UserInfoModel toUserInfoModel(){
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.setUserId(userID);
        userInfoModel.setAvatar(avatar);
        userInfoModel.setNickname(nickname);
        userInfoModel.setVipInfo(vipInfo);
        return userInfoModel;
    }
}
