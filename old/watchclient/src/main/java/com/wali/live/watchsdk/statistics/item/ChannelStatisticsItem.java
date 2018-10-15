package com.wali.live.watchsdk.statistics.item;

import com.wali.live.proto.StatisticsProto;

/**
 * Created by lan on 2017/6/29.
 */
public class ChannelStatisticsItem extends MilinkStatisticsItem {
    // 采用和直播相同的打点类型, 1=曝光 2=点击
    public static final int CHANNEL_TYPE_EXPOSURE = 1;
    public static final int CHANNEL_TYPE_CLICK = 2;

    public ChannelStatisticsItem(long date, int type, String recommend) {
        super(date, type);
        generateData(recommend);
    }

    private void generateData(String recommend) {
        mRecommend = recommend;
    }

    @Override
    public StatisticsProto.LiveRecvFlagItem build() {
        if (mFlagItem == null) {
            mFlagItem = StatisticsProto.LiveRecvFlagItem.newBuilder()
                    .setDate(mDate)
                    .setType(mType)
                    .setRecommend(mRecommend)
                    .build();
        }
        return mFlagItem;
    }
}
