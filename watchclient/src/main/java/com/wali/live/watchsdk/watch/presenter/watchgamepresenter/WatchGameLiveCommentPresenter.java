package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.NonNull;
import android.view.View;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameLiveCommentView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_BOTTOM_POPUP_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_BOTTOM_POPUP_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

public class WatchGameLiveCommentPresenter extends ComponentPresenter<WatchGameLiveCommentView.IView>
        implements WatchGameLiveCommentView.IPresenter {
    private static final String TAG = "WatchGameLiveCommentPresenter";

    @Override
    protected final String getTAG() {
        return TAG;
    }

    public WatchGameLiveCommentPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_BOTTOM_POPUP_SHOWED);
        registerAction(MSG_BOTTOM_POPUP_HIDDEN);
        registerAction(MSG_INPUT_VIEW_SHOWED);
        registerAction(MSG_INPUT_VIEW_HIDDEN);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mView != null) {
            mView.destroy();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommentRefreshEvent event) {
        MyLog.d(TAG, "CommentRefreshEvent ");
        if (mView != null) {
            mView.onCommentRefreshEvent(event);
        }
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_INPUT_VIEW_SHOWED:
                break;
            case MSG_INPUT_VIEW_HIDDEN:
                break;
            case MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                return true;
            case MSG_BOTTOM_POPUP_SHOWED:
                mView.getRealView().setVisibility(View.INVISIBLE);
                return true;
            case MSG_BOTTOM_POPUP_HIDDEN:
                return true;
            default:
                break;
        }
        return false;
    }
}
