package com.wali.live.modulewatch.watch.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.wali.live.modulewatch.R;
import com.wali.live.modulewatch.watch.game.view.WatchGameView;

public class WatchGameFragment extends BaseWatchFragment {

    private WatchGameView mWatchGameView;

    @Override
    public int initView() {
        return R.layout.fragment_watch_game_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mWatchGameView = new WatchGameView(getActivity(), (ViewGroup) mRootView, mController);
        mWatchGameView.setupView(getWatchSdkInterface().isDisplayLandscape());
        mWatchGameView.startView();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mWatchGameView != null) {
            mWatchGameView.stopView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mWatchGameView != null) {
            mWatchGameView.onResume();
        }
    }
}
