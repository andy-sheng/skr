package com.respicker.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public abstract class ResItem implements Serializable, Parcelable {
    public static final int RES_IMAGE = 1;
    public static final int RES_VIDEO = 2;

    String name;       //图片的名字
    String path;       //图片的路径
    long size;         //图片的大小
    int width;         //图片的宽度
    int height;        //图片的高度
    String mimeType;   //图片的类型
    long addTime;      //图片的创建时间

    public ResItem() {

    }

    protected ResItem(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.size = in.readLong();
        this.width = in.readInt();
        this.height = in.readInt();
        this.mimeType = in.readString();
        this.addTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeLong(this.size);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.mimeType);
        dest.writeLong(this.addTime);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * 图片的路径相同就认为是同一张图片
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ImageItem) {
            ImageItem item = (ImageItem) o;
            return this.path.equalsIgnoreCase(item.path);
        }

        return super.equals(o);
    }

    public abstract int getType();
}
