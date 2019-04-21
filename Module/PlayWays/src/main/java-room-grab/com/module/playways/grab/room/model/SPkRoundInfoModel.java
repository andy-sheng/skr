package com.module.playways.grab.room.model;

import com.common.log.MyLog;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Room.OnlineInfo;
import com.zq.live.proto.Room.QBLightMsg;
import com.zq.live.proto.Room.QCHOInnerRoundInfo;
import com.zq.live.proto.Room.QMLightMsg;
import com.zq.live.proto.Room.QSPKInnerRoundInfo;
import com.zq.live.proto.Room.WantSingInfo;

import java.io.Serializable;
import java.util.HashSet;

public class SPkRoundInfoModel implements Serializable {
    public final static String TAG = "SPkRoundInfoModel";

    int userID;
    int singBeginMs;
    int singEndMs;
    private HashSet<BLightInfoModel> bLightInfos = new HashSet<>();//已经爆灯的人, 一唱到底

    private HashSet<MLightInfoModel> mLightInfos = new HashSet<>();//已经灭灯的人, 一唱到底

    private int overReason; // 结束的原因
    private int resultType; // 结果类型

    public static SPkRoundInfoModel parse(QSPKInnerRoundInfo roundInfo) {
        SPkRoundInfoModel roundInfoModel = new SPkRoundInfoModel();
        roundInfoModel.setUserID(roundInfo.getUserID());

        roundInfoModel.setSingBeginMs(roundInfo.getSingBeginMs());
        roundInfoModel.setSingEndMs(roundInfo.getSingEndMs());

        roundInfoModel.setOverReason(roundInfo.getOverReason().getValue());
        roundInfoModel.setResultType(roundInfo.getResultType().getValue());

        for (QBLightMsg m : roundInfo.getBLightInfosList()) {
            roundInfoModel.getbLightInfos().add(BLightInfoModel.parse(m));
        }

        for (QMLightMsg m : roundInfo.getMLightInfosList()) {
            roundInfoModel.getMLightInfos().add(MLightInfoModel.parse(m));
        }

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

    public HashSet<BLightInfoModel> getbLightInfos() {
        return bLightInfos;
    }

    public void setbLightInfos(HashSet<BLightInfoModel> bLightInfos) {
        this.bLightInfos = bLightInfos;
    }

    public HashSet<MLightInfoModel> getMLightInfos() {
        return mLightInfos;
    }

    public void setMLightInfos(HashSet<MLightInfoModel> lightInfos) {
        mLightInfos = lightInfos;
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

    public void tryUpdateRoundInfoModel(SPkRoundInfoModel pkRoundInfoModel) {
        if (pkRoundInfoModel == null) {
            MyLog.d(TAG, "tryUpdateRoundInfoModel" + " pkRoundInfoModel=" + pkRoundInfoModel);
            return;
        }
    }
}
