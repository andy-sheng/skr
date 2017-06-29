package com.wali.live.watchsdk.statistics.item;

import com.google.protobuf.ByteString;
import com.wali.live.proto.StatisticsProto;

public abstract class MilinkStatisticsItem {
    // 外层应用类型
    public static final int LIVE_SDK_TYPE = 200;

    protected long mDate;
    protected int mType;
    protected ByteString mExtBytes;     // 不用，直播频道使用
    protected String mRecommend;        // 不用，直播频道使用
    protected StatisticsProto.CommonLog mCommonLog;

    public MilinkStatisticsItem(long date, int type) {
        mDate = date;
        mType = type;
    }

    public long getDate() {
        return mDate;
    }

    public int getType() {
        return mType;
    }

    public StatisticsProto.CommonLog getCommonLog() {
        return mCommonLog;
    }
}