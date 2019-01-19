package com.module.playways.rank.prepare.model;

import com.common.log.MyLog;
import com.module.playways.rank.song.model.SongModel;
import com.zq.live.proto.Room.QRoundInfo;
import com.zq.live.proto.Room.RoundInfo;

import java.io.Serializable;
import java.util.Set;

public class RoundInfoModel implements Serializable {
    public static final int TYPE_RANK = 1;
    public static final int TYPE_GRAB = 2;
    /**
     * userID : 7
     * playbookID : 1
     * roundSeq : 1
     * singBeginMs : 3000
     * singEndMs : 341000
     */
    private int type = TYPE_RANK;
    private int userID;
    private int playbookID;   //songModelId
    private int roundSeq;
    private int singBeginMs;
    private int singEndMs;
    private long startTs;// 开始时间，服务器的
    private long endTs;// 结束时间，服务器的
    private int sysScore;//本轮系统打分，先搞个默认60分
    private boolean hasSing = false;

    //已经抢了的人
    private Set<Integer> hasGrabUserSet;

    //已经灭灯的人
    private Set<Integer> hasLightOffUserSet;

    //本轮次要唱的歌儿的详细信息
    private SongModel songModel;

    public RoundInfoModel(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public SongModel getSongModel() {
        return songModel;
    }

    public void setSongModel(SongModel songModel) {
        this.songModel = songModel;
    }

    public Set<Integer> getHasGrabUserSet() {
        return hasGrabUserSet;
    }

    public void setHasGrabUserSet(Set<Integer> hasGrabUserSet) {
        this.hasGrabUserSet = hasGrabUserSet;
    }

    public Set<Integer> getHasLightOffUserSet() {
        return hasLightOffUserSet;
    }

    public void setHasLightOffUserSet(Set<Integer> hasLightOffUserSet) {
        this.hasLightOffUserSet = hasLightOffUserSet;
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

    public static RoundInfoModel parseFromRoundInfo(RoundInfo roundInfo) {
        RoundInfoModel roundInfoModel = new RoundInfoModel(TYPE_RANK);
        roundInfoModel.setUserID(roundInfo.getUserID());
        roundInfoModel.setPlaybookID(roundInfo.getPlaybookID());
        roundInfoModel.setRoundSeq(roundInfo.getRoundSeq());
        roundInfoModel.setSingBeginMs(roundInfo.getSingBeginMs());
        roundInfoModel.setSingEndMs(roundInfo.getSingEndMs());
        return roundInfoModel;
    }

    public static RoundInfoModel parseFromRoundInfo(QRoundInfo roundInfo) {
        RoundInfoModel roundInfoModel = new RoundInfoModel(TYPE_GRAB);
        roundInfoModel.setUserID(roundInfo.getUserID());
        roundInfoModel.setRoundSeq(roundInfo.getRoundSeq());
        roundInfoModel.setSingBeginMs(roundInfo.getSingBeginMs());
        roundInfoModel.setSingEndMs(roundInfo.getSingEndMs());
        return roundInfoModel;
    }

    public void update(RoundInfoModel roundInfo) {
        if (roundInfo == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null");
            return;
        }

        this.setUserID(roundInfo.getUserID());
        this.setPlaybookID(roundInfo.getPlaybookID());
        this.setRoundSeq(roundInfo.getRoundSeq());
        this.setSingBeginMs(roundInfo.getSingBeginMs());
        this.setSingEndMs(roundInfo.getSingEndMs());
        return;
    }

    @Override
    public boolean equals(Object o) {
        RoundInfoModel that = (RoundInfoModel) o;
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

    @Override
    public String toString() {
        return "RoundInfoModel{" +
                "userID=" + userID +
                ", playbookID=" + playbookID +
                ", roundSeq=" + roundSeq +
                ", singBeginMs=" + singBeginMs +
                ", singEndMs=" + singEndMs +
                ", startTs=" + startTs +
                ", endTs=" + endTs +
                ", sysScore=" + sysScore +
                ", hasSing=" + hasSing +
                ", songModel=" + songModel +
                '}';
    }
}