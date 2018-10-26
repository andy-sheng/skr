package com.common.core.channel;

import java.util.HashMap;

public class HostChannelManager {
    public final static String KEY_SHARE_ENABLE = "key_share_enable";
    public final static String KEY_FOLLOW_ENABLE = "key_follow_enable";

    private static class HostChannelManagerHolder {
        private static final HostChannelManager INSTANCE = new HostChannelManager();
    }

    private HashMap<Integer, HashMap<String, Object>> mDataMap = new HashMap<>();

    private HostChannelManager() {

    }

    public static final HostChannelManager getInstance() {
        return HostChannelManagerHolder.INSTANCE;
    }

    public int getChannelId() {
        return 50019;
    }

    public void put(int channelId, String key, Object obj) {
        HashMap<String, Object> map = mDataMap.get(channelId);
        if (map == null) {
            map = new HashMap<>();
            mDataMap.put(channelId, map);
        }
        map.put(key, obj);
    }


    public Object get(String key) {
        HashMap<String, Object> map = mDataMap.get(HostChannelManager.getInstance().getChannelId());
        if (map == null) {
            return null;
        }
        return map.get(key);
    }
}
