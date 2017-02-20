package com.mi.live.data.repository.DataType;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by yurui on 16/6/4.
 */
public class MediaItem implements Serializable {
    public static final int RESIZE = 256;
    public String mPhotoPath;
    public String mThumbPath;

    public int mDuration;
    public boolean mIsSelected;

    public String mVideoPath;

    public int mSrcWidth;
    public int mSrcHeight;

    // 与ui相关
    public boolean mIsOrigin = true; //是否是原图
    public int mOoriginSize;//原图的大小
    public boolean mIsFirstItem = false;
    public static final int TYPE_PICTURE = 1;
    public static final int TYPE_VIDEO = 2;

    public int mType = TYPE_PICTURE;


    public MediaItem() {
    }

    public void setSrcSize(int width, int height) {
        mSrcWidth = width;
        mSrcHeight = height;
    }

    @Override
    public boolean equals(Object other) {
        if(other==null|| !(other instanceof MediaItem)){
            return false;
        }
        MediaItem otherItem=(MediaItem)other;
        if(!TextUtils.isEmpty(mPhotoPath)){
            return mPhotoPath.equalsIgnoreCase(otherItem.mPhotoPath);
        }

        if(!TextUtils.isEmpty(mVideoPath)){
            return mVideoPath.equalsIgnoreCase(otherItem.mVideoPath);
        }
        return super.equals(other);
    }
}
