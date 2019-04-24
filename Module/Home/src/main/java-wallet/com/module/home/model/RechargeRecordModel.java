package com.module.home.model;

import java.io.Serializable;

public class RechargeRecordModel implements Serializable {


    /**
     * action : 1
     * amount : 9000
     * amountStr : +9.00
     * createAt : 1555845729
     * dateTime : 2019-04-21
     * decrEvent : 0
     * desc : 充值
     * incrEvent : 4
     * remark :
     * userID : 1115638
     */

    private int action;
    private int amount;
    private String amountStr;
    private int createAt;
    private String dateTime;
    private int decrEvent;
    private String desc;
    private int incrEvent;
    private String remark;
    private int userID;

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getAmountStr() {
        return amountStr;
    }

    public void setAmountStr(String amountStr) {
        this.amountStr = amountStr;
    }

    public int getCreateAt() {
        return createAt;
    }

    public void setCreateAt(int createAt) {
        this.createAt = createAt;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getDecrEvent() {
        return decrEvent;
    }

    public void setDecrEvent(int decrEvent) {
        this.decrEvent = decrEvent;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getIncrEvent() {
        return incrEvent;
    }

    public void setIncrEvent(int incrEvent) {
        this.incrEvent = incrEvent;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
