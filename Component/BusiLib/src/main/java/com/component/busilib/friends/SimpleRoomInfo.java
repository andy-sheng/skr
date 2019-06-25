package com.component.busilib.friends;

import java.io.Serializable;

/**
 * 房间简易数据
 */
public class SimpleRoomInfo implements Serializable {
    /**
     * roomTag : 1
     * roomID : 20367094
     * isOwner : false
     * roomType : 4
     * inPlayersNum : 2
     * totalPlayersNum : 12
     * userID : 1982416
     * tagID : 13
     * currentRoundSeq : 3
     * totalGameRoundSeq : 36
     * roomName : 天选05后房间
     */

    private int roomTag;
    private int roomID;
    private boolean isOwner;
    private int roomType;
    private int inPlayersNum;
    private int totalPlayersNum;
    private int userID;
    private int tagID;
    private int currentRoundSeq;
    private int totalGameRoundSeq;
    private String roomName;
    private int mediaType;

    public int getRoomTag() {
        return roomTag;
    }

    public void setRoomTag(int roomTag) {
        this.roomTag = roomTag;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public boolean isIsOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public int getInPlayersNum() {
        return inPlayersNum;
    }

    public void setInPlayersNum(int inPlayersNum) {
        this.inPlayersNum = inPlayersNum;
    }

    public int getTotalPlayersNum() {
        return totalPlayersNum;
    }

    public void setTotalPlayersNum(int totalPlayersNum) {
        this.totalPlayersNum = totalPlayersNum;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getTagID() {
        return tagID;
    }

    public void setTagID(int tagID) {
        this.tagID = tagID;
    }

    public int getCurrentRoundSeq() {
        return currentRoundSeq;
    }

    public void setCurrentRoundSeq(int currentRoundSeq) {
        this.currentRoundSeq = currentRoundSeq;
    }

    public int getTotalGameRoundSeq() {
        return totalGameRoundSeq;
    }

    public void setTotalGameRoundSeq(int totalGameRoundSeq) {
        this.totalGameRoundSeq = totalGameRoundSeq;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        return "SimpleRoomInfo{" +
                "roomTag=" + roomTag +
                ", roomID=" + roomID +
                ", isOwner=" + isOwner +
                ", roomType=" + roomType +
                ", inPlayersNum=" + inPlayersNum +
                ", totalPlayersNum=" + totalPlayersNum +
                ", userID=" + userID +
                ", tagID=" + tagID +
                ", currentRoundSeq=" + currentRoundSeq +
                ", totalGameRoundSeq=" + totalGameRoundSeq +
                ", roomName='" + roomName + '\'' +
                '}';
    }
}
