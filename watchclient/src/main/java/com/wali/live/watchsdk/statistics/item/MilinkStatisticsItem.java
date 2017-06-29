package com.wali.live.watchsdk.statistics.item;

import com.google.protobuf.ByteString;

public abstract class MilinkStatisticsItem {
    protected long mTime;
    protected int mType;
    protected ByteString mExtBytes;
    protected String mRecommend;       // 暂时不用

    public MilinkStatisticsItem(long time, int type) {
        mTime = time;
        mType = type;
    }

    public long getTime() {
        return mTime;
    }

    public int getType() {
        return mType;
    }

    public ByteString getExtBytes() {
        return mExtBytes;
    }

    public String getRecommend() {
        return mRecommend;
    }
}