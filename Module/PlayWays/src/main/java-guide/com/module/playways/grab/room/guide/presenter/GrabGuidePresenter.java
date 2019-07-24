package com.module.playways.grab.room.guide.presenter;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.engine.ScoreConfig;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.player.IPlayer;
import com.common.player.VideoPlayerAdapter;
import com.common.player.exoplayer.ExoPlayer;
import com.common.player.mediaplayer.AndroidMediaPlayer;
import com.common.utils.ActivityUtils;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.component.busilib.SkrConfig;
import com.component.busilib.constans.GameModeType;
import com.engine.EngineEvent;
import com.engine.Params;
import com.engine.arccloud.RecognizeConfig;
import com.module.ModuleServiceManager;
import com.module.msg.CustomMsgType;
import com.module.msg.IMsgService;
import com.module.playways.BuildConfig;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabGameOverEvent;
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.grab.room.event.GrabWaitSeatUpdateEvent;
import com.module.playways.grab.room.event.SomeOneJoinWaitSeatEvent;
import com.module.playways.grab.room.event.SomeOneLeavePlaySeatEvent;
import com.module.playways.grab.room.guide.IGrabGuideView;
import com.module.playways.grab.room.model.BLightInfoModel;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.GrabSkrResourceModel;
import com.module.playways.grab.room.model.MLightInfoModel;
import com.module.playways.grab.room.model.WantSingerInfo;
import com.module.playways.others.LyricAndAccMatchManager;
import com.module.playways.room.gift.event.GiftBrushMsgEvent;
import com.module.playways.room.msg.event.GiftPresentEvent;
import com.module.playways.room.msg.event.MachineScoreEvent;
import com.module.playways.room.msg.event.QChoGiveUpEvent;
import com.module.playways.room.msg.event.QCoinChangeEvent;
import com.module.playways.room.msg.event.QExitGameMsgEvent;
import com.module.playways.room.msg.event.QGetSingChanceMsgEvent;
import com.module.playways.room.msg.event.QJoinNoticeEvent;
import com.module.playways.room.msg.event.QLightBurstMsgEvent;
import com.module.playways.room.msg.event.QLightOffMsgEvent;
import com.module.playways.room.msg.event.QPkInnerRoundOverEvent;
import com.module.playways.room.msg.event.QRoundAndGameOverMsgEvent;
import com.module.playways.room.msg.event.QRoundOverMsgEvent;
import com.module.playways.room.msg.event.QWantSingChanceMsgEvent;
import com.module.playways.room.msg.filter.PushMsgFilter;
import com.module.playways.room.msg.manager.ChatRoomMsgManager;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.prepare.model.PlayerInfoModel;
import com.module.playways.room.room.comment.model.CommentLightModel;
import com.module.playways.room.room.comment.model.CommentSysModel;
import com.module.playways.room.room.comment.model.CommentTextModel;
import com.module.playways.room.room.event.PretendCommentMsgEvent;
import com.module.playways.room.room.score.MachineScoreItem;
import com.module.playways.room.room.score.RobotScoreHelper;
import com.module.playways.room.song.model.SongModel;
import com.component.live.proto.Common.ESex;
import com.component.live.proto.Common.UserInfo;
import com.component.live.proto.Room.EMsgPosType;
import com.component.live.proto.Room.EQGameOverReason;
import com.component.live.proto.Room.EQRoundOverReason;
import com.component.live.proto.Room.EQRoundResultType;
import com.component.live.proto.Room.EQRoundStatus;
import com.component.live.proto.Room.ERoomMsgType;
import com.component.live.proto.Room.EWantSingType;
import com.component.live.proto.Room.MachineScore;
import com.component.live.proto.Room.RoomMsg;
import com.component.lyrics.utils.SongResUtils;
import com.component.lyrics.utils.ZipUrlResourceManager;
import com.component.mediaengine.kit.ZqEngineKit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.util.List;

import io.agora.rtc.Constants;

public class GrabGuidePresenter extends RxLifeCyclePresenter {
    public String TAG = "GrabGuidePresenter";

