package com.module.posts.detail.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.posts.watch.model.PostsResoureModel;

import java.io.Serializable;
import java.util.List;

public class PostsSecondLevelCommentModel implements Serializable {

    /**
     * comment : {"audios":[{"URL":"string","durTimeMs":0}],"auditStatus":"ECAS_UNKNOWN","auditStatusDesc":"string","commentID":0,"commentType":"ECT_UNKNOWN","content":"string","createdAt":"string","likedCnt":0,"parentCommentID":0,"pictures":["string"],"postsID":0,"replyType":"ET_UNKNOWN","replyedUserID":0,"status":"ECS_UNKNOWN","statusDesc":"string","subCommentCnt":0,"userID":0,"videos":[{"URL":"string","durTimeMs":0}]}
     * commentUser : {"avatar":"string","hasRedpacket":true,"nickname":"string","userID":0,"vipInfo":{"desc":"string","vipType":"EVT_UNKNOWN"}}
     * isLiked : true
     * replyUser : {"avatar":"string","nickname":"string","userID":0,"vipInfo":{"desc":"string","vipType":"EVT_UNKNOWN"}}
     */

    private SecondLevelCommentBean comment;
    private UserInfoModel commentUser;
    private boolean isLiked;
    private UserInfoModel replyUser;

    public SecondLevelCommentBean getComment() {
        return comment;
    }

    public void setComment(SecondLevelCommentBean comment) {
        this.comment = comment;
    }

    public UserInfoModel getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(UserInfoModel commentUser) {
        this.commentUser = commentUser;
    }

    public boolean isIsLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public UserInfoModel getReplyUser() {
        return replyUser;
    }

    public void setReplyUser(UserInfoModel replyUser) {
        this.replyUser = replyUser;
    }

    public static class SecondLevelCommentBean implements Serializable {
        /**
         * audios : [{"URL":"string","durTimeMs":0}]
         * auditStatus : ECAS_UNKNOWN
         * auditStatusDesc : string
         * commentID : 0
         * commentType : ECT_UNKNOWN
         * content : string
         * createdAt : string
         * likedCnt : 0
         * parentCommentID : 0
         * pictures : ["string"]
         * postsID : 0
         * replyType : ET_UNKNOWN
         * replyedUserID : 0
         * status : ECS_UNKNOWN
         * statusDesc : string
         * subCommentCnt : 0
         * userID : 0
         * videos : [{"URL":"string","durTimeMs":0}]
         */

        private String auditStatus;
        private String auditStatusDesc;
        private int commentID;
        private String commentType;
        private String content;
        private long createdAt;
        private int likedCnt;
        private int parentCommentID;
        private int postsID;
        private String replyType;
        private int replyedUserID;
        private String status;
        private String statusDesc;
        private int subCommentCnt;
        private int userID;
        private List<PostsResoureModel> audios;
        private List<String> pictures;
        private List<PostsResoureModel> videos;

        public String getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(String auditStatus) {
            this.auditStatus = auditStatus;
        }

        public String getAuditStatusDesc() {
            return auditStatusDesc;
        }

        public void setAuditStatusDesc(String auditStatusDesc) {
            this.auditStatusDesc = auditStatusDesc;
        }

        public int getCommentID() {
            return commentID;
        }

        public void setCommentID(int commentID) {
            this.commentID = commentID;
        }

        public String getCommentType() {
            return commentType;
        }

        public void setCommentType(String commentType) {
            this.commentType = commentType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
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

        public int getPostsID() {
            return postsID;
        }

        public void setPostsID(int postsID) {
            this.postsID = postsID;
        }

        public String getReplyType() {
            return replyType;
        }

        public void setReplyType(String replyType) {
            this.replyType = replyType;
        }

        public int getReplyedUserID() {
            return replyedUserID;
        }

        public void setReplyedUserID(int replyedUserID) {
            this.replyedUserID = replyedUserID;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStatusDesc() {
            return statusDesc;
        }

        public void setStatusDesc(String statusDesc) {
            this.statusDesc = statusDesc;
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

        public List<String> getPictures() {
            return pictures;
        }

        public void setPictures(List<String> pictures) {
            this.pictures = pictures;
        }

        public List<PostsResoureModel> getAudios() {
            return audios;
        }

        public void setAudios(List<PostsResoureModel> audios) {
            this.audios = audios;
        }

        public List<PostsResoureModel> getVideos() {
            return videos;
        }

        public void setVideos(List<PostsResoureModel> videos) {
            this.videos = videos;
        }
    }
}
