package com.module.playways.grab.room.presenter;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.engine.ScoreConfig;
import com.common.jiguang.JiGuangPush;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.player.IPlayer;
import com.common.player.VideoPlayerAdapter;
import com.common.player.ExoPlayer;
import com.common.player.AndroidMediaPlayer;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.utils.ActivityUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.log.DebugLogView;
import com.component.busilib.recommend.RA;
import com.dialog.view.TipsDialogView;
import com.engine.EngineEvent;
import com.engine.Params;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.arccloud.SongInfo;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.CustomMsgType;
import com.module.msg.IMsgService;
import com.module.playways.BuildConfig;
import com.module.playways.RoomDataUtils;
import com.module.playways.event.GrabChangeRoomEvent;
import com.module.playways.grab.room.GrabResultData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.event.GrabGameOverEvent;
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.grab.room.event.GrabSpeakingControlEvent;
import com.module.playways.grab.room.event.GrabSwitchRoomEvent;
import com.module.playways.grab.room.event.GrabWaitSeatUpdateEvent;
import com.module.playways.grab.room.event.SomeOneJoinWaitSeatEvent;
import com.module.playways.grab.room.event.SomeOneLeavePlaySeatEvent;
import com.module.playways.grab.room.inter.IGrabRoomView;
import com.module.playways.grab.room.model.BLightInfoModel;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.GrabSkrResourceModel;
import com.module.playways.grab.room.model.MLightInfoModel;
import com.module.playways.grab.room.model.NumericDetailModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.grab.room.model.WantSingerInfo;
import com.module.playways.grab.room.model.WorksUploadModel;
import com.module.playways.grab.room.songmanager.event.BeginRecordCustomGameEvent;
import com.module.playways.grab.room.songmanager.event.RoomNameChangeEvent;
import com.module.playways.others.LyricAndAccMatchManager;
import com.module.playways.room.gift.event.GiftBrushMsgEvent;
import com.module.playways.room.gift.event.UpdateCoinEvent;
import com.module.playways.room.gift.event.UpdateMeiliEvent;
import com.module.playways.room.gift.model.GPrensentGiftMsgModel;
import com.module.playways.room.msg.event.GiftPresentEvent;
import com.module.playways.room.msg.event.MachineScoreEvent;
import com.module.playways.room.msg.event.QChangeMusicTagEvent;
import com.module.playways.room.msg.event.QChangeRoomNameEvent;
import com.module.playways.room.msg.event.QChoGiveUpEvent;
import com.module.playways.room.msg.event.QCoinChangeEvent;
import com.module.playways.room.msg.event.QExitGameMsgEvent;
import com.module.playways.room.msg.event.QGameBeginEvent;
import com.module.playways.room.msg.event.QGetSingChanceMsgEvent;
import com.module.playways.room.msg.event.QJoinNoticeEvent;
import com.module.playways.room.msg.event.QKickUserReqEvent;
import com.module.playways.room.msg.event.QKickUserResultEvent;
import com.module.playways.room.msg.event.QLightBurstMsgEvent;
import com.module.playways.room.msg.event.QLightOffMsgEvent;
import com.module.playways.room.msg.event.QPkInnerRoundOverEvent;
import com.module.playways.room.msg.event.QRoundAndGameOverMsgEvent;
import com.module.playways.room.msg.event.QRoundOverMsgEvent;
import com.module.playways.room.msg.event.QSyncStatusMsgEvent;
import com.module.playways.room.msg.event.QWantSingChanceMsgEvent;
import com.module.playways.room.msg.filter.PushMsgFilter;
import com.module.playways.room.msg.manager.ChatRoomMsgManager;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel;
import com.module.playways.room.prepare.model.PlayerInfoModel;
import com.module.playways.room.room.SwapStatusType;
import com.module.playways.room.room.comment.model.CommentLightModel;
import com.module.playways.room.room.comment.model.CommentModel;
import com.module.playways.room.room.comment.model.CommentSysModel;
import com.module.playways.room.room.comment.model.CommentTextModel;
import com.module.playways.room.room.event.PretendCommentMsgEvent;
import com.module.playways.room.room.model.score.ScoreResultModel;
import com.module.playways.room.room.score.MachineScoreItem;
import com.module.playways.room.room.score.RobotScoreHelper;
import com.module.playways.room.song.model.SongModel;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.StandPlayType;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.EMsgPosType;
import com.zq.live.proto.Room.EQGameOverReason;
import com.zq.live.proto.Room.EQRoundOverReason;
import com.zq.live.proto.Room.EQRoundResultType;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.EWantSingType;
import com.zq.live.proto.Room.MachineScore;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.lyrics.utils.SongResUtils;
import com.zq.lyrics.utils.ZipUrlResourceManager;
import com.zq.mediaengine.kit.ZqEngineKit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import io.agora.rtc.Constants;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabCorePresenter extends RxLifeCyclePresenter {
    public String TAG = "GrabCorePresenter";

    private static long sSyncStateTaskInterval = 5000;

    static final int MSG_ENSURE_IN_RC_ROOM = 9;// 确保在融云的聊天室，保证融云的长链接

    static final int MSG_ROBOT_SING_BEGIN = 10;

    static final int MSG_ENSURE_SWITCH_BROADCAST_SUCCESS = 21; // 确保用户切换成主播成功，防止引擎不回调的保护

    static final int MSG_RECOVER_VOLUME = 22; // 房主说话后 恢复音量

    static final int MSG_ENSURE_EXIT = 8; // 房主说话后 恢复音量

    long mFirstKickOutTime = -1; //用时间和次数来判断一个人有没有在一个房间里

    int mAbsenTimes = 0;

    GrabRoomData mRoomData;

    GrabRoomServerApi mRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);

    HandlerTaskTimer mSyncGameStateTask;

    IGrabRoomView mIGrabView;

    RobotScoreHelper mRobotScoreHelper;

    boolean mDestroyed = false;

    IPlayer mExoPlayer;

    boolean mSwitchRooming = false;

    GrabRedPkgPresenter mGrabRedPkgPresenter;

    ZipUrlResourceManager mZipUrlResourceManager;

    EngineParamsTemp mEngineParamsTemp;

    BaseActivity mBaseActivity;

    DialogPlus mDialogPlus;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ENSURE_IN_RC_ROOM:
                    MyLog.d(TAG, "handleMessage 长时间没收到push，重新进入融云房间容错");
                    ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(mRoomData.getGameId() + "");
                    joinRcRoom(0);
                    ensureInRcRoom();
                    break;
                case MSG_ROBOT_SING_BEGIN:
                    robotSingBegin();
                    break;
                case MSG_ENSURE_SWITCH_BROADCAST_SUCCESS:
                    onChangeBroadcastSuccess();
                    break;
                case MSG_RECOVER_VOLUME:
                    if (mEngineParamsTemp != null) {
                        ZqEngineKit.getInstance().adjustAudioMixingVolume(mEngineParamsTemp.audioVolume, false);
                        ZqEngineKit.getInstance().adjustRecordingSignalVolume(mEngineParamsTemp.recordVolume, false);

                        if (ZqEngineKit.getInstance().getParams().isAnchor()) {
                            int audioVolume = ZqEngineKit.getInstance().getParams().getAudioMixingVolume();
                            int recordVolume = ZqEngineKit.getInstance().getParams().getRecordingSignalVolume();
                            ZqEngineKit.getInstance().adjustAudioMixingVolume(audioVolume, false);
                            ZqEngineKit.getInstance().adjustRecordingSignalVolume(recordVolume, false);
                        } else {
                            MyLog.d(TAG, "我不是主播，忽略");
                        }
                        mEngineParamsTemp = null;
                    }
                    if (mExoPlayer != null) {
                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mExoPlayer.getVolume());
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float v = (float) animation.getAnimatedValue();
                                if (mExoPlayer != null) {
                                    mExoPlayer.setVolume(v, false);
                                }
                            }
                        });
                        valueAnimator.setDuration(1000);
                        valueAnimator.start();
                    }
                    break;
                case MSG_ENSURE_EXIT:
                    if (mIGrabView != null) {
                        mIGrabView.onGetGameResult(false);
                    }
                    break;
            }
        }
    };

    PushMsgFilter mPushMsgFilter = new PushMsgFilter<RoomMsg>() {
        @Override
        public boolean doFilter(RoomMsg msg) {
            if (msg != null && msg.getRoomID() == mRoomData.getGameId()) {
                return true;
            }
            return false;
        }
    };

    GrabSongResPresenter mGrabSongResPresenter = new GrabSongResPresenter();

    public GrabCorePresenter(@NotNull IGrabRoomView iGrabView, @NotNull GrabRoomData roomData, BaseActivity baseActivity) {
        mIGrabView = iGrabView;
        mRoomData = roomData;
        mBaseActivity = baseActivity;
        TAG = "GrabCorePresenter";
        ChatRoomMsgManager.getInstance().addFilter(mPushMsgFilter);
        joinRoomAndInit(true);
        U.getFileUtils().deleteAllFiles(U.getAppInfoUtils().getSubDirPath("WonderfulMoment"));
    }

    public void setGrabRedPkgPresenter(GrabRedPkgPresenter grabRedPkgPresenter) {
        mGrabRedPkgPresenter = grabRedPkgPresenter;
    }

    /**
     * 加入引擎房间
     * 加入融云房间
     * 系统消息弹幕
     */
    private void joinRoomAndInit(boolean first) {
        MyLog.w(TAG, "joinRoomAndInit" + " first=" + first + ", gameId is " + mRoomData.getGameId());
        mFirstKickOutTime = -1;
        mAbsenTimes = 0;

        if (mRoomData.getGameId() > 0) {
            boolean reInit = false;
            if (first) {
                reInit = true;
            }
            if (!reInit && ZqEngineKit.getInstance().getParams().isEnableVideo() != mRoomData.isVideoRoom()) {
                MyLog.d(TAG, "音视频模式发生切换");
                mIGrabView.changeRoomMode(mRoomData.isVideoRoom());
                // 发出通知
                reInit = true;
            }
            if (reInit) {
                Params params = Params.getFromPref();
//            params.setStyleEnum(Params.AudioEffect.none);
                params.setScene(Params.Scene.grab);
                params.setEnableVideo(mRoomData.isVideoRoom());
                ZqEngineKit.getInstance().init("grabroom", params);
            }
            ZqEngineKit.getInstance().joinRoom(String.valueOf(mRoomData.getGameId()), (int) UserAccountManager.getInstance().getUuidAsLong(), false, mRoomData.getAgoraToken());
            // 不发送本地音频, 会造成第一次抢没声音
            ZqEngineKit.getInstance().muteLocalAudioStream(true);
            if (mRoomData.isVideoRoom()) {
                ZqEngineKit.getInstance().unbindAllRemoteVideo();
            }
        }
        joinRcRoom(-1);
        if (mRoomData.getGameId() > 0) {
            for (GrabPlayerInfoModel playerInfoModel : mRoomData.getPlayerInfoList()) {
                if (!playerInfoModel.isOnline()) {
                    continue;
                }
                pretendEnterRoom(playerInfoModel);
            }
            pretendRoomNameSystemMsg(mRoomData.getRoomName(), CommentSysModel.TYPE_ENTER_ROOM);
        }
        if (mRoomData.hasGameBegin()) {
            startSyncGameStateTask(sSyncStateTaskInterval);
        } else {
            cancelSyncGameStateTask();
        }
    }

    private void joinRcRoom(int deep) {
        if (deep > 4) {
            MyLog.d(TAG, "加入融云房间，重试5次仍然失败，放弃");
            return;
        }
        if (mRoomData.getGameId() > 0) {
            ModuleServiceManager.getInstance().getMsgService().joinChatRoom(String.valueOf(mRoomData.getGameId()), -1, new ICallback() {
                @Override
                public void onSucess(Object obj) {
                    MyLog.d(TAG, "加入融云房间成功");
                }

                @Override
                public void onFailed(Object obj, int errcode, String message) {
                    MyLog.d(TAG, "加入融云房间失败， msg is " + message + ", errcode is " + errcode);
                    joinRcRoom(deep + 1);
                }
            });
            if (deep == -1) {
                /**
                 * 说明是初始化时那次加入房间，这时加入极光房间做个备份，使用tag的方案
                 */
                JiGuangPush.joinSkrRoomId(String.valueOf(mRoomData.getGameId()));
            }
        }
    }

    private void ensureInRcRoom() {
        mUiHandler.removeMessages(MSG_ENSURE_IN_RC_ROOM);
        mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_IN_RC_ROOM, 30 * 1000);
    }

    private void pretendSystemMsg(String text) {
        CommentSysModel commentSysModel = new CommentSysModel(mRoomData.getGameType(), text);
        EventBus.getDefault().post(new PretendCommentMsgEvent(commentSysModel));
    }

    private void pretendRoomNameSystemMsg(String roomName, int type) {
        CommentSysModel commentSysModel = new CommentSysModel(roomName, type);
        EventBus.getDefault().post(new PretendCommentMsgEvent(commentSysModel));
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
        if (mRoomData.hasGameBegin()) {
            mRoomData.checkRoundInEachMode();
            ensureInRcRoom();
        } else {
            MyLog.d(TAG, "onOpeningAnimationOver 游戏未开始");
        }
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
                mExoPlayer = new AndroidMediaPlayer();
                if (mRoomData.isMute() || !U.getActivityUtils().isAppForeground()) {
                    mExoPlayer.setVolume(0);
                } else {
                    mExoPlayer.setVolume(1);
                }
            }
            mExoPlayer.setCallback(new VideoPlayerAdapter.PlayerCallbackAdapter() {
                @Override
                public void onPrepared() {
                    super.onPrepared();
                    if (!now.isParticipant() && now.getEnterStatus() == EQRoundStatus.QRS_INTRO.getValue()) {
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
     * 如果确定是自己唱了,预先可以做的操作
     */
    void preOpWhenSelfRound() {
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        boolean needAcc = false;
        boolean needScore = false;
        if (now != null) {
            Params p = ZqEngineKit.getInstance().getParams();
            if (p != null) {
                p.setGrabSingNoAcc(false);
            }
            if (now.getWantSingType() == EWantSingType.EWST_SPK.getValue()) {
                needAcc = true;
                needScore = true;
            } else if (now.getWantSingType() == EWantSingType.EWST_CHORUS.getValue()) {
                needAcc = false;
                needScore = false;
            } else if (now.getWantSingType() == EWantSingType.EWST_MIN_GAME.getValue()) {
                needAcc = false;
                needScore = false;
            } else if (mRoomData.isAccEnable()) {
                needAcc = true;
                needScore = true;
            } else {
                if (p != null) {
                    p.setGrabSingNoAcc(true);
                    needScore = true;
                }
            }
        }
        if (needAcc) {
            // 1. 开启伴奏的，预先下载 melp 资源
            if (now != null) {
                File midiFile = SongResUtils.getMIDIFileByUrl(now.getMusic().getMidi());
                if (midiFile != null && !midiFile.exists()) {
                    U.getHttpUtils().downloadFileAsync(now.getMusic().getMidi(), midiFile, true, null);
                }
            }
        }
        if (!ZqEngineKit.getInstance().getParams().isAnchor()) {
            ZqEngineKit.getInstance().setClientRole(true);
            ZqEngineKit.getInstance().muteLocalAudioStream(false);
            if (needAcc) {
                // 如果需要播放伴奏，一定要在角色切换成功才能播
                mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS, 2000);
            }
        } else {
            // 如果是房主,不在这里 解禁，会录进去音效的声音 延后一些再解开
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ZqEngineKit.getInstance().muteLocalAudioStream(false);
                    onChangeBroadcastSuccess();
                }
            }, 500);
        }

        // 开始acr打分
        if (ScoreConfig.isAcrEnable() && now != null && now.getMusic() != null) {
            if (needAcc) {
                ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                        .setSongName(now.getMusic().getItemName())
                        .setArtist(now.getMusic().getOwner())
                        .setMode(RecognizeConfig.MODE_MANUAL)
                        .build());
            } else {
                if (needScore) {
                    // 清唱还需要打分，那就只用 acr 打分
                    ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                            .setSongName(now.getMusic().getItemName())
                            .setArtist(now.getMusic().getOwner())
                            .setMode(RecognizeConfig.MODE_AUTO)
                            .setAutoTimes(3)
                            .setMResultListener(new ArcRecognizeListener() {
                                @Override
                                public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo, int lineNo) {
                                    int mAcrScore = 0;
                                    if (targetSongInfo != null) {
                                        mAcrScore = (int) (targetSongInfo.getScore() * 100);
                                    }
                                    EventBus.getDefault().post(new LyricAndAccMatchManager.ScoreResultEvent("preOpWhenSelfRound", -1, mAcrScore, 0));
                                }
                            })
                            .build());
                } else {

                }
            }
        }
    }

    public void preOpWhenOtherRound(long uid) {
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
            mUiHandler.removeMessages(MSG_ROBOT_SING_BEGIN);
            Message message = mUiHandler.obtainMessage(MSG_ROBOT_SING_BEGIN);
            mUiHandler.sendMessage(message);
        }

        // 别人的轮次
