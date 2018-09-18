package com.wali.live.watchsdk.watch.model;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuting on 18-8-22.
 * 记录游戏直播间与游戏对应关系
 */

public class WatchGameInfoConfig {
    public static Map<String, InfoItem> sGameInfoMap = new HashMap<>(); // key为包下载地址

    public static void update(String downloadUrl, long anchorId, long channelId, String packageName, long gameId) {
        if (!TextUtils.isEmpty(downloadUrl)) {
            InfoItem infoItem = new InfoItem();
            infoItem.anchorId = anchorId;
            infoItem.channelId = channelId;
            infoItem.packageName = packageName;
            infoItem.gameId = gameId;
            sGameInfoMap.put(downloadUrl, infoItem);
        }
    }

    public static class InfoItem {
        public long anchorId;
        public long channelId;
        public String packageName;
        public long gameId;
    }

}
