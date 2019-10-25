package com.module.playways.mic.room.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.log.MyLog;
import com.zq.live.proto.Room.MSPKInnerRoundInfo;

import java.io.Serializable;

public class SPkRoundInfoModel implements Serializable {
    public final String TAG = "SPkRoundInfoModel";

    int userID;
    int singBeginMs;
    int singEndMs;

    private int overReason; // 结束的原因
    private int resultType; // 结果类型
    @JSONField(name = "SPKFinalScore")
    private float score;
    private boolean isWin;

    private int meiliTotal;   //用来记录魅力值

    public static SPkRoundInfoModel parse(MSPKInnerRoundInfo roundInfo) {
        SPkRoundInfoModel roundInfoModel = new SPkRoundInfoModel();
        roundInfoModel.setUserID(roundInfo.getUserID());

        roundInfoModel.setSingBeginMs(roundInfo.getSingBeginMs());
        roundInfoModel.setSingEndMs(roundInfo.getSingEndMs());

        roundInfoModel.setOverReason(roundInfo.getOverReason().getValue());
//        roundInfoModel.setResultType(roundInfo.getResultType().getValue());

        roundInfoModel.setScore(roundInfo.getSPKFinalScore());
        roundInfoModel.setWin(roundInfo.getIsWin());
        return roundInfoModel;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getSingBeginMs() {
        return singBeginMs;
    }

    public void setSingBeginMs(int singBeginMs) {
        this.singBeginMs = singBeginMs;
    }

    public int getSingEndMs() {
        return singEndMs;
    }

    public void setSingEndMs(int singEndMs) {
        this.singEndMs = singEndMs;
    }

    public int getOverReason() {
        return overReason;
    }

    public void setOverReason(int overReason) {
        this.overReason = overReason;
    }

    public int getResultType() {
        return resultType;
    }

    public void setResultType(int resultType) {
        this.resultType = resultType;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public int getMeiliTotal() {
        return meiliTotal;
    }

    public void setMeiliTotal(int meiliTotal) {
        this.meiliTotal = meiliTotal;
    }

    public void tryUpdateRoundInfoModel(SPkRoundInfoModel roundInfo, boolean notify) {
        if (roundInfo == null) {
            MyLog.d(TAG, "tryUpdateRoundInfoModel" + " pkRoundInfoModel=" + roundInfo);
            return;
        }
        if (userID == 0) {
            setUserID(roundInfo.getUserID());
        }
        if (roundInfo.getUserID() == userID) {
            this.setSingBeginMs(roundInfo.getSingBeginMs());
            this.setSingEndMs(roundInfo.getSingEndMs());

            if (roundInfo.getOverReason() > 0) {
                this.setOverReason(roundInfo.getOverReason());
            }
            if (roundInfo.getResultType() > 0) {
                this.setResultType(roundInfo.getResultType());
            }
            if (roundInfo.getScore() > 0) {
                this.setScore(roundInfo.getScore());
            }
            this.setWin(roundInfo.isWin());
        }
    }


    @Override
    public String toString() {
        return "SPkRoundInfoModel{" +
                "userID=" + userID +
                ", singBeginMs=" + singBeginMs +
                ", singEndMs=" + singEndMs +
                ", overReason=" + overReason +
                ", resultType=" + resultType +
                ", score=" + score +
                '}';
    }
}
