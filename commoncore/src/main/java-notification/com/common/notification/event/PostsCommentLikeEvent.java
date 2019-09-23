package com.common.notification.event;

import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.PostsCommentLikeMsg;

public class PostsCommentLikeEvent {
    int userID; //点赞人的userID
    int postsID; //posts
    int commentID; //评论id
    boolean isLike = false; //点赞 or 取消点赞
    int likeCnt; //点赞数
    BaseNotiInfo basePushInfo;

    public PostsCommentLikeEvent(BaseNotiInfo basePushInfo, PostsCommentLikeMsg postsCommentLikeMsg) {
        this.userID = postsCommentLikeMsg.getUserID();
        this.postsID = postsCommentLikeMsg.getPostsID();
        this.commentID = postsCommentLikeMsg.getCommentID();
        this.isLike = postsCommentLikeMsg.getIsLike();
        this.likeCnt = postsCommentLikeMsg.getLikeCnt();
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
