package com.base.utils.system;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.MD5;

import java.io.File;

/**
 * Created by lan on 2017/6/14.
 */
public class PackageUtils {
    public final static String TAG = "PackageUtils";

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


    /**
     * 是否是一个完整的apk
     *
     * @param apkPath     apk路径
     * @param packageName 包名
     * @return
     */
    public static boolean isCompletedPackage(String apkPath, String packageName) {
        File file = new File(apkPath);
        if (!file.exists()) {
            return false;
        }
        try {
            PackageManager pm = GlobalData.app().getApplicationContext().getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (packageInfo == null) {
                return false;
            }
            if (!TextUtils.equals(packageName, packageInfo.packageName)) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean tryInstall(String apkPath){
        if(!new File(apkPath).exists()){
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri;
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 待补充
            uri = FileProvider.getUriForFile(GlobalData.app().getApplicationContext(), "com.wali.live.watchsdk.editinfo.fileprovider", new File(apkPath));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(new File(apkPath));
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        if (intent.resolveActivity(GlobalData.app().getPackageManager()) != null) {
            try {
                GlobalData.app().startActivity(intent);
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    public static boolean tryLaunch(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        Intent intent = GlobalData.app().getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            if (intent.resolveActivity(GlobalData.app().getPackageManager()) != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                GlobalData.app().startActivity(intent);
                return true;
            }
        } else {
            MyLog.w(TAG, "intent launch fail, packageName=" + packageName);
        }
        return false;
    }
}
