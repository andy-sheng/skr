package com.common.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Display;
import android.view.WindowManager;


/**
 * Created by MK on 15-3-25.
 */
public class DisplayUtils {
    private DisplayMetrics sMetrics = null;

    private DisplayMetrics sRealMetrics = null;

    DisplayUtils() {
    }

    public void initialize() {
        if (sMetrics == null) {
            if (U.app() != null && U.app().getResources() != null) {
                sMetrics = U.app().getResources().getDisplayMetrics();
            }
        }
        if (sRealMetrics == null) {
            WindowManager windowManager = (WindowManager) U.app().getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            sRealMetrics = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(sRealMetrics);
            } else {
                display.getMetrics(sRealMetrics);
            }
        }
    }

    public float getDensity() {
        initialize();
        return sMetrics.density;
    }

    public float getDensityDpi() {
        initialize();
        return sMetrics.densityDpi;
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
        int w = sMetrics.widthPixels;
        return w;
    }

    /**
     * 不会计算虚拟按键的高度
     *
     * @return
     */
    public int getScreenHeight() {
        initialize();
        int h = sMetrics.heightPixels;
        /**
         * 在小米8青春版 这个值计算得不对
         * 修复米8青春版本
         * 开启虚拟导航按键，返回键盘高度不对的问题
         * 一般都是2118，这里返回2028,莫名其妙的
         */
        if (U.getDeviceUtils().hasNavigationBar()
                && U.getDeviceUtils().getProductModel().equals("MI 8")) {
            if (h == 2028) {
                return 2118;
            }
        }
        return h;
    }


    /**
     * 会算上虚拟按键的高度
     *
     * @return
     */
    public int getPhoneWidth() {
        initialize();
        return sRealMetrics.widthPixels;
    }

    public int getPhoneHeight() {
        initialize();
        return sRealMetrics.heightPixels;
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