    static final int MSG_ROBOT_SING_BEGIN = 10;

    static final int MSG_ENSURE_SWITCH_BROADCAST_SUCCESS = 21; // 确保用户切换成主播成功，防止引擎不回调的保护

    long mFirstKickOutTime = -1; //用时间和次数来判断一个人有没有在一个房间里

    int mAbsenTimes = 0;

    GrabRoomData mRoomData;

    IGrabGuideView mIGrabView;

    RobotScoreHelper mRobotScoreHelper;

    boolean mDestroyed = false;

    IPlayer mExoPlayer;

    ZipUrlResourceManager mZipUrlResourceManager;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ROBOT_SING_BEGIN:
                    robotSingBegin();
                    break;
                case MSG_ENSURE_SWITCH_BROADCAST_SUCCESS:
                    onChangeBroadcastSuccess();
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


    public GrabGuidePresenter(@NotNull IGrabGuideView iGrabView, @NotNull GrabRoomData roomData) {
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
        MyLog.w(TAG, "joinRoomAndInit" + " first=" + first + ", gameId is " + mRoomData.getGameId());
        mFirstKickOutTime = -1;
        mAbsenTimes = 0;

        if (mRoomData.getGameId() > 0) {
            if (first) {
                Params params = Params.getFromPref();
//            params.setStyleEnum(Params.AudioEffect.none);
                params.setScene(Params.Scene.grab);
                ZqEngineKit.getInstance().init("grabroom", params);
            }
            ZqEngineKit.getInstance().joinRoom(String.valueOf(mRoomData.getGameId()), (int) UserAccountManager.getInstance().getUuidAsLong(), mRoomData.isOwner(), mRoomData.getAgoraToken());
            // 不发送本地音频, 会造成第一次抢没声音
            ZqEngineKit.getInstance().muteLocalAudioStream(true);
        }
        if (mRoomData.getGameId() > 0) {
            for (GrabPlayerInfoModel playerInfoModel : mRoomData.getPlayerInfoList()) {
                if (!playerInfoModel.isOnline()) {
                    continue;
                }
                pretendEnterRoom(playerInfoModel);
            }
            pretendRoomNameSystemMsg(mRoomData.getRoomName(), CommentSysModel.TYPE_ENTER_ROOM);
        }
    }

