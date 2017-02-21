package com.wali.live.sdk.manager.version;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wali.live.sdk.manager.R;
import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.http.HttpUtils;
import com.wali.live.sdk.manager.http.SimpleRequest;
import com.wali.live.sdk.manager.http.bean.BasicNameValuePair;
import com.wali.live.sdk.manager.http.bean.NameValuePair;
import com.wali.live.sdk.manager.http.exception.AccessDeniedException;
import com.wali.live.sdk.manager.http.exception.AuthenticationFailureException;
import com.wali.live.sdk.manager.http.utils.IOUtils;
import com.wali.live.sdk.manager.http.utils.StringUtils;
import com.wali.live.sdk.manager.notification.NotificationManger;
import com.wali.live.sdk.manager.toast.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class VersionCheckManager {
    public final static String TAG = VersionCheckManager.class.getSimpleName();
    public static final int HAS_UPGRADE = 1;
    public static final int NO_UPGRADE = 2;
    public static final int CHECK_FAILED = 3;
    public static final int IS_UPGRADING = 4;
    public static final int HAS_FORCE_UPGRADE = 0;

    private static final String CHECK_GRAY_UPGRADE_INFO = "http://api.chat.xiaomi.net/v2/user/%s/grayupgarde";

    public static final String PACKAGE_NAME = "com.mi.liveassistant";
    public static final String JUMP_CLASS_NAME = "com.wali.live.jump.JumpSdkActivity";

    private static final String APP_NAME = "liveassistant";
    private static final String APP_PLATFORM = "android";

    private static final String PREF_FILE_NAME = "liveassistant_upgrade";
    private static final String PREF_SHOW_UPGRADE_DIALOG = "show_upgrade_dialog";
    private static final String PREF_LAST_CHECK = "last_check";
    private static final String PREF_FORCE_TO = "force_to";
    private static final String REMOTE_VERSION = "remote_version";

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

    public static synchronized VersionCheckManager getInstance() {
        if (sInstance == null) {
            sInstance = new VersionCheckManager();
        }
        return sInstance;
    }

    public int checkNewVersion() {
        if (mIsUpgrading) {
            return IS_UPGRADING;
        }
        String miId = "0";
        if (miId == null || miId == "") {
            miId = "0";
        }
        Log.d(TAG, "VersionCheckManager miId == " + miId);
        String url = String.format(CHECK_GRAY_UPGRADE_INFO, miId);
        List<NameValuePair> postBody = new ArrayList<NameValuePair>();
        NameValuePair p = new BasicNameValuePair("uuid", miId);
        postBody.add(p);
        p = new BasicNameValuePair("app", APP_NAME);
        postBody.add(p);
        p = new BasicNameValuePair("platform", APP_PLATFORM);
        postBody.add(p);
        p = new BasicNameValuePair("system",
                String.valueOf(Build.VERSION.SDK_INT));
        postBody.add(p);
        // 渠道号
        p = new BasicNameValuePair("channel", "DEFAULT");
        //p = new BasicNameValuePair("channel", "debug");
        postBody.add(p);
        p = new BasicNameValuePair("device", Build.MODEL);
        postBody.add(p);
        int version = getCurrentVersion(GlobalData.app().getApplicationContext());
        if (version == 0) {
            version = 1;
        }
        p = new BasicNameValuePair("currentVersion",
                String.valueOf(version));
        postBody.add(p);
        p = new BasicNameValuePair("language", "zh_CN");
        postBody.add(p);
        p = new BasicNameValuePair("updateId", "0");
        postBody.add(p);
        p = new BasicNameValuePair("unique", miId);
        postBody.add(p);
        // 发送当前包的hash值
        String packagePath = getPackagePath(GlobalData.app().getApplicationContext());
        String packageHash = getPackageHash(GlobalData.app().getApplicationContext(), packagePath);
        if (!TextUtils.isEmpty(packageHash)) {
            p = new BasicNameValuePair("hash", packageHash);
            postBody.add(p);
        } else {
            // 缺个hash会没法下载，随便写个
            p = new BasicNameValuePair("hash", "5fcc13f203341157dae7469f10b3121a9cb67721");
            postBody.add(p);
        }
        SimpleRequest.StringContent result = null;
        try {
            Log.w(TAG, "VersionCheck Get Request Params : " + postBody);
            result = HttpUtils.doV2Get(url, postBody);
            Log.w(TAG, "VersionCheck return : " + result);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (AccessDeniedException e) {
            Log.e(TAG, e.getMessage());
        } catch (AuthenticationFailureException e) {
            Log.e(TAG, e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        setCheckTime();
        return parseResult(result);
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
            Log.w(TAG, "updateResult" + resultObj.toString());
            JSONObject dataObj = resultObj.getJSONObject("data");
            boolean shouldUpdate = dataObj.getBoolean("newUpdate");
            if (!shouldUpdate) {
                return NO_UPGRADE;
            }
            this.mRemoteAppVersion = dataObj.getInt("toVersion");
            this.mRemoteApkUrl = dataObj.getString("downloadUrl");
            this.mUpdateMessage = dataObj.optString("remark");
            this.mAdditionalUrl = dataObj.optString("additionalUrl");
            this.mAdditionalSize = dataObj.optInt("fullSize", -1);
            this.mFullPackageHash = dataObj.optString("fullHash");
            // additionalUrl不为空，走增量升级
            this.mIsAdditionalUpgrade = !TextUtils.isEmpty(this.mAdditionalUrl)
                    && !TextUtils.isEmpty(this.mFullPackageHash);
            JSONObject custom = dataObj.optJSONObject("custom");
            boolean shouldForceUpdate = false;
            if (custom != null) {
                shouldForceUpdate = custom.optBoolean("forced", false);
            }
            if (shouldForceUpdate) {
                mNeedUpdateApp = true;
                setForceToVersion(true);
                return HAS_FORCE_UPGRADE;
            } else {
                mNeedUpdateApp = true;
                setForceToVersion(false);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return CHECK_FAILED;
        }
        return HAS_UPGRADE;
    }

    public static int getCurrentVersion(Context context) {
        int thisVersion = 0;
        PackageInfo pInfo = getPackageInfo(context);
        if (pInfo != null)
            thisVersion = pInfo.versionCode;
        return thisVersion;
    }

    public static String getCurrentVersionName(Context context) {
        String thisVersion = null;
        PackageInfo pInfo = getPackageInfo(context);
        if (pInfo != null)
            thisVersion = pInfo.versionName;
        return thisVersion;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(
                    PACKAGE_NAME, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        return pInfo;
    }

    private static String getPackagePath(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(
                    PACKAGE_NAME, 0).sourceDir;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "error in get package path...", e);
        }
        return null;
    }

    private static String getPackageHash(Context context, String packagePath) {
        try {
            byte sha1Byte[];
            sha1Byte = IOUtils.getFileSha1Digest(packagePath);
            if (sha1Byte != null) {
                return StringUtils.getHexString(sha1Byte);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "error in calc package sha1...", e);
        } catch (IOException e) {
            Log.e(TAG, "error in calc package sha1...", e);
        }
        return null;
    }

    public void saveRemoteVersion() {
        SharedPreferences pref = GlobalData.app().getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        Editor ed = pref.edit();
        ed.putInt(REMOTE_VERSION, mRemoteAppVersion);
        ed.apply();
    }

    public int getRemoteVersion() {
        SharedPreferences pref = GlobalData.app().getApplicationContext()
                .getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(REMOTE_VERSION, -1);
    }

    public void showUpgradeDialog(final WeakReference<Activity> activity, final boolean isManualCheck, final boolean canCancled) {
        if (activity.get() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
        View updateView = LayoutInflater.from(GlobalData.app()).inflate(R.layout.upgrage_dialog_layout, null);
        final TextView version = (TextView) updateView.findViewById(R.id.version);
        version.setText(StringUtils.getString(R.string.app_version, getVersionNumberByTransfer(mRemoteAppVersion)));
        final TextView size = (TextView) updateView.findViewById(R.id.size);
        size.setText(StringUtils.getString(R.string.apksize, String.valueOf(this.mAdditionalSize)));
        final TextView update_content = (TextView) updateView.findViewById(R.id.update_content);
        update_content.setText(StringUtils.getString(R.string.upgrade_description, mUpdateMessage));
        builder.setView(updateView);
        if (canCancled) {
            builder.setPositiveButton(R.string.update_rightnow, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startDownload();
                }
            });
            builder.setNegativeButton(R.string.cancel_update, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (!isManualCheck) {
                        setCheckTime();
                    }
                }
            });
        } else {
            builder.setPositiveButton(R.string.update_rightnow, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startDownload();
                    //setCheckTime(); //不纪录升级时间
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void startDownload() {
        ToastUtils.showToast(GlobalData.app().getApplicationContext(),
                R.string.downloading_background);
        if (mIsUpgrading) {
            return;
        }
        final String localFileName = String.format("%s_%d.apk", PACKAGE_NAME,
                mRemoteAppVersion);
        AsyncTask<Void, Void, Boolean> downloadTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                mIsUpgrading = true;
                File destFile = new File(getCachePath(localFileName));
                return HttpUtils.downloadFile(mRemoteApkUrl, destFile,
                        new HttpUtils.OnDownloadProgress() {
                            long lastNotifyTime = 0;
                            final int NOTIFY_GAP = 500;// 刷通知栏时间间隔

                            @Override
                            public void onFailed() {
                                mIsUpgrading = false;
                                dismissNotification();
                                ToastUtils.showToast(GlobalData.app().getApplicationContext(),
                                        R.string.download_update_failed);
                            }

                            @Override
                            public void onDownloaded(long downloaded,
                                                     long totalLength) {
                                if (totalLength != 0) {
                                    int percentage = (int) (downloaded * 100 / totalLength);
                                    long now = System.currentTimeMillis();
                                    if (now - lastNotifyTime >= NOTIFY_GAP) {
                                        String percStr = GlobalData.app().getApplicationContext()
                                                .getString(
                                                        R.string.milive_upgrade_progress,
                                                        percentage);
                                        showNotification(percStr);
                                        lastNotifyTime = now;
                                    }
                                }
                            }

                            @Override
                            public void onCompleted(String localPath) {
                                mIsUpgrading = false;
                                dismissNotification();
                                ToastUtils.showToast(GlobalData.app().getApplicationContext(),
                                        R.string.download_update_succeed);
                                installLocalPackage();
                            }

                            @Override
                            public void onCanceled() {
                                mIsUpgrading = false;
                                dismissNotification();
                            }
                        });
            }

        };
        downloadTask.executeOnExecutor(HttpUtils.ONLINE_FILE_TASK_EXECUTOR);
    }

    public void installLocalPackage() {
        // 首先将本地文件重命名，这样在下次检查的时候就会把这个文件删除，
        // 防止这次下载的是一个错误的包，安装失败后，下次继续会安装失败。
        String localFileName = getCachePath(String.format("%s_%d.apk",
                PACKAGE_NAME, mRemoteAppVersion));
        String newFileName = getCachePath(String.format("%s_%d_local.apk",
                PACKAGE_NAME, mRemoteAppVersion));
        File f = new File(localFileName);
        File newFile = new File(newFileName);
        if (newFile.exists()) {
            newFile.delete();
        }
        f.renameTo(newFile);
        PackageInfo packageInfo = GlobalData.app().getApplicationContext().getPackageManager()
                .getPackageArchiveInfo(newFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
        if (null != packageInfo) {
            Log.w("VersionCheckManager", "the apk file packageName is " + packageInfo.packageName);
            Log.w("VersionCheckManager", "the apk file packageName is com.wali.live");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(newFile),
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            GlobalData.app().getApplicationContext().startActivity(intent);
            return;
        }
        Log.w("VersionCheckManager", "the apk file packageName is not com.wali.live");
        ToastUtils.showToast(R.string.update_file_illegal);
    }

    private static String getCachePath(String name) {
        File tempPath = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/WaliLivesdk/cache/");
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
        return new File(tempPath.getAbsolutePath(), name).getAbsolutePath();
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
        SharedPreferences pref = GlobalData.app().getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        Editor ed = pref.edit();
        ed.putLong(PREF_LAST_CHECK, System.currentTimeMillis());
        ed.apply();
    }

    // 看与上次成功检查时间间隔是否大于半小时
    public boolean canAutoCheck() {
        SharedPreferences pref = GlobalData.app().getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        long last = pref.getLong(PREF_LAST_CHECK, 0);
        Log.d(TAG, "canAutoCheck last == " + last);
        return System.currentTimeMillis() - last >= 1800 * 1000;
    }

    private void showNotification(String msg) {
        NotificationManger.getInstance().showDownloadNotification(msg);
    }

    private void dismissNotification() {
        NotificationManger.getInstance().removeNotification(NotificationManger.UPDATE_DOWNLOADING);
    }

    public void setForceToVersion(boolean needForceUpdate) {
        SharedPreferences pref = GlobalData.app().getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        Editor ed = pref.edit();
        ed.putBoolean(PREF_FORCE_TO, needForceUpdate);
        ed.apply();
    }

    // 看当前版本是否低于要求的最低版本
    public boolean needForceCheck() {
        SharedPreferences pref = GlobalData.app().getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean needForceUpdate = pref.getBoolean(PREF_FORCE_TO, false);
        Log.d(TAG, "need_force_to_version == " + needForceUpdate);
        return needForceUpdate;
    }

    private String getVersionNumberByTransfer(int versionNumber) {
        String mVersion = "";
        if (versionNumber < 0) {
            return "1.0.0";
        }
//        String mSourceVersion = String.valueOf(versionNumber);
//        if (null == mSourceVersion || mSourceVersion.length() < 5) {
//            return "version has error";
//        }
//        mVersion += mSourceVersion.substring(0, 1) + ".";
//        mVersion += mSourceVersion.substring(1, mSourceVersion.length() - 4) + ".";
//        mVersion += mSourceVersion.substring(mSourceVersion.length() - 4, mSourceVersion.length() - 1);
        mVersion += versionNumber / 100000 + ".";
        mVersion += (versionNumber % 100000) / 1000 + ".";
        mVersion += (versionNumber % 100000) % 1000;
        return mVersion;
    }

    public static class NewVersion {

    }
}
