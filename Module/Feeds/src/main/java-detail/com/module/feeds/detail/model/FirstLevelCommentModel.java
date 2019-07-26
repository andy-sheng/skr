package com.module.feeds.detail.model;

import com.component.feeds.model.FeedUserInfo;

import java.io.Serializable;

public class FirstLevelCommentModel implements Serializable {

    /**
     * comment : {"commentID":0,"content":"string","createdAt":"string","isLiked":true,"replyUserID":0,"starCnt":0,"subCommentCnt":0,"userID":0}
     * user : {"avatar":"string","nickname":"string","userID":0}
     */

    private CommentBean comment;
    private FeedUserInfo replyUser;
    private FeedUserInfo commentUser;
    private boolean isLiked;

    public FirstLevelCommentModel() {
    }

    public CommentBean getComment() {
        return comment;
    }

    public void setComment(CommentBean comment) {
        this.comment = comment;
    }

    public FeedUserInfo getReplyUser() {
        return replyUser;
    }

    public void setReplyUser(FeedUserInfo replyUser) {
        this.replyUser = replyUser;
    }

    public FeedUserInfo getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(FeedUserInfo commentUser) {
        this.commentUser = commentUser;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public static class CommentBean implements Serializable {

        /**
         * commentID : 0
         * commentType : ECT_UNKNOWN
         * content : string
         * createdAt : string
         * feedID : 0
         * likedCnt : 0
         * parentCommentID : 0
         * replyType : ET_UNKNOWN
         * replyedUserID : 0
         * subCommentCnt : 0
         * userID : 0
         */

        private int commentID;
        private int commentType;//ECT_UNKNOWN = 0 : 未知 - ECT_FIRST_LEVEL = 1 : 一级评论 - ECT_SECOND_LEVEL = 2 : 二级评论
        private String content;
        private Long createdAt;
        private int feedID;
        private int likedCnt;
        private int parentCommentID;
        private int replyType;//- ET_UNKNOWN = 0 : 未知 - ET_REPLY_TO_MAIN = 1 : 回复主评论 - ET_REPLY_TO_SUB = 2 : 回复子评论
        private int replyedUserID;
        private int subCommentCnt;
        private int userID;

        public CommentBean() {
        }

        public CommentBean(int commentID, String content, Long createdAt, int userID) {
            this.commentID = commentID;
            this.content = content;
            this.createdAt = createdAt;
            this.userID = userID;
        }

        public int getCommentID() {
            return commentID;
        }

        public int getCommentType() {
            return commentType;
        }

        public void setCommentType(int commentType) {
            this.commentType = commentType;
        }

        public int getReplyType() {
            return replyType;
        }

        public void setReplyType(int replyType) {
            this.replyType = replyType;
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

        public Long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Long createdAt) {
            this.createdAt = createdAt;
        }

        public int getFeedID() {
            return feedID;
        }

        public void setFeedID(int feedID) {
            this.feedID = feedID;
        }

        public int getLikedCnt() {
            return likedCnt;
        }

        public void setLikedCnt(int likedCnt) {
            this.likedCnt = likedCnt;
        }

        public int getParentCommentID() {
            return parentCommentID;
        }

        public void setParentCommentID(int parentCommentID) {
            this.parentCommentID = parentCommentID;
        }

        public int getReplyedUserID() {
            return replyedUserID;
        }

        public void setReplyedUserID(int replyedUserID) {
            this.replyedUserID = replyedUserID;
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
