package com.module.playways.grab.room.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.constans.GameModeType;
import com.module.RouterConstants;
import com.module.playways.R;
import com.module.playways.grab.prepare.GrabMatchFragment;
import com.module.playways.grab.prepare.NewGrabMatchFragment;
import com.module.playways.room.prepare.model.PrepareData;

@Route(path = RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
public class GrabMatchActivity extends BaseActivity {
    public static void open(PrepareData prepareData) {
        Intent intent = new Intent(U.app(),GrabMatchActivity.class);
        intent.putExtra("prepare_data", prepareData);
        U.app().startActivity(intent);
    }

    /**
     * 存起该房间一些状态信息
     */
//    RoomData mRoomData = new RoomData();
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.grab_match_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        PrepareData prepareData = (PrepareData) getIntent().getSerializableExtra("prepare_data");
        if (prepareData == null) {
            MyLog.e("GrabMatchActivity", "initData prepareData is null");
            finish();
            return;
        }

        if (prepareData.getGameType() == GameModeType.GAME_MODE_CLASSIC_RANK) {
            U.getFragmentUtils().addFragment(
                    FragmentUtils.newAddParamsBuilder(this, GrabMatchFragment.class)
                            .setAddToBackStack(false)
                            .setHasAnimation(false)
                            .addDataBeforeAdd(0, prepareData)
                            .build()
            );
        } else {
            U.getFragmentUtils().addFragment(
                    FragmentUtils.newAddParamsBuilder(this, NewGrabMatchFragment.class)
                            .setAddToBackStack(false)
                            .setHasAnimation(false)
                            .addDataBeforeAdd(0, prepareData)
                            .build()
            );
        }
        U.getStatusBarUtil().setTransparentBar(this, false);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void destroy() {
        super.destroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
