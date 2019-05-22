package com.zq.person.model;

import java.io.Serializable;

public class ProducationModel implements Serializable {
    public static final int TYPE_STAND_NORMAL = 1;    // 一唱到底
    public static final int TYPE_STAND_HIGHLIGHT = 2; // 一唱到底高光时刻
    public static final int TYPE_PRACTICE = 3;        // 练歌房
    public static final int TYPE_TEAM = 4;            // 团队赛

    /**
     * artist : string
     * category : 0
     * cover : string
     * duration : string
     * name : string
     * playCnt : 0
     * songID : 0
     * userID : 0
     * worksID : 0
     * worksURL : string
     */

    private String artist;
    private int category;
    private String cover;
    private String duration;
    private String name;
    private int playCnt;
    private int songID;
    private int userID;
    private int worksID;
    private String worksURL;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlayCnt() {
        return playCnt;
    }

    public void setPlayCnt(int playCnt) {
        this.playCnt = playCnt;
    }

    public int getSongID() {
        return songID;
    }

    public void setSongID(int songID) {
        this.songID = songID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getWorksID() {
        return worksID;
    }

    public void setWorksID(int worksID) {
        this.worksID = worksID;
    }

    public String getWorksURL() {
        return worksURL;
    }

    public void setWorksURL(String worksURL) {
        this.worksURL = worksURL;
    }
}
