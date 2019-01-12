package com.common.core.userinfo.model;

public class UserLevelModel {
    public static int RANKING_TYPE = 1;
    public static int SUB_RANKING_TYPE = 2;
    public static int TOTAL_RANKING_STAR_TYPE = 3;
    public static int REAL_RANKING_STAR_TYPE = 4;

    /**
     * userID : 1000011
     * type : 4
     * score : 3
     * desc : 子段位总星星数量
     */

    private int userID;
    private int type;
    private int score;
    private String desc;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
