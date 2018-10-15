package com.wali.live.watchsdk.statistics.item;

import com.base.log.MyLog;
import com.mi.live.data.account.channel.HostChannelManager;
import com.wali.live.proto.StatisticsProto;

import org.json.JSONObject;

/**
 * Created by liuting on 18-7-25.
 */

public class StayExposureStatisticItem extends MilinkStatisticsItem {
    private String TAG = getClass().getSimpleName();

    private final static int STAY_EXPOSURE_TYPE = 630; // 频道曝光type 630

    public StayExposureStatisticItem(long date, long userId, String recommend, long channelId) {
        super(date, STAY_EXPOSURE_TYPE);
        mCommonLog = StatisticsProto.CommonLog.newBuilder()
                .setBizType(0)
                .setExtStr(generateExtraString(userId, channelId))
                .build();
        mRecommend = recommend;
    }

    private String generateExtraString(long userId, long channelId) {
        MyLog.d(TAG, "userId=" + userId);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userId);
            if (channelId > 0) {
                // recommend字段中有频道id 数据那边还是要加在extStr里
                jsonObject.put("channel_id", channelId);
            }
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public StatisticsProto.LiveRecvFlagItem build() {
        MyLog.d(TAG, "type=" + mType + " recommend=" + mRecommend);
        mFlagItem = StatisticsProto.LiveRecvFlagItem.newBuilder()
                .setDate(mDate)
                .setType(mType)
                .setRecommend(mRecommend)
                .setLog(mCommonLog)
                .build();
        return mFlagItem;
    }
}
