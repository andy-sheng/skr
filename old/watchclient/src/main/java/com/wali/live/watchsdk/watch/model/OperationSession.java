package com.wali.live.watchsdk.watch.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhujianning on 18-9-2.
 */

public class OperationSession implements Parcelable {

    private String gameId;

    private long recv;

    private long total;

    private String packageName;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(gameId);
        dest.writeLong(recv);
        dest.writeLong(total);
        dest.writeString(packageName);
    }

    public OperationSession(Parcel in) {
        gameId = in.readString();
        recv = in.readLong();
        total = in.readLong();
        packageName = in.readString();
    }

    public static final Creator<OperationSession> CREATOR = new Creator<OperationSession>() {

        @Override
        public OperationSession createFromParcel(Parcel source) {
            OperationSession session = new OperationSession(source);
            return session;
        }

        @Override
        public OperationSession[] newArray(int size) {
            return new OperationSession[size];
        }
    };
}
