package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.CallSuper;

import com.base.log.MyLog;
import com.mi.live.data.gamecenter.GameCenterDataManager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameHomeTabView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameHomeTabPresenter extends BaseSdkRxPresenter<WatchGameHomeTabView.IView>
        implements WatchGameHomeTabView.IPresenter {
    private static final String TAG = "WatchGameBottomEditPresenter";

    Subscription mGetGameInfoSubscription;

    public WatchGameHomeTabPresenter(IEventController controller) {
        super(controller);
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }

    @CallSuper
    public void startPresenter() {
        super.startPresenter();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (mGetGameInfoSubscription != null && !mGetGameInfoSubscription.isUnsubscribed()) {
            mGetGameInfoSubscription.unsubscribe();
        }
        mGetGameInfoSubscription = Observable.create(new Observable.OnSubscribe<GameInfoModel>() {
            @Override
            public void call(Subscriber<? super GameInfoModel> subscriber) {
                GameInfoModel gameInfoModel = GameCenterDataManager.getGameInfo(10);
                subscriber.onNext(gameInfoModel);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .compose(this.<GameInfoModel>bindUntilEvent(BaseSdkRxPresenter.PresenterEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GameInfoModel>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, e);
                    }

                    @Override
                    public void onNext(GameInfoModel gameInfoModel) {
                        if (gameInfoModel != null) {
                            if (mView != null) {
                                mView.updateUi(gameInfoModel);
                            }
                        }
                    }
                });
    }

    @Override
    @CallSuper
    public void stopPresenter() {
        super.stopPresenter();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE: {
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_TICKET: {
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWER_COUNT: {
            }
            break;
            case RoomDataChangeEvent.TYPE_CHANGE_VIEWERS: {
            }
            break;
            default:
                break;
        }
    }

}
