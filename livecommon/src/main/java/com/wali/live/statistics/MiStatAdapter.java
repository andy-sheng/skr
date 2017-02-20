package com.wali.live.statistics;

import android.content.Context;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.channel.ReleaseChannelUtils;
import com.mi.live.data.account.UserAccountManager;
import com.xiaomi.mistatistic.sdk.MiStatInterface;
import com.xiaomi.mistatistic.sdk.URLStatsRecorder;
import com.xiaomi.mistatistic.sdk.controller.HttpEventFilter;
import com.xiaomi.mistatistic.sdk.data.HttpEvent;

/**
 * Created by linjinbin on 16/3/28.
 */
public class MiStatAdapter {
    static boolean sHasInitStats = false;

    public static void initMiStatsFromActivity() {
        if (!sHasInitStats) {
            sHasInitStats = true;
            MiStatInterface.initialize(GlobalData.app(), Constants.MI_STATISTIC_APPID, Constants.MI_STATISTIC_APPKEY,
                    ReleaseChannelUtils.getReleaseChannel());
            MiStatInterface.setUploadPolicy(
                    MiStatInterface.UPLOAD_POLICY_BATCH, 0);
            MiStatInterface.enableLog();

            // enable exception catcher.
            MiStatInterface.enableExceptionCatcher(true);

            // enable network monitor
            URLStatsRecorder.enableAutoRecord();
            URLStatsRecorder.setEventFilter(new HttpEventFilter() {

                @Override
                public HttpEvent onEvent(HttpEvent event) {
//                    MyLog.v("MI_STA onEvent " + event.getUrl() + " result =" + event.toJSON());
                    // returns null if you want to drop this event.
                    // you can modify it here too.
                    return event;
                }
            });

            MyLog.v("MI_STAT" + MiStatInterface.getDeviceID(GlobalData.app()) + " is the device.");
        }
    }

    public static void initMiStats() {
        if (!sHasInitStats && UserAccountManager.getInstance().hasAccount()) {
            sHasInitStats = true;
            MiStatInterface.initialize(GlobalData.app(), Constants.MI_STATISTIC_APPID, Constants.MI_STATISTIC_APPKEY,
                    ReleaseChannelUtils.getReleaseChannel());
            MiStatInterface.setUploadPolicy(
                    MiStatInterface.UPLOAD_POLICY_BATCH, 0);
            MiStatInterface.enableLog();

            // enable exception catcher.
            MiStatInterface.enableExceptionCatcher(true);

            // enable network monitor
            URLStatsRecorder.enableAutoRecord();
            URLStatsRecorder.setEventFilter(new HttpEventFilter() {

                @Override
                public HttpEvent onEvent(HttpEvent event) {
//                    MyLog.v("MI_STA onEvent " + event.getUrl() + " result =" + event.toJSON());
                    // returns null if you want to drop this event.
                    // you can modify it here too.
                    return event;
                }
            });

            MyLog.v("MI_STAT" + MiStatInterface.getDeviceID(GlobalData.app()) + " is the device.");
        }
    }

    public static void recordPageEnd(Context context, String className) {
        if (UserAccountManager.getInstance().hasAccount()) {
            if (!sHasInitStats) {
                initMiStatsFromActivity();
            }
            try {
                MiStatInterface.recordPageEnd(context, className);
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

    public static void recordPageStart(Context context, String className) {
        if (UserAccountManager.getInstance().hasAccount()) {
            if (!sHasInitStats) {
                initMiStatsFromActivity();
            }
            try {
                MiStatInterface.recordPageStart(context, className);
            } catch (java.lang.IllegalStateException e) {
                initMiStatsFromActivity();
            }
        }
    }

    public static final void recordCalculateEvent(String var0, String var1, long var2) {
        if (UserAccountManager.getInstance().hasAccount()) {
            if (!sHasInitStats) {
                initMiStatsFromActivity();
            }
            try {
                MiStatInterface.recordCalculateEvent(var0, var1, var2, null);
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

    public static final void recordNumericPropertyEvent(String var0, String var1, long var2) {
        if (UserAccountManager.getInstance().hasAccount()) {
            if (!sHasInitStats) {
                initMiStatsFromActivity();
            }
            try {
                MiStatInterface.recordNumericPropertyEvent(var0, var1, var2);
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

}
