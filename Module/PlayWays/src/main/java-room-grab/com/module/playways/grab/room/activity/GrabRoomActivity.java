package com.module.playways.grab.room.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.constans.GrabRoomType;
import com.component.busilib.friends.SpecialModel;
import com.module.RouterConstants;
import com.module.playways.R;
//import com.module.playways.battle.songlist.BattleListActivity;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.GrabConfigModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.ui.GrabRoomFragment;
import com.module.playways.room.data.H;
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.GrabRoom.EQRoundStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSON;

@Route(path = RouterConstants.ACTIVITY_GRAB_ROOM)
public class GrabRoomActivity extends BaseActivity {

    /**
     * 存起该房间一些状态信息
     */
    GrabRoomData mRoomData = new GrabRoomData();

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.grab_room_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        H.INSTANCE.setType(GameModeType.GAME_MODE_GRAB, "GrabRoomActivity");
        H.INSTANCE.setGrabRoomData(mRoomData);
        JoinGrabRoomRspModel rsp = (JoinGrabRoomRspModel) getIntent().getSerializableExtra("prepare_data");
        Boolean isNewUser = getIntent().getBooleanExtra("is_new_user", false);
        SpecialModel specialModel = (SpecialModel) getIntent().getSerializableExtra("special_model");
        if (rsp != null) {
            mRoomData.loadFromRsp(rsp);
            mRoomData.setSpecialModel(specialModel);
            mRoomData.setNewUser(isNewUser);
            go();
        } else {
            int roomID = getIntent().getIntExtra("roomID", 0);
            if (roomID > 0) {
                GrabRoomServerApi roomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
                HashMap<String, Object> map = new HashMap<>();
                map.put("roomID", roomID);
                RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map));
                ApiMethods.subscribe(roomServerApi.joinGrabRoom(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            JoinGrabRoomRspModel grabCurGameStateModel = JSON.parseObject(result.getData().toString(), JoinGrabRoomRspModel.class);
                            mRoomData.loadFromRsp(grabCurGameStateModel);
                            mRoomData.setNewUser(isNewUser);
                            go();
                        } else {
                            U.getToastUtil().showShort(result.getErrmsg());
                            GrabRoomActivity.this.finish();
                        }
                    }

                    @Override
                    public void onNetworkError(ErrorType errorType) {
                        super.onNetworkError(errorType);
                        GrabRoomActivity.this.finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        GrabRoomActivity.this.finish();
                    }
                });
            } else {
                //TODO test
                GrabRoundInfoModel grabRoundInfoModel = new GrabRoundInfoModel();
                List<GrabPlayerInfoModel> playerInfoModelList = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    GrabPlayerInfoModel playerInfoModel = new GrabPlayerInfoModel();
                    UserInfoModel userInfoModel = new UserInfoModel();
                    if (i == 0) {
                        userInfoModel.setAvatar(MyUserInfoManager.INSTANCE.getAvatar());
                        userInfoModel.setUserId((int) MyUserInfoManager.INSTANCE.getUid());
                        userInfoModel.setNickname("用户：" + i);
                    } else {
                        userInfoModel.setAvatar(UserAccountManager.INSTANCE.getSYSTEM_AVATAR());
                        userInfoModel.setUserId(1 + i * 2);
                        userInfoModel.setNickname("用户：" + i);
                    }

                    playerInfoModel.setUserInfo(userInfoModel);
                    playerInfoModelList.add(playerInfoModel);
                }
                grabRoundInfoModel.updatePlayUsers(playerInfoModelList);
                mRoomData.setGameId(1);
                GrabConfigModel grabConfigModel = new GrabConfigModel();
                grabConfigModel.setTotalGameRoundSeq(88);
                grabRoundInfoModel.setStatus(EQRoundStatus.QRS_INTRO.getValue());
                grabRoundInfoModel.setParticipant(false);
                grabRoundInfoModel.setElapsedTimeMs(5000);
                mRoomData.setGrabConfigModel(grabConfigModel);
                if (mRoomData.getGameStartTs() == 0) {
                    mRoomData.setGameStartTs(System.currentTimeMillis());
                }
                if (mRoomData.getGameCreateTs() == 0) {
                    mRoomData.setGameCreateTs(System.currentTimeMillis());
                }
                SongModel songModel = new SongModel();
                songModel.setItemName("歌曲22");
                grabRoundInfoModel.setMusic(songModel);
                grabRoundInfoModel.setEnterStatus(grabRoundInfoModel.getStatus());
                mRoomData.setExpectRoundInfo(grabRoundInfoModel);
            }
        }
        U.getStatusBarUtil().setTransparentBar(this, false);
    }

    void go() {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, GrabRoomFragment.class)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .addDataBeforeAdd(0, mRoomData)
                        .build());
        if (mRoomData.isVideoRoom()) {
            StatisticsAdapter.recordCountEvent("grabroom", "video", null);
        }
        // 销毁其他的除一唱到底页面所有界面
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (activity == this) {
                continue;
            }
            if (U.getActivityUtils().isHomeActivity(activity)) {
                continue;
            }
            if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_PLAYBOOK) {
//                if (activity instanceof BattleListActivity) {
//                    continue;
//                }
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void destroy() {
        if (getWindow() != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        super.destroy();
        H.INSTANCE.reset("GrabRoomActivity");
    }

    @Override
    public void finish() {
        super.finish();
        MyLog.w(getTAG(), "finish");
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean canGoPersonPage() {
        return false;
    }

    @Override
    public boolean resizeLayoutSelfWhenKeyboardShow() {
        return true;
    }
}
