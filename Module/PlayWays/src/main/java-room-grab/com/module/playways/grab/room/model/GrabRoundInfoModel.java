package com.module.playways.grab.room.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.module.playways.grab.room.event.GrabChorusUserStatusChangeEvent;
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent;
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent;
import com.module.playways.grab.room.event.GrabWaitSeatUpdateEvent;
import com.module.playways.grab.room.event.SomeOneGrabEvent;
import com.module.playways.grab.room.event.SomeOneJoinPlaySeatEvent;
import com.module.playways.grab.room.event.SomeOneJoinWaitSeatEvent;
import com.module.playways.grab.room.event.SomeOneLeavePlaySeatEvent;
import com.module.playways.grab.room.event.SomeOneLeaveWaitSeatEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.live.proto.Room.EWantSingType;
import com.zq.live.proto.Room.OnlineInfo;
import com.zq.live.proto.Room.QBLightMsg;
import com.zq.live.proto.Room.QCHOInnerRoundInfo;
import com.zq.live.proto.Room.QMLightMsg;
import com.zq.live.proto.Room.QRoundInfo;
import com.zq.live.proto.Room.QSPKInnerRoundInfo;
import com.zq.live.proto.Room.WantSingInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GrabRoundInfoModel extends BaseRoundInfoModel {

    /* 一唱到底使用 */
    private int status = EQRoundStatus.QRS_UNKNOWN.getValue();// 轮次状态，在一唱到底中使用

    private HashSet<BLightInfoModel> bLightInfos = new HashSet<>();//已经爆灯的人, 一唱到底

    private HashSet<MLightInfoModel> mLightInfos = new HashSet<>();//已经灭灯的人, 一唱到底

    private List<GrabPlayerInfoModel> playUsers = new ArrayList<>(); // 参与这轮游戏中的人，包括离线

    private List<GrabPlayerInfoModel> waitUsers = new ArrayList<>(); // 等待参与这轮游戏的人，包括观众

    private GrabSkrResourceModel skrResource; // 机器人资源

    private HashSet<WantSingerInfo> wantSingInfos = new HashSet<>(); //已经抢了的人

    //0未知
    //1有种优秀叫一唱到底（全部唱完）
    //2有种结束叫刚刚开始（t<30%）
    //3有份悲伤叫都没及格(30%<=t <60%)
    //4有种遗憾叫明明可以（60%<=t<90%）
    //5有种可惜叫我觉得你行（90%<=t<=100%)
    private int resultType; // 结果类型

    private boolean isParticipant = true;// 我是不是这局的参与者

    private int elapsedTimeMs;//这个轮次当前状态已经经过的时间，一般用于中途加入者使用

    private int enterStatus;//你进入这个轮次处于的状态

    /**
     * EWST_DEFAULT = 0; //默认抢唱类型：普通
     * EWST_ACCOMPANY = 1; //带伴奏抢唱
     * EWST_COMMON_OVER_TIME = 2; //普通加时抢唱
     * EWST_ACCOMPANY_OVER_TIME = 3; //带伴奏加时抢唱
     */
    private int wantSingType = EWantSingType.EWST_DEFAULT.getValue();

    @JSONField(name = "CHORoundInfos")
    List<ChorusRoundInfoModel> chorusRoundInfoModels = new ArrayList<>();

    @JSONField(name = "SPKRoundInfos")
    List<SPkRoundInfoModel> sPkRoundInfoModels = new ArrayList<>();

    public GrabRoundInfoModel() {

    }

    @Override
    public int getType() {
        return TYPE_GRAB;
    }


    public HashSet<BLightInfoModel> getbLightInfos() {
        return bLightInfos;
    }

    public void setbLightInfos(HashSet<BLightInfoModel> bLightInfos) {
        this.bLightInfos = bLightInfos;
    }

    public HashSet<MLightInfoModel> getLightInfos() {
        return mLightInfos;
    }

    public void setLightInfos(HashSet<MLightInfoModel> lightInfos) {
        mLightInfos = lightInfos;
    }

    public List<GrabPlayerInfoModel> getPlayUsers() {
        return playUsers;
    }

    public boolean isContainInRoom() {
        for (GrabPlayerInfoModel grabPlayerInfoModel : playUsers) {
            if (grabPlayerInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                return true;
            }
        }

        for (GrabPlayerInfoModel grabPlayerInfoModel : waitUsers) {
            if (grabPlayerInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                return true;
            }
        }

        return false;
    }

    public void setPlayUsers(List<GrabPlayerInfoModel> playUsers) {
        this.playUsers = playUsers;
    }

    public List<GrabPlayerInfoModel> getWaitUsers() {
        return waitUsers;
    }

    public void setWaitUsers(List<GrabPlayerInfoModel> waitUsers) {
        this.waitUsers = waitUsers;
    }

    public GrabSkrResourceModel getSkrResource() {
        return skrResource;
    }

    public void setSkrResource(GrabSkrResourceModel skrResource) {
        this.skrResource = skrResource;
    }

    public HashSet<WantSingerInfo> getWantSingInfos() {
        return wantSingInfos;
    }

    public void setWantSingInfos(HashSet<WantSingerInfo> wantSingInfos) {
        this.wantSingInfos = wantSingInfos;
    }

    public HashSet<MLightInfoModel> getMLightInfos() {
        return mLightInfos;
    }

    public void setMLightInfos(HashSet<MLightInfoModel> noPassSingInfos) {
        this.mLightInfos = noPassSingInfos;
    }

    public int getResultType() {
        return resultType;
    }

    public void setResultType(int resultType) {
        this.resultType = resultType;
    }

    public void updateStatus(boolean notify, int statusGrab) {
        if (getStatusPriority(status) < getStatusPriority(statusGrab)) {
            int old = status;
            status = statusGrab;
            if (notify) {
                EventBus.getDefault().post(new GrabRoundStatusChangeEvent(this, old));
            }
        }
    }

    /**
     * 重排一下状态机的优先级
     *
     * @param status
     * @return
     */
    int getStatusPriority(int status) {
        if (status == EQRoundStatus.QRS_END.getValue()) {
            return 1000;
        } else {
            return status;
        }
    }

    /**
     * 一唱到底使用 抢唱
     */
    public void addGrabUid(boolean notify, WantSingerInfo wantSingerInfo) {
        if (!wantSingInfos.contains(wantSingerInfo)) {
            wantSingInfos.add(wantSingerInfo);
            if (notify) {
                SomeOneGrabEvent event = new SomeOneGrabEvent(wantSingerInfo, this);
                EventBus.getDefault().post(event);
            }
        }
    }

    /**
     * 一唱到底使用 灭灯
     */
    public boolean addLightOffUid(boolean notify, MLightInfoModel noPassingInfo) {
        if (status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue() && getsPkRoundInfoModels().size() > 1) {
            return getsPkRoundInfoModels().get(0).addLightOffUid(notify, noPassingInfo, this);
        }
        if (status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue() && getsPkRoundInfoModels().size() > 1) {
            return getsPkRoundInfoModels().get(1).addLightOffUid(notify, noPassingInfo, this);
        }
        if (!mLightInfos.contains(noPassingInfo)) {
            mLightInfos.add(noPassingInfo);
            if (notify) {
                GrabSomeOneLightOffEvent event = new GrabSomeOneLightOffEvent(noPassingInfo.getUserID(), this);
                EventBus.getDefault().post(event);
            }
            return true;
        }

        return false;
    }

    /**
     * 一唱到底使用 爆灯
     */
    public boolean addLightBurstUid(boolean notify, BLightInfoModel bLightInfoModel) {
        if (status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue() && getsPkRoundInfoModels().size() > 1) {
            return getsPkRoundInfoModels().get(0).addLightBurstUid(notify, bLightInfoModel, this);
        }
        if (status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue() && getsPkRoundInfoModels().size() > 1) {
            return getsPkRoundInfoModels().get(1).addLightBurstUid(notify, bLightInfoModel, this);
        }
        if (!bLightInfos.contains(bLightInfoModel)) {
            bLightInfos.add(bLightInfoModel);
            if (notify) {
                GrabSomeOneLightBurstEvent event = new GrabSomeOneLightBurstEvent(bLightInfoModel.getUserID(), this);
                EventBus.getDefault().post(event);
            }
            return true;
        }
        return false;
    }

    public boolean addWaitUser(boolean notify, GrabPlayerInfoModel grabPlayerInfoModel) {
        if (!waitUsers.contains(grabPlayerInfoModel)) {
            waitUsers.add(grabPlayerInfoModel);
            if (notify) {
                SomeOneJoinWaitSeatEvent event = new SomeOneJoinWaitSeatEvent(grabPlayerInfoModel);
                EventBus.getDefault().post(event);
            }
            return true;
        }
        return false;
    }

    public boolean addPlayUser(boolean notify, GrabPlayerInfoModel grabPlayerInfoModel) {
        if (!playUsers.contains(grabPlayerInfoModel)) {
            playUsers.add(grabPlayerInfoModel);
            if (notify) {
                SomeOneJoinPlaySeatEvent event = new SomeOneJoinPlaySeatEvent(grabPlayerInfoModel);
                EventBus.getDefault().post(event);
            }
            return true;
        }
        return false;
    }

    public void updateWaitUsers(List<GrabPlayerInfoModel> l) {
        waitUsers.clear();
        waitUsers.addAll(l);
        EventBus.getDefault().post(new GrabWaitSeatUpdateEvent(waitUsers));
    }

    public void updatePlayUsers(List<GrabPlayerInfoModel> l) {
        playUsers.clear();
        playUsers.addAll(l);
        EventBus.getDefault().post(new GrabPlaySeatUpdateEvent(playUsers));
    }

    /**
     * 一唱到底使用
     */
    public void tryUpdateRoundInfoModel(BaseRoundInfoModel round, boolean notify) {
        if (round == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null");
            return;
        }
        GrabRoundInfoModel roundInfo = (GrabRoundInfoModel) round;
        this.setUserID(roundInfo.getUserID());
        this.setPlaybookID(roundInfo.getPlaybookID());
        this.setRoundSeq(roundInfo.getRoundSeq());
        this.setSingBeginMs(roundInfo.getSingBeginMs());
        this.setSingEndMs(roundInfo.getSingEndMs());
        if (this.getSkrResource() == null) {
            this.setSkrResource(roundInfo.getSkrResource());
        }
        if (this.getMusic() == null) {
            this.setMusic(roundInfo.getMusic());
        }
        for (WantSingerInfo wantSingerInfo : roundInfo.getWantSingInfos()) {
            addGrabUid(notify, wantSingerInfo);
        }
        for (MLightInfoModel m : roundInfo.getMLightInfos()) {
            addLightOffUid(notify, m);
        }
        for (BLightInfoModel m : roundInfo.getbLightInfos()) {
            addLightBurstUid(notify, m);
        }
        // 观众席与玩家席更新，以最新的为准
        {
            boolean needUpdate = false;
            if (playUsers.size() == roundInfo.getPlayUsers().size()) {
                for (int i = 0; i < roundInfo.getPlayUsers().size() && i < playUsers.size(); i++) {
                    GrabPlayerInfoModel infoModel1 = playUsers.get(i);
                    GrabPlayerInfoModel infoModel2 = roundInfo.getPlayUsers().get(i);
                    if (!infoModel1.equals(infoModel2)) {
                        needUpdate = true;
                        break;
                    }
                }
            } else {
                needUpdate = true;
            }
            if (needUpdate) {
                updatePlayUsers(roundInfo.getPlayUsers());
            }
        }

        {
            boolean needUpdate = false;
            if (waitUsers.size() == roundInfo.getWaitUsers().size()) {
                for (int i = 0; i < roundInfo.getWaitUsers().size() && i < waitUsers.size(); i++) {
                    GrabPlayerInfoModel infoModel1 = waitUsers.get(i);
                    GrabPlayerInfoModel infoModel2 = roundInfo.getWaitUsers().get(i);
                    if (!infoModel1.equals(infoModel2)) {
                        needUpdate = true;
                        break;
                    }
                }
            } else {
                needUpdate = true;
            }
            if (needUpdate) {
                updateWaitUsers(roundInfo.getWaitUsers());
            }
        }

        if (roundInfo.getOverReason() > 0) {
            this.setOverReason(roundInfo.getOverReason());
        }
        if (roundInfo.getResultType() > 0) {
            this.setResultType(roundInfo.getResultType());
        }
        this.setWantSingType(roundInfo.getWantSingType());

        // 更新合唱信息
        if (wantSingType == EWantSingType.EWST_CHORUS.getValue()) {
            if (this.getChorusRoundInfoModels().size() <= 1) {
                // 不满足两人通知全量更新
                this.getChorusRoundInfoModels().clear();
                this.getChorusRoundInfoModels().addAll(roundInfo.getChorusRoundInfoModels());
            } else {
                for (int i = 0; i < this.getChorusRoundInfoModels().size() && i < roundInfo.getChorusRoundInfoModels().size(); i++) {
                    ChorusRoundInfoModel chorusRoundInfoModel1 = this.getChorusRoundInfoModels().get(i);
                    ChorusRoundInfoModel chorusRoundInfoModel2 = roundInfo.getChorusRoundInfoModels().get(i);
                    chorusRoundInfoModel1.tryUpdateRoundInfoModel(chorusRoundInfoModel2);
                }
            }
        }

        // 更新pk信息
        if (wantSingType == EWantSingType.EWST_SPK.getValue()) {
            // pk房间
            if (this.getsPkRoundInfoModels().size() <= 1) {
                // 不满足两人通知全量更新
                this.getsPkRoundInfoModels().clear();
                this.getsPkRoundInfoModels().addAll(roundInfo.getsPkRoundInfoModels());
            } else {
                for (int i = 0; i < this.getsPkRoundInfoModels().size() && i < roundInfo.getsPkRoundInfoModels().size(); i++) {
                    SPkRoundInfoModel sPkRoundInfoModel1 = this.getsPkRoundInfoModels().get(i);
                    SPkRoundInfoModel sPkRoundInfoModel2 = roundInfo.getsPkRoundInfoModels().get(i);
                    sPkRoundInfoModel1.tryUpdateRoundInfoModel(sPkRoundInfoModel2, notify, this);
                }
            }
        }
        updateStatus(notify, roundInfo.getStatus());
        return;
    }

    /**
     * 一唱到底合唱某人放弃了演唱
     *
     * @param userID
     */
    public void giveUpInChorus(int userID) {
        for (int i = 0; i < this.getChorusRoundInfoModels().size(); i++) {
            ChorusRoundInfoModel chorusRoundInfoModel = this.getChorusRoundInfoModels().get(i);
            if (chorusRoundInfoModel.getUserID() == userID) {
                if (!chorusRoundInfoModel.isHasGiveUp()) {
                    chorusRoundInfoModel.setHasGiveUp(true);
                    EventBus.getDefault().post(new GrabChorusUserStatusChangeEvent(chorusRoundInfoModel));
                }
            }
        }
    }

    public static GrabRoundInfoModel parseFromRoundInfo(QRoundInfo roundInfo) {
        GrabRoundInfoModel roundInfoModel = new GrabRoundInfoModel();
        roundInfoModel.setUserID(roundInfo.getUserID());
        roundInfoModel.setPlaybookID(roundInfo.getPlaybookID());
        roundInfoModel.setRoundSeq(roundInfo.getRoundSeq());

        roundInfoModel.setSingBeginMs(roundInfo.getSingBeginMs());
        roundInfoModel.setSingEndMs(roundInfo.getSingEndMs());

        // 轮次状态
        roundInfoModel.setStatus(roundInfo.getStatus().getValue());

        for (WantSingInfo wantSingInfo : roundInfo.getWantSingInfosList()) {
            roundInfoModel.addGrabUid(false, WantSingerInfo.parse(wantSingInfo));
        }

        roundInfoModel.setOverReason(roundInfo.getOverReason().getValue());
        roundInfoModel.setResultType(roundInfo.getResultType().getValue());

        SongModel songModel = new SongModel();
        songModel.parse(roundInfo.getMusic());
        roundInfoModel.setMusic(songModel);

        for (QBLightMsg m : roundInfo.getBLightInfosList()) {
            roundInfoModel.addLightBurstUid(false, BLightInfoModel.parse(m));
        }

        for (QMLightMsg m : roundInfo.getMLightInfosList()) {
            roundInfoModel.addLightOffUid(false, MLightInfoModel.parse(m));
        }

        GrabSkrResourceModel grabSkrResourceModel = GrabSkrResourceModel.parse(roundInfo.getSkrResource());
        roundInfoModel.setSkrResource(grabSkrResourceModel);

        // 观众席
        for (OnlineInfo m : roundInfo.getWaitUsersList()) {
            GrabPlayerInfoModel grabPlayerInfoModel = GrabPlayerInfoModel.parse(m);
            roundInfoModel.addWaitUser(false, grabPlayerInfoModel);
        }

        // 玩家
        for (OnlineInfo m : roundInfo.getPlayUsersList()) {
            GrabPlayerInfoModel grabPlayerInfoModel = GrabPlayerInfoModel.parse(m);
            roundInfoModel.addPlayUser(false, grabPlayerInfoModel);
        }
        // 想唱类型
        roundInfoModel.setWantSingType(roundInfo.getWantSingType().getValue());

        for (QCHOInnerRoundInfo qchoInnerRoundInfo : roundInfo.getCHORoundInfosList()) {
            ChorusRoundInfoModel chorusRoundInfoModel = ChorusRoundInfoModel.parse(qchoInnerRoundInfo);
            roundInfoModel.getChorusRoundInfoModels().add(chorusRoundInfoModel);
        }

        for (QSPKInnerRoundInfo qspkInnerRoundInfo : roundInfo.getSPKRoundInfosList()) {
            SPkRoundInfoModel pkRoundInfoModel = SPkRoundInfoModel.parse(qspkInnerRoundInfo);
            roundInfoModel.getsPkRoundInfoModels().add(pkRoundInfoModel);
        }
        return roundInfoModel;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean addUser(boolean b, GrabPlayerInfoModel playerInfoModel) {
        if (playerInfoModel.getRole() == GrabPlayerInfoModel.ROLE_PLAY) {
            return addPlayUser(b, playerInfoModel);
        } else if (playerInfoModel.getRole() == GrabPlayerInfoModel.ROLE_WAIT) {
            return addWaitUser(b, playerInfoModel);
        }
        return false;
    }

    public void removeUser(boolean notify, int uid) {
        for (int i = 0; i < playUsers.size(); i++) {
            GrabPlayerInfoModel infoModel = playUsers.get(i);
            if (infoModel.getUserID() == uid) {
                playUsers.remove(infoModel);
                if (notify) {
                    EventBus.getDefault().post(new SomeOneLeavePlaySeatEvent(infoModel));
                }
                break;
            }
        }
        for (int i = 0; i < waitUsers.size(); i++) {
            GrabPlayerInfoModel infoModel = waitUsers.get(i);
            if (infoModel.getUserID() == uid) {
                waitUsers.remove(infoModel);
                if (notify) {
                    EventBus.getDefault().post(new SomeOneLeaveWaitSeatEvent(infoModel));
                }
                break;
            }
        }
    }

    public boolean isParticipant() {
        return isParticipant;
    }

    public void setParticipant(boolean participant) {
        isParticipant = participant;
    }

    public int getElapsedTimeMs() {
        return elapsedTimeMs;
    }

    public void setElapsedTimeMs(int elapsedTimeMs) {
        this.elapsedTimeMs = elapsedTimeMs;
    }

    public int getEnterStatus() {
        return enterStatus;
    }

    public boolean isEnterInSingStatus() {
        return enterStatus == EQRoundStatus.QRS_SING.getValue()
                || enterStatus == EQRoundStatus.QRS_CHO_SING.getValue()
                || enterStatus == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()
                || enterStatus == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()
                ;
    }

    public void setEnterStatus(int enterStatus) {
        this.enterStatus = enterStatus;
    }

    public int getWantSingType() {
        return wantSingType;
    }

    public boolean isChallengeRound() {
        return wantSingType == EWantSingType.EWST_COMMON_OVER_TIME.getValue() || wantSingType == EWantSingType.EWST_ACCOMPANY_OVER_TIME.getValue();
    }

    public boolean isAccRound() {
        return wantSingType == EWantSingType.EWST_ACCOMPANY.getValue() || wantSingType == EWantSingType.EWST_ACCOMPANY_OVER_TIME.getValue();
    }

    public void setWantSingType(int wantSingType) {
        this.wantSingType = wantSingType;
    }

    public List<ChorusRoundInfoModel> getChorusRoundInfoModels() {
        return chorusRoundInfoModels;
    }

    public void setChorusRoundInfoModels(List<ChorusRoundInfoModel> chorusRoundInfoModels) {
        this.chorusRoundInfoModels = chorusRoundInfoModels;
    }

    public List<SPkRoundInfoModel> getsPkRoundInfoModels() {
        return sPkRoundInfoModels;
    }

    public void setsPkRoundInfoModels(List<SPkRoundInfoModel> sPkRoundInfoModels) {
        this.sPkRoundInfoModels = sPkRoundInfoModels;
    }

    /**
     * 判断当前是否是自己的演唱轮次
     *
     * @return
     */
    public boolean singBySelfNow() {
        if (getStatus() == EQRoundStatus.QRS_SING.getValue()) {
            return getUserID() == MyUserInfoManager.getInstance().getUid();
        } else if (getStatus() == EQRoundStatus.QRS_CHO_SING.getValue()) {
            for (ChorusRoundInfoModel roundInfoModel : chorusRoundInfoModels) {
                if (roundInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                    return true;
                }
            }
        } else if (getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
            if (getsPkRoundInfoModels().size() > 0) {
                return getsPkRoundInfoModels().get(0).getUserID() == MyUserInfoManager.getInstance().getUid();
            }
        } else if (getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            if (getsPkRoundInfoModels().size() > 1) {
                return getsPkRoundInfoModels().get(1).getUserID() == MyUserInfoManager.getInstance().getUid();
            }
        }
        return false;
    }

    /**
     * 判断是各种演唱阶段
     *
     * @return
     */
    public boolean isSingStatus() {
        if (status == EQRoundStatus.QRS_SING.getValue()
                || status == EQRoundStatus.QRS_CHO_SING.getValue()
                || status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()
                || status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            return true;
        }
        return false;
    }

    /**
     * 是否是合唱轮次
     *
     * @return
     */
    public boolean isChorusRound() {
        return status == EQRoundStatus.QRS_CHO_SING.getValue();
    }

    /**
     * 是否是pk轮次
     *
     * @return
     */
    public boolean isPKRound() {
        return status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()
                || status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue();
    }

    /**
     * 返回当前演唱者的id信息
     *
     * @return
     */
    public List<Integer> getSingUserIds() {
        List<Integer> singerUserIds = new ArrayList<>();
        if (isPKRound()) {
            for (SPkRoundInfoModel infoModel : getsPkRoundInfoModels()) {
                singerUserIds.add(infoModel.getUserID());
            }
        } else if (isChorusRound()) {
            for (ChorusRoundInfoModel infoModel : getChorusRoundInfoModels()) {
                singerUserIds.add(infoModel.getUserID());
            }
        } else {
            singerUserIds.add(getUserID());
        }
        return singerUserIds;
    }

    public int getSingTotalMs() {
        /**
         * 该轮次的总时间，之前用的是歌曲内的总时间，但是不灵活，现在都放在服务器的轮次信息的 begin 和 end 里
         *
         */
        int totalTs = 0;
        /**
         * pk第一轮和第二轮的演唱时间 和 歌曲截取的部位不一样
         */
        if (getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue() && getsPkRoundInfoModels().size() > 1) {
            totalTs = getsPkRoundInfoModels().get(1).getSingEndMs() - getsPkRoundInfoModels().get(1).getSingBeginMs();
        } else if (getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue() && getsPkRoundInfoModels().size() > 0) {
            totalTs = getsPkRoundInfoModels().get(0).getSingEndMs() - getsPkRoundInfoModels().get(0).getSingBeginMs();
        } else {
            totalTs = getSingEndMs() - getSingBeginMs();
        }
        if (totalTs <= 0) {
            MyLog.d(TAG, "playLyric" + " totalTs时间不合法,做矫正");
            if (getWantSingType() == 0) {
                totalTs = 20 * 1000;
            } else if (getWantSingType() == 1) {
                totalTs = 30 * 1000;
            } else if (getWantSingType() == 2) {
                totalTs = 40 * 1000;
            } else if (getWantSingType() == 3) {
                totalTs = 50 * 1000;
            }
        }
        return totalTs;
    }

    @Override
    public String toString() {
        return "GrabRoundInfoModel{" +
                "roundSeq=" + roundSeq +
                ", status=" + status +
                ", userID=" + userID +
                ", playbookID=" + playbookID +
                ", songModel=" + (music == null ? "" : music.toSimpleString()) +
                ", singBeginMs=" + singBeginMs +
                ", singEndMs=" + singEndMs +
//                ", startTs=" + startTs +
//                ", endTs=" + endTs +
//                ", sysScore=" + sysScore +
                ", hasSing=" + hasSing +
                ", overReason=" + overReason +
                ", bLightInfos=" + bLightInfos +
                ", mLightInfos=" + mLightInfos +
                ", playUsers=" + playUsers +
                ", waitUsers=" + waitUsers +
//                ", skrResource=" + skrResource +
                ", wantSingInfos=" + wantSingInfos +
                ", resultType=" + resultType +
                ", isParticipant=" + isParticipant +
                ", elapsedTimeMs=" + elapsedTimeMs +
                ", enterStatus=" + enterStatus +
                '}';
    }


}
