package com.module.playways.room.room.score;

import java.io.Serializable;

public class MachineScoreItem implements Serializable {
    long ts;// 这句对应的时间戳,只跟对应的播放资源的时间戳有关，单位是ms
    int score;// 这句的打分
    int no; // 对应歌词哪一行

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

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
