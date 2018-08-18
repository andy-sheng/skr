package com.wali.live.watchsdk.watch.download;

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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Pair;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.MD5;
import com.base.utils.WLReflect;
import com.base.utils.toast.ToastUtils;
import com.wali.live.utils.FileUtils;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

public class CustomDownloadManager {

    public static String TAG = "CustomDownloadManager";

    private static final String PF_KEY_DOWNLOAD_ID_MAP = "pf_key_download_id_map";

    private static final String JSON_KEY_DOWNLOAD_KEY = "download_key";
    private static final String JSON_KEY_DOWNLOAD_VALUE = "download_value";

    /**
     * 保存这 url 与 downloadid 的关系
     */
    private HashMap<String, Long> mDownloadingMap = new HashMap<>();

    /**
     * 监听那些downloadid
     */
    private HashSet<Holder> mMonitorDownloadIds = new HashSet<>();

    // 注册下载监听回调
    private BroadcastReceiver mDownloadReceiver;

    // 安装监听
    private BroadcastReceiver mInstallReceiver;

    private boolean mHasInstalled;

    private DownloadManager mDownloadManager;

    private Subscription mDownloadSubscription;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ContentObserver mDownloadObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            MyLog.d(TAG, "onChange" + " selfChange=" + selfChange);
            updateProgress();
        }
    };

    private static class CustomDownloadManagerHolder {
        private static final CustomDownloadManager INSTANCE = new CustomDownloadManager();
    }

    private CustomDownloadManager() {
        mDownloadManager = (DownloadManager) GlobalData.app().getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
        registerObserver();
        registerReceiver();
        loadDownloadIdToMapFromPF();
    }

    public static final CustomDownloadManager getInstance() {
        return CustomDownloadManagerHolder.INSTANCE;
    }

    void loadDownloadIdToMapFromPF() {
        String content = PreferenceUtils.getSettingString(GlobalData.app(), PF_KEY_DOWNLOAD_ID_MAP, "");
        try {
            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                String key = jsonObject.optString(JSON_KEY_DOWNLOAD_KEY);
                long value = jsonObject.optLong(JSON_KEY_DOWNLOAD_VALUE);
                mDownloadingMap.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void saveDownloadIdToPFFromMap() {
        JSONArray jsonArray = new JSONArray();
        for (String key : mDownloadingMap.keySet()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(JSON_KEY_DOWNLOAD_KEY, key);
                long value = mDownloadingMap.get(key);
                jsonObject.putOpt(JSON_KEY_DOWNLOAD_VALUE, value);
            } catch (JSONException e) {
            }
            jsonArray.put(jsonObject);
        }
        String content = jsonArray.toString();
        PreferenceUtils.setSettingString(GlobalData.app(), PF_KEY_DOWNLOAD_ID_MAP, content);
    }

    public void beginDownload(Item item) {
        MyLog.d(TAG, "beginDownload item:" + item);
        if (TextUtils.isEmpty(item.getUrl())) {
            return;
        }
        String downloadKey = MD5.MD5_32(item.getUrl());

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(item.getUrl()));
        request.setTitle(item.getTitle());
        // 通知栏可见
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 由于COLUMN_LOCAL_FILENAME废弃，生成固定的下载路径
//        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADSS);
//        mDownloadFilename = Uri.withAppendedPath(Uri.fromFile(file), filename).getPath();
        String ext = FileUtils.getFileExt(item.getUrl());
        String fileName = downloadKey;
        if (!TextUtils.isEmpty(ext)) {
            fileName += "." + ext;
        }
        MyLog.d(TAG, "beginDownload" + " fileName=" + fileName);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        long downloadId = mDownloadManager.enqueue(request);
        MyLog.w(TAG, "downloadId=" + downloadId);
        mDownloadingMap.put(downloadKey, downloadId);
        addMonitorUrl(item.getUrl());
        saveDownloadIdToPFFromMap();
        ToastUtils.showToast(R.string.downloading);
    }

    public void pauseDownload(String url) {
        String key = MD5.MD5_32(url);
        long did = mDownloadingMap.get(key);
        WLReflect.pauseDownload(mDownloadManager, new long[]{did});
    }

    public boolean checkDownLoadPackage(String packageName, String packageUrl) {
        String downloadKey = MD5.MD5_32(packageUrl);
        String ext = FileUtils.getFileExt(packageUrl);
        String fileName = downloadKey;
        if (!TextUtils.isEmpty(ext)) {
            fileName += "." + ext;
        }

        MyLog.w(TAG, "checkDownLoadPackage" + " fileName = " + fileName);
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!file.exists()) {
            return false;
        }
        String mDownloadFilename = Uri.withAppendedPath(Uri.fromFile(file), fileName).getPath();

        try {
            PackageManager pm = GlobalData.app().getApplicationContext().getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(mDownloadFilename, PackageManager.GET_ACTIVITIES);
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

    public void tryInstall(Item item) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String downloadKey = MD5.MD5_32(item.getUrl());
        String ext = FileUtils.getFileExt(item.getUrl());
        String fileName = downloadKey;
        if (!TextUtils.isEmpty(ext)) {
            fileName += "." + ext;
        }

        MyLog.w(TAG, "tryInstall" + " fileName = " + fileName);
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String mDownloadFilename = Uri.withAppendedPath(Uri.fromFile(file), fileName).getPath();

        Uri uri;
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 待补充
            uri = FileProvider.getUriForFile(GlobalData.app().getApplicationContext(), "com.wali.live.watchsdk.editinfo.fileprovider", new File(mDownloadFilename));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(new File(mDownloadFilename));
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        GlobalData.app().startActivity(intent);

    }

    public void tryLaunch(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }

        Intent intent = GlobalData.app().getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            GlobalData.app().startActivity(intent);
        } else {
            MyLog.w(TAG, "intent launch fail, packageName=" + packageName);
        }
    }

    public void addMonitorUrl(String url) {
        String downloadKey = MD5.MD5_32(url);
        long downloadId = mDownloadingMap.get(downloadKey);
        if (downloadId != 0) {
            Holder h = new Holder(downloadKey, downloadId);
            // 这里先 remove 再 add 是因为 复写 Holder 的 hashcode 和 equals 方法，这根据key字段来
            // 这里先remove 再 add 保证 key 对应 downloadid 是最新的
            mMonitorDownloadIds.remove(h);
            mMonitorDownloadIds.add(h);
        }
    }

    public void removeMonitorUrl(String url) {
        String downloadKey = MD5.MD5_32(url);
        if (TextUtils.isEmpty(downloadKey)) {
            return;
        }
        long downloadId = mDownloadingMap.get(downloadKey);
        mMonitorDownloadIds.remove(new Holder(downloadKey, downloadId));
    }

    private void updateProgress() {
        if (mDownloadSubscription != null && !mDownloadSubscription.isUnsubscribed()) {
            return;
        }
        mDownloadSubscription = Observable
                .create((new Observable.OnSubscribe<Pair<String, int[]>>() {
                    @Override
                    public void call(Subscriber<? super Pair<String, int[]>> subscriber) {
                        Cursor cursor = null;
                        try {
                            for (Holder h : mMonitorDownloadIds) {
                                MyLog.d(TAG, "updateProgress" + " holder=" + h);
                                int[] result = new int[]{
                                        -1, -1, 0, 0
                                };
                                long downloadId = h.downloadId;
                                DownloadManager.Query query = new DownloadManager.Query()
                                        .setFilterById(downloadId);
                                cursor = mDownloadManager.query(query);
                                if (cursor != null && cursor.moveToFirst()) {
                                    //已经下载文件大小
                                    result[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                    //下载文件的总大小
                                    result[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                    //下载状态
                                    result[2] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                                    //变成这种状态的原因，如因为没有网络而暂停等
                                    result[3] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                                    subscriber.onNext(new Pair<>(h.key, result));
                                } else {
                                    // 查不到了，说明任务已经被取消了或者删除了，这时别的状态没关系，
                                    // 如果之前是下载中状态就要变为未下载了
                                    EventBus.getDefault().post(new TaskEvent(h.key, TaskEvent.STATUS_REMOVE));
                                }
                            }
                        } catch (Exception e) {
                            subscriber.onError(e);
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                            subscriber.onCompleted();
                        }
                    }
                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<String, int[]>>() {
                    @Override
                    public void call(Pair<String, int[]> result) {
                        // 下载状态以数据库为准
                        int status = result.second[2];
                        MyLog.d(TAG, "call" + " status=" + result.second[2] + " key:" + result.first);

                        if (status == DownloadManager.STATUS_RUNNING) {
                            if (result.second[0] >= 0 && result.second[1] > 0) {
                                int progress = (int) (result.second[0] * 100l / result.second[1]);
                                ApkStatusEvent event = new ApkStatusEvent(result.first, ApkStatusEvent.STATUS_DOWNLOADING);
                                event.progress = progress;
                                EventBus.getDefault().post(event);
                            }
                        } else if (status == DownloadManager.STATUS_PAUSED) {
                            if (result.second[0] >= 0 && result.second[1] > 0) {
                                int progress = (int) (result.second[0] * 100l / result.second[1]);
                                ApkStatusEvent event = new ApkStatusEvent(result.first, ApkStatusEvent.STATUS_PAUSE_DOWNLOAD);
                                event.progress = progress;
                                event.reason = result.second[3];
                                EventBus.getDefault().post(event);
                            }
                        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            ApkStatusEvent event = new ApkStatusEvent(result.first, ApkStatusEvent.STATUS_DOWNLOAD_COMPELED);
                            EventBus.getDefault().post(event);
                        } else if (status == DownloadManager.STATUS_FAILED) {

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "updateProgress", throwable);
                    }
                });
    }

    private void registerObserver() {
        GlobalData.app().getApplicationContext().getContentResolver().
                registerContentObserver(
                        Uri.parse("content://downloads/my_downloads"), true, mDownloadObserver);
    }

    private void unregisterObserver() {
        GlobalData.app().getApplicationContext().getContentResolver().unregisterContentObserver(mDownloadObserver);
    }

    private void registerReceiver() {
        /**
         * 监听 下载完成 app安装完成 app 卸载完成 等广播
         */
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MyLog.d(TAG, "onReceive" + " context=" + context + " intent=" + intent);
                // 非暂停直接删除任务，也会触发，所以暂时先不用
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            }
        };
        GlobalData.app().getApplicationContext().registerReceiver(mDownloadReceiver, intentFilter);

        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        installFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        installFilter.addDataScheme("package");
        mInstallReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MyLog.d(TAG, "onReceive" + " context=" + context + " intent=" + intent);
                if (intent != null) {
                    String action = intent.getAction();
                    String packageName = intent.getData().getSchemeSpecificPart();
                    MyLog.w(TAG, "intent action=" + action);
                    switch (action) {
                        case Intent.ACTION_PACKAGE_ADDED:
                            // 安装的广播监听
                            ApkStatusEvent event = new ApkStatusEvent(packageName, null, ApkStatusEvent.STATUS_LAUNCH);
                            EventBus.getDefault().post(event);
                            break;
                        case Intent.ACTION_PACKAGE_REMOVED:

                            break;
                    }

                }
            }
        };
        GlobalData.app().getApplicationContext().registerReceiver(mInstallReceiver, installFilter);

    }

    private void unregisterReceiver() {
        MyLog.w(TAG, "unregisterReceiver");
        if (mDownloadReceiver != null) {
            GlobalData.app().getApplicationContext().unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
        }
        if (mInstallReceiver != null) {
            GlobalData.app().getApplicationContext().unregisterReceiver(mInstallReceiver);
            mInstallReceiver = null;
        }
    }

