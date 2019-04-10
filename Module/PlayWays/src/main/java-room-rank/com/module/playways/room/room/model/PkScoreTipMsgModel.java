package com.module.playways.room.room.model;

import com.zq.live.proto.Room.PKScoreTipMsg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PkScoreTipMsgModel implements Serializable {
    ScoreTipTypeModel scoreTipTypeModel;
    String tipDesc;
    int fromScore;
    int toScore;

    public ScoreTipTypeModel getScoreTipTypeModel() {
        return scoreTipTypeModel;
    }

    public void setScoreTipTypeModel(ScoreTipTypeModel scoreTipTypeModel) {
        this.scoreTipTypeModel = scoreTipTypeModel;
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

    public static List<PkScoreTipMsgModel> parse(List<PKScoreTipMsg> pkScoreTipMsgList){
        ArrayList<PkScoreTipMsgModel> pkScoreTipMsgModels = new ArrayList<>();
        if(pkScoreTipMsgList != null){
            for (PKScoreTipMsg pkScoreTipMsg : pkScoreTipMsgList) {
                pkScoreTipMsgModels.add(parse(pkScoreTipMsg));
            }
        }
        return pkScoreTipMsgModels;
    }

    public static PkScoreTipMsgModel parse(PKScoreTipMsg pkScoreTipMsg) {
        PkScoreTipMsgModel pkScoreTipMsgModel = new PkScoreTipMsgModel();
        pkScoreTipMsgModel.setFromScore(pkScoreTipMsg.getFromScore());
        pkScoreTipMsgModel.setTipDesc(pkScoreTipMsg.getTipDesc());
        pkScoreTipMsgModel.setToScore(pkScoreTipMsg.getToScore());

        switch (pkScoreTipMsg.getTipType()){
            case ST_UNKNOWN:
                pkScoreTipMsgModel.scoreTipTypeModel = ScoreTipTypeModel.ST_UNKNOWN;
                break;
            case ST_TOO_BAD:
                pkScoreTipMsgModel.scoreTipTypeModel = ScoreTipTypeModel.ST_TOO_BAD;
                break;
            case ST_NOT_BAD:
                pkScoreTipMsgModel.scoreTipTypeModel = ScoreTipTypeModel.ST_NOT_BAD;
                break;
            case ST_VERY_GOOD:
                pkScoreTipMsgModel.scoreTipTypeModel = ScoreTipTypeModel.ST_VERY_GOOD;
                break;
            case ST_NICE_PERFECT:
                pkScoreTipMsgModel.scoreTipTypeModel = ScoreTipTypeModel.ST_NICE_PERFECT;
                break;
        }

        return pkScoreTipMsgModel;
    }
}
