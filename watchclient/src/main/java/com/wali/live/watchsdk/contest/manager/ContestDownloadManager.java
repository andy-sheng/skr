package com.wali.live.watchsdk.contest.manager;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.base.utils.system.PackageUtils;
import com.base.utils.toast.ToastUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.model.DownloadItemInfo;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditAvatarPresenter;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS_FAILED;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS_NONE;

/**
 * Created by wanglinzhang on 2018/1/30.
 */
public class ContestDownloadManager extends BaseRxPresenter<IContestDownloadView> {
    private DownloadManager mDownloadManager;
    private BroadcastReceiver mDownloadReceiver;
    private BroadcastReceiver mInstallReceiver;

    private Subscription mDownloadSubscription;
    private DownloadItemInfo mDownloadInfo;

    private State mState;

    private int mDownloadStatus;
    private boolean mReceiverRegistered;
    private boolean mObserverRegistered;

    private long mDownloadId;
    private boolean mHasInstalled;
    private String mDownloadFilename;
    private int mProgress = -1;

    private Context mParentContext;

    public ContestDownloadManager(IContestDownloadView view, Context context) {
        super(view);
        mParentContext = context;
    }

    //Todo: add download task
    public void addTask(DownloadItemInfo itemInfo) {
        mDownloadInfo = itemInfo;
    }

