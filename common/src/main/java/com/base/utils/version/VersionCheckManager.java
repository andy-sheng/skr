package com.base.utils.version;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.IntRange;
import android.support.annotation.Keep;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;


import com.base.common.R;
import com.base.global.GlobalData;
import com.base.log.logger.Logger;
import com.base.utils.CommonUtils;
import com.base.utils.FileIOUtils;
import com.base.utils.IOUtils;
import com.base.utils.StringUtils;
import com.base.version.http.HttpUtils;
import com.base.version.http.SimpleRequest;
import com.base.version.http.bean.BasicNameValuePair;
import com.base.version.http.bean.NameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class VersionCheckManager {
    public final static String TAG = VersionCheckManager.class.getSimpleName();
    public final static long CHECKTIME = 2 * 60 * 1000;

    public static final int HAS_UPGRADE = 1;
    public static final int NO_UPGRADE = 2;
    public static final int CHECK_FAILED = 3;
    public static final int IS_UPGRADING = 4;
    public static final int HAS_FORCE_UPGRADE = 0;

    private static final String CHECK_GRAY_UPGRADE_INFO = "http://api.chat.xiaomi.net/v2/user/%s/grayupgarde";

    public static final String PACKAGE_NAME = "com.mi.liveassistant";

    private static final String APP_NAME = "liveassistant";
    /*Staging APP NAME*/
    private static final String STAGING_NAME = "liveassistantstaging";

    private static final String APP_PLATFORM = "android";

    /*SharedPreferences FileName & Key*/
    private static final String PREF_FILE_NAME = "liveassistant_upgrade";
    private static final String PREF_SHOW_UPGRADE_DIALOG = "show_upgrade_dialog";
    private static final String PREF_LAST_CHECK = "last_check";

    private static VersionCheckManager sInstance;

    private int mRemoteAppVersion = -1;
    private String mRemoteApkUrl;
    private String mUpdateMessage;
    private String mAdditionalUrl;
    private int mAdditionalSize;
    private String mFullPackageHash;
    private boolean mIsAdditionalUpgrade; // 增量升级

    private boolean mIsUpgrading = false;
    private boolean mNeedUpdateApp = false;

    private boolean mIsStaging = false;

    private boolean mForceUpdate = false;

    public static synchronized VersionCheckManager getInstance() {
        if (sInstance == null) {
            sInstance = new VersionCheckManager();
        }
        return sInstance;
    }

    public int getAdditionalSize() {
        return mAdditionalSize;
    }

    public String getUpdateMessage() {
        return mUpdateMessage;
    }

    public boolean isUpgrading() {
        return mIsUpgrading;
    }

    /**
     * 测试调用的接口，正常线上环境请不要调用
     */
    public void setStaging(boolean isStaging) {
        mIsStaging = isStaging;
    }

    public boolean isForceUpdate(){
        return mForceUpdate;
    }

    public int checkNewVersion() {
        if (mIsUpgrading) {
            return IS_UPGRADING;
        }
        SimpleRequest.StringContent result = getStringContent();
        setCheckTime();
        return parseResult(result);
    }

    public SimpleRequest.StringContent getStringContent() {
        String miId = "0";
        String url = String.format(CHECK_GRAY_UPGRADE_INFO, miId);
        List<NameValuePair> postBody = new ArrayList();
        postBody.add(new BasicNameValuePair("uuid", miId));
        // 注意这里为了测试，增加了STAGING_NAME
        postBody.add(new BasicNameValuePair("app", mIsStaging ? STAGING_NAME : APP_NAME));
        postBody.add(new BasicNameValuePair("platform", APP_PLATFORM));
        postBody.add(new BasicNameValuePair("system", String.valueOf(Build.VERSION.SDK_INT)));
        postBody.add(new BasicNameValuePair("channel", "DEFAULT")); // 渠道号
        postBody.add(new BasicNameValuePair("device", Build.MODEL));

//        int version = getCurrentVersion(GlobalData.app().getApplicationContext());
        int version = 1;
        postBody.add(new BasicNameValuePair("currentVersion", String.valueOf(version)));
        postBody.add(new BasicNameValuePair("language", "zh_CN"));
        postBody.add(new BasicNameValuePair("updateId", "0"));
        postBody.add(new BasicNameValuePair("unique", miId));

        // 发送当前包的hash值
        String packagePath = getPackagePath(GlobalData.app().getApplicationContext());
        String packageHash = getPackageHash(GlobalData.app().getApplicationContext(), packagePath);
        if (!TextUtils.isEmpty(packageHash)) {
            postBody.add(new BasicNameValuePair("hash", packageHash));
        } else {
            postBody.add(new BasicNameValuePair("hash", "5fcc13f203341157dae7469f10b3121a9cb67721")); // 缺个hash会没法下载，随便写个
        }
        SimpleRequest.StringContent result = null;
        try {
            result = HttpUtils.doV2Get(url, postBody);
        } catch (Exception e) {
        }
        return result;
    }

    private int parseResult(SimpleRequest.StringContent result) {
        if (result == null) {
            return CHECK_FAILED;
        }
        String jsonString = result.getBody();
        if (TextUtils.isEmpty(jsonString)) {
            return CHECK_FAILED;
        }
        try {
            JSONObject resultObj = new JSONObject(jsonString);
            if (!resultObj.has("result") || !"ok".equalsIgnoreCase(resultObj.getString("result"))) {
                return CHECK_FAILED;
            }
            JSONObject dataObj = resultObj.getJSONObject("data");

            mRemoteAppVersion = dataObj.getInt("toVersion");
            mRemoteApkUrl = dataObj.getString("downloadUrl");
            mUpdateMessage = dataObj.optString("remark");
            mAdditionalUrl = dataObj.optString("additionalUrl");
            mAdditionalSize = dataObj.optInt("fullSize", -1);
            mFullPackageHash = dataObj.optString("fullHash");
            // additionalUrl不为空，走增量升级
            mIsAdditionalUpgrade = !TextUtils.isEmpty(mAdditionalUrl)
                    && !TextUtils.isEmpty(mFullPackageHash);

            // 别的字段都不下发，也是无奈。 默认这个message以三个!!!结尾就强制更新。

            mForceUpdate = mUpdateMessage.endsWith("!!!");

            boolean shouldUpdate = dataObj.getBoolean("newUpdate");
            if (!shouldUpdate) {
                return NO_UPGRADE;
            }
//            JSONObject custom = dataObj.optJSONObject("custom");
//            mForceUpdate = false;
//            if (custom != null) {
//                mForceUpdate = custom.optBoolean("forced", false);
//            }
            if (mForceUpdate) {
                mNeedUpdateApp = true;
                return HAS_FORCE_UPGRADE;
            } else {
                mNeedUpdateApp = true;
            }
        } catch (JSONException e) {
            return CHECK_FAILED;
        }
        return HAS_UPGRADE;
    }

    public static int getCurrentVersion(Context context) {
        PackageInfo pInfo = getPackageInfo(context);
        return pInfo != null ? pInfo.versionCode : 0;
    }

    public String getCurrentVersionName(Context context) {
        PackageInfo pInfo = getPackageInfo(context);
        return pInfo != null ? pInfo.versionName : null;
    }

    private static PackageInfo getPackageInfo(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    PACKAGE_NAME, PackageManager.GET_META_DATA);
            return pInfo;
        } catch (NameNotFoundException e) {
        }
        return null;
    }

    private static String getPackagePath(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(
                    PACKAGE_NAME, 0).sourceDir;
        } catch (NameNotFoundException e) {
            Logger.e(TAG, "error in get package path...", e);
        }
        return null;
    }

    private static String getPackageHash(Context context, String packagePath) {
        try {
            byte sha1Byte[];
            sha1Byte = IOUtils.getFileSha1Digest(packagePath);
            return sha1Byte != null ? StringUtils.getHexString(sha1Byte) : null;
        } catch (NoSuchAlgorithmException e) {
            Logger.e(TAG, "error in calc package sha1...", e);
        } catch (IOException e) {
            Logger.e(TAG, "error in calc package sha1...", e);
        }
        return null;
    }

    public int getRemoteVersion() {
        return mRemoteAppVersion;
    }

    public void startDownload(final IUpdateListener updateListener) {
        Logger.e(TAG, "startDownload");
        if (TextUtils.isEmpty(mRemoteApkUrl)) {
            int code = checkNewVersion();
            if (TextUtils.isEmpty(mRemoteApkUrl)) {
                if (updateListener != null) {
                    updateListener.onDownloadFailed(-1);
                }
                return;
            }
        }
        if (mIsUpgrading) {
            if (updateListener != null) {
                updateListener.onRepeatedRequest();
            }
            return;
        }
        mIsUpgrading = true;

        final String localFileName = String.format("%s_%d.apk", PACKAGE_NAME, mRemoteAppVersion);//下载完成的文件
        final String downFileName = String.format("%s_%d_down.apk", PACKAGE_NAME, mRemoteAppVersion);//下载中的文件

        if (checkLocalPackage(localFileName)) {
            if (updateListener != null) {
                String path = getCachePath(localFileName);
                updateListener.onDownloadSuccess(path);
                mIsUpgrading = false;
            }
            return;
        } else {
            if (updateListener != null) {
                updateListener.onDownloadStart();
            }
            File destFile = new File(getCachePath(downFileName));

            Logger.e(TAG, "startDownload onDownloadStart");
            HttpUtils.downloadFile(mRemoteApkUrl, destFile,
                    new HttpUtils.OnDownloadProgress() {
                        long lastNotifyTime = 0;
                        final int NOTIFY_GAP = 500;// 刷通知栏时间间隔

                        @Override
                        public void onFailed() {
                            mIsUpgrading = false;
                            if (updateListener != null) {
                                updateListener.onDownloadFailed(-1);
                            }
                        }

                        @Override
                        public void onDownloaded(long downloaded, long totalLength) {
                            if (totalLength <= 0) {
                                return;
                            }
                            int percentage = (int) (downloaded * 100 / totalLength);
                            long now = System.currentTimeMillis();
                            if (now - lastNotifyTime >= NOTIFY_GAP) {
                                lastNotifyTime = now;
                                if (updateListener != null) {
                                    updateListener.onDownloadProgress(percentage);
                                }
                            }
                        }

                        @Override
                        public void onCompleted(String localPath) {
                            mIsUpgrading = false;
                            if (updateListener != null) {
                                File downFile = new File(localPath);
                                File newFile = new File(getCachePath(localFileName));
                                if (newFile.exists()) {
                                    newFile.delete();
                                }
                                boolean r = downFile.renameTo(newFile);
                                if (!r) {
                                    try {
                                        CommonUtils.copyFile(downFile.getPath(), newFile.getPath());
                                    } catch (Exception e) {
                                        updateListener.onDownloadSuccess(localPath);
                                        return;
                                    }
                                }
                                updateListener.onDownloadSuccess(getCachePath(localFileName));
                            }
                        }

                        @Override
                        public void onCanceled() {
                            mIsUpgrading = false;
                            if (updateListener != null) {
                                updateListener.onDownloadFailed(0);
                            }
                        }
                    });
        }

    }

    public boolean installLocalPackage(String localFilePath) {
        return installLocalPackageInner(null,localFilePath);
    }


    public boolean installLocalPackageN(String auth,String localFilePath) {
        return installLocalPackageInner(auth,localFilePath);
    }

    private boolean installLocalPackageInner(String auth,String localFileName) {
        // 首先将本地文件重命名，这样在下次检查的时候就会把这个文件删除，
        // 防止这次下载的是一个错误的包，安装失败后，下次继续会安装失败。

        String newFileName = getCachePath(String.format("%s_%d_local.apk",
                PACKAGE_NAME, mRemoteAppVersion)); //最终的安装包
        File f = new File(localFileName);
        File newFile = new File(newFileName);
        if (newFile.exists()) {
            newFile.delete();
        }
        CommonUtils.copyFile(localFileName, newFileName);
        PackageInfo packageInfo = GlobalData.app().getApplicationContext().getPackageManager()
                .getPackageArchiveInfo(newFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
        if (null != packageInfo) {
            Logger.w("VersionCheckManager", "the apk file packageName is " + packageInfo.packageName);
            Logger.w("VersionCheckManager", "the apk file packageName is com.wali.live");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (null != auth) {
                Uri uri = FileProvider.getUriForFile(GlobalData.app().getApplicationContext(), auth, newFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(newFile), "application/vnd.android.package-archive");
            }
            GlobalData.app().getApplicationContext().startActivity(intent);
            return true;

        }
        Logger.w("VersionCheckManager", "the apk file packageName is not com.wali.live");
        return false;
    }

    private boolean checkLocalPackage(String oldLocalFile) {
        String cachePath = getCachePath(oldLocalFile);
        File file = new File(cachePath);
        if (!file.exists()) {
            return false;
        }

        long now = System.currentTimeMillis();
        long last = file.lastModified();
        Logger.w("VersionCheckManager", " now " + now);
        Logger.w("VersionCheckManager", " last " + last);
        if (now > (CHECKTIME + last)) {
            return false;
        }

        try {
            PackageManager pm = GlobalData.app().getApplicationContext().getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(getCachePath(oldLocalFile), PackageManager.GET_ACTIVITIES);
            if (packageInfo == null) {
                return false;
            }
            if (packageInfo.versionCode != mRemoteAppVersion) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private static String getCachePath(String name) {
        File tempPath;
        if (Environment.getExternalStorageDirectory().canWrite()) {
            tempPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/WaliLivesdk/cache/");
            if (!tempPath.exists()) {
                tempPath.mkdirs();
            }
        } else {
            tempPath = GlobalData.app().getCacheDir();
        }
        Logger.w("VersionCheckManager", "getCachePath=" + tempPath.getAbsolutePath());
        String path = new File(tempPath.getAbsolutePath(), name).getAbsolutePath();
        return path;
    }

    public void setShowUpgradeDialog(boolean shown) {
        SharedPreferences pref = GlobalData.app().getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        Editor ed = pref.edit();
        ed.putBoolean(PREF_SHOW_UPGRADE_DIALOG, shown);
        ed.apply();
    }

    public boolean getShowUpgradeDialog() {
        SharedPreferences pref = GlobalData.app().getApplicationContext()
                .getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_SHOW_UPGRADE_DIALOG, false);
    }

    // 记录检查更新成功的时间
    public void setCheckTime() {
        SharedPreferences pref = GlobalData.app().getApplicationContext()
                .getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        Editor ed = pref.edit();
        ed.putLong(PREF_LAST_CHECK, System.currentTimeMillis());
        ed.apply();
    }

    // 看与上次成功检查时间间隔是否大于半小时
    public boolean canAutoCheck() {
        SharedPreferences pref = GlobalData.app().getApplicationContext()
                .getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        long last = pref.getLong(PREF_LAST_CHECK, 0);
        Logger.d(TAG, "canAutoCheck last == " + last);
        return System.currentTimeMillis() - last >= 1800 * 1000;
    }


    public String getVersionNumberByTransfer() {
        return getVersionNumberByTransfer(mRemoteAppVersion);
    }

    private String getVersionNumberByTransfer(int versionNumber) {
        String version = "";
        if (versionNumber < 0) {
            return "1.0.0";
        }
        version += versionNumber / 100000 + ".";
        version += (versionNumber % 100000) / 1000 + ".";
        version += (versionNumber % 100000) % 1000;
        return version;
    }

    public void showDownloadNotification(String msg) {
        NotificationManager mNotificationManager = (NotificationManager) GlobalData.app().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder nb = new Notification.Builder(GlobalData.app());
        nb.setContentText(msg);
        nb.setContentTitle(GlobalData.app().getString(R.string.update_downloading));
        nb.setSmallIcon(R.drawable.milive_ic);
        nb.setAutoCancel(true);
        int defaults = 0;
        nb.setDefaults(defaults);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= 16) {
            notification = nb.build();
        } else {
            notification = nb.getNotification();
        }
        mNotificationManager.notify(100001, notification);
    }

    public void removeNotification() {
        NotificationManager mNotificationManager = (NotificationManager) GlobalData.app().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(100001);
    }


    @Keep
    public interface IUpdateListener {


        /**
         * 通知上层应用更新下载开始
         */
        void onRepeatedRequest();

        /**
         * 通知上层应用更新下载开始
         */
        void onDownloadStart();

        /**
         * 通知上层应用更新下载进度更新
         */
        void onDownloadProgress(@IntRange(from = 0, to = 100) int progress);

        /**
         * 通知上层应用更新下载成功
         */
        void onDownloadSuccess(String path);

        /**
         * 通知上层应用更新下载失败
         */
        void onDownloadFailed(int errCode);
    }
}
