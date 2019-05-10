package com.common.bugly;

import com.common.log.MyLog;
import com.common.utils.U;
import com.tencent.bugly.crashreport.CrashReport;

public class BuglyInit {
    public static void init(boolean coreProess){
        CrashReport.setIsDevelopmentDevice(U.app(), MyLog.isDebugLogOpen());
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(U.app());
        strategy.setAppChannel(U.getChannelUtils().getChannel());  //设置渠道
//        strategy.setAppVersion(U.getAppInfoUtils().getVersionName());      //App的版本
//        strategy.setAppPackageName(U.getAppInfoUtils().getVersionName());  //App的包名
        strategy.setUploadProcess(coreProess);
        strategy.setEnableANRCrashMonitor(true);
        strategy.setEnableNativeCrashMonitor(true);
        CrashReport.initCrashReport(U.app(), "75917797f3", MyLog.isDebugLogOpen());
    }
}
