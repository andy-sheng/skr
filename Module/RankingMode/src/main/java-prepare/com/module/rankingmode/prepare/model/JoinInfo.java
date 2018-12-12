package com.module.rankingmode.prepare.model;

import android.os.Parcel;
import android.os.Parcelable;

public class JoinInfo implements Parcelable {

    /**
     * userID : 30
     * joinSeq : 1
     * joinTimeMs : 1544439278416
     */

    private int userID;
    private int joinSeq;
    private long joinTimeMs;

    public JoinInfo() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getJoinSeq() {
        return joinSeq;
    }

    public void setJoinSeq(int joinSeq) {
        this.joinSeq = joinSeq;
    }

    public long getJoinTimeMs() {
        return joinTimeMs;
    }

    public void setJoinTimeMs(long joinTimeMs) {
        this.joinTimeMs = joinTimeMs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userID);
        dest.writeInt(this.joinSeq);
        dest.writeLong(this.joinTimeMs);
    }

    protected JoinInfo(Parcel in) {
        this.userID = in.readInt();
        this.joinSeq = in.readInt();
        this.joinTimeMs = in.readLong();
    }

    public static final Parcelable.Creator<JoinInfo> CREATOR = new Parcelable.Creator<JoinInfo>() {
        @Override
        public JoinInfo createFromParcel(Parcel source) {
            return new JoinInfo(source);
        }

        @Override
        public JoinInfo[] newArray(int size) {
            return new JoinInfo[size];
        }
    };
}
