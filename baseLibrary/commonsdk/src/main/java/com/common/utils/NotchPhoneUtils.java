package com.common.utils;

import android.content.Context;

import com.common.log.MyLog;

import java.lang.reflect.Method;

public class NotchPhoneUtils {

    public final String TAG = "NotchPhoneUtils";

    NotchPhoneUtils(){

    }

    /**
     * 华为手机
     *
     * @param context
     * @return
     */
    public boolean hasNotchInHuawei(Context context) {
        boolean hasNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method hasNotchInScreen = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            if (hasNotchInScreen != null) {
                hasNotch = (boolean) hasNotchInScreen.invoke(HwNotchSizeUtil);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNotch;
    }

    public int getNotchHightHuawei(Context context) {
        int notchHight = 0;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("getNotchSize");
            int[] ret = (int[]) get.invoke(HwNotchSizeUtil);
            notchHight = ret[1];
        } catch (ClassNotFoundException e) {
            MyLog.e("test", "getNotchSize ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            MyLog.e("test", "getNotchSize NoSuchMethodException");
        } catch (Exception e) {
            MyLog.e("test", "getNotchSize Exception");
        } finally {
            return notchHight;
        }
    }

    /**
     * oppo手机
     *
     * @param context
     * @return
     */
    public boolean hasNotchInOppo(Context context) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    public int getNotchHightOppo(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * vivo 手机
     *
     * @param context
     * @return
     */
    public boolean hasNotchInVivo(Context context) {
        boolean hasNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class ftFeature = cl.loadClass("android.util.FtFeature");
            Method[] methods = ftFeature.getDeclaredMethods();
            if (methods != null) {
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method != null) {
                        if (method.getName().equalsIgnoreCase("isFeatureSupport")) {
                            hasNotch = (boolean) method.invoke(ftFeature, 0x00000020);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            hasNotch = false;
        }
        return hasNotch;
    }

    public int getNotchHightVivo(Context context) {
        return U.getStatusBarUtil().getStatusBarHeight(context);
    }

    /**
     * xiaomi手机
     *
     * @param context
     * @return
     */
    public boolean hasNotchMiui(Context context) {
        boolean hasNotch = false;
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String notch = (String) m.invoke(systemPropertiesClass, "ro.miui.notch");
            if ("1".equals(notch)) {
                hasNotch = true;
            }
        } catch (Exception e) {
            MyLog.w(TAG, "hasNotchMiui" + " context=" + context);
        }
        return hasNotch;
    }


    public int getNotchHightMiui(Context context) {
        int result = U.getStatusBarUtil().getStatusBarHeight(context);
        int resourceId = context.getResources().getIdentifier("notch_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
