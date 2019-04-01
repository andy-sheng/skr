package com.module.playways.grab.room.presenter;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.engine.ScoreConfig;
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
import com.common.utils.SongResUtils;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.component.busilib.SkrConfig;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.arccloud.SongInfo;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.CustomMsgType;
import com.module.msg.IMsgService;
import com.module.playways.event.GrabChangeRoomEvent;
import com.module.playways.grab.room.GrabResultData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.event.GrabGameOverEvent;
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent;
import com.module.playways.grab.room.event.GrabSwitchRoomEvent;
import com.module.playways.grab.room.event.GrabWaitSeatUpdateEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.grab.room.inter.IGrabView;
import com.module.playways.grab.room.model.BLightInfoModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.grab.room.model.GrabSkrResourceModel;
import com.module.playways.grab.room.model.MLightInfoModel;
import com.module.playways.grab.room.model.WantSingerInfo;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.rank.msg.event.MachineScoreEvent;

import com.module.playways.rank.msg.event.QCoinChangeEvent;

import com.module.playways.rank.msg.event.QChangeMusicTagEvent;

import com.module.playways.rank.msg.event.QExitGameMsgEvent;
import com.module.playways.rank.msg.event.QGameBeginEvent;
import com.module.playways.rank.msg.event.QGetSingChanceMsgEvent;
import com.module.playways.rank.msg.event.QJoinNoticeEvent;
import com.module.playways.rank.msg.event.QKickUserReqEvent;
import com.module.playways.rank.msg.event.QKickUserResultEvent;
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
import com.module.playways.rank.room.model.score.ScoreResultModel;

