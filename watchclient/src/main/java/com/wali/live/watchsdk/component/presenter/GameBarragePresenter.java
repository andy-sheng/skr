package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.base.log.MyLog;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.view.GameBarrageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by yangli on 2017/03/02.
 *
 * @module 游戏直播弹幕表现, 观看
 */
public class GameBarragePresenter extends ComponentPresenter<GameBarrageView.IView>
        implements GameBarrageView.IPresenter {
    private static final String TAG = "GameBarragePresenter";

    public GameBarragePresenter(
            @NonNull IComponentController componentController) {
        super(componentController);
        startPresenter();
    }

    @Override
    public void destroy() {
        super.destroy();
        stopPresenter();
        if (mView != null) {
            mView.destroy();
        }
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(WatchComponentController.MSG_SHOW_GAME_BARRAGE);
        registerAction(WatchComponentController.MSG_HIDE_GAME_BARRAGE);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommentRefreshEvent event) {
        MyLog.d(TAG, "CommentRefreshEvent ");
        if (mView != null) {
            mView.onCommentRefreshEvent(event);
        }
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case WatchComponentController.MSG_SHOW_GAME_BARRAGE:
                    mView.getRealView().setVisibility(View.VISIBLE);
                    return true;
                case WatchComponentController.MSG_HIDE_GAME_BARRAGE:
                    mView.getRealView().setVisibility(View.GONE);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
