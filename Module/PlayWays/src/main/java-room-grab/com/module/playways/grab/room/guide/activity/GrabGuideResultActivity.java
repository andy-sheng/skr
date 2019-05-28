package com.module.playways.grab.room.guide.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.guide.fragment.GrabGuideResultFragment;

@Route(path = RouterConstants.ACTIVITY_GRAB_GUIDE_RESULT)
public class GrabGuideResultActivity extends BaseActivity {
    /**
     * 存起该房间一些状态信息
     */
    GrabRoomData mRoomData = new GrabRoomData();

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.empty_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRoomData = (GrabRoomData) getIntent().getSerializableExtra("room_data");
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, GrabGuideResultFragment.class)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
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
}
