package com.module.playways.rank.prepare.model;

import com.common.log.MyLog;
import com.zq.live.proto.Room.QRoundInfo;
import com.zq.live.proto.Room.RoundInfo;

import java.io.Serializable;

public class RoundInfoModel implements Serializable {
    /**
     * userID : 7
     * playbookID : 1
     * roundSeq : 1
     * singBeginMs : 3000
     * singEndMs : 341000
     */

    private int userID;
    private int playbookID;   //songModelId
    private int roundSeq;
    private int singBeginMs;
    private int singEndMs;
    private long startTs;// 开始时间，服务器的
    private long endTs;// 结束时间，服务器的
    private int sysScore;//本轮系统打分，先搞个默认60分
    private boolean hasSing = false;


    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getPlaybookID() {
        return playbookID;
    }

    public void setPlaybookID(int playbookID) {
        this.playbookID = playbookID;
    }

    public int getRoundSeq() {
        return roundSeq;
    }

    public void setRoundSeq(int roundSeq) {
        this.roundSeq = roundSeq;
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

    public long getStartTs() {
        return startTs;
    }

    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    public long getEndTs() {
        return endTs;
    }

    public void setEndTs(long endTs) {
        this.endTs = endTs;
    }

    public int getSysScore() {
        return sysScore;
    }

    public void setSysScore(int sysScore) {
        this.sysScore = sysScore;
    }

    public boolean isHasSing() {
        return hasSing;
    }

    public void setHasSing(boolean hasSing) {
        this.hasSing = hasSing;
    }

    public void parse(RoundInfo roundInfo) {
        if (roundInfo == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null");
            return;
        }

        this.setUserID(roundInfo.getUserID());
        this.setPlaybookID(roundInfo.getPlaybookID());
        this.setRoundSeq(roundInfo.getRoundSeq());
        this.setSingBeginMs(roundInfo.getSingBeginMs());
        this.setSingEndMs(roundInfo.getSingEndMs());
        return;
    }

    public static RoundInfoModel parseFromQRoundInfo(QRoundInfo roundInfo){
        RoundInfoModel roundInfoModel = new RoundInfoModel();
        roundInfoModel.parse(roundInfo);

        return roundInfoModel;
    }

    public void parse(QRoundInfo roundInfo) {
        if (roundInfo == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null");
            return;
        }

        this.setUserID(roundInfo.getUserID());
        this.setRoundSeq(roundInfo.getRoundSeq());
        this.setSingBeginMs(roundInfo.getSingBeginMs());
        this.setSingEndMs(roundInfo.getSingEndMs());
        return;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoundInfoModel that = (RoundInfoModel) o;

        if (userID != that.userID) return false;
        if (playbookID != that.playbookID) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = userID;
        result = 31 * result + playbookID;
        result = 31 * result + roundSeq;
        return result;
    }

    @Override
    public String toString() {
        return "JsonRoundInfo{" +
                "userID=" + userID +
                ", playbookID=" + playbookID +
                ", roundSeq=" + roundSeq +
                ", singBeginMs=" + singBeginMs +
                ", singEndMs=" + singEndMs +
                ", endTs=" + endTs +
                ", sysScore=" + sysScore +
                '}';
    }


}