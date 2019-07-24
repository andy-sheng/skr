package com.module.playways.room.room.model;

import com.common.log.MyLog;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.room.event.PkSomeOneBurstLightEvent;
import com.module.playways.room.room.event.PkSomeOneLightOffEvent;
import com.zq.live.proto.Room.BLightInfo;
import com.zq.live.proto.Room.MLightInfo;
import com.zq.live.proto.Room.RoundInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;

public class RankRoundInfoModel extends BaseRoundInfoModel {

    private HashSet<BLightInfoModel> bLightInfos = new HashSet<>();//已经爆灯的人, pk

    private HashSet<MLightInfoModel> mLightInfos = new HashSet<>();  //已经灭灯的人, pk

    public RankRoundInfoModel() {

    }

    @Override
    public int getType() {
        return TYPE_RANK;
    }

    public HashSet<BLightInfoModel> getbLightInfos() {
        return bLightInfos;
    }

    /**
     * 排位赛使用
     */
    public void setbLightInfos(HashSet<BLightInfoModel> bLightInfos) {
        this.bLightInfos = bLightInfos;
    }

    public HashSet<MLightInfoModel> getLightInfos() {
        return mLightInfos;
    }

    public void setLightInfos(HashSet<MLightInfoModel> lightInfos) {
        mLightInfos = lightInfos;
    }

    /**
     * 排位赛使用
     */
    public void addBrustLightUid(boolean notify, BLightInfoModel bLightInfoModel) {
        if (!bLightInfos.contains(bLightInfoModel)) {
            bLightInfos.add(bLightInfoModel);
            MyLog.d(TAG, "addBrustLightUid" + " notify=" + notify + " bLightInfoModel=" + bLightInfoModel);
            if (notify) {
                PkSomeOneBurstLightEvent event = new PkSomeOneBurstLightEvent(bLightInfoModel.getUserID(), this);
                EventBus.getDefault().post(event);
            }
        }
    }

    /**
     * 排位赛使用
     */
    public void addPkLightOffUid(boolean notify, MLightInfoModel mLightInfoModel) {
        if (!mLightInfos.contains(mLightInfoModel)) {
            mLightInfos.add(mLightInfoModel);
            if (notify) {
                PkSomeOneLightOffEvent event = new PkSomeOneLightOffEvent(mLightInfoModel.getUserID(), this);
                EventBus.getDefault().post(event);
            }
        }
    }

    /**
     * 排位赛使用
     */
    public void tryUpdateRoundInfoModel(BaseRoundInfoModel round, boolean notify) {
        if (round == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null");
            return;
        }
        RankRoundInfoModel roundInfo = (RankRoundInfoModel) round;
        this.setUserID(roundInfo.getUserID());
        this.setPlaybookID(roundInfo.getPlaybookID());
        this.setRoundSeq(roundInfo.getRoundSeq());
        this.setSingBeginMs(roundInfo.getSingBeginMs());
        this.setSingEndMs(roundInfo.getSingEndMs());
        //TODO 抢 灭 结束原因 补全 注意json里面是不带这些的
        for (BLightInfoModel bLightInfoModel : roundInfo.getbLightInfos()) {
            bLightInfoModel.setSeq(roundInfo.getRoundSeq());
            addBrustLightUid(notify, bLightInfoModel);
        }
        for (MLightInfoModel mLightInfoModel : roundInfo.getLightInfos()) {
            mLightInfoModel.setSeq(roundInfo.getRoundSeq());
            addPkLightOffUid(notify, mLightInfoModel);
        }
        if (roundInfo.getOverReason() > 0) {
            this.setOverReason(roundInfo.getOverReason());
        }
    }

    public static RankRoundInfoModel parseFromRoundInfo(RoundInfo roundInfo) {
        RankRoundInfoModel roundInfoModel = new RankRoundInfoModel();
        roundInfoModel.setUserID(roundInfo.getUserID());
        roundInfoModel.setPlaybookID(roundInfo.getPlaybookID());
        roundInfoModel.setRoundSeq(roundInfo.getRoundSeq());
        roundInfoModel.setSingBeginMs(roundInfo.getSingBeginMs());
        roundInfoModel.setSingEndMs(roundInfo.getSingEndMs());
        if (roundInfo.getBLightInfosList() != null) {
            for (BLightInfo bLightInfo : roundInfo.getBLightInfosList()) {
                roundInfoModel.addBrustLightUid(false, BLightInfoModel.parse(bLightInfo, roundInfo.getRoundSeq()));
            }
        }
        if (roundInfo.getMLightInfosList() != null) {
            for (MLightInfo mlightInfo : roundInfo.getMLightInfosList()) {
                roundInfoModel.addPkLightOffUid(false, MLightInfoModel.parse(mlightInfo, roundInfo.getRoundSeq()));
            }
        }
        roundInfoModel.setOverReason(roundInfo.getOverReason().getValue());
        return roundInfoModel;
    }
}
