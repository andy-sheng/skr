package com.module.playways.rank.room.model.score;

import java.io.Serializable;

// 状态变化具体信息
public class ScoreItemModel implements Serializable {

    private String why;  // 分值变动原因
    private int score;   // 分值变动
    private int index;   // 原因标识别

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}