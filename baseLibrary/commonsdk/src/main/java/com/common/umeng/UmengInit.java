package com.common.umeng;

import android.util.Log;

import com.common.statistics.UmengStatistics;
import com.common.utils.U;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class UmengInit {
    public final static String TAG = "UmengInit";
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
            MobclickAgent.setCatchUncaughtExceptions(true);
            hasInited = true;
        }
    }



}
