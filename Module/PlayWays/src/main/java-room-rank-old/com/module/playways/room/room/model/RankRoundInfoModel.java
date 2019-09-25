package com.module.playways.room.room.model;

import com.common.log.MyLog;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.room.event.PkSomeOneBurstLightEvent;
import com.module.playways.room.room.event.PkSomeOneLightOffEvent;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Room.BLightInfo;
import com.zq.live.proto.Room.MLightInfo;
import com.zq.live.proto.Room.RoundInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;

public class RankRoundInfoModel extends BaseRoundInfoModel {

    private HashSet<BLightInfoModel> bLightInfos = new HashSet<>();//已经爆灯的人, pk

    private HashSet<MLightInfoModel> mLightInfos = new HashSet<>();  //已经灭灯的人, pk


    protected int userID;// 本人在演唱的人
    protected int playbookID;   //songModelId
    protected SongModel music;//本轮次要唱的歌儿的详细信息
    protected int singBeginMs; // 轮次开始时间
    protected int singEndMs; // 轮次结束时间
    protected long startTs;// 开始时间，服务器的
    protected long endTs;// 结束时间，服务器的
    protected int sysScore;//本轮系统打分，先搞个默认60分
    protected boolean hasSing = false;// 是否已经在演唱，依据时引擎等回调，不是作为是否演唱阶段的依据
    public SongModel getMusic() {
        return music;
    }

    public void setMusic(SongModel songModel) {
        this.music = songModel;
    }

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
