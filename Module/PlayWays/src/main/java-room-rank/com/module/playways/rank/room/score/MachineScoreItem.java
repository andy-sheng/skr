package com.module.playways.rank.room.score;

import java.io.Serializable;

public class MachineScoreItem implements Serializable {
    long ts;// 这句对应的时间戳，是这首歌完整时间为区间的，单位是ms
    int score;// 这句的打分

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
