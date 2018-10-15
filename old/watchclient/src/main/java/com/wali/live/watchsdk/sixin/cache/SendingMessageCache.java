package com.wali.live.watchsdk.sixin.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SendingMessageCache {
    private static Map<Long, Long> sSendingMessage = Collections.synchronizedMap(new HashMap<Long, Long>());

    public static Long get(Long key) {
        return sSendingMessage.get(key);
    }

    public static void put(Long key, Long value) {
        sSendingMessage.put(key, value);
    }

    public static void remove(Long key) {
        sSendingMessage.remove(key);
    }
}