import com.module.playways.rank.room.score.MachineScoreItem;
import com.module.playways.rank.room.score.RobotScoreHelper;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.BuildConfig;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.EMsgPosType;
import com.zq.live.proto.Room.EQGameOverReason;
import com.zq.live.proto.Room.EQRoundOverReason;
import com.zq.live.proto.Room.EQRoundResultType;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.MachineScore;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.utils.ZipUrlResourceManager;

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

    static final int MSG_SHOW_SCORE_EVENT = 32;

    static final int MSG_ENSURE_SWITCH_BROADCAST_SUCCESS = 21; // 确保用户切换成主播成功，防止引擎不回调的保护

    GrabRoomData mRoomData;

    GrabRoomServerApi mRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);

    HandlerTaskTimer mSyncGameStateTask;

    IGrabView mIGrabView;

    RobotScoreHelper mRobotScoreHelper;

    boolean mDestroyed = false;

    ExoPlayer mExoPlayer;

    boolean mSwitchRooming = false;

    ZipUrlResourceManager mZipUrlResourceManager;

    int mLastLineNum = -1;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ENSURE_IN_RC_ROOM:
                    MyLog.d(TAG, "handleMessage 长时间没收到push，重新进入融云房间容错");
                    ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(mRoomData.getGameId() + "");
                    joinRcRoom(0);
                    break;
                case MSG_ROBOT_SING_BEGIN:
                    robotSingBegin();
                    break;
                case MSG_ENSURE_SWITCH_BROADCAST_SUCCESS:
                    onChangeBroadcastSuccess();
                    break;
                default:
                    int lineNo = (msg.what - MSG_SHOW_SCORE_EVENT) / 100;
                    MyLog.d(TAG, "handleMessage" + " lineNo=" + lineNo);
                    if (lineNo > mLastLineNum) {
                        int score = EngineManager.getInstance().getLineScore1();
                        MyLog.d(TAG, "handleMessage acr超时 本地获取得分:" + score);
                        processScore(score, lineNo);
                    }
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
            if (first) {
                Params params = Params.getFromPref();
//            params.setStyleEnum(Params.AudioEffect.none);
                params.setScene(Params.Scene.grab);
                EngineManager.getInstance().init("grabroom", params);
            }
            EngineManager.getInstance().joinRoom(String.valueOf(mRoomData.getGameId()), (int) UserAccountManager.getInstance().getUuidAsLong(), false, mRoomData.getAgoraToken());
            // 不发送本地音频
            EngineManager.getInstance().muteLocalAudioStream(true);
        }
        joinRcRoom(0);
        if (mRoomData.getGameId() > 0) {
            pretenSystemMsg("撕歌倡导文明游戏，遇到恶意玩家，可以发起投票将ta踢出房间哦～");
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
            ModuleServiceManager.getInstance().getMsgService().joinChatRoom(String.valueOf(mRoomData.getGameId()), new ICallback() {
                @Override
                public void onSucess(Object obj) {
                    MyLog.d(TAG, "加入融云房间成功");
                }

                @Override
                public void onFailed(Object obj, int errcode, String message) {
                    MyLog.d(TAG, "加入融云房间失败");
                    joinRcRoom(deep + 1);
                }
            });
        }
    }

    private void ensureInRcRoom() {
        mUiHandler.removeMessages(MSG_ENSURE_IN_RC_ROOM);
        mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_IN_RC_ROOM, 60 * 1000);
    }

    private void pretenSystemMsg(String text) {
        CommentModel commentModel = new CommentModel();
        commentModel.setCommentType(CommentModel.TYPE_TRICK);
        commentModel.setUserId(UserAccountManager.SYSTEM_ID);
        commentModel.setAvatar(UserAccountManager.SYSTEM_AVATAR);
        commentModel.setUserName("系统消息");
        commentModel.setAvatarColor(Color.WHITE);
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append(text).setForegroundColor(CommentModel.TEXT_RED)
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
                mExoPlayer = new ExoPlayer();
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
     * 如果确定是自己唱了,预先可以做的操作
     */
    void preOpWhenSelfRound() {
        if (mRoomData.isAccEnable()) {
            mLastLineNum = -1;
            // 1. 开启伴奏的，预先下载 melp 资源
            GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
            if (now != null) {
                File midiFile = SongResUtils.getMIDIFileByUrl(now.getMusic().getMidi());
                if (midiFile != null && !midiFile.exists()) {
                    U.getHttpUtils().downloadFileAsync(now.getMusic().getMidi(), midiFile, null);
                }
            }
        }
        EngineManager.getInstance().setClientRole(true);
        if (mRoomData.isAccEnable()) {
            // 如果需要播放伴奏，一定要在角色切换成功才能播
            mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
            mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS, 2000);
        }
        // 开始acr打分
        BaseRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (ScoreConfig.isAcrEnable() && now != null && now.getMusic() != null) {
            EngineManager.getInstance().startRecognize(RecognizeConfig.newBuilder()
                    .setSongName(now.getMusic().getItemName())
                    .setArtist(now.getMusic().getOwner())
                    .setMode(RecognizeConfig.MODE_MANUAL)
                    .setMResultListener(new ArcRecognizeListener() {
                        @Override
                        public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo, int lineNo) {
                            mUiHandler.removeMessages(MSG_SHOW_SCORE_EVENT + lineNo * 100);
                            if (lineNo > mLastLineNum) {
                                // 使用最新的打分方案做优化
                                int score1 = EngineManager.getInstance().getLineScore1();
                                int score2 = -1;
                                if (targetSongInfo != null) {
                                    score2 = (int) (targetSongInfo.getScore() * 100);
                                }
                                MyLog.d(TAG, "lineNo=" + lineNo + " melp打分=" + score1 + " acr打分=" + score2);
                                if (ScoreConfig.isMelpEnable()) {
                                    if (score1 > score2) {
                                        processScore(score1, lineNo);
                                    } else {
                                        processScore(score2, lineNo);
                                    }
                                } else {
                                    processScore(score2, lineNo);
                                }
                            }
                        }
                    })
                    .build());
        }
    }

    /**
     * 真正打开引擎开始演唱
     */
    public void beginSing() {
        // 打开引擎，变为主播
        BaseRoundInfoModel now = mRoomData.getRealRoundInfo();
        //开始录制声音
        if (SkrConfig.getInstance().isNeedUploadAudioForAI()) {
            // 需要上传音频伪装成机器人
            EngineManager.getInstance().startAudioRecording(RoomDataUtils.getSaveAudioForAiFilePath(), Constants.AUDIO_RECORDING_QUALITY_HIGH);
            if (now != null) {
                if (mRobotScoreHelper == null) {
                    mRobotScoreHelper = new RobotScoreHelper();
                }
                mRobotScoreHelper.reset();
            }
        }
    }

    /**
     * 抢唱歌权
     */
    public void grabThisRound(int seq) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", seq);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
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
        if (now == null) {
            return;
        }
        if (now.getStatus() != GrabRoundInfoModel.STATUS_SING) {
            MyLog.d(TAG, "lightsOff 不在演唱状态，cancel status=" + now.getStatus() + " roundSeq=" + now.getRoundSeq());
            return;
        }
        int roundSeq = now.getRoundSeq();
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundSeq);

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
        if (now.getStatus() != GrabRoundInfoModel.STATUS_SING) {
            MyLog.d(TAG, "lightsBurst 不在演唱状态，cancel status=" + now.getStatus() + " roundSeq=" + now.getRoundSeq());
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        int roundSeq = now.getRoundSeq();
        map.put("roundSeq", mRoomData.getRealRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
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
                if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
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
        Params.save2Pref(EngineManager.getInstance().getParams());
        if (!mRoomData.isHasExitGame()) {
            exitRoom();
        }
        cancelSyncGameStateTask();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EngineManager.getInstance().destroy("grabroom");
        mUiHandler.removeCallbacksAndMessages(null);
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter);
        if (mExoPlayer != null) {
            mExoPlayer.release();
        } else {
            MyLog.d(TAG, "mExoPlayer == null ");
        }

        if (mZipUrlResourceManager != null) {
            mZipUrlResourceManager.cancelAllTask();
        }
        ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(String.valueOf(mRoomData.getGameId()));
        MyLog.d(TAG, "destroy over");
    }

    /**
     * 告知我的的抢唱阶段结束了
     */
    public void sendMyGrabOver() {
        MyLog.d(TAG, "上报我的抢唱结束 ");
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

        BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel == null || roundInfoModel.getUserID() != MyUserInfoManager.getInstance().getUid()) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundInfoModel.getRoundSeq());

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
    public void giveUpSing() {
        MyLog.w(TAG, "我放弃演唱");
        estimateOverTsThisRound();

        BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel == null || roundInfoModel.getUserID() != MyUserInfoManager.getInstance().getUid()) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("roundSeq", roundInfoModel.getRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.giveUpSing(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mIGrabView.giveUpSuccess(roundInfoModel.getRoundSeq());
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
                    U.getToastUtil().showShort("发起踢人请求成功");
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
        ApiMethods.subscribe(mRoomServerApi.repKickUser(body), new ApiObserver<ApiResult>() {
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
    public void exitRoom() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.exitRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mRoomData.setHasExitGame(true);
                    GrabResultInfoModel grabResultInfoModel = JSON.parseObject(result.getData().getString("resultInfo"), GrabResultInfoModel.class);
                    List<ScoreResultModel> scoreResultModel = JSON.parseArray(result.getData().getString("userScoreResult"), ScoreResultModel.class);
                    if (grabResultInfoModel != null && scoreResultModel != null) {
                        // 得到结果
                        mRoomData.setGrabResultData(new GrabResultData(grabResultInfoModel, scoreResultModel));
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
    public void changeRoom() {
        if (mSwitchRooming) {
            U.getToastUtil().showShort("切换中");
            return;
        }
        mSwitchRooming = true;
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", mRoomData.getGameId());
        map.put("tagID", mRoomData.getTagId());
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
            mRoomData.loadFromRsp(joinGrabRoomRspModel);
            joinRoomAndInit(false);
            mRoomData.checkRoundInEachMode();
            mIGrabView.onChangeRoomResult(true, null);
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

                    updatePlayerState(gameOverTimeMs, syncStatusTimes, currentInfo);
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
    private synchronized void updatePlayerState(long gameOverTimeMs, long syncStatusTimes, GrabRoundInfoModel newRoundInfo) {
        MyLog.w(TAG, "updatePlayerState" + " gameOverTimeMs=" + gameOverTimeMs + " syncStatusTimes=" + syncStatusTimes + " currentInfo=" + newRoundInfo.getRoundSeq());
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
        EngineManager.getInstance().stopRecognize();
        mRoomData.setIsGameFinish(true);
        cancelSyncGameStateTask();
        // 游戏结束了,处理相应的ui逻辑
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mIGrabView.roundOver(event.lastRoundInfo.getMusic().getItemID(), event.lastRoundInfo.getOverReason(), event.lastRoundInfo.getResultType(), false, null);
            }
        });
        // 销毁引擎，减小成本
        EngineManager.getInstance().destroy("grabroom");
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
        MyLog.d(TAG, "GrabRoundChangeEvent" + " event=" + event);
        // 轮次变化尝试更新头像
        estimateOverTsThisRound();
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
        closeEngine();
        tryStopRobotPlay();
        EngineManager.getInstance().stopRecognize();
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
                    mIGrabView.showPracticeFlag(finalSize <= 2);
                }
            });
        }

        if (now.getStatus() == GrabRoundInfoModel.STATUS_GRAB) {
            //抢唱阶段，播抢唱卡片
            if (event.lastRoundInfo != null && event.lastRoundInfo.getStatus() >= GrabRoundInfoModel.STATUS_SING) {
                // 新一轮的抢唱阶段，得告诉上一轮演唱结束了啊，上一轮演唱结束卡片播完，才播歌曲卡片
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabView.roundOver(event.lastRoundInfo.getMusic().getItemID(), event.lastRoundInfo.getOverReason(), event.lastRoundInfo.getResultType(), true, now);
                    }
                });
                if (event.lastRoundInfo.getUserID() == MyUserInfoManager.getInstance().getUid()) {
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
        } else if (now.getStatus() == GrabRoundInfoModel.STATUS_SING) {
            // 演唱阶段
            if (now.getUserID() == MyUserInfoManager.getInstance().getUid()) {
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
                        mIGrabView.singByOthers(now.getUserID());
                    }
                });
                checkMachineUser(now.getUserID());
            }
        } else if (now.getStatus() == GrabRoundInfoModel.STATUS_OVER) {
            MyLog.w(TAG, "GrabRoundChangeEvent 刚切换到该轮次就告诉我轮次结束？？？roundSeq:" + now.getRoundSeq());
            MyLog.w(TAG, "自动切换到下个轮次");
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
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
        closeEngine();
        GrabRoundInfoModel now = event.roundInfo;
        tryStopRobotPlay();
        if (now.getStatus() == GrabRoundInfoModel.STATUS_GRAB) {
            //抢唱阶段，播抢唱卡片
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mIGrabView.grabBegin(now.getRoundSeq(), now.getMusic());
                }
            });
        } else if (now.getStatus() == GrabRoundInfoModel.STATUS_SING) {
            // 演唱阶段
            if (now.getUserID() == MyUserInfoManager.getInstance().getUid()) {
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
                        mIGrabView.singByOthers(now.getUserID());
                    }
                });
                checkMachineUser(now.getUserID());
            }
        }
    }

    private void closeEngine() {
        if (mRoomData.getGameId() > 0) {
            EngineManager.getInstance().stopAudioMixing();
            EngineManager.getInstance().setClientRole(false);
        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EngineEvent event) {
        if (event.getType() == EngineEvent.TYPE_USER_ROLE_CHANGE) {
            EngineEvent.RoleChangeInfo roleChangeInfo = event.getObj();
            if (roleChangeInfo.getNewRole() == 1) {
                onChangeBroadcastSuccess();
            }
        }
    }

    /**
     * 成功切换为主播
     */
    private void onChangeBroadcastSuccess() {
        MyLog.d(TAG, "onChangeBroadcastSuccess");
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
                if (infoModel == null || infoModel.getUserID() != MyUserInfoManager.getInstance().getUid()) {
                    MyLog.d(TAG, "onChangeBroadcastSuccess,但已经不是你的轮次了，cancel");
                    return;
                }
                SongModel songModel = infoModel.getMusic();
                if (songModel == null) {
                    return;
                }
                // 开始开始混伴奏，开始解除引擎mute
                File accFile = SongResUtils.getAccFileByUrl(infoModel.getMusic().getAcc());
                // midi不需要在这下，只要下好，native就会解析，打分就能恢复
                File midiFile = SongResUtils.getMIDIFileByUrl(infoModel.getMusic().getMidi());
                if (mRoomData.isAccEnable()) {
                    int songBeginTs = songModel.getBeginMs();
                    if (accFile != null && accFile.exists()) {
                        // 伴奏文件存在
                        EngineManager.getInstance().startAudioMixing((int) MyUserInfoManager.getInstance().getUid(), accFile.getAbsolutePath()
                                , midiFile.getAbsolutePath(), songBeginTs, false, false, 1);
                    } else {
                        EngineManager.getInstance().startAudioMixing((int) MyUserInfoManager.getInstance().getUid(), songModel.getAcc()
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
            if (roundInfoModel.getStatus() == GrabRoundInfoModel.STATUS_GRAB) {
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
        pretendLightMsgComment(event.roundInfo.getUserID(), event.uid, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightBurstEvent event) {
        pretendLightMsgComment(event.roundInfo.getUserID(), event.uid, true);
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
        boolean canAdd = false;
        GrabPlayerInfoModel playerInfoModel = event.infoModel;
        MyLog.d(TAG, "有人加入房间,id=" + playerInfoModel.getUserID() + " name=" + playerInfoModel.getUserInfo().getNickname() + " role=" + playerInfoModel.getRole() + " roundSeq=" + event.roundSeq);
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
    public void onEvent(QCoinChangeEvent event) {
        if (event.userID == MyUserInfoManager.getInstance().getUid()) {
            if (event.remainCoin > 0) {
                mRoomData.setCoin(event.remainCoin);
            }
            if (event.reason.getValue() == 1) {
                pretenSystemMsg("你获取了" + event.changeCoin + "金币奖励");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QRoundOverMsgEvent event) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push event:" + event);
        ensureInRcRoom();
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
            if (event.totalRoundNum > 0) {
                mRoomData.getGrabConfigModel().setTotalGameRoundSeq(event.totalRoundNum);
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
            U.getToastUtil().showLong("房主离开了游戏，房间解散");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QSyncStatusMsgEvent event) {
        ensureInRcRoom();
        MyLog.w(TAG, "收到服务器push更新状态,event.currentRound是" + event.getCurrentRound().getRoundSeq() + ", timeMs 是" + event.info.getTimeMs());
        startSyncGameStateTask(sSyncStateTaskInterval);
        updatePlayerState(event.getGameOverTimeMs(), event.getSyncStatusTimeMs(), event.getCurrentRound());
//        fetchAcc(event.getNextRound());
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
            mUiHandler.removeMessages(MSG_ROBOT_SING_BEGIN);
            Message message = mUiHandler.obtainMessage(MSG_ROBOT_SING_BEGIN);
            mUiHandler.sendMessage(message);
        }
    }

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
            pretenSystemMsg(String.format("房主已将歌单切换为 %s 专场", event.getTagName()));
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


    /*打分相关*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MachineScoreEvent event) {
        //收到其他人的机器打分消息，比较复杂，暂时简单点，轮次正确就直接展示
        if (RoomDataUtils.isThisUserRound(mRoomData.getRealRoundInfo(), event.userId)) {
            mIGrabView.updateScrollBarProgress(event.score, event.lineNum);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(LrcEvent.LineLineEndEvent event) {
        MyLog.d(TAG, "onEvent LineEndEvent lineno=" + event.lineNum);
        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
            if (ScoreConfig.isAcrEnable()) {
                EngineManager.getInstance().recognizeInManualMode(event.lineNum);
            } else {
                if (ScoreConfig.isMelpEnable()) {
                    int score = EngineManager.getInstance().getLineScore1();
                    processScore(score, event.lineNum);
                }
            }
            Message msg = mUiHandler.obtainMessage(MSG_SHOW_SCORE_EVENT + event.lineNum * 100);
            mUiHandler.sendMessageDelayed(msg, 1000);
        } else {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LrcEvent.LyricStartEvent event) {
        MyLog.d(TAG, "onEvent LineStartEvent");
        Params params = EngineManager.getInstance().getParams();
        if (params != null) {
            params.setLrcHasStart(true);
        }
    }

    void processScore(int score, int line) {
        if (score < 0) {
            return;
        }
        if (line <= mLastLineNum) {
            return;
        }
        mLastLineNum = line;
        if (ScoreConfig.isMelpEnable() && ScoreConfig.isAcrEnable()) {

        } else {
            U.getToastUtil().showShort("score:" + score);
        }
        MyLog.d(TAG, "onEvent" + " 得分=" + score);
        MachineScoreItem machineScoreItem = new MachineScoreItem();
        machineScoreItem.setScore(score);
        long ts = EngineManager.getInstance().getAudioMixingCurrentPosition();
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
        sendScoreToServer(score, line);
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
     * 单句打分上报
     *
     * @param score
     * @param line
     */
    public void sendScoreToServer(int score, int line) {
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("gameID", mRoomData.getGameId());
//        RankRoundInfoModel infoModel = RoomDataUtils.getRoundInfoByUserId(mRoomData, (int) MyUserInfoManager.getInstance().getUid());
//        if (infoModel == null) {
//            return;
//        }
//        int itemID = infoModel.getPlaybookID();
//        map.put("itemID", itemID);
//        int mainLevel = 0;
//        PlayerInfoModel playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, MyUserInfoManager.getInstance().getUid());
//        if (playerInfoModel != null) {
//            mainLevel = playerInfoModel.getUserInfo().getMainLevel();
//        }
//        map.put("mainLevel", mainLevel);
//        map.put("no", line);
//        int roundSeq = infoModel.getRoundSeq();
//        map.put("roundSeq", roundSeq);
//        map.put("score", score);
//        long nowTs = System.currentTimeMillis();
//        int singSecond = (int) ((nowTs - mRoomData.getSingBeginTs()) / 1000);
//        map.put("singSecond", singSecond);
//        map.put("timeMs", nowTs);
//        map.put("userID", MyUserInfoManager.getInstance().getUid());
//        StringBuilder sb = new StringBuilder();
//        sb.append("skrer")
//                .append("|").append(MyUserInfoManager.getInstance().getUid())
//                .append("|").append(itemID)
//                .append("|").append(score)
//                .append("|").append(line)
//                .append("|").append(mRoomData.getGameId())
//                .append("|").append(mainLevel)
//                .append("|").append(singSecond)
//                .append("|").append(roundSeq)
//                .append("|").append(nowTs);
//        map.put("sign", U.getMD5Utils().MD5_32(sb.toString()));
//        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
//        ApiMethods.subscribe(mRoomServerApi.sendPkPerSegmentResult(body), new ApiObserver<ApiResult>() {
//            @Override
//            public void process(ApiResult result) {
//                if (result.getErrno() == 0) {
//                    // TODO: 2018/12/13  当前postman返回的为空 待补充
//                    MyLog.w(TAG, "单句打分上报成功");
//                } else {
//                    MyLog.w(TAG, "单句打分上报失败" + result.getErrno());
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                MyLog.e(e);
//            }
//        }, this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QKickUserReqEvent qKickUserReqEvent) {
        // 踢人的请求
        MyLog.d(TAG, "收到踢人请求 kickUserID:" + qKickUserReqEvent.kickUserID);
        mIGrabView.showKickVoteDialog(qKickUserReqEvent.kickUserID, qKickUserReqEvent.sourceUserID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QKickUserResultEvent qKickUserResultEvent) {
        // 踢人的结果
        MyLog.d(TAG, "收到踢人结果 kickUserID:" + qKickUserResultEvent.kickUserID);
        if (qKickUserResultEvent.kickUserID == MyUserInfoManager.getInstance().getUid()) {
            // 自己被踢出去
            if (qKickUserResultEvent.isKickSuccess) {
                mIGrabView.kickBySomeOne();
            }
        } else {
            // 别人被踢出去
            mIGrabView.kickSomeOne();
            pretenSystemMsg(qKickUserResultEvent.kickResultContent);
        }
    }
}
