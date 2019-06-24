package com.module.playways;

import android.app.Activity;
import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.userinfo.model.LocalCombineRoomConfig;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.notification.event.CombineRoomSyncInviteUserNotifyEvent;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.component.busilib.GrabJoinRoomFailEvent;
import com.component.busilib.constans.GameModeType;
import com.module.RouterConstants;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo;
import com.module.playways.event.GrabChangeRoomEvent;
import com.module.playways.grab.room.GrabGuideServerApi;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.activity.GrabRoomActivity;
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel;
import com.module.playways.room.prepare.model.PrepareData;
import com.module.playways.room.room.fragment.LeaderboardFragment;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSON;

@Route(path = RouterConstants.SERVICE_RANKINGMODE, name = "测试服务")
public class PlayWaysServiceImpl implements IPlaywaysModeService {
    public final static String TAG = "ChannelServiceImpl";

    /**
     * 主要返回的是只在 channel 自定义类型，注意在 commonservice 中增加接口，
     * 如是一个自定义view，增加自定义view需要的接口即可
     * 如果是一个实体类，可以简单的直接移动到 commonservice 相应的包下
     */
    @Override
    public Object getData(int type, Object object) {
        return null;
    }

    @Override
    public Class getLeaderboardFragmentClass() {
        return LeaderboardFragment.class;
    }

    Disposable mJoinRoomDisposable;

