package com.common.notification.event;

import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.PostsCommentAddMsg;

public class PostsCommentAddEvent {
    int userID; //评论人userID
    int postsID; //posts
    int commentID; //新生成评论ID
    int firstLevelCommentID; //若为二级评论，则需要传入所属的一级评论id
    int replyedCommentID; //被回复的commentID
    BaseNotiInfo basePushInfo;

    public PostsCommentAddEvent(BaseNotiInfo basePushInfo, PostsCommentAddMsg postsCommentAddMsg) {
        this.userID = postsCommentAddMsg.getUserID();
        this.postsID = postsCommentAddMsg.getPostsID();
        this.commentID = postsCommentAddMsg.getCommentID();
        this.firstLevelCommentID = postsCommentAddMsg.getFirstLevelCommentID();
        this.replyedCommentID = postsCommentAddMsg.getReplyedCommentID();
        this.basePushInfo = basePushInfo;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getPostsID() {
        return postsID;
    }

    public void setPostsID(int postsID) {
        this.postsID = postsID;
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
