package com.module.playways.grab.room.model;

import com.module.playways.rank.room.model.ScoreTipTypeModel;
import com.zq.live.proto.Room.PKScoreTipMsg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrabScoreTipMsgModel implements Serializable {
    int tipType;
    String tipDesc;
    int fromScore;
    int toScore;


    public int getTipType() {
        return tipType;
    }

    public void setTipType(int tipType) {
        this.tipType = tipType;
    }

    public String getTipDesc() {
        return tipDesc;
    }

    public void setTipDesc(String tipDesc) {
        this.tipDesc = tipDesc;
    }

    public int getFromScore() {
        return fromScore;
    }

    public void setFromScore(int fromScore) {
        this.fromScore = fromScore;
    }

    public int getToScore() {
        return toScore;
    }

    public void setToScore(int toScore) {
        this.toScore = toScore;
    }

}
