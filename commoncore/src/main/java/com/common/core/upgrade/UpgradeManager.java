package com.common.core.upgrade;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;

import com.alibaba.fastjson.JSON;
import com.common.core.R;
import com.common.log.MyLog;
import com.common.provideer.MyFileProvider;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.io.File;


public class UpgradeManager {
    public final static String TAG = "UpgradeManager";

    public static final int MSG_UPDATE_PROGRESS = 1;
    public static final int MSG_INSTALL = 2;

    DownloadManager mDownloadManager;
    DialogPlus mForceUpgradeDialog;
    UpgradeInfoModel mUpdateInfoModel;
    long mDownloadId;
    ForceUpgradeView mForceUpgradeView;
    FinishReceiver mFinishReceiver;
    DownloadChangeObserver mDownloadChangeObserver;

    private static class UpgradeManagerHolder {
        private static final UpgradeManager INSTANCE = new UpgradeManager();
    }

    public static final UpgradeManager getInstance() {
        return UpgradeManagerHolder.INSTANCE;
    }

    public UpgradeManager() {
        mDownloadManager = (DownloadManager) U.app().getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS:
                    if (mForceUpgradeView != null) {
                        DS ds = (DS) msg.obj;
                        mForceUpgradeView.updateProgress(ds.getProgress());
                        if (ds.status == DS.STATUS_SUCCESS) {
                            // 如果已经下载完成，走安装逻辑
                            install();
                        }
                    }
                    break;
                case MSG_INSTALL:
                    install();
                    break;
            }
        }
    };

    public void checkUpdate() {
        if (true) {
            return;
        }
        /**
         * 一旦拿到更新数据了，这个生命周期内就不访问了
         */
        if (mUpdateInfoModel == null) {
            if (true) {
                //TEST
                mUpdateInfoModel = new UpgradeInfoModel();
                mUpdateInfoModel.setDownloadUrl("https://s1.zb.mi.com/miliao/apk/miliao/7.4/11.apk");
                mUpdateInfoModel.setForceUpdate(true);
                mUpdateInfoModel.setVersionCode(50021);

                if (mUpdateInfoModel.isForceUpdate()) {
                    // 如果是强制更新,不管那么多，直接弹窗
                    showForceUpgradeDialog();
                }
                // 需要更新,弹窗
                return;
            }
            UpgradeCheckApi checkApi = ApiManager.getInstance().createService(UpgradeCheckApi.class);
            ApiMethods.subscribe(checkApi.getUpdateInfo(), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult apiResult) {
                    if (apiResult.getErrno() == 0) {
                        boolean needUpdate = apiResult.getData().getBoolean("needUpdate");
                        String updateInfo = apiResult.getData().getString("updateInfo");
                        UpgradeInfoModel updateInfoModel = JSON.parseObject(updateInfo, UpgradeInfoModel.class);
                        mUpdateInfoModel = updateInfoModel;
                        if (needUpdate && updateInfoModel.getVersionCode() > U.getAppInfoUtils().getVersionCode()) {
                            if (updateInfoModel.isForceUpdate()) {
                                // 如果是强制更新,不管那么多，直接弹窗
                                showForceUpgradeDialog();
                            }
                            // 需要更新,弹窗
                        }
                    }
                }
            });
        }
    }

    public void showForceUpgradeDialog() {
        if (mUpdateInfoModel == null) {
            return;
        }
        if (mForceUpgradeDialog == null) {
            Activity activity = U.getActivityUtils().getTopActivity();
            if (activity != null) {
                mForceUpgradeView = new ForceUpgradeView(activity);
                mForceUpgradeView.setListener(new ForceUpgradeView.Listener() {
                    @Override
                    public void onUpdateBtnClick() {
                        forceDownloadBegin();
                    }

                    @Override
                    public void onInstallBtnClick() {
                        install();
                    }
                });
                mForceUpgradeDialog = DialogPlus.newDialog(activity)
                        .setContentHolder(new ViewHolder(mForceUpgradeView))
                        .setGravity(Gravity.CENTER)
                        .setCancelable(false)
                        .setContentBackgroundResource(R.color.transparent)
                        .setOverlayBackgroundResource(R.color.black_trans_80)
                        .setExpanded(false)
                        .create();
            }
        }
        mForceUpgradeDialog.show();
    }

    private void forceDownloadBegin() {
        int localVersionCode = tryGetSaveFileApkVersion();
        if (localVersionCode == mUpdateInfoModel.getVersionCode()) {
            // 本地包有效，直接安装吧
            mUiHandler.sendEmptyMessage(MSG_INSTALL);
        } else {
            downloadApk();
        }
    }

    private int tryGetSaveFileApkVersion() {
        File saveFile = getSaveFile();
        if (saveFile.exists()) {
            try {
                PackageManager pm = U.app().getApplicationContext().getPackageManager();
                PackageInfo packageInfo = pm.getPackageArchiveInfo(saveFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
                if (U.getAppInfoUtils().getPackageName().equals(packageInfo)) {
                    return packageInfo.versionCode;
                }
            } catch (Exception e) {
            }
        }
        return 0;
    }

    // 下载apk
    private void downloadApk() {
        if (mUpdateInfoModel == null) {
            return;
        }
        if (mUpdateInfoModel.isDownloading()) {
            return;
        }
        File saveFile = getSaveFile();
        if (saveFile.exists()) {
            saveFile.delete();
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUpdateInfoModel.getDownloadUrl()));
//        request.setTitle(mUpdateInfoModel.getUpdateTitle());

        request.setMimeType("application/vnd.android.package-archive");
        // 设置标题
        request.setTitle(U.getAppInfoUtils().getAppName());
        // 通知栏可见
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 允许下载的网络状况
        int networkFlag;
        if (mUpdateInfoModel.isForceUpdate()) {
            networkFlag = DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI;
        } else {
            networkFlag = DownloadManager.Request.NETWORK_WIFI;
        }
        request.setAllowedNetworkTypes(networkFlag);
        // 设置文件存放路径

        request.setDestinationUri(Uri.fromFile(getSaveFile()));
        // 设置漫游状态下是否可以下载
        request.setAllowedOverRoaming(false);
        // 如果我们希望下载的文件可以被系统的Downloads应用扫描到并管理，
        // 我们需要调用Request对象的setVisibleInDownloadsUi方法，传递参数true.
        request.setVisibleInDownloadsUi(true);
        mDownloadId = mDownloadManager.enqueue(request);
        //执行下载任务时注册广播监听下载成功状态
        registerObserver();
        registerReceiver();
    }

    private void registerObserver() {
        if (mDownloadChangeObserver == null) {
            mDownloadChangeObserver = new DownloadChangeObserver();
        }
        U.app().getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, mDownloadChangeObserver);
    }

    private void registerReceiver() {
        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        U.app().registerReceiver(mFinishReceiver, intentFilter);
    }

    private void unregister() {
        if (mDownloadId > 0) {
            mDownloadManager.remove(mDownloadId);
        }
        if (mDownloadChangeObserver != null) {
            U.app().getContentResolver().unregisterContentObserver(mDownloadChangeObserver);
            mDownloadChangeObserver = null;
        }
        if (mFinishReceiver != null) {
            U.app().unregisterReceiver(mFinishReceiver);
            mFinishReceiver = null;
        }
    }

    private void cancelDownload() {

    }

    /**
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
     *
     * @param downloadId
     * @return
     */
    private DS getBytesAndStatus(long downloadId) {
        DS ds = new DS();
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = null;
        try {
            cursor = mDownloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                //已经下载文件大小
                ds.downloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                //下载文件的总大小
                ds.total = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                //下载状态
                ds.status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ds;
    }

    private void updateProgress() {
        DS ds = getBytesAndStatus(mDownloadId);
        if (ds.status == DS.STATUS_DOWNLOADING) {
            mUpdateInfoModel.setDownloading(true);
        } else {
            mUpdateInfoModel.setDownloading(false);
        }
        MyLog.d(TAG, "updateProgress " + ds);
        Message msg = mUiHandler.obtainMessage(MSG_UPDATE_PROGRESS);
        msg.obj = ds;
        mUiHandler.sendMessage(msg);
    }

    private File getSaveFile() {
        File file = new File(U.getAppInfoUtils().getMainDir(), "skrer.apk");
        return file;
    }

    /**
     * 安装逻辑
     */
    public void install() {
        MyLog.d(TAG, "install");
        File file = getSaveFile();
        if (file == null || !file.exists()) {
            MyLog.d(TAG, "文件不存在，cancel");
            return;
        }
        if (mUpdateInfoModel.isInstalling()) {
            MyLog.d(TAG, "已经在安装，cancel");
            return;
        }
//        mUpdateInfoModel.setInstalling(true);
        if (mForceUpgradeDialog != null) {
            mForceUpgradeDialog.dismiss();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 由于没有在Activity环境下启动Activity,设置下面的标签
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = MyFileProvider.getUriForFile(U.app(), U.getAppInfoUtils().getPackageName()+".provider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            U.app().startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri,
                    "application/vnd.android.package-archive");
            U.app().startActivity(intent);
        }
    }

    private void onInstallOk() {
        MyLog.d(TAG, "onInstallOk");
        unregister();
        mUpdateInfoModel.setDownloading(false);
        mUpdateInfoModel.setInstalling(false);
        U.getToastUtil().showShort("安装成功");
    }

    static class DS {
        public static final int STATUS_DOWNLOADING = 2;
        public static final int STATUS_SUCCESS = 8;
        int downloaded;
        int total = -1;
        int status;

        public int getProgress() {
            return (int) (downloaded / (total * 0.01));
        }

        @Override
        public String toString() {
            return "DS{" +
                    "downloaded=" + downloaded +
                    ", total=" + total +
                    ", status=" + status +
                    '}';
        }
    }

    /**
     * 监听下载进度
     */
    private class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(mUiHandler);
        }


        /**
         * 当所监听的Uri发生改变时，就会回调此方法
         *
         * @param selfChange 此值意义不大, 一般情况下该回调值false
         */
        @Override
        public void onChange(boolean selfChange) {
            updateProgress();
        }
    }

    /**
     * 监听下载完成
     * 安装完成
     */
    private class FinishReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.EXTRA_DOWNLOAD_ID)) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == mDownloadId) {
                    install();
                }
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (U.getAppInfoUtils().getPackageName().equals(packageName)) {
                    onInstallOk();
                }
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (U.getAppInfoUtils().getPackageName().equals(packageName)) {
                    onInstallOk();
                }
            }
        }
    }

}
