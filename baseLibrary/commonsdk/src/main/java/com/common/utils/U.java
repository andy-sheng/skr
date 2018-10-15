package com.common.utils;

import android.app.Application;
import android.inputmethodservice.Keyboard;

/**
 * 每个工具类都必须由U引用来调用，以防止工具类混乱难以管理的问题，
 * 类似好几个ImageUtils
 */
public class U {
    private static boolean sCoreProcess = true;

    private static Application application;

    private static AppInfoUtils appInfoUtils;

    private static DeviceUtils deviceUtils;

    private static DisplayUtils displayUtils;

    private static ThreadUtils threadUtils;

    private static ImageUtils imageUtils;

    private static BlurUtils blurUtils;

    private static HttpUtils httpUtils;

    private static StringUtils stringUtils;

    private static PermissionUtil permissionUtil;

    private static StatusBarUtil statusBarUtil;

    private static CacheUtils cacheUtils;

    private static ActivityUtils activityUtils;

    private static KeyBoardUtils keyBoardUtils;

    private static FileUtils fileUtils;

    private static ToastUtil toastUtil;

    private static CommonUtils commonUtils;

    private static ChannelUtils channelUtils;

    public static void setApp(Application app) {
        application = app;
    }

    public static Application app() {
        return application;
    }

    public static AppInfoUtils getAppInfoUtils() {
        if (appInfoUtils == null) {
            appInfoUtils = new AppInfoUtils();
        }
        return appInfoUtils;
    }

    public static DeviceUtils getDeviceUtils() {
        if (deviceUtils == null) {
            deviceUtils = new DeviceUtils();
        }
        return deviceUtils;
    }

    public static DisplayUtils getDisplayUtils() {
        if (displayUtils == null) {
            displayUtils = new DisplayUtils();
        }
        return displayUtils;
    }

    public static ThreadUtils getThreadUtils() {
        if (threadUtils == null) {
            threadUtils = new ThreadUtils();
        }
        return threadUtils;
    }

    public static ImageUtils getImageUtils() {
        if (imageUtils == null) {
            imageUtils = new ImageUtils();
        }
        return imageUtils;
    }

    public static BlurUtils getBlurUtils() {
        if (blurUtils == null) {
            blurUtils = new BlurUtils();
        }
        return blurUtils;
    }

    public static HttpUtils getHttpUtils() {
        if (httpUtils == null) {
            httpUtils = new HttpUtils();
        }
        return httpUtils;
    }

    public static StringUtils getStringUtils() {
        if (stringUtils == null) {
            stringUtils = new StringUtils();
        }
        return stringUtils;
    }

    public static PermissionUtil getPermissionUtils() {
        if (permissionUtil == null) {
            permissionUtil = new PermissionUtil();
        }
        return permissionUtil;
    }

    public static StatusBarUtil getStatusBarUtil() {
        if (statusBarUtil == null) {
            statusBarUtil = new StatusBarUtil();
        }
        return statusBarUtil;
    }

    public static CacheUtils getCacheUtils() {
        if (cacheUtils == null) {
            cacheUtils = new CacheUtils();
        }
        return cacheUtils;
    }

    public static ActivityUtils getActivityUtils() {
        if (activityUtils == null) {
            activityUtils = new ActivityUtils();
        }
        return activityUtils;
    }

    public static KeyBoardUtils getKeyBoardUtils() {
        if (keyBoardUtils == null) {
            keyBoardUtils = new KeyBoardUtils();
        }
        return keyBoardUtils;
    }

    public static FileUtils getFileUtils() {
        if (fileUtils == null) {
            fileUtils = new FileUtils();
        }
        return fileUtils;
    }

    public static ToastUtil getToastUtil() {
        if (toastUtil == null) {
            toastUtil = new ToastUtil();
        }
        return toastUtil;
    }

    public static CommonUtils getCommonUtils() {
        if (commonUtils == null) {
            commonUtils = new CommonUtils();
        }
        return commonUtils;
    }

    public static ChannelUtils getChannelUtils() {
        if (channelUtils == null) {
            channelUtils = new ChannelUtils();
        }
        return channelUtils;
    }

    public static void setCoreProcess(boolean coreProcess) {
        sCoreProcess = coreProcess;
    }

    public static boolean isCoreProcess() {
        return sCoreProcess;
    }
}
