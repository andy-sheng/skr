package com.module.rankingmode.room.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.sence.FastMatchSuccessSence;
import com.module.rankingmode.room.fragment.RankingRoomFragment;
import com.module.rankingmode.room.model.RoomData;

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
        if(prepareData!=null) {
            mRoomData.setGameId(prepareData.getGameId());
            mRoomData.setCreateTs(prepareData.getGameCreatMs());
        }

        U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(this, RankingRoomFragment.class)
                .setAddToBackStack(false)
                .setDataBeforeAdd(0,mRoomData)
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
