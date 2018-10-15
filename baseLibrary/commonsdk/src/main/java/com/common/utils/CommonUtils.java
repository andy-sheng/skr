package com.common.utils;

/**
 * 通过U.getCommonUtils 获得
 * 一些实在不好分类的 util 方法 放在这。
 */
public class CommonUtils {
    public final int FAST_DOUBLE_CLICK_INTERVAL = 500;

    private long sLastClickTime = 0;

    /**
     * 判断是否是快速点击
     */
    public boolean isFastDoubleClick() {
        return isFastDoubleClick(FAST_DOUBLE_CLICK_INTERVAL);
    }

    /**
     * 判断是否是快速点击
     *
     * @param time, 时间间隔, 单位为毫秒
     * @return
     */
    public boolean isFastDoubleClick(long time) {
        if (time <= 0) {
            return true;
        }

        long now = System.currentTimeMillis();
        long delta = now - sLastClickTime;
        if (delta > 0 && delta < time) {
            return true;
        }
        sLastClickTime = now;
        return false;
    }

}
