package com.common.utils;

import android.app.Application;

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

    private static VideoUtils videoUtils;

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

    private static Base64Utils base64Utils;

    private static MD5Utils MD5Utils;

    private static FragmentUtils fragmentUtils;

    private static NetworkUtils networkUtils;

    public static void setApp(Application app) {
        application = app;

        // 初始化一些网络状态
        getNetworkUtils().syncActiveNetworkType();
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

    public static VideoUtils getVideoUtils() {
        if (videoUtils == null) {
            videoUtils = new VideoUtils();
        }
        return videoUtils;
    }

    public static Base64Utils getBase64Utils() {
        if (base64Utils == null) {
            base64Utils = new Base64Utils();
        }
        return base64Utils;
    }

    public static MD5Utils getMD5Utils() {
        if (MD5Utils == null) {
            MD5Utils = new MD5Utils();
        }
        return MD5Utils;
    }

    public static FragmentUtils getFragmentUtils() {
        if (fragmentUtils == null) {
            fragmentUtils = new FragmentUtils();
        }
        return fragmentUtils;
    }

    public static NetworkUtils getNetworkUtils() {
        if (networkUtils == null) {
            networkUtils = new NetworkUtils();
        }
        return networkUtils;
    }

    private static int REQUEST_CODE_FIRST = 100000;

    private static Object sRequestCodeLock = new Object();

    /**
     * 返回一个 code 唯一标识 一个 实例 对象
     * @return
     */
    public static int getRequestCode() {
        synchronized (sRequestCodeLock) {
            return REQUEST_CODE_FIRST++;
        }
    }

    public static void setCoreProcess(boolean coreProcess) {
        sCoreProcess = coreProcess;
    }

    public static boolean isCoreProcess() {
        return sCoreProcess;
    }
}
