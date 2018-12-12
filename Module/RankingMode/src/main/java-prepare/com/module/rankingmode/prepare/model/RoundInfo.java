package com.module.rankingmode.prepare.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RoundInfo implements Parcelable {
    /**
     * userID : 7
     * playbookID : 1
     * roundSeq : 1
     * singBeginMs : 3000
     * singEndMs : 341000
     */

    private int userID;
    private int playbookID;
    private int roundSeq;
    private int singBeginMs;
    private int singEndMs;

    public RoundInfo() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getPlaybookID() {
        return playbookID;
    }

    public void setPlaybookID(int playbookID) {
        this.playbookID = playbookID;
    }

    public int getRoundSeq() {
        return roundSeq;
    }

    public void setRoundSeq(int roundSeq) {
        this.roundSeq = roundSeq;
    }

    public int getSingBeginMs() {
        return singBeginMs;
    }

    public void setSingBeginMs(int singBeginMs) {
        this.singBeginMs = singBeginMs;
    }

    public int getSingEndMs() {
        return singEndMs;
    }

    public void setSingEndMs(int singEndMs) {
        this.singEndMs = singEndMs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userID);
        dest.writeInt(this.playbookID);
        dest.writeInt(this.roundSeq);
        dest.writeInt(this.singBeginMs);
        dest.writeInt(this.singEndMs);
    }

    protected RoundInfo(Parcel in) {
        this.userID = in.readInt();
        this.playbookID = in.readInt();
        this.roundSeq = in.readInt();
        this.singBeginMs = in.readInt();
        this.singEndMs = in.readInt();
    }

    public static final Parcelable.Creator<RoundInfo> CREATOR = new Parcelable.Creator<RoundInfo>() {
        @Override
        public RoundInfo createFromParcel(Parcel source) {
            return new RoundInfo(source);
        }

        @Override
        public RoundInfo[] newArray(int size) {
            return new RoundInfo[size];
        }
    };
}