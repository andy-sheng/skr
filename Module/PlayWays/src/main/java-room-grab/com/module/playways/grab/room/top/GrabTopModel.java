package com.module.playways.grab.room.top;

import com.orhanobut.dialogplus.ViewHolder;

import java.io.Serializable;

public class GrabTopModel implements Serializable {
    public static final int STATUS_INIT = 0;
    public static final int STATUS_LIGHT_ON = 1;
    public static final int STATUS_LIGHT_OFF = 2;
    int userId;

    String avatar;

    int sex;

    int status = STATUS_INIT;

    GrabTopViewHolder viewHolder;

    public GrabTopModel(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public GrabTopViewHolder getViewHolder() {
        return viewHolder;
    }

    public void setViewHolder(GrabTopViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GrabTopModel that = (GrabTopModel) o;

        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return userId;
    }
}
