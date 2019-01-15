package com.module.playways.rank.room.scoremodel;

import com.common.log.MyLog;
import com.zq.live.proto.Room.ScoreItem;

import java.io.Serializable;

public class UserScoreItem implements Serializable {
    /**
     * why : 段位提升了
     * score : 40
     */

    private String why;
    private int score;

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

    public void parse(ScoreItem scoreItem) {
        if (scoreItem == null) {
            MyLog.e("ScoreItem == null");
            return;
        }

        this.setWhy(scoreItem.getWhy());
        this.setScore(scoreItem.getScore());
    }

    @Override
    public String toString() {
        return "UserScoreModel{" +
                "why=" + why +
                ", score=" + score +
                '}';
    }
}
