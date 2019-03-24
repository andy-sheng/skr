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
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.grab.createroom.model.SpecialModel;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.GrabConfigModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.prepare.model.JoinGrabRoomRspModel;

import com.module.playways.rank.room.activity.RankRoomActivity;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.module.playways.grab.room.fragment.GrabRoomFragment;

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
        JoinGrabRoomRspModel rsp = (JoinGrabRoomRspModel) getIntent().getSerializableExtra("prepare_data");
        SpecialModel specialModel = (SpecialModel) getIntent().getSerializableExtra("special_model");
        if (rsp != null) {
            mRoomData.loadFromRsp(rsp);
            mRoomData.setSpecialModel(specialModel);
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
                        userInfoModel.setAvatar(MyUserInfoManager.getInstance().getAvatar());
                        userInfoModel.setUserId((int) MyUserInfoManager.getInstance().getUid());
                        userInfoModel.setNickname("用户：" + i);
                    } else {
                        userInfoModel.setAvatar(UserAccountManager.SYSTEM_AVATAR);
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
                grabRoundInfoModel.setStatus(GrabRoundInfoModel.STATUS_GRAB);
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
                        .addDataBeforeAdd(0, mRoomData)
                        .build());
        // 销毁其他的一唱到底页面
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (activity instanceof GrabRoomActivity) {
                if (activity != this) {
                    activity.finish();
                }
            }
            if (activity instanceof RankRoomActivity) {
                activity.finish();
            }
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
