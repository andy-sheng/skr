package com.module.playways.room.prepare.model;

import com.module.playways.room.song.model.SongModel;

import java.io.Serializable;

public abstract class BaseRoundInfoModel implements Serializable {
    public final String TAG = "RoundInfoModel";
    public static final int TYPE_RANK = 1;
    public static final int TYPE_GRAB = 2;

    protected int userID;// 本人在演唱的人
    protected int roundSeq;// 本局轮次
    protected int playbookID;   //songModelId
    protected SongModel music;//本轮次要唱的歌儿的详细信息
    protected int singBeginMs; // 轮次开始时间
    protected int singEndMs; // 轮次结束时间
    protected long startTs;// 开始时间，服务器的
    protected long endTs;// 结束时间，服务器的
    protected int sysScore;//本轮系统打分，先搞个默认60分
    protected boolean hasSing = false;// 是否已经在演唱，依据时引擎等回调，不是作为是否演唱阶段的依据

    /**
     * 一唱到底 结束原因
     * 0未知
     * 1上个轮次结束
     * 2没人抢唱
     * 3当前玩家退出
     * 4多人灭灯
     * 5自己放弃演唱
     * <p>
     * 排位赛 结束原因
     * 0 未知
     * 1 正常
     * 2 玩家退出
     * 3 多人灭灯
     * 4
     */
    protected int overReason; // 结束的原因

    public BaseRoundInfoModel() {

    }

    public abstract int getType();

    public int getOverReason() {
        return overReason;
    }

    public void setOverReason(int overReason) {
        this.overReason = overReason;
    }

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
        BaseRoundInfoModel that = (BaseRoundInfoModel) o;
        if (that == null) {
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
        int result = 0;
        result = 31 * result + roundSeq;
        result = 31 * result + playbookID;
        return result;
    }

    public abstract void tryUpdateRoundInfoModel(BaseRoundInfoModel round, boolean notify);

    @Override
    public String toString() {
        return "RoundInfoModel{" +
                "type=" + getType() +
                ", userID=" + userID +
                ", playbookID=" + playbookID +
                ", songModel=" + music +
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

    public int getDuration() {
        return singEndMs - singBeginMs;
    }
}