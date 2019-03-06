package com.module.playways.grab.room.presenter;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.changba.songstudio.audioeffect.AudioEffectStyleEnum;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.player.VideoPlayerAdapter;
import com.common.player.exoplayer.ExoPlayer;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.utils.ActivityUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.component.busilib.SkrConfig;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.arccloud.SongInfo;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.event.GrabGameOverEvent;
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent;
import com.module.playways.grab.room.event.GrabSwitchRoomEvent;
import com.module.playways.grab.room.event.GrabWaitSeatUpdateEvent;
import com.module.playways.grab.room.inter.IGrabView;
import com.module.playways.grab.room.model.BLightInfoModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.grab.room.model.GrabSkrResourceModel;
import com.module.playways.grab.room.model.MLightInfoModel;
import com.module.playways.grab.room.model.WantSingerInfo;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.rank.msg.event.QExitGameMsgEvent;
import com.module.playways.rank.msg.event.QGetSingChanceMsgEvent;
import com.module.playways.rank.msg.event.QJoinNoticeEvent;
import com.module.playways.rank.msg.event.QLightBurstMsgEvent;
import com.module.playways.rank.msg.event.QLightOffMsgEvent;
import com.module.playways.rank.msg.event.QRoundAndGameOverMsgEvent;
import com.module.playways.rank.msg.event.QRoundOverMsgEvent;
import com.module.playways.rank.msg.event.QSyncStatusMsgEvent;
import com.module.playways.rank.msg.event.QWantSingChanceMsgEvent;
import com.module.playways.rank.msg.filter.PushMsgFilter;
import com.module.playways.rank.msg.manager.ChatRoomMsgManager;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.prepare.model.JoinGrabRoomRspModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.BaseRoundInfoModel;
import com.module.playways.rank.room.SwapStatusType;
import com.module.playways.RoomDataUtils;
import com.module.playways.rank.room.comment.CommentModel;
import com.module.playways.rank.room.event.PretendCommentMsgEvent;
import com.module.playways.rank.room.score.MachineScoreItem;
import com.module.playways.rank.room.score.RobotScoreHelper;
import com.module.rank.BuildConfig;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.EQRoundOverReason;
import com.zq.live.proto.Room.EQRoundResultType;
import com.zq.live.proto.Room.RoomMsg;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.agora.rtc.Constants;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabCorePresenter extends RxLifeCyclePresenter {
    public String TAG = "GrabCorePresenter";

    private static long sSyncStateTaskInterval = 5000;
    static final int MSG_ROBOT_SING_BEGIN = 10;

    GrabRoomData mRoomData;

    GrabRoomServerApi mRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);

    HandlerTaskTimer mSyncGameStateTask;

    IGrabView mIGrabView;

    RobotScoreHelper mRobotScoreHelper;

    boolean mDestroyed = false;

    ExoPlayer mExoPlayer;

    boolean mSwitchRooming = false;

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ROBOT_SING_BEGIN:
                    robotSingBegin();
                    break;
            }
        }
    };

    PushMsgFilter mPushMsgFilter = new PushMsgFilter() {
        @Override
        public boolean doFilter(RoomMsg msg) {
            if (msg != null && msg.getRoomID() == mRoomData.getGameId()) {
                return true;
            }
            return false;
        }
    };

    public GrabCorePresenter(@NotNull IGrabView iGrabView, @NotNull GrabRoomData roomData) {
        mIGrabView = iGrabView;
        mRoomData = roomData;
        TAG = "GrabCorePresenter";
        ChatRoomMsgManager.getInstance().addFilter(mPushMsgFilter);
        joinRoomAndInit(true);
    }

    /**
     * 加入引擎房间
     * 加入融云房间
     * 系统消息弹幕
     */
    private void joinRoomAndInit(boolean first) {
        MyLog.d(TAG, "joinRoomAndInit" + " first=" + first);
        if (mRoomData.getGameId() > 0) {
            Params params = Params.getFromPref();
            params.setStyleEnum(AudioEffectStyleEnum.ORIGINAL);
            params.setScene(Params.Scene.grab);
            EngineManager.getInstance().init("grabroom", params);
            EngineManager.getInstance().joinRoom(String.valueOf(mRoomData.getGameId()), (int) UserAccountManager.getInstance().getUuidAsLong(), false);
            // 不发送本地音频
            EngineManager.getInstance().muteLocalAudioStream(true);
        }
        if (mRoomData.getGameId() > 0) {
            ModuleServiceManager.getInstance().getMsgService().joinChatRoom(String.valueOf(mRoomData.getGameId()), new ICallback() {
                @Override
                public void onSucess(Object obj) {
                    MyLog.d(TAG, "加入融云房间成功");
                }

                @Override
                public void onFailed(Object obj, int errcode, String message) {
                }
            });
        }
        if (mRoomData.getGameId() > 0) {
            pretenSystemMsg();
            for (PlayerInfoModel playerInfoModel : mRoomData.getPlayerInfoList()) {
                if (!playerInfoModel.isOnline()) {
                    continue;
                }
                BasePushInfo basePushInfo = new BasePushInfo();
                basePushInfo.setRoomID(mRoomData.getGameId());
                basePushInfo.setSender(new UserInfo.Builder()
                        .setUserID(playerInfoModel.getUserInfo().getUserId())
                        .setAvatar(playerInfoModel.getUserInfo().getAvatar())
                        .setNickName(playerInfoModel.getUserInfo().getNickname())
                        .setSex(ESex.fromValue(playerInfoModel.getUserInfo().getSex()))
                        .build());
                String text = String.format("加入了房间");
                if (playerInfoModel.getUserInfo().getUserId() == UserAccountManager.SYSTEM_GRAB_ID) {
                    text = "我是撕歌最傲娇小助手多音，来和你们一起唱歌卖萌~";
                }
                CommentMsgEvent msgEvent = new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text);
                EventBus.getDefault().post(msgEvent);
            }
        }
        startSyncGameStateTask(sSyncStateTaskInterval);
    }

    private void pretenSystemMsg() {
        CommentModel commentModel = new CommentModel();
        commentModel.setCommentType(CommentModel.TYPE_TRICK);
        commentModel.setUserId(UserAccountManager.SYSTEM_ID);
        commentModel.setAvatar(UserAccountManager.SYSTEM_AVATAR);
        commentModel.setUserName("系统消息");
        commentModel.setAvatarColor(Color.WHITE);
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append("欢迎进入撕歌一唱到底，对局马上开始，比赛过程发现坏蛋请用力举报哦～").setForegroundColor(CommentModel.TEXT_RED)
                .create();
        commentModel.setStringBuilder(stringBuilder);
        EventBus.getDefault().post(new PretendCommentMsgEvent(commentModel));
    }

    @Override
    public void start() {
        super.start();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * 由ui层告知
     * 开场动画结束
     */
    public void onOpeningAnimationOver() {
        // 开始触发触发轮次变化
        mRoomData.checkRoundInEachMode();
    }

    /**
     * 播放导唱
     */
    public void playGuide() {
        if (mDestroyed) {
            return;
        }
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            if (mExoPlayer == null) {
                mExoPlayer = new ExoPlayer();
                if (mRoomData.isMute()) {
                    mExoPlayer.setVolume(0);
                } else {
                    mExoPlayer.setVolume(1);
                }
            }
            mExoPlayer.setCallback(new VideoPlayerAdapter.PlayerCallbackAdapter() {
                @Override
                public void onPrepared() {
                    super.onPrepared();
                    if (!now.isParticipant() && now.getEnterStatus() == GrabRoundInfoModel.STATUS_GRAB) {
                        MyLog.d(TAG, "这轮刚进来，导唱需要seek");
                        mExoPlayer.seekTo(now.getElapsedTimeMs());
                    }
                }
            });
            mExoPlayer.startPlay(now.getMusic().getStandIntro());
        }
    }

    /**
     * 停止播放导唱
     */
    public void stopGuide() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
        }
    }

    /**
     * 开始演唱
     */
    public void beginSing() {
        // 打开引擎，变为主播
        if (mRoomData.getGameId() > 0) {
            EngineManager.getInstance().setClientRole(true);
            //开始录制声音
            if (SkrConfig.getInstance().isNeedUploadAudioForAI()) {
                // 需要上传音频伪装成机器人
                EngineManager.getInstance().startAudioRecording(RoomDataUtils.getSaveAudioForAiFilePath(), Constants.AUDIO_RECORDING_QUALITY_HIGH);
                BaseRoundInfoModel now = mRoomData.getRealRoundInfo();
                if (now != null) {
                    if (mRobotScoreHelper == null) {
                        mRobotScoreHelper = new RobotScoreHelper();
                    }
                    mRobotScoreHelper.reset();
                    EngineManager.getInstance().startRecognize(RecognizeConfig.newBuilder()
                            .setSongName(now.getMusic().getItemName())
                            .setArtist(now.getMusic().getOwner())
                            .setMode(RecognizeConfig.MODE_AUTO)
                            .setAutoTimes(8)
                            .setMResultListener(new ArcRecognizeListener() {
                                @Override
                                public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo, int lineNo) {
                                    int score = 0;
                                    if (targetSongInfo != null) {
                                        score = (int) (targetSongInfo.getScore() * 100);
                                    }
                                    MachineScoreItem machineScoreItem = new MachineScoreItem();
                                    machineScoreItem.setNo(lineNo);
                                    machineScoreItem.setTs(System.currentTimeMillis() - mRobotScoreHelper.getBeginRecordTs());
                                    machineScoreItem.setScore(score);
                                    mRobotScoreHelper.add(machineScoreItem);
                                }
                            })
                            .build());
                }
            }
        }
    }

    /**
     * 抢唱歌权
     */
    public void grabThisRound(int seq) {
//        RoundInfoModel now = mRoomData.getRealRoundInfo();
        //TEST
//        if (true && MyLog.isDebugLogOpen()) {
//            U.getToastUtil().showShort("执行测试抢代码");
//            now.addGrabUid(RoomDataUtils.isCurrentRound(now.getRoundSeq(), mRoomData), (int) MyUserInfoManager.getInstance().getUid());
//            mUiHanlder.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    now.setUserID((int) MyUserInfoManager.getInstance().getUid());
//                    now.updateStatus(true, GrabRoundInfoModel.STATUS_SING);
//                }
//            }, 3000);
//
//            HandlerTaskTimer.newBuilder()
//                    .interval(4000)
//                    .start(new HandlerTaskTimer.ObserverW() {
//                        @Override
//                        public void onNext(Integer integer) {
//                            SomeOneLightOffEvent event = new SomeOneLightOffEvent(5, now);
//                            EventBus.getDefault().post(event);
//                            mUiHanlder.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    SomeOneLightOffEvent event2 = new SomeOneLightOffEvent(3, now);
//                                    EventBus.getDefault().post(event2);
//                                }
//                            },1000);
//                        }
//                    });
//            return;
//        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", seq);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.wangSingChance(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "grabThisRound erro code is " + result.getErrno() + ",traceid is " + result.getTraceId());
                if (result.getErrno() == 0) {
                    //抢成功了
                    GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
                    if (now != null && now.getRoundSeq() == seq) {
                        WantSingerInfo wantSingerInfo = new WantSingerInfo();
                        wantSingerInfo.setUserID((int) MyUserInfoManager.getInstance().getUid());
                        wantSingerInfo.setTimeMs(System.currentTimeMillis());
                        now.addGrabUid(true, wantSingerInfo);
                    } else {
                        MyLog.w(TAG, "now != null && now.getRoundSeq() == seq 条件不满足，" + result.getTraceId());
                    }
                } else {
                    MyLog.w(TAG, "grabThisRound failed, " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "grabThisRound error " + e);

            }
        }, this);
    }

    /**
     * 灭灯
     */
    public void lightsOff() {
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        int roundSeq = now.getRoundSeq();
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundSeq);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.lightOff(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.e(TAG, "lightsOff erro code is " + result.getErrno() + ",traceid is " + result.getTraceId());
                if (result.getErrno() == 0) {
                    GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
                    if (now != null && now.getRoundSeq() == roundSeq) {
                        MLightInfoModel noPassingInfo = new MLightInfoModel();
                        noPassingInfo.setUserID((int) MyUserInfoManager.getInstance().getUid());
                        now.addLightOffUid(true, noPassingInfo);
                    }
                } else {

                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "lightsOff error " + e);

            }
        }, this);
    }

    /**
     * 爆灯
     */
    public void lightsBurst() {
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        int roundSeq = now.getRoundSeq();
        map.put("roundSeq", mRoomData.getRealRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.lightBurst(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.e(TAG, "lightsBurst erro code is " + result.getErrno() + ",traceid is " + result.getTraceId());
                if (result.getErrno() == 0) {
                    GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
                    if (now != null && now.getRoundSeq() == roundSeq) {
                        int coin = result.getData().getInteger("coin");
                        mRoomData.setCoin(coin);
                        if (result.getData().getBoolean("isBLightSuccess")) {
                            BLightInfoModel m = new BLightInfoModel();
                            m.setUserID((int) MyUserInfoManager.getInstance().getUid());
                            now.addLightBurstUid(true, m);
                        } else {
                            String reason = result.getData().getString("bLightFailedMsg");
                            if (!TextUtils.isEmpty(reason)) {
                                U.getToastUtil().showShort(reason);
                            }
                        }
                    }
                } else {

                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "lightsOff error " + e);

            }
        }, this);
    }

    private void robotSingBegin() {

        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        GrabSkrResourceModel grabSkrResourceModel = grabRoundInfoModel.getSkrResource();
        String skrerUrl = null;
        if (grabSkrResourceModel != null) {
            skrerUrl = grabSkrResourceModel.getAudioURL();
        }
        if (mRobotScoreHelper == null) {
            mRobotScoreHelper = new RobotScoreHelper();
        }

        if (mExoPlayer == null) {
            mExoPlayer = new ExoPlayer();
        }
        mExoPlayer.startPlay(skrerUrl);
        mExoPlayer.setCallback(new VideoPlayerAdapter.PlayerCallbackAdapter() {
            @Override
            public void onPrepared() {
                if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
                    MyLog.d(TAG, "进来时已经时演唱阶段了，则机器人资源要seek一下 " + grabRoundInfoModel.getElapsedTimeMs());
                    mExoPlayer.seekTo(grabRoundInfoModel.getElapsedTimeMs());
                }
            }
        });
        if (!EngineManager.getInstance().getParams().isAllRemoteAudioStreamsMute()) {
            mExoPlayer.setVolume(1);
        } else {
            mExoPlayer.setVolume(0);
        }
    }

    private void tryStopRobotPlay() {
        if (mExoPlayer != null) {
            mExoPlayer.reset();
        }
    }

    public void muteAllRemoteAudioStreams(boolean mute, boolean fromUser) {
        if (fromUser) {
            mRoomData.setMute(mute);
        }
        EngineManager.getInstance().muteAllRemoteAudioStreams(mute);
        // 如果是机器人的话
        if (mute) {
            // 如果是静音
            if (mExoPlayer != null) {
                mExoPlayer.setVolume(0);
            }
        } else {
            // 如果打开静音
            if (mExoPlayer != null) {
                mExoPlayer.setVolume(1f);
            }
        }
    }

    /**
     * 自己的轮次结束了
     *
     * @param roundInfoModel
     */
    private void onSelfRoundOver(GrabRoundInfoModel roundInfoModel) {
        // 上一轮演唱是自己，开始上传资源
        if (SkrConfig.getInstance().isNeedUploadAudioForAI()) {
            //属于需要上传音频文件的状态
            // 上一轮是我的轮次，暂停录音
            if (mRoomData.getGameId() > 0) {
                EngineManager.getInstance().stopAudioRecording();
            }
            // 上传打分
            if (mRobotScoreHelper != null) {
                if (mRobotScoreHelper.isScoreEnough()) {
                    if (roundInfoModel.getOverReason() == EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue()
                            && roundInfoModel.getResultType() == EQRoundResultType.ROT_TYPE_1.getValue()) {
                        // 是一唱到底的才上传
                        roundInfoModel.setSysScore(mRobotScoreHelper.getAverageScore());
                        uploadRes1ForAi(roundInfoModel);
                    } else {
                        MyLog.d(TAG, "没有唱到一唱到底不上传");
                    }
                } else {
                    MyLog.d(TAG, "isScoreEnough false");
                }
            }
        }
    }

    /**
     * 上传音频文件用作机器人
     *
     * @param roundInfoModel
     */
    private void uploadRes1ForAi(BaseRoundInfoModel roundInfoModel) {
        if (mRobotScoreHelper != null) {
            MyLog.d(TAG, "uploadRes1ForAi 开始上传资源 得分:" + roundInfoModel.getSysScore());
            UploadParams.newBuilder(RoomDataUtils.getSaveAudioForAiFilePath())
                    .setFileType(UploadParams.FileType.audioAi)
                    .startUploadAsync(new UploadCallback() {
                        @Override
                        public void onProgress(long currentSize, long totalSize) {

                        }

                        @Override
                        public void onSuccess(String url) {
                            MyLog.d(TAG, "uploadRes1ForAi 上传成功 url=" + url);
                            sendUploadRequest(roundInfoModel, url);
                        }

                        @Override
                        public void onFailure(String msg) {

                        }
                    });
        }
    }

    /**
     * 上传机器人资源相关文件到服务器
     *
     * @param roundInfoModel
     * @param audioUrl
     */
    private void sendUploadRequest(BaseRoundInfoModel roundInfoModel, String audioUrl) {
        long timeMs = System.currentTimeMillis();
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("itemID", roundInfoModel.getPlaybookID());
        map.put("sysScore", roundInfoModel.getSysScore());
        map.put("audioURL", audioUrl);
//        map.put("midiURL", midiUrl);
        map.put("timeMs", timeMs);
        StringBuilder sb = new StringBuilder();
        sb.append("skrer")
                .append("|").append(mRoomData.getGameId())
                .append("|").append(roundInfoModel.getPlaybookID())
                .append("|").append(roundInfoModel.getSysScore())
                .append("|").append(audioUrl)
//                .append("|").append(midiUrl)
                .append("|").append(timeMs);
        String sign = U.getMD5Utils().MD5_32(sb.toString());
        map.put("sign", sign);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.saveRes(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.e(TAG, "sendAiUploadRequest success");
                } else {
                    MyLog.e(TAG, "sendAiUploadRequest failed， errno is " + result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "sendUploadRequest error " + e);
            }
        }, this);
    }

    @Override
    public void destroy() {
        MyLog.d(TAG, "destroy begin");
        super.destroy();
        mDestroyed = true;
        if (!mRoomData.isHasExitGame()) {
            exitRoom();
        }
        cancelSyncGameStateTask();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        EngineManager.getInstance().destroy("grabroom");
        mUiHanlder.removeCallbacksAndMessages(null);
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter);
        if (mExoPlayer != null) {
            mExoPlayer.release();
        } else {
            MyLog.d(TAG, "mExoPlayer == null ");
        }
        ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(String.valueOf(mRoomData.getGameId()));
        MyLog.d(TAG, "destroy over");
    }

    /**
     * 告知我的的抢唱阶段结束了
     */
    public void sendMyGrabOver() {
        MyLog.d(TAG, "上报我的抢唱结束 ");
        BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel == null) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundInfoModel.getRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.sendGrapOver(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.w(TAG, "我的抢唱结束上报成功 traceid is " + result.getTraceId());
                } else {
                    MyLog.w(TAG, "我的抢唱结束上报失败 traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "sendRoundOverInfo" + " error " + e);
            }
        }, this);
    }

    /**
     * 上报轮次结束信息
     */
    public void sendRoundOverInfo() {
        MyLog.w(TAG, "上报我的演唱结束");
        estimateOverTsThisRound();

        BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel == null || roundInfoModel.getUserID() != MyUserInfoManager.getInstance().getUid()) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundInfoModel.getRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.sendRoundOver(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.w(TAG, "演唱结束上报成功 traceid is " + result.getTraceId());
                } else {
                    MyLog.w(TAG, "演唱结束上报失败 traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "sendRoundOverInfo" + " error " + e);
            }
        }, this);
    }

    /**
     * 退出房间
     */
    public void exitRoom() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.exitRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mRoomData.setHasExitGame(true);
                    String resultStr = result.getData().getString("resultInfo");
                    if (!TextUtils.isEmpty(resultStr)) {
                        GrabResultInfoModel grabResultInfoModel = JSON.parseObject(resultStr, GrabResultInfoModel.class);
                        List<GrabResultInfoModel> l = new ArrayList<>();
                        l.add(grabResultInfoModel);
                        // 得到结果
                        mRoomData.setResultList(l);
                        mIGrabView.onGetGameResult(true);
                    } else {
                        mIGrabView.onGetGameResult(false);
                    }
                } else {
                    mIGrabView.onGetGameResult(false);
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                mIGrabView.onGetGameResult(false);
            }
        });
    }

    /**
     * 切换房间
     */
    public void switchRoom() {
        if (mSwitchRooming) {
            U.getToastUtil().showShort("切换中");
            return;
        }
        mSwitchRooming = true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("tagID", mRoomData.getTagId());
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.switchRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    EventBus.getDefault().post(new GrabSwitchRoomEvent());
                    JoinGrabRoomRspModel joinGrabRoomRspModel = JSON.parseObject(result.getData().toJSONString(), JoinGrabRoomRspModel.class);
                    mRoomData.loadFromRsp(joinGrabRoomRspModel);
                    joinRoomAndInit(false);
                    mRoomData.checkRoundInEachMode();
                    mIGrabView.onChangeRoomResult(true);
                } else {
                    U.getToastUtil().showShort("切换失败:" + result.getErrmsg());
                    mIGrabView.onChangeRoomResult(false);
                }
                mSwitchRooming = false;

            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                mSwitchRooming = false;
                mIGrabView.onChangeRoomResult(false);
            }
        }, this);
    }

    /**
     * 游戏切后台或切回来
     *
     * @param out 切出去
     * @param in  切回来
     */
    public void swapGame(boolean out, boolean in) {
        MyLog.w(TAG, "swapGame" + " out=" + out + " in=" + in);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        if (out) {
            map.put("status", SwapStatusType.SS_SWAP_OUT);
        } else if (in) {
            map.put("status", SwapStatusType.SS_SWAP_IN);
        }

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.swap(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
//                    U.getToastUtil().showShort("切换请求发送成功");
                } else {
                    MyLog.e(TAG, "swapGame result errno is " + result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "swapGame error " + e);
            }
        }, this);
    }

    /**
     * 轮询同步状态task
     */
    public void startSyncGameStateTask(long delayTime) {
        cancelSyncGameStateTask();

        if (mRoomData.isIsGameFinish()) {
            MyLog.w(TAG, "游戏结束了，还特么Sync");
            return;
        }

        mSyncGameStateTask = HandlerTaskTimer.newBuilder()
                .delay(delayTime)
                .interval(sSyncStateTaskInterval)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        MyLog.w(TAG, "4秒钟的 syncGameTask 去更新状态了");
                        syncGameStatus(mRoomData.getGameId());
                    }
                });
    }

    public void cancelSyncGameStateTask() {
        if (mSyncGameStateTask != null) {
            mSyncGameStateTask.dispose();
        }
    }

    // 同步游戏详情状态(检测不到长连接调用)
    public void syncGameStatus(int gameID) {
        ApiMethods.subscribe(mRoomServerApi.syncGameStatus(gameID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    if (gameID != mRoomData.getGameId()) {
                        MyLog.d(TAG, "syncGameStatus gameID 不一致");
                        return;
                    }
                    long syncStatusTimes = result.getData().getLong("syncStatusTimeMs");  //状态同步时的毫秒时间戳
                    long gameOverTimeMs = result.getData().getLong("gameOverTimeMs");  //游戏结束时间
                    GrabRoundInfoModel currentInfo = JSON.parseObject(result.getData().getString("currentRound"), GrabRoundInfoModel.class); //当前轮次信息
                    String msg = "";
                    if (currentInfo != null) {
                        msg = "syncGameStatus成功了, currentRound 是 " + currentInfo;
                    } else {
                        msg = "syncGameStatus成功了, currentRound 是 null";
                    }

                    msg = msg + ",traceid is " + result.getTraceId();
                    MyLog.w(TAG, msg);

                    if (currentInfo == null) {
                        onGameOver("syncGameStatus", gameOverTimeMs, null);
                        return;
                    }

                    updatePlayerState(gameOverTimeMs, syncStatusTimes, currentInfo);
                } else {
                    MyLog.w(TAG, "syncGameStatus失败 traceid is " + result.getTraceId());
                    estimateOverTsThisRound();
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "syncGameStatus失败了，errno是" + e);
            }
        }, this);
    }

    /**
     * 根据时间戳更新选手状态,目前就只有两个入口，SyncStatusEvent push了sycn，不写更多入口
     */
    private synchronized void updatePlayerState(long gameOverTimeMs, long syncStatusTimes, GrabRoundInfoModel newRoundInfo) {
        MyLog.w(TAG, "updatePlayerState" + " gameOverTimeMs=" + gameOverTimeMs + " syncStatusTimes=" + syncStatusTimes + " currentInfo=" + newRoundInfo.getRoundSeq());
        if (syncStatusTimes > mRoomData.getLastSyncTs()) {
            mRoomData.setLastSyncTs(syncStatusTimes);
        }

        if (gameOverTimeMs != 0) {
            if (gameOverTimeMs > mRoomData.getGameStartTs()) {
                MyLog.w(TAG, "gameOverTimeMs ！= 0 游戏应该结束了");
                // 游戏结束了
                onGameOver("sync", gameOverTimeMs, null);
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 startTs:" + mRoomData.getGameStartTs() + " overTs:" + gameOverTimeMs);
            }
        } else {
            // 没结束 current 不应该为null
            if (newRoundInfo != null) {
                // 服务下发的轮次已经大于当前轮次了，说明本地信息已经不对了，更新
                if (RoomDataUtils.roundSeqLarger(newRoundInfo, mRoomData.getExpectRoundInfo())) {
                    MyLog.w(TAG, "updatePlayerState" + " sync发现本地轮次信息滞后，更新");
                    // 轮次确实比当前的高，可以切换
                    mRoomData.setExpectRoundInfo(newRoundInfo);
                    mRoomData.checkRoundInEachMode();
                } else if (RoomDataUtils.isCurrentExpectingRound(newRoundInfo.getRoundSeq(), mRoomData)) {
                    /**
                     * 是当前轮次，最近状态就更新整个轮次
                     */
                    if (syncStatusTimes > mRoomData.getLastSyncTs()) {
                        mRoomData.getExpectRoundInfo().tryUpdateRoundInfoModel(newRoundInfo, true);
                    }
                }
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 currentInfo=null");
            }
        }
    }

    /**
     * 通知游戏结束
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING, priority = 9)
    public void onEvent(GrabGameOverEvent event) {
        MyLog.d(TAG, "GrabGameOverEvent");
        estimateOverTsThisRound();
        tryStopRobotPlay();
        EngineManager.getInstance().stopRecognize();
        mRoomData.setIsGameFinish(true);
        cancelSyncGameStateTask();
        // 游戏结束了,处理相应的ui逻辑
        mUiHanlder.post(new Runnable() {
            @Override
            public void run() {
                mIGrabView.roundOver(event.lastRoundInfo.getMusic().getItemID(), event.lastRoundInfo.getOverReason(), event.lastRoundInfo.getResultType(), false, null);
            }
        });
        // 销毁引擎，减小成本
        EngineManager.getInstance().destroy("grabroom");
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIGrabView.gameFinish();
            }
        }, 2000);
    }

    /**
     * 轮次信息有更新
     */
    @Subscribe(threadMode = ThreadMode.POSTING, priority = 9)
    public void onEvent(GrabRoundChangeEvent event) {
        MyLog.d(TAG, "GrabRoundChangeEvent" + " event=" + event);
        // 轮次变化尝试更新头像
        estimateOverTsThisRound();
        closeEngine();
        tryStopRobotPlay();
        EngineManager.getInstance().stopRecognize();
        GrabRoundInfoModel now = event.newRoundInfo;
        EventBus.getDefault().post(new GrabPlaySeatUpdateEvent(now.getPlayUsers()));
        EventBus.getDefault().post(new GrabWaitSeatUpdateEvent(now.getWaitUsers()));
        if (now.getStatus() == GrabRoundInfoModel.STATUS_GRAB) {
            //抢唱阶段，播抢唱卡片
            if (event.lastRoundInfo != null && event.lastRoundInfo.getStatus() >= GrabRoundInfoModel.STATUS_SING) {
                // 新一轮的抢唱阶段，得告诉上一轮演唱结束了啊，上一轮演唱结束卡片播完，才播歌曲卡片
                mUiHanlder.post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabView.roundOver(event.lastRoundInfo.getMusic().getItemID(), event.lastRoundInfo.getOverReason(), event.lastRoundInfo.getResultType(), true, now);
                    }
                });

                if (event.lastRoundInfo.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                    onSelfRoundOver(event.lastRoundInfo);
                }
            } else {
                mIGrabView.grabBegin(now.getRoundSeq(), now.getMusic());
            }
        } else if (now.getStatus() == GrabRoundInfoModel.STATUS_SING) {
            // 演唱阶段
            if (now.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                mIGrabView.singBySelf();
            } else {
                mIGrabView.singByOthers(now.getUserID());
                checkMachineUser(now.getUserID());
            }
        } else if (now.getStatus() == GrabRoundInfoModel.STATUS_OVER) {
            MyLog.w(TAG, "GrabRoundChangeEvent 刚切换到该轮次就告诉我轮次结束？？？roundSeq:" + now.getRoundSeq());
            MyLog.w(TAG, "自动切换到下个轮次");
//            GrabRoundInfoModel roundInfoModel = RoomDataUtils.findRoundInfoBySeq(mRoomData.getRoundInfoModelList(), now.getRoundSeq() + 1);
//            mRoomData.setExpectRoundInfo(roundInfoModel);
//            mRoomData.checkRoundInEachMode();
        }
    }

    /**
     * 轮次内状态更新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING, priority = 9)
    public void onEvent(GrabRoundStatusChangeEvent event) {
        MyLog.d(TAG, "GrabRoundStatusChangeEvent" + " event=" + event);
        estimateOverTsThisRound();
        closeEngine();
        GrabRoundInfoModel now = event.roundInfo;
        tryStopRobotPlay();
        if (now.getStatus() == GrabRoundInfoModel.STATUS_GRAB) {
            //抢唱阶段，播抢唱卡片
            mIGrabView.grabBegin(now.getRoundSeq(), now.getMusic());
        } else if (now.getStatus() == GrabRoundInfoModel.STATUS_SING) {
            // 演唱阶段
            if (now.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                mIGrabView.singBySelf();
            } else {
                mIGrabView.singByOthers(now.getUserID());
                checkMachineUser(now.getUserID());
            }
        }
    }

    private void closeEngine() {
        if (mRoomData.getGameId() > 0) {
            EngineManager.getInstance().setClientRole(false);
        }
    }

    /**
     * 想要演唱机会的人
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QWantSingChanceMsgEvent event) {
        if (RoomDataUtils.isCurrentExpectingRound(event.getRoundSeq(), mRoomData)) {
            MyLog.w(TAG, "有人想唱：userID " + event.getUserID() + ", seq " + event.getRoundSeq());
            GrabRoundInfoModel roundInfoModel = mRoomData.getExpectRoundInfo();
            WantSingerInfo wantSingerInfo = new WantSingerInfo();
            wantSingerInfo.setUserID(event.getUserID());
            wantSingerInfo.setTimeMs(System.currentTimeMillis());
            roundInfoModel.addGrabUid(true, wantSingerInfo);
        } else {
            MyLog.w(TAG, "有人想唱,但是不是这个轮次：userID " + event.getUserID() + ", seq " + event.getRoundSeq() + "，当前轮次是 " + mRoomData.getExpectRoundInfo());
        }
    }

    /**
     * 抢到演唱机会的人
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QGetSingChanceMsgEvent event) {
        if (RoomDataUtils.isCurrentExpectingRound(event.getRoundSeq(), mRoomData)) {
            MyLog.w(TAG, "抢到唱歌权：userID " + event.getUserID() + ", seq " + event.getRoundSeq());
            GrabRoundInfoModel roundInfoModel = mRoomData.getExpectRoundInfo();
            roundInfoModel.setHasSing(true);
            roundInfoModel.setUserID(event.getUserID());
            roundInfoModel.updateStatus(true, GrabRoundInfoModel.STATUS_SING);
        } else {
            MyLog.w(TAG, "抢到唱歌权,但是不是这个轮次：userID " + event.getUserID() + ", seq " + event.getRoundSeq() + "，当前轮次是 " + mRoomData.getExpectRoundInfo());
        }
    }

    /**
     * 有人灭灯
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QLightOffMsgEvent event) {
        if (RoomDataUtils.isCurrentExpectingRound(event.roundSeq, mRoomData)) {
            MyLog.w(TAG, "有人灭灯了：userID " + event.userID + ", seq " + event.roundSeq);
            GrabRoundInfoModel roundInfoModel = mRoomData.getExpectRoundInfo();
            //都开始灭灯肯定是已经开始唱了
            roundInfoModel.updateStatus(true, GrabRoundInfoModel.STATUS_SING);
            MLightInfoModel noPassingInfo = new MLightInfoModel();
            noPassingInfo.setUserID(event.userID);
            roundInfoModel.addLightOffUid(true, noPassingInfo);
            pretendLightMsgComment(roundInfoModel.getUserID(), event.userID, false);
        } else {
            MyLog.w(TAG, "有人灭灯了,但是不是这个轮次：userID " + event.userID + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.getExpectRoundInfo());
        }
    }

    /**
     * 有人爆灯
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QLightBurstMsgEvent event) {
        if (RoomDataUtils.isCurrentExpectingRound(event.roundSeq, mRoomData)) {
            MyLog.w(TAG, "有人爆灯了：userID " + event.userID + ", seq " + event.roundSeq);
            GrabRoundInfoModel roundInfoModel = mRoomData.getExpectRoundInfo();
            //都开始灭灯肯定是已经开始唱了
            roundInfoModel.updateStatus(true, GrabRoundInfoModel.STATUS_SING);
            BLightInfoModel noPassingInfo = new BLightInfoModel();
            noPassingInfo.setUserID(event.userID);
            roundInfoModel.addLightBurstUid(true, noPassingInfo);
            pretendLightMsgComment(roundInfoModel.getUserID(), event.userID, true);
        } else {
            MyLog.w(TAG, "有人爆灯了,但是不是这个轮次：userID " + event.userID + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.getExpectRoundInfo());
        }
    }

    /**
     * 伪装爆灭灯消息
     *
     * @param singerId 被灭灯演唱者
     * @param uid      灭灯操作者
     */
    private void pretendLightMsgComment(int singerId, int uid, boolean isBao) {
        PlayerInfoModel singerModel = RoomDataUtils.getPlayerInfoById(mRoomData, singerId);
        PlayerInfoModel playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, uid);
        if (singerModel != null && playerInfoModel != null) {
            CommentModel commentModel = new CommentModel();
            commentModel.setCommentType(CommentModel.TYPE_TRICK);
            commentModel.setUserId(playerInfoModel.getUserID());
            commentModel.setAvatar(playerInfoModel.getUserInfo().getAvatar());
            commentModel.setUserName(playerInfoModel.getUserInfo().getNickname());
            commentModel.setAvatarColor(playerInfoModel.getUserInfo().getSex() == ESex.SX_MALE.getValue() ?
                    U.getColor(R.color.color_man_stroke_color) : U.getColor(R.color.color_woman_stroke_color));
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(playerInfoModel.getUserInfo().getNickname() + " ").setForegroundColor(CommentModel.TEXT_YELLOW)
                    .append("对").setForegroundColor(CommentModel.TEXT_WHITE)
                    .append(singerModel.getUserInfo().getNickname()).setForegroundColor(CommentModel.TEXT_YELLOW)
                    .append(isBao ? "爆灯啦" : "灭了盏灯").setForegroundColor(CommentModel.TEXT_WHITE)
                    .create();
            commentModel.setStringBuilder(stringBuilder);
            EventBus.getDefault().post(new PretendCommentMsgEvent(commentModel));
        }
    }

    /**
     * 有人加入房间
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QJoinNoticeEvent event) {
        if (RoomDataUtils.isCurrentExpectingRound(event.roundSeq, mRoomData)) {
            GrabPlayerInfoModel playerInfoModel = event.infoModel;
            MyLog.d(TAG, "有人加入房间,id=" + playerInfoModel.getUserID() + " name=" + playerInfoModel.getUserInfo().getNickname() + " role=" + playerInfoModel.getRole() + " roundSeq=" + event.roundSeq);
            GrabRoundInfoModel grabRoundInfoModel = mRoomData.getExpectRoundInfo();
            if (grabRoundInfoModel.addUser(true, playerInfoModel)) {
                CommentModel commentModel = new CommentModel();
                commentModel.setCommentType(CommentModel.TYPE_TRICK);
                commentModel.setUserId(playerInfoModel.getUserInfo().getUserId());
                commentModel.setAvatar(playerInfoModel.getUserInfo().getAvatar());
                commentModel.setUserName(playerInfoModel.getUserInfo().getNickname());
                commentModel.setAvatarColor(playerInfoModel.getUserInfo().getSex() == ESex.SX_MALE.getValue() ?
                        U.getColor(R.color.color_man_stroke_color) : U.getColor(R.color.color_woman_stroke_color));
                SpannableStringBuilder stringBuilder;
                if (playerInfoModel.getUserInfo().getUserId() == UserAccountManager.SYSTEM_GRAB_ID) {
                    stringBuilder = new SpanUtils()
                            .append(playerInfoModel.getUserInfo().getNickname() + " ").setForegroundColor(CommentModel.TEXT_YELLOW)
                            .append("我是撕歌最傲娇小助手多音，来和你们一起唱歌卖萌~").setForegroundColor(CommentModel.TEXT_WHITE)
                            .create();
                } else {
                    SpanUtils spanUtils = new SpanUtils()
                            .append(playerInfoModel.getUserInfo().getNickname() + " ").setForegroundColor(CommentModel.TEXT_YELLOW)
                            .append("加入了房间").setForegroundColor(CommentModel.TEXT_WHITE);
                    if (BuildConfig.DEBUG) {
                        spanUtils.append(" 角色为" + playerInfoModel.getRole())
                                .append(" 在线状态为" + playerInfoModel.isOnline());
                    }
                    stringBuilder = spanUtils.create();
                }
                commentModel.setStringBuilder(stringBuilder);
                EventBus.getDefault().post(new PretendCommentMsgEvent(commentModel));
            }
        } else {
            MyLog.w(TAG, "有人加入房间了,但是不是这个轮次：userID " + event.infoModel.getUserID() + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.getExpectRoundInfo());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QExitGameMsgEvent event) {
        if (RoomDataUtils.isCurrentExpectingRound(event.roundSeq, mRoomData)) {
            MyLog.d(TAG, "有人离开房间,id=" + event.userID);
            GrabRoundInfoModel grabRoundInfoModel = mRoomData.getExpectRoundInfo();
            grabRoundInfoModel.removeUser(true, event.userID);
        } else {
            MyLog.w(TAG, "有人离开房间了,但是不是这个轮次：userID " + event.userID + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.getExpectRoundInfo());
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QRoundOverMsgEvent event) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push event:" + event);
        if (mRoomData.getLastSyncTs() >= event.getInfo().getTimeMs()) {
            MyLog.w(TAG, "但是是个旧数据");
            return;
        }

        if (RoomDataUtils.isCurrentRunningRound(event.getCurrentRound().getRoundSeq(), mRoomData)) {
            // 如果是当前轮次
            mRoomData.getRealRoundInfo().tryUpdateRoundInfoModel(event.currentRound, true);
            if (event.myCoin >= 0) {
                mRoomData.setCoin(event.myCoin);
            }
        }
        // 游戏轮次结束
        if (RoomDataUtils.roundSeqLarger(event.nextRound, mRoomData.getExpectRoundInfo())) {
            // 轮次确实比当前的高，可以切换
            MyLog.w(TAG, "轮次确实比当前的高，可以切换");
            mRoomData.setExpectRoundInfo(event.nextRound);
            mRoomData.checkRoundInEachMode();
        } else {
            MyLog.w(TAG, "轮次比当前轮次还小,直接忽略 当前轮次:" + mRoomData.getExpectRoundInfo().getRoundSeq()
                    + " push轮次:" + event.currentRound.getRoundSeq());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QRoundAndGameOverMsgEvent event) {
        cancelSyncGameStateTask();
        if (RoomDataUtils.isCurrentRunningRound(event.roundInfoModel.getRoundSeq(), mRoomData)) {
            // 如果是当前轮次
            mRoomData.getRealRoundInfo().tryUpdateRoundInfoModel(event.roundInfoModel, true);
            if (event.myCoin >= 0) {
                mRoomData.setCoin(event.myCoin);
            }
        }
        onGameOver("QRoundAndGameOverMsgEvent", event.roundOverTimeMs, event.resultInfo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QSyncStatusMsgEvent event) {
        MyLog.w(TAG, "收到服务器push更新状态,event.currentRound是" + event.getCurrentRound().getRoundSeq() + ", timeMs 是" + event.info.getTimeMs());
        startSyncGameStateTask(sSyncStateTaskInterval);
        updatePlayerState(event.getGameOverTimeMs(), event.getSyncStatusTimeMs(), event.getCurrentRound());
    }

    private void onGameOver(String from, long gameOverTs, List<GrabResultInfoModel> grabResultInfoModels) {
        MyLog.w(TAG, "游戏结束 gameOverTs=" + gameOverTs + " from:" + from);
        if (gameOverTs > mRoomData.getGameStartTs() && gameOverTs > mRoomData.getGameOverTs()) {
            cancelSyncGameStateTask();
            if (grabResultInfoModels != null && grabResultInfoModels.size() > 0) {
                mRoomData.setResultList(grabResultInfoModels);
            }
            mRoomData.setGameOverTs(gameOverTs);
            mRoomData.setExpectRoundInfo(null);
            mRoomData.checkRoundInEachMode();
        } else {
            MyLog.w(TAG, "游戏结束 gameOverTs 不合法，取消");
        }
    }

    public void checkMachineUser(long uid) {
        PlayerInfoModel playerInfo = RoomDataUtils.getPlayerInfoById(mRoomData, uid);
        if (playerInfo == null) {
            MyLog.w(TAG, "切换别人的时候PlayerInfo为空");
            return;
        }
        /**
         * 机器人
         */
        if (playerInfo.isSkrer()) {
            MyLog.d(TAG, "checkMachineUser" + " uid=" + uid + " is machine");
            //这个时间现在待定
            //移除之前的要发生的机器人演唱
            mUiHanlder.removeMessages(MSG_ROBOT_SING_BEGIN);
            Message message = mUiHanlder.obtainMessage(MSG_ROBOT_SING_BEGIN);
            mUiHanlder.sendMessage(message);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        swapGame(!event.foreground, event.foreground);
        if (event.foreground) {
            muteAllRemoteAudioStreams(mRoomData.isMute(), false);
        } else {
            muteAllRemoteAudioStreams(true, false);
        }
    }

    private int estimateOverTsThisRound() {
        int pt = RoomDataUtils.estimateTs2End(mRoomData, mRoomData.getRealRoundInfo());
        MyLog.w(TAG, "估算出距离本轮结束还有" + pt + "ms");
        return pt;
    }


}
