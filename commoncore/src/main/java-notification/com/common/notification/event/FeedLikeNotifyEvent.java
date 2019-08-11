package com.common.notification.event;

import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.FeedLikeMsg;

public class FeedLikeNotifyEvent {
    long userID; //点赞人的userID
    int feedID; //点赞的feed
    boolean isLike; //点赞 or 取消点赞
    int likeCnt; //点赞数
    BaseNotiInfo basePushInfo;

    public FeedLikeNotifyEvent(BaseNotiInfo basePushInfo, FeedLikeMsg feedLikeMsg) {
        this.basePushInfo = basePushInfo;
        this.userID = feedLikeMsg.getUserID();
        this.feedID = feedLikeMsg.getFeedID();
        this.isLike = feedLikeMsg.getIsLike();
        this.likeCnt = feedLikeMsg.getLikeCnt();
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public int getFeedID() {
        return feedID;
    }

    public void setFeedID(int feedID) {
        this.feedID = feedID;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    public int getLikeCnt() {
        return likeCnt;
    }

    public void setLikeCnt(int likeCnt) {
        this.likeCnt = likeCnt;
    }
}
