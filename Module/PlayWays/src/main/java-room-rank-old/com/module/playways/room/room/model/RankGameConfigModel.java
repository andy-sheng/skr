package com.module.playways.room.room.model;

import com.zq.live.proto.Room.GameConfig;
import java.io.Serializable;
import java.util.List;

public class RankGameConfigModel implements Serializable {
    int pKMaxShowBLightTimes; //最大爆灯次数
    int pKMaxShowMLightTimes; //最大灭灯次数
    int pKEnableShowBLightWaitTimeMs; //爆灯等待时间(毫秒)
    int pKEnableShowMLightWaitTimeMs; //灭灯等待时间(毫秒)
    List<PkScoreTipMsgModel> pkScoreTipMsgModelList; //反馈分提示语
    float pKFullEnergyPercentage; //满能量比例
    float pKBLightEnergyPercentage; //爆灯加能量比例
    float pKMLightEnergyPercentage; //灭灯减能量比例

    public int getpKMaxShowBLightTimes() {
        return pKMaxShowBLightTimes;
    }

    public void setpKMaxShowBLightTimes(int pKMaxShowBLightTimes) {
        this.pKMaxShowBLightTimes = pKMaxShowBLightTimes;
    }

    public int getpKMaxShowMLightTimes() {
        return pKMaxShowMLightTimes;
    }

    public void setpKMaxShowMLightTimes(int pKMaxShowMLightTimes) {
        this.pKMaxShowMLightTimes = pKMaxShowMLightTimes;
    }

    public int getpKEnableShowBLightWaitTimeMs() {
        return pKEnableShowBLightWaitTimeMs;
    }

    public void setpKEnableShowBLightWaitTimeMs(int pKEnableShowBLightWaitTimeMs) {
        this.pKEnableShowBLightWaitTimeMs = pKEnableShowBLightWaitTimeMs;
    }

    public int getpKEnableShowMLightWaitTimeMs() {
        return pKEnableShowMLightWaitTimeMs;
    }

    public void setpKEnableShowMLightWaitTimeMs(int pKEnableShowMLightWaitTimeMs) {
        this.pKEnableShowMLightWaitTimeMs = pKEnableShowMLightWaitTimeMs;
    }

    public List<PkScoreTipMsgModel> getPkScoreTipMsgModelList() {
        return pkScoreTipMsgModelList;
    }

    public void setPkScoreTipMsgModelList(List<PkScoreTipMsgModel> pkScoreTipMsgModelList) {
        this.pkScoreTipMsgModelList = pkScoreTipMsgModelList;
    }

    public float getpKFullEnergyPercentage() {
        return pKFullEnergyPercentage;
    }

    public void setpKFullEnergyPercentage(float pKFullEnergyPercentage) {
        this.pKFullEnergyPercentage = pKFullEnergyPercentage;
    }

    public float getpKBLightEnergyPercentage() {
        return pKBLightEnergyPercentage;
    }

    public void setpKBLightEnergyPercentage(float pKBLightEnergyPercentage) {
        this.pKBLightEnergyPercentage = pKBLightEnergyPercentage;
    }

    public float getpKMLightEnergyPercentage() {
        return pKMLightEnergyPercentage;
    }

    public void setpKMLightEnergyPercentage(float pKMLightEnergyPercentage) {
        this.pKMLightEnergyPercentage = pKMLightEnergyPercentage;
    }

    @Override
    public String toString() {
        return "RankGameConfigModel{" +
                "pKMaxShowBLightTimes=" + pKMaxShowBLightTimes +
                ", pKMaxShowMLightTimes=" + pKMaxShowMLightTimes +
                ", pKEnableShowBLightWaitTimeMs=" + pKEnableShowBLightWaitTimeMs +
                ", pKEnableShowMLightWaitTimeMs=" + pKEnableShowMLightWaitTimeMs +
                ", pKFullEnergyPercentage=" + pKFullEnergyPercentage +
                ", pKBLightEnergyPercentage=" + pKBLightEnergyPercentage +
                ", pKMLightEnergyPercentage=" + pKMLightEnergyPercentage +
                '}';
    }

    public static RankGameConfigModel parse(GameConfig gameConfig) {
        RankGameConfigModel gameConfigModel = new RankGameConfigModel();
        gameConfigModel.pKMaxShowBLightTimes = gameConfig.getPKMaxShowBLightTimes();
        gameConfigModel.pKMaxShowMLightTimes = gameConfig.getPKMaxShowMLightTimes();
        gameConfigModel.pKEnableShowBLightWaitTimeMs = gameConfig.getPKEnableShowBLightWaitTimeMs();
        gameConfigModel.pKEnableShowMLightWaitTimeMs = gameConfig.getPKEnableShowMLightWaitTimeMs();
        gameConfigModel.pkScoreTipMsgModelList = PkScoreTipMsgModel.parse(gameConfig.getPkScoreTipMsgList());
        gameConfigModel.pKFullEnergyPercentage = gameConfig.getPKFullEnergyPercentage();
        gameConfigModel.pKBLightEnergyPercentage = gameConfig.getPKBLightEnergyPercentage();
        gameConfigModel.pKMLightEnergyPercentage = gameConfig.getPKMLightEnergyPercentage();

        return gameConfigModel;
    }
}
