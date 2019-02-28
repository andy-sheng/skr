package com.module.playways.grab.room.model;

import com.module.playways.rank.room.model.PkScoreTipMsgModel;

import java.io.Serializable;
import java.util.List;

public class GrabConfigModel implements Serializable {
    int enableShowBLightWaitTimeMs;
    int enableShowMLightWaitTimeMs;
    int totalGameRoundSeq;

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
}
