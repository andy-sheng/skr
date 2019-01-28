package com.common.utils;

public class TimeUtils {
    private static final int MIN_CLICK_DELAY_TIME = 500;
    private static long lastClickTime;

    TimeUtils() {
    }

    public boolean isFastClick() {
        boolean flag = true;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = curClickTime;
        return flag;
    }
}