//    public void destory() {
//        unregisterObserver();
//        unregisterReceiver();
//    }


    public static class Item {
        String url;
        String title;

        public Item(String url, String title) {
            this.url = url;
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getKey() {
            String key = MD5.MD5_32(url);
            return key;
        }
    }

    public static class Holder {
        String key;
        long downloadId;

        public Holder(String key, long downloadId) {
            this.key = key;
            this.downloadId = downloadId;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Holder) {
                Holder a = (Holder) obj;
                if (a.key.equals(key)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "Holder{" +
                    "key='" + key + '\'' +
                    ", downloadId=" + downloadId +
                    '}';
        }
    }

    public static class ApkStatusEvent {
        public String downloadKey;
        public String packageName;
        public int status;
        public int progress;
        public int reason;
        public static final int STATUS_NO_DOWNLOAD = 1; //未下载
        public static final int STATUS_DOWNLOADING = 2; //下载中
        public static final int STATUS_PAUSE_DOWNLOAD = 5; //暂停下载
        public static final int STATUS_DOWNLOAD_COMPELED = 3;//已下载待安装
        public static final int STATUS_LAUNCH = 4;//启动

        public ApkStatusEvent(String downloadKey, int status) {
            this.downloadKey = downloadKey;
            this.status = status;
        }

        public ApkStatusEvent(String packageName, String downloadKey, int status) {
            this.packageName = packageName;
            this.downloadKey = downloadKey;
            this.status = status;
        }
    }

    public static class TaskEvent {
        public String downloadKey;
        public int status;
        public static final int STATUS_REMOVE = 4;//启动

        public TaskEvent(String downloadKey, int status) {
            this.downloadKey = downloadKey;
            this.status = status;
        }
    }
}
