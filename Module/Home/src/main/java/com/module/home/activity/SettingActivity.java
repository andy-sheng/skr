package com.module.home.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.fragment.SettingFragment;

@Route(path = RouterConstants.ACTIVITY_SETTING)
public class SettingActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this,SettingFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build()
        );
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
