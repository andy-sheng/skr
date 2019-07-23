package com.wali.live.moduletest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.common.view.ex.ExTextView;

public class BFragment extends BaseFragment {


    @Override
    public int initView() {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        setRootView(new ExTextView(getContext()));
        ((ExTextView) getRootView()).setText("BFragment");
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
