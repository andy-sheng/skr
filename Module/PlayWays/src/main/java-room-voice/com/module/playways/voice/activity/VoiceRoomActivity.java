package com.module.playways.voice.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.RoomData;
import com.module.playways.RoomDataUtils;
import com.module.playways.rank.prepare.model.GrabRoundInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.module.playways.voice.fragment.VoiceRoomFragment;
import com.module.rank.R;

import java.util.ArrayList;
import java.util.List;

@Route(path = RouterConstants.ACTIVITY_VOICEROOM)
public class VoiceRoomActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.voice_room_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        RoomData mRoomData = (RoomData) getIntent().getSerializableExtra("voice_room_data");
        if (mRoomData == null) {
            //TODO test
            mRoomData = new RoomData();
            mRoomData.setGameId(10001);
            {
                List<GrabRoundInfoModel> roundingModeList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    GrabRoundInfoModel roundingMode = new GrabRoundInfoModel(RoundInfoModel.TYPE_GRAB);
                    roundingMode.setRoundSeq(i + 1);
                    SongModel songModel = new SongModel();
                    songModel.setItemName("歌曲" + i);
                    roundingMode.setSongModel(songModel);
                    roundingModeList.add(roundingMode);
                }
                List<PlayerInfoModel> playerInfoModelList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    PlayerInfoModel playerInfoModel = new PlayerInfoModel();
                    UserInfoModel userInfoModel = new UserInfoModel();
                    if (i == 0) {
                        userInfoModel.setAvatar(MyUserInfoManager.getInstance().getAvatar());
                        userInfoModel.setUserId((int) MyUserInfoManager.getInstance().getUid());
                        userInfoModel.setNickname("用户：" + i);
                    } else if (i == 1) {
                        userInfoModel.setAvatar("http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/common/system_default.png");
                        if (MyUserInfoManager.getInstance().getUid() == 1156569) {
                            userInfoModel.setUserId(1738030);
                        } else if (MyUserInfoManager.getInstance().getUid() == 1738030) {
                            userInfoModel.setUserId(1156569);
                        } else {
                            userInfoModel.setUserId(1 + i * 2);
                        }
                        userInfoModel.setNickname("用户：" + i);
                    } else {
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
        U.getStatusBarUtil().setTransparentBar(this, false);
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, VoiceRoomFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
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
