package com.module.playways.doubleplay.model;

import java.io.Serializable;

public class DoubleEndRoomModel implements Serializable {

    /**
     * chatDurTime : 0
     * combineRoomCloseReasonDesc : string
     * createType : UnKnown
     * exitUserID : 0
     * isFollow : true
     * isFriend : true
     * noResponseUserID : 0
     * reason : DRCR_UNKNOWN
     * todayResTimes : 0
     */

    private int chatDurTime;
    private String combineRoomCloseReasonDesc;
    private String createType;
    private int exitUserID;
    private boolean isFollow;
    private boolean isFriend;
    private int noResponseUserID;
    private String reason;
    private int todayResTimes;

    public int getChatDurTime() {
        return chatDurTime;
    }

    public void setChatDurTime(int chatDurTime) {
        this.chatDurTime = chatDurTime;
    }

    public String getCombineRoomCloseReasonDesc() {
        return combineRoomCloseReasonDesc;
    }

    public void setCombineRoomCloseReasonDesc(String combineRoomCloseReasonDesc) {
        this.combineRoomCloseReasonDesc = combineRoomCloseReasonDesc;
    }

    public String getCreateType() {
        return createType;
    }

    public void setCreateType(String createType) {
        this.createType = createType;
    }

    public int getExitUserID() {
        return exitUserID;
    }

    public void setExitUserID(int exitUserID) {
        this.exitUserID = exitUserID;
    }

    public boolean isIsFollow() {
        return isFollow;
    }

    public void setIsFollow(boolean isFollow) {
        this.isFollow = isFollow;
    }

    public boolean isIsFriend() {
        return isFriend;
    }

    public void setIsFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }

    public int getNoResponseUserID() {
        return noResponseUserID;
    }

    public void setNoResponseUserID(int noResponseUserID) {
        this.noResponseUserID = noResponseUserID;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getTodayResTimes() {
        return todayResTimes;
    }

    public void setTodayResTimes(int todayResTimes) {
        this.todayResTimes = todayResTimes;
    }
}