    @Override
    public void tryGoGrabRoom(int roomID, int inviteType) {
        GrabRoomServerApi roomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);
        map.put("inviteType", inviteType);
        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map));
        mJoinRoomDisposable = ApiMethods.subscribe(roomServerApi.joinGrabRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    JoinGrabRoomRspModel grabCurGameStateModel = JSON.parseObject(result.getData().toString(), JoinGrabRoomRspModel.class);
                    for (Activity activity : U.getActivityUtils().getActivityList()) {
                        if (activity instanceof GrabRoomActivity) {
                            MyLog.d(TAG, " 存在一唱到底主页面了，发event刷新view");
                            EventBus.getDefault().post(new GrabChangeRoomEvent(grabCurGameStateModel));
                            return;
                        }
                    }
                    U.getKeyBoardUtils().hideSoftInputKeyBoard(U.getActivityUtils().getTopActivity());
                    // 页面不存在 跳转 到一唱到底页面
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                            .withSerializable("prepare_data", grabCurGameStateModel)
                            .navigation();
                } else {
                    if (result.getErrno() == 8344135) {
                        // 房间已满
                        EventBus.getDefault().post(new GrabJoinRoomFailEvent(roomID, GrabJoinRoomFailEvent.TYPE_FULL_ROOM));
                        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                                .setImage(R.drawable.grab_room_fill_player)
                                .setText("" + result.getErrmsg())
                                .build());
                    } else if (result.getErrno() == 8344141) {
                        // 房间解散
                        EventBus.getDefault().post(new GrabJoinRoomFailEvent(roomID, GrabJoinRoomFailEvent.TYPE_DISSOLVE_ROOM));
                        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                                .setImage(R.drawable.grab_room_dissolve)
                                .setText("" + result.getErrmsg())
                                .build());
                    } else {
                        U.getToastUtil().showShort("" + result.getErrmsg());
                    }

                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
            }
        });
    }

    @Override
    public void tryGoCreateRoom() {
//        if(true)
//        {
//            new MakeGamePanelView(U.getActivityUtils().getTopActivity()).showByDialog(1);
//            return;
//        }
        if (mJoinRoomDisposable != null && !mJoinRoomDisposable.isDisposed()) {
            MyLog.d(TAG, "tryGoCreateRoom 正在进入一唱到底，cancel");
            return;
        }
        if (U.getActivityUtils().getTopActivity() instanceof GrabRoomActivity) {
            MyLog.d(TAG, "tryGoCreateRoom 顶部一唱到底房间，cancel");
            return;
        }
        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_CREATE_ROOM)
                .navigation();
    }

    @Override
    public void tryGoGrabMatch(int tagId) {
        // 拉取音乐不是关键路径，不block进入匹配
        goGrabMatch(tagId, null);
    }

    private void goGrabMatch(int tagId, List<String> musicURLs) {
        PrepareData prepareData = new PrepareData();
        prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
        prepareData.setTagId(tagId);

        if (musicURLs != null && musicURLs.size() > 0) {
            prepareData.setBgMusic(musicURLs.get(0));
        }

        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                .withSerializable("prepare_data", prepareData)
                .navigation();
    }

    @Override
    public void tryGoGrabGuide(int tagId) {
        GrabGuideServerApi grabGuideServerApi = ApiManager.getInstance().createService(GrabGuideServerApi.class);
        if (grabGuideServerApi != null) {
//            ApiMethods.subscribe(grabGuideServerApi.getGuideRes(1, (int) MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
//                @Override
//                public void process(ApiResult obj) {
//                    GrabGuideInfoModel grabGuideInfoModel = JSON.parseObject(obj.getData().toJSONString(), GrabGuideInfoModel.class);
//
//                    ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_GUIDE)
//                            .withSerializable("guide_data", grabGuideInfoModel)
//                            .withInt("tag_id", tagId)
//                            .navigation();
//                }
//            }, new ApiMethods.RequestControl("getGuideRes", ApiMethods.ControlType.CancelThis));
        }
    }

    /**
     * 新手引导匹配
     */
    @Override
    public void tryGoNewGrabMatch() {
        PrepareData prepareData = new PrepareData();
        prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
        prepareData.setTagId(0);
        prepareData.setNewUser(true);

        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                .withSerializable("prepare_data", prepareData)
                .navigation();
    }

    @Override
    public void jumpToDoubleRoom(Object o) {
        DoubleRoomData doubleRoomData = new DoubleRoomData();
        if (o instanceof CombineRoomSyncInviteUserNotifyEvent) {
            CombineRoomSyncInviteUserNotifyEvent event = (CombineRoomSyncInviteUserNotifyEvent) o;
            doubleRoomData.setGameId(event.getRoomID());
            doubleRoomData.setEnableNoLimitDuration(true);
            doubleRoomData.setPassedTimeMs(event.getPassedTimeMs());
            doubleRoomData.setConfig(event.getConfig());

            {
                HashMap<Integer, UserInfoModel> hashMap = new HashMap();
                for (UserInfoModel userInfoModel : event.getUsers()) {
                    hashMap.put(userInfoModel.getUserId(), userInfoModel);
                }
                doubleRoomData.setUserInfoListMap(hashMap);
            }

            {
                List<LocalAgoraTokenInfo> list = new ArrayList<>();
                Iterator<Map.Entry<Integer, String>> iterator = event.getTokens().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, String> entry = iterator.next();
                    list.add(new LocalAgoraTokenInfo(entry.getKey(), entry.getValue()));
                }
                doubleRoomData.setTokens(list);
            }

            doubleRoomData.setNeedMaskUserInfo(event.isNeedMaskUserInfo());
        } else if (o instanceof JSONObject) {
            JSONObject obj = (JSONObject) o;
            doubleRoomData.setGameId(obj.getIntValue("roomID"));
            doubleRoomData.setEnableNoLimitDuration(true);
            doubleRoomData.setPassedTimeMs(obj.getIntValue("passedTimeMs"));
            doubleRoomData.setConfig(JSON.parseObject(obj.getString("config"), LocalCombineRoomConfig.class));
            List<UserInfoModel> userList = JSON.parseArray(obj.getString("users"), UserInfoModel.class);

            HashMap<Integer, UserInfoModel> hashMap = new HashMap();
            for (UserInfoModel userInfoModel : userList) {
                hashMap.put(userInfoModel.getUserId(), userInfoModel);
            }
            doubleRoomData.setUserInfoListMap(hashMap);

            doubleRoomData.setTokens(JSON.parseArray(obj.getString("tokens"), LocalAgoraTokenInfo.class));
            doubleRoomData.setNeedMaskUserInfo(obj.getBooleanValue("needMaskUserInfo"));
        }

        doubleRoomData.setDoubleRoomOri(DoubleRoomData.DoubleRoomOri.GRAB_INVITE);
        ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                .withSerializable("roomData", doubleRoomData)
                .navigation();
    }

    @Override
    public void init(Context context) {

    }


}
