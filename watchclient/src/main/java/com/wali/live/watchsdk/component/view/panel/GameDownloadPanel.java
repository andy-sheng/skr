package com.wali.live.watchsdk.component.view.panel;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.system.PackageUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.MyRatingBar;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.componentwrapper.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.viewmodel.GameViewModel;
import com.wali.live.watchsdk.log.LogConstants;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by lan on 17/4/11.
 */
public class GameDownloadPanel extends BaseBottomPanel<RelativeLayout, RelativeLayout>
        implements IComponentView<GameDownloadPanel.IPresenter, GameDownloadPanel.IView> {

    private static final int STATUS_NONE = 0;

    @Nullable
    protected GameDownloadPanel.IPresenter mPresenter;

    private View mBgView;
    private View mHeadBgView;
    private BaseImageView mGameIv;
    private TextView mGameTv;
    private MyRatingBar mGameRb;
    private TextView mClassTv;
    private TextView mCountTv;
    private TextView mDownloadTv;
    private ProgressBar mDownloadBar;

//    private GestureDetector mGestureDetector;

    private GameViewModel mGameViewModel;
    private long mDownloadId;
    private DownloadManager mDownloadManager;
    private BroadcastReceiver mDownloadReceiver;

    // 注册下载监听回调
    private boolean mHasRegistered;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ContentObserver mDownloadObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            updateProgress();
        }
    };
    private Subscription mDownloadSubscription;
    private int mDownloadStatus;
    private String mDownloadFilename;

    // 安装监听
    private BroadcastReceiver mInstallReceiver;
    private boolean mHasInstalled;

    @Override
    public void setPresenter(@Nullable IPresenter presenter) {
        mPresenter = presenter;
    }

    public GameDownloadPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.game_download_view;
    }

    protected String getTAG() {
        return LogConstants.GAME_DOWNLOAD_PREFIX + BaseBottomPanel.class.getSimpleName();
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();
        // 阻断下层点击事件
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mBgView = $(R.id.bg_view);
        mHeadBgView = $(R.id.head_bg_view);
        mGameIv = $(R.id.game_iv);
        mGameTv = $(R.id.game_tv);
        mGameRb = $(R.id.game_rb);
        mClassTv = $(R.id.class_tv);
        mCountTv = $(R.id.count_tv);
        mDownloadTv = $(R.id.download_tv);
        mDownloadBar = $(R.id.download_bar);

//        mGestureDetector = new GestureDetector(mContentView.getContext(),
//                new GestureDetector.SimpleOnGestureListener() {
//                    public boolean onFling(MotionEvent e1, MotionEvent e2,
//                                           float velocityX, float velocityY) {
//                        if (velocityY > 1000) {
//                            hideSelf(true);
//                        }
//                        return false;
//                    }
//                });
//        mHeadBgView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                mGestureDetector.onTouchEvent(event);
//                return true;
//            }
//        });

        mBgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideGameDownloadView();
            }
        });

        mHeadBgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideGameDownloadView();
            }
        });
    }

    private void inflate() {
        if (mContentView == null) {
            inflateContentView();
        }
    }

    private void showGameDownloadView() {
        inflate();

        GameViewModel gameModel = mPresenter.getGameModel();
        if (gameModel != mGameViewModel) {
            mGameViewModel = gameModel;
            MyLog.d(TAG, "gameModel start=" + mGameViewModel.getGameId());

            FrescoWorker.loadImage(mGameIv, ImageFactory.newHttpImage(mGameViewModel.getIconUrl()).build());

            mGameTv.setText(mGameViewModel.getName());
            mGameRb.setStarValue(mGameViewModel.getRatingScore());

            mClassTv.setText(mGameViewModel.getClassName());

            int count = mGameViewModel.getDownloadCount();
            if (count >= 10000) {
                mCountTv.setText(mContentView.getContext().getString(R.string.ten_thousand_download, count / 10000));
            } else {
                mCountTv.setText(mContentView.getContext().getString(R.string.count_download, count));
            }

            if (TextUtils.isEmpty(mGameViewModel.getDownloadUrl())) {
                mDownloadTv.setVisibility(View.GONE);
                mDownloadBar.setVisibility(View.GONE);
            } else {
                mHasInstalled = PackageUtils.isInstallPackage(mGameViewModel.getPackageName());
                if (mHasInstalled) {
                    mDownloadTv.setText(R.string.open);
                } else {
                    mDownloadTv.setText(R.string.download);
                }
                mDownloadBar.setProgress(100);
                mDownloadTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyLog.d(TAG, "onClick");
                        if (mHasInstalled) {
                            tryLaunchApk(mGameViewModel.getPackageName());
                            return;
                        }
                        if (mDownloadId == 0) {
                            beginDownload();
                            return;
                        }
                        if (mDownloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                            tryInstallApk(mDownloadId);
                        }
                    }
                });
                mDownloadTv.setVisibility(View.VISIBLE);
                mDownloadBar.setVisibility(View.VISIBLE);
            }
        }
        showSelf(true, mIsLandscape);
    }

    private void hideGameDownloadView() {
        hideSelf(true);
    }

    private void beginDownload() {
        MyLog.d(TAG, "beginDownload");

        registerObserver();
        registerReceiver();

        if (mDownloadManager == null) {
            mDownloadManager = (DownloadManager) mParentView.getContext().getSystemService(DOWNLOAD_SERVICE);
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mGameViewModel.getDownloadUrl()));
        MyLog.w(TAG, "gameName=" + mGameViewModel.getName());
        request.setTitle(mGameViewModel.getName());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        mDownloadId = mDownloadManager.enqueue(request);
        MyLog.w(TAG, "downloadId=" + mDownloadId);

        ToastUtils.showToast(R.string.downloading);
        mDownloadTv.setText(0 + "%");
        mDownloadBar.setProgress(0);

        mPresenter.reportDownloadKey();
    }

    private void cancelDownload() {
        if (mDownloadId != 0) {
            mDownloadManager.remove(mDownloadId);
        }
    }

    private void tryLaunchApk(String packageName) {
        Intent intent = mParentView.getContext().getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mParentView.getContext().startActivity(intent);
        } else {
            MyLog.w(TAG, "intent launch fail, packageName=" + packageName);
        }
    }

    private void tryInstallApk(long downloadId) {
        MyLog.w(TAG, "tryInstallApk " + downloadId + ":" + mDownloadId + "; filename=" + mDownloadFilename);
        if (downloadId == mDownloadId) {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            Uri uri = Uri.fromFile(new File(mDownloadFilename));
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            mParentView.getContext().startActivity(intent);
        }
    }

    private void registerObserver() {
        if (!mHasRegistered) {
            mHasRegistered = true;
            mParentView.getContext().getContentResolver().
                    registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, mDownloadObserver);
        }
    }

    private void unregisterObserver() {
        if (mHasRegistered) {
            mParentView.getContext().getContentResolver().unregisterContentObserver(mDownloadObserver);
            mHasRegistered = false;
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 非暂停直接删除任务，也会触发，所以暂时先不用
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                MyLog.d(TAG, "download complete=" + downloadId + "; myId=" + mDownloadId + ", status=" + mDownloadStatus);
            }
        };
        mParentView.getContext().registerReceiver(mDownloadReceiver, intentFilter);

        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        installFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        installFilter.addDataScheme("package");
        mInstallReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && mGameViewModel != null) {
                    String action = intent.getAction();
                    String packageName = intent.getData().getSchemeSpecificPart();
                    MyLog.d(TAG, "intent packageName=" + packageName + ";modelPackageName=" + mGameViewModel.getPackageName());
                    if (packageName.equals(mGameViewModel.getPackageName())) {
                        MyLog.w(TAG, "intent action=" + action);
                        switch (action) {
                            case Intent.ACTION_PACKAGE_ADDED:
                                mHasInstalled = true;
                                mDownloadTv.setText(R.string.open);
                                mDownloadBar.setProgress(100);
                                break;
                            case Intent.ACTION_PACKAGE_REMOVED:
                                MyLog.w(TAG, "intent packageRemove downloadStatus=" + mDownloadStatus);
                                mHasInstalled = false;
                                if (mDownloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                                    mDownloadTv.setText(R.string.install);
                                    mDownloadBar.setProgress(100);
                                } else if (mDownloadStatus == STATUS_NONE) {
                                    mDownloadTv.setText(R.string.download);
                                    mDownloadBar.setProgress(100);
                                }
                                break;
                        }
                    }
                }
            }
        };
        mParentView.getContext().registerReceiver(mInstallReceiver, installFilter);
    }

    private void unregisterReceiver() {
        MyLog.w(TAG, "unregisterReceiver");
        if (mDownloadReceiver != null) {
            mParentView.getContext().unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
        }
        if (mInstallReceiver != null) {
            mParentView.getContext().unregisterReceiver(mInstallReceiver);
            mInstallReceiver = null;
        }
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

                                //下载地址
                                if (TextUtils.isEmpty(mDownloadFilename)) {
                                    mDownloadFilename = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                                }
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
                                mDownloadTv.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDownloadTv.setText(R.string.install);
                                        mDownloadBar.setProgress(100);
                                    }
                                }, 400);
                            } else if (mDownloadStatus == STATUS_NONE) {
                                // 重置下载标识
                                mDownloadId = 0;
                                mDownloadFilename = null;
                                mDownloadTv.setText(R.string.download);
                                mDownloadBar.setProgress(100);
                            }
                        } else if (mDownloadStatus == DownloadManager.STATUS_RUNNING) {
                            if (result[0] >= 0 && result[1] > 0) {
                                int progress = (int) (result[0] * 100l / result[1]);
                                mDownloadTv.setText(progress + "%");
                                mDownloadBar.setProgress(progress);
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

    private void cancelSubscription() {
        if (mDownloadSubscription != null && !mDownloadSubscription.isUnsubscribed()) {
            mDownloadSubscription.unsubscribe();
        }
    }

    private void destroy() {
        MyLog.w(TAG, "destroy");
        cancelDownloadTask();
    }

    private void cancelDownloadTask() {
        MyLog.d(TAG, "cancelDownloadTask");
        unregisterObserver();
        unregisterReceiver();
        cancelSubscription();

        mDownloadId = 0;
    }

    // 隐藏自己，并且取消下载任务
    private void reset() {
        hideSelf(false);
        cancelDownloadTask();
        mGameViewModel = null;
    }

    @Override
    public GameDownloadPanel.IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements GameDownloadPanel.IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }

            @Override
            public void showGameDownloadView() {
                GameDownloadPanel.this.showGameDownloadView();
            }

            @Override
            public void hideGameDownloadView() {
                GameDownloadPanel.this.hideGameDownloadView();
            }

            @Override
            public void inflate() {
                GameDownloadPanel.this.inflate();
            }

            @Override
            public void reset() {
                GameDownloadPanel.this.reset();
            }

            @Override
            public boolean isShow() {
                return GameDownloadPanel.this.isShow();
            }

            @Override
            public void destroy() {
                GameDownloadPanel.this.destroy();
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        GameViewModel getGameModel();

        void reportDownloadKey();
    }

    public interface IView extends IViewProxy {
        void inflate();

        void showGameDownloadView();

        void hideGameDownloadView();

        void reset();

        boolean isShow();

        void destroy();
    }
}
