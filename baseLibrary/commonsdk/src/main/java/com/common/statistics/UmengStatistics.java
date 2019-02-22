package com.common.statistics;

import android.content.Context;
import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.umeng.UmengInit;
import com.common.utils.U;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.Map;

/**
 * 使用StatisticsAdapter
 */
public class UmengStatistics {
    public static final String TAG = "UmengStatistics";


    private static void init() {
        UmengInit.init();
    }

    /**
     * 记录某个页面被打开，并在sdk内部会创建一个session
     *
     * @param context 必填
     * @param key
     */
    static void recordSessionStart(Context context, String key) {
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
    static void recordSessionEnd(Context context, String key) {
        MyLog.d(TAG, "recordSessionEnd" + " key=" + key);
        init();
        MobclickAgent.onPause(context);
    }

    /**
     * 页面统计开始
     *
     * @param pageName
     */
    static void recordPageStart(String pageName) {
        MyLog.d(TAG, "recordPageStart" + " pageName=" + pageName);
        init();
        MobclickAgent.onPageStart(pageName);
    }

    /**
     * 页面统计结束
     *
     * @param pageName
     */
    static void recordPageEnd(String pageName) {
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
    static void recordCountEvent(String eventId, Map<String, String> param) {
        if (TextUtils.isEmpty(eventId)) {
            MyLog.w(TAG, "eventid is empty");
            return;
        }
        init();
        MobclickAgent.onEvent(U.app().getApplicationContext(), eventId, param);
    }

    /**
     * 计算事件
     *
     * @param eventId
     * @param param
     * @param cal     需要计算的
     */
    static void recordCalculateEvent(String eventId, Map<String, String> param, int cal) {
        if (TextUtils.isEmpty(eventId)) {
            MyLog.w(TAG, "eventid is empty");
            return;
        }
        init();
        MobclickAgent.onEventValue(U.app().getApplicationContext(), eventId, param, cal);
    }
}