    private void pretendEnterRoom(GrabPlayerInfoModel playerInfoModel) {
        CommentTextModel commentModel = new CommentTextModel();
        commentModel.setUserId(playerInfoModel.getUserInfo().getUserId());
        commentModel.setAvatar(playerInfoModel.getUserInfo().getAvatar());
        commentModel.setUserName(playerInfoModel.getUserInfo().getNicknameRemark());
        commentModel.setAvatarColor(Color.WHITE);
        SpannableStringBuilder stringBuilder;
        if (playerInfoModel.getUserInfo().getUserId() == UserAccountManager.SYSTEM_GRAB_ID) {
            stringBuilder = new SpanUtils()
                    .append(playerInfoModel.getUserInfo().getNicknameRemark() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                    .append("我是撕歌最傲娇小助手多音，来和你们一起唱歌卖萌~").setForegroundColor(Color.parseColor("#586D94"))
                    .create();
        } else {
            SpanUtils spanUtils = new SpanUtils()
                    .append(playerInfoModel.getUserInfo().getNicknameRemark() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                    .append("加入了房间").setForegroundColor(Color.parseColor("#586D94"));
            if (BuildConfig.DEBUG) {
                spanUtils.append(" 角色为" + playerInfoModel.getRole())
                        .append(" 在线状态为" + playerInfoModel.isOnline());
            }
            stringBuilder = spanUtils.create();
        }
        commentModel.setStringBuilder(stringBuilder);
        EventBus.getDefault().post(new PretendCommentMsgEvent(commentModel));
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
                public void onPrepared(long duration) {
                    super.onPrepared(duration);
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
        if (now != null) {
            if (now.getWantSingType() == EWantSingType.EWST_SPK.getValue()) {
                needAcc = true;
            } else if (now.getWantSingType() == EWantSingType.EWST_CHORUS.getValue()) {
                needAcc = false;
            } else if (mRoomData.isAccEnable()) {
                needAcc = true;
            }
        }
        if (needAcc) {
            // 1. 开启伴奏的，预先下载 melp 资源
            if (now != null) {
                String midi = now.getMusic().getMidi();
                if(!TextUtils.isEmpty(midi)){
                    File midiFile = SongResUtils.getMIDIFileByUrl(midi);
                    if (midiFile != null && !midiFile.exists()) {
                        U.getHttpUtils().downloadFileAsync(now.getMusic().getMidi(), midiFile,true, null);
                    }
                }
            }
        }
        if (!mRoomData.isOwner()) {
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

        if (needAcc) {
            if (mRoomData.isOwner()) {
                MyLog.d(TAG, "preOpWhenSelfRound 是主播 直接 onChangeBroadcastSuccess");
                onChangeBroadcastSuccess();
            } else {
                // 如果需要播放伴奏，一定要在角色切换成功才能播
                mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS, 2000);
            }
        }
        // 开始acr打分
        if (ScoreConfig.isAcrEnable() && now != null && now.getMusic() != null) {
            ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                    .setSongName(now.getMusic().getItemName())
                    .setArtist(now.getMusic().getOwner())
                    .setMode(RecognizeConfig.MODE_MANUAL)
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
        if (SkrConfig.getInstance().isNeedUploadAudioForAI(GameModeType.GAME_MODE_GRAB)) {
            // 需要上传音频伪装成机器人
            ZqEngineKit.getInstance().startAudioRecording(RoomDataUtils.getSaveAudioForAiFilePath(), Constants.AUDIO_RECORDING_QUALITY_HIGH);
            if (now != null) {
                if (mRobotScoreHelper == null) {
                    mRobotScoreHelper = new RobotScoreHelper();
                }
                mRobotScoreHelper.reset();
            }
        }
    }

    public void changeSong() {
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            now.setMusic(mRoomData.getGrabGuideInfoModel().getNextSongModel());
        }
        tryStopRobotPlay();
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mIGrabView.changeSong();
            }
        });
    }

