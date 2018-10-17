package com.common.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;


/**
 * Created by MK on 15-3-25.
 */
public class DisplayUtils {
    private DisplayMetrics sMetrics = null;

    DisplayUtils() {
    }

    public void initialize() {
        if (sMetrics == null) {
            if (U.app() != null && U.app().getResources() != null) {
                sMetrics = U.app().getResources().getDisplayMetrics();
            }
        }
    }

    public float getDensity() {
        initialize();
        return sMetrics.density;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(final float dpValue) {
        initialize();
        final float scale = sMetrics.density;
        return (int) ((dpValue * scale) + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public int px2dip(final float pxValue) {
        initialize();
        final float scale = sMetrics.density;
        return (int) ((pxValue / scale) + 0.5f);
    }

    public int getDimenPx(int resId) {
        return U.app().getResources().getDimensionPixelSize(resId);
    }

    /**
     * 获取屏幕宽度和高度，单位为px
     *
     * @return pair对象, first是width，second是height
     */
    public Pair<Integer, Integer> getScreenWidthAndHeight() {
        initialize();
        return new Pair<Integer, Integer>(sMetrics.widthPixels, sMetrics.heightPixels);
    }

    public int getScreenWidth() {
        initialize();
//        MyLog.v("PicViewFragment" + " getScreenWidth sMetrics == " + sMetrics.hashCode());
        return sMetrics.widthPixels;
    }

    public int getScreenHeight() {
        initialize();
//        MyLog.v("PicViewFragment" + " getScreenHeight sMetrics == " + sMetrics.hashCode());
        return sMetrics.heightPixels;
    }

    public int getPhoneWidth() {
//        MyLog.v("PicViewFragment" + " getScreenWidth sMetrics == " + sMetrics.hashCode());
        int width = getScreenWidth();
        int height = getScreenHeight();
        return width < height ? width : height;
    }

    public int getPhoneHeight() {
//        MyLog.v("PicViewFragment" + " getScreenHeight sMetrics == " + sMetrics.hashCode());
        int width = getScreenWidth();
        int height = getScreenHeight();
        return width > height ? width : height;
    }

    /**
     * 获取屏幕长宽比(height/width)
     *
     * @return
     */
    public float getScreenRate() {
        float H = getScreenHeight();
        float W = getScreenWidth();
        return (H / W);
    }

    /**
     * 判断是否是全面屏
     *
     * @return
     */
    public boolean isFullScree() {
        float H = 16;
        float W = 9;
        float deault = H / W;

        if (getScreenRate() > deault) {
            return true;
        }

        return false;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(final Activity activity, final float dpValue) {
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final float scale = metrics.density;
        return (int) ((dpValue * scale) + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(final Context context, final float dpValue) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float scale = metrics.density;
        return (int) ((dpValue * scale) + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public int px2dip(final Activity activity, final float pxValue) {
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final float scale = metrics.density;
        return (int) ((pxValue / scale) + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public int px2dip(final Context context, final float pxValue) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float scale = metrics.density;
        return (int) ((pxValue / scale) + 0.5f);
    }

}
