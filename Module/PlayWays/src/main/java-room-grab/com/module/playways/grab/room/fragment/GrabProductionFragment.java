package com.module.playways.grab.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.module.playways.R;

public class GrabProductionFragment extends BaseFragment {
    @Override
    public int initView() {
        return R.layout.grab_production_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
