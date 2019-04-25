package com.module.playways.grab.room.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.log.MyLog;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.live.proto.Room.EWantSingType;
import com.zq.live.proto.Room.OnlineInfo;
import com.zq.live.proto.Room.QBLightMsg;
import com.zq.live.proto.Room.QCHOInnerRoundInfo;
import com.zq.live.proto.Room.QMLightMsg;
import com.zq.live.proto.Room.QSPKInnerRoundInfo;
import com.zq.live.proto.Room.WantSingInfo;

import org.greenrobot.eventbus.EventBus;

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
    @JSONField(name = "SPKFinalScore")
    private float score;

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
        roundInfoModel.setScore(roundInfo.getSPKFinalScore());
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

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void tryUpdateRoundInfoModel(SPkRoundInfoModel roundInfo, boolean notify, GrabRoundInfoModel grabRoundInfoModel) {
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
            for (MLightInfoModel m : roundInfo.getMLightInfos()) {
                addLightOffUid(notify, m, grabRoundInfoModel);
            }
            for (BLightInfoModel m : roundInfo.getbLightInfos()) {
                addLightBurstUid(notify, m, grabRoundInfoModel);
            }

            if (roundInfo.getOverReason() > 0) {
                this.setOverReason(roundInfo.getOverReason());
            }
            if (roundInfo.getResultType() > 0) {
                this.setResultType(roundInfo.getResultType());
            }
            if (roundInfo.getScore() > 0) {
                this.setScore(roundInfo.getScore());
            }
        }
    }

    public boolean addLightOffUid(boolean notify, MLightInfoModel noPassingInfo, GrabRoundInfoModel roundInfoModel) {
        if (!mLightInfos.contains(noPassingInfo)) {
            mLightInfos.add(noPassingInfo);
            if (notify) {
                GrabSomeOneLightOffEvent event = new GrabSomeOneLightOffEvent(noPassingInfo.getUserID(), roundInfoModel);
                EventBus.getDefault().post(event);
            }
            return true;
        }
        return false;
    }

    public boolean addLightBurstUid(boolean notify, BLightInfoModel bLightInfoModel, GrabRoundInfoModel roundInfoModel) {
        if (!bLightInfos.contains(bLightInfoModel)) {
            bLightInfos.add(bLightInfoModel);
            if (notify) {
                GrabSomeOneLightBurstEvent event = new GrabSomeOneLightBurstEvent(bLightInfoModel.getUserID(), roundInfoModel);
                EventBus.getDefault().post(event);
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "SPkRoundInfoModel{" +
                "userID=" + userID +
                ", singBeginMs=" + singBeginMs +
                ", singEndMs=" + singEndMs +
                ", bLightInfos=" + bLightInfos +
                ", mLightInfos=" + mLightInfos +
                ", overReason=" + overReason +
                ", resultType=" + resultType +
                ", score=" + score +
                '}';
    }
}
