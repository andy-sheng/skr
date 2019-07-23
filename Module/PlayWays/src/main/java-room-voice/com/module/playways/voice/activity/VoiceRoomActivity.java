package com.module.playways.voice.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.PlayWaysActivity;
import com.module.playways.RoomDataUtils;
import com.module.playways.room.room.model.RankPlayerInfoModel;
import com.module.playways.room.room.model.RankRoundInfoModel;
import com.module.playways.room.room.RankRoomData;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.voice.fragment.VoiceRoomFragment;
import com.module.playways.R;

import java.util.ArrayList;
import java.util.List;

@Route(path = RouterConstants.ACTIVITY_VOICEROOM)
public class VoiceRoomActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 由准备页面未准备回退时，如果不在前台
         */
        if (!U.getActivityUtils().isAppForeground()
                // 如果应用刚回到前台500ms，也认为应用在后台。防止某些手机，比如华为Mate P20，
                // onActivityStarted 会比 onNewIntent 先调用，这里就是前台状态了
                || (System.currentTimeMillis() - U.getActivityUtils().getIsAppForegroundChangeTs() < 500)) {
            MyLog.d(getTAG(), "VoiceRoomActivity 在后台，不唤起");
            moveTaskToBack(true);
        }
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.voice_room_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        RankRoomData roomData = (RankRoomData) getIntent().getSerializableExtra("voice_room_data");
        if (roomData == null) {
            //TODO test
            roomData = new RankRoomData();
            roomData.setGameId(10001);
            {
                List<RankRoundInfoModel> roundingModeList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    RankRoundInfoModel roundingMode = new RankRoundInfoModel();
                    roundingMode.setRoundSeq(i + 1);
                    SongModel songModel = new SongModel();
                    songModel.setItemName("歌曲" + i);
                    roundingMode.setMusic(songModel);
                    roundingModeList.add(roundingMode);
                }
                List<RankPlayerInfoModel> playerInfoModelList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    RankPlayerInfoModel playerInfoModel = new RankPlayerInfoModel();
                    UserInfoModel userInfoModel = new UserInfoModel();
                    if (i == 0) {
                        userInfoModel.setAvatar(MyUserInfoManager.getInstance().getAvatar());
                        userInfoModel.setUserId((int) MyUserInfoManager.getInstance().getUid());
                        userInfoModel.setNickname("用户：" + i);
                    } else if (i == 1) {
                        userInfoModel.setAvatar(UserAccountManager.SYSTEM_AVATAR);
                        if (MyUserInfoManager.getInstance().getUid() == 1156569) {
                            userInfoModel.setUserId(1738030);
                        } else if (MyUserInfoManager.getInstance().getUid() == 1738030) {
                            userInfoModel.setUserId(1156569);
                        } else {
                            userInfoModel.setUserId(1 + i * 2);
                        }
                        userInfoModel.setNickname("用户：" + i);
                    } else {
                        userInfoModel.setAvatar(UserAccountManager.SYSTEM_AVATAR);
                        userInfoModel.setUserId(1 + i * 2);
                        userInfoModel.setNickname("用户：" + i);
                    }
                    playerInfoModel.setUserInfo(userInfoModel);
                    playerInfoModelList.add(playerInfoModel);
                }
                roomData.setPlayerInfoList(playerInfoModelList);
                roomData.setRoundInfoModelList(roundingModeList);
                roomData.setExpectRoundInfo(RoomDataUtils.findFirstRoundInfo(roomData.getRoundInfoModelList()));
            }
        }
        U.getStatusBarUtil().setTransparentBar(this, false);
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, VoiceRoomFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, roomData)
                .build()
        );
    }

    @Override
    public boolean onBackPressedForActivity() {
        for(Activity activity : U.getActivityUtils().getActivityList()){
            if(activity instanceof PlayWaysActivity){
                activity.finish();
                break;
            }
        }
        return super.onBackPressedForActivity();
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
