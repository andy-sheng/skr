package com.wali.live.watchsdk.statistics.item;

import com.base.log.MyLog;
import com.mi.live.data.account.channel.HostChannelManager;
import com.wali.live.proto.StatisticsProto;

import org.json.JSONObject;

/**
 * Created by liuting on 18-7-27.
 */

public class AliveStatisticItem extends MilinkStatisticsItem {
    private String TAG = getClass().getSimpleName();

    private final static int ALIVE_TYPE = 609; // 活跃时长 type 609

    private final static int ALIVE_BIZ_TYPE_GAME_CENTER = 1; // 游戏中心bizType
    private final static int ALIVE_BIZ_TYPE_MUSIC = 2; // 小米音乐bizType

    public AliveStatisticItem(long date, long userId, long times) {
        super(date, ALIVE_TYPE);
        mCommonLog = StatisticsProto.CommonLog.newBuilder()
                .setBizType(getBizTypeByChannel())
                .setExtStr(getExtraString(userId, times))
                .build();
    }

    private String getExtraString(long userId, long times) {
        MyLog.d(TAG, "userId=" + userId + " time=" + times);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userId);
            jsonObject.put("times", times);
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
                .setLog(mCommonLog)
                .build();
        return mFlagItem;
    }

    /**
     * 通过渠道区分bizType
     * @return
     */
    public static int getBizTypeByChannel() {
        int channelId = HostChannelManager.getInstance().getChannelId();
        switch (channelId) {
            case 50010: {
                // 游戏中心
                return ALIVE_BIZ_TYPE_GAME_CENTER;
            }
            case 50019: {
                // 小米音乐
                return ALIVE_BIZ_TYPE_MUSIC;
            }
            default:
                // 其他未定义 暂时不上传打点
                return -1;
        }
    }
}
