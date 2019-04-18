package com.common.core.userinfo.model;

import java.io.Serializable;

public class UserRankModel implements Serializable {

    public static final int STAR_BADGE = 1;
    public static final int TOP_BADGE = 2;
    public static final int SHANDIAN_BADGE = 3;

    public static final int COUNTRY = 1;     //国家
    public static final int PROVINCIAL = 2;  //省会
    public static final int CITY = 3;        //城市
    public static final int REGION = 4;      //镇区

    /**
     * category : 1
     * seq : 4
     * regionDesc : 全国
     * starCnt : 3
     * maxStar : 5
     * mainRanking : 4
     * subRanking : 4
     * rankingDesc : 铂金唱将IV
     * text : 全国第4名
     * highlight : 4
     * diff : 0
     * mainDesc : 铂金唱将
     */

    private int category;      // 类别
    private int rankSeq;       // 排名
    private String regionDesc; // 类别描述
    private int starCnt;       // 当前星星数
    private int maxStar;       // 最大星星数
    private int mainRanking;   // 主段位
    private int subRanking;    // 子段位
    private String levelDesc;  // 段位描述
    private String mainDesc;   // 主段位描述
    private String text;       // 排行描述
    private String highlight;  // 排行描述中高亮部分
    private int diff;          // 上升和下降
    private int badge;         // 类别

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getRankSeq() {
        return rankSeq;
    }

    public void setRankSeq(int rankSeq) {
        this.rankSeq = rankSeq;
    }

    public String getRegionDesc() {
        return regionDesc;
    }

    public void setRegionDesc(String regionDesc) {
        this.regionDesc = regionDesc;
    }

    public int getStarCnt() {
        return starCnt;
    }

    public void setStarCnt(int starCnt) {
        this.starCnt = starCnt;
    }

    public int getMaxStar() {
        return maxStar;
    }

    public void setMaxStar(int maxStar) {
        this.maxStar = maxStar;
    }

    public int getMainRanking() {
        return mainRanking;
    }

    public void setMainRanking(int mainRanking) {
        this.mainRanking = mainRanking;
    }

    public int getSubRanking() {
        return subRanking;
    }

    public void setSubRanking(int subRanking) {
        this.subRanking = subRanking;
    }

    public String getLevelDesc() {
        return levelDesc;
    }

    public void setLevelDesc(String levelDesc) {
        this.levelDesc = levelDesc;
    }


    public String getMainDesc() {
        return mainDesc;
    }

    public void setMainDesc(String mainDesc) {
        this.mainDesc = mainDesc;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    @Override
    public String toString() {
        return "UserRankModel{" +
                "category=" + category +
                ", rankSeq=" + rankSeq +
                ", regionDesc='" + regionDesc + '\'' +
                ", starCnt=" + starCnt +
                ", maxStar=" + maxStar +
                ", mainRanking=" + mainRanking +
                ", subRanking=" + subRanking +
                ", levelDesc='" + levelDesc + '\'' +
                ", mainDesc='" + mainDesc + '\'' +
                ", text='" + text + '\'' +
                ", highlight='" + highlight + '\'' +
                ", diff=" + diff +
                ", badge=" + badge +
                '}';
    }
}
