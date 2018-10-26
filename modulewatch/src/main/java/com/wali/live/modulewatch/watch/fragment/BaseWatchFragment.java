package com.wali.live.modulewatch.watch.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.common.image.fresco.BaseImageView;
import com.wali.live.modulewatch.watch.normal.WatchComponentController;
import com.wali.live.modulewatch.watch.assist.WatchSdkActivityInterface;

public class BaseWatchFragment extends BaseFragment {

    // 高斯蒙层
    protected BaseImageView mMaskIv;

    protected WatchComponentController mController;

    @Override
    public int initView() {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public boolean useEventBus() { return false; }

    public WatchSdkActivityInterface getWatchSdkInterface() {
        return (WatchSdkActivityInterface) getActivity();
    }
}
