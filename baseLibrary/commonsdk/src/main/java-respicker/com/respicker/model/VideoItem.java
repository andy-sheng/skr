package com.respicker.model;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoItem extends ResItem {
    int videoId;       //视频id
    ImageItem thumb;  //缩略图信息

    public VideoItem(){

    }

    public VideoItem(Parcel parcel){
        super(parcel);
    }

    @Override
    public int getType() {
        return RES_VIDEO;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public ImageItem getThumb() {
        return thumb;
    }

    public void setThumb(ImageItem thumb) {
        this.thumb = thumb;
    }

    public static final Parcelable.Creator<VideoItem> CREATOR = new Parcelable.Creator<VideoItem>() {
        @Override
        public VideoItem createFromParcel(Parcel source) {
            return new VideoItem(source);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };
}
