package com.wali.live.watchsdk.watch.download;

import android.content.Intent;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.system.PackageUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.InstallOrLaunchEvent.FAILED;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.InstallOrLaunchEvent.STATTUS_INSTALL;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.InstallOrLaunchEvent.STATTUS_LAUNCH;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.InstallOrLaunchEvent.SUCCESS;

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
            EventBus.getDefault().post(new EventClass.UpdateGameInfoStatus());
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
                            EventBus.getDefault().post(new EventClass.UpdateGameInfoStatus());
                        }
                    }
                });
    }

    public static void tryDownloadGame(final int type, final GameInfoModel model) {
        if (model == null) {
            return;
        }

        if(!HostChannelManager.getInstance().isFromGameCenter()) {
            downloadByMiLive(type, model);
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
                        if (!aBoolean) {
                            downloadByMiLive(type, model);
                        }
                    }
                });
    }

    private static void downloadByMiLive(int type, GameInfoModel model) {
        if (type == TYPE_GAME_BEGIN_DOWNLOAD
                || type == TYPE_GAME_CONTINUE_DOWNLOAD) {
            CustomDownloadManager.Item item = new CustomDownloadManager.Item(model.getPackageUrl(), model.getGameName());
            CustomDownloadManager.getInstance().beginDownload(item, GlobalData.app());
        } else if (type == TYPE_GAME_PAUSE_DOWNLOAD) {
            CustomDownloadManager.getInstance().pauseDownload(model.getPackageUrl());
        } else if (type == TYPE_GAME_DOWNLOAD_COMPELED) {
            String apkPath = CustomDownloadManager.getInstance().getDownloadPath(model.getPackageUrl());
            if (PackageUtils.tryInstall(apkPath)) {
                EventBus.getDefault().post(new CustomDownloadManager.InstallOrLaunchEvent(model, STATTUS_INSTALL, SUCCESS));
            } else {
                ToastUtils.showToast("apk包解析失败，重新下载");
                EventBus.getDefault().post(new CustomDownloadManager.InstallOrLaunchEvent(model, STATTUS_INSTALL, FAILED));
            }
        } else if (type == TYPE_GAME_INSTALL_FINISH){
            if (PackageUtils.tryLaunch(model.getPackageName())) {
                EventBus.getDefault().post(new CustomDownloadManager.InstallOrLaunchEvent(model, STATTUS_LAUNCH, SUCCESS));
            } else {
                EventBus.getDefault().post(new CustomDownloadManager.InstallOrLaunchEvent(model, STATTUS_LAUNCH, FAILED));
                ToastUtils.showToast("启动失败");
            }
        }
    }
}

