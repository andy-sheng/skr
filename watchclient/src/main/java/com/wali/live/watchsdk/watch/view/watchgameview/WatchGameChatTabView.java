package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameLiveCommentPresenter;

public class WatchGameChatTabView extends RelativeLayout {

    WatchGameLiveCommentView mWatchGameLiveCommentView;
    WatchGameLiveCommentPresenter mWatchGameLiveCommentPresenter;

    public WatchGameChatTabView(Context context, WatchComponentController componentController) {
        super(context);
        init(context, componentController);
    }

    private void init(Context context, WatchComponentController componentController) {
        inflate(context, R.layout.watch_game_tab_chat_layout, this);
        mWatchGameLiveCommentView = (WatchGameLiveCommentView) this.findViewById(R.id.live_comment_view);
        mWatchGameLiveCommentView.setToken(componentController.mRoomChatMsgManager.toString());
        mWatchGameLiveCommentPresenter = new WatchGameLiveCommentPresenter(componentController);
        mWatchGameLiveCommentPresenter.setView(mWatchGameLiveCommentView.getViewProxy());
        mWatchGameLiveCommentView.setPresenter(mWatchGameLiveCommentPresenter);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mWatchGameLiveCommentPresenter != null) {
            mWatchGameLiveCommentPresenter.startPresenter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWatchGameLiveCommentPresenter != null) {
            mWatchGameLiveCommentPresenter.stopPresenter();
        }
    }
}
