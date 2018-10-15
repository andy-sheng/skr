package com.wali.live.watchsdk.statistics.item;

import com.base.log.MyLog;
import com.mi.live.data.account.channel.HostChannelManager;
import com.wali.live.proto.StatisticsProto;

/**
 * Created by liuting on 18-7-12.
 * 直播频道切换打点
 */

public class ChannelChangeStatisticsItem extends MilinkStatisticsItem{
    private String TAG = getClass().getSimpleName();
    public static int CHANNEL_CHANGE_TYPE_MI_MUSIC = 605;

    public ChannelChangeStatisticsItem(long date, int type, int channelId) {
        super(date, type);
        MyLog.d(TAG, "channelId= " + channelId);
        mCommonLog = StatisticsProto.CommonLog.newBuilder().setBizType(channelId).build();
    }

    @Override
    public StatisticsProto.LiveRecvFlagItem build() {
        MyLog.d(TAG, "type= " + mType);
        mFlagItem = StatisticsProto.LiveRecvFlagItem.newBuilder()
                .setDate(mDate)
                .setType(mType)
                .setLog(mCommonLog)
                .build();
        return mFlagItem;
    }

    /**
     * 通过渠道区分Type 目前只有小米音乐上传频道切换打点 其他未定义
     * @return
     */
    public static int getTypeByChannel() {
        int channelId = HostChannelManager.getInstance().getChannelId();
        switch (channelId) {
            case 50019: {
                // 小米音乐
                return CHANNEL_CHANGE_TYPE_MI_MUSIC;
            }
            default:
                // 其他未定义 暂时不上传打点
                return -1;
        }
    }
}
