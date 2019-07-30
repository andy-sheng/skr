package com.module.feeds.detail.event;

public class LikeFirstLevelCommentEvent {
    int commentID;
    boolean like;

    public int getCommentID() {
        return commentID;
    }

    public boolean isLike() {
        return like;
    }

    public LikeFirstLevelCommentEvent(int commentID, boolean like) {
        this.commentID = commentID;
        this.like = like;
    }
}
