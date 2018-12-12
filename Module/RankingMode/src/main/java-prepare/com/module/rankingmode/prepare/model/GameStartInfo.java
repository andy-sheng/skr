package com.module.rankingmode.prepare.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GameStartInfo implements Parcelable {
    /**
     * startTimeMs : 1544586876239
     * startPassedMs : 3119
     */

    private long startTimeMs;
    private int startPassedMs;

    public GameStartInfo() {
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public int getStartPassedMs() {
        return startPassedMs;
    }

    public void setStartPassedMs(int startPassedMs) {
        this.startPassedMs = startPassedMs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.startTimeMs);
        dest.writeInt(this.startPassedMs);
    }

    protected GameStartInfo(Parcel in) {
        this.startTimeMs = in.readLong();
        this.startPassedMs = in.readInt();
    }

    public static final Parcelable.Creator<GameStartInfo> CREATOR = new Parcelable.Creator<GameStartInfo>() {
        @Override
        public GameStartInfo createFromParcel(Parcel source) {
            return new GameStartInfo(source);
        }

        @Override
        public GameStartInfo[] newArray(int size) {
            return new GameStartInfo[size];
        }
    };
}