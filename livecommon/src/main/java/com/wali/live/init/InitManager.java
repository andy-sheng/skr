package com.wali.live.init;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.Constants;
import com.base.utils.channel.ReleaseChannelUtils;
import com.base.utils.language.LocaleUtil;
import com.base.utils.sdcard.SDCardUtils;
import com.base.utils.version.VersionManager;
import com.facebook.drawee.backends.pipeline.BuildConfig;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.gift.handler.GiftPacketHandler;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.milink.sdk.base.Global;
import com.mi.milink.sdk.base.debug.TraceLevel;
import com.mi.milink.sdk.data.ClientAppInfo;
import com.mi.milink.sdk.debug.MiLinkLog;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.dns.PreDnsManager;
import com.wali.live.fresco.FrescoManager;
import com.wali.live.log.LogHandler;
import com.wali.live.service.PacketProcessService;
import com.wali.live.utils.ReplayBarrageMessageManager;
import com.xsj.crasheye.Crasheye;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by lan on 16/12/1.
 */
public class InitManager {
    private static final String TAG = InitManager.class.getSimpleName();

    private static String sPackageName;
    private static String sRemoteProcessName;
    private static String LOGTAG = "WatchSdk";

    public static void init(@NonNull Application application) {
        GlobalData.setApplication(application);

        sPackageName = application.getPackageName();
        sRemoteProcessName = sPackageName + ":remote";

        int pid = android.os.Process.myPid();
        ActivityManager am = ((ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();

        boolean isCoreProcess = false;
        boolean isRemoteProcess = false;
        if (processInfos != null) {
            for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                if (info.pid == pid) {
                    if (sPackageName.equals(info.processName)) {
                        isCoreProcess = true;
                        break;
                    } else if (sRemoteProcessName.equals(info.processName)) {
                        isRemoteProcess = true;
                        break;
                    }
                }
            }
        }

        MyLog.d(TAG, "onCreate isRemoteProcess=" + isRemoteProcess);
        Global.init(application, getClientAppInfo());

        if (isCoreProcess) {
            LOGTAG = "WatchSdk_" + sPackageName;
            MyLog.d(TAG, "onCreate coreProcess: " + sPackageName);
            FrescoManager.initFresco(application);
            PacketProcessService.startPacketProcessService(application);

            // 初始化账号,先用匿名
            UserAccountManager.getInstance().initAnonymous();
            ThreadPool.startup();
            initLibrary();
            initMiLinkPacketHandler();
            registerAllEventBus();
            initCrasheye();
            ThreadPool.runOnWorker(new Runnable() {
                @Override
                public void run() {
                    initLogger();
                    SDCardUtils.generateDirectory();
                }
            });
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

    private static ClientAppInfo getClientAppInfo() {
        return new ClientAppInfo.Builder(Constants.MILINK_APP_ID)
                .setAppName(Constants.APPNAME)
                .setPackageName(sPackageName)
                .setReleaseChannel(Constants.ReleaseChannel)
                .setVersionName(VersionManager.getVersionName(GlobalData.app()))
                .setVersionCode(VersionManager.getCurrentVersionCode(GlobalData.app()))
                .setLanguageCode(LocaleUtil.getLanguageCode())
                .setServiceProcessName(sRemoteProcessName)
                .build();
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

    private static void initCrasheye() {
        Crasheye.init(GlobalData.app(), Constants.CRASHEYE_APPID);
        Crasheye.setChannelID(ReleaseChannelUtils.getReleaseChannel());
        Crasheye.setUserIdentifier(UserAccountManager.getInstance().getUuid());
    }

    /**
     * 同时设置app和milink日志
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
     */
    public static void setMilinkLogLevel(int logLevel) {
        if (logLevel > TraceLevel.ALL || logLevel < TraceLevel.VERBOSE) {
            logLevel = TraceLevel.ALL;
        }

        MiLinkClientAdapter.getsInstance().setMilinkLogLevel(logLevel);
    }
}
