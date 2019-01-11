package com.common.core.userinfo.model;

public class UserRankModel {

    public static final int COUNTRY = 1;     //国家
    public static final int PROVINCIAL = 2;  //省会
    public static final int CITY = 3;        //城市
    public static final int REGION = 4;      //镇区

    /**
     * categoy : 1
     * seq : 101
     * regionDesc : 全国
     */

    private int category;
    private int seq;
    private String regionDesc;

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getRegionDesc() {
        return regionDesc;
    }

    public void setRegionDesc(String regionDesc) {
        this.regionDesc = regionDesc;
    }
}
