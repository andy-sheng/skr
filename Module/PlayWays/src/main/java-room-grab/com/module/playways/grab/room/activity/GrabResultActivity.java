package com.module.playways.grab.room.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.fragment.GrabResultFragment;
import com.module.playways.grab.room.fragment.GrabRoomFragment;
import com.module.playways.grab.room.model.GrabConfigModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.prepare.model.JoinGrabRoomRspModel;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

import java.util.ArrayList;
import java.util.List;

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
