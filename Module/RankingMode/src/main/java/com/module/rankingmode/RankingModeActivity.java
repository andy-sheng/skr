package com.module.rankingmode;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.RouterConstants;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.rankingmode.fragment.RoomFragment;

@Route(path = RouterConstants.ACTIVITY_RANKINGMODE)
public class RankingModeActivity extends BaseActivity {

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.rankingmode_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(this, RoomFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
