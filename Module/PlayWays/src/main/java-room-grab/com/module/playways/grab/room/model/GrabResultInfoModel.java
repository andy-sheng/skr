package com.module.playways.grab.room.model;

import com.zq.live.proto.Room.QResultInfo;

import java.io.Serializable;

public class GrabResultInfoModel implements Serializable {
    int userID; //用户标识
    int wantSingChanceCnt; //想唱歌数量
    int getSingChanceCnt; //演唱机会数量
    int wholeTimeSingCnt; //一唱到底数量
    float wholeTimeSingRatio; //一唱到底成功率
    float beyondSkrerRatio; //超过同段位

    public static GrabResultInfoModel parse(QResultInfo info) {
        GrabResultInfoModel grabResultInfoModel = new GrabResultInfoModel();
        grabResultInfoModel.setUserID(info.getUserID());
        grabResultInfoModel.setWantSingChanceCnt(info.getWantSingChanceCnt());
        grabResultInfoModel.setGetSingChanceCnt(info.getGetSingChanceCnt());
        grabResultInfoModel.setWholeTimeSingCnt(info.getWholeTimeSingCnt());
        grabResultInfoModel.setWholeTimeSingRatio(info.getWholeTimeSingRatio());
        grabResultInfoModel.setBeyondSkrerRatio(info.getBeyondSkrerRatio());
        return null;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getWantSingChanceCnt() {
        return wantSingChanceCnt;
    }

    public void setWantSingChanceCnt(int wantSingChanceCnt) {
        this.wantSingChanceCnt = wantSingChanceCnt;
    }

    public int getGetSingChanceCnt() {
        return getSingChanceCnt;
    }

    public void setGetSingChanceCnt(int getSingChanceCnt) {
        this.getSingChanceCnt = getSingChanceCnt;
    }

    public int getWholeTimeSingCnt() {
        return wholeTimeSingCnt;
    }

    public void setWholeTimeSingCnt(int wholeTimeSingCnt) {
        this.wholeTimeSingCnt = wholeTimeSingCnt;
    }

    public float getWholeTimeSingRatio() {
        return wholeTimeSingRatio;
    }

    public void setWholeTimeSingRatio(float wholeTimeSingRatio) {
        this.wholeTimeSingRatio = wholeTimeSingRatio;
    }

    public float getBeyondSkrerRatio() {
        return beyondSkrerRatio;
    }

    public void setBeyondSkrerRatio(float beyondSkrerRatio) {
        this.beyondSkrerRatio = beyondSkrerRatio;
    }
}
