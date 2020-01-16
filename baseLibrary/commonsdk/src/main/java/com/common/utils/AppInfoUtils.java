package com.common.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.common.base.BuildConfig;
import com.common.log.MyLog;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * 通过U.getAppInfoUtils去用
 * 获得本app的一些信息，手机上别的app用{@link DeviceUtils}
 */
public class AppInfoUtils {
    public final String TAG = "AppInfoUtils";

    AppInfoUtils() {

    }

    String appName;
    int versionCode;
    String versionName;

    File mainFile;

    /**
     * 存储的主目录,所有的存储请基于此目录
     *
     * 上面这些方法，我们可能似曾相识，但是对于有些同学来说却又很难分清出，主要还是不同的Android版本的问题。为了方便大家理解，我先简要介绍以上各个方法，为方便大家理解我把这些方法的结果打印出来（以下的打印结果是基于荣耀7的（系统版本6.0）：
     * 1、Environment.getDataDirectory() = /data
     * 这个方法是获取内部存储的根路径
     * 2、getFilesDir().getAbsolutePath() = /data/user/0/packname/files
     * 这个方法是获取某个应用在内部存储中的files路径
     * 3、getCacheDir().getAbsolutePath() = /data/user/0/packname/cache
     * 这个方法是获取某个应用在内部存储中的cache路径
     * 4、getDir(“myFile”, MODE_PRIVATE).getAbsolutePath() = /data/user/0/packname/app_myFile
     * 这个方法是获取某个应用在内部存储中的自定义路径
     * 方法2,3,4的路径中都带有包名，说明他们是属于某个应用
     * …………………………………………………………………………………………
     * 5、Environment.getExternalStorageDirectory().getAbsolutePath() = /storage/emulated/0
     * 这个方法是获取外部存储的根路径
     * 6、Environment.getExternalStoragePublicDirectory(“”).getAbsolutePath() = /storage/emulated/0
     * 这个方法是获取外部存储的根路径
     * 7、getExternalFilesDir(“”).getAbsolutePath() = /storage/emulated/0/Android/data/packname/files
     * 这个方法是获取某个应用在外部存储中的files路径
     * 8、getExternalCacheDir().getAbsolutePath() = /storage/emulated/0/Android/data/packname/cache
     * 这个方法是获取某个应用在外部存储中的cache路径
     *
     * @return
     */
    public File getMainDir() {
        if (mainFile != null) {
            return mainFile;
        }
        if (U.getDeviceUtils().existSDCard()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                mainFile = U.app().getExternalFilesDir("");
                return mainFile;
            }else{
                mainFile = new File(Environment.getExternalStorageDirectory(), "ZQ_LIVE");
                return mainFile;
            }
        } else {
            return U.app().getFilesDir();
        }
    }

    /**
     * 返回 类似 /ZQ_Live/logs/
     *
     * @param dirName
     * @return
     */
    public String getSubDirPath(String dirName) {
        return getMainDir().getAbsolutePath() + File.separator + dirName + File.separator;
    }

    public File getSubDirFile(String dirName) {
        return new File(getMainDir().getAbsolutePath() + File.separator + dirName + File.separator);
    }

    /**
     * 返回 类似 /ZQ_Live/logs/aaa.png
     *
     * @param dirName
     * @return
     */
    public String getFilePathInSubDir(String dirName, String fileName) {
        return getMainDir().getAbsolutePath() + File.separator + dirName + File.separator + fileName;
    }

    /**
     * 获取应用程序名称
     * 如 直播助手
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
     * 获取应用程序桌面图标
     * 如 直播助手
     *
     * @return
     */
    public Drawable getAppIcon() {
        PackageManager pm = U.app().getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(U.app().getPackageName(), 0);
            ApplicationInfo ai = pi.applicationInfo;
            Drawable icon = ai.loadIcon(pm);
            return icon;
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
            System.out.println("getMetaInfo:" + e);
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
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        return versionCode;
    }

    public String getVersionName() {
        try {
            versionName = U.app().getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
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

    public String getDebugDBAddressLog() {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                return (String) value;
            } catch (Exception ignore) {

            }
        }
        return "";
    }

    public Signature[] getAppSignature() {
        try {
            PackageManager pm = U.app().getPackageManager();
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo pi = pm.getPackageInfo(U.app().getPackageName(), PackageManager.GET_SIGNATURES);
            return pi == null ? null : pi.signatures;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getAppSignatureSha1() {
        try {
            Signature signatures[] = getAppSignature();
            if (signatures != null && signatures.length > 0) {
                byte[] cert = signatures[0].toByteArray();
                MessageDigest md = MessageDigest.getInstance("SHA1");
                byte[] publicKey = md.digest(cert);
                StringBuffer hexString = new StringBuffer();
                for (int i = 0; i < publicKey.length; i++) {
                    String appendString = Integer.toHexString(0xFF & publicKey[i])
                            .toUpperCase(Locale.US);
                    if (appendString.length() == 1)
                        hexString.append("0");
                    hexString.append(appendString);
                    hexString.append(":");
                }
                String result = hexString.toString();
                return result.substring(0, result.length() - 1);
            }

        } catch (NoSuchAlgorithmException var3) {
        }
        return "";
    }

}
