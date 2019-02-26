package com.module.playways.rank.prepare.model;

import com.common.log.MyLog;
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent;
import com.module.playways.grab.room.event.SomeOneGrabEvent;
import com.module.playways.grab.room.event.SomeOneLightOffEvent;
import com.module.playways.grab.room.model.NoPassingInfo;
import com.module.playways.grab.room.model.WantSingerInfo;
import com.module.playways.rank.room.event.PkSomeOneBurstLightEvent;
import com.module.playways.rank.room.event.PkSomeOneLightOffEvent;
import com.module.playways.rank.room.model.BLightInfoModel;
import com.module.playways.rank.room.model.MLightInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.zq.live.proto.Room.BLightInfo;
import com.zq.live.proto.Room.MLightInfo;
import com.zq.live.proto.Room.NoPassSingInfo;
import com.zq.live.proto.Room.QRoundInfo;
import com.zq.live.proto.Room.RoundInfo;
import com.zq.live.proto.Room.WantSingInfo;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.HashSet;

public abstract class RoundInfoModel implements Serializable {
    public final static String TAG = "RoundInfoModel";
    public static final int TYPE_RANK = 1;
    public static final int TYPE_GRAB = 2;

    public static final int STATUS_INIT = 1;
    public static final int STATUS_GRAB = 2;
    public static final int STATUS_SING = 3;
    public static final int STATUS_OVER = 4;
    /**
     * userID : 7
     * playbookID : 1
     * roundSeq : 1
     * singBeginMs : 3000
     * singEndMs : 341000
     */
    protected int type = TYPE_RANK;
    protected int userID;
    protected int playbookID;   //songModelId
    protected SongModel songModel;//本轮次要唱的歌儿的详细信息
    protected int roundSeq;
    protected int singBeginMs;
    protected int singEndMs;
    protected long startTs;// 开始时间，服务器的
    protected long endTs;// 结束时间，服务器的
    protected int sysScore;//本轮系统打分，先搞个默认60分
    protected boolean hasSing = false;

    /**
     * 一唱到底 结束原因
     * 0未知
     * 1上个轮次结束
     * 2没人抢唱
     * 3当前玩家退出
     * 4多人灭灯
     * <p>
     * 排位赛 结束原因
     * 0 未知
     * 1 正常
     * 2 玩家退出
     * 3 多人灭灯
     */
    protected int overReason; // 结束的原因

    public RoundInfoModel() {

    }

    public RoundInfoModel(int type) {
        this.type = type;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOverReason() {
        return overReason;
    }

    public void setOverReason(int overReason) {
        this.overReason = overReason;
    }

    public SongModel getSongModel() {
        return songModel;
    }

    public void setSongModel(SongModel songModel) {
        this.songModel = songModel;
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

    public int getRoundSeq() {
        return roundSeq;
    }

    public void setRoundSeq(int roundSeq) {
        this.roundSeq = roundSeq;
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

    @Override
    public boolean equals(Object o) {
        RoundInfoModel that = (RoundInfoModel) o;
        if (that == null) {
            return false;
        }

        if (this.type != that.type) {
            return false;
        }
        if (roundSeq != that.roundSeq) {
            return false;
        }
        if (playbookID != that.playbookID) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + roundSeq;
        result = 31 * result + playbookID;
        return result;
    }

    public abstract void  tryUpdateRoundInfoModel(RoundInfoModel round, boolean notify);

    @Override
    public String toString() {
        return "RoundInfoModel{" +
                "type=" + type +
                ", userID=" + userID +
                ", playbookID=" + playbookID +
//                ", songModel=" + songModel.getItemName() +
                ", roundSeq=" + roundSeq +
                ", singBeginMs=" + singBeginMs +
                ", singEndMs=" + singEndMs +
                ", startTs=" + startTs +
                ", endTs=" + endTs +
                ", sysScore=" + sysScore +
                ", hasSing=" + hasSing +
                ", overReason=" + overReason +
                '}';
    }

}