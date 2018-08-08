package com.wali.live.watchsdk.watch.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.fragment.BaseFragment;
import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameView;

public class WatchGameFragment extends BaseWatchFragment {

    private WatchGameView mWatchGameView;

    @Override
    public int getRequestCode() {
        return GlobalData.getRequestCode();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_watch_game_layout, container, false);
    }

    @Override
    protected void bindView() {
        mWatchGameView = new WatchGameView(getActivity(), (ViewGroup) mRootView, getController());
    }

    private WatchComponentController getController() {
        // TODO 讨论一下这个是用新的还是共用现有的
        return null;
    }

}
