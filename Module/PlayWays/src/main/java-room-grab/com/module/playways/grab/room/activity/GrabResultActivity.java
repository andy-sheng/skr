package com.module.playways.grab.room.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.fragment.GrabResultFragment;

@Route(path = RouterConstants.ACTIVITY_GRAB_RESULT)
public class GrabResultActivity extends BaseActivity {

    /**
     * 存起该房间一些状态信息
     */
    GrabRoomData mRoomData = new GrabRoomData();

    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRoomData = (GrabRoomData) getIntent().getSerializableExtra("room_data");
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, GrabResultFragment.class)
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void destroy() {
        super.destroy();
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
