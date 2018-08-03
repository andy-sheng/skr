package com.wali.live.watchsdk.statistics.item;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.wali.live.proto.StatisticsProto;

import org.json.JSONObject;

/**
 * Created by liuting on 18-7-27.
 */

public class AliveStatisticItem extends MilinkStatisticsItem {
    private String TAG = getClass().getSimpleName();

    private final static int ALIVE_TYPE = 609; // 活跃时长 type 609

    public final static int ALIVE_BIZ_TYPE_ALL = 1; // APP总活跃时长
    public final static int ALIVE_BIZ_TYPE_LIVE_ROOM = 2; // 直播间观看时长（切换一个房间上传一次）
    public final static int ALIVE_BIZ_TYPE_CHANNEL = 3; // 频道停留时长（切换一次频道上传一次）

    public AliveStatisticItem(long date, long userId, long times, String roomId, long channelId, int bizType) {
        super(date, ALIVE_TYPE);
        mCommonLog = StatisticsProto.CommonLog.newBuilder()
                .setBizType(bizType)
                .setExtStr(getExtraString(userId, times, roomId, channelId))
                .build();
    }

    private String getExtraString(long userId, long times, String roomId, long channelId) {
        MyLog.d(TAG, "user_id=" + userId + " times=" + times);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userId);
            jsonObject.put("times", times);
            if (!TextUtils.isEmpty(roomId)) {
                MyLog.d(TAG, "room_id=" + roomId);
                jsonObject.put("room_id", roomId);
            }
            if (channelId > 0) {
                MyLog.d(TAG, "channel_id=" + channelId);
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
                .setLog(mCommonLog)
                .build();
        return mFlagItem;
    }
}
