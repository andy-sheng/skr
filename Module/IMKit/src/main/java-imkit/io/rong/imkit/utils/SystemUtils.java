//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Process;

import java.util.Iterator;
import java.util.List;

public class SystemUtils {
    public SystemUtils() {
    }

    public static String getCurProcessName(Context context) {
        int pid = Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService("activity");
        List<RunningAppProcessInfo> runningAppProcessInfos = mActivityManager.getRunningAppProcesses();
        if (runningAppProcessInfos == null) {
            return null;
        } else {
            Iterator var4 = runningAppProcessInfos.iterator();

            RunningAppProcessInfo appProcess;
            do {
                if (!var4.hasNext()) {
                    return null;
                }

                appProcess = (RunningAppProcessInfo) var4.next();
            } while (appProcess.pid != pid);

            return appProcess.processName;
        }
    }

    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException var4) {
            var4.printStackTrace();
            return null;
        }
    }
}
