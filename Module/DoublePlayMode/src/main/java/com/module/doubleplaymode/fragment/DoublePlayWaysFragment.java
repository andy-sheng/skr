package com.module.doubleplaymode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.module.doubleplaymode.R;

public class DoublePlayWaysFragment extends BaseFragment {
    @Override
    public int initView() {
        return R.layout.double_play_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
