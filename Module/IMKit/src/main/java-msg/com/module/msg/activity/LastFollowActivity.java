package com.module.msg.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.msg.follow.LastFollowFragment;

import io.rong.imkit.R;

@Route(path = RouterConstants.ACTIVITY_LAST_FOLLOW)
public class LastFollowActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.empty_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, LastFollowFragment.class)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return true;
    }
}
