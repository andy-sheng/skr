package com.module.feeds.detail.model;

import java.io.Serializable;

public class FeedLikeModel implements Serializable {

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
    private int feedID;
    private long timeMs;
    private int userID;

    public int getFeedID() {
        return feedID;
    }

    public void setFeedID(int feedID) {
        this.feedID = feedID;
    }

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
}
