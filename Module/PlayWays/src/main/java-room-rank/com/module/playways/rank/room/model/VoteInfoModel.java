package com.module.playways.rank.room.model;

import com.common.log.MyLog;
import com.zq.live.proto.Room.VoteInfo;

import java.io.Serializable;
import java.util.List;

// 投票打分信息
public class VoteInfoModel implements Serializable {
    /**
     * userID : 122
     * itemID : 1
     * isEscape : true
     * sysScore : 90
     * voter : [123]
     */

    private int userID;
    private int itemID;
    private int rank;
    private boolean sysVote;
    private int sysScore;
    private List<Integer> voter;
    private boolean isEscape;
    private boolean hasVote;
    private List<Integer> otherVoters;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isSysVote() {
        return sysVote;
    }

    public void setSysVote(boolean sysVote) {
        this.sysVote = sysVote;
    }

    public int getSysScore() {
        return sysScore;
    }

    public void setSysScore(int sysScore) {
        this.sysScore = sysScore;
    }

    public List<Integer> getVoter() {
        return voter;
    }

    public void setVoter(List<Integer> voter) {
        this.voter = voter;
    }

    public boolean isEscape() {
        return isEscape;
    }

    public void setEscape(boolean escape) {
        isEscape = escape;
    }

    public boolean isHasVote() {
        return hasVote;
    }

    public void setHasVote(boolean hasVote) {
        this.hasVote = hasVote;
    }

    public List<Integer> getOtherVoters() {
        return otherVoters;
    }

    public void setOtherVoters(List<Integer> otherVoters) {
        this.otherVoters = otherVoters;
    }

    public void parse(VoteInfo voteInfo) {
        if (voteInfo == null) {
            MyLog.e("VoteInfoModel VoteInfo == null");
            return;
        }

        this.setUserID(voteInfo.getUserID());
        this.setItemID(voteInfo.getItemID());
        this.setRank(voteInfo.getRank());
        this.setSysVote(voteInfo.getSysVote());
        this.setSysScore(voteInfo.getSysScore());
        this.setVoter(voteInfo.getVoterList());
        this.setEscape(voteInfo.getIsEscape());
        this.setHasVote(voteInfo.getHasVote());
        this.setOtherVoters(voteInfo.getOtherVotersList());
        return;
    }

    @Override
    public String toString() {
        return "VoteInfoModel{" +
                "userID=" + userID +
                ", itemID=" + itemID +
                ", rank=" + rank +
                ", sysVote=" + sysVote +
                ", sysScore=" + sysScore +
                ", voter=" + voter +
                ", isEscape=" + isEscape +
                ", hasVote=" + hasVote +
                ", otherVoters=" + otherVoters +
                '}';
    }
}
