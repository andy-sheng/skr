package com.module.home.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.fragment.DiamondBallanceFragment;
import com.module.home.fragment.InComeFragment;

@Route(path = RouterConstants.ACTIVITY_INCOME)
public class InComeActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.empty_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, InComeFragment.class)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
