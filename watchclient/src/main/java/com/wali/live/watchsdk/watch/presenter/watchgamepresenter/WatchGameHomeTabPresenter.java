package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.CallSuper;

import com.base.log.MyLog;
import com.mi.live.data.gamecenter.GameCenterDataManager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;
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

    GameInfoModel mGameInfoModel;

    public WatchGameHomeTabPresenter(WatchComponentController controller) {
        super(controller);
        mGameInfoModel = controller.getRoomBaseDataModel().getGameInfoModel();
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
        if (mView != null) {
            mView.updateUi(mGameInfoModel);
        }
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
            case RoomDataChangeEvent.TYPE_CHANGE_GAME_INFO: {
                if (mView != null) {
                    mView.updateUi(mGameInfoModel);
                }
            }
            break;
            default:
                break;
        }
    }

}
