package com.wali.live.moduletest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.RouterConstants;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.wali.live.moduletest.fragment.DeviceInfoFragment;

@Route(path = RouterConstants.ACTIVITY_DEVICE_INFO)
public class DeviceInfoActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(this, DeviceInfoFragment.class)
                .setAddToBackStack(false)
                .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return true;
    }
}
