package com.module.rankingmode.prepare.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class GameInfo implements Parcelable {
    /**
     * joinInfo : [{"userID":30,"joinSeq":1,"joinTimeMs":1544439278416},{"userID":20,"joinSeq":2,"joinTimeMs":1544439286547},{"userID":10,"joinSeq":3,"joinTimeMs":1544441838343}]
     * hasJoinedUserCnt : 3
     * readyClockResMs : -7986350
     */

    private int hasJoinedUserCnt;
    private int readyClockResMs;
    private List<JoinInfo> joinInfo;


    public GameInfo() {
    }

    public int getHasJoinedUserCnt() {
        return hasJoinedUserCnt;
    }

    public void setHasJoinedUserCnt(int hasJoinedUserCnt) {
        this.hasJoinedUserCnt = hasJoinedUserCnt;
    }

    public int getReadyClockResMs() {
        return readyClockResMs;
    }

    public void setReadyClockResMs(int readyClockResMs) {
        this.readyClockResMs = readyClockResMs;
    }

    public List<JoinInfo> getJoinInfo() {
        return joinInfo;
    }

    public void setJoinInfo(List<JoinInfo> joinInfo) {
        this.joinInfo = joinInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.hasJoinedUserCnt);
        dest.writeInt(this.readyClockResMs);
        dest.writeList(this.joinInfo);
    }

    protected GameInfo(Parcel in) {
        this.hasJoinedUserCnt = in.readInt();
        this.readyClockResMs = in.readInt();
        this.joinInfo = new ArrayList<JoinInfo>();
        in.readList(this.joinInfo, JoinInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<GameInfo> CREATOR = new Parcelable.Creator<GameInfo>() {
        @Override
        public GameInfo createFromParcel(Parcel source) {
            return new GameInfo(source);
        }

        @Override
        public GameInfo[] newArray(int size) {
            return new GameInfo[size];
        }
    };
}
