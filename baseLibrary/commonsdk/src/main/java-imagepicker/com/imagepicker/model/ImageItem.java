package com.imagepicker.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：图片信息
 * 修订历史：
 * ================================================
 */
public class ResItem extends ResItem {

    public ImageItem(){

    }

    public ImageItem(Parcel source) {
        super(source);
    }

    @Override
    public int getType() {
        return RES_IMAGE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ImageItem> CREATOR = new Parcelable.Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };


}
