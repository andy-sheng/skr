package com.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.common.base.BuildConfig;
import com.common.log.MyLog;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 通过U.getAppInfoUtils去用
 */
public class AppInfoUtils {
    public final static String TAG = "AppInfoUtils";

    AppInfoUtils() {

    }

    String appName;
    int versionCode;
    String versionName;

    /**
     * 获取应用程序名称
     *
     * @return
     */
    public String getAppName() {
        if (!TextUtils.isEmpty(appName)) {
            return appName;
        }
        try {
            PackageManager packageManager = U.app().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    U.app().getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return U.app().getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取application节点下的meta信息
     *
     * @param params 想要获取的参数
     * @return
     */
    public String getMetaInfo(String params) {

        String packageName = U.app().getPackageName();
        // 获取application里面的meta信息
        try {
            ApplicationInfo appInfo = U.app().getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_META_DATA);
            return appInfo.metaData.getString(params);
        } catch (Exception e) {
            System.out.println("获取渠道失败:" + e);
            e.printStackTrace();
        }
        return null;
    }

    public String getPackageName() {
        return U.app().getPackageName();
    }


    public int getVersionCode() {
        try {
            versionCode = U.app().getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            MyLog.e(TAG, e);
        }
        return versionCode;
    }

    public String getVersionName() {
        try {
            versionName = U.app().getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            MyLog.e(TAG, e);
        }
        return versionName;
    }


    /**
     * 获取应用地区的字符串表示，例如zh_CN
     *
     * @return
     */
    public String getLanguageCode() {
        return Locale.getDefault().toString();
    }

    public void showDebugDBAddressLogToast() {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                Toast.makeText(U.app(), "debugdb:" + value, Toast.LENGTH_LONG).show();
            } catch (Exception ignore) {

            }
        }
    }

}
