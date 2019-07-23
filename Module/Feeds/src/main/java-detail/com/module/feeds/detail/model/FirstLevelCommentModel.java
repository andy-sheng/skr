package com.module.feeds.detail.model;

import com.module.feeds.watch.model.FeedUserInfo;

import java.io.Serializable;

public class FirstLevelCommentModel implements Serializable {

    /**
     * comment : {"commentID":0,"content":"string","createdAt":"string","isLiked":true,"replyUserID":0,"starCnt":0,"subCommentCnt":0,"userID":0}
     * user : {"avatar":"string","nickname":"string","userID":0}
     */

    private CommentBean comment;
    private FeedUserInfo user;

    public CommentBean getComment() {
        return comment;
    }

    public void setComment(CommentBean comment) {
        this.comment = comment;
    }

    public FeedUserInfo getUser() {
        return user;
    }

    public void setUser(FeedUserInfo user) {
        this.user = user;
    }

    public static class CommentBean implements Serializable {
        /**
         * commentID : 0
         * content : string
         * createdAt : string
         * isLiked : true
         * replyUserID : 0
         * starCnt : 0
         * subCommentCnt : 0
         * userID : 0
         */

        private int commentID;
        private String content;
        private String createdAt;
        private boolean isLiked;
        private int replyUserID;
        private int starCnt;
        private int subCommentCnt;
        private int userID;

        public int getCommentID() {
            return commentID;
        }

        public void setCommentID(int commentID) {
            this.commentID = commentID;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public boolean isIsLiked() {
            return isLiked;
        }

        public void setIsLiked(boolean isLiked) {
            this.isLiked = isLiked;
        }

        public int getReplyUserID() {
            return replyUserID;
        }

        public void setReplyUserID(int replyUserID) {
            this.replyUserID = replyUserID;
        }

        public int getStarCnt() {
            return starCnt;
        }

        public void setStarCnt(int starCnt) {
            this.starCnt = starCnt;
        }

        public int getSubCommentCnt() {
            return subCommentCnt;
        }

        public void setSubCommentCnt(int subCommentCnt) {
            this.subCommentCnt = subCommentCnt;
        }

        public int getUserID() {
            return userID;
        }

        public void setUserID(int userID) {
            this.userID = userID;
        }
    }
}
