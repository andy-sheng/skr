package com.module.home.model;

import java.io.Serializable;

public class WithDrawHistoryModel implements Serializable {

    /**
     * userID : 1362586
     * amount : 100000
     * action : 1
     * createAt : 1552471517
     * status : 2
     * dateTime : 2019-03-13 18:05:17
     * desc : 提现
     */

    private int userID;
    private int amount;
    private int action;
    private int createAt;
    private int status;
    private String dateTime;
    private String desc;
    private String statusDesc;

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getCreateAt() {
        return createAt;
    }

    public void setCreateAt(int createAt) {
        this.createAt = createAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
