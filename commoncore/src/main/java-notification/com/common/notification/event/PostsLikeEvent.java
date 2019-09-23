package com.common.notification.event;

import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.PostsLikeMsg;

public class PostsLikeEvent {
    int userID; //点赞人的userID
    int postsID; //点赞的posts
    boolean isLike = false; //点赞 or 取消点赞
    int likeCnt; //点赞数
    BaseNotiInfo basePushInfo;

    public PostsLikeEvent(BaseNotiInfo basePushInfo, PostsLikeMsg postsLikeMsg) {
        this.userID = postsLikeMsg.getUserID();
        this.postsID = postsLikeMsg.getPostsID();
        this.isLike = postsLikeMsg.getIsLike();
        this.likeCnt = postsLikeMsg.getLikeCnt();
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
