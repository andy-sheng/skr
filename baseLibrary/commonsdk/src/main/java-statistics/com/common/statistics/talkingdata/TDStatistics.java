package com.common.statistics.talkingdata;

import android.content.Context;
import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.U;
import com.tendcloud.tenddata.TCAgent;
import com.tendcloud.tenddata.TDAccount;

import java.util.Map;

public class TDStatistics {
    public static final String TAG = "TDStatistics";

    private static boolean hasInited = false;

    public static void init() {
        if (hasInited) {
            return;
        }
        synchronized (TDStatistics.class) {
            if (hasInited) {
                return;
            }
            TCAgent.LOG_ON = MyLog.isDebugLogOpen();
            // App ID: 在TalkingData创建应用后，进入数据报表页中，在“系统设置”-“编辑应用”页面里查看App ID。
            // 渠道 ID: 是渠道标识符，可通过不同渠道单独追踪数据。
            TCAgent.init(U.app(), "D09AC4EF0EEF449DB54C9089F6F68394", U.getChannelUtils().getChannel());
            // 如果已经在AndroidManifest.xml配置了App ID和渠道ID，调用TCAgent.init(this)即可；或与AndroidManifest.xml中的对应参数保持一致。
            TCAgent.setReportUncaughtExceptions(false);
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
    }

    /**
     * 页面统计开始
     *
     * @param pageName
     */
    public static void recordPageStart(String pageName) {
        MyLog.d(TAG, "recordPageStart" + " pageName=" + pageName);
        init();
    }

    /**
     * 页面统计结束
     *
     * @param pageName
     */
    public static void recordPageEnd(String pageName) {
        MyLog.d(TAG, "recordPageEnd" + " pageName=" + pageName);
        init();
    }

    public static void onProfileSignIn(String accountId, TDAccount.AccountType type, String name, boolean first) {
        init();
        if (first) {
            TCAgent.onRegister(accountId, type, name);
        }
        TCAgent.onLogin(accountId, type, name);
    }

    public static void onProfileSignOff() {
        init();
    }

    /**
     * 计数事件
     *
     * @param eventId
     * @param param
     */
    public static void recordCountEvent(String eventId, Map<String, String> param) {
        MyLog.d(TAG, "recordCountEvent" + " eventId=" + eventId + " param=" + param);
        if (TextUtils.isEmpty(eventId)) {
            MyLog.w(TAG, "eventid is empty");
            return;
        }
        init();

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
    }
}
