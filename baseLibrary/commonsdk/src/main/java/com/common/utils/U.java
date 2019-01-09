package com.common.utils;

import android.app.Application;
import android.graphics.drawable.Drawable;

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

    private static BitmapUtils bitmapUtils;

    private static VideoUtils videoUtils;

    private static BlurUtils blurUtils;

    private static HttpUtils httpUtils;

    private static StringUtils stringUtils;

    private static PermissionUtils permissionUtil;

    private static StatusBarUtils statusBarUtil;

    private static CacheUtils cacheUtils;

    private static ActivityUtils activityUtils;

    private static KeyBoardUtils keyBoardUtils;

    private static FileUtils fileUtils;

    private static ToastUtils toastUtil;

    private static CommonUtils commonUtils;

    private static ChannelUtils channelUtils;

    private static Base64Utils base64Utils;

    private static MD5Utils MD5Utils;

    private static FragmentUtils fragmentUtils;

    private static NetworkUtils networkUtils;

    private static DateTimeUtils dateTimeUtils;

    private static PreferenceUtils preferenceUtils;

    private static PinyinUtils pinyinUtils;

    private static LbsUtils lbsUtils;

    private static ZipUtils zipUtils;

    private static UriUtils uriUtils;

    private static ReflectUtils reflectUtils;

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

    public static BitmapUtils getBitmapUtils() {
        if (bitmapUtils == null) {
            bitmapUtils = new BitmapUtils();
        }
        return bitmapUtils;
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

    public static PermissionUtils getPermissionUtils() {
        if (permissionUtil == null) {
            permissionUtil = new PermissionUtils();
        }
        return permissionUtil;
    }

    public static StatusBarUtils getStatusBarUtil() {
        if (statusBarUtil == null) {
            statusBarUtil = new StatusBarUtils();
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

    public static ToastUtils getToastUtil() {
        if (toastUtil == null) {
            toastUtil = new ToastUtils();
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

    public static DateTimeUtils getDateTimeUtils() {
        if (dateTimeUtils == null) {
            dateTimeUtils = new DateTimeUtils();
        }
        return dateTimeUtils;
    }

    public static PreferenceUtils getPreferenceUtils() {
        if (preferenceUtils == null) {
            preferenceUtils = new PreferenceUtils();
        }
        return preferenceUtils;
    }

    public static PinyinUtils getPinyinUtils() {
        if (pinyinUtils == null) {
            pinyinUtils = new PinyinUtils();
        }
        return pinyinUtils;
    }

    public static LbsUtils getLbsUtils() {
        if (lbsUtils == null) {
            lbsUtils = new LbsUtils();
        }
        return lbsUtils;
    }

    public static ZipUtils getZipUtils(){
        if(zipUtils==null){
            zipUtils = new ZipUtils();
        }
        return zipUtils;
    }

    public static UriUtils getUriUtils(){
        if(uriUtils==null){
            uriUtils = new UriUtils();
        }
        return uriUtils;
    }

    public static ReflectUtils getReflectUtils(){
        if(reflectUtils==null){
            reflectUtils = new ReflectUtils();
        }
        return reflectUtils;
    }

    private static int REQUEST_CODE_FIRST = 100000;

    private static Object sRequestCodeLock = new Object();

    /**
     * 返回一个 code 唯一标识 一个 实例 对象
     *
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

    public static int getColor(int colorId){
        return U.app().getResources().getColor(colorId);
    }

    public static Drawable getDrawable(int drawableId){
        return U.app().getResources().getDrawable(drawableId);
    }
}
