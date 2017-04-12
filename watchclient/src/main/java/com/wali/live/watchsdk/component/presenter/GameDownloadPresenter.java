package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.log.logger.Logger;
import com.base.utils.Constants;
import com.base.version.http.HttpUtils;
import com.base.version.http.SimpleRequest;
import com.base.version.http.bean.BasicNameValuePair;
import com.base.version.http.bean.NameValuePair;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.GameDownloadPanel;
import com.wali.live.watchsdk.component.viewmodel.GameViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by xiaolan on 2017/04/10.
 */
public class GameDownloadPresenter extends ComponentPresenter<GameDownloadPanel.IView>
        implements GameDownloadPanel.IPresenter {
    private static final String TAG = "GameDownloadPresenter";

    private static final String GAME_INFO_URL = "http://app.migc.xiaomi.com/contentapi/m/gameinfo?gameId=%s";
    private RoomBaseDataModel mMyRoomData;

    private GameViewModel mGameModel;

    public GameDownloadPresenter(@NonNull IComponentController componentController,
                                 @NonNull RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
        registerAction(ComponentController.MSG_ON_LIVE_SUCCESS);
        registerAction(ComponentController.MSG_SHOW_GAME_DOWNLOAD);
        registerAction(ComponentController.MSG_ON_BACK_PRESSED);
    }

    private void getGameInfo() {
        MyLog.d(TAG, "getGameInfo: gameId=" + mMyRoomData.getGameId());
        if (TextUtils.isEmpty(mMyRoomData.getGameId())) {
            return;
        }
        // 启动2分钟后的定时任务
        Observable.timer(2 * 60, TimeUnit.SECONDS)
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Observable
                                .create((new Observable.OnSubscribe<GameViewModel>() {
                                    @Override
                                    public void call(Subscriber<? super GameViewModel> subscriber) {
                                        String url = String.format(GAME_INFO_URL, mMyRoomData.getGameId());
                                        List<NameValuePair> postBody = new ArrayList();
                                        postBody.add(new BasicNameValuePair("gameId", String.valueOf(mMyRoomData.getGameId())));
                                        try {
                                            SimpleRequest.StringContent result = HttpUtils.doV2Get(url, postBody);
                                            GameViewModel gameModel = new GameViewModel(result.getBody());
                                            subscriber.onNext(gameModel);
                                            subscriber.onCompleted();
                                        } catch (Exception e) {
                                            Logger.e(TAG, e.getMessage());
                                            subscriber.onError(e);
                                            return;
                                        }
                                    }
                                }))
                                .subscribeOn(Schedulers.io())
                                .compose(GameDownloadPresenter.this.<GameViewModel>bindUntilEvent(PresenterEvent.DESTROY))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<GameViewModel>() {
                                    @Override
                                    public void call(GameViewModel gameModel) {
                                        if (gameModel.isValid()) {
                                            MyLog.d(TAG, "call: onEvent MSG_BOTTOM_SHOE_GAME_ICON");
                                            mGameModel = gameModel;
                                            mView.inflate();
                                            mComponentController.onEvent(ComponentController.MSG_SHOE_GAME_ICON);
                                        }
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        MyLog.e(TAG, throwable);
                                    }
                                });
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    private void showGameDownloadView() {
        mView.showGameDownloadView();
    }

    private void hideGameDownloadView() {
        mView.hideGameDownloadView();
    }

    @Override
    public GameViewModel getGameModel() {
        return mGameModel;
    }

    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction: view is null, source=" + source);
                return false;
            }
            switch (source) {
                case ComponentController.MSG_ON_LIVE_SUCCESS:
                    if (!Constants.isGooglePlayBuild && !Constants.isIndiaBuild) {
                        int liveType = mMyRoomData.getLiveType();
                        MyLog.d(TAG, "live type=" + liveType);
                        if (liveType == LiveManager.TYPE_LIVE_GAME) {
                            getGameInfo();
                        }
                    }
                    break;
                case ComponentController.MSG_SHOW_GAME_DOWNLOAD:
                    showGameDownloadView();
                    break;
                case ComponentController.MSG_ON_BACK_PRESSED:
                    hideGameDownloadView();
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
