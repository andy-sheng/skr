package com.module.rankingmode.room.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.rankingmode.R;
import com.module.rankingmode.room.fragment.RankingRoomFragment;

@Route(path = RouterConstants.ACTIVITY_RANKING_ROOM)
public class RankingRoomActivity extends BaseActivity {

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.ranking_room_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(this, RankingRoomFragment.class)
                .setAddToBackStack(false)
                .build()
        );
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return true;
    }
}
