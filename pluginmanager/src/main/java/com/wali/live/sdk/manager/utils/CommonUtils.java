package com.wali.live.sdk.manager.utils;

/**
 * Created by lan on 17/3/1.
 */
public class CommonUtils {
    public static final int FAST_DOUBLE_CLICK_INTERVAL = 500;

    private static long sLastClickTime = 0;

    /**
     * 判断是否是快速点击
     */
    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(FAST_DOUBLE_CLICK_INTERVAL);
    }

    /**
     * 判断是否是快速点击
     *
     * @param time, 时间间隔, 单位为毫秒
     * @return
     */
    private static boolean isFastDoubleClick(final long time) {
        long now = System.currentTimeMillis();
        long delta = now - sLastClickTime;
        sLastClickTime = now;
        return delta > 0 && delta < time;
    }
}
