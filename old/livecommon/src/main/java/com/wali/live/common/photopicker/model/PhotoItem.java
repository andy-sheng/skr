package com.wali.live.common.photopicker.model;

import java.io.Serializable;

/**
 * Created by lan on 15-11-5.
 */
public class PhotoItem implements Serializable {
    public static final int RESIZE = 128;
    private String mLocalPath;

    private int mSrcWidth;
    private int mSrcHeight;

    // 与ui相关
    private boolean mIsSelected;

    private boolean isOrigin =true; //是否是原图

    private int originSize;//原图的大小

    public PhotoItem() {
    }

    public PhotoItem(String localPath) {
        setLocalPath(localPath);
    }

    public void setLocalPath(String localPath) {
        mLocalPath = localPath;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public void setSrcSize(int width, int height) {
        mSrcWidth = width;
        mSrcHeight = height;
    }

    public int getSrcWidth() {
        return mSrcWidth;
    }

    public int getSrcHeight() {
        return mSrcHeight;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public boolean isOrigin() {
        return isOrigin;
    }

    public void setIsOrigin(boolean isOrigin) {
        this.isOrigin = isOrigin;
    }

    public int getOriginSize() {
        return originSize;
    }

    public void setOriginSize(int originSize) {
        this.originSize = originSize;
    }
}
