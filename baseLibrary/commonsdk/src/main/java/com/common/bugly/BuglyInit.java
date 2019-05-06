package com.common.bugly;

import com.common.log.MyLog;
import com.common.utils.U;
import com.tencent.bugly.crashreport.CrashReport;

public class BuglyInit {
    public static void init(boolean coreProess){
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(U.app());
        strategy.setUploadProcess(coreProess);
        CrashReport.initCrashReport(U.app(), "75917797f3", MyLog.isDebugLogOpen());
    }
}
