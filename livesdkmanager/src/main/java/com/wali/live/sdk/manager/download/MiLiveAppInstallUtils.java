package com.wali.live.sdk.manager.download;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.wali.live.sdk.manager.R;
import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.http.HttpUtils;
import com.wali.live.sdk.manager.notification.NotificationManger;
import com.wali.live.sdk.manager.toast.ToastUtils;

import java.io.File;
import java.util.List;

/**
 * Created by milive on 16/12/6.
 */
@Deprecated
public class MiLiveAppInstallUtils {
    public static final String MILIVESDK_PACKAGE_NAME = "com.wali.live.demo";

    public static final int MILIVE_AVAILABLE = 1;
    public static final int MILIVE_NOT_INSTALL = -2;
    public static final int MILIVE_VERSION_TOO_LOW = -3;

    // 网络上的最新版本
    public static final int MILIVE_AVAILABLE_VERSION = 201020;
    public static final String APP_DOWNLOAD_URL = "https://s1.zb.mi.com/miliao/apk/milive/livesdk/test/watchsdklite-release.apk";
//    public static final String APP_DOWNLOAD_URL = "https://simonjava.github.io/img/milivesdk.apk";

    private static boolean sDownloadingApk = false;

    /**
     * 通过schema跳转到小米直播app中，如果没有安装小米直播则提示下载安装小米直播，否则直接跳转
     */
    public static void checkUpdateIfNeed(Context activity, final Runnable avaliableRunable) {
        int result = isMiLiveAvailable(activity);
        if (MILIVE_AVAILABLE == result) {
            if(avaliableRunable!=null) {
                avaliableRunable.run();
            }
        } else if (MILIVE_VERSION_TOO_LOW == result) {
            AlertDialog.Builder myAlertDialogBuilder = new AlertDialog.Builder(activity);
            myAlertDialogBuilder.setMessage(GlobalData.app().getString(R.string.dialog_milive_app_version_too_live_message));
            myAlertDialogBuilder.setPositiveButton(GlobalData.app().getString(R.string.sure_install_milive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startDownload();
                }
            });
            myAlertDialogBuilder.setNegativeButton(GlobalData.app().getString(R.string.cancel_install_milve), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(avaliableRunable!=null) {
                        avaliableRunable.run();
                    }
                }
            });
            myAlertDialogBuilder.create().show();
        } else if (MILIVE_NOT_INSTALL == result) {
            AlertDialog.Builder myAlertDialogBuilder = new AlertDialog.Builder(activity);
            myAlertDialogBuilder.setMessage(GlobalData.app().getString(R.string.dialog_milive_app_noinstall_message));
            myAlertDialogBuilder.setPositiveButton(GlobalData.app().getString(R.string.sure_install_milive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startDownload();
                }
            });
            myAlertDialogBuilder.setNegativeButton(GlobalData.app().getString(R.string.cancel_install_milve), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            myAlertDialogBuilder.create().show();
        }
    }

    /**
     * 判断当前安装的直播是否可用
     *
     * @param context
     * @return
     */
    public static int isMiLiveAvailable(final Context context) {
        PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                PackageInfo pi = pinfo.get(i);
                String installPackageName = pi.packageName;
                if (MILIVESDK_PACKAGE_NAME.equals(installPackageName)) {
                    int versionCode = pi.versionCode;
                    return versionCode >= MILIVE_AVAILABLE_VERSION ? MILIVE_AVAILABLE : MILIVE_VERSION_TOO_LOW;
                }
            }
        }
        return MILIVE_NOT_INSTALL;
    }


    private static String getCachePath(String name) {

        return new File(Environment.getExternalStorageDirectory(), name).getAbsolutePath();
    }

    private static void startDownload() {
        if (sDownloadingApk) {
            return;
        }
        sDownloadingApk = true;

        final String localFileName = String.format("%s_%d.apk", MILIVESDK_PACKAGE_NAME,
                MILIVE_AVAILABLE_VERSION);
        AsyncTask<Void, Void, Boolean> downloadTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {

                File destFile = new File(getCachePath(localFileName));
                return HttpUtils.downloadFile(APP_DOWNLOAD_URL, destFile,
                        new HttpUtils.OnDownloadProgress() {
                            long lastNotifyTime = 0;
                            final int NOTIFY_GAP = 500;// 刷通知栏时间间隔

                            @Override
                            public void onFailed() {
                                NotificationManger.getInstance().removeNotification(NotificationManger.UPDATE_DOWNLOADING);
                                ToastUtils.showToast(R.string.milive_upgrade_progress_faild);
                                sDownloadingApk = false;
                            }

                            @Override
                            public void onDownloaded(long downloaded,
                                                     long totalLength) {
                                if (totalLength != 0) {
                                    int percentage = (int) (downloaded * 100 / totalLength);
                                    long now = System.currentTimeMillis();
                                    if (now - lastNotifyTime >= NOTIFY_GAP) {
                                        String percStr = GlobalData.app().getString(
                                                R.string.milive_upgrade_progress,
                                                percentage);
                                        NotificationManger.getInstance().showDownloadNotification(percStr);
                                        lastNotifyTime = now;
                                    }
                                    if (now == 1) {
                                        ToastUtils.showToast(R.string.milive_upgrade_progress_start);
                                    }
                                }
                            }

                            @Override
                            public void onCompleted(String localPath) {
                                NotificationManger.getInstance().removeNotification(NotificationManger.UPDATE_DOWNLOADING);
                                ToastUtils.showToast(R.string.milive_upgrade_progress_sucess);
                                installLocalPackage(MILIVESDK_PACKAGE_NAME, MILIVE_AVAILABLE_VERSION);
                                sDownloadingApk = false;
                            }

                            @Override
                            public void onCanceled() {
                                NotificationManger.getInstance().removeNotification(NotificationManger.UPDATE_DOWNLOADING);
                                ToastUtils.showToast(R.string.milive_upgrade_progress_faild);
                                sDownloadingApk = false;
                            }
                        });
            }

        };
        downloadTask.executeOnExecutor(HttpUtils.ONLINE_FILE_TASK_EXECUTOR);
    }

    public static void installLocalPackage(String appPackage, int appVersion) {
        // 首先将本地文件重命名，这样在下次检查的时候就会把这个文件删除，
        // 防止这次下载的是一个错误的包，安装失败后，下次继续会安装失败。
        String localFileName = getCachePath(String.format("%s_%d.apk",
                appPackage, appVersion));
        String newFileName = getCachePath(String.format("%s_%d_local.apk",
                appPackage, appVersion));
        File f = new File(localFileName);
        File newFile = new File(newFileName);
        if (newFile.exists()) {
            newFile.delete();
        }
        if(f.exists()) {
            f.renameTo(newFile);
        }
        PackageInfo packageInfo = GlobalData.app().getApplicationContext().getPackageManager()
                .getPackageArchiveInfo(newFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
        if (null != packageInfo) {
            Log.w("VersionCheckManag／er", "the apk file packageName is " + packageInfo.packageName);
            if (MILIVESDK_PACKAGE_NAME.equals(packageInfo.packageName)) {
                Log.w("VersionCheckManager", "the apk file packageName is "+MILIVESDK_PACKAGE_NAME);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(newFile),
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                GlobalData.app().startActivity(intent);
                return;
            }
        }
        Log.w("VersionCheckManager", "the apk file packageName is not "+MILIVESDK_PACKAGE_NAME);
    }
}
