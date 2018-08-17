package com.wali.live.watchsdk.watch.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

public class GameDownLoadUtil {

    public static String TAG = "GameDownLoadUtil";

    private static final int STATUS_NONE = 0;

    public static final int DOWNLOAD = 1; //下载
    public static final int DOWNLOAD_RUNNING = 2; //下载中
    public static final int GAME_INSTALL = 3;//安装
    public static final int GAME_LUNCH = 4;//启动

    GameInfoModel mGameInfoModel;

    // 注册下载监听回调
    private boolean mHasRegistered;
    private BroadcastReceiver mDownloadReceiver;

    // 安装监听
    private BroadcastReceiver mInstallReceiver;
    private boolean mHasInstalled;

    private DownloadManager mDownloadManager;
    private long mDownloadId;
    private String mDownloadFilename;

    private Subscription mDownloadSubscription;
    private Subscription mCheckDownloadSubscription;

    private int mDownloadStatus;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ContentObserver mDownloadObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            updateProgress();
        }
    };

    private final static GameDownLoadUtil sInstance = new GameDownLoadUtil();

    private GameDownLoadUtil() {

    }

    public void init(GameInfoModel gameInfoModel) {
        this.mGameInfoModel = gameInfoModel;
        registerObserver();
        registerReceiver();
    }

    public static GameDownLoadUtil getInstance() {
        return sInstance;
    }

    public void beginDownload(GameInfoModel gameInfoModel) {
        MyLog.d(TAG, "beginDownload");
        this.mGameInfoModel = gameInfoModel;
        registerObserver();
        registerReceiver();

        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) GlobalData.app().getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mGameInfoModel.getPackageUrl()));
        MyLog.w(TAG, "gameName=" + mGameInfoModel.getPackageName());
        request.setTitle(mGameInfoModel.getGameName());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String filename = mGameInfoModel.getGameId() + ".apk";
        // 由于COLUMN_LOCAL_FILENAME废弃，生成固定的下载路径
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mDownloadFilename = Uri.withAppendedPath(Uri.fromFile(file), filename).getPath();
        MyLog.d(TAG, "mDownloadFilename=" + mDownloadFilename);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        mDownloadId = mDownloadManager.enqueue(request);
        saveDownId(mGameInfoModel.getGameId(), mDownloadId);
        MyLog.w(TAG, "downloadId=" + mDownloadId);

        ToastUtils.showToast(R.string.downloading);
    }

    public void checkDownLoad(GameInfoModel gameInfoModel) {
        if (mCheckDownloadSubscription != null && !mCheckDownloadSubscription.isUnsubscribed()) {
            return;
        }

        this.mGameInfoModel = gameInfoModel;

        mCheckDownloadSubscription = Observable
                .create((new Observable.OnSubscribe<int[]>() {
                    @Override
                    public void call(Subscriber<? super int[]> subscriber) {
                        int[] result = new int[]{
                                -1, -1, 0
                        };

                        if (mDownloadId == 0) {
                            mDownloadId = getDownId(mGameInfoModel.getGameId());
                        }

                        DownloadManager.Query query = new DownloadManager.Query().setFilterById(mDownloadId);
                        Cursor cursor = null;
                        try {
                            if (mDownloadManager == null) {
                                mDownloadManager = (DownloadManager) GlobalData.app().getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
                            }
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
                        // 下载状态以数据库为准
                        int status = result[2];
                        if (mDownloadStatus != status) {
                            MyLog.d(TAG, "status=" + result[2] + "; oldStatus=" + mDownloadStatus);
                            mDownloadStatus = status;

                            if (mDownloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtils.showToast("下载完成,安装");
                                        EventBus.getDefault().post(new EventClass.GameDownLoadEvent(mGameInfoModel.getGameId(), EventClass.GameDownLoadEvent.GAME_INSTALL, 100));
                                    }
                                }, 400);
                            } else if (mDownloadStatus == STATUS_NONE) {
                                // 重置下载标识,开始下载
                                mDownloadId = 0;
                                mDownloadFilename = null;
                                beginDownload(mGameInfoModel);
                            }
                        } else if (mDownloadStatus == DownloadManager.STATUS_RUNNING) {
                            if (result[0] >= 0 && result[1] > 0) {
                                int progress = (int) (result[0] * 100l / result[1]);
                                ToastUtils.showToast("正在下载");
                                EventBus.getDefault().post(new EventClass.GameDownLoadEvent(mGameInfoModel.getGameId(), EventClass.GameDownLoadEvent.DOWNLOAD_RUNNING, progress));
                            }
                        } else if (mDownloadStatus == STATUS_NONE) {
                            beginDownload(mGameInfoModel);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "updateProgress", throwable);
                    }
                });
    }

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

                        if (mDownloadId == 0) {
                            mDownloadId = getDownId(mGameInfoModel.getGameId());
                        }

                        DownloadManager.Query query = new DownloadManager.Query().setFilterById(mDownloadId);
                        Cursor cursor = null;
                        try {
                            if (mDownloadManager == null) {
                                mDownloadManager = (DownloadManager) GlobalData.app().getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
                            }
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
                        // 下载状态以数据库为准
                        int status = result[2];
                        if (mDownloadStatus != status) {
                            MyLog.d(TAG, "status=" + result[2] + "; oldStatus=" + mDownloadStatus);
                            mDownloadStatus = status;

                            if (mDownloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        EventBus.getDefault().post(new EventClass.GameDownLoadEvent(mGameInfoModel.getGameId(), EventClass.GameDownLoadEvent.GAME_INSTALL, 100));
                                    }
                                }, 400);
                            } else if (mDownloadStatus == STATUS_NONE) {
                                // 重置下载标识
                                mDownloadId = 0;
                                mDownloadFilename = null;

                                EventBus.getDefault().post(new EventClass.GameDownLoadEvent(mGameInfoModel.getGameId(), EventClass.GameDownLoadEvent.DOWNLOAD, 100));
                            }
                        } else if (mDownloadStatus == DownloadManager.STATUS_RUNNING) {
                            if (result[0] >= 0 && result[1] > 0) {
                                int progress = (int) (result[0] * 100l / result[1]);
                                EventBus.getDefault().post(new EventClass.GameDownLoadEvent(mGameInfoModel.getGameId(), EventClass.GameDownLoadEvent.DOWNLOAD_RUNNING, progress));
                            }
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
        if (!mHasRegistered) {
            mHasRegistered = true;
            GlobalData.app().getApplicationContext().getContentResolver().
                    registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, mDownloadObserver);
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
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
                if (intent != null && mGameInfoModel != null) {
                    String action = intent.getAction();
                    String packageName = intent.getData().getSchemeSpecificPart();
                    MyLog.d(TAG, "intent packageName=" + packageName + ";modelPackageName=" + mGameInfoModel.getPackageName());
                    if (packageName.equals(mGameInfoModel.getPackageName())) {
                        MyLog.w(TAG, "intent action=" + action);
                        switch (action) {
                            case Intent.ACTION_PACKAGE_ADDED:
                                mHasInstalled = true;
                                EventBus.getDefault().post(new EventClass.GameDownLoadEvent(mGameInfoModel.getGameId(), EventClass.GameDownLoadEvent.GAME_LUNCH, 100));
                                break;
                            case Intent.ACTION_PACKAGE_REMOVED:
                                MyLog.w(TAG, "intent packageRemove downloadStatus=" + mDownloadStatus);
                                mHasInstalled = false;
                                if (mDownloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                                    EventBus.getDefault().post(new EventClass.GameDownLoadEvent(mGameInfoModel.getGameId(), EventClass.GameDownLoadEvent.GAME_INSTALL, 100));
                                } else if (mDownloadStatus == STATUS_NONE) {
                                    EventBus.getDefault().post(new EventClass.GameDownLoadEvent(mGameInfoModel.getGameId(), EventClass.GameDownLoadEvent.DOWNLOAD, 100));
                                }
                                break;
                        }
                    }
                }
            }
        };
        GlobalData.app().getApplicationContext().registerReceiver(mInstallReceiver, installFilter);

    }

    private void unregisterObserver() {
        if (mHasRegistered) {
            GlobalData.app().getApplicationContext().getContentResolver().unregisterContentObserver(mDownloadObserver);
            mHasRegistered = false;
        }
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

    public void destory() {
        unregisterObserver();
        unregisterReceiver();
    }


    static final String JSON_KEY_GAME_ID = "game_id";
    static final String JSON_KEY_GAME_DOWNLOAD_ID = "down_id";
    static final String PRE_KEY_GAME_DOWNLOAD = "pre_key_game_download";

    private void saveDownId(long gameId, long downId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_KEY_GAME_ID, gameId);
            jsonObject.put(JSON_KEY_GAME_DOWNLOAD_ID, downId);
        } catch (JSONException e) {
            MyLog.e(e);
        }

        String string = jsonObject.toString();
        String stringBuilder = PreferenceUtils.getSettingString(GlobalData.app().getApplicationContext(), PRE_KEY_GAME_DOWNLOAD, null);
        if (TextUtils.isEmpty(stringBuilder)) {
            stringBuilder = string;
        } else {
            // todo 有优化的空间,优化去掉重复的
            stringBuilder = stringBuilder + "/" + string;
        }

        PreferenceUtils.setSettingString(GlobalData.app().getApplicationContext(), PRE_KEY_GAME_DOWNLOAD, stringBuilder);
    }

    private long getDownId(long gameId) {
        String string = PreferenceUtils.getSettingString(GlobalData.app().getApplicationContext(), PRE_KEY_GAME_DOWNLOAD, null);
        if (TextUtils.isEmpty(string)) {
            return 0;
        }

        String[] list = string.split("/");
        Map<Long, Long> map = new HashMap<>();
        for (String s : list) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                Long optGameId = jsonObject.optLong(JSON_KEY_GAME_ID);
                Long optDownId = jsonObject.optLong(JSON_KEY_GAME_DOWNLOAD_ID);
                map.put(optGameId, optDownId);
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
        }

        if (map.containsKey(gameId)) {
            return map.get(gameId);
        }

        return 0;
    }
}
