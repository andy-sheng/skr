package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.NonNull;
import android.view.View;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.watchsdk.component.presenter.GameBarragePresenter;

import static com.wali.live.component.BaseSdkController.MSG_HIDE_GAME_BARRAGE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_GAME_BARRAGE;

/**
 * Created by zhujianning on 18-8-14.
 */

public class GameNewBarrageViewPresenter extends GameBarragePresenter {
    private static final String TAG = "GameNewBarrageViewPresenter";
    private boolean mIsLandscape;
    private boolean mNeedForceHideBarrage;

    public GameNewBarrageViewPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        if(mView != null) {
            mView.destroy();
        }
    }

    @Override
    protected void addCommentEvent(CommentRefreshEvent event) {
        if(mIsLandscape
                && mView != null) {
            mView.onCommentRefreshEvent(event);
        }
    }

    @Override
    protected void optEvent(int id) {
        switch (id) {
            case MSG_SHOW_GAME_BARRAGE:
                mNeedForceHideBarrage = false;
                if(mIsLandscape) {
                    mView.getRealView().setVisibility(View.VISIBLE);
                }
                break;
            case MSG_HIDE_GAME_BARRAGE:
                mNeedForceHideBarrage = true;
                mView.getRealView().setVisibility(View.GONE);
                break;
            case MSG_ON_ORIENT_PORTRAIT:
                mIsLandscape = false;
                mView.getRealView().setVisibility(View.GONE);
                break;
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
                if(!mNeedForceHideBarrage) {
                    mView.getRealView().setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
