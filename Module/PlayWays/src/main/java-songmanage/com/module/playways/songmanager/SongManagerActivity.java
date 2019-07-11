package com.module.playways.songmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.PinyinUtils;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.songmanager.fragment.DoubleSongManageFragment;
import com.module.playways.songmanager.fragment.GrabSongManageFragment;

public class SongManagerActivity extends BaseActivity {

    public static final int TYPE_FROM_GRAB = 1;
    public static final int TYPE_FROM_DOUBLE = 2;

    public static void open(FragmentActivity activity, GrabRoomData roomData) {
        Intent intent = new Intent(activity, SongManagerActivity.class);
        intent.putExtra("from", TYPE_FROM_GRAB);
        intent.putExtra("room_data", roomData);
        if (activity != null) {
            activity.startActivity(intent);
        }
    }

    public static void open(FragmentActivity activity, DoubleRoomData roomData) {
        Intent intent = new Intent(activity, SongManagerActivity.class);
        intent.putExtra("from", TYPE_FROM_DOUBLE);
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
        int from = getIntent().getIntExtra("from", 0);
        if (from == TYPE_FROM_GRAB) {
            GrabRoomData mRoomData = (GrabRoomData) getIntent().getSerializableExtra("room_data");
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, GrabSongManageFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .setEnterAnim(R.anim.slide_right_in)
                    .setExitAnim(R.anim.slide_right_out)
                    .addDataBeforeAdd(0, mRoomData)
                    .build());
        } else if (from == TYPE_FROM_DOUBLE) {
            DoubleRoomData mRoomData = (DoubleRoomData) getIntent().getSerializableExtra("room_data");
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, DoubleSongManageFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .setEnterAnim(R.anim.slide_right_in)
                    .setExitAnim(R.anim.slide_right_out)
                    .addDataBeforeAdd(0, mRoomData)
                    .build());
        }
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
