package com.module.playways.grab.room.top;

import java.io.Serializable;

public class GrabTopModel implements Serializable {
    String avatar;
    int sex;

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
}
