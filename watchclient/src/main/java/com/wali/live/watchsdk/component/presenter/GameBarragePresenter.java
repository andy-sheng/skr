package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.view.View;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.watchsdk.component.view.GameBarrageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_HIDE_GAME_BARRAGE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_GAME_BARRAGE;

/**
 * Created by yangli on 2017/03/02.
 *
 * @module 游戏直播弹幕表现, 观看
 */
public class GameBarragePresenter extends ComponentPresenter<GameBarrageView.IView>
        implements GameBarrageView.IPresenter {
    private static final String TAG = "GameBarragePresenter";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public GameBarragePresenter(@NonNull IEventController controller) {
        super(controller);
        startPresenter();
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_SHOW_GAME_BARRAGE);
        registerAction(MSG_HIDE_GAME_BARRAGE);
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
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
        addCommentEvent(event);
    }

    protected void optEvent(int id) {
        switch (id) {
            case MSG_SHOW_GAME_BARRAGE:
                mView.getRealView().setVisibility(View.VISIBLE);
                break;
            case MSG_HIDE_GAME_BARRAGE:
                mView.getRealView().setVisibility(View.GONE);
                break;
        }
    }

    protected void addCommentEvent(CommentRefreshEvent event) {
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
            case MSG_SHOW_GAME_BARRAGE:
            case MSG_HIDE_GAME_BARRAGE:
                optEvent(event);
                return true;
            case MSG_ON_ORIENT_PORTRAIT:
            case MSG_ON_ORIENT_LANDSCAPE:
                optEvent(event);
                return true;
            default:
                break;
        }
        return false;
    }
}
