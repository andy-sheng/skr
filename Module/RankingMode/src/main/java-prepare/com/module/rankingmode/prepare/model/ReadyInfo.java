package com.module.rankingmode.prepare.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ReadyInfo implements Parcelable {
    /**
     * userID : 1
     * readySeq : 1
     * readyTimeMs : 1544583392608
     */

    private int userID;
    private int readySeq;
    private long readyTimeMs;

    public ReadyInfo() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getReadySeq() {
        return readySeq;
    }

    public void setReadySeq(int readySeq) {
        this.readySeq = readySeq;
    }

    public long getReadyTimeMs() {
        return readyTimeMs;
    }

    public void setReadyTimeMs(long readyTimeMs) {
        this.readyTimeMs = readyTimeMs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userID);
        dest.writeInt(this.readySeq);
        dest.writeLong(this.readyTimeMs);
    }

    protected ReadyInfo(Parcel in) {
        this.userID = in.readInt();
        this.readySeq = in.readInt();
        this.readyTimeMs = in.readLong();
    }

    public static final Parcelable.Creator<ReadyInfo> CREATOR = new Parcelable.Creator<ReadyInfo>() {
        @Override
        public ReadyInfo createFromParcel(Parcel source) {
            return new ReadyInfo(source);
        }

        @Override
        public ReadyInfo[] newArray(int size) {
            return new ReadyInfo[size];
        }
    };
}
