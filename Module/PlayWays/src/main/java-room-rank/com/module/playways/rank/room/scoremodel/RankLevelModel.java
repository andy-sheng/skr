package com.module.playways.rank.room.scoremodel;


// 段位信息
public class RankLevelModel {

    private int levelNow;           // 现在的段位
    private int levelBefore;        // 之前的段位
    private String levelNowDesc;    // 现在的段位的描述
    private String levelBeforeDesc; // 之前的段位的描述

    public int getLevelNow() {
        return levelNow;
    }

    public void setLevelNow(int levelNow) {
        this.levelNow = levelNow;
    }

    public int getLevelBefore() {
        return levelBefore;
    }

    public void setLevelBefore(int levelBefore) {
        this.levelBefore = levelBefore;
    }

    public String getLevelNowDesc() {
        return levelNowDesc;
    }

    public void setLevelNowDesc(String levelNowDesc) {
        this.levelNowDesc = levelNowDesc;
    }

    public String getLevelBeforeDesc() {
        return levelBeforeDesc;
    }

    public void setLevelBeforeDesc(String levelBeforeDesc) {
        this.levelBeforeDesc = levelBeforeDesc;
    }

    @Override
    public String toString() {
        return "RankLevelModel{" +
                "levelNow=" + levelNow +
                ", levelBefore=" + levelBefore +
                ", levelNowDesc='" + levelNowDesc + '\'' +
                ", levelBeforeDesc='" + levelBeforeDesc + '\'' +
                '}';
    }
}
