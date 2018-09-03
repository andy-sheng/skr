package com.wali.live.watchsdk.watch.download;

import android.content.Intent;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-8-30.
 *
 */

public class GameDownloadOptControl {
    private static final String TAG = "GameDownloadOptControl";

    public static final int TYPE_GAME_DOWNLOAD_DEFAULT = 1;
    public static final int TYPE_GAME_BEGIN_DOWNLOAD = 2;
    public static final int TYPE_GAME_DOWNLOAD_COMPELED = 3;
    public static final int TYPE_GAME_INSTALL_FINISH = 4;
    public static final int TYPE_GAME_PAUSE_DOWNLOAD= 5;
    public static final int TYPE_GAME_REMOVE= 6;
    public static final int TYPE_GAME_CONTINUE_DOWNLOAD= 7;
    public static final int TYPE_GAME_INSTALLING= 8;//待定

    private static Subscription mDownloadSubscription;


    public static void tryDownloadGame(final int type, final GameInfoModel model) {
        if(model == null) {
            return;
        }

        MyLog.d(TAG, "game info tostirng:" + model.toString());

        if (mDownloadSubscription != null && !mDownloadSubscription.isUnsubscribed()) {
            return;
        }

        mDownloadSubscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean isLaunshSucess = MiLiveSdkBinder.getInstance().onEventGameInstallOpt(HostChannelManager.getInstance().getChannelId()
                        , type
                        , model.getGameId()
                        , model.getPackageName()
                        , model.getPackageUrl());
                subscriber.onNext(isLaunshSucess);
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
                            if(type == TYPE_GAME_BEGIN_DOWNLOAD
                                    || type == TYPE_GAME_CONTINUE_DOWNLOAD) {
                                CustomDownloadManager.Item item = new CustomDownloadManager.Item(model.getPackageUrl(), model.getGameName());
                                CustomDownloadManager.getInstance().beginDownload(item, GlobalData.app());
                            } else if(type == TYPE_GAME_PAUSE_DOWNLOAD) {
                                CustomDownloadManager.getInstance().pauseDownload(model.getPackageUrl());
                            }
                        }
                    }
                });
    }
}
