package com.base.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.toast.ToastUtils;
import com.base.utils.version.VersionCheckManager;
import com.base.utils.version.VersionManager;

import java.io.File;
import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class SelfUpdateManager {

    public final static String TAG = "SelfUpdateManager";


    static final String NO_REMIND_TS_APK_INSTALL = "no_remind_ts_apk_install";
    static final String NO_REMIND_TS_APK_DOWNLOAD = "no_remind_ts_apk_download";

    static final long REMIND_MAX_INTERVAL = 3600 * 1000 * 24;

    static long sLastShowDialogRemindTs = 0;

    static boolean sCanSilentDownload = false;

    static boolean sDownloading = false;

    static Handler sMainHandler = new Handler(Looper.getMainLooper());

    public static void selfUpdateAsnc(final WeakReference contextRef) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                selfUpdate(contextRef);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private static void selfUpdate(WeakReference<Context> contextRef) {
        if (VersionCheckManager.getInstance().getRemoteVersion() <= 0) {
            VersionCheckManager.getInstance().checkNewVersion();
        }
        if (VersionManager.getCurrentVersionCode(GlobalData.app()) >= VersionCheckManager.getInstance().getRemoteVersion()) {
            MyLog.d(TAG, "// 不需要更新，本地是最新的");
        } else {
            // 判断文件在不在
            final String localFileName = String.format("%s_%d.apk", GlobalData.app().getPackageName(), VersionCheckManager.getInstance().getRemoteVersion());
            final File apkFile = new File(getCachePath(localFileName));

            boolean hasNewestApkInLocal = false;
            if (apkFile != null && apkFile.exists()) {
                try {
                    PackageManager pm = GlobalData.app().getApplicationContext().getPackageManager();
                    PackageInfo packageInfo = pm.getPackageArchiveInfo(getCachePath(localFileName), PackageManager.GET_ACTIVITIES);
                    if (packageInfo != null && packageInfo.versionCode == VersionCheckManager.getInstance().getRemoteVersion()) {
                        hasNewestApkInLocal = true;
                    }
                } catch (Exception e) {
                }
            }

            if (hasNewestApkInLocal) {
                MyLog.d(TAG, "本地是有最新的apk");
                if (System.currentTimeMillis() - sLastShowDialogRemindTs > 2 * 3600 * 1000) {
                    // 包现成的
                    long ts = PreferenceUtils.getSettingLong(NO_REMIND_TS_APK_INSTALL, 0);
                    if (System.currentTimeMillis() - ts > REMIND_MAX_INTERVAL) {
                        sLastShowDialogRemindTs = System.currentTimeMillis();
                        // 可提醒用户
                        showDialog(contextRef.get(), "发现直播助手新版本，可直接安装", "免流量安装", new Runnable() {
                            @Override
                            public void run() {
                                //安装apk
                                VersionCheckManager.getInstance().installLocalPackageN("com.wali.live.watchsdk.editinfo.fileprovider", apkFile.getPath());
                            }
                        }, null);
                    } else {
                        // 算了
                    }
                } else {
                    // 算了 不弹了
                }
            } else {
                if (VersionCheckManager.getInstance().isForceUpdate()) {
                    MyLog.d(TAG, "强制更新");
                    if (System.currentTimeMillis() - sLastShowDialogRemindTs > 2 * 3600 * 1000) {
                        long ts = PreferenceUtils.getSettingLong(NO_REMIND_TS_APK_DOWNLOAD, 0);
                        if (System.currentTimeMillis() - ts > REMIND_MAX_INTERVAL) {
                            // 可提醒用户
                            sLastShowDialogRemindTs = System.currentTimeMillis();
                            showDialog(contextRef.get(), "需要下载安装直播助手新版本", "下载", new Runnable() {
                                @Override
                                public void run() {
                                    // 开始下载
                                    Observable.create(new Observable.OnSubscribe<Object>() {
                                        @Override
                                        public void call(Subscriber<? super Object> subscriber) {
                                            downloadApk(true);
                                            subscriber.onCompleted();
                                        }
                                    })
                                            .subscribeOn(Schedulers.io())
                                            .subscribe();
                                }
                            }, new Runnable() {
                                @Override
                                public void run() {
                                    sCanSilentDownload = true;
                                }
                            });
                        } else {
                            // 算了
                            sCanSilentDownload = true;
                        }
                    }
                } else {
                    sCanSilentDownload = true;
                }
            }
        }
    }

    public static void tryDownloadNewestApk() {
        if (sCanSilentDownload) {
            Observable.create(new Observable.OnSubscribe<Object>() {
                @Override
                public void call(Subscriber<? super Object> subscriber) {
                    downloadApk(true);
                    subscriber.onCompleted();
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    public static void downloadApk(final boolean noNotice) {
        if (sDownloading) {
            MyLog.d(TAG, "下载中");
            return;
        }
        sDownloading = true;
        VersionCheckManager.getInstance().startDownload(new VersionCheckManager.IUpdateListener() {
            @Override
            public void onRepeatedRequest() {
                MyLog.d(TAG, "onRepeatedRequest");
                if (!noNotice) {
                    ToastUtils.showToast("直播组件正在更新中,可在通知栏查看进度");
                }
            }

            @Override
            public void onDownloadStart() {
                MyLog.d(TAG, "onDownloadStart");
                if (!noNotice) {
                    ToastUtils.showToast("更新直播助手");
                }
            }

            @Override
            public void onDownloadProgress(int progress) {
                MyLog.d(TAG, "onDownloadProgress" + " progress=" + progress);
                if (!noNotice) {
                    String tips = String.format("已下载%d%%", progress);
                    VersionCheckManager.getInstance().showDownloadNotification(tips);
                }

            }

            @Override
            public void onDownloadSuccess(String path) {
                MyLog.d(TAG, "onDownloadSuccess" + " path=" + path);
                VersionCheckManager.getInstance().removeNotification();

                sDownloading = false;
                sCanSilentDownload = false;
                // 删除旧的apk
                File file = new File(path);
                File parentFile = file.getParentFile();
                for (File t : parentFile.listFiles()) {
                    if (t.getPath().equals(file.getPath())) {
                        continue;
                    }
                    String fileName = t.getName();
                    if (fileName.startsWith(GlobalData.app().getPackageName())
                            && fileName.endsWith(".apk")) {
                        t.delete();
                    }
                }
            }

            @Override
            public void onDownloadFailed(int errCode) {
                MyLog.d(TAG, "onDownloadFailed" + " errCode=" + errCode);
                sDownloading = false;
            }
        });
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
        String path = new File(tempPath.getAbsolutePath(), name).getAbsolutePath();
        return path;
    }

    public static void showDialog(final Context activity, final String message, final String btnTitle, final Runnable okRunnable, final Runnable cancelRunnable) {
        if (activity == null) {
            if (cancelRunnable != null) {
                cancelRunnable.run();
            }
        }
        sMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (activity == null) {
                    return;
                }
                MyAlertDialog alertDialog = new MyAlertDialog.Builder(activity).create();
                alertDialog.setMessage(message);
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {

                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, btnTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (okRunnable != null) {
                            okRunnable.run();
                        }
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cancelRunnable != null) {
                            cancelRunnable.run();
                        }
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

    }

}
