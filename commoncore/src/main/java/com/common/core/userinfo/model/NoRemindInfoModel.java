package com.common.core.userinfo.model;

import android.support.annotation.NonNull;

import com.common.core.userinfo.noremind.NoRemindInfoDB;

public class NoRemindInfoModel {
    private long userId;
    private int msgType;

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public NoRemindInfoModel() {
    }

    public NoRemindInfoModel(long userId, int msgType) {
        this.userId = userId;
        this.msgType = msgType;
    }

    public static NoRemindInfoDB toNoRemindInfoDB(NoRemindInfoModel model){
        NoRemindInfoDB infoDB = new NoRemindInfoDB();
        infoDB.setUserId(model.userId);
        infoDB.setMsgType(model.msgType);
        return infoDB;
    }

    @NonNull
    @Override
    public String toString() {
        return "DisturbedInfoModel{" +
                "userId=" + userId +
                ", userId=" + userId +
                '}';
    }
}
