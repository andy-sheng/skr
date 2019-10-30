package com.common.statistics.umeng;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.common.log.MyLog;
import com.common.utils.U;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.Map;

/**
 * 在仅有Activity的应用中，SDK自动帮助开发者调用了 2 中的onPageStart/onPageEnd方法，并把Activity 类名作为页面名称统计。但是在包含fragment的程序中我们希望统计更详细的页面，所以需要自己调用onPageStart/onPageEnd方法做更详细的统计。
 *
 * 首先，需要在程序入口处，调用 MobclickAgent.openActivityDurationTrack(false) 禁止默认的页面统计功能，这样将不会再自动统计Activity页面。
 *
 * 然后需要手动添加以下代码：
 *
 * 使用 MobclickAgent.onResume 和 MobclickAgent.onPause方法统计时长, 这和基本统计中的情况一样(针对Activity)。
 *
 * 使用 MobclickAgent.onPageStart 和 MobclickAgent.onPageEnd 方法统计页面(针对页面,页面可能是Activity 也可能是Fragment或View)
 */
public class UmengStatistics {
    public static final String TAG = "UmengStatistics";

    private static boolean hasInited = false;

    /**
     * 需要保证线性不交叉
     * 每个 onPageStart 都有一个 onPageEnd 配对。这样才能保证每个页面统计的正确。
     */
    public static void init() {
        if (hasInited) {
            return;
        }
        synchronized (UmengStatistics.class) {
            if (hasInited) {
                return;
            }
            Log.d(TAG, "UmengInit init 友盟初始化开始 " + U.getProcessName());
            UMConfigure.init(U.app(), "5bf40cc8f1f556f36200032b"
                    , U.getChannelUtils().getChannel(), UMConfigure.DEVICE_TYPE_PHONE, "34d3e8844e007050b8a968d974f1adee");
            //MobclickAgent.setScenarioType(U.app(), MobclickAgent.EScenarioType.E_UM_NORMAL);
            // 选用MANUAL页面采集模式
            //默认SDK对Activity页面进行自动统计，现在我们自己统计，所以需要把自动统计关掉
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);
            //UmengPush.pushInit();
            /**
             * 组件化统计SDK内建JVM层错误统计。SDK通过Thread.UncaughtExceptionHandler 捕获程序崩溃日志，并在程序下次启动时发送到服务器。
             * 如不需要错误统计功能，可通过此方法关闭：
             * 代码:复制代码到剪切板
             * isEnable: false-关闭错误统计功能；true-打开错误统计功能（默认打开）
             *
             */
            /**
             * 打开bug统计，umeng只统计java层的，原理也是
             * Thread.setDefaultUncaughtExceptionHandler();
             * 是用组合的模式，不会覆盖已有的 handler
             */
            MobclickAgent.setCatchUncaughtExceptions(false);
            hasInited = true;
        }
    }

    /**
     * 记录某个页面被打开，并在sdk内部会创建一个session
     *
     * @param context 必填
     * @param key
     */
    public static void recordSessionStart(Context context, String key) {
        MyLog.d(TAG, "recordSessionStart" + " key=" + key);
        init();
        MobclickAgent.onResume(context);
    }


    /**
     * 记录某个页面被关闭
     *
     * @param context
     * @param key     必填，确保与recordActivityPageStart的一致
     */
    public static void recordSessionEnd(Context context, String key) {
        MyLog.d(TAG, "recordSessionEnd" + " key=" + key);
        init();
        MobclickAgent.onPause(context);
    }

    /**
     * 页面统计开始
     *
     * @param pageName
     */
    public static void recordPageStart(String pageName) {
        MyLog.d(TAG, "recordPageStart" + " pageName=" + pageName);
        init();
        MobclickAgent.onPageStart(pageName);
    }

    /**
     * 页面统计结束
     *
     * @param pageName
     */
    public static void recordPageEnd(String pageName) {
        MyLog.d(TAG, "recordPageEnd" + " pageName=" + pageName);
        init();
        MobclickAgent.onPageEnd(pageName);
    }

    /**
     * 登录登出时调用这个进行统计
     * 但这个统计一般服务端都有
     *
     * @param from
     * @param userId
     */
    public static void onProfileSignIn(String from, String userId) {
        init();
        MobclickAgent.onProfileSignIn(from, userId);
    }

    public static void onProfileSignOff() {
        init();
        MobclickAgent.onProfileSignOff();
    }

    /**
     * 计数事件
     *
     * @param eventId
     * @param param
     */
    public static void recordCountEvent(String eventId, Map<String, String> param) {
        MyLog.d(TAG,"recordCountEvent" + " eventId=" + eventId + " param=" + param);
        if (TextUtils.isEmpty(eventId)) {
            MyLog.w(TAG, "eventid is empty");
            return;
        }
        init();

        if(param == null || param.isEmpty()){
            MobclickAgent.onEvent(U.app().getApplicationContext(), eventId);
        }else {
            MobclickAgent.onEvent(U.app().getApplicationContext(), eventId, param);
        }
    }

    /**
     * 计算事件
     *
     * @param eventId
     * @param param
     * @param cal     需要计算的
     */
    public static void recordCalculateEvent(String eventId, Map<String, String> param, int cal) {
        if (TextUtils.isEmpty(eventId)) {
            MyLog.w(TAG, "eventid is empty");
            return;
        }
        init();
        MobclickAgent.onEventValue(U.app().getApplicationContext(), eventId, param, cal);
    }
}
