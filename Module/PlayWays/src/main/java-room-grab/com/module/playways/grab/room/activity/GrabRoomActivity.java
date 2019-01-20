package com.module.playways.grab.room.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
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
            for (int i = 0; i < prepareData.getSongModelList().size(); i++) {
                SongModel songModel = prepareData.getSongModelList().get(i);
                RoundInfoModel roundInfoModel = mRoomData.getRoundInfoModelList().get(i);
                roundInfoModel.setSongModel(songModel);
            }
            mRoomData.setExpectRoundInfo(RoomDataUtils.findFirstRoundInfo(mRoomData.getRoundInfoModelList()));
            MyLog.d(TAG, "" + prepareData.getPlayerInfoList());
            mRoomData.setPlayerInfoList(prepareData.getPlayerInfoList());
        } else {
            //TODO test
            {
                List<RoundInfoModel> roundingModeList = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    RoundInfoModel roundingMode = new RoundInfoModel(RoundInfoModel.TYPE_GRAB);
                    roundingMode.setRoundSeq(i + 1);
                    SongModel songModel = new SongModel();
                    songModel.setItemName("歌曲" + i);
                    roundingMode.setSongModel(songModel);
                    roundingModeList.add(roundingMode);
                }
                List<PlayerInfoModel> playerInfoModelList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    PlayerInfoModel playerInfoModel = new PlayerInfoModel();
                    UserInfoModel userInfoModel = new UserInfoModel();
                    if(i==0){
                        userInfoModel.setAvatar(MyUserInfoManager.getInstance().getAvatar());
                        userInfoModel.setUserId((int) MyUserInfoManager.getInstance().getUid());
                        userInfoModel.setNickname("用户：" + i);
                    }else{
                        userInfoModel.setAvatar("http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/common/system_default.png");
                        userInfoModel.setUserId(1 + i * 2);
                        userInfoModel.setNickname("用户：" + i);
                    }

                    playerInfoModel.setUserInfo(userInfoModel);
                    playerInfoModelList.add(playerInfoModel);
                }
                mRoomData.setPlayerInfoList(playerInfoModelList);
                mRoomData.setRoundInfoModelList(roundingModeList);
                mRoomData.setExpectRoundInfo(RoomDataUtils.findFirstRoundInfo(mRoomData.getRoundInfoModelList()));
            }
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