    /**
     * 抢唱歌权
     */
    public void grabThisRound(int userId, int seq, boolean challenge) {
        MyLog.d(TAG, "grabThisRound" + " seq=" + seq + " challenge=" + challenge + " accenable=" + mRoomData.isAccEnable());
        //抢成功了
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            WantSingerInfo wantSingerInfo = new WantSingerInfo();
            wantSingerInfo.setWantSingType(EWantSingType.EWST_DEFAULT.getValue());
            wantSingerInfo.setUserID(userId);
            wantSingerInfo.setTimeMs(System.currentTimeMillis());
            now.addGrabUid(true, wantSingerInfo);

            // 第一轮，按剧本自己唱，直接触发轮次变化
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    now.setHasSing(true);
                    now.setUserID(userId);
                    now.updateStatus(true, EQRoundStatus.QRS_SING.getValue());
                }
            }, 1000);
        }
    }

    /**
     * 灭灯
     */
    public void lightsOff() {
        //TODO 这里自动触发灭灯操作后的结果
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            MLightInfoModel noPassingInfo = new MLightInfoModel();
            noPassingInfo.setUserID((int) MyUserInfoManager.getInstance().getUid());
            now.addLightOffUid(true, noPassingInfo);
        }
    }

    /**
     * 爆灯
     */
    public void lightsBurst() {
        //TODO 这里自动触发爆灯操作后的结果
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            mRoomData.setCoin(mRoomData.getCoin() - 2);
            BLightInfoModel m = new BLightInfoModel();
            m.setUserID((int) MyUserInfoManager.getInstance().getUid());
            now.addLightBurstUid(true, m);
        }
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
            public void onPrepared(long duration) {
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

        //机器人唱完自动结束
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onGameOver("robotSingBegin", System.currentTimeMillis());
            }
        }, 23000);
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
        if (SkrConfig.getInstance().isNeedUploadAudioForAI(GameModeType.GAME_MODE_GRAB)) {
            //属于需要上传音频文件的状态
            // 上一轮是我的轮次，暂停录音
            if (mRoomData.getGameId() > 0) {
                ZqEngineKit.getInstance().stopAudioRecording();
            }
            // 上传打分
            if (mRobotScoreHelper != null) {
                if (mRobotScoreHelper.isScoreEnough()) {
                    if (roundInfoModel.getOverReason() == EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue()
                            && roundInfoModel.getResultType() == EQRoundResultType.ROT_TYPE_1.getValue()) {
                        // 是一唱到底的才上传
                        roundInfoModel.setSysScore(mRobotScoreHelper.getAverageScore());
//                        uploadRes1ForAi(roundInfoModel);
                    } else {
                        MyLog.d(TAG, "没有唱到一唱到底不上传");
                    }
                } else {
                    MyLog.d(TAG, "isScoreEnough false");
                }
            }
        }
    }

    @Override
    public void destroy() {
        MyLog.d(TAG, "destroy begin");
        super.destroy();
        mDestroyed = true;
        Params.save2Pref(ZqEngineKit.getInstance().getParams());
        if (!mRoomData.isHasExitGame()) {
            exitRoom();
        }
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        ZqEngineKit.getInstance().destroy("grabroom");
        mUiHandler.removeCallbacksAndMessages(null);
        if (mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer = null;
        } else {
            MyLog.d(TAG, "mExoPlayer == null ");
        }

        if (mZipUrlResourceManager != null) {
            mZipUrlResourceManager.cancelAllTask();
        }
        MyLog.d(TAG, "destroy over");
    }

    /**
     * 告知我的的抢唱阶段结束了
     */
    public void sendMyGrabOver() {
        //TODO 轮次悬停住，让用户可以一直抢

    }

    /**
     * 上报轮次结束信息
     */
    public void sendRoundOverInfo() {
        MyLog.w(TAG, "上报我的演唱结束");
        //TODO 自动切换轮次，我的轮次结束
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            now.setOverReason(EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue());
        }
        GrabRoundInfoModel bRoundInfo = mRoomData.getGrabGuideInfoModel().createBRoundInfo();
        mRoomData.setExpectRoundInfo(bRoundInfo);
        mRoomData.checkRoundInEachMode();
        mRoomData.setCoin(mRoomData.getCoin() + 1);
    }

    /**
     * 放弃演唱接口
     */
    public void giveUpSing() {
        MyLog.w(TAG, "我放弃演唱");
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            now.setOverReason(EQRoundOverReason.ROR_SELF_GIVE_UP.getValue());
        }
        GrabRoundInfoModel bRoundInfo = mRoomData.getGrabGuideInfoModel().createBRoundInfo();
        mRoomData.setExpectRoundInfo(bRoundInfo);
        mRoomData.checkRoundInEachMode();
    }


    /**
     * 退出房间
     */
    public void exitRoom() {
        //TODO 自动退出房间，切换到毕业了结果页
        mIGrabView.onGetGameResult(true);
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
        } else if (now.getStatus() == EQRoundStatus.QRS_SING.getValue()
                || now.getStatus() == EQRoundStatus.QRS_CHO_SING.getValue()
                || now.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()
                || now.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
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
                checkMachineUser(now.getUserID());
            }
        } else if (now.getStatus() == EQRoundStatus.QRS_END.getValue()) {
            MyLog.w(TAG, "GrabRoundChangeEvent 刚切换到该轮次就告诉我轮次结束？？？roundSeq:" + now.getRoundSeq());
            MyLog.w(TAG, "自动切换到下个轮次");
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabWaitSeatUpdateEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        if (event.list != null && event.list.size() > 0) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneJoinWaitSeatEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
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
        closeEngine();
        GrabRoundInfoModel now = event.roundInfo;
        tryStopRobotPlay();
        if (now.getStatus() == EQRoundStatus.QRS_INTRO.getValue()) {
            //抢唱阶段，播抢唱卡片
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mIGrabView.grabBegin(now.getRoundSeq(), now.getMusic());
                }
            });
        } else if (now.getStatus() == EQRoundStatus.QRS_SING.getValue()
                || now.getStatus() == EQRoundStatus.QRS_CHO_SING.getValue()
                || now.getStatus() == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.getValue()
                || now.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
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
                checkMachineUser(now.getUserID());
            }
