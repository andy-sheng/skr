package com.wali.live.watchsdk.watch.presenter;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameChatTabView;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameMoreTabView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_FULLSCREEN_MORE_LIVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_TOUCH_VIEW;

/**
 * Created by liuting on 18-9-12.
 */

public class WatchGameMoreTabPresenter extends BaseSdkRxPresenter<WatchGameMoreTabView.IView> implements WatchGameMoreTabView.IPresenter {
    private static final String TAG = "WatchGameMoreTabPresenter";

    private RoomBaseDataModel mMyRoomData;

    public WatchGameMoreTabPresenter(@NonNull WatchComponentController controller) {
        super(controller);
        mMyRoomData = controller.getRoomBaseDataModel();
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @CallSuper
    public void startPresenter() {
        super.startPresenter();

        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_VIDEO_TOUCH_VIEW);
        registerAction(MSG_SHOW_FULLSCREEN_MORE_LIVE_VIEW);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (mMyRoomData != null) {
            mView.updateGameInfo(mMyRoomData);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_GAME_INFO: {
                if (mView != null) {
                    mView.updateGameInfo(event.source);
                }
            }
            break;
            default:break;
        }
    }


    @Override
    @CallSuper
    public void stopPresenter() {
        super.stopPresenter();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                // 接收到切换为竖屏通知
                 mView.onOrientChange(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                // 接收到切换为横屏通知
                mView.onOrientChange(true);
                return true;
            case MSG_SHOW_FULLSCREEN_MORE_LIVE_VIEW:
                mView.onFullScreenMoreLiveClick();
                return true;
            case MSG_VIDEO_TOUCH_VIEW:
                mView.onVideoTouchViewClick();
                return true;
        }
        return false;
    }
}