//        if (mRoomData.isVideoRoom()) {
//            // 如果是语音房间
//            GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
//            if (infoModel != null) {
//                if (infoModel.isPKRound()) {
//                    if (infoModel.getsPkRoundInfoModels().size() >= 2) {
//                        int userId1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
//                        int userId2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
//                        if (MyUserInfoManager.getInstance().getUid() == userId1 ||
//                                MyUserInfoManager.getInstance().getUid() == userId2) {
//                            // 万一这个人是一个人 这个人点不唱了
//                            //join房间也变成主播
//                            if (!ZqEngineKit.getInstance().getParams().isAnchor()) {
//                                ZqEngineKit.getInstance().setClientRole(true);
//                            }
//                            // 不发声
//                            ZqEngineKit.getInstance().muteLocalAudioStream(true);
//                        }
//                    }
//                }
//            }
//        }
    }

    /**
     * 真正打开引擎开始演唱
     */
    public void beginSing() {
        // 打开引擎，变为主播
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (mRoomData.openAudioRecording()) {
            // 需要上传音频伪装成机器人
            if (now != null && !now.isMiniGameRound()) {
                String fileName = String.format("wm_%s_%s.aac", mRoomData.getGameId(), now.getRoundSeq());
                String savePath = U.getAppInfoUtils().getFilePathInSubDir("WonderfulMoment", fileName);
                ZqEngineKit.getInstance().startAudioRecording(savePath, Constants.AUDIO_RECORDING_QUALITY_HIGH);
            }
        }

        /**
         *     if (now != null) {
         *                 if (mRobotScoreHelper == null) {
         *                     mRobotScoreHelper = new RobotScoreHelper();
         *                 }
         *                 mRobotScoreHelper.reset();
         *             }
         */


    }

    /**
     * 房主点击开始游戏
     */
    public void ownerBeginGame() {
        MyLog.d(TAG, "ownerBeginGame");
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.ownerBeginGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    JoinGrabRoomRspModel rsp = JSON.parseObject(result.getData().toJSONString(), JoinGrabRoomRspModel.class);
                    // 模拟服务器push，触发游戏更新
                    QGameBeginEvent event = new QGameBeginEvent();
                    event.roomID = rsp.getRoomID();
                    event.mGrabConfigModel = rsp.getConfig();
                    event.mInfoModel = rsp.getCurrentRound();
                    onEvent(event);
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        }, this, new ApiMethods.RequestControl("ownerBeginGame", ApiMethods.ControlType.CancelThis));
    }

    /**
     * 抢唱歌权
     */
    public void grabThisRound(int seq, boolean challenge) {
        MyLog.d(TAG, "grabThisRound" + " seq=" + seq + " challenge=" + challenge + " accenable=" + mRoomData.isAccEnable());


        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            if (infoModel.getWantSingInfos().contains(new WantSingerInfo((int) MyUserInfoManager.getInstance().getUid()))) {
                MyLog.w(TAG, "grabThisRound cancel 想唱列表中已经有你了");
                return;
            }
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", seq);


        SongModel songModel = null;
        if (infoModel != null && infoModel.getMusic() != null) {
//            HashMap map1 = new HashMap();
//            map.put("songId2", String.valueOf(infoModel.getMusic().getItemID()));
//            map.put("songName", infoModel.getMusic().getItemName());
//            StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                    "game_grab", map1);
            songModel = infoModel.getMusic();
        }

        String preAccUrl = "";

        int wantSingType;
        // 根据玩法决定抢唱类型
        if (songModel != null && songModel.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
            wantSingType = EWantSingType.EWST_SPK.getValue();
            if (infoModel != null) {
                if (infoModel.getWantSingInfos().isEmpty()) {
                    // 自己大概率第一个唱
                    if (infoModel.getMusic() != null) {
                        preAccUrl = infoModel.getMusic().getAcc();
                    }
                } else {
                    //  自己大概率不是第一个唱
                    if (infoModel.getMusic() != null) {
                        SongModel pkSongModel = infoModel.getMusic().getPkMusic();
                        if (pkSongModel != null) {
                            preAccUrl = pkSongModel.getAcc();
                        }
                    }
                }
            }
        } else if (songModel != null && songModel.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
            wantSingType = EWantSingType.EWST_CHORUS.getValue();
        } else if (songModel != null && songModel.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
            wantSingType = EWantSingType.EWST_MIN_GAME.getValue();
        } else {
            if (challenge) {
                if (mRoomData.getCoin() < 1) {
                    MyLog.w(TAG, "没有充足金币,无法进行挑战");
                    U.getToastUtil().showShort("没有充足的金币");
                    return;
                }
                if (mRoomData.isAccEnable() && songModel != null && !TextUtils.isEmpty(songModel.getAcc())) {
                    wantSingType = EWantSingType.EWST_ACCOMPANY_OVER_TIME.getValue();
                    preAccUrl = songModel.getAcc();
                } else {
                    wantSingType = EWantSingType.EWST_COMMON_OVER_TIME.getValue();
                }
            } else {
                if (mRoomData.isAccEnable() && songModel != null && !TextUtils.isEmpty(songModel.getAcc())) {
                    wantSingType = EWantSingType.EWST_ACCOMPANY.getValue();
                    preAccUrl = songModel.getAcc();
                } else {
                    wantSingType = EWantSingType.EWST_DEFAULT.getValue();
                }
            }
        }

        map.put("wantSingType", wantSingType);
        map.put("hasPassedCertify", MyUserInfoManager.getInstance().hasGrabCertifyPassed());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.wangSingChance(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "grabThisRound erro code is " + result.getErrno() + ",traceid is " + result.getTraceId());
                if (result.getErrno() == 0) {
                    //true为已经认证过了或者无需认证，false为未认证
                    boolean mHasPassedCertify = result.getData().getBoolean("hasPassedCertify");

                    if (mHasPassedCertify) {
                        MyUserInfoManager.getInstance().setGrabCertifyPassed(mHasPassedCertify);
                        //抢成功了
                        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
                        if (now != null && now.getRoundSeq() == seq) {
                            WantSingerInfo wantSingerInfo = new WantSingerInfo();
                            wantSingerInfo.setWantSingType(wantSingType);
                            wantSingerInfo.setUserID((int) MyUserInfoManager.getInstance().getUid());
                            wantSingerInfo.setTimeMs(System.currentTimeMillis());
                            now.addGrabUid(true, wantSingerInfo);

                            if (result.getData().getBoolean("success")) {
                                int coin = result.getData().getIntValue("coin");
                                mRoomData.setCoin(coin);
                            }
                        } else {
                            MyLog.w(TAG, "now != null && now.getRoundSeq() == seq 条件不满足，" + result.getTraceId());
                        }
                    } else {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }

                        TipsDialogView tipsDialogView = new TipsDialogView.Builder(mBaseActivity)
                                .setMessageTip("亲～实名认证通过后即可参与抢唱啦！")
                                .setConfirmTip("立即认证")
                                .setCancelTip("残忍拒绝")
                                .setConfirmBtnClickListener(new AnimateClickListener() {
                                    @Override
                                    public void click(View view) {
                                        if (mDialogPlus != null) {
                                            mDialogPlus.dismiss();
                                        }
                                        mIGrabView.beginOuath();
                                    }
                                })
                                .setCancelBtnClickListener(new AnimateClickListener() {
                                    @Override
                                    public void click(View view) {
                                        if (mDialogPlus != null) {
                                            mDialogPlus.dismiss();
                                        }
                                    }
                                })
                                .build();

                        mDialogPlus = DialogPlus.newDialog(mBaseActivity)
                                .setContentHolder(new ViewHolder(tipsDialogView))
                                .setGravity(Gravity.BOTTOM)
                                .setContentBackgroundResource(com.component.busilib.R.color.transparent)
                                .setOverlayBackgroundResource(com.component.busilib.R.color.black_trans_80)
                                .setExpanded(false)
                                .create();
                        mDialogPlus.show();
                    }
                } else if (result.getErrno() == 8346144) {
                    MyLog.w(TAG, "grabThisRound failed 没有充足金币 ");
                    U.getToastUtil().showShort(result.getErrmsg());
                } else {
                    MyLog.w(TAG, "grabThisRound failed, " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "grabThisRound error " + e);

            }
        }, this);

        if (!TextUtils.isEmpty(preAccUrl)) {
            mGrabSongResPresenter.tryDownloadAcc(preAccUrl);
        }
    }

    /**
     * 灭灯
     */
    public void lightsOff() {
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now == null) {
            return;
        }
        if (!now.isSingStatus()) {
            MyLog.d(TAG, "lightsOff 不在演唱状态，cancel status=" + now.getStatus() + " roundSeq=" + now.getRoundSeq());
            return;
        }
        int roundSeq = now.getRoundSeq();
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundSeq);
        if (now.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
            map.put("subRoundSeq", 0);
        } else if (now.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            map.put("subRoundSeq", 1);
        }
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
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
        if (now == null) {
            return;
        }
        if (!now.isSingStatus()) {
            MyLog.d(TAG, "lightsBurst 不在演唱状态，cancel status=" + now.getStatus() + " roundSeq=" + now.getRoundSeq());
            return;
        }
        if (RA.hasTestList()) {
            HashMap map = new HashMap();
            map.put("testList", RA.getTestList());
            StatisticsAdapter.recordCountEvent("ra", "burst", map);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        int roundSeq = now.getRoundSeq();
        map.put("roundSeq", mRoomData.getRealRoundSeq());

        if (now.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
            map.put("subRoundSeq", 0);
        } else if (now.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            map.put("subRoundSeq", 1);
        }
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.lightBurst(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.e(TAG, "lightsBurst erro code is " + result.getErrno() + ",traceid is " + result.getTraceId());
                if (result.getErrno() == 0) {
                    GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
                    if (now != null && now.getRoundSeq() == roundSeq) {
                        int coin = result.getData().getIntValue("coin");
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
        String skrerUrl = null;
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null) {
            GrabSkrResourceModel grabSkrResourceModel = grabRoundInfoModel.getSkrResource();
            if (grabSkrResourceModel != null) {
                skrerUrl = grabSkrResourceModel.getAudioURL();
            }
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
                if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == EQRoundStatus.QRS_SING.getValue()) {
                    MyLog.d(TAG, "进来时已经时演唱阶段了，则机器人资源要seek一下 " + grabRoundInfoModel.getElapsedTimeMs());
                    mExoPlayer.seekTo(grabRoundInfoModel.getElapsedTimeMs());
                }
            }
        });
        if (mRoomData.isMute() || !U.getActivityUtils().isAppForeground()) {
            mExoPlayer.setVolume(0);
        } else {
            mExoPlayer.setVolume(1);
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
        ZqEngineKit.getInstance().muteAllRemoteAudioStreams(mute);
        // 如果是机器人的话
        if (mute) {
            // 如果是静音
            if (mExoPlayer != null) {
                mExoPlayer.setMuteAudio(true);
            }
        } else {
            // 如果打开静音
            if (mExoPlayer != null) {
                mExoPlayer.setMuteAudio(false);
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
//        if (SkrConfig.getInstance().isNeedUploadAudioForAI(GameModeType.GAME_MODE_GRAB)) {
//            //属于需要上传音频文件的状态
//            // 上一轮是我的轮次，暂停录音
//            if (mRoomData.getGameId() > 0) {
//                ZqEngineKit.getInstance().stopAudioRecording();
//            }
//            // 上传打分
//            if (mRobotScoreHelper != null) {
//                if (mRobotScoreHelper.isScoreEnough()) {
//                    if (roundInfoModel.getOverReason() == EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue()
//                            && roundInfoModel.getResultType() == EQRoundResultType.ROT_TYPE_1.getValue()) {
//                        // 是一唱到底的才上传
//                        roundInfoModel.setSysScore(mRobotScoreHelper.getAverageScore());
//                        uploadRes1ForAi(roundInfoModel);
//                    } else {
//                        MyLog.d(TAG, "没有唱到一唱到底不上传");
//                    }
//                } else {
//                    MyLog.d(TAG, "isScoreEnough false");
//                }
//            }
//        }

        if (mGrabRedPkgPresenter != null && mGrabRedPkgPresenter.isCanReceive()) {
            mGrabRedPkgPresenter.getRedPkg();
        }

        if (mRoomData.openAudioRecording() && !roundInfoModel.isMiniGameRound()) {
            SongModel songModel = null;
            boolean baodeng = false;
            if (roundInfoModel.getOverReason() == EQRoundOverReason.ROR_CHO_SUCCESS.getValue() ||
                    roundInfoModel.getOverReason() == EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue()) {
                if (roundInfoModel.getResultType() == EQRoundResultType.ROT_TYPE_1.getValue()) {
                    // 一唱到底 或者是 是pk轮次，正常结束
                    songModel = roundInfoModel.getMusic();
                    if (!roundInfoModel.getbLightInfos().isEmpty()) {
                        baodeng = true;
                    } else {
                        baodeng = false;
                    }
                }
            }
            if (roundInfoModel.getsPkRoundInfoModels().size() == 2) {
                if (roundInfoModel.getsPkRoundInfoModels().get(0).getUserID() == MyUserInfoManager.getInstance().getUid()
                        && roundInfoModel.getsPkRoundInfoModels().get(0).getOverReason() == EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue()) {
                    // 第一轮我唱
                    songModel = roundInfoModel.getMusic();
                    if (!roundInfoModel.getsPkRoundInfoModels().get(0).getbLightInfos().isEmpty()) {
                        baodeng = true;
                    } else {
                        baodeng = false;
                    }
                } else if (roundInfoModel.getsPkRoundInfoModels().get(1).getUserID() == MyUserInfoManager.getInstance().getUid()
                        && roundInfoModel.getsPkRoundInfoModels().get(1).getOverReason() == EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue()) {
                    // 第一轮我唱
                    if (roundInfoModel.getMusic() != null) {
                        songModel = roundInfoModel.getMusic().getPkMusic();
                    }
                    if (!roundInfoModel.getsPkRoundInfoModels().get(1).getbLightInfos().isEmpty()) {
                        baodeng = true;
                    } else {
                        baodeng = false;
                    }
                }
            }
            if (songModel != null) {
                MyLog.d(TAG, "添加到待选作品");
                String fileName = String.format("wm_%s_%s.aac", mRoomData.getGameId(), roundInfoModel.getRoundSeq());
                String savePath = U.getAppInfoUtils().getFilePathInSubDir("WonderfulMoment", fileName);
                mRoomData.addWorksUploadModel(new WorksUploadModel(savePath, songModel, baodeng));
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
                        public void onProgressNotInUiThread(long currentSize, long totalSize) {

                        }

                        @Override
                        public void onSuccessNotInUiThread(String url) {
                            MyLog.w(TAG, "uploadRes1ForAi 上传成功 url=" + url);
                            sendUploadRequest(roundInfoModel, url);
                        }

                        @Override
                        public void onFailureNotInUiThread(String msg) {

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
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
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
        Params.save2Pref(ZqEngineKit.getInstance().getParams());
        if (mGrabSongResPresenter != null) {
            mGrabSongResPresenter.destroy();
        }
        if (!mRoomData.isHasExitGame()) {
            exitRoom("destroy");
        }
        cancelSyncGameStateTask();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        ZqEngineKit.getInstance().destroy("grabroom");
        mUiHandler.removeCallbacksAndMessages(null);
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter);
        if (mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer = null;
        } else {
            MyLog.d(TAG, "mExoPlayer == null ");
        }

        if (mZipUrlResourceManager != null) {
            mZipUrlResourceManager.cancelAllTask();
        }
        ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(String.valueOf(mRoomData.getGameId()));
        JiGuangPush.exitSkrRoomId(String.valueOf(mRoomData.getGameId()));
        MyLog.d(TAG, "destroy over");
    }

    /**
     * 告知我的的抢唱阶段结束了
     */
    public void sendMyGrabOver(String from) {
        MyLog.d(TAG, "上报我的抢唱结束 from=" + from);
        GrabRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel == null) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundInfoModel.getRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
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

        GrabRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel == null || !roundInfoModel.singBySelf()) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundInfoModel.getRoundSeq());
        if (roundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
            map.put("subRoundSeq", 0);
        } else if (roundInfoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            map.put("subRoundSeq", 1);
        }

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
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
     * 放弃演唱接口
     */
    public void giveUpSing(boolean ownerControl) {
        if (ownerControl) {
            MyLog.w(TAG, "房主结束小游戏");
            estimateOverTsThisRound();
            GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
            if (now == null || !mRoomData.isOwner()) {
                return;
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("roomID", mRoomData.getGameId());
            map.put("roundSeq", now.getRoundSeq());
            RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
            if (now.isFreeMicRound()) {
                ApiMethods.subscribe(mRoomServerApi.stopFreeMicroByOwner(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            mIGrabView.giveUpSuccess(now.getRoundSeq());
                            closeEngine();
                            MyLog.w(TAG, "房主结束自由麦成功 traceid is " + result.getTraceId());
                        } else {
                            MyLog.w(TAG, "房主结束自由麦成功 traceid is " + result.getTraceId());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "stopFreeMicroByOwner error " + e);
                    }
                }, this);
            } else {
                ApiMethods.subscribe(mRoomServerApi.stopMiniGameByOwner(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            mIGrabView.giveUpSuccess(now.getRoundSeq());
                            closeEngine();
                            MyLog.w(TAG, "房主结束小游戏成功 traceid is " + result.getTraceId());
                        } else {
                            MyLog.w(TAG, "房主结束小游戏成功 traceid is " + result.getTraceId());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "stopMiniGameByOwner error " + e);
                    }
                }, this);
            }

        } else {
            MyLog.w(TAG, "我放弃演唱");
            estimateOverTsThisRound();
            GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
            if (now == null || !now.singBySelf()) {
                return;
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("roomID", mRoomData.getGameId());
            map.put("roundSeq", now.getRoundSeq());
            if (now.getMusic() != null) {
                map.put("playType", now.getMusic().getPlayType());
            }
            if (now.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
                map.put("subRoundSeq", 0);
            } else if (now.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                map.put("subRoundSeq", 1);
            }
            RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
            ApiMethods.subscribe(mRoomServerApi.giveUpSing(body), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        mIGrabView.giveUpSuccess(now.getRoundSeq());
                        closeEngine();
                        MyLog.w(TAG, "放弃演唱上报成功 traceid is " + result.getTraceId());
                    } else {
                        MyLog.w(TAG, "放弃演唱上报失败 traceid is " + result.getTraceId());
                    }
                }

                @Override
                public void onError(Throwable e) {
                    MyLog.w(TAG, "giveUpSing" + " error " + e);
                }
            }, this);
        }
    }

    /**
     * 房主小游戏控场 开麦 闭麦
     *
     * @param mute true 开麦
     */
    public void miniOwnerMic(boolean mute) {
        MyLog.d(TAG, "miniOwnerMic" + " mute=" + mute);
        if (mute) {
            if (ZqEngineKit.getInstance().getParams().isAnchor()) {
                ZqEngineKit.getInstance().setClientRole(false);
            }
            ZqEngineKit.getInstance().muteLocalAudioStream(true);
        } else {
            if (!ZqEngineKit.getInstance().getParams().isAnchor()) {
                ZqEngineKit.getInstance().setClientRole(true);
            }
            ZqEngineKit.getInstance().muteLocalAudioStream(false);
        }

    }

    /**
     * 请求踢人
     *
     * @param userId 被踢人id
     */
    public void reqKickUser(int userId) {
        GrabRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel == null) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("kickUserID", userId);
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundInfoModel.getRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.reqKickUser(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    if (mRoomData.isOwner()) {
                        int kickTimes = result.getData().getIntValue("resKickUserTimes");
                        // TODO: 2019/5/8 更新剩余次数
                        mRoomData.setOwnerKickTimes(kickTimes);
                        if (kickTimes > 0) {
                            U.getToastUtil().showShort("踢人成功");
                        } else {
                            U.getToastUtil().showShort("发起踢人请求成功");
                        }
                    } else {
                        U.getToastUtil().showShort("发起踢人请求成功");
                    }
                    int coin = result.getData().getIntValue("coin");
                    mRoomData.setCoin(coin);
                } else {
                    U.getToastUtil().showShort("" + result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        }, this);
    }


    /**
     * 回应踢人请求
     *
     * @param isAgree      是否同意
     * @param userId       被踢人ID
     * @param sourceUserId 发起人ID
     */
    public void voteKickUser(boolean isAgree, int userId, int sourceUserId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("agree", isAgree);
        map.put("kickUserID", userId);
        map.put("roomID", mRoomData.getGameId());
        map.put("sourceUserID", sourceUserId);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.rspKickUser(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("同意踢人成功");
                } else {
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        }, this);

    }

    /**
     * 退出房间
     */
    public void exitRoom(String from) {
        MyLog.w(TAG, "exitRoom" + " from=" + from);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_EXIT, 5000);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.exitRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mUiHandler.removeMessages(MSG_ENSURE_EXIT);
                if (result.getErrno() == 0) {
                    mRoomData.setHasExitGame(true);
                    List<NumericDetailModel> models = JSON.parseArray(result.getData().getString("numericDetail"), NumericDetailModel.class);
                    if (models != null) {
                        // 得到结果
                        mRoomData.setGrabResultData(new GrabResultData(models));
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
                mUiHandler.removeMessages(MSG_ENSURE_EXIT);
                super.onNetworkError(errorType);
                mIGrabView.onGetGameResult(false);
            }
        });
    }

    /**
     * 切换房间
     */
    public void changeRoom() {
        if (mSwitchRooming) {
            U.getToastUtil().showShort("切换中");
            return;
        }
//        if(true){
//            stopGuide();
//            mRoomData.setRealRoundInfo(null);
//            mIGrabView.hideAllCardView();
//            joinRoomAndInit(false);
//            ZqEngineKit.getInstance().unbindAllRemoteVideo();
//            mRoomData.checkRoundInEachMode();
//            mIGrabView.onChangeRoomResult(true, null);
//            mIGrabView.dimissKickDialog();
//            return;
//        }
        mSwitchRooming = true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("tagID", mRoomData.getTagId());
        map.put("vars", RA.getVars());
        map.put("testList", RA.getTestList());
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.changeRoom(body), new ApiObserver<ApiResult>() {

            public void onNext(ApiResult result) {
                if (result.getErrno() == 0) {
                    EventBus.getDefault().post(new GrabSwitchRoomEvent());
                    JoinGrabRoomRspModel joinGrabRoomRspModel = JSON.parseObject(result.getData().toJSONString(), JoinGrabRoomRspModel.class);
                    onChangeRoomSuccess(joinGrabRoomRspModel);
                } else {
                    mIGrabView.onChangeRoomResult(false, result.getErrmsg());
                }
                mSwitchRooming = false;
            }

            @Override
            public void process(ApiResult obj) {

            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                mSwitchRooming = false;
                mIGrabView.onChangeRoomResult(false, "网络错误");
            }
        }, this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabChangeRoomEvent event) {
        onChangeRoomSuccess(event.mJoinGrabRoomRspModel);
    }

    public void onChangeRoomSuccess(JoinGrabRoomRspModel joinGrabRoomRspModel) {
        MyLog.d(TAG, "onChangeRoomSuccess" + " joinGrabRoomRspModel=" + joinGrabRoomRspModel);
        if (joinGrabRoomRspModel != null) {
            stopGuide();
            mRoomData.loadFromRsp(joinGrabRoomRspModel);
            joinRoomAndInit(false);
            mIGrabView.onChangeRoomResult(true, null);
            mRoomData.checkRoundInEachMode();
            mIGrabView.dimissKickDialog();
        }
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
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
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
                        MyLog.w(TAG, sSyncStateTaskInterval / 1000 + "秒钟的 syncGameTask 去更新状态了");
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
                    long syncStatusTimes = result.getData().getLongValue("syncStatusTimeMs");  //状态同步时的毫秒时间戳
                    long gameOverTimeMs = result.getData().getLongValue("gameOverTimeMs");  //游戏结束时间
                    GrabRoundInfoModel currentInfo = JSON.parseObject(result.getData().getString("currentRound"), GrabRoundInfoModel.class); //当前轮次信息
                    GrabRoundInfoModel nextInfo = JSON.parseObject(result.getData().getString("nextRound"), GrabRoundInfoModel.class); //当前轮次信息

                    String msg = "";
                    if (currentInfo != null) {
                        msg = "syncGameStatus成功了, currentRound 是 " + currentInfo;
                    } else {
                        msg = "syncGameStatus成功了, currentRound 是 null";
                    }

                    msg = msg + ",traceid is " + result.getTraceId();
                    MyLog.w(TAG, msg);

                    if (currentInfo == null) {
                        onGameOver("syncGameStatus", gameOverTimeMs);
                        return;
                    }

                    updatePlayerState(gameOverTimeMs, syncStatusTimes, currentInfo, gameID);
//                    fetchAcc(nextInfo);
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
    private synchronized void updatePlayerState(long gameOverTimeMs, long syncStatusTimes, GrabRoundInfoModel newRoundInfo, int gameId) {
        MyLog.w(TAG, "updatePlayerState" + " gameOverTimeMs=" + gameOverTimeMs + " syncStatusTimes=" + syncStatusTimes + " currentInfo=" + newRoundInfo.getRoundSeq() + ",gameId is " + gameId);
        if (!newRoundInfo.isContainInRoom()) {
            MyLog.w(TAG, "updatePlayerState" + ", 不再当前的游戏里， game id is " + gameId);
            if (mFirstKickOutTime == -1) {
                mFirstKickOutTime = System.currentTimeMillis();
            }
            mAbsenTimes++;
            if (System.currentTimeMillis() - mFirstKickOutTime > 15000 && mAbsenTimes > 10) {
                MyLog.w(TAG, "超过15秒 && 缺席次数是10以上，需要退出");
                exitRoom("updatePlayerState");
                return;
            }
        } else {
            mFirstKickOutTime = -1;
            mAbsenTimes = 0;
        }

        if (syncStatusTimes > mRoomData.getLastSyncTs()) {
            mRoomData.setLastSyncTs(syncStatusTimes);
        }

        if (gameOverTimeMs != 0) {
            if (gameOverTimeMs > mRoomData.getGameStartTs()) {
                MyLog.w(TAG, "gameOverTimeMs ！= 0 游戏应该结束了");
                // 游戏结束了
                onGameOver("sync", gameOverTimeMs);
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 startTs:" + mRoomData.getGameStartTs() + " overTs:" + gameOverTimeMs);
            }
        } else {
            // 没结束 current 不应该为null
            if (newRoundInfo != null) {
                // 服务下发的轮次已经大于当前轮次了，说明本地信息已经不对了，更新
                if (!mRoomData.hasGameBegin()) {
                    MyLog.w(TAG, "updatePlayerState 游戏未开始，但同步到轮次信息，更新");
                    // 轮次确实比当前的高，可以切换
                    mRoomData.setHasGameBegin(true);
                    mRoomData.setExpectRoundInfo(newRoundInfo);
                    mRoomData.checkRoundInEachMode();
                } else if (RoomDataUtils.roundSeqLarger(newRoundInfo, mRoomData.getExpectRoundInfo())) {
                    MyLog.w(TAG, "updatePlayerState sync 发现本地轮次信息滞后，更新");
                    // 轮次确实比当前的高，可以切换
                    mRoomData.setExpectRoundInfo(newRoundInfo);
                    mRoomData.checkRoundInEachMode();
                } else if (RoomDataUtils.isCurrentExpectingRound(newRoundInfo.getRoundSeq(), mRoomData)) {
                    /**
                     * 是当前轮次，最近状态就更新整个轮次
                     */
                    if (syncStatusTimes >= mRoomData.getLastSyncTs()) {
                        MyLog.w(TAG, "updatePlayerState sync 更新当前轮次");
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
        ZqEngineKit.getInstance().stopRecognize();
        mRoomData.setIsGameFinish(true);
        cancelSyncGameStateTask();
        // 游戏结束了,处理相应的ui逻辑
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mIGrabView.roundOver(event.lastRoundInfo, false, null);
            }
        });
        // 销毁引擎，减小成本
        ZqEngineKit.getInstance().destroy("grabroom");
        mUiHandler.postDelayed(new Runnable() {
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
        DebugLogView.println(TAG, "---轮次" + event.newRoundInfo.getRoundSeq() + "开始--- ");
        MyLog.d(TAG, "GrabRoundChangeEvent" + " event=" + event);
        // 轮次变化尝试更新头像
        estimateOverTsThisRound();
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
        closeEngine();
        tryStopRobotPlay();
        ZqEngineKit.getInstance().stopRecognize();
        GrabRoundInfoModel now = event.newRoundInfo;
        if (now != null) {
            EventBus.getDefault().post(new GrabPlaySeatUpdateEvent(now.getPlayUsers()));
            EventBus.getDefault().post(new GrabWaitSeatUpdateEvent(now.getWaitUsers()));
            int size = 0;
            for (GrabPlayerInfoModel playerInfoModel : now.getPlayUsers()) {
                if (playerInfoModel.getUserID() == 2) {
                    continue;
                }
                size++;
            }
            int finalSize = size;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mIGrabView.showPracticeFlag(finalSize <= 1);
                }
            });
        }

        if (now.getStatus() == EQRoundStatus.QRS_INTRO.getValue()) {
            //抢唱阶段，播抢唱卡片
            //TODO 再梳理整个流程
            if (event.lastRoundInfo != null && event.lastRoundInfo.getStatus() >= EQRoundStatus.QRS_SING.getValue()) {
                // 新一轮的抢唱阶段，得告诉上一轮演唱结束了啊，上一轮演唱结束卡片播完，才播歌曲卡片
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabView.roundOver(event.lastRoundInfo, true, now);
                    }
                });
                if (event.lastRoundInfo.singBySelf()) {
                    onSelfRoundOver(event.lastRoundInfo);
                }
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabView.grabBegin(now.getRoundSeq(), now.getMusic());
                    }
                });
            }
        } else if (now.isSingStatus()) {
            // 演唱阶段
            if (now.singBySelf()) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabView.singBySelf();
                    }
                });
                preOpWhenSelfRound();
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabView.singByOthers();
                    }
                });
                preOpWhenOtherRound(now.getUserID());
            }
        } else if (now.getStatus() == EQRoundStatus.QRS_END.getValue()) {
            MyLog.w(TAG, "GrabRoundChangeEvent 刚切换到该轮次就告诉我轮次结束？？？roundSeq:" + now.getRoundSeq());
            MyLog.w(TAG, "自动切换到下个轮次");
        }

        mUiHandler.post(() -> mIGrabView.hideManageTipView());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabWaitSeatUpdateEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        if (event.list != null && event.list.size() > 0) {
            mIGrabView.hideInviteTipView();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneJoinWaitSeatEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        mIGrabView.hideInviteTipView();
    }

    /**
     * 轮次内状态更新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GrabRoundStatusChangeEvent event) {
        MyLog.d(TAG, "GrabRoundStatusChangeEvent" + " event=" + event);
        estimateOverTsThisRound();
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
        GrabRoundInfoModel now = event.roundInfo;

        boolean needCloseEngine = true;
        if (mRoomData.isVideoRoom()) {
            if (now.isPKRound() && now.getsPkRoundInfoModels().size() >= 2) {
                if (now.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
                    // pk的第一轮
                    SPkRoundInfoModel pkRoundInfoModel2 = now.getsPkRoundInfoModels().get(1);
                    if (MyUserInfoManager.getInstance().getUid() == pkRoundInfoModel2.getUserID()) {
                        // 本人第二个唱
                        if (pkRoundInfoModel2.getOverReason() == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.getValue()
                                || pkRoundInfoModel2.getOverReason() == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
                            needCloseEngine = true;
                        } else {
                            needCloseEngine = false;
                        }
                    }
                } else if (now.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                    // pk第二轮
                    SPkRoundInfoModel pkRoundInfoModel1 = now.getsPkRoundInfoModels().get(0);
                    if (MyUserInfoManager.getInstance().getUid() == pkRoundInfoModel1.getUserID()) {
                        // 本人第二个唱
                        if (pkRoundInfoModel1.getOverReason() == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.getValue()
                                || pkRoundInfoModel1.getOverReason() == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
                            needCloseEngine = true;
                        } else {
                            needCloseEngine = false;
                        }
                    }
                }
            }
        }

        if (needCloseEngine) {
            closeEngine();
        } else {
            // pk第二轮，只把混音关了
            if (!ZqEngineKit.getInstance().getParams().isAnchor()) {
                ZqEngineKit.getInstance().setClientRole(true);
            }
            // 不发声
            ZqEngineKit.getInstance().muteLocalAudioStream(true);
            ZqEngineKit.getInstance().stopAudioMixing();
            ZqEngineKit.getInstance().stopAudioRecording();
        }
        tryStopRobotPlay();
        if (now.getStatus() == EQRoundStatus.QRS_INTRO.getValue()) {
            //抢唱阶段，播抢唱卡片
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mIGrabView.grabBegin(now.getRoundSeq(), now.getMusic());
                }
            });
        } else if (now.isSingStatus()) {
            // 演唱阶段
            if (now.singBySelf()) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabView.singBySelf();
                    }
                });
                preOpWhenSelfRound();
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabView.singByOthers();
                    }
                });
                preOpWhenOtherRound(now.getUserID());
            }
        }
    }

    private void closeEngine() {
        if (mRoomData.getGameId() > 0) {
            ZqEngineKit.getInstance().stopAudioMixing();
            ZqEngineKit.getInstance().stopAudioRecording();
            if (mRoomData.isSpeaking()) {
                MyLog.d(TAG, "closeEngine 正在抢麦说话，无需闭麦");
            } else {
//                if (mRoomData.isOwner()) {
//                    MyLog.d(TAG, "closeEngine 是房主 mute即可");
//                    ZqEngineKit.getInstance().muteLocalAudioStream(true);
//                } else {
                if (ZqEngineKit.getInstance().getParams().isAnchor()) {
                    ZqEngineKit.getInstance().setClientRole(false);
                }
//                }
            }
        }
    }

    /**
     * 引擎相关事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EngineEvent event) {
        if (event.getType() == EngineEvent.TYPE_USER_ROLE_CHANGE) {
            EngineEvent.RoleChangeInfo roleChangeInfo = event.getObj();
            if (roleChangeInfo.getNewRole() == 1) {
                GrabRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
                if (roundInfoModel != null && roundInfoModel.singBySelf()) {
                    MyLog.d(TAG, "演唱环节切换主播成功");
                    onChangeBroadcastSuccess();
                } else if (mRoomData.isSpeaking()) {
                    MyLog.d(TAG, "房主抢麦切换主播成功");
                }
            }
        } else if (event.getType() == EngineEvent.TYPE_USER_MUTE_AUDIO) {
//            UserStatus userStatus = event.getUserStatus();
//            if (userStatus != null) {
//                MyLog.d(TAG, "有人mute变化 uid=" + userStatus.getUserId());
//                if (userStatus.getUserId() == mRoomData.getOwnerId()) {
//                    if (mRoomData.isOwner()) {
//                        MyLog.d(TAG, "自己就是房主，忽略");
//                    } else {
//                        if (!userStatus.isAudioMute()) {
//                            MyLog.d(TAG, "房主解开mute，如果检测到房主说话，音量就衰减");
//                            weakVolume(1000);
//                        } else {
//                            MyLog.d(TAG, "房主mute了，恢复音量");
//                            mUiHandler.removeMessages(MSG_RECOVER_VOLUME);
//                            mUiHandler.sendEmptyMessage(MSG_RECOVER_VOLUME);
//                        }
//                    }
//                }
//            }
        } else if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
//            List<EngineEvent.UserVolumeInfo> list = event.getObj();
//            for (EngineEvent.UserVolumeInfo uv : list) {
//                //    MyLog.d(TAG, "UserVolumeInfo uv=" + uv);
//                if (uv != null) {
//                    int uid = uv.getUid();
//                    if (uid == 0) {
//                        uid = (int) MyUserInfoManager.getInstance().getUid();
//                    }
//                    if (mRoomData != null
//                            && uid == mRoomData.getOwnerId()
//                            && uv.getVolume() > 40
//                            && !mRoomData.isOwner()) {
//                        MyLog.d(TAG, "房主在说话");
//                        weakVolume(1000);
//                    }
//                }
//            }
        } else {
            // 可以考虑监听下房主的说话提示 做下容错
        }
    }

    /**
     * 弱化声音
     *
     * @param time
     */
    private void weakVolume(int time) {
        mUiHandler.removeMessages(MSG_RECOVER_VOLUME);
        mUiHandler.sendEmptyMessageDelayed(MSG_RECOVER_VOLUME, time);
        if (ZqEngineKit.getInstance().getParams().isAnchor()) {
            if (mEngineParamsTemp == null) {
                int audioVolume = ZqEngineKit.getInstance().getParams().getAudioMixingVolume();
                int recordVolume = ZqEngineKit.getInstance().getParams().getRecordingSignalVolume();
                mEngineParamsTemp = new EngineParamsTemp(audioVolume, recordVolume);
                ZqEngineKit.getInstance().adjustAudioMixingVolume((int) (audioVolume * 0.2), false);
                ZqEngineKit.getInstance().adjustRecordingSignalVolume((int) (recordVolume * 0.2), false);
            }
        } else {
            MyLog.d(TAG, "我不是主播，忽略");
        }
        if (mExoPlayer != null) {
            mExoPlayer.setVolume(mExoPlayer.getVolume() * 0.0f, false);
        }
    }

    /**
     * 成功切换为主播
     */
    private void onChangeBroadcastSuccess() {
        MyLog.d(TAG, "onChangeBroadcastSuccess 我的演唱环节");
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
                if (infoModel == null || !infoModel.singBySelf()) {
                    MyLog.d(TAG, "onChangeBroadcastSuccess,但已经不是你的轮次了，cancel");
                    return;
                }
                SongModel songModel = infoModel.getMusic();
                if (songModel == null) {
                    return;
                }
                if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                    songModel = songModel.getPkMusic();
                }
                if (songModel == null) {
                    return;
                }
                // 开始开始混伴奏，开始解除引擎mute
                File accFile = SongResUtils.getAccFileByUrl(songModel.getAcc());
                // midi不需要在这下，只要下好，native就会解析，打分就能恢复
                File midiFile = SongResUtils.getMIDIFileByUrl(songModel.getMidi());

                if (mRoomData.isAccEnable() && infoModel.isAccRound() || infoModel.isPKRound()) {
                    int songBeginTs = songModel.getBeginMs();
                    if (accFile != null && accFile.exists()) {
                        // 伴奏文件存在
                        ZqEngineKit.getInstance().startAudioMixing((int) MyUserInfoManager.getInstance().getUid(), accFile.getAbsolutePath()
                                , midiFile.getAbsolutePath(), songBeginTs, false, false, 1);
                    } else {
                        ZqEngineKit.getInstance().startAudioMixing((int) MyUserInfoManager.getInstance().getUid(), songModel.getAcc()
                                , midiFile.getAbsolutePath(), songBeginTs, false, false, 1);
                    }
                }
            }
        });
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
            wantSingerInfo.setWantSingType(event.getWantSingType());

            if (roundInfoModel.getStatus() == EQRoundStatus.QRS_INTRO.getValue()) {
                roundInfoModel.addGrabUid(true, wantSingerInfo);
            } else {
                MyLog.d(TAG, "但不是抢唱阶段，不发通知");
                roundInfoModel.addGrabUid(false, wantSingerInfo);
            }
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
        ensureInRcRoom();
        if (RoomDataUtils.isCurrentExpectingRound(event.getRoundSeq(), mRoomData)) {
            MyLog.w(TAG, "抢到唱歌权：userID " + event.getUserID() + ", roundInfo" + event.currentRound);
            GrabRoundInfoModel roundInfoModel = mRoomData.getExpectRoundInfo();
            roundInfoModel.setHasSing(true);
            roundInfoModel.setUserID(event.getUserID());
            roundInfoModel.tryUpdateRoundInfoModel(event.getCurrentRound(), true);
            // 加入抢唱状态后，不能用这个 updateStatus了
            //roundInfoModel.updateStatus(true, EQRoundStatus.QRS_SING.getValue());
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
            //roundInfoModel.updateStatus(true, EQRoundStatus.QRS_SING.getValue());
            MLightInfoModel noPassingInfo = new MLightInfoModel();
            noPassingInfo.setUserID(event.userID);
            roundInfoModel.addLightOffUid(true, noPassingInfo);
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
//            roundInfoModel.updateStatus(true, EQRoundStatus.QRS_SING.getValue());
            BLightInfoModel noPassingInfo = new BLightInfoModel();
            noPassingInfo.setUserID(event.userID);
            roundInfoModel.addLightBurstUid(true, noPassingInfo);
        } else {
            MyLog.w(TAG, "有人爆灯了,但是不是这个轮次：userID " + event.userID + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.getExpectRoundInfo());
        }
    }

    /**
     * 这里来伪装弹幕的好处，是sych下来的爆灭灯变化也会触发这个时间
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightOffEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        if (event.roundInfo.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
            if (event.roundInfo.getsPkRoundInfoModels().size() > 0) {
                pretendLightMsgComment(event.roundInfo.getsPkRoundInfoModels().get(0).getUserID(), event.uid, false);
            }
        } else if (event.roundInfo.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            if (event.roundInfo.getsPkRoundInfoModels().size() > 1) {
                pretendLightMsgComment(event.roundInfo.getsPkRoundInfoModels().get(1).getUserID(), event.uid, false);
            }
        } else {
            pretendLightMsgComment(event.roundInfo.getUserID(), event.uid, false);
        }
    }

    /**
     * 同上
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightBurstEvent event) {
        if (event.roundInfo.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()) {
            if (event.roundInfo.getsPkRoundInfoModels().size() > 0) {
                pretendLightMsgComment(event.roundInfo.getsPkRoundInfoModels().get(0).getUserID(), event.uid, true);
            }
        } else if (event.roundInfo.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            if (event.roundInfo.getsPkRoundInfoModels().size() > 1) {
                pretendLightMsgComment(event.roundInfo.getsPkRoundInfoModels().get(1).getUserID(), event.uid, true);
            }
        } else {
            pretendLightMsgComment(event.roundInfo.getUserID(), event.uid, true);
        }

        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            if (now.singBySelf()) {
                StatisticsAdapter.recordCountEvent("grab", "game_getlike", null);
            }
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
        MyLog.d(TAG, "pretendLightMsgComment" + " singerId=" + singerModel + " uid=" + playerInfoModel + " isBao=" + isBao);
        if (singerModel != null && playerInfoModel != null) {
            boolean isChorus = false;
            boolean isMiniGame = false;
            GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
            if (now != null) {
                isMiniGame = now.isMiniGameRound();
                isChorus = now.isChorusRound();
            }
            CommentLightModel commentLightModel = new CommentLightModel(mRoomData.getGameType(), playerInfoModel, singerModel, isBao, isChorus, isMiniGame);
            EventBus.getDefault().post(new PretendCommentMsgEvent(commentLightModel));
        }
    }

    /**
     * 有人加入房间
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QJoinNoticeEvent event) {
        boolean canAdd = false;
        GrabPlayerInfoModel playerInfoModel = event.infoModel;
        MyLog.d(TAG, "有人加入房间,id=" + playerInfoModel.getUserID() + " name=" + playerInfoModel.getUserInfo().getNicknameRemark() + " role=" + playerInfoModel.getRole() + " roundSeq=" + event.roundSeq);
        if (playerInfoModel != null && playerInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
            /**
             * 自己加入房间不提示
             * 因为会有一个bug，
             * 场景如下，A中途进入房间，返回的轮次信息里waitlist里没有A，但是会下发一个 A 以观众身份加入房间的push，导致提示语重复
             */
            canAdd = false;
        } else if (RoomDataUtils.isCurrentExpectingRound(event.roundSeq, mRoomData)) {
            GrabRoundInfoModel grabRoundInfoModel = mRoomData.getExpectRoundInfo();
            if (grabRoundInfoModel != null && grabRoundInfoModel.addUser(true, playerInfoModel)) {
                canAdd = true;
            }
        } else if (!mRoomData.hasGameBegin()) {
            canAdd = true;
        } else {
            MyLog.w(TAG, "有人加入房间了,但是不是这个轮次：userID " + event.infoModel.getUserID() + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.getExpectRoundInfo());
        }
        //TODO 如果加入房间提示有遗漏，可以考虑接受 SomeOne 事件，一担用户有变化都会回调
        if (canAdd) {
            pretendEnterRoom(playerInfoModel);
        }
    }

    /**
     * 某人退出游戏
     *
     * @param event
     */
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

    /**
     * 某人离开选手席
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLeavePlaySeatEvent event) {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null) {
            for (ChorusRoundInfoModel chorusRoundInfoModel : grabRoundInfoModel.getChorusRoundInfoModels()) {
                if (event.mPlayerInfoModel != null) {
                    if (chorusRoundInfoModel.getUserID() == event.mPlayerInfoModel.getUserID()) {
                        chorusRoundInfoModel.userExit();
                        pretendGiveUp(mRoomData.getUserInfo(event.mPlayerInfoModel.getUserID()));
                    }
                }
            }
        }
    }


    /**
     * 金币变化
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QCoinChangeEvent event) {
        if (event.userID == MyUserInfoManager.getInstance().getUid()) {
            if (event.remainCoin > 0) {
                mRoomData.setCoin(event.remainCoin);
            }
            if (event.reason.getValue() == 1) {
                pretendSystemMsg("你获取了" + event.changeCoin + "金币奖励");
            }
        }
    }

    /**
     * 合唱某人放弃了演唱
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QChoGiveUpEvent event) {
        MyLog.d(TAG, "QChoGiveUpEvent" + " event=" + event);
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            if (now.getRoundSeq() == event.roundSeq) {
                now.giveUpInChorus(event.userID);
                List<ChorusRoundInfoModel> list = now.getChorusRoundInfoModels();
                if (list != null) {
                    for (ChorusRoundInfoModel chorusRoundInfoModel : list) {
                        if (chorusRoundInfoModel.getUserID() == event.userID) {
                            UserInfoModel userInfoModel = mRoomData.getUserInfo(event.userID);
                            if (event.userID == MyUserInfoManager.getInstance().getUid()) {
                                // 是我自己不唱了
                                U.getToastUtil().showShort("你已经退出合唱");
                            } else if (now.singBySelf()) {
                                // 是我的对手不唱了
                                if (userInfoModel != null) {
                                    U.getToastUtil().showShort(userInfoModel.getNicknameRemark() + "已经退出合唱");
                                }
                            } else {
                                // 观众视角，有人不唱了
                                if (userInfoModel != null) {
                                    U.getToastUtil().showShort(userInfoModel.getNicknameRemark() + "已经退出合唱");
                                }
                            }
                            pretendGiveUp(userInfoModel);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 轮次结束
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QPkInnerRoundOverEvent event) {
        MyLog.d(TAG, "QPkInnerRoundOverEvent" + " event=" + event);
        if (RoomDataUtils.isCurrentRunningRound(event.mRoundInfoModel.getRoundSeq(), mRoomData)) {
            GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
            if (now != null) {
                now.tryUpdateRoundInfoModel(event.mRoundInfoModel, true);
//                // PK 第一个人不唱了 加个弹幕
                if (now.getsPkRoundInfoModels().size() > 0) {
                    if (now.getsPkRoundInfoModels().get(0).getOverReason() == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
                        UserInfoModel userInfoModel = mRoomData.getUserInfo(now.getsPkRoundInfoModels().get(0).getUserID());
                        pretendGiveUp(userInfoModel);
                    }
                }
                if (now.getsPkRoundInfoModels().size() > 1) {
                    if (now.getsPkRoundInfoModels().get(1).getOverReason() == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
                        UserInfoModel userInfoModel = mRoomData.getUserInfo(now.getsPkRoundInfoModels().get(1).getUserID());
                        pretendGiveUp(userInfoModel);
                    }
                }
            }
        }
    }

    private void pretendGiveUp(UserInfoModel userInfoModel) {
        if (userInfoModel != null) {
            CommentTextModel commentModel = new CommentTextModel();
            commentModel.setUserId(userInfoModel.getUserId());
            commentModel.setAvatar(userInfoModel.getAvatar());
            commentModel.setUserName(userInfoModel.getNicknameRemark());
            commentModel.setAvatarColor(CommentModel.AVATAR_COLOR);
            SpannableStringBuilder stringBuilder;
            SpanUtils spanUtils = new SpanUtils()
                    .append(userInfoModel.getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("不唱了").setForegroundColor(CommentModel.GRAB_TEXT_COLOR);
            stringBuilder = spanUtils.create();
            commentModel.setStringBuilder(stringBuilder);
            EventBus.getDefault().post(new PretendCommentMsgEvent(commentModel));
        }
    }

    private void pretendEnterRoom(GrabPlayerInfoModel playerInfoModel) {
        CommentTextModel commentModel = new CommentTextModel();
        commentModel.setUserId(playerInfoModel.getUserInfo().getUserId());
        commentModel.setAvatar(playerInfoModel.getUserInfo().getAvatar());
        commentModel.setUserName(playerInfoModel.getUserInfo().getNicknameRemark());
        commentModel.setAvatarColor(CommentModel.AVATAR_COLOR);
        SpannableStringBuilder stringBuilder;
        if (playerInfoModel.getUserInfo().getUserId() == UserAccountManager.SYSTEM_GRAB_ID) {
            stringBuilder = new SpanUtils()
                    .append(playerInfoModel.getUserInfo().getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("我是撕歌最傲娇小助手多音，来和你们一起唱歌卖萌~").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .create();
        } else {
            SpanUtils spanUtils = new SpanUtils()
                    .append(playerInfoModel.getUserInfo().getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("加入了房间").setForegroundColor(CommentModel.GRAB_TEXT_COLOR);
            if (BuildConfig.DEBUG) {
                spanUtils.append(" 角色为" + playerInfoModel.getRole())
                        .append(" 在线状态为" + playerInfoModel.isOnline());
            }
            stringBuilder = spanUtils.create();
        }
        commentModel.setStringBuilder(stringBuilder);
        EventBus.getDefault().post(new PretendCommentMsgEvent(commentModel));
    }

    /**
     * 轮次变化
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QRoundOverMsgEvent event) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push event:" + event);
        ensureInRcRoom();
//        if (mRoomData.getLastSyncTs() >= event.getInfo().getTimeMs()) {
//            MyLog.w(TAG, "但是是个旧数据");
//            return;
//        }
        if (RoomDataUtils.isCurrentRunningRound(event.getCurrentRound().getRoundSeq(), mRoomData)) {
            // 如果是当前轮次
            mRoomData.getRealRoundInfo().tryUpdateRoundInfoModel(event.currentRound, true);
            if (event.myCoin >= 0) {
                mRoomData.setCoin(event.myCoin);
            }
            if (event.totalRoundNum > 0) {
                mRoomData.getGrabConfigModel().setTotalGameRoundSeq(event.totalRoundNum);
            }

            //非PK和合唱轮次 加上不唱了弹幕 产品又让加回来了
            GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
            if (!infoModel.isPKRound() && !infoModel.isChorusRound()) {
                if (infoModel.getOverReason() == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
                    pretendGiveUp(mRoomData.getUserInfo(infoModel.getUserID()));
                }
            }
        }
        if (!mRoomData.hasGameBegin()) {
            MyLog.w(TAG, "收到 QRoundOverMsgEvent，游戏未开始？将游戏设置为开始状态");
            mRoomData.setHasGameBegin(true);
            mRoomData.setExpectRoundInfo(event.nextRound);
            mRoomData.checkRoundInEachMode();
        } else if (RoomDataUtils.roundSeqLarger(event.nextRound, mRoomData.getExpectRoundInfo())) {
            // 游戏轮次结束
            // 轮次确实比当前的高，可以切换
            MyLog.w(TAG, "轮次确实比当前的高，可以切换");
            mRoomData.setExpectRoundInfo(event.nextRound);
            mRoomData.checkRoundInEachMode();
        } else {
            MyLog.w(TAG, "轮次比当前轮次还小,直接忽略 当前轮次:" + mRoomData.getExpectRoundInfo().getRoundSeq()
                    + " push轮次:" + event.currentRound.getRoundSeq());
        }
    }

    /**
     * 游戏结束事件
     *
     * @param event
     */
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
        onGameOver("QRoundAndGameOverMsgEvent", event.roundOverTimeMs);
        if (event.mOverReason == EQGameOverReason.GOR_OWNER_EXIT) {
            MyLog.w(TAG, "房主离开了游戏，房间解散");
            U.getToastUtil().showLong("房主离开了游戏，房间解散");
        }
    }

    /**
     * 同步
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QSyncStatusMsgEvent event) {
        if (event.getInfo().getRoomID() != mRoomData.getGameId()) {
            MyLog.w(TAG, "onEvent QSyncStatusMsgEvent， current roomid is " + mRoomData.getGameId() + ", event.getInfo().getRoomID() is " + event.getInfo().getRoomID());
            return;
        }

        ensureInRcRoom();
        MyLog.w(TAG, "收到服务器 sync push更新状态,event.currentRound是" + event.getCurrentRound().getRoundSeq() + ", timeMs 是" + event.info.getTimeMs());
        // 延迟10秒sync ，一旦启动sync 间隔 5秒 sync 一次
        startSyncGameStateTask(sSyncStateTaskInterval * 2);
        updatePlayerState(event.getGameOverTimeMs(), event.getSyncStatusTimeMs(), event.getCurrentRound(), event.getInfo().getRoomID());
//        fetchAcc(event.getNextRound());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateMeiliEvent event) {
        // TODO: 2019-06-05 暂时只对pk做个特殊处理
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null && grabRoundInfoModel.isPKRound()) {
            for (SPkRoundInfoModel roundInfoModel : grabRoundInfoModel.getsPkRoundInfoModels()) {
                if (roundInfoModel.getUserID() == event.userID) {
                    roundInfoModel.setMeiliTotal(event.value);
                    return;
                }
            }
        }
    }

    private void onGameOver(String from, long gameOverTs) {
        MyLog.w(TAG, "游戏结束 gameOverTs=" + gameOverTs + " from:" + from);
        if (gameOverTs > mRoomData.getGameStartTs() && gameOverTs > mRoomData.getGameOverTs()) {
            cancelSyncGameStateTask();
            mRoomData.setGameOverTs(gameOverTs);
            mRoomData.setExpectRoundInfo(null);
            mRoomData.checkRoundInEachMode();
        } else {
            MyLog.w(TAG, "游戏结束 gameOverTs 不合法，取消");
        }
    }

    /**
     * 房主 被告知游戏开始
     *
     * @param event
     */
    @Subscribe
    public void onEvent(QGameBeginEvent event) {
        MyLog.d(TAG, "onEvent QGameBeginEvent !!收到游戏开始的push " + event);
        if (mRoomData.hasGameBegin()) {
            MyLog.d(TAG, "onEvent 游戏开始的标记为已经为true" + " event=" + event);
            mRoomData.setGrabConfigModel(event.mGrabConfigModel);
        } else {
            mRoomData.setHasGameBegin(true);
            mRoomData.setGrabConfigModel(event.mGrabConfigModel);
            mRoomData.setExpectRoundInfo(event.mInfoModel);
            mRoomData.checkRoundInEachMode();
        }
        if (mRoomData.hasGameBegin()) {
            startSyncGameStateTask(sSyncStateTaskInterval);
        } else {
            cancelSyncGameStateTask();
        }
        ensureInRcRoom();
    }

    @Subscribe
    public void onEvent(QChangeMusicTagEvent event) {
        MyLog.d(TAG, "onEvent QChangeMusicTagEvent !!切换专场 " + event);
        if (mRoomData.getGameId() == event.info.getRoomID()) {
            pretendSystemMsg(String.format("房主已将歌单切换为 %s 专场", event.getTagName()));
        }
    }

    @Subscribe
    public void onEvent(QChangeRoomNameEvent event) {
        MyLog.d(TAG, "onEvent QChangeRoomNameEvent !!改变房间名 " + event);
        if (mRoomData.getGameId() == event.info.getRoomID()) {
            pretendRoomNameSystemMsg(event.newName, CommentSysModel.TYPE_MODIF_ROOM_NAME);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        swapGame(!event.foreground, event.foreground);
        if (event.foreground) {
            muteAllRemoteAudioStreams(mRoomData.isMute(), false);
            if (mRoomData.isVideoRoom()) {
                ZqEngineKit.getInstance().muteLocalVideoStream(false);
            }
        } else {
            muteAllRemoteAudioStreams(true, false);
            if (mRoomData.isVideoRoom()) {
                if (ZqEngineKit.getInstance().getParams().isAnchor()) {
                    // 我是主播
                    ZqEngineKit.getInstance().muteLocalVideoStream(true);
                }
            }
        }
    }

    /**
     * 录制小游戏事件，防止录进去背景音
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BeginRecordCustomGameEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        if (event.getBegin()) {
            muteAllRemoteAudioStreams(true, false);
        } else {
            muteAllRemoteAudioStreams(mRoomData.isMute(), false);
        }
    }

    private int estimateOverTsThisRound() {
        int pt = RoomDataUtils.estimateTs2End(mRoomData, mRoomData.getRealRoundInfo());
        MyLog.w(TAG, "估算出距离本轮结束还有" + pt + "ms");
        return pt;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RoomNameChangeEvent event) {
        MyLog.w(TAG, "onEvent" + " event=" + event);
        mRoomData.setRoomName(event.getMRoomName());
    }


    /*打分相关*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MachineScoreEvent event) {
        //收到其他人的机器打分消息，比较复杂，暂时简单点，轮次正确就直接展示
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel != null && infoModel.singByUserId(event.userId)) {
            mIGrabView.updateScrollBarProgress(event.score, event.lineNum);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LyricAndAccMatchManager.ScoreResultEvent event) {
        int line = event.line;
        int acrScore = event.acrScore;
        int melpScore = event.melpScore;
        String from = event.from;
        if (acrScore > melpScore) {
            processScore(acrScore, line);
        } else {
            processScore(melpScore, line);
        }
    }

    void processScore(int score, int line) {
        if (score < 0) {
            return;
        }
        MyLog.d(TAG, "onEvent" + " 得分=" + score);
        MachineScoreItem machineScoreItem = new MachineScoreItem();
        machineScoreItem.setScore(score);
        // 这有时是个耗时操作
//        long ts = ZqEngineKit.getInstance().getAudioMixingCurrentPosition();
        long ts = -1;
        machineScoreItem.setTs(ts);
        machineScoreItem.setNo(line);
        // 打分信息传输给其他人
        sendScoreToOthers(machineScoreItem);
        if (mRobotScoreHelper != null) {
            mRobotScoreHelper.add(machineScoreItem);
        }
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mIGrabView.updateScrollBarProgress(score, mRoomData.getSongLineNum());
            }
        });
        //打分传给服务器
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            /**
             * pk 与 普通 都发送
             */
            if (now.isPKRound() || now.isNormalRound()) {
                sendScoreToServer(score, line);
            }
        }
    }

    /**
     * 将自己的分数传给其他人
     *
     * @param machineScoreItem
     */
    private void sendScoreToOthers(MachineScoreItem machineScoreItem) {
        // 后续加个优化，如果房间里两人都是机器人就不加了
        IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
        if (msgService != null) {
            long ts = System.currentTimeMillis();
            UserInfo senderInfo = new UserInfo.Builder()
                    .setUserID((int) MyUserInfoManager.getInstance().getUid())
                    .setNickName(MyUserInfoManager.getInstance().getNickName())
                    .setAvatar(MyUserInfoManager.getInstance().getAvatar())
                    .setSex(ESex.fromValue(MyUserInfoManager.getInstance().getSex()))
                    .setDescription("")
                    .setIsSystem(false)
                    .build();

            GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
            if (now != null && now.getMusic() != null) {
                RoomMsg roomMsg = new RoomMsg.Builder()
                        .setTimeMs(ts)
                        .setMsgType(ERoomMsgType.RM_ROUND_MACHINE_SCORE)
                        .setRoomID(mRoomData.getGameId())
                        .setNo(ts)
                        .setPosType(EMsgPosType.EPT_UNKNOWN)
                        .setSender(senderInfo)
                        .setMachineScore(new MachineScore.Builder()
                                .setUserID((int) MyUserInfoManager.getInstance().getUid())
                                .setNo(machineScoreItem.getNo())
                                .setScore(machineScoreItem.getScore())
                                .setItemID(now.getMusic().getItemID())
                                .setLineNum(mRoomData.getSongLineNum())
                                .build()
                        )
                        .build();
                String contnet = U.getBase64Utils().encode(roomMsg.toByteArray());
                msgService.sendChatRoomMessage(String.valueOf(mRoomData.getGameId()), CustomMsgType.MSG_TYPE_ROOM, contnet, null);
            }
        }
    }


    /**
     * 单句打分上报,只在pk模式上报
     *
     * @param score
     * @param line
     */
    public void sendScoreToServer(int score, int line) {
        //score = (int) (Math.mRandom()*100);
        HashMap<String, Object> map = new HashMap<>();
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return;
        }
        map.put("userID", MyUserInfoManager.getInstance().getUid());

        int itemID = 0;
        if (infoModel.getMusic() != null) {
            itemID = infoModel.getMusic().getItemID();
            if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                SongModel pkSong = infoModel.getMusic().getPkMusic();
                if (pkSong != null) {
                    itemID = pkSong.getItemID();
                }
            }
        }

        map.put("itemID", itemID);
        map.put("score", score);
        map.put("no", line);
        map.put("gameID", mRoomData.getGameId());
        map.put("mainLevel", 0);
        map.put("singSecond", 0);
        int roundSeq = infoModel.getRoundSeq();
        map.put("roundSeq", roundSeq);
        long nowTs = System.currentTimeMillis();
        map.put("timeMs", nowTs);


        StringBuilder sb = new StringBuilder();
        sb.append("skrer")
                .append("|").append(MyUserInfoManager.getInstance().getUid())
                .append("|").append(itemID)
                .append("|").append(score)
                .append("|").append(line)
                .append("|").append(mRoomData.getGameId())
                .append("|").append(0)
                .append("|").append(0)
                .append("|").append(roundSeq)
                .append("|").append(nowTs);
        map.put("sign", U.getMD5Utils().MD5_32(sb.toString()));
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.sendPkPerSegmentResult(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2018/12/13  当前postman返回的为空 待补充
                    MyLog.w(TAG, "单句打分上报成功");
                } else {
                    MyLog.w(TAG, "单句打分上报失败" + result.getErrno());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(e);
            }
        }, this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QKickUserReqEvent qKickUserReqEvent) {
        MyLog.d(TAG, "onEvent" + " qKickUserReqEvent=" + qKickUserReqEvent);
        // 踢人的请求
        mIGrabView.showKickVoteDialog(qKickUserReqEvent.kickUserID, qKickUserReqEvent.sourceUserID);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GiftPresentEvent giftPresentEvent) {
        MyLog.d(TAG, "onEvent" + " giftPresentEvent=" + giftPresentEvent);
        EventBus.getDefault().post(new GiftBrushMsgEvent(giftPresentEvent.mGPrensentGiftMsgModel));

        if (giftPresentEvent.mGPrensentGiftMsgModel.getPropertyModelList() != null) {
            for (GPrensentGiftMsgModel.PropertyModel property : giftPresentEvent.mGPrensentGiftMsgModel.getPropertyModelList()) {
                if (property.userID == MyUserInfoManager.getInstance().getUid()) {
                    if (property.coinBalance != -1) {
                        UpdateCoinEvent.sendEvent((int) property.coinBalance, property.lastChangeMs);
                    }
                    if (property.hongZuanBalance != -1) {
                        mRoomData.setHzCount(property.hongZuanBalance, property.lastChangeMs);
                    }
                }
                if (property.curRoundSeqMeiliTotal > 0) {
                    // 他人的只关心魅力值的变化
                    EventBus.getDefault().post(new UpdateMeiliEvent(property.userID, (int) property.curRoundSeqMeiliTotal, property.lastChangeMs));
                }
            }
        }

        if (giftPresentEvent.mGPrensentGiftMsgModel.getReceiveUserInfo().getUserId() == MyUserInfoManager.getInstance().getUid()) {
            if (giftPresentEvent.mGPrensentGiftMsgModel.getGiftInfo().getPrice() <= 0) {
                StatisticsAdapter.recordCountEvent("grab", "game_getflower", null);
            } else {
                StatisticsAdapter.recordCountEvent("grab", "game_getgift", null);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QKickUserResultEvent qKickUserResultEvent) {
        MyLog.d(TAG, "onEvent" + " qKickUserResultEvent=" + qKickUserResultEvent);
        // 踢人的结果
        if (qKickUserResultEvent.kickUserID == MyUserInfoManager.getInstance().getUid()) {
            // 自己被踢出去
            if (qKickUserResultEvent.isKickSuccess) {
                if (mRoomData.getOwnerId() == qKickUserResultEvent.sourceUserID) {
                    mIGrabView.kickBySomeOne(true);
                } else {
                    mIGrabView.kickBySomeOne(false);
                }
            }
        } else {
            // 别人被踢出去
            mIGrabView.dimissKickDialog();
            pretendSystemMsg(qKickUserResultEvent.kickResultContent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSpeakingControlEvent event) {
        mRoomData.setSpeaking(event.speaking);
        // 踢人的结果
        if (event.speaking) {
            ZqEngineKit.getInstance().muteLocalAudioStream(false);
            int v = ZqEngineKit.getInstance().getParams().getPlaybackSignalVolume() / 4;
            ZqEngineKit.getInstance().adjustPlaybackSignalVolume(v, false);
            if (mExoPlayer != null) {
                mExoPlayer.setVolume(mExoPlayer.getVolume() * 0.0f, false);
            }
        } else {
            // 要闭麦
            GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
            if (infoModel != null && infoModel.singBySelf()) {
                MyLog.d(TAG, "自己的轮次，无需闭麦");
            } else {
                ZqEngineKit.getInstance().muteLocalAudioStream(true);
            }
            int v = ZqEngineKit.getInstance().getParams().getPlaybackSignalVolume();
            ZqEngineKit.getInstance().adjustPlaybackSignalVolume(v, false);
            if (mExoPlayer != null) {
                mExoPlayer.setVolume(mExoPlayer.getVolume(), false);
            }
        }
    }

    public static class EngineParamsTemp {
        int audioVolume;
        int recordVolume;

        public EngineParamsTemp(int audioVolume, int recordVolume) {
            this.audioVolume = audioVolume;
            this.recordVolume = recordVolume;
        }
    }
}
