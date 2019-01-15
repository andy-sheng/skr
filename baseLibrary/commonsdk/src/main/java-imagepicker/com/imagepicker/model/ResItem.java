package com.imagepicker.model;

public abstract class ResItem {
    public static final int RES_IMAGE = 1;
    public static final int RES_VIDEO = 2;

    long addTime;      //图片的创建时间

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public abstract int getType();
}
