package com.module.playways.grab.room.guide.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.constans.GrabRoomType;
import com.module.RouterConstants;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.guide.fragment.GrabGuideRoomFragment;
import com.module.playways.grab.room.guide.model.GrabGuideInfoModel;
import com.module.playways.grab.room.model.GrabConfigModel;

@Route(path = RouterConstants.ACTIVITY_GRAB_GUIDE)
public class GrabGuideActivity extends BaseActivity {

    /**
     * 存起该房间一些状态信息
     */
    GrabRoomData mRoomData = new GrabRoomData();

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.grab_guide_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        GrabGuideInfoModel grabGuideInfoModel = (GrabGuideInfoModel) getIntent().getSerializableExtra("guide_data");
        mRoomData.setRoomType(GrabRoomType.ROOM_TYPE_GUIDE);
        mRoomData.setGrabGuideInfoModel(grabGuideInfoModel);

        mRoomData.setGameId((int) MyUserInfoManager.getInstance().getUid());
        mRoomData.setCoin(10);

        GrabConfigModel grabConfigModel = new GrabConfigModel();
        grabConfigModel.setTotalGameRoundSeq(2);
        mRoomData.setGrabConfigModel(grabConfigModel);

        mRoomData.setExpectRoundInfo(grabGuideInfoModel.createARoundInfo());

        if (mRoomData.getGameStartTs() == 0) {
            mRoomData.setGameStartTs(System.currentTimeMillis());
        }
        if (mRoomData.getGameCreateTs() == 0) {
            mRoomData.setGameCreateTs(System.currentTimeMillis());
        }
        mRoomData.setHasGameBegin(true);
        mRoomData.setIsGameFinish(false);
        mRoomData.setHasExitGame(false);

        mRoomData.setChallengeAvailable(false);
        mRoomData.setRoomName("新手引导房间");
        go();
        U.getStatusBarUtil().setTransparentBar(this, false);
    }

    void go() {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, GrabGuideRoomFragment.class)
                        .setAddToBackStack(false)
                        .addDataBeforeAdd(0, mRoomData)
                        .build());
        // 销毁其他的一唱到底页面
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (activity == this) {
                continue;
            }
            if (U.getActivityUtils().isHomeActivity(activity)) {
                continue;
            }
            activity.finish();
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                StatConstants.KEY_GAME_START, null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void destroy() {
        if (getWindow() != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
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
