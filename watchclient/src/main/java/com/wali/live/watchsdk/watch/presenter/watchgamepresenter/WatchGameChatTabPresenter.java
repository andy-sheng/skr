package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.CallSuper;

import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameChatTabView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameChatTabPresenter extends BaseSdkRxPresenter<WatchGameChatTabView.IView>
        implements WatchGameChatTabView.IPresenter {
    private static final String TAG = "WatchGameBottomEditPresenter";

    public WatchGameChatTabPresenter(IEventController controller) {
        super(controller);
    }

    @Override
    protected String getTAG() {
        return null;
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
                if (mView != null) {
                    mView.updateViewerNum(event.source.getViewerCnt());
                }
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
