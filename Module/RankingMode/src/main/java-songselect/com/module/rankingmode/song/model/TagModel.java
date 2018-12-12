package com.module.rankingmode.song.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TagModel implements Parcelable {
    /**
     * tagID : 1
     * tagName : 网络歌曲
     */

    private int tagID;
    private String tagName;

    public TagModel() {
    }

    public int getTagID() {
        return tagID;
    }

    public void setTagID(int tagID) {
        this.tagID = tagID;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.tagID);
        dest.writeString(this.tagName);
    }

    protected TagModel(Parcel in) {
        this.tagID = in.readInt();
        this.tagName = in.readString();
    }

    public static final Parcelable.Creator<TagModel> CREATOR = new Parcelable.Creator<TagModel>() {
        @Override
        public TagModel createFromParcel(Parcel source) {
            return new TagModel(source);
        }

        @Override
        public TagModel[] newArray(int size) {
            return new TagModel[size];
        }
    };
}
