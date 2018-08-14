package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameTabView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_BOTTOM_POPUP_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_BOTTOM_POPUP_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameTabPresenter extends ComponentPresenter<WatchGameTabView.IView>
        implements WatchGameTabView.IPresenter {

    public final static String TAG = "WatchGameTabPresenter";

    public WatchGameTabPresenter(IEventController controller) {
        super(controller);
    }

    @Override
    protected String getTAG() {
        return TAG;
    }


    @Override
    public void startPresenter() {
        super.startPresenter();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
            EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onEvent(int i, IParams iParams) {
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_GAME_INFO: {
                if (mView != null) {
                    mView.updateGameHomePage(event.source);
                }
            }
            break;
            default:
                break;
        }
    }
}
