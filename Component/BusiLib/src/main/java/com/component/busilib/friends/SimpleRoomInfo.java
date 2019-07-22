package com.component.busilib.friends;

import java.io.Serializable;

/**
 * 房间简易数据
 */
public class SimpleRoomInfo implements Serializable {
    /**
     * roomID : 20369723
     * inPlayersNum : 1
     * totalPlayersNum : 0
     * roomName : 视频专场
     * roomTagURL : http://res-static.inframe.mobi/recommend/friend.png
     * mediaType : 2
     * mediaTagURL : http://res-static.inframe.mobi/recommend/vedio-room.png
     * roomType : 2
     * tagID : 12
     */

    private int roomID;
    private int inPlayersNum;
    private int totalPlayersNum;
    private String roomName;
    private String roomTagURL;
    private int mediaType;
    private String mediaTagURL;
    private int roomType;
    private int tagID;

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
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

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomTagURL() {
        return roomTagURL;
    }

    public void setRoomTagURL(String roomTagURL) {
        this.roomTagURL = roomTagURL;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaTagURL() {
        return mediaTagURL;
    }

    public void setMediaTagURL(String mediaTagURL) {
        this.mediaTagURL = mediaTagURL;
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

    @Override
    public String toString() {
        return "SimpleRoomInfo{" +
                "roomID=" + roomID +
                ", inPlayersNum=" + inPlayersNum +
                ", totalPlayersNum=" + totalPlayersNum +
                ", roomName='" + roomName + '\'' +
                ", roomTagURL='" + roomTagURL + '\'' +
                ", mediaType=" + mediaType +
                ", mediaTagURL='" + mediaTagURL + '\'' +
                ", roomType=" + roomType +
                ", tagID=" + tagID +
                '}';
    }
}
