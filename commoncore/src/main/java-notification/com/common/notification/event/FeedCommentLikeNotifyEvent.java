package com.common.notification.event;

import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.FeedCommentLikeMsg;

public class FeedCommentLikeNotifyEvent {
    long userID; //点赞人的userID
    int feedID; //feed
    int commentID; //评论id
    boolean isLike; //点赞 or 取消点赞
    int likeCnt; //点赞数
    BaseNotiInfo basePushInfo;

    public FeedCommentLikeNotifyEvent(BaseNotiInfo basePushInfo, FeedCommentLikeMsg feedCommentLikeMsg) {
        this.basePushInfo = basePushInfo;
        this.userID = feedCommentLikeMsg.getUserID();
        this.feedID = feedCommentLikeMsg.getFeedID();
        this.commentID = feedCommentLikeMsg.getCommentID();
        this.isLike = feedCommentLikeMsg.getIsLike();
        this.likeCnt = feedCommentLikeMsg.getLikeCnt();
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

    public int getCommentID() {
        return commentID;
    }

    public void setCommentID(int commentID) {
        this.commentID = commentID;
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
