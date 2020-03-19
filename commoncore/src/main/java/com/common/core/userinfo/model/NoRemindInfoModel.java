package com.common.core.userinfo.model;

import android.support.annotation.NonNull;

import com.common.core.userinfo.NoRemindInfoDB;

public class NoRemindInfoModel {
    private long userId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public NoRemindInfoModel(long userId) {
        this.userId = userId;
    }

    public static NoRemindInfoDB toDisturbedInfoDB(NoRemindInfoModel model){
        NoRemindInfoDB infoDB = new NoRemindInfoDB();
        infoDB.setUserId(model.userId);
        return infoDB;
    }

    public static NoRemindInfoDB toDisturbedInfoDB(int userId){
        NoRemindInfoDB infoDB = new NoRemindInfoDB();
        infoDB.setUserId((long) userId);
        return infoDB;
    }

    @NonNull
    @Override
    public String toString() {
        return "DisturbedInfoModel{" +
                "userId=" + userId +
                '}';
    }
}