//            if (now.getStatus() == EQRoundStatus.QRS_CHO_SING.getValue()) {
//                pretendSystemMsg("合唱配对成功");
//            }
        }
    }

    private void closeEngine() {
        if (mRoomData.getGameId() > 0) {
            ZqEngineKit.getInstance().stopAudioMixing();
            if (mRoomData.isSpeaking()) {
                MyLog.d(TAG, "closeEngine 正在抢麦说话，无需闭麦");
            } else {
                if (mRoomData.isOwner()) {
                    MyLog.d(TAG, "closeEngine 是房主 mute即可");
                    ZqEngineKit.getInstance().muteLocalAudioStream(true);
                } else {
                    if (ZqEngineKit.getInstance().getParams().isAnchor()) {
                        ZqEngineKit.getInstance().setClientRole(false);
                    }
                }
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
        } else {
            // 可以考虑监听下房主的说话提示 做下容错
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
            GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
            if (now != null) {
                isChorus = now.isChorusRound();
            }
            CommentLightModel commentLightModel = new CommentLightModel(mRoomData.getGameType(), playerInfoModel, singerModel, isBao, isChorus);
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
            CommentTextModel commentModel = new CommentTextModel();
            commentModel.setUserId(playerInfoModel.getUserInfo().getUserId());
            commentModel.setAvatar(playerInfoModel.getUserInfo().getAvatar());
            commentModel.setUserName(playerInfoModel.getUserInfo().getNicknameRemark());
            commentModel.setAvatarColor(Color.WHITE);
            SpannableStringBuilder stringBuilder;
            if (playerInfoModel.getUserInfo().getUserId() == UserAccountManager.SYSTEM_GRAB_ID) {
                stringBuilder = new SpanUtils()
                        .append(playerInfoModel.getUserInfo().getNicknameRemark() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                        .append("我是撕歌最傲娇小助手多音，来和你们一起唱歌卖萌~").setForegroundColor(Color.parseColor("#586D94"))
                        .create();
            } else {
                SpanUtils spanUtils = new SpanUtils()
                        .append(playerInfoModel.getUserInfo().getNicknameRemark() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                        .append("加入了房间").setForegroundColor(Color.parseColor("#586D94"));
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
            commentModel.setAvatarColor(Color.WHITE);
            SpannableStringBuilder stringBuilder;
            SpanUtils spanUtils = new SpanUtils()
                    .append(userInfoModel.getNicknameRemark() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                    .append("不唱了").setForegroundColor(Color.parseColor("#586D94"));
            stringBuilder = spanUtils.create();
            commentModel.setStringBuilder(stringBuilder);
            EventBus.getDefault().post(new PretendCommentMsgEvent(commentModel));
        }
    }

    /**
     * 轮次变化
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QRoundOverMsgEvent event) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push event:" + event);
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


    private void onGameOver(String from, long gameOverTs) {
        MyLog.w(TAG, "游戏结束 gameOverTs=" + gameOverTs + " from:" + from);
        if (gameOverTs > mRoomData.getGameStartTs() && gameOverTs > mRoomData.getGameOverTs()) {
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GiftPresentEvent giftPresentEvent) {
        MyLog.d(TAG, "onEvent" + " giftPresentEvent=" + giftPresentEvent);
        if (giftPresentEvent.info.getRoomID() == mRoomData.getGameId()) {
            EventBus.getDefault().post(new GiftBrushMsgEvent(giftPresentEvent.mGPrensentGiftMsgModel));
        }
    }


}
