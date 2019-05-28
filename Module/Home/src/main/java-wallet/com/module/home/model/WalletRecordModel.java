package com.module.home.model;

import java.io.Serializable;

public class WalletRecordModel implements Serializable {
    /**
     * recordID : 1
     * action : 1
     * incrEvent : 1
     * decrEvent : 0
     * changeAmount : +50.00
     * remark : 达到铂金I段位
     * dateTime : 2019-02-14
     * failedMsg :
     * eventDesc : 段位升级奖励1
     */

    private String recordID;
    private int action;
    private int incrEvent;
    private int decrEvent;
    private String changeAmount;   //金额
    private String remark;         //原因
    private String dateTime;       //时间
    private String failedMsg;
    private String eventDesc;

    public String getRecordID() {
        return recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getIncrEvent() {
        return incrEvent;
    }

    public void setIncrEvent(int incrEvent) {
        this.incrEvent = incrEvent;
    }

    public int getDecrEvent() {
        return decrEvent;
    }

    public void setDecrEvent(int decrEvent) {
        this.decrEvent = decrEvent;
    }

    public String getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(String changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getFailedMsg() {
        return failedMsg;
    }

    public void setFailedMsg(String failedMsg) {
        this.failedMsg = failedMsg;
    }

    public String getEventDesc() {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
    }

    @Override
    public String toString() {
        return "WalletRecordModel{" +
                "recordID='" + recordID + '\'' +
                ", action=" + action +
                ", incrEvent=" + incrEvent +
                ", decrEvent=" + decrEvent +
                ", changeAmount='" + changeAmount + '\'' +
                ", remark='" + remark + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", failedMsg='" + failedMsg + '\'' +
                ", eventDesc='" + eventDesc + '\'' +
                '}';
    }
}
