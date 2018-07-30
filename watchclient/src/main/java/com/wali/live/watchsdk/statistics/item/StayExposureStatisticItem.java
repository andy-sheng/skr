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

    private final static int STAY_EXPOSURE_TYPE = 608; // 频道曝光type 608

    private final static int STAY_EXPOSURE_BIZ_TYPE_MUSIC = 2; // 小米音乐bizType

    public StayExposureStatisticItem(long date, long userId, String recommend) {
        super(date, STAY_EXPOSURE_TYPE);
        mCommonLog = StatisticsProto.CommonLog.newBuilder()
                .setBizType(getBizTypeByChannel())
                .setExtStr(generateExtraString(userId))
                .build();
        mRecommend = recommend;
    }

    private String generateExtraString(long userId) {
        MyLog.d(TAG, "userId=" + userId);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userId);
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

    /**
     * 通过渠道区分bizType 目前只有小米音乐有曝光 游戏中心的频道曝光内嵌在app中
     * @return
     */
    public static int getBizTypeByChannel() {
        int channelId = HostChannelManager.getInstance().getChannelId();
        switch (channelId) {
            case 50019: {
                // 小米音乐
                return STAY_EXPOSURE_BIZ_TYPE_MUSIC;
            }
            default:
                // 其他未定义 暂时不上传打点
                return -1;
        }
    }
}
