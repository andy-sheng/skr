package com.module.playways.rank.room.model;

import com.common.log.MyLog;
import com.zq.live.proto.Room.VoteInfo;

import java.util.List;

// 投票打分信息
public class VoteInfoModel {
    /**
     * userID : 122
     * itemID : 1
     * isEscape : true
     * sysScore : 90
     * voter : [123]
     */

    private int userID;
    private int itemID;
    private boolean isEscape;
    private int sysScore;
    private int rank;
    private List<Integer> voter;

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

    public boolean isIsEscape() {
        return isEscape;
    }

    public void setIsEscape(boolean isEscape) {
        this.isEscape = isEscape;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void parse(VoteInfo voteInfo) {
        if (voteInfo == null) {
            MyLog.e("VoteInfoModel VoteInfo == null");
            return;
        }

        this.setRank(voteInfo.getRank());
        this.setUserID(voteInfo.getUserID());
        this.setItemID(voteInfo.getItemID());
        this.setIsEscape(voteInfo.getIsEscape());
        this.setSysScore(voteInfo.getSysScore());
        this.setVoter(voteInfo.getVoterList());

        return;
    }
}
