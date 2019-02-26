package com.module.playways.rank.prepare.model;

import com.common.log.MyLog;
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent;
import com.module.playways.grab.room.event.SomeOneGrabEvent;
import com.module.playways.grab.room.event.SomeOneLightOffEvent;
import com.module.playways.grab.room.model.NoPassingInfo;
import com.module.playways.grab.room.model.WantSingerInfo;
import com.zq.live.proto.Room.NoPassSingInfo;
import com.zq.live.proto.Room.QRoundInfo;
import com.zq.live.proto.Room.WantSingInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;

public class GrabRoundInfoModel extends RoundInfoModel {
    /* 一唱到底使用 */
    private int status = STATUS_INIT;// 轮次状态，在一唱到底中使用

    private HashSet<WantSingerInfo> wantSingInfos = new HashSet<>(); //已经抢了的人

    private HashSet<NoPassingInfo> noPassSingInfos = new HashSet<>();//已经灭灯的人, 一唱到底

    //0未知
    //1有种优秀叫一唱到底（全部唱完）
    //2有种结束叫刚刚开始（t<30%）
    //3有份悲伤叫都没及格(30%<=t <60%)
    //4有种遗憾叫明明可以（60%<=t<90%）
    //5有种可惜叫我觉得你行（90%<=t<=100%)
    private int resultType; // 结果类型

    public GrabRoundInfoModel() {

    }

    public GrabRoundInfoModel(int type) {
        this.type = type;
    }

    public HashSet<WantSingerInfo> getWantSingInfos() {
        return wantSingInfos;
    }

    public void setWantSingInfos(HashSet<WantSingerInfo> wantSingInfos) {
        this.wantSingInfos = wantSingInfos;
    }

    public HashSet<NoPassingInfo> getNoPassSingInfos() {
        return noPassSingInfos;
    }

    public void setNoPassSingInfos(HashSet<NoPassingInfo> noPassSingInfos) {
        this.noPassSingInfos = noPassSingInfos;
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
     * 一唱到底使用
     */
    public void addGrabUid(boolean notify, WantSingerInfo wantSingerInfo) {
        if (!wantSingInfos.contains(wantSingerInfo)) {
            wantSingInfos.add(wantSingerInfo);
            if (notify) {
                SomeOneGrabEvent event = new SomeOneGrabEvent(wantSingerInfo.getUserID(), this);
                EventBus.getDefault().post(event);
            }
        }
    }

    /**
     * 一唱到底使用
     */
    public void addLightOffUid(boolean notify, NoPassingInfo noPassingInfo) {
        if (!noPassSingInfos.contains(noPassingInfo)) {
            noPassSingInfos.add(noPassingInfo);
            if (notify) {
                SomeOneLightOffEvent event = new SomeOneLightOffEvent(noPassingInfo.getUserID(), this);
                EventBus.getDefault().post(event);
            }
        }
    }

    /**
     * 一唱到底使用
     */
    public void tryUpdateRoundInfoModel(RoundInfoModel round, boolean notify) {
        if (round == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null");
            return;
        }
        GrabRoundInfoModel roundInfo = (GrabRoundInfoModel)round;
        this.setUserID(roundInfo.getUserID());
        this.setPlaybookID(roundInfo.getPlaybookID());
        this.setRoundSeq(roundInfo.getRoundSeq());
        this.setSingBeginMs(roundInfo.getSingBeginMs());
        this.setSingEndMs(roundInfo.getSingEndMs());
        for (WantSingerInfo wantSingerInfo : roundInfo.getWantSingInfos()) {
            addGrabUid(notify, wantSingerInfo);
        }
        for (NoPassingInfo noPassingInfo : roundInfo.getNoPassSingInfos()) {
            addLightOffUid(notify, noPassingInfo);
        }
        if (roundInfo.getOverReason() > 0) {
            this.setOverReason(roundInfo.getOverReason());
        }
        if (roundInfo.getResultType() > 0) {
            this.setResultType(roundInfo.getResultType());
        }
        updateStatus(notify, roundInfo.getStatus());
        return;
    }

    /**
     * 一唱到底使用
     */
    public static GrabRoundInfoModel parseFromRoundInfo(QRoundInfo roundInfo) {
        GrabRoundInfoModel roundInfoModel = new GrabRoundInfoModel(TYPE_GRAB);
        roundInfoModel.setUserID(roundInfo.getUserID());
        roundInfoModel.setPlaybookID(roundInfo.getPlaybookID());
        roundInfoModel.setRoundSeq(roundInfo.getRoundSeq());
        roundInfoModel.setSingBeginMs(roundInfo.getSingBeginMs());
        roundInfoModel.setSingEndMs(roundInfo.getSingEndMs());
        roundInfoModel.setStatus(roundInfo.getStatus().getValue());
        for (WantSingInfo wantSingInfo : roundInfo.getWantSingInfosList()) {
            roundInfoModel.addGrabUid(false, WantSingerInfo.parse(wantSingInfo));
        }
        for (NoPassSingInfo noPassSingInfo : roundInfo.getNoPassSingInfosList()) {
            roundInfoModel.addLightOffUid(false, NoPassingInfo.parse(noPassSingInfo));
        }
        roundInfoModel.setOverReason(roundInfo.getOverReason().getValue());
        roundInfoModel.setResultType(roundInfo.getResultType().getValue());
        return roundInfoModel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
