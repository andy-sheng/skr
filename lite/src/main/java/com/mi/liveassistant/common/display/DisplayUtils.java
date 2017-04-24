package com.mi.liveassistant.common.display;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.log.MyLog;

/**
 * Created by MK on 15-3-25.
 */
public class DisplayUtils {
    private static DisplayMetrics sMetrics = null;

    public static void initialize() {
        if (sMetrics == null) {
            MyLog.v("PicViewFragment", "sMetrics == null");
            sMetrics = GlobalData.app().getResources().getDisplayMetrics();
        }
    }

    public static float getDensity() {
        initialize();
        return sMetrics.density;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(final float dpValue) {
        initialize();
        final float scale = sMetrics.density;
        return (int) ((dpValue * scale) + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(final float pxValue) {
        initialize();
        final float scale = sMetrics.density;
        return (int) ((pxValue / scale) + 0.5f);
    }

    public static int getScreenWidth() {
        initialize();
        return sMetrics.widthPixels;
    }

    public static int getScreenHeight() {
        initialize();
        return sMetrics.heightPixels;
    }

    /**
     * 获取屏幕长宽比(height/width)
     */
    public static float getScreenRate() {
        float H = getScreenHeight();
        float W = getScreenWidth();
        return (H / W);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(final Activity activity, final float dpValue) {
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final float scale = metrics.density;
        return (int) ((dpValue * scale) + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(final Context context, final float dpValue) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float scale = metrics.density;
        return (int) ((dpValue * scale) + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(final Activity activity, final float pxValue) {
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final float scale = metrics.density;
        return (int) ((pxValue / scale) + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(final Context context, final float pxValue) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float scale = metrics.density;
        return (int) ((pxValue / scale) + 0.5f);
    }
}
