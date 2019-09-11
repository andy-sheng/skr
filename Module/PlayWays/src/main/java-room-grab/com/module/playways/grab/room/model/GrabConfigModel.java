package com.module.playways.grab.room.model;

import com.zq.live.proto.Room.QGameConfig;
import com.zq.live.proto.Room.QScoreTipMsg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrabConfigModel implements Serializable {
    int enableShowBLightWaitTimeMs = 5000;
    int enableShowMLightWaitTimeMs = 5000;
    int totalGameRoundSeq = 100;
    int wantSingDelayTimeMs = 500;
    List<GrabScoreTipMsgModel> qScoreTipMsg = new ArrayList<>();
    int kickUserConsumCoinCnt = 2;

    public static GrabConfigModel parse(QGameConfig config) {
        GrabConfigModel grabConfigModel = new GrabConfigModel();
        grabConfigModel.setEnableShowBLightWaitTimeMs(config.getEnableShowBLightWaitTimeMs());
        grabConfigModel.setEnableShowMLightWaitTimeMs(config.getEnableShowMLightWaitTimeMs());
        grabConfigModel.setTotalGameRoundSeq(config.getTotalGameRoundSeq());
        grabConfigModel.setWantSingDelayTimeMs(config.getWantSingDelayTimeMs());
        for(QScoreTipMsg qScoreTipMsg:config.getQScoreTipMsgList()){
            GrabScoreTipMsgModel grabScoreTipMsgModel = GrabScoreTipMsgModel.parse(qScoreTipMsg);
            grabConfigModel.getQScoreTipMsg().add(grabScoreTipMsgModel);
        }
        grabConfigModel.setKickUserConsumCoinCnt(config.getKickUserConsumCoinCnt());
        return grabConfigModel;
    }

    public int getEnableShowBLightWaitTimeMs() {
        return enableShowBLightWaitTimeMs;
    }

    public void setEnableShowBLightWaitTimeMs(int enableShowBLightWaitTimeMs) {
        this.enableShowBLightWaitTimeMs = enableShowBLightWaitTimeMs;
    }

    public int getEnableShowMLightWaitTimeMs() {
        return enableShowMLightWaitTimeMs;
    }

    public void setEnableShowMLightWaitTimeMs(int enableShowMLightWaitTimeMs) {
        this.enableShowMLightWaitTimeMs = enableShowMLightWaitTimeMs;
    }

    public int getTotalGameRoundSeq() {
        return totalGameRoundSeq;
    }

    public void setTotalGameRoundSeq(int totalGameRoundSeq) {
        this.totalGameRoundSeq = totalGameRoundSeq;
    }

    public int getWantSingDelayTimeMs() {
        return wantSingDelayTimeMs;
    }

    public void setWantSingDelayTimeMs(int wantSingDelayTimeMs) {
        this.wantSingDelayTimeMs = wantSingDelayTimeMs;
    }

    public List<GrabScoreTipMsgModel> getQScoreTipMsg() {
        return qScoreTipMsg;
    }

    public void setQScoreTipMsg(List<GrabScoreTipMsgModel> qScoreTipMsg) {
        this.qScoreTipMsg = qScoreTipMsg;
    }

    public int getKickUserConsumCoinCnt() {
        return kickUserConsumCoinCnt;
    }

    public void setKickUserConsumCoinCnt(int kickUserConsumCoinCnt) {
        this.kickUserConsumCoinCnt = kickUserConsumCoinCnt;
    }

    @Override
    public String toString() {
        return "GrabConfigModel{" +
                "enableShowBLightWaitTimeMs=" + enableShowBLightWaitTimeMs +
                ", enableShowMLightWaitTimeMs=" + enableShowMLightWaitTimeMs +
                ", totalGameRoundSeq=" + totalGameRoundSeq +
                '}';
    }
}
