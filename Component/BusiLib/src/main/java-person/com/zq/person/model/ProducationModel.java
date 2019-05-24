package com.zq.person.model;

import java.io.Serializable;

public class ProducationModel implements Serializable {
    public static final int TYPE_STAND_NORMAL = 1;    // 一唱到底
    public static final int TYPE_STAND_HIGHLIGHT = 2; // 一唱到底高光时刻
    public static final int TYPE_PRACTICE = 3;        // 练歌房
    public static final int TYPE_TEAM = 4;            // 团队赛


    /**
     * userID : 2449970
     * worksID : 40000080
     * songID : 2624
     * worksURL : http://res-static.inframe.mobi/audios/2449970/5203acecc455a782.aac
     * playCnt : 0
     * name : 再见你好
     * artist : 黄飞鸿
     * cover : http://res-static.inframe.mobi/image/default_m_v2.png
     * category : 2
     * duration : 19456
     * nickName : 哈哈程序
     */

    private int userID;
    private int worksID;
    private int songID;
    private String worksURL;
    private int playCnt;
    private String name;
    private String artist;
    private String cover;
    private int category;
    private int duration;
    private String nickName;

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

    public int getSongID() {
        return songID;
    }

    public void setSongID(int songID) {
        this.songID = songID;
    }

    public String getWorksURL() {
        return worksURL;
    }

    public void setWorksURL(String worksURL) {
        this.worksURL = worksURL;
    }

    public int getPlayCnt() {
        return playCnt;
    }

    public void setPlayCnt(int playCnt) {
        this.playCnt = playCnt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
