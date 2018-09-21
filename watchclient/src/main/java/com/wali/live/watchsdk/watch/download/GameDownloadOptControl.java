package com.wali.live.watchsdk.watch.download;

import com.base.log.MyLog;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;
import com.wali.live.watchsdk.statistics.MilinkStatistics;
import com.wali.live.watchsdk.watch.download.callback.IDownloadGameOptCallback;
import com.wali.live.watchsdk.watch.model.WatchGameInfoConfig;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_BIZTYPE_START_DOWNLOAD;
import static com.wali.live.watchsdk.statistics.item.GameWatchDownloadStatisticItem.GAME_WATCH_TYPE_DOWNLOAD;

/**
 * Created by zhujianning on 18-8-30.
 */

public class GameDownloadOptControl {
    private static final String TAG = "GameDownloadOptControl";

    public static final int TYPE_GAME_DOWNLOAD_DEFAULT = 1;
    public static final int TYPE_GAME_BEGIN_DOWNLOAD = 2;
    public static final int TYPE_GAME_DOWNLOAD_COMPELED = 3;
    public static final int TYPE_GAME_INSTALL_FINISH = 4;
    public static final int TYPE_GAME_PAUSE_DOWNLOAD = 5;
    public static final int TYPE_GAME_REMOVE = 6;
    public static final int TYPE_GAME_CONTINUE_DOWNLOAD = 7;
    public static final int TYPE_GAME_INSTALLING = 8;//待定
    public static final int TYPE_GAME_LAUNCH_SUCCESS = 9; //启动成功
    public static final int TYPE_GAME_DOWNLOAD_FAILED = 10; //下载失败

    private static Subscription mDownloadSubscription;
    private static Subscription mQueryGameDownStatusSubscription;

    /**
     * 如果成功就交给宿主的回调
     * 如果不成功抛出eventbus事件
     *
     * @param model
     */
    public static void tryQueryGameDownStatus(final GameInfoModel model) {
        if (model == null) {
            return;
        }

        if(!HostChannelManager.getInstance().isFromGameCenter()) {
            EventBus.getDefault().post(new EventClass.UpdateGameInfoStatus(model));
            return;
        }

        if (mQueryGameDownStatusSubscription != null && !mQueryGameDownStatusSubscription.isUnsubscribed()) {
            return;
        }

        mQueryGameDownStatusSubscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean ret = MiLiveSdkBinder.getInstance().onEventQueryGameDownloadStatus(HostChannelManager.getInstance().getChannelId()
                        , model.getGameId()
                        , model.getPackageName()
                        , model.getPackageUrl());

                subscriber.onNext(ret);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (!aBoolean) {
                            EventBus.getDefault().post(new EventClass.UpdateGameInfoStatus(model));
                        }
                    }
                });
    }

    public static void tryDownloadGame(final int type, final GameInfoModel model, final IDownloadGameOptCallback callback) {
        if (model == null) {
            return;
        }

        if(!HostChannelManager.getInstance().isFromGameCenter()) {
            if(callback != null) {
                callback.onResultCallback(model, type);
            }
            return;
        }

        if (mDownloadSubscription != null && !mDownloadSubscription.isUnsubscribed()) {
            return;
        }

        mDownloadSubscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean ret = MiLiveSdkBinder.getInstance().onEventSendGameDownloadRequest(HostChannelManager.getInstance().getChannelId()
                        , type
                        , model.getGameId()
                        , model.getPackageName()
                        , model.getPackageUrl());

                subscriber.onNext(ret);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (!aBoolean && callback != null) {
                            callback.onResultCallback(model, type);
                        } else {
                            //从游戏中心发起下载打点
                            if(type == TYPE_GAME_BEGIN_DOWNLOAD) {
                                firstStartDownloadStatistic(model.getPackageUrl());
                            }
                        }
                    }
                });
    }

    private static void firstStartDownloadStatistic(String url) {
        WatchGameInfoConfig.InfoItem infoItem = WatchGameInfoConfig.sGameInfoMap.get(url);
        if (infoItem != null) {
            MyLog.d(TAG, "firstStartDownloadStatistic gameId:" + infoItem.gameId + ", packageName:" + infoItem.packageName);
            MilinkStatistics.getInstance().statisticGameWatchDownload(GAME_WATCH_TYPE_DOWNLOAD,
                    GAME_WATCH_BIZTYPE_START_DOWNLOAD, infoItem.anchorId, infoItem.channelId, infoItem.packageName);
        }
    }
}

