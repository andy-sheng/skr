package com.wali.live.modulewatch.watch.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.wali.live.modulewatch.R;
import com.wali.live.modulewatch.watch.normal.WatchSdkView;

public class WatchNormalFragment extends BaseWatchFragment {

    protected WatchSdkView mSdkView;

    @Override
    public int initView() {
        return R.layout.fragment_watch_normal_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }
}
