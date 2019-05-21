package com.module.playways.grab.room.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.fragment.GrabProductionFragment;
import com.module.playways.grab.room.fragment.GrabResultFragment;
import com.module.playways.grab.room.model.WonderfulMomentModel;
import com.module.playways.room.song.model.SongModel;

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
//        for (int i = 0; i < 10; i++) {
//            SongModel songModel = new SongModel();
//            songModel.setCover("http://song-static.inframe.mobi/cover/98c7135e5df7869a7b010157e28808b5.jpg");
//            songModel.setOwner("王力宏");
//            songModel.setItemName("依然爱你");
//            String url = "http://song-static.inframe.mobi/bgm/893161bbfa5a8e33fe81d2a07cfcd39a_2.mp3";
//            WonderfulMomentModel wonderfulMomentModel = new WonderfulMomentModel(url, songModel);
//            mRoomData.getWonderfulMomentList().add(wonderfulMomentModel);
//        }
        if (mRoomData.getWonderfulMomentList() != null && mRoomData.getWonderfulMomentList().size() > 0) {
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, GrabProductionFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .addDataBeforeAdd(0, mRoomData)
                    .build());
        } else {
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, GrabResultFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .addDataBeforeAdd(0, mRoomData)
                    .build());
        }
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
