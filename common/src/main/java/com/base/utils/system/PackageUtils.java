package com.base.utils.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.base.global.GlobalData;

/**
 * Created by lan on 2017/6/14.
 */
public class PackageUtils {
    public static boolean isInstallPackage(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        PackageInfo pInfo = null;
        try {
            pInfo = GlobalData.app().getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return pInfo != null;
    }
}
