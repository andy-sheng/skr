package com.module.rankingmode;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.module.RouterConstants;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.rankingmode.fragment.PkRoomFragment;
import com.module.rankingmode.song.fragment.SongSelectFragment;

@Route(path = RouterConstants.ACTIVITY_RANKINGMODE)
public class RankingModeActivity extends BaseActivity {

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.rankingmode_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        boolean selectSong = getIntent().getBooleanExtra("selectSong", false);
        if (selectSong) {
            U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(this, SongSelectFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .setFragmentDataListener(new FragmentDataListener() {
                        @Override
                        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle,Object obj) {
                            showRoomFragment();
                        }
                    })
                    .build());
        } else {
            showRoomFragment();
        }
    }

    void showRoomFragment() {
        U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(this, PkRoomFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return false;
    }
}
