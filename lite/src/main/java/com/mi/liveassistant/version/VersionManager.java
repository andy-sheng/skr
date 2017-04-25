package com.mi.liveassistant.version;

import android.content.Context;

/**
 * Created by lan on 17/4/25.
 */
public class VersionManager {
    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
        }
        return "";
    }

    public static int getCurrentVersionCode(Context context) {
        int code = 0;
        try {
            code = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
        }
        return code;
    }
}
