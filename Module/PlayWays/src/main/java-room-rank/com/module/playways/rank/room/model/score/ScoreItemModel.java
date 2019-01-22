package com.module.playways.rank.room.model.score;

import com.common.log.MyLog;
import com.zq.live.proto.Room.ScoreItem;

import java.io.Serializable;

// 状态变化具体信息(星星和战力)
public class ScoreItemModel implements Serializable {

    /**
     * "why": "玩了一局排位模式",
     * "score": 50,
     * "index": 1
     */

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

    public void parse(ScoreItem scoreItem) {
        if (scoreItem == null) {
            MyLog.e("scoreItem ScoreItem == null");
            return;
        }

        this.setWhy(scoreItem.getWhy());
        this.setScore(scoreItem.getScore());
        this.setIndex(scoreItem.getIndex());
    }

    @Override
    public String toString() {
        return "ScoreItemModel{" +
                "why='" + why + '\'' +
                ", score=" + score +
                ", index=" + index +
                '}';
    }
}