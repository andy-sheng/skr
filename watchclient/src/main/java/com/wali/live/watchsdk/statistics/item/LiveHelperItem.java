package com.wali.live.watchsdk.statistics.item;

import com.wali.live.proto.StatisticsProto;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lan on 2017/6/29.
 */
public class LiveHelperItem extends MilinkStatisticsItem {
    private static final String PARAM_KEY = "key";
    private static final String PARAM_TIME = "time";

    public static final int GAME_ACTIVE_TYPE = 200;

    public LiveHelperItem(long time, int type, int channelId, String key) throws JSONException {
        super(time, type);
        generateData(channelId, key);
    }

    private void generateData(int channelId, String key) throws JSONException {
        JSONObject helperObject = new JSONObject();
        helperObject.put(PARAM_KEY, key);
        helperObject.put(PARAM_TIME, 1);

        StatisticsProto.LiveHelper helperItem = StatisticsProto.LiveHelper.newBuilder()
                .setChannelId(channelId)
                .setExtStr(helperObject.toString())
                .build();
        mExtBytes = helperItem.toByteString();
    }
}
