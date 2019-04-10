package com.module.playways.grab.room.model;

import com.common.log.MyLog;
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
import com.module.playways.rank.prepare.model.BaseRoundInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.zq.live.proto.Room.OnlineInfo;
import com.zq.live.proto.Room.QBLightMsg;
import com.zq.live.proto.Room.QMLightMsg;
import com.zq.live.proto.Room.QRoundInfo;
import com.zq.live.proto.Room.WantSingInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GrabRoundInfoModel extends BaseRoundInfoModel {
    public static final int STATUS_INIT = 1;
    public static final int STATUS_GRAB = 2;
    public static final int STATUS_SING = 3;
    public static final int STATUS_OVER = 4;

    /* 一唱到底使用 */
    private int status = STATUS_INIT;// 轮次状态，在一唱到底中使用

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
    private int wantSingType;

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
        if (status < statusGrab) {
            int old = status;
            status = statusGrab;
            if (notify) {
                EventBus.getDefault().post(new GrabRoundStatusChangeEvent(this, old));
            }
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
        updateStatus(notify, roundInfo.getStatus());

        return;
    }


    public static GrabRoundInfoModel parseFromRoundInfo(QRoundInfo roundInfo) {
        GrabRoundInfoModel roundInfoModel = new GrabRoundInfoModel();
        roundInfoModel.setUserID(roundInfo.getUserID());
        roundInfoModel.setPlaybookID(roundInfo.getPlaybookID());
        roundInfoModel.setRoundSeq(roundInfo.getRoundSeq());

        roundInfoModel.setSingBeginMs(roundInfo.getSingBeginMs());
        roundInfoModel.setSingEndMs(roundInfo.getSingEndMs());

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

        roundInfoModel.setWantSingType(roundInfo.getWantSingType().getValue());
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

    public void setEnterStatus(int enterStatus) {
        this.enterStatus = enterStatus;
    }

    public int getWantSingType() {
        return wantSingType;
    }

    public void setWantSingType(int wantSingType) {
        this.wantSingType = wantSingType;
    }

    @Override
    public String toString() {
        return "GrabRoundInfoModel{" +
                "roundSeq=" + roundSeq +
                ", status=" + status +
                ", userID=" + userID +
                ", playbookID=" + playbookID +
                ", songModel=" + (music == null ? "" : music.toSimpleString()) +
//                ", singBeginMs=" + singBeginMs +
//                ", singEndMs=" + singEndMs +
//                ", startTs=" + startTs +
//                ", endTs=" + endTs +
//                ", sysScore=" + sysScore +
                ", hasSing=" + hasSing +
                ", overReason=" + overReason +
                ", bLightInfos=" + bLightInfos +
                ", mLightInfos=" + mLightInfos +
                ", playUsers=" + playUsers +
                ", waitUsers=" + waitUsers +
                ", skrResource=" + skrResource +
                ", wantSingInfos=" + wantSingInfos +
                ", resultType=" + resultType +
                ", isParticipant=" + isParticipant +
                ", elapsedTimeMs=" + elapsedTimeMs +
                ", enterStatus=" + enterStatus +
                '}';
    }
}
