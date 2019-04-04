package com.component.busilib.friends;

import java.io.Serializable;

/**
 * 房间简易数据
 */
public class SimpleRoomInfo implements Serializable {
    /**
     * inPlayersNum : 0
     * isOwner : true
     * roomID : 0
     * roomTag : 1
     * roomType : 2
     * tagID : 0
     * totalPlayersNum : 0
     * userID : 0
     * currentRoundSeq : 0
     * totalGameRoundSeq : 0
     */

    private int inPlayersNum;
    private boolean isOwner;
    private int roomID;
    private int roomTag;
    private int roomType;
    private int tagID;
    private int totalPlayersNum;
    private int userID;
    private int currentRoundSeq;
    private int totalGameRoundSeq;

    public int getInPlayersNum() {
        return inPlayersNum;
    }

    public void setInPlayersNum(int inPlayersNum) {
        this.inPlayersNum = inPlayersNum;
    }

    public boolean isIsOwner() {
        return isOwner;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public int getRoomTag() {
        return roomTag;
    }

    public void setRoomTag(int roomTag) {
        this.roomTag = roomTag;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public int getTagID() {
        return tagID;
    }

    public void setTagID(int tagID) {
        this.tagID = tagID;
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

    @Override
    public String toString() {
        return "SimpleRoomInfo{" +
                "inPlayersNum=" + inPlayersNum +
                ", isOwner=" + isOwner +
                ", roomID=" + roomID +
                ", roomTag=" + roomTag +
                ", roomType=" + roomType +
                ", tagID=" + tagID +
                ", totalPlayersNum=" + totalPlayersNum +
                ", userID=" + userID +
                ", currentRoundSeq=" + currentRoundSeq +
                ", totalGameRoundSeq=" + totalGameRoundSeq +
                '}';
    }
}
