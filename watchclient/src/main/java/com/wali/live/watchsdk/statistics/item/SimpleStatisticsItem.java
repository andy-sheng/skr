package com.wali.live.watchsdk.statistics.item;

import com.wali.live.proto.StatisticsProto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lan on 2017/6/29.
 */
public class SimpleStatisticsItem extends MilinkStatisticsItem {
    // 内层数据类型
    public static final int GAME_ACTIVE_BIZTYPE = LIVE_SDK_TYPE * 1000 + 1;             // 游戏标签tab日活
    public static final int MIVIDEO_ACTIVE_BIZTYPE = LIVE_SDK_TYPE * 1000 + 2;          // 小米视频日活

    private static final String PARAM_KEY = "key";
    private static final String PARAM_TIME = "time";

    public SimpleStatisticsItem(long date, int type, int bizType, String key, long time) throws JSONException {
        super(date, type);
        generateData(bizType, key, time);
    }

    private void generateData(int bizType, String key, long time) throws JSONException {
        JSONObject extObject = new JSONObject();
        extObject.put(PARAM_KEY, key);
        extObject.put(PARAM_TIME, time);

        mCommonLog = StatisticsProto.CommonLog.newBuilder()
                .setBizType(bizType)
                .setExtStr(extObject.toString())
                .build();
    }

    @Override
    public StatisticsProto.LiveRecvFlagItem build() {
        if (mFlagItem == null) {
            mFlagItem = StatisticsProto.LiveRecvFlagItem.newBuilder()
                    .setDate(mDate)
                    .setType(mType)
                    .setLog(mCommonLog)
                    .build();
        }
        return mFlagItem;
    }
}
