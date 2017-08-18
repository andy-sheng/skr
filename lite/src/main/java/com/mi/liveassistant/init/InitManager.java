package com.mi.liveassistant.init;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.barrage.manager.BarragePushMessageManager;
import com.mi.liveassistant.common.filesystem.SDCardUtils;
import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.language.LocaleUtil;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.thread.ThreadPool;
import com.mi.liveassistant.config.Constants;
import com.mi.liveassistant.dns.PreDnsManager;
import com.mi.liveassistant.account.AccountManager;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.version.VersionManager;
import com.mi.milink.sdk.base.Global;
import com.mi.milink.sdk.base.debug.TraceLevel;
import com.mi.milink.sdk.data.ClientAppInfo;
import com.mi.milink.sdk.debug.MiLinkLog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by lan on 16/12/1.
 */
public class InitManager {
    private static final String TAG = InitManager.class.getSimpleName();

    private static String sPackageName;
    private static String sRemoteProcessName;
    private static String LOGTAG = "Lite";

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
            LOGTAG = "Lite_" + sPackageName;
            MyLog.d(TAG, "onCreate coreProcess: " + sPackageName);

            // 初始化账号,先用匿名
            UserAccountManager.getInstance().initAnonymous();
            ThreadPool.startup();
            initLibrary();
            initMiLinkPacketHandler();
            registerAllEventBus();
            ThreadPool.runOnWorker(new Runnable() {
                @Override
                public void run() {
                    initLogger();
                    SDCardUtils.generateDirectory();
                }
            });
        }
    }

    private static void registerAllEventBus() {
        EventBus.getDefault().register(InitDaemon.INSTANCE);
        EventBus.getDefault().register(PreDnsManager.INSTANCE);
        EventBus.getDefault().register(AccountManager.getInstance());
    }

    private static void initLibrary() {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("broadcast");
        System.loadLibrary("milive_transport");
    }

    private static void initMiLinkPacketHandler() {
        MiLinkClientAdapter.getsInstance().addPacketDataHandler(BarragePushMessageManager.getInstance());
    }

    private static ClientAppInfo getClientAppInfo() {
        return new ClientAppInfo.Builder(Constants.MILINK_APP_ID)
                .setAppName(Constants.APP_NAME)
                .setPackageName(sPackageName)
                .setReleaseChannel(Constants.ReleaseChannel)
                .setVersionName(VersionManager.getVersionName(GlobalData.app()))
                .setVersionCode(VersionManager.getCurrentVersionCode(GlobalData.app()))
                .setLanguageCode(LocaleUtil.getLanguageCode())
                .setServiceProcessName(sRemoteProcessName)
                .build();
    }

    private static void initLogger() {
        if (Constants.isTestBuild
                || Constants.isDailyBuild
                || Constants.isDebugBuild) {
            setAppAndMilinkLogLevel(TraceLevel.ALL);
        } else {
            setAppAndMilinkLogLevel(TraceLevel.ABOVE_INFO);
        }
    }

    /**
     * 同时设置app和milink日志
     */
    private static void setAppAndMilinkLogLevel(int logLevel) {
        if (logLevel > TraceLevel.ALL || logLevel < TraceLevel.VERBOSE) {
            logLevel = TraceLevel.ALL;
        }

        setAppLogLevel(logLevel);
        setMilinkLogLevel(logLevel);
    }

    /**
     * 设置app log级别
     */
    private static void setAppLogLevel(int logLevel) {
        if (logLevel > TraceLevel.ALL || logLevel < TraceLevel.VERBOSE) {
            logLevel = TraceLevel.ALL;
        }

        MyLog.setLogcatTraceLevel(logLevel, LOGTAG);
        MiLinkLog.setLogcatTraceLevel(logLevel);
        MiLinkLog.setFileTraceLevel(logLevel);
    }

    /**
     * 设置milink log级别
     */
    private static void setMilinkLogLevel(int logLevel) {
        if (logLevel > TraceLevel.ALL || logLevel < TraceLevel.VERBOSE) {
            logLevel = TraceLevel.ALL;
        }

        MiLinkClientAdapter.getsInstance().setMilinkLogLevel(logLevel);
    }
}
