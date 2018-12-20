package com.module.rankingmode.room.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.room.fragment.RankingRoomFragment;
import com.module.rankingmode.room.model.RoomData;
import com.module.rankingmode.room.model.RoomDataUtils;

@Route(path = RouterConstants.ACTIVITY_RANKING_ROOM)
public class RankingRoomActivity extends BaseActivity {

    /**
     * 存起该房间一些状态信息
     */
    RoomData mRoomData = new RoomData();

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.ranking_room_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        PrepareData prepareData = (PrepareData) getIntent().getSerializableExtra("prepare_data");
        if (prepareData != null) {
            mRoomData.setGameId(prepareData.getGameId());
            mRoomData.setGameCreateTs(prepareData.getGameCreatMs());
            mRoomData.setGameStartTs(prepareData.getGameReadyInfo().getJsonGameStartInfo().getStartTimeMs());
            mRoomData.setShiftTs(prepareData.getShiftTs());


            mRoomData.setRoundInfoModelList(prepareData.getGameReadyInfo().getJsonRoundInfo());
            mRoomData.setExpectRoundInfo(RoomDataUtils.findFirstRoundInfo(mRoomData.getRoundInfoModelList()));
            MyLog.d(TAG, "" + prepareData.getPlayerInfoList());
            mRoomData.setPlayerInfoList(prepareData.getPlayerInfoList());
            mRoomData.setSongModel(RoomDataUtils.getPlayerInfoUserId(mRoomData.getPlayerInfoList(), MyUserInfoManager.getInstance().getUid()));
        } else {

        }
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, RankingRoomFragment.class)
                .setAddToBackStack(false)
                .addDataBeforeAdd(0, mRoomData)
                .build()
        );
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