    public void startDownload() {
        MyLog.d(TAG, "beginDownload");

        registerObserver();
        registerReceiver();

        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) mParentContext.getSystemService(DOWNLOAD_SERVICE);
        }
        updateState(State.StartDownload);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mDownloadInfo.getDownloadUrl()));
        MyLog.w(TAG, "gameName=" + mDownloadInfo.getName());
        request.setTitle(mDownloadInfo.getName());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String filename = mDownloadInfo.getName() + "_" + System.currentTimeMillis() + ".apk";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        // 由于COLUMN_LOCAL_FILENAME废弃，采用提前设置路径的方案
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mDownloadFilename = Uri.withAppendedPath(Uri.fromFile(file), filename).getPath();
        MyLog.d(TAG, "mDownloadFilename=" + mDownloadFilename);

        mDownloadId = mDownloadManager.enqueue(request);
        MyLog.w(TAG, "downloadId=" + mDownloadId);

        ToastUtils.showToast(R.string.downloading);
        updateProgress(0);
    }

    public void doNext() {
        switch (mState) {
            case Idle:
                startDownload();
                break;
            case DownloadSuccess:
                tryInstallAPK();
                break;
            case InstallSuccess:
                tryLaunchAPk();
                break;
            case Launch:
                tryLaunchAPk();
                break;
        }
    }

    private void tryInstallAPK() {
        MyLog.w(TAG, "tryInstallApk " + ":" + mDownloadId + "; filename=" + mDownloadFilename);
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri;
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(mParentContext, EditAvatarPresenter.AUTHORITY, new File(mDownloadFilename));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(new File(mDownloadFilename));
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        mParentContext.startActivity(intent);
    }

    private void tryLaunchAPk() {
        Intent intent = mParentContext.getPackageManager().getLaunchIntentForPackage(mDownloadInfo.getPackageName());
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mParentContext.startActivity(intent);
        } else {
            MyLog.w(TAG, "intent launch fail, packageName=" + mDownloadInfo.getPackageName());
        }
        updateState(State.Launch);
    }

    public void initState() {
        mHasInstalled = PackageUtils.isInstallPackage(mDownloadInfo.getPackageName());
        if (mHasInstalled) {
            mState = State.InstallSuccess;
        } else {
            mState = State.Idle;
        }
        mView.statusChanged(mState);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ContentObserver mDownloadObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            updateProgress();
        }
    };

    private void updateProgress() {
        if (mDownloadSubscription != null && !mDownloadSubscription.isUnsubscribed()) {
            return;
        }
        mDownloadSubscription = Observable
                .create((new Observable.OnSubscribe<int[]>() {
                    @Override
                    public void call(Subscriber<? super int[]> subscriber) {
                        int[] result = new int[]{
                                -1, -1, 0
                        };
                        DownloadManager.Query query = new DownloadManager.Query().setFilterById(mDownloadId);
                        Cursor cursor = null;
                        try {
                            cursor = mDownloadManager.query(query);
                            if (cursor != null && cursor.moveToFirst()) {
                                //已经下载文件大小
                                result[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                //下载文件的总大小
                                result[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                //下载状态
                                result[2] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                            }
                            subscriber.onNext(result);
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
                .subscribe(new Action1<int[]>() {
                    @Override
                    public void call(int[] result) {
                        int status = result[2];
                        if (mDownloadStatus != status) {
                            MyLog.d(TAG, "status=" + result[2] + "; oldStatus=" + mDownloadStatus);
                            mDownloadStatus = status;
                            if (mDownloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                                updateState(State.DownloadSuccess);
                            } else if (mDownloadStatus == STATUS_NONE) {
                                // 重置下载标识
                                //Todo: what to do?
                                mDownloadId = 0;
                            } else if (mDownloadStatus == STATUS_FAILED) {
                                updateState(State.DownloadFailed);
                            }
                        } else if (mDownloadStatus == DownloadManager.STATUS_RUNNING) {
                            if (result[0] >= 0 && result[1] > 0) {
                                int progress = (int) (result[0] * 100l / result[1]);
                                updateProgress(progress);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        updateState(State.DownloadFailed);
                        MyLog.e(TAG, "updateProgress", throwable);
                    }
                });
    }

    private void registerObserver() {
        if (!mObserverRegistered) {
            mObserverRegistered = true;
            mParentContext.getContentResolver().
                    registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, mDownloadObserver);
        }
    }

    private void unregisterObserver() {
        if (mObserverRegistered) {
            mParentContext.getContentResolver().unregisterContentObserver(mDownloadObserver);
            mObserverRegistered = false;
        }
    }

    private void registerDownloadReceiver() {
        IntentFilter downloadFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 非暂停直接删除任务，也会触发，所以暂时先不用
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                MyLog.d(TAG, "download complete=" + downloadId + "; myId=" + mDownloadId + ", status=" + mDownloadStatus);
            }
        };
        mParentContext.registerReceiver(mDownloadReceiver, downloadFilter);
    }

    private void registerInstallReceiver() {
        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        installFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        installFilter.addDataScheme("package");
        mInstallReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && mDownloadInfo != null) {
                    String action = intent.getAction();
                    String packageName = intent.getData().getSchemeSpecificPart();
                    MyLog.d(TAG, "intent packageName=" + packageName + ";modelPackageName=" + mDownloadInfo.getPackageName());
                    if (packageName.equals(mDownloadInfo.getPackageName())) {
                        MyLog.w(TAG, "intent action=" + action);
                        switch (action) {
                            case Intent.ACTION_PACKAGE_ADDED:
                                mHasInstalled = true;
                                updateState(State.InstallSuccess);
                                break;
                            case Intent.ACTION_PACKAGE_REMOVED:
                                MyLog.w(TAG, "intent packageRemove downloadStatus=" + mDownloadStatus);
                                mHasInstalled = false;
                                updateState(State.Idle);
                                break;
                        }
                    }
                }
            }
        };
        mParentContext.registerReceiver(mInstallReceiver, installFilter);
    }

    private void registerReceiver() {
        if (mReceiverRegistered) {
            return;
        }
        registerDownloadReceiver();
        registerInstallReceiver();
        mReceiverRegistered = true;
    }

    private void unregisterDownloadReceiver() {
        MyLog.w(TAG, "unregisterReceiver");
        if (mDownloadReceiver != null) {
            mParentContext.unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
        }
    }

    private void unregisterInstallReceiver() {
        if (mInstallReceiver != null) {
            mParentContext.unregisterReceiver(mInstallReceiver);
            mInstallReceiver = null;
        }
    }

    private void cancelSubscription() {
        if (mDownloadSubscription != null && !mDownloadSubscription.isUnsubscribed()) {
            mDownloadSubscription.unsubscribe();
        }
    }

    private void unregisterReceiver() {
        if (mReceiverRegistered == false) {
            return;
        }
        unregisterDownloadReceiver();
        unregisterInstallReceiver();
        mReceiverRegistered = false;
    }

    public void destroy() {
        unregisterObserver();
        unregisterReceiver();
        cancelSubscription();
    }

    private void updateState(State state) {
        if (mState == state) {
            return;
        }
        mState = state;
        mView.statusChanged(mState);
    }


    private void updateProgress(int progress) {
        if (mProgress == progress) {
            return;
        }
        mProgress = progress;
        mView.processChanged(mProgress);
    }

    public enum State {
        Idle, StartDownload, DownloadFailed, DownloadSuccess, StartInstall, InstallFailed, InstallSuccess, Launch
    }

}
