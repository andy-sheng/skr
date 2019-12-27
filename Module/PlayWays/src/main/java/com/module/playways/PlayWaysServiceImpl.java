package com.module.playways;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.notification.event.CNRelayEnterFromOuterInviteNotifyEvent;
import com.common.notification.event.CRSyncInviteUserNotifyEvent;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.event.GrabJoinRoomFailEvent;
import com.component.busilib.recommend.RA;
import com.component.busilib.verify.SkrVerifyUtils;
import com.component.toast.CommonToastView;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.module.playways.doubleplay.DoublePlayActivity;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.doubleplay.DoubleRoomServerApi;
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo;
import com.module.playways.doubleplay.pbLocalModel.LocalEnterRoomModel;
import com.module.playways.doubleplay.pbLocalModel.LocalGameSenceDataModel;
import com.module.playways.event.GrabChangeRoomEvent;
import com.module.playways.friendroom.FriendRoomGameView;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.activity.GrabMatchActivity;
import com.module.playways.grab.room.activity.GrabRoomActivity;
import com.module.playways.mic.match.model.JoinMicRoomRspModel;
import com.module.playways.mic.room.MicRoomActivity;
import com.module.playways.mic.room.MicRoomServerApi;
import com.module.playways.mic.room.event.MicChangeRoomEvent;
import com.module.playways.party.home.PartyRoomView;
import com.module.playways.party.match.model.JoinPartyRoomRspModel;
import com.module.playways.party.room.PartyRoomActivity;
import com.module.playways.party.room.PartyRoomServerApi;
import com.module.playways.party.room.event.PartyChangeRoomEvent;
import com.module.playways.relay.match.model.JoinRelayRoomRspModel;
import com.module.playways.relay.room.RelayRoomActivity;
import com.module.playways.relay.room.RelayRoomData;
import com.module.playways.relay.room.RelayRoomServerApi;
import com.module.playways.room.gift.event.ShowHalfRechargeFragmentEvent;
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel;
import com.module.playways.room.prepare.model.PrepareData;
import com.module.playways.room.room.fragment.LeaderboardFragment;
import com.zq.live.proto.MicRoom.EJoinRoomSrc;

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
    public final String TAG = "ChannelServiceImpl";

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

    SkrAudioPermission skrAudioPermission = new SkrAudioPermission();

    SkrVerifyUtils skrVerifyUtils = new SkrVerifyUtils();

    HandlerTaskTimer checkTimer;

    TipsDialogView tipsDialogView;

    @Override
    public void tryGoGrabRoom(int roomID, int inviteType) {
        GrabRoomServerApi roomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);
        map.put("inviteType", inviteType);
        map.put("vars", RA.getVars());
        map.put("testList", RA.getTestList());
        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map));
        mJoinRoomDisposable = ApiMethods.subscribe(roomServerApi.joinGrabRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    JoinGrabRoomRspModel grabCurGameStateModel = JSON.parseObject(result.getData().toString(), JoinGrabRoomRspModel.class);
                    for (Activity activity : U.getActivityUtils().getActivityList()) {
                        if (activity instanceof GrabRoomActivity) {
                            // 如果 视频房 被邀请进 非视频房
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

    /**
     * 新手引导匹配
     */
    @Override
    public void tryGoNewGrabMatch(Activity activity) {
        PrepareData prepareData = new PrepareData();
        prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
        prepareData.setTagId(0);
        prepareData.setNewUser(true);
        GrabMatchActivity.open(activity, prepareData);
    }

    @Override
    public void jumpToDoubleRoom(Object o) {
        DoubleRoomData doubleRoomData = new DoubleRoomData();
        if (o instanceof CRSyncInviteUserNotifyEvent) {
            CRSyncInviteUserNotifyEvent event = (CRSyncInviteUserNotifyEvent) o;
            LocalEnterRoomModel localEnterRoomModel = new LocalEnterRoomModel(event.getBasePushInfo(), event.getCombineRoomEnterMsg());
            doubleRoomData.setGameId(localEnterRoomModel.getRoomID());
            doubleRoomData.setEnableNoLimitDuration(true);
            doubleRoomData.setPassedTimeMs(localEnterRoomModel.getPassedTimeMs());
            doubleRoomData.setConfig(localEnterRoomModel.getConfig());
            doubleRoomData.setLocalGamePanelInfo(localEnterRoomModel.getGamePanelInfo());
            doubleRoomData.setSceneType(localEnterRoomModel.getCurrentSceneType());
            doubleRoomData.setGameSenceDataModel(new LocalGameSenceDataModel(localEnterRoomModel.getGamePanelInfo().getPanelSeq()));

            {
                HashMap<Integer, UserInfoModel> hashMap = new HashMap();
                for (UserInfoModel userInfoModel : localEnterRoomModel.getUsers()) {
                    hashMap.put(userInfoModel.getUserId(), userInfoModel);
                }
                doubleRoomData.setUserInfoListMap(hashMap);
            }

            {
                List<LocalAgoraTokenInfo> list = new ArrayList<>();
                Iterator<Map.Entry<Integer, String>> iterator = localEnterRoomModel.getTokens().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, String> entry = iterator.next();
                    list.add(new LocalAgoraTokenInfo(entry.getKey(), entry.getValue()));
                }
                doubleRoomData.setTokens(list);
            }

            doubleRoomData.setNeedMaskUserInfo(localEnterRoomModel.isNeedMaskUserInfo());
            doubleRoomData.setInviterId(event.getInviterId());
        } else if (o instanceof JSONObject) {
            JSONObject obj = (JSONObject) o;
            doubleRoomData = DoubleRoomData.Companion.makeRoomDataFromJsonObject(obj);
        }

        doubleRoomData.setDoubleRoomOri(DoubleRoomData.DoubleRoomOri.GRAB_INVITE);
        ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                .withSerializable("roomData", doubleRoomData)
                .navigation();
    }

    @Override
    public void jumpToDoubleRoomFromDoubleRoomInvite(Object o) {
        //这个是这个人已经开了一个房间，但还没邀请别人进来的时候还是回收到邀请的push，需要finish掉
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (activity instanceof DoublePlayActivity) {
                activity.finish();
            }
        }

        DoubleRoomData doubleRoomData = DoubleRoomData.Companion.makeRoomDataFromJsonObject((JSONObject) o);
        doubleRoomData.setDoubleRoomOri(DoubleRoomData.DoubleRoomOri.CREATE);
        ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                .withSerializable("roomData", doubleRoomData)
                .navigation();
    }

    @Override
    public void jumpMicRoomBySuggest(int roomID) {
        HashMap map = new HashMap();
        map.put("platform", 20);
        map.put("roomID", roomID);
        map.put("src", EJoinRoomSrc.JRS_SUGGEST.getValue());

        goMicRoom(map);
    }

    @Override
    public IFriendRoomView getFriendRoomView(Context context) {
        return new FriendRoomGameView(context);
    }

    @Override
    public IPartyRoomView getPartyRoomView(Context context) {
        return new PartyRoomView(context);
    }

    @Override
    public void tryGoPartyRoom(int roomID, int joinSrc, int roomType) {
        // 列表添加 JRS_LIST    = 1;  邀请添加 JRS_INVITE  = 2;
        // roomType RT_PERSONAL = 1;普通房间  RT_FAMILY = 2;家族剧场
        HashMap map = new HashMap();
        map.put("platform", 20);
        map.put("roomID", roomID);
        map.put("joinSrc", joinSrc);
        if (roomType != 0) {
            map.put("roomType", roomType);
        }
        skrAudioPermission.ensurePermission(new Runnable() {
            @Override
            public void run() {
                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                PartyRoomServerApi roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi.class);
                ApiMethods.subscribe(roomServerApi.joinRoom(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            JoinPartyRoomRspModel rsp = JSON.parseObject(result.getData().toString(), JoinPartyRoomRspModel.class);
                            if (rsp.getCurrentRound() != null) {
                                rsp.getCurrentRound().setElapsedTimeMs(rsp.getElapsedTimeMs());
                            }

                            for (Activity activity : U.getActivityUtils().getActivityList()) {
                                if (activity instanceof PartyRoomActivity) {
                                    MyLog.d(TAG, " 存在派对房页面了，发event刷新view");
                                    EventBus.getDefault().post(new PartyChangeRoomEvent(rsp));
                                    return;
                                }
                            }
                            ARouter.getInstance().build(RouterConstants.ACTIVITY_PARTY_ROOM)
                                    .withSerializable("JoinPartyRoomRspModel", rsp)
                                    .navigation();
                        } else {
                            U.getToastUtil().showShort(result.getErrmsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        U.getToastUtil().showShort("网络错误");
                    }

                    @Override
                    public void onNetworkError(ErrorType errorType) {
                        U.getToastUtil().showShort("网络延迟");
                    }
                });
            }
        }, true);
    }

    //在外面（抢唱，party，小k房里面）邀请别人一起合唱之后当被邀请的人同意之后邀请人收到push之后调用的这个
    @Override
    public void tryToRelayRoomByOuterInvite(Object o) {
        if (o instanceof CNRelayEnterFromOuterInviteNotifyEvent) {
            Intent intent = new Intent(U.app(), RelayRoomActivity.class);
            JoinRelayRoomRspModel rsp = JoinRelayRoomRspModel.Companion.parseFromPB(((CNRelayEnterFromOuterInviteNotifyEvent) o).getRelayRoomEnterMsg());
            rsp.setEnterType(RelayRoomData.EnterType.INVITE);

            intent.putExtra("JoinRelayRoomRspModel", rsp);
            U.app().startActivity(intent);
        }
    }

    @Override
    public boolean canShowRelayInvite(int peerUserID) {
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (activity instanceof RelayRoomActivity) {
                refuseJoinRelayRoom(peerUserID, 3);
                return false;
            }
        }

        return true;
    }

    @Override
    public void refuseJoinRelayRoom(int peerUserID, int refuseType) {
        HashMap map = new HashMap();
        map.put("peerUserID", peerUserID);
        //RT_ACTIVE_REFUSE = 1 : 主动拒绝 - RT_NO_RSP_REFUSE = 2 : 没响应拒绝 RT_IN_ROOM = 3 : 已经在合唱房了
        map.put("refuseType", refuseType);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        MicRoomServerApi mRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi.class);

        ApiMethods.subscribe(mRoomServerApi.relayRefuseEnter(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {

                } else {
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络错误");
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                U.getToastUtil().showShort("网络延迟");
            }
        });
    }

    // 个人中心或者个人卡片上的邀请合唱
    @Override
    public void tryInviteToRelay(int userID, boolean isFriend) {
        skrAudioPermission.ensurePermission(new Runnable() {
            @Override
            public void run() {
                RelayRoomServerApi relayRoomServerApi = ApiManager.getInstance().createService(RelayRoomServerApi.class);
                if (isFriend) {
                    HashMap map = new HashMap();
                    map.put("inviteUserID", userID);
                    RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                    ApiMethods.subscribe(relayRoomServerApi.sendRelayInvite(body), new ApiObserver<ApiResult>() {
                        @Override
                        public void process(ApiResult obj) {
                            if (obj.getErrno() == 0) {
                                startCheckLoop();
                                U.getToastUtil().showShort("邀请成功");
                            } else {
                                if (obj.getErrno() == 8343024) {
                                    EventBus.getDefault().post(new ShowHalfRechargeFragmentEvent());
                                }

                                U.getToastUtil().showShort(obj.getErrmsg());
                            }
                        }

                        @Override
                        public void onNetworkError(ErrorType errorType) {
                            super.onNetworkError(errorType);
                            U.getToastUtil().showShort("网络错误");
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            U.getToastUtil().showShort("请求错误");
                        }
                    });
                } else {
                    ApiMethods.subscribe(relayRoomServerApi.getInviteCostZS(userID), new ApiObserver<ApiResult>() {
                        @Override
                        public void process(ApiResult obj) {
                            if (obj.getErrno() == 0) {
                                int zs = obj.getData().getIntValue("zs");
                                if (tipsDialogView != null) {
                                    tipsDialogView.dismiss(false);
                                }
                                tipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                                        .setMessageTip("对方不是您的好友，要花" + zs + "钻石邀请ta一起心动合唱吗？")
                                        .setConfirmTip("邀请")
                                        .setCancelTip("取消")
                                        .setConfirmBtnClickListener(new DebounceViewClickListener() {
                                            @Override
                                            public void clickValid(View v) {
                                                tipsDialogView.dismiss(false);
                                                tryInviteToRelay(userID, true);
                                            }
                                        })
                                        .setCancelBtnClickListener(new DebounceViewClickListener() {
                                            @Override
                                            public void clickValid(View v) {
                                                tipsDialogView.dismiss();
                                            }
                                        })
                                        .build();
                                tipsDialogView.showByDialog();
                            } else {

                            }
                        }
                    });
                }
            }
        }, true);
    }

    private void startCheckLoop() {
        if (checkTimer != null) {
            checkTimer.dispose();
        }
        checkTimer = HandlerTaskTimer.newBuilder()
                .interval(3000)
                .delay(500)
                .take(3)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        getInviteState();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }

    /**
     * 发出邀请之后轮询检查对方的进房情况，因为push有可能会丢
     */
    private void getInviteState() {
        RelayRoomServerApi relayRoomServerApi = ApiManager.getInstance().createService(RelayRoomServerApi.class);
        ApiMethods.subscribe(relayRoomServerApi.getRelayInviteEnterResult(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0 && obj.getData().getBoolean("hasInvitedRoom")) {
                    JoinRelayRoomRspModel joinRelayRoomRspModel = JSON.parseObject(obj.getData().toJSONString(), JoinRelayRoomRspModel.class);
                    joinRelayRoomRspModel.setEnterType(RelayRoomData.EnterType.INVITE);

                    if (checkTimer != null) {
                        checkTimer.dispose();
                    }
                    Intent intent = new Intent(U.app(), RelayRoomActivity.class);
                    intent.putExtra("JoinRelayRoomRspModel", joinRelayRoomRspModel);
                    U.app().startActivity(intent);
                } else {
                    U.getToastUtil().showShort(obj.getErrmsg());
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                U.getToastUtil().showShort("网络错误");
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                U.getToastUtil().showShort("请求错误");
            }
        });
    }

    //房间内和房间外同意的时候都调用这个，roomID > 0 的时候是房间内邀请
    @Override
    public void acceptRelayRoomInvite(int ownerId, int roomID, long ts) {
        skrAudioPermission.ensurePermission(new Runnable() {
            @Override
            public void run() {
                HashMap map = new HashMap();
                map.put("peerUserID", ownerId);
                if (roomID > 0) {
                    map.put("roomID", roomID);
                }

                MicRoomServerApi mRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi.class);

                if (roomID > 0) {
                    RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                    ApiMethods.subscribe(mRoomServerApi.relayRoomInviteUserEnter(body), new ApiObserver<ApiResult>() {
                        @Override
                        public void process(ApiResult result) {
                            if (result.getErrno() == 0) {
                                //先跳转
                                JoinRelayRoomRspModel rsp = JSON.parseObject(result.getData().toJSONString(), JoinRelayRoomRspModel.class);
                                rsp.setEnterType(RelayRoomData.EnterType.INVITE);

                                Intent intent = new Intent(U.app(), RelayRoomActivity.class);
                                intent.putExtra("JoinRelayRoomRspModel", rsp);
                                U.app().startActivity(intent);
                            } else {
                                U.getToastUtil().showShort(result.getErrmsg());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            U.getToastUtil().showShort("网络错误");
                        }

                        @Override
                        public void onNetworkError(ErrorType errorType) {
                            U.getToastUtil().showShort("网络延迟");
                        }
                    });
                } else {
                    map.put("inviteTimeMs", ts);
                    RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

                    ApiMethods.subscribe(mRoomServerApi.relayInviteUserEnter(body), new ApiObserver<ApiResult>() {
                        @Override
                        public void process(ApiResult result) {
                            if (result.getErrno() == 0) {
                                //先跳转
                                JoinRelayRoomRspModel rsp = JSON.parseObject(result.getData().toJSONString(), JoinRelayRoomRspModel.class);
                                rsp.setEnterType(RelayRoomData.EnterType.INVITE);

                                Intent intent = new Intent(U.app(), RelayRoomActivity.class);
                                intent.putExtra("JoinRelayRoomRspModel", rsp);
                                U.app().startActivity(intent);
                            } else {
                                U.getToastUtil().showShort(result.getErrmsg());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            U.getToastUtil().showShort("网络错误");
                        }

                        @Override
                        public void onNetworkError(ErrorType errorType) {
                            U.getToastUtil().showShort("网络延迟");
                        }
                    });
                }
            }
        }, true);
    }

    private void goMicRoom(HashMap map) {
        skrVerifyUtils.checkHasMicAudioPermission(new Runnable() {
            @Override
            public void run() {
                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                MicRoomServerApi mRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi.class);
                ApiMethods.subscribe(mRoomServerApi.joinRoom2(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            //先跳转
                            JoinMicRoomRspModel rsp = JSON.parseObject(result.getData().toJSONString(), JoinMicRoomRspModel.class);
                            rsp.setRoomID(rsp.getRoomID());
                            rsp.setGameCreateTimeMs(rsp.getGameCreateTimeMs());
                            for (Activity activity : U.getActivityUtils().getActivityList()) {
                                if (activity instanceof MicRoomActivity) {
                                    MyLog.d(TAG, " 存在排麦房页面了，发event刷新view");
                                    EventBus.getDefault().post(new MicChangeRoomEvent(rsp));
                                    return;
                                }
                            }
                            ARouter.getInstance().build(RouterConstants.ACTIVITY_MIC_ROOM)
                                    .withSerializable("JoinMicRoomRspModel", rsp)
                                    .navigation();
                        } else {
                            U.getToastUtil().showShort(result.getErrmsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        U.getToastUtil().showShort("网络错误");
                    }

                    @Override
                    public void onNetworkError(ErrorType errorType) {
                        U.getToastUtil().showShort("网络延迟");
                    }
                });
            }
        });
    }

    @Override
    public void jumpMicRoom(int roomID) {
        HashMap map = new HashMap();
        map.put("platform", 20);
        map.put("roomID", roomID);
        map.put("src", EJoinRoomSrc.JRS_INVITE_ONLINE.getValue());

        goMicRoom(map);
    }

    @Override
    public void createDoubleRoom() {
        DoubleRoomServerApi mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi.class);
        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(new HashMap()));
        ApiMethods.subscribe(mDoubleRoomServerApi.createRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    DoubleRoomData doubleRoomData = DoubleRoomData.Companion.makeRoomDataFromJsonObject(result.getData());
                    doubleRoomData.setDoubleRoomOri(DoubleRoomData.DoubleRoomOri.CREATE);
                    doubleRoomData.setInviterId(MyUserInfoManager.INSTANCE.getUid());
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                            .withSerializable("roomData", doubleRoomData)
                            .navigation();
                } else {
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络错误");
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                U.getToastUtil().showShort("网络延迟");
            }
        });
    }

    @Override
    public void init(Context context) {

    }


}
