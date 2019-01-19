package com.module.playways.grab.room.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.rank.prepare.model.PrepareData;

import com.module.playways.RoomData;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.playways.RoomDataUtils;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.module.playways.grab.room.fragment.GrabRoomFragment;

import java.util.ArrayList;
import java.util.List;

@Route(path = RouterConstants.ACTIVITY_GRAB_ROOM)
public class GrabRoomActivity extends BaseActivity {

    /**
     * 存起该房间一些状态信息
     */
    RoomData mRoomData = new RoomData();

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.grab_room_activity_layout;
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
        } else {

        }

        //TODO test
        {
            List<RoundInfoModel> roundingModeList = new ArrayList<>();
            for(int i=0;i<10;i++){
                RoundInfoModel roundingMode = new RoundInfoModel(RoundInfoModel.TYPE_GRAB);
                roundingMode.setRoundSeq(i+1);
                SongModel songModel = new SongModel();
                songModel.setItemName("歌曲"+i);
                roundingMode.setSongModel(songModel);
                roundingModeList.add(roundingMode);
            }

            mRoomData.setRoundInfoModelList(roundingModeList);
            mRoomData.setExpectRoundInfo(RoomDataUtils.findFirstRoundInfo(mRoomData.getRoundInfoModelList()));
        }

        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, GrabRoomFragment.class)
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
