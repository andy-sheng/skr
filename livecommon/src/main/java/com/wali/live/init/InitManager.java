package com.wali.live.init;

import android.app.Application;

import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.Constants;
import com.base.utils.sdcard.SDCardUtils;
import com.base.utils.version.VersionManager;
import com.facebook.drawee.backends.pipeline.BuildConfig;
import com.live.module.common.R;
import com.mi.live.data.gift.handler.GiftPacketHandler;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.milink.sdk.base.debug.TraceLevel;
import com.mi.milink.sdk.debug.MiLinkLog;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.dns.PreDnsManager;
import com.wali.live.fresco.FrescoManager;
import com.wali.live.log.LogHandler;
import com.wali.live.service.PacketProcessService;
import com.wali.live.utils.ReplayBarrageMessageManager;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 16/12/1.
 */
public class InitManager {
    private static final String TAG = InitManager.class.getSimpleName();

    private static boolean hasInit = false;
    private static boolean hasInitForCoreProcess = false;

    private static String LOGTAG = "WatchSdk";

    public static void init() {
        if (!hasInit) {
            initMiLinkPacketHandler();
            ThreadPool.startup();
            ThreadPool.getWorkerHandler().post(new Runnable() {
                @Override
                public void run() {
                    initLogger();
                    SDCardUtils.generateDirectory();
                    hasInit = true;
                }
            });
        }
    }

    public static void initForCoreProcess(Application context) {
        if (!hasInitForCoreProcess) {
            LOGTAG = "WatchSdk_" + context.getPackageName();
            InitManager.registerAllEventBus();
            PacketProcessService.startPacketProcessService(context);
            InitManager.init();
            FrescoManager.initFresco(context);
            MyLog.w("version_for_qa " + VersionManager.getReleaseChannel(context) + " " + VersionManager.getVersionName(context) +
                    " " + VersionManager.getCurrentVersionCode(context) + " " + context.getResources().getString(R.string.app_name));
            initLibrary();
            hasInitForCoreProcess = true;
        }
    }

    private static void initMiLinkPacketHandler() {
        MiLinkClientAdapter.getsInstance().addPacketDataHandler(BarrageMessageManager.getInstance());
        MiLinkClientAdapter.getsInstance().addPacketDataHandler(new GiftPacketHandler());
        MiLinkClientAdapter.getsInstance().addPacketDataHandler(ReplayBarrageMessageManager.getInstance());
        MiLinkClientAdapter.getsInstance().addPacketDataHandler(new LogHandler());
    }

    public static void registerAllEventBus() {
        EventBus.getDefault().register(PreDnsManager.INSTANCE);
        EventBus.getDefault().register(EventBusDelegate.getInstance());
    }

    private static void initLibrary() {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("broadcast");
    }

    public static void initLogger() {
        if (BuildConfig.DEBUG
                || Constants.isTestBuild
                || Constants.isDailyBuild
                || Constants.isDebugBuild) {
            setAppAndMilinkLogLevel(TraceLevel.ALL);
        } else {
            setAppAndMilinkLogLevel(TraceLevel.ABOVE_INFO);
        }
    }

    /**
     * 同时设置app和milink日志
     *
     * @param logLevel
     */
    public static void setAppAndMilinkLogLevel(int logLevel) {
        if (logLevel > TraceLevel.ALL || logLevel < TraceLevel.VERBOSE) {
            logLevel = TraceLevel.ALL;
        }

        setAppLogLevel(logLevel);

        setMilinkLogLevel(logLevel);
    }

    /**
     * 设置app log级别
     *
     * @param logLevel
     */
    public static void setAppLogLevel(int logLevel) {
        if (logLevel > TraceLevel.ALL || logLevel < TraceLevel.VERBOSE) {
            logLevel = TraceLevel.ALL;
        }

        MyLog.setLogcatTraceLevel(logLevel, LOGTAG);
//        MyLog.setFileTraceLevel(logLevel);
        MiLinkLog.setLogcatTraceLevel(logLevel);
        MiLinkLog.setFileTraceLevel(logLevel);
    }

    /**
     * 设置milink log级别
     *
     * @param logLevel
     */
    public static void setMilinkLogLevel(int logLevel) {
        if (logLevel > TraceLevel.ALL || logLevel < TraceLevel.VERBOSE) {
            logLevel = TraceLevel.ALL;
        }

        MiLinkClientAdapter.getsInstance().setMilinkLogLevel(logLevel);

    }
}
