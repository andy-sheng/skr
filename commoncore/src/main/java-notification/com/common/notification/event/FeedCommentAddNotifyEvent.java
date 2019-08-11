package com.common.notification.event;

import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.FeedCommentAddMsg;

public class FeedCommentAddNotifyEvent {
    long userID; //评论人userID
    int feedID; //feed
    int commentID; //新生成评论ID
    int firstLevelCommentID; //若为二级评论，则需要传入所属的一级评论id
    int replyedCommentID; //被回复的commentID
    BaseNotiInfo basePushInfo;

    public FeedCommentAddNotifyEvent(BaseNotiInfo basePushInfo, FeedCommentAddMsg feedCommentAddMsg) {
        this.basePushInfo = basePushInfo;
        this.userID = feedCommentAddMsg.getUserID();
        this.feedID = feedCommentAddMsg.getFeedID();
        this.commentID = feedCommentAddMsg.getCommentID();
        this.firstLevelCommentID = feedCommentAddMsg.getFirstLevelCommentID();
        this.replyedCommentID = feedCommentAddMsg.getReplyedCommentID();
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

    public int getFirstLevelCommentID() {
        return firstLevelCommentID;
    }

    public void setFirstLevelCommentID(int firstLevelCommentID) {
        this.firstLevelCommentID = firstLevelCommentID;
    }

    public int getReplyedCommentID() {
        return replyedCommentID;
    }

    public void setReplyedCommentID(int replyedCommentID) {
        this.replyedCommentID = replyedCommentID;
    }
}
