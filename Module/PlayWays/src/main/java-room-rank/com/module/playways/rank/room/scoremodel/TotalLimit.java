package com.module.playways.rank.room.scoremodel;

//上限值
public class TotalLimit {

    int limitBefore;  // 之前的上限值
    int limitNow;     // 现在的上限值

    public int getLimitBefore() {
        return limitBefore;
    }

    public void setLimitBefore(int limitBefore) {
        this.limitBefore = limitBefore;
    }

    public int getLimitNow() {
        return limitNow;
    }

    public void setLimitNow(int limitNow) {
        this.limitNow = limitNow;
    }
}
