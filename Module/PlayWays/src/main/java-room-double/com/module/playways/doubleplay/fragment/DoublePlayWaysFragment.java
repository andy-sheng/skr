package com.module.playways.doubleplay.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.module.playways.R;

public class DoublePlayWaysFragment extends BaseFragment {
    @Override
    public int initView() {
        return R.layout.empty_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
