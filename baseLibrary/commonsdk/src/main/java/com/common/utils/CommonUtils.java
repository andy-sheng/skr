package com.common.utils;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通过U.getCommonUtils 获得
 * 一些实在不好分类的 util 方法 放在这。
 */
public class CommonUtils {
    public final int FAST_DOUBLE_CLICK_INTERVAL = 500;

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private long sLastClickTime = 0;

    private Handler sMainHandler;

    CommonUtils() {
    }

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

    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public Handler getUiHandler() {
        if (sMainHandler == null) {
            sMainHandler = new Handler(Looper.getMainLooper());
        }
        return sMainHandler;
    }

    /**
     * 检测某个app是否已经安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public boolean isAppInstalled(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        return packageInfo != null;
    }

    public int generateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }

        } else {
            return View.generateViewId();
        }
    }

}
