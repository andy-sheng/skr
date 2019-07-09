package com.module.playways.songmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.songmanager.fragment.OwnerManageFragment;

public class OwnerManagerActivity extends BaseActivity {
    public static void open(FragmentActivity activity, SongManageData roomData) {
        Intent intent = new Intent(activity, OwnerManagerActivity.class);
        intent.putExtra("room_data", roomData);
        if (activity != null) {
            activity.startActivity(intent);
        }
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        SongManageData mRoomData = (SongManageData) getIntent().getSerializableExtra("room_data");
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, OwnerManageFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .setEnterAnim(R.anim.slide_right_in)
                .setExitAnim(R.anim.slide_right_out)
                .addDataBeforeAdd(0, mRoomData)
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

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return true;
    }
}
