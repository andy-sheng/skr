package com.zq.mediaengine.kit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.RectF;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import com.common.log.DebugLogView;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.CustomHandlerThread;
import com.common.utils.DeviceUtils;
import com.common.utils.U;
import com.common.videocache.MediaCacheManager;
import com.engine.EngineEvent;
import com.engine.Params;
import com.engine.UserStatus;
import com.engine.agora.AgoraOutCallback;
import com.engine.agora.effect.EffectModel;
import com.engine.api.EngineServerApi;
import com.engine.arccloud.AcrRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.score.Score2Callback;
import com.zq.engine.avstatistics.SDataManager;
import com.zq.engine.avstatistics.datastruct.Skr;
import com.zq.mediaengine.capture.AudioCapture;
import com.zq.mediaengine.capture.AudioPlayerCapture;
import com.zq.mediaengine.capture.CameraCapture;
import com.zq.mediaengine.encoder.MediaCodecAudioEncoder;
import com.zq.mediaengine.filter.audio.APMFilter;
import com.zq.mediaengine.filter.audio.AudioCopyFilter;
import com.zq.mediaengine.filter.audio.AudioFilterMgt;
import com.zq.mediaengine.filter.audio.AudioMixer;
import com.zq.mediaengine.filter.audio.AudioPreview;
import com.zq.mediaengine.filter.audio.AudioResampleFilter;
import com.zq.mediaengine.filter.audio.AudioReverbFilter;
import com.zq.mediaengine.filter.imgtex.ImgTexMixer;
import com.zq.mediaengine.filter.imgtex.ImgTexPreview;
import com.zq.mediaengine.filter.imgtex.ImgTexScaleFilter;
import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.AudioCodecFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.kit.agora.AgoraRTCAdapter;
import com.zq.mediaengine.kit.bytedance.BytedEffectFilter;
import com.zq.mediaengine.kit.filter.AcrRecognizer;
import com.zq.mediaengine.kit.filter.AudioDummyFilter;
import com.zq.mediaengine.kit.filter.CbAudioEffectFilter;
import com.zq.mediaengine.kit.filter.CbAudioScorer;
import com.zq.mediaengine.kit.filter.TbAudioAgcFilter;
import com.zq.mediaengine.kit.log.LogRunnable;
import com.zq.mediaengine.publisher.MediaMuxerPublisher;
import com.zq.mediaengine.publisher.Publisher;
import com.zq.mediaengine.publisher.RawFrameWriter;
import com.zq.mediaengine.util.audio.AudioUtil;
import com.zq.mediaengine.util.gles.GLRender;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

public class ZqEngineKit implements AgoraOutCallback {

    public final String TAG = "ZqEngineKit";
    public static final String PREF_KEY_TOKEN_ENABLE = "key_agora_token_enable";
    public static final String AUDIO_FEEDBACK_DIR = "audio_feedback";
    public static final int VIDEO_RESOLUTION_360P = 0;
    public static final int VIDEO_RESOLUTION_480P = 1;
    public static final int VIDEO_RESOLUTION_540P = 2;
    public static final int VIDEO_RESOLUTION_720P = 3;
    public static final int VIDEO_RESOLUTION_1080P = 4;

    private static final int DEFAULT_PREVIEW_WIDTH = 720;
    private static final int DEFAULT_PREVIEW_HEIGHT = 1280;

    // 用来打分的音频采样率, 过高的采样率会造成很大的性能消耗
    private static final int SCORE_SAMPLE_RATE = 16000;

    static final int STATUS_UNINIT = 0;
    static final int STATUS_INITING = 1;
    static final int STATUS_INITED = 2;
    static final int STATUS_UNINITING = 3;
    static final int MSG_JOIN_ROOM_TIMEOUT = 11;
    static final int MSG_JOIN_ROOM_AGAIN = 12;
    static final int MSG_ROLE_CHANGE_TIMEOUT = 13;

    private static final boolean SCORE_DEBUG = false;
    private static final String SCORE_DEBUG_PATH = "/sdcard/tongzhuodeni.pcm";
    public static final boolean RECORD_FOR_DEBUG = false;
    public final boolean OPEN_AUDIO_RECORD_FOR_CALLBACK = true; // 是否开启用于用户反馈的录制

    private Context mContext;
    private Params mConfig = new Params(); // 为了防止崩溃

    private volatile int mStatus = STATUS_UNINIT;// 0未初始化 1 初始ing 2 初始化 3 释放ing
    /**
     * 存储该房间所有用户在引擎中的状态的，
     * key为在引擎中的用户 id
     */
    public HashMap<Integer, UserStatus> mUserStatusMap = new HashMap<>();
    private HashSet<View> mRemoteViewCache = new HashSet<>();
    //    private Handler mUiHandler = new Handler();
    private Disposable mMusicTimePlayTimeListener;

    private String mInitFrom;

    private CustomHandlerThread mCustomHandlerThread;

    private boolean mTokenEnable = false; // 是否开启token校验
    private String mLastJoinChannelToken; // 上一次加入房间用的token
    private String mRoomId = ""; // 房间id
//    private boolean mInChannel = false; // 是否已经在频道中

    private GLRender mGLRender;
    private CameraCapture mCameraCapture;
    private ImgTexScaleFilter mImgTexScaleFilter;
    private BytedEffectFilter mBytedEffectFilter;
    private ImgTexMixer mImgTexPreviewMixer;
    private ImgTexMixer mImgTexMixer;
    private ImgTexPreview mImgTexPreview;
    private Map<Integer, Integer> mRemoteUserPinMap = new HashMap<>();

    private AgoraRTCAdapter mAgoraRTCAdapter;
    private AudioDummyFilter mAudioDummyFilter;
    private AudioFilterMgt mAudioFilterMgt;
    private AudioReverbFilter mAudioReverbFilter;
    private AudioResampleFilter mScoreResampleFilter;
    private CbAudioScorer mCbAudioScorer;
    private AcrRecognizer mAcrRecognizer;

    // 自采集相关
    private AudioCapture mAudioCapture;
    private AudioPlayerCapture mAudioPlayerCapture;
    private SrcPin<AudioBufFrame> mAudioLocalSrcPin;
    private SrcPin<AudioBufFrame> mAudioSendSrcPin;
    private SrcPin<AudioBufFrame> mAudioRemoteSrcPin;
    private AudioPreview mRemoteAudioPreview;
    private AudioPreview mLocalAudioPreview;

    // AEC/NS/AGC等处理
    private APMFilter mAPMFilter;
    // 输出前的重采样模块
    private AudioResampleFilter mAudioSendResampleFilter;
    // 录制前的重采样模块
    private AudioResampleFilter mAudioRecordResampleFilter;
    // 对mic, bgm进行混音，用于远端发送
    private AudioMixer mLocalAudioMixer;
    // 对mic, bgm, remote进行混音，用于录制
    private AudioMixer mRecordAudioMixer;
    private MediaCodecAudioEncoder mAudioEncoder;
    private MediaMuxerPublisher mFilePublisher;
    // 对纯人声进行录制
    private MediaCodecAudioEncoder mHumanVoiceAudioEncoder;
    private MediaMuxerPublisher mHumanVoiceFilePublisher;
    // debug录制用途
    private RawFrameWriter mRawFrameWriter;
    private RawFrameWriter mCapRawFrameWriter;
    private RawFrameWriter mBgmRawFrameWriter;

    // 伴奏状态相关
    private int mCdnType = 0;
    private String mAccUrlInUse;
    private long mAccStartTime;
    private boolean mIsAccPrepared;
    private boolean mAccPreparedSent = false;
    private long mAccRecoverPosition = 0;
    private int mAccRemainedLoopCount = 0;

    private HeadSetReceiver mHeadSetReceiver;
    protected boolean mHeadSetPlugged = false;
    protected boolean mBluetoothPlugged = false;

    // 视频相关参数
    protected int mScreenRenderWidth = 0;
    protected int mScreenRenderHeight = 0;
    protected int mPreviewResolution = VIDEO_RESOLUTION_360P;
    protected int mPreviewWidthOrig = 0;
    protected int mPreviewHeightOrig = 0;
    protected int mPreviewWidth = 0;
    protected int mPreviewHeight = 0;
    protected float mPreviewFps = 0;
    protected int mPreviewMixerWidth = 0;
    protected int mPreviewMixerHeight = 0;
    protected int mTargetResolution = VIDEO_RESOLUTION_360P;
    protected int mTargetWidthOrig = 0;
    protected int mTargetHeightOrig = 0;
    protected int mTargetWidth = 0;
    protected int mTargetHeight = 0;
    protected float mTargetFps = 0;
    protected int mRotateDegrees = 0;

    protected boolean mFrontCameraMirror = false;
    protected int mCameraFacing = CameraCapture.FACING_FRONT;
    protected boolean mIsCaptureStarted = false;
    protected boolean mDelayedStartCameraPreview = false;


    protected ZqLSCredentialHolder mSCHolder = null;

    @Override
    public void onUserJoined(int uid, int elapsed) {
        MyLog.i(TAG, "onUserJoined" + " uid=" + uid + " elapsed=" + elapsed);
        // 主播加入了，自己不会回调，自己回到角色变化接口
        UserStatus userStatus = ensureJoin(uid, "onUserMuteAudio");
        userStatus.setAnchor(true);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_JOIN, userStatus));
        tryStartRecordForFeedback("onUserJoined");
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        // 用户离开
        UserStatus userStatus = mUserStatusMap.remove(uid);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_LEAVE, userStatus));
        MyLog.i(TAG, "onUserOffline mUserStatusMap=" + mUserStatusMap);
        tryStopRecordForFeedback("onUserOffline");
    }

    @Override
    public void onUserMuteVideo(int uid, boolean muted) {
        UserStatus status = ensureJoin(uid, "onUserMuteAudio");
        status.setVideoMute(muted);
        status.setAnchor(true);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_MUTE_VIDEO, status));
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        UserStatus status = ensureJoin(uid, "onUserMuteAudio");
        status.setAudioMute(muted);
        status.setAnchor(true);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_MUTE_AUDIO, status));
    }

    @Override
    public void onUserEnableVideo(int uid, boolean enabled) {
        UserStatus status = ensureJoin(uid, "onUserEnableVideo");
        status.setEnableVideo(enabled);
        status.setAnchor(true);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_VIDEO_ENABLE, status));
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        UserStatus status = ensureJoin(uid, "onFirstRemoteVideoDecoded");
        status.setEnableVideo(true);
        status.setFirstVideoDecoded(true);
        status.setFirstVideoWidth(width);
        status.setFirstVideoHeight(height);

        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_FIRST_REMOTE_VIDEO_DECODED, status));
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        MyLog.d(TAG, "onJoinChannelSuccess" + " channel=" + channel + " uid=" + uid + " elapsed=" + elapsed);
        mConfig.setJoinChannelSuccess(true);
        initWhenInChannel();
        UserStatus userStatus = ensureJoin(uid, "onJoinChannelSuccess");
//        userStatus.setIsSelf(true);
        mConfig.setSelfUid(uid);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_JOIN, userStatus));
        StatisticsAdapter.recordCalculateEvent("agora", "join_duration", System.currentTimeMillis() - mConfig.getJoinRoomBeginTs(), null);
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_TIMEOUT);
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_AGAIN);
        }
        tryPlayPendingMixingMusic("onJoinChannelSuccess");
        mAgoraRTCAdapter.muteLocalAudioStream(mConfig.isLocalAudioStreamMute());
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_SELF_JOIN_SUCCESS));
    }

    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        MyLog.i(TAG, "onRejoinChannelSuccess" + " channel=" + channel + " uid=" + uid + " elapsed=" + elapsed);
        mConfig.setJoinChannelSuccess(true);
        UserStatus userStatus = ensureJoin(uid, "onRejoinChannelSuccess");
//        userStatus.setIsSelf(true);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_REJOIN, userStatus));
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        mConfig.setJoinChannelSuccess(false);
    }

    @Override
    public void onClientRoleChanged(int oldRole, int newRole) {
        MyLog.i(TAG, "onClientRoleChanged" + " oldRole=" + oldRole + " newRole=" + newRole);
        mCustomHandlerThread.removeMessage(MSG_ROLE_CHANGE_TIMEOUT);
        if (mConfig.getSelfUid() > 0) {
            UserStatus userStatus = ensureJoin(mConfig.getSelfUid(), "onClientRoleChanged");
            if (newRole == Constants.CLIENT_ROLE_BROADCASTER) {
                userStatus.setAnchor(true);
                tryStartRecordForFeedback("onClientRoleChanged");
                tryPlayPendingMixingMusic("onClientRoleChanged");
            } else {
                userStatus.setAnchor(false);
                tryStopRecordForFeedback("onClientRoleChanged");
            }
        }
        // 只有切换时才会触发
        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_USER_ROLE_CHANGE);
        EngineEvent.RoleChangeInfo roleChangeInfo = new EngineEvent.RoleChangeInfo(oldRole, newRole);
        engineEvent.obj = roleChangeInfo;
        EventBus.getDefault().post(engineEvent);
    }

    @Override
    public void onVideoSizeChanged(int uid, int width, int height, int rotation) {

    }

    @Override
    public void onAudioMixingFinished() {
        MyLog.i(TAG, "onAudioMixingFinished");
        mConfig.setMixMusicPlaying(false);
        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_FINISH, null);
        EventBus.getDefault().post(engineEvent);
    }

    @Override
    public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {
        List<EngineEvent.UserVolumeInfo> l = new ArrayList<>();
        for (IRtcEngineEventHandler.AudioVolumeInfo info : speakers) {
//            MyLog.i(TAG,"onAudioVolumeIndication" + " info=" + info.uid+" volume="+info.volume);
            /**
             * 如果是自己的声音 id 是0 。
             */
            EngineEvent.UserVolumeInfo userVolumeInfo = new EngineEvent.UserVolumeInfo(info.uid, info.volume);
            l.add(userVolumeInfo);
        }
        if (l.isEmpty()) {
            return;
        }
        if (l.size() == 1 && l.get(0).getUid() == 0 && l.get(0).getVolume() == 0) {
            return;
        }
        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION, null);
        engineEvent.obj = l;
        EventBus.getDefault().post(engineEvent);
    }


    /**
     * AUDIO_ROUTE_DEFAULT(-1)：使用默认的音频路由。
     * AUDIO_ROUTE_HEADSET(0)：使用耳机为语音路由。
     * AUDIO_ROUTE_EARPIECE(1)：使用听筒为语音路由。
     * AUDIO_ROUTE_HEADSETNOMIC(2)：使用不带麦的耳机为语音路由。
     * AUDIO_ROUTE_SPEAKERPHONE(3)：使用手机的扬声器为语音路由。
     * AUDIO_ROUTE_LOUDSPEAKER(4)：使用外接的扬声器为语音路由。
     * AUDIO_ROUTE_HEADSETBLUETOOTH(5)：使用蓝牙耳机为语音路由。
     *
     * @param routing
     */
    @Override
    public void onAudioRouteChanged(int routing) {
        MyLog.w(TAG, "onAudioRouteChanged 音频路由发生变化 routing=" + routing);
    }

    @Override
    public void onRecordingBuffer(byte[] samples) {
        // TODO: remove this later
    }

    @Override
    public void onWarning(int warn) {
        MyLog.i(TAG, "onWarning" + " warn=" + warn);
    }

    @Override
    public void onError(int error) {
        MyLog.i(TAG, "onError" + " error=" + error);
        if (error == Constants.ERR_JOIN_CHANNEL_REJECTED) {
            // 加入 channel 失败，在不要token时，传入token也会触发这个
            if (mCustomHandlerThread != null) {
                mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_AGAIN);
                mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_TIMEOUT);
                mCustomHandlerThread.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mStatus == STATUS_INITED) {
                            if (TextUtils.isEmpty(mLastJoinChannelToken)) {
                                MyLog.w(TAG, "上一次加入房间没有token，加入失败，那这次使用token");
                                joinRoomInner2(mRoomId, mConfig.getSelfUid(), getToken(mRoomId));
                            } else {
                                MyLog.w(TAG, "上一次加入房间有token，加入失败，那这次不用了");
                                joinRoomInner2(mRoomId, mConfig.getSelfUid(), null);
                            }
                        }
                    }
                });

            }
        } else if (error == Constants.ERR_INVALID_TOKEN) {
            // token验证失败
            if (mCustomHandlerThread != null) {
                mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_AGAIN);
                mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_TIMEOUT);
                mCustomHandlerThread.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mStatus == STATUS_INITED) {
                            String token = getToken(mRoomId);
                            joinRoomInner2(mRoomId, mConfig.getSelfUid(), token);
                        }
                    }
                });
            }
        }
    }

    /**
     * state	状态码：
     * MEDIA_ENGINE_AUDIO_EVENT_MIXING_PLAY(710)：音乐文件正常播放
     * MEDIA_ENGINE_AUDIO_EVENT_MIXING_PAUSED(711)：音乐文件暂停播放
     * MEDIA_ENGINE_AUDIO_EVENT_MIXING_STOPPED(713)：音乐文件停止播放
     * MEDIA_ENGINE_AUDIO_EVENT_MIXING_ERROR(714)：音乐文件报错。SDK 会在 errorCode 参数中返回具体的报错原因
     * errorCode	错误码：
     * MEDIA_ENGINE_AUDIO_ERROR_MIXING_OPEN(701)：音乐文件打开出错
     * MEDIA_ENGINE_AUDIO_ERROR_MIXING_TOO_FREQUENT(702)：音乐文件打开太频繁
     * MEDIA_ENGINE_AUDIO_EVENT_MIXING_INTERRUPTED_EOF(703)：音乐文件播放异常中断
     *
     * @param state
     * @param errorCode
     */
    @Override
    public void onAudioMixingStateChanged(int state, int errorCode) {
        MyLog.i(TAG, "onAudioMixingStateChanged" + " state=" + state + " errorCode=" + errorCode);

        // 伴奏状态上报
        if (state == Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_PLAY) {
            mIsAccPrepared = true;
            doUploadAccStartEvent(0);
            if (mAccPreparedSent) {
                return;
            }
            mAccPreparedSent = true;
        } else if (state == Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_ERROR) {
            doUploadAccStopEvent(1, errorCode);
            if (mCustomHandlerThread != null) {
                if (mIsAccPrepared) {
                    mAccRecoverPosition = getAudioMixingCurrentPosition();
                    if (mConfig.isUseExternalAudio()) {
                        mAccRemainedLoopCount = mAudioPlayerCapture.getRemainedLoopCount();
                    }
                }
                mCustomHandlerThread.postDelayed(() -> {
                    if (!mConfig.isMixMusicPlaying()) {
                        return;
                    }
                    MyLog.i(TAG, "retry acc playback with pos: " + mAccRecoverPosition + " remainLoopCount: " + mAccRemainedLoopCount);
                    doStopAudioMixing();
                    doStartAudioMixing(mAccUrlInUse, mAccRecoverPosition, mAccRemainedLoopCount);
                }, 100);
            }
            return;
        }

        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_STATE_CHANGE, null);
        engineEvent.obj = new EngineEvent.MusicStateChange(state, errorCode);
        EventBus.getDefault().post(engineEvent);
    }

    private UserStatus ensureJoin(int uid, String from) {
        if (!mUserStatusMap.containsKey(uid)) {
            MyLog.i(TAG, "ensureJoin" + " uid=" + uid + " from=" + from);
            UserStatus userStatus = new UserStatus(uid);
            userStatus.setEnterTs(System.currentTimeMillis());
            mUserStatusMap.put(uid, userStatus);
            return userStatus;
        } else {
            return mUserStatusMap.get(uid);
        }
    }

    private static class ZqEngineKitHolder {
        private static final ZqEngineKit INSTANCE = new ZqEngineKit();
    }

    private ZqEngineKit() {
        mGLRender = new GLRender();
        mAgoraRTCAdapter = AgoraRTCAdapter.create(mGLRender);
        mAgoraRTCAdapter.setOutCallback(this);
        mAcrRecognizer = new AcrRecognizer();

        mTokenEnable = U.getPreferenceUtils().getSettingBoolean(PREF_KEY_TOKEN_ENABLE, false);
        mSCHolder = new ZqLSCredentialHolder();
        initWorkThread();
    }

    private void initWorkThread() {
        mCustomHandlerThread = new CustomHandlerThread(TAG) {
            @Override
            protected void processMessage(Message msg) {
                if (msg.what == MSG_JOIN_ROOM_AGAIN) {
                    MyLog.i(TAG, "processMessage MSG_JOIN_ROOM_AGAIN 再次加入房间");
                    JoinParams joinParams = (JoinParams) msg.obj;
                    joinRoomInner(joinParams.roomID, joinParams.userId, joinParams.token);
                } else if (msg.what == MSG_JOIN_ROOM_TIMEOUT) {
                    MyLog.i(TAG, "handleMessage 加入房间超时");
                    StatisticsAdapter.recordCountEvent("agora", "join_timeout", null);
                    JoinParams joinParams = (JoinParams) msg.obj;
                    joinRoomInner2(joinParams.roomID, joinParams.userId, joinParams.token);
                } else if (msg.what == MSG_ROLE_CHANGE_TIMEOUT) {
                    MyLog.i(TAG, "handleMessage 身份切换超时");
                    mAgoraRTCAdapter.setClientRole(mConfig.isAnchor());
                }
            }
        };
    }

    public static ZqEngineKit getInstance() {
        return ZqEngineKitHolder.INSTANCE;
    }

    public void init(final String from, final Params params) {
        mInitFrom = from;
        mCustomHandlerThread.post(new LogRunnable("init" + " from=" + from + " params=" + params) {
            @Override
            public void realRun() {
                /**
                 * 注意这里 如果 init 后立马 执行其他 runnable 会被remove掉
                 */
                destroyInner();
                initInner(from, params);
            }
        });
    }

    private void initInner(String from, Params params) {
        mStatus = STATUS_INITING;
        mInitFrom = from;
        mConfig = params;
        mContext = U.app().getApplicationContext();

        // 使用config里的默认值
//        mConfig.setUseExternalAudio(true);
//        mConfig.setEnableInEarMonitoring(true);
        mConfig.setUseLocalAPM(false);

        // TODO: engine代码合并后，采样率初始值在Params初始化时获取
        mConfig.setAudioSampleRate(AudioUtil.getNativeSampleRate(mContext));
        MyLog.i(TAG, "Audio native sampleRate: " + mConfig.getAudioSampleRate());

        initModules();
        mAgoraRTCAdapter.init(mConfig);
        mCbAudioScorer.init(mConfig);
        mAcrRecognizer.init(mConfig);
        if (SCORE_DEBUG) {
            mAudioDummyFilter.init(SCORE_DEBUG_PATH, mConfig);
        }
        doSetAudioEffect(mConfig.getStyleEnum(), true);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        // 回调消息前更新状态
        mStatus = STATUS_INITED;
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_ENGINE_INITED));
    }

    private void initModules() {
        MyLog.i(TAG, "Latency test ProductModel: " + U.getDeviceUtils().getProductModel() +
                " brand: " + U.getDeviceUtils().getProp("ro.product.brand") +
                " manufacturer: " + U.getDeviceUtils().getProp("ro.product.manufacturer") +
                " name: " + U.getDeviceUtils().getProp("ro.product.name") +
                " device: " + U.getDeviceUtils().getProp("ro.product.device"));
        MyLog.i(TAG, "isUseExternalAudio: " + mConfig.isUseExternalAudio() +
                " isUseExternalVideo: " + mConfig.isUseExternalVideo() +
                " isUseExternalRecord: " + mConfig.isUseExternalAudioRecord() +
                " isEnableAudioLowLatency: " + mConfig.isEnableAudioLowLatency() +
                " accMixLatency: " + mConfig.getConfigFromServerNotChange().getAccMixingLatencyOnSpeaker() +
                " " + mConfig.getConfigFromServerNotChange().getAccMixingLatencyOnHeadset());

        if (mConfig.isEnableAudio()) {
            initAudioModules();
        }

        if (mConfig.isEnableVideo()) {
            initVideoModules();
            mBytedEffectFilter.initDyEffects();
        }
    }

    @SuppressWarnings("unchecked")
    private void initAudioModules() {
        MyLog.i(TAG, "initAudioModules");
        mAudioFilterMgt = new AudioFilterMgt();
        mAudioReverbFilter = new AudioReverbFilter();
        mScoreResampleFilter = new AudioResampleFilter();
        mCbAudioScorer = new CbAudioScorer();
        // 单mic数据PCM录制
        mRawFrameWriter = new RawFrameWriter();

        if (mConfig.isUseExternalAudio()) {
            mAudioCapture = new AudioCapture(mContext);
            mAudioCapture.setSampleRate(mConfig.getAudioSampleRate());
            mAudioPlayerCapture = new AudioPlayerCapture(mContext);
            mAudioPlayerCapture.setOutFormat(new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16,
                    mConfig.getAudioSampleRate(), mConfig.getAudioChannels()));
            mRemoteAudioPreview = new AudioPreview(mContext, mConfig.isEnableAudioLowLatency());
            mLocalAudioPreview = new AudioPreview(mContext, mConfig.isEnableAudioLowLatency());
            mAPMFilter = new APMFilter();

            // debug录制的相关连接
            mCapRawFrameWriter = new RawFrameWriter();
            mBgmRawFrameWriter = new RawFrameWriter();
            mAudioCapture.getSrcPin().connect((SinkPin<AudioBufFrame>) mCapRawFrameWriter.getSinkPin());
            mAudioPlayerCapture.getSrcPin().connect((SinkPin<AudioBufFrame>) mBgmRawFrameWriter.getSinkPin());

            // 远端声音播放
            mAudioRemoteSrcPin = mAgoraRTCAdapter.getRemoteAudioSrcPin();
            mAudioRemoteSrcPin.connect(mRemoteAudioPreview.getSinkPin());

            if (mConfig.isUseLocalAPM()) {
                mAudioCapture.getSrcPin().connect(mAPMFilter.getSinkPin());
                mAudioLocalSrcPin = mAPMFilter.getSrcPin();

                // 开启降噪模块
                mAPMFilter.enableNs(true);
                mAPMFilter.setNsLevel(APMFilter.NS_LEVEL_1);
                // 开启AEC
                mAPMFilter.enableAEC(true);
            } else {
                mAudioLocalSrcPin = mAudioCapture.getSrcPin();
            }

            mAudioCapture.setAudioCaptureListener(mOnAudioCaptureListener);
            mAudioPlayerCapture.setOnPreparedListener(new AudioPlayerCapture.OnPreparedListener() {
                @Override
                public void onPrepared(AudioPlayerCapture audioPlayerCapture) {
                    MyLog.i(TAG, "AudioPlayerCapture onPrepared");
                    // TODO: 预加载完成通知
                    onAudioMixingStateChanged(Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_PLAY, 0);
                }
            });
            mAudioPlayerCapture.setOnCompletionListener(new AudioPlayerCapture.OnCompletionListener() {
                @Override
                public void onCompletion(AudioPlayerCapture audioPlayerCapture) {
                    MyLog.i(TAG, "AudioPlayerCapture onCompletion");
                    onAudioMixingFinished();
                }
            });
            mAudioPlayerCapture.setOnFirstAudioFrameDecodedListener(new AudioPlayerCapture.OnFirstAudioFrameDecodedListener() {
                @Override
                public void onFirstAudioFrameDecoded(AudioPlayerCapture audioFileCapture, long time) {
                    MyLog.i(TAG, "AudioPlayerCapture onFirstAudioFrameDecoded: " + time);
                    EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_FIRST_PKT);
                    engineEvent.setObj(time);
                    EventBus.getDefault().post(engineEvent);
                }
            });
            mAudioPlayerCapture.setOnErrorListener(new AudioPlayerCapture.OnErrorListener() {
                @Override
                public void onError(AudioPlayerCapture audioPlayerCapture, int type, long msg) {
                    MyLog.e(TAG, "AudioPlayerCapture error: " + type);
                    // TODO: 伴奏播放出错
                    onAudioMixingStateChanged(Constants.MEDIA_ENGINE_AUDIO_EVENT_MIXING_ERROR, type);

                    EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_ERROR);
                    engineEvent.setObj(type);
                    EventBus.getDefault().post(engineEvent);
                }
            });
        } else {
            mAudioLocalSrcPin = mAgoraRTCAdapter.getLocalAudioSrcPin();

            AudioResampleFilter copyFilter = new AudioResampleFilter();
            copyFilter.setOutFormat(new AudioBufFormat(-1, -1, -1), true);
            mAgoraRTCAdapter.getRemoteAudioSrcPin().connect(copyFilter.getSinkPin());
            mAudioRemoteSrcPin = copyFilter.getSrcPin();
        }

        // 纯人声录制的连接
        mHumanVoiceAudioEncoder = new MediaCodecAudioEncoder();
        mHumanVoiceFilePublisher = new MediaMuxerPublisher();
        mAudioLocalSrcPin.connect(mHumanVoiceAudioEncoder.getSinkPin());
        mHumanVoiceAudioEncoder.getSrcPin().connect(mHumanVoiceFilePublisher.getAudioSink());
        mHumanVoiceFilePublisher.setPubListener(mPubListener);

        // 打分重采样, 使用16k采样率, 单声道
        AudioBufFormat scoreFormat = new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16, SCORE_SAMPLE_RATE, 1);
        mScoreResampleFilter.setOutFormat(scoreFormat, true);
        mScoreResampleFilter.setEnableLowLatency(false);
        if (SCORE_DEBUG) {
            mAudioDummyFilter = new AudioDummyFilter();
            mAudioLocalSrcPin.connect(mAudioDummyFilter.getSinkPin());
            mAudioDummyFilter.getSrcPin().connect(mScoreResampleFilter.getSinkPin());
            SrcPin<AudioBufFrame> scoreSrcPin = mScoreResampleFilter.getSrcPin();
            scoreSrcPin.connect(mCbAudioScorer.getSinkPin());
            scoreSrcPin.connect(mAcrRecognizer.getSinkPin());
            mAudioDummyFilter.getSrcPin().connect(mAudioFilterMgt.getSinkPin());
        } else {
            mAudioLocalSrcPin.connect(mScoreResampleFilter.getSinkPin());
            SrcPin<AudioBufFrame> scoreSrcPin = mScoreResampleFilter.getSrcPin();
            scoreSrcPin.connect(mCbAudioScorer.getSinkPin());
            scoreSrcPin.connect(mAcrRecognizer.getSinkPin());
            // 连接音效模块
            mAudioLocalSrcPin.connect(mAudioFilterMgt.getSinkPin());
        }

        // 添加音效滤镜
        mAudioFilterMgt.setFilter(mAudioReverbFilter);

        if (mConfig.isUseExternalAudio() || mConfig.isUseExternalAudioRecord()) {
            // 录制时的重采样模块, 需要录制时再连接
            mAudioRecordResampleFilter = new AudioResampleFilter();
            mRecordAudioMixer = new AudioMixer();

            if (mConfig.isUseExternalAudio()) {
                // 连接耳返模块
                mAudioFilterMgt.getSrcPin().connect(mLocalAudioPreview.getSinkPin());
            }
            // PCM dump
            mAudioFilterMgt.getSrcPin().connect((SinkPin<AudioBufFrame>) mRawFrameWriter.getSinkPin());

            if (mConfig.isUseExternalAudio()) {
                // 转换下声道数
                mAudioSendResampleFilter = new AudioResampleFilter();
                mAudioSendResampleFilter.setOutFormat(new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16,
                        mConfig.getAudioSampleRate(), mConfig.getAudioChannels()));
                mLocalAudioMixer = new AudioMixer();
                mLocalAudioPreview.getSrcPin().connect(mAudioSendResampleFilter.getSinkPin());
                mAudioSendResampleFilter.getSrcPin().connect(mLocalAudioMixer.getSinkPin(0));
                mAudioPlayerCapture.getSrcPin().connect(mLocalAudioMixer.getSinkPin(1));
                mLocalAudioMixer.getSrcPin().connect(mAgoraRTCAdapter.getAudioSinkPin());
                mAudioSendSrcPin = mLocalAudioMixer.getSrcPin();
            } else {
                AudioResampleFilter copyFilter = new AudioResampleFilter();
                copyFilter.setOutFormat(new AudioBufFormat(-1, -1, -1), true);
                mAudioFilterMgt.getSrcPin().connect(copyFilter.getSinkPin());
                mAudioSendSrcPin = copyFilter.getSrcPin();
            }

            // 录制功能
            mAudioEncoder = new MediaCodecAudioEncoder();
            mFilePublisher = new MediaMuxerPublisher();
            mAudioRecordResampleFilter.getSrcPin().connect(mAudioEncoder.getSinkPin());
            mAudioEncoder.getSrcPin().connect(mFilePublisher.getAudioSink());
            mFilePublisher.setPubListener(mPubListener);
        } else {
            mAudioFilterMgt.getSrcPin().connect((SinkPin<AudioBufFrame>) mRawFrameWriter.getSinkPin());
        }

        if (mConfig.isUseExternalAudio()) {
            AudioManager localAudioManager = (AudioManager) mContext.getSystemService(Context
                    .AUDIO_SERVICE);
            mHeadSetPlugged = localAudioManager.isWiredHeadsetOn();
            mBluetoothPlugged = localAudioManager.isBluetoothA2dpOn() || localAudioManager.isBluetoothScoOn();
            toggleAEC();
            registerHeadsetPlugReceiver();

            // 初始参数配置
            if (mConfig.isEnableAudioLowLatency()) {
                mAudioCapture.setAudioCaptureType(AudioCapture.AUDIO_CAPTURE_TYPE_OPENSLES);
                mAudioPlayerCapture.setAudioPlayerType(AudioPlayerCapture.AUDIO_PLAYER_TYPE_OPENSLES);
                mAudioPlayerCapture.setEnableLowLatency(true);
            } else {
                mAudioCapture.setAudioCaptureType(AudioCapture.AUDIO_CAPTURE_TYPE_AUDIORECORDER);
                mAudioPlayerCapture.setAudioPlayerType(AudioPlayerCapture.AUDIO_PLAYER_TYPE_AUDIOTRACK);
                mAudioPlayerCapture.setEnableLowLatency(false);
            }
            int mixingLatency = mHeadSetPlugged ? mConfig.getConfigFromServerNotChange().getAccMixingLatencyOnHeadset()
                    : mConfig.getConfigFromServerNotChange().getAccMixingLatencyOnSpeaker();
            mLocalAudioMixer.setDelay(1, mixingLatency);
            mAudioCapture.setVolume(mConfig.getRecordingSignalVolume() / 100.f);
            mRemoteAudioPreview.setVolume(mConfig.getPlaybackSignalVolume() / 100.f);
            mLocalAudioPreview.setVolume(mConfig.getEarMonitoringVolume() / 100.f);
            mAudioPlayerCapture.setPlayoutVolume(mConfig.getAudioMixingPlayoutVolume() / 100.f);
            setAudioMixingPublishVolume(mConfig.getAudioMixingPublishVolume());
            if (mConfig.isEnableInEarMonitoring() && shouldStartAudioPreview()) {
                mLocalAudioPreview.start();
            }
        }
    }

    private AudioCapture.OnAudioCaptureListener mOnAudioCaptureListener = new AudioCapture.OnAudioCaptureListener() {
        @Override
        public void onStatusChanged(int status) {
            MyLog.i(TAG, "AudioCapture onStatusChanged: " + status);
        }

        @Override
        public void onFirstPacketReceived(long time) {
            MyLog.i(TAG, "AudioCapture onFirstPacketReceived: " + time);
            if (mConfig.isRecordingForBusi()) {
                EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_RECORD_AUDIO_FIRST_PKT);
                engineEvent.setObj(time);
                EventBus.getDefault().post(engineEvent);
            }
        }

        @Override
        public void onError(int errorCode) {
            MyLog.e(TAG, "AudioCapture onError err: " + errorCode);
        }
    };

    private Publisher.PubListener mPubListener = new Publisher.PubListener() {
        @Override
        public void onInfo(int type, long msg) {
            MyLog.i(TAG, "FilePubListener onInfo type: " + type + " msg: " + msg);
            if (type == Publisher.INFO_STOPPED) {
                EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_RECORD_FINISHED));
            }
        }

        @Override
        public void onError(int err, long msg) {
            MyLog.e(TAG, "FilePubListener onError err: " + err + " msg: " + msg);
            EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_RECORD_ERROR));
        }
    };

    private void connectRecord(AudioBufFormat format) {
        if (mConfig.isUseExternalAudioRecord() || mConfig.isUseExternalAudio()) {
            mAudioRecordResampleFilter.setOutFormat(format);
            mAudioRecordResampleFilter.setEnableLowLatency(false);
            // TODO: 非主播模式下, 需要用远端音频驱动录制
            mRecordAudioMixer.setMainSinkPinIndex((mConfig.isAnchor() || !mConfig.isJoinChannelSuccess()) ? 0 : 1);
            mAudioSendSrcPin.connect(mRecordAudioMixer.getSinkPin(0));
            mAudioRemoteSrcPin.connect(mRecordAudioMixer.getSinkPin(1));
            mRecordAudioMixer.getSrcPin().connect(mAudioRecordResampleFilter.getSinkPin());
        }
    }

    private void disconnectRecord() {
        if (mConfig.isUseExternalAudioRecord() || mConfig.isUseExternalAudio()) {
            // 先断开非主索引的连接
            if (mRecordAudioMixer.getMainSinkPinIndex() == 0) {
                mAudioRemoteSrcPin.disconnect(mRecordAudioMixer.getSinkPin(1), false);
                mAudioSendSrcPin.disconnect(mRecordAudioMixer.getSinkPin(0), false);
            } else {
                mAudioSendSrcPin.disconnect(mRecordAudioMixer.getSinkPin(0), false);
                mAudioRemoteSrcPin.disconnect(mRecordAudioMixer.getSinkPin(1), false);
            }
            mRecordAudioMixer.getSrcPin().disconnect(mAudioRecordResampleFilter.getSinkPin(), false);
        }
    }

    private void toggleAEC() {
        if (mConfig.isUseExternalAudio()) {
            if (mHeadSetPlugged || mBluetoothPlugged) {
                if (mConfig.isUseLocalAPM()) {
                    // 开启APM处理后耳返延迟会增加20ms左右，当前在耳机模式下直接bypass掉
                    //mAPMFilter.enableAEC(false);
                    mAPMFilter.setBypass(true);
                } else {
                    mAgoraRTCAdapter.setEnableAPM(false);
                }
            } else {
                if (mConfig.isUseLocalAPM()) {
                    //mAPMFilter.enableAEC(true);
                    mAPMFilter.setBypass(false);
                } else {
                    mAgoraRTCAdapter.setEnableAPM(true);
                }
            }

            // 调节伴奏混音音量
            setAudioMixingPublishVolume(mConfig.getAudioMixingPublishVolume());
        }
    }

    public boolean isInit() {
        return mStatus == STATUS_INITED;
    }

    /**
     * 注意：在初始化完成前获取到的配置可能是不正确的，如需保证获取到的配置是init时传入的，
     * 可以在收到EngineEvent.TYPE_ENGINE_INITED事件后再进行下一步的处理。
     *
     * @return Params实例
     */
    public Params getParams() {
        return mConfig;
    }

    /**
     * 离开房间
     */
    public void leaveChannel() {
        stopAudioMixing();
        mCustomHandlerThread.post(new LogRunnable("leaveChannel") {
            @Override
            public void realRun() {
                if (mConfig.isUseExternalAudio()) {
                    mRemoteAudioPreview.stop();
                    mLocalAudioPreview.stop();
                    mAudioCapture.stop();
                    mAudioPlayerCapture.stop();
                    doStopAudioRecordingInner();
                }
                mAgoraRTCAdapter.leaveChannel();
            }
        });
    }

    /**
     * 销毁所有
     */
    public void destroy(final String from) {
        MyLog.i(TAG, "destroy" + " from=" + from);
        if (!"force".equals(from)) {
            if (mInitFrom != null && !mInitFrom.equals(from)) {
                MyLog.i(TAG, "mInitFrom=" + mInitFrom + " from=" + from + " cancel");
                return;
            }
        }
        // 销毁前清理掉其他的异步任务
        mCustomHandlerThread.removeCallbacksAndMessages(null);
        // 销毁前停止伴奏播放, 保证统计事件的发送
        stopAudioMixing();
        mCustomHandlerThread.post(new LogRunnable("destroy" + " from=" + from + " status=" + mStatus) {
            @Override
            public void realRun() {
                if (from.equals(mInitFrom)) {
                    destroyInner();
                }
            }
        });
    }

    private void destroyInner() {
        MyLog.i(TAG, "destroy inner");
        // destroy前必须先停止所有录制
        doStopAudioRecordingInner();
        mConfig.setAnchor(false);
        MyLog.i(TAG, "destroyInner1");
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_AGAIN);
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_TIMEOUT);
        }
        if (mStatus == STATUS_INITED) {
            mStatus = STATUS_UNINITING;
            if (mMusicTimePlayTimeListener != null && !mMusicTimePlayTimeListener.isDisposed()) {
                mMusicTimePlayTimeListener.dispose();
            }
            mConfig.setJoinChannelSuccess(false);
            MyLog.i(TAG, "destroyInner11");
            {
                // 释放录制相关模块
                mRecordAudioMixer.getSrcPin().disconnect(false);
                mAudioRecordResampleFilter.release();
                mRecordAudioMixer.release();
            }
            MyLog.i(TAG, "destroyInner12");
            if (mConfig.isUseExternalAudio()) {
                // 如果有连接Mixer, 主idx的AudioSource需要最后release
                mAudioPlayerCapture.release();
                MyLog.i(TAG, "destroyInner13");
                mAudioCapture.release();
                unregisterHeadsetPlugReceiver();
            }
            MyLog.i(TAG, "destroyInner2");
            if (mConfig.isEnableVideo() && mConfig.isUseExternalVideo()) {
                mCameraCapture.release();
                mGLRender.release();
            }
            MyLog.i(TAG, "destroyInner3");
            mAgoraRTCAdapter.destroy(true);
            mUserStatusMap.clear();
            mRemoteViewCache.clear();
            mRemoteUserPinMap.clear();
            MyLog.i(TAG, "destroyInner4");
            mConfig = new Params();
            mPendingStartMixAudioParams = null;
            mIsCaptureStarted = false;
            // 发送消息前，更新状态
            mStatus = STATUS_UNINIT;
            EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_ENGINE_DESTROY, null));
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
            MyLog.i(TAG, "destroyInner5");
        }
    }

    public void startRecord() {
        if (mConfig.isUseExternalAudio()) {
//            CbEngineAdapter.getInstance().startRecord();
        } else {
            U.getToastUtil().showShort("mConfig.isUseZqEngine is false ，cancel");
        }
    }

    private String getToken(String roomId) {
        MyLog.i(TAG, "getToken" + " roomId=" + roomId);
        EngineServerApi agoraTokenApi = ApiManager.getInstance().createService(EngineServerApi.class);
        if (agoraTokenApi != null) {
            Call<ApiResult> apiResultCall = agoraTokenApi.getToken(roomId);
            if (apiResultCall != null) {
                try {
                    Response<ApiResult> resultResponse = apiResultCall.execute();
                    if (resultResponse != null) {
                        ApiResult obj = resultResponse.body();
                        if (obj != null) {
                            if (obj.getErrno() == 0) {
                                String token = obj.getData().getString("token");
                                MyLog.i(TAG, "getToken 成功 token=" + token);
                                return token;
                            }
                        } else {
                            MyLog.w(TAG, "syncMyInfoFromServer obj==null");
                        }
                    }
                } catch (Exception e) {
                    MyLog.e(e);
                }
            }
        }
        return null;
    }

    /**
     * 加入agora的房间
     *
     * @param roomid
     * @param userId
     * @param isAnchor 是否以主播的身份
     *                 不是主播只看不能说
     */
    public void joinRoom(final String roomid, final int userId, final boolean isAnchor, final String token) {
        mCustomHandlerThread.post(new LogRunnable("joinRoom" + " roomid=" + roomid + " userId=" + userId + " isAnchor=" + isAnchor + " token=" + token) {
            @Override
            public void realRun() {
                mConfig.setSelfUid(userId);
                if (mConfig.getChannelProfile() == Params.CHANNEL_TYPE_LIVE_BROADCASTING) {
                    mConfig.setAnchor(isAnchor);
                    mAgoraRTCAdapter.setClientRole(isAnchor);
                    if (isAnchor) {
                        mCustomHandlerThread.removeMessage(MSG_ROLE_CHANGE_TIMEOUT);
                        Message msg = mCustomHandlerThread.obtainMessage();
                        msg.what = MSG_ROLE_CHANGE_TIMEOUT;
                        mCustomHandlerThread.sendMessageDelayed(msg, 3000);
                    }
                }
                joinRoomInner(roomid, userId, token);
                //TODO 临时关闭耳返
//                if (U.getDeviceUtils().getHeadsetPlugOn()) {
//                    setEnableSpeakerphone(false);
//                    enableInEarMonitoring(false);
//                } else {
//                    setEnableSpeakerphone(true);
//                    enableInEarMonitoring(false);
//                }
            }
        });
    }


    private void joinRoomInner(final String roomid, final int userId, final String token) {
        if (Looper.myLooper() != mCustomHandlerThread.getLooper()) {
            mCustomHandlerThread.post(new LogRunnable("joinRoomInner" + " roomid=" + roomid + " userId=" + userId + " token=" + token) {
                @Override
                public void realRun() {
                    joinRoomInner(roomid, userId, token);
                }
            });
            return;
        }
        mRoomId = roomid;
        String token2 = token;
        if (mTokenEnable) {
            MyLog.i(TAG, "joinRoomInner 明确告知已经启用token了 token=" + token2);
            if (TextUtils.isEmpty(token2)) {
                // 但是token2还为空，短链接要个token
                token2 = getToken(roomid);
            } else {
                // token不为空，继续使用
            }
        } else {
            MyLog.i(TAG, "joinRoomInner 未启用token，一是真的未启用，二是启用了不知道");
            if (TextUtils.isEmpty(token)) {
                // 没有token
            } else {
                // 但是已经有token了
            }
        }
        joinRoomInner2(roomid, userId, token2);
    }

    private void joinRoomInner2(final String roomid, final int userId, final String token) {
        MyLog.i(TAG, "joinRoomInner2" + " roomid=" + roomid + " userId=" + userId + " token=" + token);
        mLastJoinChannelToken = token;
        mAgoraRTCAdapter.leaveChannel();

        int retCode = 0;
        // TODO: 自采集模式下，练歌房不需要加入声网房间
        if (mConfig.getScene() == Params.Scene.audiotest &&
                mConfig.isUseExternalAudio() && mConfig.isUseExternalVideo()) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    onJoinChannelSuccess(roomid, userId, 0);
                }
            });
        } else {
            retCode = mAgoraRTCAdapter.joinChannel(token, roomid, "Extra Optional Data", userId, mSCHolder);
            MyLog.i(TAG, "joinRoomInner2" + " retCode=" + retCode);
        }

        if (retCode < 0) {
            HashMap map = new HashMap();
            map.put("reason", "" + retCode);
            StatisticsAdapter.recordCountEvent("agora", "join_failed", map);
            Message msg = mCustomHandlerThread.obtainMessage();
            msg.what = MSG_JOIN_ROOM_AGAIN;
            JoinParams joinParams = new JoinParams();
            joinParams.roomID = roomid;
            joinParams.token = token;
            joinParams.userId = userId;
            msg.obj = joinParams;
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_AGAIN);
            mCustomHandlerThread.sendMessageDelayed(msg, 4000);
        } else {
            //告诉我成功
            mConfig.setJoinRoomBeginTs(System.currentTimeMillis());
            Message msg = mCustomHandlerThread.obtainMessage();
            msg.what = MSG_JOIN_ROOM_TIMEOUT;
            JoinParams joinParams = new JoinParams();
            joinParams.roomID = roomid;
            joinParams.token = token;
            joinParams.userId = userId;
            msg.obj = joinParams;
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_TIMEOUT);
            mCustomHandlerThread.sendMessageDelayed(msg, 3000);
        }
    }

    public void setClientRole(final boolean isAnchor) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("setClientRole" + " isAnchor=" + isAnchor) {
                @Override
                public void realRun() {
                    if (mConfig.isUseExternalAudio() && mConfig.isJoinChannelSuccess()) {
                        if (isAnchor) {
                            mAudioCapture.start();
                            if (mConfig.isEnableInEarMonitoring() && shouldStartAudioPreview()) {
                                mLocalAudioPreview.start();
                            }
                        } else {
                            mAudioCapture.stop();
                            mLocalAudioPreview.stop();
                        }
                    }
                    mAgoraRTCAdapter.setClientRole(isAnchor);
                    if (isAnchor != mConfig.isAnchor()) {
                        mConfig.setAnchor(isAnchor);
                        mCustomHandlerThread.removeMessage(MSG_ROLE_CHANGE_TIMEOUT);
                        Message msg = mCustomHandlerThread.obtainMessage();
                        msg.what = MSG_ROLE_CHANGE_TIMEOUT;
                        mCustomHandlerThread.sendMessageDelayed(msg, 3000);
                    }
                }
            });
        }
    }

    /*音频基础开始*/

    public void setEnableSpeakerphone(final boolean enableSpeakerphone) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    mConfig.setEnableSpeakerphone(enableSpeakerphone);
                    mAgoraRTCAdapter.setEnableSpeakerphone(enableSpeakerphone);
                }
            });
        }
    }

    /**
     * 一些必须在频道内才能出事
     */
    private void initWhenInChannel() {
        // 初始化各个音量
        adjustRecordingSignalVolume(mConfig.getRecordingSignalVolume(), false);
        adjustPlaybackSignalVolume(mConfig.getPlaybackSignalVolume(), false);
        adjustAudioMixingPlayoutVolume(mConfig.getAudioMixingPlayoutVolume(), false);
        adjustAudioMixingPublishVolume(mConfig.getAudioMixingPublishVolume(), false);
        enableInEarMonitoring(mConfig.isEnableInEarMonitoring());

        // 成功后，自采集模式下开启采集
        if (mConfig.isUseExternalAudio()) {
            if (mConfig.isAnchor()) {
                mAudioCapture.start();
            }
            mRemoteAudioPreview.start();
        }
    }

    /**
     * 监听耳机插拔
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeviceUtils.HeadsetPlugEvent event) {
        if (event.on) {
            setEnableSpeakerphone(false);
            enableInEarMonitoring(false);
        } else {
            setEnableSpeakerphone(true);
            enableInEarMonitoring(false);
        }
    }

    /**
     * 两个方法的区别是
     * enableLocalAudio：开启或关闭本地语音采集及处理
     * muteLocalAudioStream：停止或继续发送本地音频流
     *
     * @param muted
     */
    public void muteLocalAudioStream(final boolean muted) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("muteLocalAudioStream muted=" + muted) {
                @Override
                public void realRun() {
                    UserStatus status = new UserStatus(mConfig.getSelfUid());
                    status.setAudioMute(muted);
                    EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_MUTE_AUDIO, status));
                    mConfig.setLocalAudioStreamMute(muted);
                    mAgoraRTCAdapter.muteLocalAudioStream(muted);
                }
            });
        }
    }

    /**
     * 接收/停止接收所有音频流。
     * 适用于 A 在唱歌，B C 能互相聊天，但不能打扰到 A 的场景
     */
    public void muteAllRemoteAudioStreams(final boolean muted) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    mConfig.setAllRemoteAudioStreamsMute(muted);
                    mAgoraRTCAdapter.muteAllRemoteAudioStreams(muted);
                }
            });
        }
    }

    /**
     * 开启或者关闭🎧耳返
     * 默认关闭
     */
    public void enableInEarMonitoring(final boolean enable) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    enableInEarMonitoringInternal(enable, true);
                }
            });
        }
    }

    private boolean shouldStartAudioPreview() {
        return (mHeadSetPlugged || mConfig.isEnableAudioPreviewLatencyTest()) &&
                !mConfig.isEnableAudioMixLatencyTest();
    }

    private void enableInEarMonitoringInternal(boolean enable, boolean setConfig) {
        if (setConfig) {
            mConfig.setEarMonitoringSwitch(enable?1:2);
        }
        if (mConfig.isUseExternalAudio()) {
            if (!mConfig.getConfigFromServerNotChange().hasServerConfig) {
                MyLog.i(TAG, "without server config, switch audio latency mode to " + enable);
                doSetEnableAudioLowLatency(enable);
            }
            if (enable) {
                if (shouldStartAudioPreview()) {
                    mLocalAudioPreview.start();
                }
            } else {
                mLocalAudioPreview.stop();
            }
        } else {
            mAgoraRTCAdapter.enableInEarMonitoring(enable);
        }
    }

    /**
     * 设定耳返音量
     *
     * @param volume 默认100
     */
    public void setInEarMonitoringVolume(final int volume) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    mConfig.setInEarMonitoringVolume(volume);
                    if (mConfig.isUseExternalAudio()) {
                        mLocalAudioPreview.setVolume(volume / 100.f);
                    } else {
                        mAgoraRTCAdapter.setInEarMonitoringVolume(volume);
                    }
                }
            });
        }
    }

    /**
     * 录音音量，可在 0~400 范围内进行调节 默认100
     *
     * @param volume
     */
    public void adjustRecordingSignalVolume(final int volume) {
        adjustRecordingSignalVolume(volume, true);
    }

    public void adjustRecordingSignalVolume(final int volume, final boolean setConfig) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("adjustRecordingSignalVolume " + volume + " setConfig: " + setConfig) {
                @Override
                public void realRun() {
                    if (setConfig) {
                        mConfig.setRecordingSignalVolume(volume);
                    }
                    if (mConfig.isUseExternalAudio()) {
                        mAudioCapture.setVolume(volume / 100.0f);
                    } else {
                        mAgoraRTCAdapter.adjustRecordingSignalVolume(volume);
                    }
                }
            });
        }
    }

    /**
     * 播放音量，可在 0~400 范围内进行调节 默认100
     *
     * @param volume
     */
    public void adjustPlaybackSignalVolume(final int volume) {
        adjustPlaybackSignalVolume(volume, true);
    }

    public void adjustPlaybackSignalVolume(final int volume, final boolean setConfig) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("adjustPlaybackSignalVolume " + volume + " setConfig: " + setConfig) {
                @Override
                public void realRun() {
                    if (setConfig) {
                        mConfig.setPlaybackSignalVolume(volume);
                    }
                    if (mConfig.isUseExternalAudio()) {
                        mRemoteAudioPreview.setVolume(volume / 100.f);
                    } else {
                        mAgoraRTCAdapter.adjustPlaybackSignalVolume(volume);
                    }
                }
            });
        }
    }
    /*音频基础结束*/

    /*音频高级扩展开始*/

    private void doSetAudioEffect(Params.AudioEffect styleEnum, boolean fromInit) {
        if (styleEnum == mConfig.getStyleEnum() && !fromInit) {
            return;
        }

        mConfig.setStyleEnum(styleEnum);

        if (mAudioReverbFilter != null) {
            int type = AudioReverbFilter.AUDIO_REVERB_NONE;
            switch (styleEnum) {
                case none:
                    type = AudioReverbFilter.AUDIO_REVERB_NONE;
                    break;
                case ktv:
                    type = AudioReverbFilter.AUDIO_REVERB_NEW_CENT;
                    break;
                case rock:
                    type = AudioReverbFilter.AUDIO_REVERB_ROCK;
                    break;
                case liuxing:
                    type = AudioReverbFilter.AUDIO_REVERB_POPULAR;
                    break;
                case kongling:
                    type = AudioReverbFilter.AUDIO_REVERB_RNB;
                    break;
                default:
                    break;
            }
            mAudioReverbFilter.setReverbLevel(type);
        }

//        // 添加音效
//        List<AudioFilterBase> filters = new ArrayList<>(2);
//        if (styleEnum == Params.AudioEffect.ktv) {
//            filters.add(new CbAudioEffectFilter(5));
//        } else if (styleEnum == Params.AudioEffect.rock) {
//            filters.add(new CbAudioEffectFilter(2));
//        } else if (styleEnum == Params.AudioEffect.liuxing) {
//            filters.add(new CbAudioEffectFilter(3));
//        } else if (styleEnum == Params.AudioEffect.kongling) {
//            filters.add(new CbAudioEffectFilter(1));
//        }
//
////        filters.add(new TbAudioAgcFilter(mConfig));
//
//        if (mAudioFilterMgt != null) {
//            mAudioFilterMgt.setFilter(filters);
//        }
    }

    public void setAudioEffectStyle(final Params.AudioEffect styleEnum) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("setAudioEffectStyle") {
                @Override
                public void realRun() {
                    doSetAudioEffect(styleEnum, false);
                }
            });
        }
    }

    private void doSetEnableAudioPreviewLatencyTest(boolean enable) {
        mConfig.setEnableAudioPreviewLatencyTest(enable);
        if (mConfig.isUseExternalAudio()) {
            mAudioCapture.setEnableLatencyTest(enable);
            if (enable) {
                mAudioCapture.setVolume(8.0f);
                mAudioPlayerCapture.setVolume(0.f);
                mLocalAudioPreview.start();
            } else {
                mAudioCapture.setVolume(mConfig.getRecordingSignalVolume() / 100.f);
                mAudioPlayerCapture.setVolume(mConfig.getPlaybackSignalVolume() / 100.f);
                if (!mHeadSetPlugged && !mBluetoothPlugged) {
                    mLocalAudioPreview.stop();
                }
            }
        }
    }

    /**
     * 开启或关闭耳返延迟测试
     */
    public void setEnableAudioPreviewLatencyTest(final boolean enable) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("setEnableAudioPreviewLatencyTest: " + enable) {
                @Override
                public void realRun() {
                    if (enable) {
                        doSetEnableAudioMixLatencyTest(false);
                    }
                    doSetEnableAudioPreviewLatencyTest(enable);
                }
            });
        }
    }

    private void doSetEnableAudioMixLatencyTest(boolean enable) {
        mConfig.setEnableAudioMixLatencyTest(enable);
        if (mConfig.isUseExternalAudio()) {
            if (enable) {
                mLocalAudioPreview.stop();
            }
            setAudioMixingPublishVolume(mConfig.getAudioMixingPublishVolume());
            mAudioPlayerCapture.setEnableLatencyTest(enable);
            mLocalAudioMixer.setEnableLatencyTest(enable);
        }
    }

    /**
     * 开启或关闭混音延迟测试
     */
    public void setEnableAudioMixLatencyTest(final boolean enable) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("setEnableAudioMixLatencyTest: " + enable) {
                @Override
                public void realRun() {
                    if (enable) {
                        doSetEnableAudioPreviewLatencyTest(false);
                    }
                    doSetEnableAudioMixLatencyTest(enable);
                }
            });
        }
    }

    private void doSetEnableAudioLowLatency(boolean enable) {
        if (mConfig.isUseExternalAudio()) {
            if (enable) {
                mAudioCapture.setAudioCaptureType(AudioCapture.AUDIO_CAPTURE_TYPE_OPENSLES);
                mAudioPlayerCapture.setAudioPlayerType(AudioPlayerCapture.AUDIO_PLAYER_TYPE_OPENSLES);
                mAudioPlayerCapture.setEnableLowLatency(true);
            } else {
                mAudioCapture.setAudioCaptureType(AudioCapture.AUDIO_CAPTURE_TYPE_AUDIORECORDER);
                mAudioPlayerCapture.setAudioPlayerType(AudioPlayerCapture.AUDIO_PLAYER_TYPE_AUDIOTRACK);
                mAudioPlayerCapture.setEnableLowLatency(false);
            }
            if (mRemoteAudioPreview.isEnableLowLatency() != enable) {
                MyLog.i(TAG, "recreate RemoteAudioPreview");
                mAudioRemoteSrcPin.disconnect(mRemoteAudioPreview.getSinkPin(), false);
                mRemoteAudioPreview.release();
                mRemoteAudioPreview = new AudioPreview(mContext, enable);
                mRemoteAudioPreview.setVolume(mConfig.getPlaybackSignalVolume() / 100.f);
                if (mConfig.isJoinChannelSuccess()) {
                    mRemoteAudioPreview.start();
                }
                mAudioRemoteSrcPin.connect(mRemoteAudioPreview.getSinkPin());
            }
            if (mLocalAudioPreview.isEnableLowLatency() != enable) {
                MyLog.i(TAG, "recreate LocalAudioPreview");
                mLocalAudioPreview.getSrcPin().disconnect(mAudioSendResampleFilter.getSinkPin(), false);
                mAudioFilterMgt.getSrcPin().disconnect(mLocalAudioPreview.getSinkPin(), false);
                mLocalAudioPreview.release();
                mLocalAudioPreview = new AudioPreview(mContext, enable);
                mLocalAudioPreview.setVolume(mConfig.getEarMonitoringVolume() / 100.f);
                if (mConfig.isEnableInEarMonitoring() && shouldStartAudioPreview()) {
                    if (mAudioCapture.isRecordingState()) {
                        mLocalAudioPreview.start();
                    }
                }
                mLocalAudioPreview.getSrcPin().connect(mAudioSendResampleFilter.getSinkPin());
                mAudioFilterMgt.getSrcPin().connect(mLocalAudioPreview.getSinkPin());
            }
        }
    }

    /**
     * 开启或关闭低延迟音频模式
     */
    public void setEnableAudioLowLatency(final boolean enable) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("setEnableAudioLowLatency: " + enable) {
                @Override
                public void realRun() {
                    mConfig.setEnableAudioLowLatency(enable);
                    // 这里在延迟测试模式下会改变参数值
                    mConfig.getConfigFromServerNotChange().setEnableAudioLowLatency(enable);
                    mConfig.getConfigFromServerNotChange().save2Pref();
                    doSetEnableAudioLowLatency(enable);
                }
            });
        }
    }

    /**
     * 播放音效
     */
    public void playEffects(final EffectModel effectModel) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    mAgoraRTCAdapter.playEffects(effectModel);
                }
            });
        }
    }

    public List<EffectModel> getAllEffects() {
        return mAgoraRTCAdapter.getAllEffects();
    }

    /**
     * 设置本地语音音调。
     * <p>
     * 该方法改变本地说话人声音的音调。
     * 可以在 [0.5, 2.0] 范围内设置。取值越小，则音调越低。默认值为 1.0，表示不需要修改音调。
     *
     * @param pitch
     */
    public void setLocalVoicePitch(double pitch) {
        MyLog.i(TAG, "setLocalVoicePitch" + " pitch=" + pitch);
        mConfig.setLocalVoicePitch(pitch);
        mAgoraRTCAdapter.setLocalVoicePitch(pitch);
    }

    /**
     * 设置本地语音音效均衡
     */
    public void setLocalVoiceEqualization() {
        mAgoraRTCAdapter.setLocalVoiceEqualization(mConfig.getBandFrequency(), mConfig.getBandGain());
    }

    /**
     * 设置本地音效混响。
     *
     * @param reverbKey 混响音效 Key。该方法共有 5 个混响音效 Key，分别如 value 栏列出。
     * @param value     AUDIO_REVERB_DRY_LEVEL(0)：原始声音强度，即所谓的 dry signal，取值范围 [-20, 10]，单位为 dB
     *                  AUDIO_REVERB_WET_LEVEL(1)：早期反射信号强度，即所谓的 wet signal，取值范围 [-20, 10]，单位为 dB
     *                  AUDIO_REVERB_ROOM_SIZE(2)：所需混响效果的房间尺寸，一般房间越大，混响越强，取值范围 [0, 100]，单位为 dB
     *                  AUDIO_REVERB_WET_DELAY(3)：Wet signal 的初始延迟长度，取值范围 [0, 200]，单位为毫秒
     *                  AUDIO_REVERB_STRENGTH(4)：混响持续的强度，取值范围为 [0, 100]
     */
    public void setLocalVoiceReverb(int reverbKey, int value) {
        mConfig.setLocalVoiceReverb(reverbKey, value);
        mAgoraRTCAdapter.setLocalVoiceReverb(reverbKey, value);
    }

    PendingStartMixAudioParams mPendingStartMixAudioParams;

    public void startAudioMixing(String filePath, int cycle) {
        startAudioMixing(filePath, null, 0, cycle);
    }

    /**
     * 开始播放音乐文件及混音。
     * 播放伴奏结束后，会收到 onAudioMixingFinished 回调
     *
     * @param filePath 指定需要混音的本地或在线音频文件的绝对路径。支持d的音频格式包括：mp3、mp4、m4a、aac、3gp、mkv、wav 及 flac。详见 Supported Media Formats。
     *                 如果用户提供的目录以 /assets/ 开头，则去 assets 里面查找该文件
     *                 如果用户提供的目录不是以 /assets/ 开头，一律认为是在绝对路径里查找该文件
     * @param cycle    指定音频文件循环播放的次数：
     *                 正整数：循环的次数
     *                 -1：无限循环
     */
    public void startAudioMixing(final String filePath, final String midiPath, final long mixMusicBeginOffset, final int cycle) {
        startAudioMixing(0, filePath, midiPath, mixMusicBeginOffset, cycle);
    }

    public void startAudioMixing(final int uid, final String filePath, final String midiPath, final long mixMusicBeginOffset, final int cycle) {
        if (mCustomHandlerThread != null) {
//            final String filePath = MediaCacheManager.INSTANCE.getProxyUrl("http://song-static-1.inframe.mobi/bgm/28995bcaee647a8ebba90fd4b6492820.mp3", true);
//            final String filePath = "http://song-static.inframe.mobi/bgm/28995bcaee647a8ebba90fd4b6492820.mp3";
            mCustomHandlerThread.post(new LogRunnable("startAudioMixing" + " uid=" + uid + " filePath=" + filePath + " midiPath=" + midiPath + " mixMusicBeginOffset=" + mixMusicBeginOffset + " cycle=" + cycle) {
                @Override
                public void realRun() {
                    if (TextUtils.isEmpty(filePath)) {
                        MyLog.i(TAG, "伴奏路径非法");
                        return;
                    }
                    boolean canGo = false;
                    if (uid <= 0) {
                        canGo = true;
                    } else {
                        UserStatus userStatus = mUserStatusMap.get(uid);
                        MyLog.i(TAG, "startAudioMixing userStatus=" + userStatus);
                        if ((userStatus == null || !mConfig.isJoinChannelSuccess()) && !mConfig.isUseExternalAudio()) {
                            MyLog.w(TAG, "该用户还未在频道中,且用得是声网的混音，播伴奏挂起");
                            canGo = false;
                        } else {
                            MyLog.w(TAG, "用户已经在频道中继续走起");
                            canGo = true;
                        }
                    }
                    if (canGo) {
                        mConfig.setMixMusicPlaying(true);
                        mConfig.setMixMusicFilePath(filePath);
                        mConfig.setMidiPath(midiPath);
                        mConfig.setMixMusicBeginOffset(mixMusicBeginOffset);

                        startMusicPlayTimeListener();
                        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_START);
                        EventBus.getDefault().post(engineEvent);

                        mAccPreparedSent = false;
                        mAccRecoverPosition = 0;
                        mAccRemainedLoopCount = cycle;
                        mAccUrlInUse = getUrlToPlay();
                        doStartAudioMixing(mAccUrlInUse, 0, cycle);
                    } else {
                        mPendingStartMixAudioParams = new PendingStartMixAudioParams();
                        mPendingStartMixAudioParams.uid = uid;
                        mPendingStartMixAudioParams.filePath = filePath;
                        mPendingStartMixAudioParams.midiPath = midiPath;
                        mPendingStartMixAudioParams.mixMusicBeginOffset = mixMusicBeginOffset;
                        mPendingStartMixAudioParams.cycle = cycle;
                    }
                }
            });
        }
    }

    private void doStartAudioMixing(String url, long startOffset, int loopCount) {
        mAccStartTime = System.currentTimeMillis();
        mIsAccPrepared = false;
        if (mConfig.isUseExternalAudio()) {
            mAudioPlayerCapture.start(url, startOffset, -1, loopCount);
        } else {
            mAgoraRTCAdapter.startAudioMixing(url, false, false, loopCount);
            if (startOffset > 0) {
                mAgoraRTCAdapter.setAudioMixingPosition((int) startOffset);
            }
        }
    }

    private boolean isHttpUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private String getUrlToPlay() {
        String url = mConfig.getMixMusicFilePath();
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        String urlToPlay = url;
        if (isHttpUrl(urlToPlay)) {
            if (mCdnType == 3 || mCdnType == 4) {
                urlToPlay = urlToPlay.replaceFirst("://song-static", "://song-static-1");
            }
            if (mCdnType == 2 || mCdnType == 4) {
                urlToPlay = MediaCacheManager.INSTANCE.getProxyUrl(urlToPlay, true);
            }
        }
        return urlToPlay;
    }

    private void doUploadAccStartEvent(int errCode) {
        MyLog.i(TAG, "doUploadAccStartEvent " + errCode);
        long now = System.currentTimeMillis();
        long prepareTime = -1;
        if (errCode == 0 && mAccStartTime > 0) {
            prepareTime = now - mAccStartTime;
        }

        Skr.PlayerStartInfo startInfo = new Skr.PlayerStartInfo();
        startInfo.cdnType = mCdnType;
        startInfo.extAudio = mConfig.isUseExternalAudio() ? 1 : 0;
        startInfo.url = mConfig.getMixMusicFilePath();
        startInfo.urlInUse = mAccUrlInUse;
        startInfo.prepareTime = prepareTime;
        startInfo.errCode = errCode;
        SDataManager.getInstance().getDataHolder().addPlayerStartInfo(startInfo);
    }

    private void doUploadAccStopEvent(int stopReason, int errCode) {
        MyLog.i(TAG, "doUploadAccStopEvent reason: " + stopReason + " err: " + errCode);
        long now = System.currentTimeMillis();
        long duration = -1;
        if (mAccStartTime > 0) {
            duration = now - mAccStartTime;
        }

        Skr.PlayerStopInfo stopInfo = new Skr.PlayerStopInfo();
        stopInfo.cdnType = mCdnType;
        stopInfo.extAudio = mConfig.isUseExternalAudio() ? 1 : 0;
        stopInfo.url = mConfig.getMixMusicFilePath();
        stopInfo.urlInUse = mAccUrlInUse;
        stopInfo.duration = duration;
        stopInfo.isPrepared = mIsAccPrepared ? 1 : 0;
        stopInfo.stopReason = stopReason;
        stopInfo.errCode = errCode;
        SDataManager.getInstance().getDataHolder().addPlayerStopInfo(stopInfo);
    }

    public static class PendingStartMixAudioParams {
        int uid;
        String filePath;
        String midiPath;
        long mixMusicBeginOffset;
        int cycle;
    }

    private void tryPlayPendingMixingMusic(String from) {
        MyLog.i(TAG, "tryPlayPengdingMixingMusic" + " from=" + from);
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (mPendingStartMixAudioParams != null) {
                        MyLog.i(TAG, "播放之前挂起的伴奏 uid=" + mPendingStartMixAudioParams.uid);
                        startAudioMixing(mPendingStartMixAudioParams.uid,
                                mPendingStartMixAudioParams.filePath,
                                mPendingStartMixAudioParams.midiPath,
                                mPendingStartMixAudioParams.mixMusicBeginOffset,
                                mPendingStartMixAudioParams.cycle);
                        mPendingStartMixAudioParams = null;
                    } else {
                        MyLog.i(TAG, "没有伴奏挂起");
                    }
                }
            });
        }
    }

    /**
     * 停止播放音乐文件及混音。
     * 请在频道内调用该方法。
     */
    public void stopAudioMixing() {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("stopAudioMixing") {
                @Override
                public void realRun() {
                    if (!TextUtils.isEmpty(mConfig.getMixMusicFilePath())) {
                        doUploadAccStopEvent(0, 0);
                        mConfig.setMixMusicPlaying(false);
                        mConfig.setMixMusicFilePath(null);
                        mConfig.setMidiPath(null);
                        mConfig.setMixMusicBeginOffset(0);
                        mAccUrlInUse = null;
                        mIsAccPrepared = false;
                        mAccStartTime = 0;
                        stopMusicPlayTimeListener();
                        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_STOP);
                        EventBus.getDefault().post(engineEvent);

                        doStopAudioMixing();
                    }
                    mPendingStartMixAudioParams = null;
                    mConfig.setCurrentMusicTs(0);
                    mConfig.setRecordCurrentMusicTsTs(0);
                    mConfig.setLrcHasStart(false);
                }
            });
        }
    }

    private void doStopAudioMixing() {
        if (mConfig.isUseExternalAudio()) {
            mAudioPlayerCapture.stop();
        } else {
            mAgoraRTCAdapter.stopAudioMixing();
        }
    }

    /**
     * 继续播放混音
     */
    public void resumeAudioMixing() {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("resumeAudioMixing") {
                @Override
                public void realRun() {
                    if (!TextUtils.isEmpty(mConfig.getMixMusicFilePath())) {
                        mConfig.setMixMusicPlaying(true);
                        startMusicPlayTimeListener();
                        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_START);
                        EventBus.getDefault().post(engineEvent);

                        if (mConfig.isUseExternalAudio()) {
                            mAudioPlayerCapture.resume();
                        } else {
                            mAgoraRTCAdapter.resumeAudioMixing();
                        }
                    }
                }
            });
        }
    }

    /**
     * 暂停播放音乐文件及混音
     */
    public void pauseAudioMixing() {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("pauseAudioMixing") {
                @Override
                public void realRun() {
                    if (!TextUtils.isEmpty(mConfig.getMixMusicFilePath())) {
                        mConfig.setMixMusicPlaying(false);
                        stopMusicPlayTimeListener();
                        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_PAUSE);
                        EventBus.getDefault().post(engineEvent);

                        if (mConfig.isUseExternalAudio()) {
                            mAudioPlayerCapture.pause();
                        } else {
                            mAgoraRTCAdapter.pauseAudioMixing();
                        }
                    }
                }
            });
        }
    }

    private void startMusicPlayTimeListener() {
        if (mMusicTimePlayTimeListener != null && !mMusicTimePlayTimeListener.isDisposed()) {
            mMusicTimePlayTimeListener.dispose();
        }

        mMusicTimePlayTimeListener = Observable
                .interval(1000, 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe(new Consumer<Long>() {
                    int duration = -1;

                    @Override
                    public void accept(Long aLong) throws Exception {
                        int currentPosition = getAudioMixingCurrentPosition();
                        MyLog.d(TAG, "PlayTimeListener accept timerTs=" + aLong + " currentPosition=" + currentPosition);
                        mConfig.setCurrentMusicTs(currentPosition);
                        mConfig.setRecordCurrentMusicTsTs(System.currentTimeMillis());
                        if (duration < 0) {
                            duration = getAudioMixingDuration();
                        }
                        if (currentPosition < duration) {
                            EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER);
                            engineEvent.obj = new EngineEvent.MixMusicTimeInfo(currentPosition, duration);
                            EventBus.getDefault().post(engineEvent);
                        } else {
                            MyLog.i(TAG, "playtime不合法,currentPostion=" + currentPosition + " duration=" + duration);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLog.w(TAG, throwable.getMessage());
                    }
                });
    }

    private void stopMusicPlayTimeListener() {
        if (mMusicTimePlayTimeListener != null && !mMusicTimePlayTimeListener.isDisposed()) {
            mMusicTimePlayTimeListener.dispose();
        }
    }

    /**
     * 调节混音本地播放音量大小
     *
     * @param volume 1-100 默认100
     */
    public void adjustAudioMixingPlayoutVolume(final int volume) {
        adjustAudioMixingPlayoutVolume(volume, true);
    }

    public void adjustAudioMixingPlayoutVolume(final int volume, final boolean setConfig) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("adjustAudioMixingVolume" + " volume=" + volume + " setConfig=" + setConfig) {
                @Override
                public void realRun() {
                    if (setConfig) {
                        mConfig.setAudioMixingPlayoutVolume(volume);
                    }
                    if (mConfig.isUseExternalAudio()) {
                        mAudioPlayerCapture.setPlayoutVolume(volume / 100.f);
                    } else {
                        mAgoraRTCAdapter.adjustAudioMixingPlayoutVolume(volume);
                    }
                }
            });
        }
    }

    private boolean shouldMixAcc() {
        if (mHeadSetPlugged || mBluetoothPlugged) {
            return true;
        } else {
            return (mConfig.getConfigFromServerNotChange().hasServerConfig &&
                    mConfig.isEnableAudioLowLatency() == mConfig.getConfigFromServerNotChange().isEnableAudioLowLatency() &&
                    mConfig.getConfigFromServerNotChange().getAccMixingLatencyOnSpeaker() > 0);
        }
    }

    // 自采集下伴奏声音发送大小(对于未测定机型，外放模式下不进行混音，伴奏使用外放回采的声音)
    private void setAudioMixingPublishVolume(int volume) {
        if (mConfig.isUseExternalAudio()) {
            float val = shouldMixAcc() ? volume / 100.f : 0;
            val = mConfig.isEnableAudioMixLatencyTest() ? 1.0f : val;
            MyLog.i(TAG, "setAudioMixingPublishVolume to: " + val);
            mLocalAudioMixer.setInputVolume(1, val);
        }
    }

    /**
     * 调节音乐远端播放音量大小
     *
     * @param volume 1-100 默认100
     */
    public void adjustAudioMixingPublishVolume(final int volume, final boolean setConfig) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("adjustAudioMixingPublishVolume" + " volume=" + volume + " setConfig=" + setConfig) {
                @Override
                public void realRun() {
                    if (setConfig) {
                        mConfig.setAudioMixingPublishVolume(volume);
                    }
                    if (mConfig.isUseExternalAudio()) {
                        setAudioMixingPublishVolume(volume);
                    } else {
                        mAgoraRTCAdapter.adjustAudioMixingPublishVolume(volume);
                    }
                }
            });
        }
    }

    /**
     * @return 获取伴奏时长，单位ms
     */
    public int getAudioMixingDuration() {
        if (mConfig.isUseExternalAudio()) {
            return (int) mAudioPlayerCapture.getDuration();
        } else {
            return mAgoraRTCAdapter.getAudioMixingDuration();
        }
    }

    /**
     * @return 获取混音当前播放位置 ms
     */
    public int getAudioMixingCurrentPosition() {
        if (mConfig.isUseExternalAudio()) {
            return (int) mAudioPlayerCapture.getPosition();
        } else {
            return mAgoraRTCAdapter.getAudioMixingCurrentPosition();
        }
    }

    /**
     * 拖动混音进度条
     *
     * @param posMs
     */
    public void setAudioMixingPosition(final int posMs) {

        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new LogRunnable("setAudioMixingPosition" + " posMs=" + posMs) {
                @Override
                public void realRun() {
                    if (mConfig.isUseExternalAudio()) {
                        mAudioPlayerCapture.seek(posMs);
                    } else {
                        mAgoraRTCAdapter.setAudioMixingPosition(posMs);
                    }
                }
            });
        }

    }

    private long lastTrimFeedbackFileSizeTs = 0;

    private void tryStartRecordForFeedback(String from) {
        if (!OPEN_AUDIO_RECORD_FOR_CALLBACK) {
            return;
        }
        boolean hasAnchor = false;
        /**
         * 当前的主播id 即时是合唱也只有一个id，因为对于引擎来说，不确定后面还有人成为主播
         * 但录制要马上进行
         */
        String anchor = "";
        for (UserStatus us : mUserStatusMap.values()) {
            MyLog.i(TAG, " us=" + us);
            if (us.isAnchor()) {
                anchor = String.valueOf(us.getUserId());
                hasAnchor = true;
            }
        }

        if (!hasAnchor) {
            MyLog.i(TAG, "没有主播不录制 from=" + from);
            return;
        }
        if (mConfig.isRecordingForBusi()) {
            MyLog.i(TAG, "业务录制在进行中，取消 from=" + from);
            return;
        }
        if (mConfig.isRecordingForFeedback()) {
            MyLog.i(TAG, "反馈录制在进行中，取消 from=" + from);
            return;
        }


        if (mCustomHandlerThread != null) {
            // 延迟一秒才开始录制，是为了兼容，变成主播时，业务层马上调用的录制的情况，1s时间给业务层让步，让业务先录 。发现解决不了，去掉。
            mConfig.setRecordingForFeedback(true);
            startAudioRecordingInner(getFeedbackFilepath(anchor), false, mConfig.getAudioSampleRate(), 1, 48 * 1000);
            mCustomHandlerThread.post(new LogRunnable("trimFeedbackFileSize") {
                @Override
                public void realRun() {
                    if (System.currentTimeMillis() - lastTrimFeedbackFileSizeTs > 10 * 60 * 1000) {
                        trimFeedbackFileSize();
                    }
                }
            });
        }
    }

    public String getFeedbackFilepath(String anchors) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        String fileName = String.format("%s_%s@%s.m4a", mInitFrom, simpleDateFormat.format(new Date(System.currentTimeMillis())), anchors);
        String filePath = U.getAppInfoUtils().getFilePathInSubDir(AUDIO_FEEDBACK_DIR, fileName);
        return filePath;
    }

    /**
     * 保证用于存储用户反馈的文件夹size不会太大
     */
    private void trimFeedbackFileSize() {
        lastTrimFeedbackFileSizeTs = System.currentTimeMillis();
        long fileSize = 0;
        File feedbackDir = U.getAppInfoUtils().getSubDirFile(AUDIO_FEEDBACK_DIR);
        if (feedbackDir.exists() && feedbackDir.isDirectory()) {
            File[] fileList = feedbackDir.listFiles();
            if (fileList == null || fileList.length == 0) {
                return;
            }
            // 文件修改时间排序
            Arrays.sort(fileList, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0)
                        return 1;
                    else if (diff == 0)
                        return 0;
                    else
                        return -1;
                }
            });
            for (int i = fileList.length - 1; i >= 0; i--) {
                if (fileList[i].isFile()) {
                    if (fileList[i].getName().endsWith(".m4a")) {
                        if (fileSize > U.getLogUploadUtils().MAX_AUDIO_FOR_FEEDBACK) {
                            //删除掉
                            fileList[i].delete();
                        } else {
                            fileSize += fileList[i].length();
                        }
                    }
                }
            }
        }
    }

    private void tryStopRecordForFeedback(String from) {
        if (!OPEN_AUDIO_RECORD_FOR_CALLBACK) {
            return;
        }
        boolean hasAnchor = false;
        for (UserStatus us : mUserStatusMap.values()) {
            MyLog.i(TAG, " us=" + us);
            if (us.isAnchor()) {
                hasAnchor = true;
            }
        }
        if (hasAnchor) {
            MyLog.i(TAG, "仍有主播，不取消录制 from=" + from);
            return;
        }
        if (!mConfig.isRecordingForFeedback()) {
            MyLog.i(TAG, "反馈录制不在进行中，取消 from=" + from);
            return;
        }
        mConfig.setRecordingForFeedback(false);
        stopAudioRecordingInner("ForFeedback " + from);
    }

    public void startAudioRecording(final String path, final boolean recordHumanVoice) {
        if (mCustomHandlerThread != null) {
            if (mConfig.isRecordingForFeedback()) {
                mConfig.setRecordingForFeedback(false);
                stopAudioRecordingInner("业务录制开始要求停止");
            }
            mConfig.setRecordingForBusi(true);
            startAudioRecordingInner(path, recordHumanVoice, mConfig.getAudioSampleRate(), mConfig.getAudioChannels(), mConfig.getAudioBitrate());
        }
    }

    /**
     * 开始客户端录音。
     * 仅支持m4a格式。
     * 声网采集模式下，该接口需在加入频道之后调用，如果调用 leaveChannel 时还在录音，录音会自动停止。
     */
    private void startAudioRecordingInner(final String path, final boolean recordHumanVoice, final int sampleRate, final int channels, final int bitrate) {
        mCustomHandlerThread.post(new LogRunnable("startAudioRecording" + " path=" + path +
                " recordHumanVoice=" + recordHumanVoice + " mInChannel=" + mConfig.isJoinChannelSuccess() +
                " mConfig.isUseExternalAudioRecord()=" + mConfig.isUseExternalAudioRecord() +
                " " + sampleRate + "Hz channels: " + channels + " " + bitrate / 1000 + "kbps") {
            @Override
            public void realRun() {
                if (mStatus != STATUS_INITED) {
                    MyLog.e(TAG, "startAudioRecordingInner in invalid state: " + mStatus);
                    return;
                }

                File file = new File(path);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (file.exists()) {
                    file.delete();
                }

                if (!recordHumanVoice) {
                    // 非debug人声录制，需要录制的时候再连接
                    connectRecord(new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16, sampleRate, channels));
                }
                if (RECORD_FOR_DEBUG) {
                    mRawFrameWriter.start(path);
                    if (mConfig.isUseExternalAudio()) {
                        String subfix = path.substring(path.lastIndexOf('.'));
                        String name = path.substring(0, path.lastIndexOf('.'));
                        mCapRawFrameWriter.start(name + "_cap" + subfix);
                        mBgmRawFrameWriter.start(name + "_bgm" + subfix);
                    }
                } else {
                    EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_RECORD_START);
                    EventBus.getDefault().post(engineEvent);
                    if (mConfig.isUseExternalAudioRecord() || mConfig.isUseExternalAudio() || recordHumanVoice) {
                        // 未加入房间时需要先开启音频采集
                        if (mConfig.isUseExternalAudio()) {
                            if (!mConfig.isJoinChannelSuccess() && mAudioCapture != null) {
                                mAudioCapture.start();
                            }
                        }

                        AudioCodecFormat audioCodecFormat =
                                new AudioCodecFormat(AVConst.CODEC_ID_AAC,
                                        AVConst.AV_SAMPLE_FMT_S16,
                                        sampleRate, channels, bitrate);
                        if (recordHumanVoice) {
                            if (mConfig.isUseExternalAudio() && mAudioCapture != null) {
                                audioCodecFormat.sampleRate = mAudioCapture.getSampleRate();
                                audioCodecFormat.channels = mAudioCapture.getChannels();
                                audioCodecFormat.bitrate = 64000 * audioCodecFormat.channels;
                            }
                            mHumanVoiceAudioEncoder.configure(audioCodecFormat);
                            mHumanVoiceFilePublisher.setAudioOnly(true);
                            mHumanVoiceFilePublisher.setUrl(path);
                            mHumanVoiceAudioEncoder.start();
                        } else {
                            mAudioEncoder.configure(audioCodecFormat);
                            mFilePublisher.setAudioOnly(true);
                            mFilePublisher.setUrl(path);
                            mAudioEncoder.start();
                        }
                    } else {
                        mAgoraRTCAdapter.startAudioRecording(path, Constants.AUDIO_RECORDING_QUALITY_HIGH);
                    }
                }
            }
        });
    }

    /**
     * 停止客户端录音。
     * <p>
     * 该方法停止录音。
     * 声网采集模式下，该接口需要在 leaveChannel 之前调用，不然会在调用 leaveChannel 时自动停止。
     */
    public void stopAudioRecording() {
        if (mCustomHandlerThread != null) {
            if (mConfig.isRecordingForBusi()) {
                mConfig.setRecordingForBusi(false);
            }
            stopAudioRecordingInner("ForBusi");
        }
    }

    /**
     * 停止客户端录音。
     * <p>
     * 该方法停止录音。
     * 声网采集模式下，该接口需要在 leaveChannel 之前调用，不然会在调用 leaveChannel 时自动停止。
     */
    private void stopAudioRecordingInner(String from) {
        mCustomHandlerThread.post(new LogRunnable("stopAudioRecordingInner from=" + from) {
            @Override
            public void realRun() {
                if (mStatus != STATUS_INITED) {
                    MyLog.e(TAG, "stopAudioRecordingInner in invalid state: " + mStatus);
                    return;
                }
                doStopAudioRecordingInner();
            }
        });
    }

    private void doStopAudioRecordingInner() {
        if (mStatus != STATUS_INITED) {
            return;
        }
        if (!RECORD_FOR_DEBUG) {
            if (mConfig.isUseExternalAudioRecord()) {
                mHumanVoiceAudioEncoder.stop();
                mAudioEncoder.stop();
                // 未加入房间时需要停止音频采集
                if (!mConfig.isJoinChannelSuccess() && mAudioCapture != null) {
                    mAudioCapture.stop();
                }
            } else {
                mAgoraRTCAdapter.stopAudioRecording();
            }
        } else {
            mRawFrameWriter.stop();
            EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_RECORD_FINISHED));
            if (mConfig.isUseExternalAudio()) {
                mCapRawFrameWriter.stop();
                mBgmRawFrameWriter.stop();
            }
        }
        // 录制完成断开连接
        disconnectRecord();
    }

    public int getLineScore1() {
        return mCbAudioScorer.getScoreV1();
    }

    public void getLineScore2(int lineNum, Score2Callback callback) {
        mCbAudioScorer.getScoreV2(lineNum, callback);
    }

    /*音频高级扩展结束*/

    /*打分相关开始*/

    public void startRecognize(RecognizeConfig recognizeConfig) {
        mAcrRecognizer.startRecognize(recognizeConfig);
    }

    public void setRecognizeListener(AcrRecognizeListener recognizeConfig) {
        mAcrRecognizer.setRecognizeListener(recognizeConfig);
    }

    public void stopRecognize() {
        mAcrRecognizer.stopRecognize();
    }

    public void recognizeInManualMode(int lineNo) {
        mAcrRecognizer.recognizeInManualMode(lineNo);
    }

    /*打分相关结束*/

    public static class JoinParams {
        public int userId;
        public String roomID;
        public String token;
    }

    private void registerHeadsetPlugReceiver() {
        if (mHeadSetReceiver == null && mContext != null) {
            mHeadSetReceiver = new HeadSetReceiver();
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);

            filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
            filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            mContext.registerReceiver(mHeadSetReceiver, filter);
        }
    }

    private void unregisterHeadsetPlugReceiver() {
        if (mHeadSetReceiver != null) {
            mContext.unregisterReceiver(mHeadSetReceiver);
            mHeadSetReceiver = null;
        }
    }

    private class HeadSetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }

            int state = BluetoothHeadset.STATE_DISCONNECTED;
            if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                        BluetoothHeadset.STATE_DISCONNECTED);
                Log.d(TAG, "bluetooth state:" + state);
                if (state == BluetoothHeadset.STATE_CONNECTED) {
                    Log.d(TAG, "bluetooth Headset is plugged");
                    mBluetoothPlugged = true;
                } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                    Log.d(TAG, "bluetooth Headset is unplugged");
                    mBluetoothPlugged = false;
                }
            } else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED))// audio
            {
                state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                        BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    Log.d(TAG, "bluetooth Headset is plugged");
                    mBluetoothPlugged = true;
                } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    Log.d(TAG, "bluetooth Headset is unplugged");
                    mBluetoothPlugged = false;
                }
            } else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                state = intent.getIntExtra("state", -1);

                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset is unplugged");
                        mHeadSetPlugged = false;
                        break;
                    case 1:
                        Log.d(TAG, "Headset is plugged");
                        mHeadSetPlugged = true;
                        break;
                    default:
                        Log.d(TAG, "I have no idea what the headset state is");
                }
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) { //蓝牙开关
                state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                if (state == BluetoothAdapter.STATE_OFF) {
                    Log.d(TAG, "bluetooth Headset is unplugged");
                    mBluetoothPlugged = false;
                }
            }

            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    toggleAEC();
                    if (mConfig.isUseExternalAudio() && mConfig.isEnableInEarMonitoring()) {
                        if (shouldStartAudioPreview()) {
                            mLocalAudioPreview.start();
                        } else {
                            mLocalAudioPreview.stop();
                        }
                    }
                }
            });
        }
    }

    // 视频相关接口

    /**
     * Get {@link GLRender} instance.
     *
     * @return GLRender instance.
     */
    public GLRender getGLRender() {
        return mGLRender;
    }

    public ImgTexPreview getImgTexPreview() {
        return mImgTexPreview;
    }

    /**
     * 获取头条视频特效封装类。
     *
     * @return BytedEffectFilter instance.
     */
    public BytedEffectFilter getBytedEffectFilter() {
        return mBytedEffectFilter;
    }

    /**
     * Get {@link CameraCapture} module instance.
     *
     * @return CameraCapture instance.
     */
    public CameraCapture getCameraCapture() {
        return mCameraCapture;
    }

    private void initVideoModules() {
        MyLog.i(TAG, "initVideoModules");
        // Camera preview
        mCameraCapture = new CameraCapture(U.app().getApplicationContext(), mGLRender);
        mImgTexScaleFilter = new ImgTexScaleFilter(mGLRender);
        mBytedEffectFilter = new BytedEffectFilter(mGLRender);
        mImgTexMixer = new ImgTexMixer(mGLRender);
        mImgTexPreviewMixer = new ImgTexMixer(mGLRender);
        mImgTexPreviewMixer.setScalingMode(0, ImgTexMixer.SCALING_MODE_CENTER_CROP);
        mImgTexPreview = new ImgTexPreview(mGLRender);

        // 抖音的美颜特效处理需要先翻转图像
        mImgTexScaleFilter.setFlipVertical(true);
        mCameraCapture.getImgTexSrcPin().connect(mImgTexScaleFilter.getSinkPin());
        mImgTexScaleFilter.getSrcPin().connect(mBytedEffectFilter.getImgTexSinkPin());
        // 处理完后再翻转过来
        mImgTexPreviewMixer.setFlipVertical(0, true);
        mImgTexMixer.setFlipVertical(0, true);
        mBytedEffectFilter.getSrcPin().connect(mImgTexPreviewMixer.getSinkPin(0));
        mBytedEffectFilter.getSrcPin().connect(mImgTexMixer.getSinkPin(0));
        mImgTexPreviewMixer.getSrcPin().connect(mImgTexPreview.getSinkPin());
        mImgTexMixer.getSrcPin().connect(mAgoraRTCAdapter.getVideoSinkPin());

        // set listeners
        mImgTexPreview.getGLRender().addListener(mPreviewSizeChangedListener);

        mCameraCapture.setOnCameraCaptureListener(new CameraCapture.OnCameraCaptureListener() {
            @Override
            public void onStarted() {
                MyLog.i(TAG, "CameraCapture ready");
                EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_CAMERA_OPENED));
            }

            @Override
            public void onFirstFrameRendered() {
                MyLog.i(TAG, "CameraCapture onFirstFrameRendered");
                EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_CAMERA_FIRST_FRAME_RENDERED));
            }

            @Override
            public void onFacingChanged(int facing) {
                MyLog.i(TAG, "CameraCapture onFacingChanged");
                mCameraFacing = facing;
                updateFrontMirror();
                EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_CAMERA_FACING_CHANGED));
            }

            @Override
            public void onError(int err) {
                MyLog.e(TAG, "CameraCapture error: " + err);
                EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_CAMERA_ERROR));
            }
        });

        // init with offscreen GLRender
        mGLRender.init(1, 1);
    }


    /**
     * Should be called on Activity.onResume or Fragment.onResume.
     */
    public void onResume() {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                if (mStatus != STATUS_INITED) {
                    return;
                }
                MyLog.i(TAG, "onResume");
                mImgTexPreview.onResume();
            }
        });
    }

    /**
     * Should be called on Activity.onPause or Fragment.onPause.
     */
    public void onPause() {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                if (mStatus != STATUS_INITED) {
                    return;
                }
                MyLog.i(TAG, "onPause");
                mImgTexPreview.onPause();
            }
        });
    }

    /**
     * Set GLSurfaceView as camera previewer.<br/>
     * Must set once before the GLSurfaceView created.
     *
     * @param surfaceView GLSurfaceView to be set.
     */
    public void setDisplayPreview(final GLSurfaceView surfaceView) {
        mCustomHandlerThread.post(new LogRunnable("setDisplayPreview surfaceView=" + surfaceView) {
            @Override
            public void realRun() {
                if (mStatus != STATUS_INITED) {
                    return;
                }
                MyLog.i(TAG, "setDisplayPreview " + surfaceView);
                mImgTexPreview.setDisplayPreview(surfaceView);
            }
        });
    }

    /**
     * /**
     * Set TextureView as camera previewer.<br/>
     * Must set once before the TextureView ready.
     *
     * @param textureView TextureView to be set.
     */
    public void setDisplayPreview(final TextureView textureView) {
        mCustomHandlerThread.post(new LogRunnable("setDisplayPreview textureView=" + textureView) {
            @Override
            public void realRun() {
                if (mStatus != STATUS_INITED) {
                    return;
                }
                MyLog.i(TAG, "setDisplayPreview " + textureView);
                mImgTexPreview.setDisplayPreview(textureView);
            }
        });
    }

    public View getDisplayPreview() {
        if (mImgTexPreview != null) {
            return mImgTexPreview.getDisplayPreview();
        }
        return null;
    }

    /**
     * Set rotate degrees in anti-clockwise of current Activity.
     *
     * @param rotate Degrees in anti-clockwise, only 0, 90, 180, 270 accepted.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public void setRotateDegrees(final int rotate) throws IllegalArgumentException {
        mCustomHandlerThread.post(new LogRunnable("setRotateDegrees" + " rotate=" + rotate) {
            @Override
            public void realRun() {
                int degrees = rotate % 360;
                if (degrees % 90 != 0) {
                    throw new IllegalArgumentException("Invalid rotate degrees");
                }
                if (mRotateDegrees == degrees) {
                    return;
                }
                boolean isLastLandscape = (mRotateDegrees % 180) != 0;
                boolean isLandscape = (degrees % 180) != 0;
                if (isLastLandscape != isLandscape) {
                    if (mPreviewWidth > 0 || mPreviewHeight > 0) {
                        setPreviewResolution(mPreviewHeight, mPreviewWidth);
                    }
                    if (mTargetWidth > 0 || mTargetHeight > 0) {
                        setTargetResolution(mTargetHeight, mTargetWidth);
                    }
                }
                mRotateDegrees = degrees;
                mCameraCapture.setOrientation(mRotateDegrees);
            }
        });
    }

    /**
     * get rotate degrees
     *
     * @return degrees Degrees in anti-clockwise, only 0, 90, 180, 270 accepted.
     */
    public int getRotateDegrees() {
        return mRotateDegrees;
    }

    /**
     * Set camera capture resolution.<br/>
     * <p>
     * The set resolution would take effect on next {@link #startCameraPreview()}
     * {@link #startCameraPreview(int)} call.<br/>
     * <p>
     * Both of the set width and height must be greater than 0.
     *
     * @param width  capture width
     * @param height capture height
     */
    public void setCameraCaptureResolution(final int width, final int height) throws IllegalArgumentException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid resolution");
        }
        mCustomHandlerThread.post(new LogRunnable("setCameraCaptureResolution" + " width=" + width + " height=" + height) {
            @Override
            public void realRun() {
                mCameraCapture.setPreviewSize(width, height);
            }
        });
    }

    /**
     * Set camera capture resolution.<br/>
     * <p>
     * The set resolution would take effect on next {@link #startCameraPreview()}
     * {@link #startCameraPreview(int)} call.<br/>
     *
     * @param idx Resolution index.<br/>
     * @see #VIDEO_RESOLUTION_360P
     * @see #VIDEO_RESOLUTION_480P
     * @see #VIDEO_RESOLUTION_540P
     * @see #VIDEO_RESOLUTION_720P
     * @see #VIDEO_RESOLUTION_1080P
     */
    public void setCameraCaptureResolution(final int idx) throws IllegalArgumentException {
        if (idx < VIDEO_RESOLUTION_360P ||
                idx > VIDEO_RESOLUTION_1080P) {
            throw new IllegalArgumentException("Invalid resolution index");
        }
        mCustomHandlerThread.post(new LogRunnable("setCameraCaptureResolution" + " idx=" + idx) {
            @Override
            public void realRun() {
                int height = getShortEdgeLength(idx);
                int width = height * 16 / 9;
                mCameraCapture.setPreviewSize(width, height);
            }
        });
    }

    /**
     * Set preview resolution.<br/>
     * <p>
     * The set resolution would take effect on next {@link #startCameraPreview()}
     * {@link #startCameraPreview(int)} call, if called not in previewing mode.<br/>
     * If called in previewing mode, it would take effect immediately.<br/>
     * <p>
     * The set width and height must not be 0 at same time.
     * If one of the params is 0, the other would calculated by the actual preview view size
     * to keep the ratio of the preview view.
     *
     * @param width  preview width.
     * @param height preview height.
     */
    public void setPreviewResolution(final int width, final int height) throws IllegalArgumentException {
        if (width < 0 || height < 0 || (width == 0 && height == 0)) {
            throw new IllegalArgumentException("Invalid resolution");
        }
        mCustomHandlerThread.post(new LogRunnable("setPreviewResolution" + " width=" + width + " height=" + height) {
            @Override
            public void realRun() {
                mPreviewWidthOrig = width;
                mPreviewHeightOrig = height;
                doSetPreviewResolution();
            }
        });
    }

    /**
     * Set preview resolution index.<br/>
     * <p>
     * The set resolution would take effect on next {@link #startCameraPreview()}
     * {@link #startCameraPreview(int)} call, if called not in previewing mode.<br/>
     * If called in previewing mode, it would take effect immediately.<br/>
     *
     * @param idx Resolution index.<br/>
     * @see #VIDEO_RESOLUTION_360P
     * @see #VIDEO_RESOLUTION_480P
     * @see #VIDEO_RESOLUTION_540P
     * @see #VIDEO_RESOLUTION_720P
     * @see #VIDEO_RESOLUTION_1080P
     */
    public void setPreviewResolution(final int idx) throws IllegalArgumentException {
        if (idx < VIDEO_RESOLUTION_360P ||
                idx > VIDEO_RESOLUTION_1080P) {
            throw new IllegalArgumentException("Invalid resolution index");
        }
        mCustomHandlerThread.post(new LogRunnable("setPreviewResolution" + " idx=" + idx) {
            @Override
            public void realRun() {
                mPreviewResolution = idx;
                mPreviewWidthOrig = 0;
                mPreviewHeightOrig = 0;
                doSetPreviewResolution();
            }
        });
    }

    private void doSetPreviewResolution() {
        if (mScreenRenderWidth != 0 && mScreenRenderHeight != 0) {
            calResolution();
            mImgTexScaleFilter.setTargetSize(mPreviewWidth, mPreviewHeight);
            mImgTexPreviewMixer.setTargetSize(mPreviewWidth, mPreviewHeight);
        }
    }

    /**
     * get preview width
     *
     * @return preview width
     */
    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    /**
     * get preview height
     *
     * @return preview height
     */
    public int getPreviewHeight() {
        return mPreviewHeight;
    }

    /**
     * Set preview fps.<br/>
     * <p>
     * The set fps would take effect on next {@link #startCameraPreview()}
     * {@link #startCameraPreview(int)} call.<br/>
     * <p>
     * The actual preview fps depends on the running device, may be different with the set value.
     *
     * @param fps frame rate to be set.
     */
    public void setPreviewFps(float fps) throws IllegalArgumentException {
        if (fps <= 0) {
            throw new IllegalArgumentException("the fps must > 0");
        }
        mPreviewFps = fps;
        if (mTargetFps == 0) {
            mTargetFps = mPreviewFps;
        }
    }

    /**
     * get preview frame rate
     *
     * @return preview frame rate
     */
    public float getPreviewFps() {
        return mPreviewFps;
    }

    /**
     * Get current camera preview frame rate.
     *
     * @return current camera preview frame rate
     */
    public float getCurrentPreviewFps() {
        if (mCameraCapture != null) {
            return mCameraCapture.getCurrentPreviewFps();
        } else {
            return 0;
        }
    }

    /**
     * Set streaming resolution.<br/>
     * <p>
     * The set resolution would take effect immediately if streaming started.<br/>
     * <p>
     * The set width and height must not be 0 at same time.
     * If one of the params is 0, the other would calculated by the actual preview view size
     * to keep the ratio of the preview view.
     *
     * @param width  streaming width.
     * @param height streaming height.
     */
    public void setTargetResolution(final int width, final int height) throws IllegalArgumentException {
        if (width < 0 || height < 0 || (width == 0 && height == 0)) {
            throw new IllegalArgumentException("Invalid resolution");
        }
        mCustomHandlerThread.post(new LogRunnable("setTargetResolution" + " width=" + width + " height=" + height) {
            @Override
            public void realRun() {
                mTargetWidthOrig = width;
                mTargetHeightOrig = height;
                doSetTargetResolution();
            }
        });
    }

    /**
     * Set streaming resolution index.<br/>
     * <p>
     * The set resolution would take effect immediately if streaming started.<br/>
     *
     * @param idx Resolution index.<br/>
     * @see #VIDEO_RESOLUTION_360P
     * @see #VIDEO_RESOLUTION_480P
     * @see #VIDEO_RESOLUTION_540P
     * @see #VIDEO_RESOLUTION_720P
     * @see #VIDEO_RESOLUTION_1080P
     */
    public void setTargetResolution(final int idx) throws IllegalArgumentException {
        if (idx < VIDEO_RESOLUTION_360P ||
                idx > VIDEO_RESOLUTION_1080P) {
            throw new IllegalArgumentException("Invalid resolution index");
        }
        mCustomHandlerThread.post(new LogRunnable("setTargetResolution" + " idx=" + idx) {
            @Override
            public void realRun() {
                mTargetResolution = idx;
                mTargetWidthOrig = 0;
                mTargetHeightOrig = 0;
                doSetTargetResolution();
            }
        });
    }

    private void doSetTargetResolution() {
        if (mScreenRenderWidth != 0 && mScreenRenderHeight != 0) {
            calResolution();
            mImgTexMixer.setTargetSize(mTargetWidth, mTargetHeight);
        }
    }

    /**
     * get streaming width
     *
     * @return streaming width
     */
    public int getTargetWidth() {
        return mTargetWidth;
    }

    /**
     * get streaming height
     *
     * @return streaming height
     */
    public int getTargetHeight() {
        return mTargetHeight;
    }

    /**
     * Set streaming fps.<br/>
     * <p>
     * The set fps would take effect after next streaming started.<br/>
     * <p>
     * If actual preview fps is larger than set value,
     * the extra frames will be dropped before encoding,
     * and if is smaller than set value, nothing will be done.
     * default value : 15
     *
     * @param fps frame rate.
     */
    public void setTargetFps(float fps) throws IllegalArgumentException {
        if (fps <= 0) {
            throw new IllegalArgumentException("the fps must > 0");
        }
        mTargetFps = fps;
        if (mPreviewFps == 0) {
            mPreviewFps = mTargetFps;
        }
    }

    /**
     * get streaming fps
     *
     * @return streaming fps
     */
    public float getTargetFps() {
        return mTargetFps;
    }

    /**
     * Set enable front camera mirror or not while streaming.<br/>
     * Would take effect immediately while streaming.
     *
     * @param mirror true to enable, false to disable.
     */
    public void setFrontCameraMirror(final boolean mirror) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mFrontCameraMirror = mirror;
                updateFrontMirror();
            }
        });
    }

    /**
     * check if front camera mirror enabled or not.
     *
     * @return true if mirror enabled, false if mirror disabled.
     */
    public boolean isFrontCameraMirrorEnabled() {
        return mFrontCameraMirror;
    }

    /**
     * Set initial camera facing.<br/>
     * Set before {@link #startCameraPreview()}, give a chance to set initial camera facing,
     * equals {@link #startCameraPreview(int)}.<br/>
     *
     * @param facing camera facing.
     * @see CameraCapture#FACING_FRONT
     * @see CameraCapture#FACING_BACK
     */
    public void setCameraFacing(int facing) {
        mCameraFacing = facing;
    }

    /**
     * get camera facing.
     *
     * @return camera facing
     */
    public int getCameraFacing() {
        return mCameraFacing;
    }

    /**
     * Start camera preview with default facing, or facing set by
     * {@link #setCameraFacing(int)} before.
     */
    public void startCameraPreview() {
        startCameraPreview(mCameraFacing);
    }

    /**
     * Start camera preview with given facing.
     *
     * @param facing camera facing.
     * @see CameraCapture#FACING_FRONT
     * @see CameraCapture#FACING_BACK
     */
    public void startCameraPreview(final int facing) {
        mCustomHandlerThread.post(new LogRunnable("startCameraPreview" + " facing=" + facing) {
            @Override
            public void realRun() {
                if (mStatus != STATUS_INITED) {
                    return;
                }
                mCameraFacing = facing;
                mIsCaptureStarted = true;
                if ((mPreviewWidth == 0 || mPreviewHeight == 0) &&
                        (mScreenRenderWidth == 0 || mScreenRenderHeight == 0)) {
                    if (mImgTexPreview.getDisplayPreview() != null) {
                        mDelayedStartCameraPreview = true;
                        return;
                    }
                    mScreenRenderWidth = DEFAULT_PREVIEW_WIDTH;
                    mScreenRenderHeight = DEFAULT_PREVIEW_HEIGHT;
                }
                setPreviewParams();
                mCameraCapture.start(mCameraFacing);

                // 开启了本地预览，关闭自刷新
                mImgTexPreviewMixer.setEnableAutoRefresh(false, mPreviewFps);
            }
        });
    }

    /**
     * Stop camera preview.
     */
    public void stopCameraPreview() {

        mCustomHandlerThread.post(new LogRunnable("stopCameraPreview") {
            @Override
            public void realRun() {
                if (mStatus != STATUS_INITED) {
                    return;
                }
                mIsCaptureStarted = false;
                mCameraCapture.stop();
                freeFboCacheIfNeeded();

                // 关闭了本地预览，开启自刷新
                if (!mRemoteUserPinMap.isEmpty()) {
                    mImgTexPreviewMixer.setEnableAutoRefresh(true, mPreviewFps);
                }
            }
        });
    }

    /**
     * Switch camera facing between front and back.
     */
    public void switchCamera() {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                if (mStatus != STATUS_INITED) {
                    return;
                }
                mCameraCapture.switchCamera();
            }
        });
    }

    /**
     * Get if current camera in use is front camera.<br/>
     *
     * @return true if front camera in use false otherwise.
     */
    public boolean isFrontCamera() {
        return mCameraFacing == CameraCapture.FACING_FRONT;
    }

    /**
     * Get if torch supported on current camera facing.
     *
     * @return true if supported, false if not.
     * @see #getCameraCapture()
     * @see CameraCapture#isTorchSupported()
     */
    public boolean isTorchSupported() {
        if (mCameraCapture != null) {
            return mCameraCapture.isTorchSupported();
        } else {
            return false;
        }
    }

    /**
     * Toggle torch of current camera.
     *
     * @param open true to turn on, false to turn off.
     * @see #getCameraCapture()
     * @see CameraCapture#toggleTorch(boolean)
     */
    public void toggleTorch(final boolean open) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mCameraCapture.toggleTorch(open);
            }
        });
    }

    /**
     * request screen shot with resolution of the screen
     *
     * @param screenShotListener the listener to be called when bitmap of the screen shot available
     */
    public void requestScreenShot(final GLRender.ScreenShotListener screenShotListener) {
        mCustomHandlerThread.post(new LogRunnable("requestScreenShot" + " screenShotListener=" + screenShotListener) {
            @Override
            public void realRun() {
                mImgTexPreviewMixer.requestScreenShot(screenShotListener);
            }
        });
    }

    /**
     * Set local video shown rect.
     *
     * @param x     x position for left top of logo relative to the video, between 0~1.0.
     * @param y     y position for left top of logo relative to the video, between 0~1.0.
     * @param w     width of logo relative to the video, between 0~1.0, if set to 0,
     *              width would be calculated by h and logo image radio.
     * @param h     height of logo relative to the video, between 0~1.0, if set to 0,
     *              height would be calculated by w and logo image radio.
     * @param alpha alpha value，between 0~1.0
     */
    public void setLocalVideoRect(final float x, final float y, final float w, final float h, float alpha) {
        alpha = Math.max(0.0f, alpha);
        alpha = Math.min(alpha, 1.0f);
        final float a = alpha;
        mCustomHandlerThread.post(new LogRunnable("setLocalVideoRect" + " x=" + x + " y=" + y + " w=" + w + " h=" + h + " alpha=" + alpha) {
            @Override
            public void realRun() {
                if (mImgTexPreviewMixer != null) {
                    mImgTexPreviewMixer.setRenderRect(0, x, y, w, h, a);
                }
                if (mScreenRenderWidth != 0 && mScreenRenderHeight != 0) {
                    // 重新计算预览尺寸
                    setPreviewParams();
                }
            }
        });
    }

    /**
     * Bind remote video shown rect with user id.
     *
     * @param userId which user to show
     * @param x      x position for left top of logo relative to the video, between 0~1.0.
     * @param y      y position for left top of logo relative to the video, between 0~1.0.
     * @param w      width of logo relative to the video, between 0~1.0, if set to 0,
     *               width would be calculated by h and logo image radio.
     * @param h      height of logo relative to the video, between 0~1.0, if set to 0,
     *               height would be calculated by w and logo image radio.
     * @param alpha  alpha value，between 0~1.0
     */
    public void bindRemoteVideoRect(final int userId, final float x, final float y, final float w, final float h, float alpha) {
        alpha = Math.max(0.0f, alpha);
        alpha = Math.min(alpha, 1.0f);
        final float a = alpha;

        mCustomHandlerThread.post(new LogRunnable("bindRemoteVideoRect" + " userId=" + userId + " x=" + x + " y=" + y + " w=" + w + " h=" + h + " alpha=" + alpha) {
            @Override
            public void realRun() {
                int idx;
                if (!mRemoteUserPinMap.containsKey(userId)) {
                    idx = getAvailableVideoMixerSink();
                    if (idx < 0) {
                        MyLog.e(TAG, "bindRemoteVideoRect failed!");
                        return;
                    }
                    mAgoraRTCAdapter.addRemoteVideo(userId);
                    mAgoraRTCAdapter.getRemoteVideoSrcPin(userId).connect(mImgTexPreviewMixer.getSinkPin(idx));
                    mRemoteUserPinMap.put(userId, idx);
                } else {
                    idx = mRemoteUserPinMap.get(userId);
                }

                mImgTexPreviewMixer.setScalingMode(idx, ImgTexMixer.SCALING_MODE_CENTER_CROP);
                mImgTexPreviewMixer.setRenderRect(idx, x, y, w, h, a);

                // 仅在未开启本地视频，以及绑定了远端视图的情况下开启自动刷新
                if (!mIsCaptureStarted) {
                    mImgTexPreviewMixer.setEnableAutoRefresh(true, mPreviewFps);
                }
            }
        });
    }

    /**
     * Unbind and remove remote video with user id.
     *
     * @param userId which user to unbind
     */
    public void unbindRemoteVideo(final int userId) {
        mCustomHandlerThread.post(new LogRunnable("unbindRemoteVideo" + " userId=" + userId) {
            @Override
            public void realRun() {
                doUnbindRemoteVideo(userId);
                mRemoteUserPinMap.remove(userId);
                freeFboCacheIfNeeded();
            }
        });
    }

    /**
     * Unbind and remove all remote video.
     */
    public void unbindAllRemoteVideo() {
        mCustomHandlerThread.post(new LogRunnable("unbindAllRemoteVideo") {
            @Override
            public void realRun() {
                for (int userId : mRemoteUserPinMap.keySet()) {
                    doUnbindRemoteVideo(userId);
                }
                mRemoteUserPinMap.clear();
                // 重置本地视频显示区域
                if (mImgTexPreviewMixer != null) {
                    mImgTexPreviewMixer.setRenderRect(0, 0, 0, 1.0f, 1.0f, 1.0f);
                }
                freeFboCacheIfNeeded();
            }
        });
    }

    private void doUnbindRemoteVideo(int userId) {
        DebugLogView.println(TAG, "doUnbindRemoteVideo userId=" + userId);
        SrcPin<ImgTexFrame> remoteVideoSrcPin = mAgoraRTCAdapter.getRemoteVideoSrcPin(userId);
        if (remoteVideoSrcPin != null) {
            remoteVideoSrcPin.disconnect(false);
        }
        mAgoraRTCAdapter.removeRemoteVideo(userId);
    }

    private void freeFboCacheIfNeeded() {
        // 还有要用到视频渲染的地方
        if (mIsCaptureStarted || !mRemoteUserPinMap.isEmpty()) {
            return;
        }
        mGLRender.queueEvent(new LogRunnable("freeFboCacheIfNeeded") {
            @Override
            public void realRun() {
                // 释放所有fbo缓存
                mGLRender.clearFboCache();
            }
        });
    }

    /**
     * 调用该方法时，SDK 不再发送本地视频流，但摄像头仍然处于工作状态。
     * 相比于 enableLocalVideo (false) 用于控制本地视频流发送的方法，该方法响应速度更快。
     * 该方法不影响本地视频流获取，没有禁用摄像头
     *
     * @param muted
     */
    public void muteLocalVideoStream(final boolean muted) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mAgoraRTCAdapter.muteLocalVideoStream(muted);
            }
        });
    }

    /**
     * 接收/停止接收指定视频流
     * 如果之前有调用过 muteAllRemoteVideoStreams (true) 停止接收所有远端视频流，
     * 在调用本 API 之前请确保你已调用 muteAllRemoteVideoStreams (false)。 muteAllRemoteVideoStreams 是全局控制，
     * muteRemoteVideoStream 是精细控制。
     *
     * @param uid
     * @param muted
     */
    public void muteRemoteVideoStream(final int uid, final boolean muted) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mAgoraRTCAdapter.muteRemoteVideoStream(uid, muted);
            }
        });
    }

    /**
     * 你不想看其他人的了，但其他人还能互相看
     *
     * @param muted
     */
    public void muteAllRemoteVideoStreams(final boolean muted) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mAgoraRTCAdapter.muteAllRemoteVideoStreams(muted);
            }
        });
    }

    /**
     * 该用户的首帧是否已经decoded
     *
     * @param userId
     * @return
     */
    public boolean isFirstVideoDecoded(int userId) {
        boolean r = false;
        UserStatus userStatus = mUserStatusMap.get(userId);
        if (userStatus != null) {
            r = userStatus.isEnableVideo() && userStatus.isFirstVideoDecoded();
        }
        MyLog.i(TAG, "isFirstVideoDecoded" + " userId=" + userId + " r=" + r);
        return r;
    }

    private int getAvailableVideoMixerSink() {
        int idx = -1;
        for (int i = 1; i < mImgTexPreviewMixer.getSinkPinNum(); i++) {
            if (!mRemoteUserPinMap.containsValue(i)) {
                MyLog.i(TAG, "get available sink " + i);
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            MyLog.e(TAG, "unable to get available mixer sink!");
        }
        return idx;
    }

    private int getShortEdgeLength(int resolution) {
        switch (resolution) {
            case VIDEO_RESOLUTION_360P:
                return 360;
            case VIDEO_RESOLUTION_480P:
                return 480;
            case VIDEO_RESOLUTION_540P:
                return 540;
            case VIDEO_RESOLUTION_720P:
                return 720;
            case VIDEO_RESOLUTION_1080P:
                return 1080;
            default:
                return 720;
        }
    }

    private int align(int val, int align) {
        return (val + align - 1) / align * align;
    }

    private void calResolution() {
        // 考虑存在远端视频，本地视频的尺寸需要调整
        RectF previewRect = mImgTexPreviewMixer.getRenderRect(0);
        int localRenderWidth = (int) (mScreenRenderWidth * previewRect.width());
        int localRenderHeight = (int) (mScreenRenderHeight * previewRect.height());

        if (mPreviewWidthOrig == 0 && mPreviewHeightOrig == 0) {
            int val = getShortEdgeLength(mPreviewResolution);
            if (mScreenRenderWidth > mScreenRenderHeight) {
                mPreviewMixerWidth = 0;
                mPreviewMixerHeight = val;
            } else {
                mPreviewMixerWidth = val;
                mPreviewMixerHeight = 0;
            }
            if (localRenderWidth > localRenderHeight) {
                mPreviewWidth = 0;
                mPreviewHeight = val;
            } else {
                mPreviewWidth = val;
                mPreviewHeight = 0;
            }
        } else {
            mPreviewMixerWidth = mPreviewWidthOrig;
            mPreviewMixerHeight = mPreviewHeightOrig;
        }

        if (mTargetWidthOrig == 0 && mTargetHeightOrig == 0) {
            int val = getShortEdgeLength(mTargetResolution);
            if (localRenderWidth > localRenderHeight) {
                mTargetWidth = 0;
                mTargetHeight = val;
            } else {
                mTargetWidth = val;
                mTargetHeight = 0;
            }
        }

        if (mScreenRenderWidth != 0 && mScreenRenderHeight != 0) {
            if (mPreviewMixerWidth == 0) {
                mPreviewMixerWidth = mPreviewMixerHeight * mScreenRenderWidth / mScreenRenderHeight;
            } else if (mPreviewMixerHeight == 0) {
                mPreviewMixerHeight = mPreviewMixerWidth * mScreenRenderHeight / mScreenRenderWidth;
            }
        }

        if (localRenderWidth != 0 && localRenderHeight != 0) {
            if (mPreviewWidth == 0) {
                mPreviewWidth = mPreviewHeight * localRenderWidth / localRenderHeight;
            } else if (mPreviewHeight == 0) {
                mPreviewHeight = mPreviewWidth * localRenderHeight / localRenderWidth;
            }
            if (mTargetWidth == 0) {
                mTargetWidth = mTargetHeight * localRenderWidth / localRenderHeight;
            } else if (mTargetHeight == 0) {
                mTargetHeight = mTargetWidth * localRenderHeight / localRenderWidth;
            }
        }
        mPreviewWidth = align(mPreviewWidth, 8);
        mPreviewHeight = align(mPreviewHeight, 8);
        mPreviewMixerWidth = align(mPreviewMixerWidth, 8);
        mPreviewMixerHeight = align(mPreviewMixerHeight, 8);
        mTargetWidth = align(mTargetWidth, 8);
        mTargetHeight = align(mTargetHeight, 8);

        MyLog.i(TAG, "calResolution: \n" +
                "viewRenderSize: " + mScreenRenderWidth + "x" + mScreenRenderHeight + "\n" +
                "localRenderRect: " + previewRect + "\n" +
                "localRenderSize: " + localRenderWidth + "x" + localRenderHeight + "\n" +
                "previewSize: " + mPreviewWidth + "x" + mPreviewHeight + "\n" +
                "mixerSize: " + mPreviewMixerWidth + "x" + mPreviewMixerHeight + "\n" +
                "targetSize: " + mTargetWidth + "x" + mTargetHeight);
    }

    private void updateFrontMirror() {
        if (mCameraFacing == CameraCapture.FACING_FRONT) {
            mImgTexMixer.setMirror(0, !mFrontCameraMirror);
        } else {
            mImgTexMixer.setMirror(0, false);
        }
    }

    private void setPreviewParams() {
        calResolution();
        mCameraCapture.setOrientation(mRotateDegrees);
        if (mPreviewFps == 0) {
            mPreviewFps = CameraCapture.DEFAULT_PREVIEW_FPS;
        }
        mCameraCapture.setPreviewFps(mPreviewFps);

        mImgTexScaleFilter.setTargetSize(mPreviewWidth, mPreviewHeight);
        mImgTexPreviewMixer.setTargetSize(mPreviewMixerWidth, mPreviewMixerHeight);
        mImgTexMixer.setTargetSize(mTargetWidth, mTargetHeight);
    }

    private void onPreviewSizeChanged(final int width, final int height) {
        mCustomHandlerThread.post(new LogRunnable("onPreviewSizeChanged" + " width=" + width + " height=" + height) {
            @Override
            public void realRun() {
                boolean notifySizeChanged = mScreenRenderWidth != 0 && mScreenRenderHeight != 0;
                mScreenRenderWidth = width;
                mScreenRenderHeight = height;
                setPreviewParams();
                if (mDelayedStartCameraPreview) {
                    mCameraCapture.start(mCameraFacing);
                    mDelayedStartCameraPreview = false;
                }
                if (notifySizeChanged) {
                    // TODO: notify preview size changed
                }
            }
        });
    }

    private GLRender.OnSizeChangedListener mPreviewSizeChangedListener =
            new GLRender.OnSizeChangedListener() {
                @Override
                public void onSizeChanged(int width, int height) {
                    MyLog.i(TAG, "onPreviewSizeChanged: " + width + "x" + height);
                    onPreviewSizeChanged(width, height);
                }
            };
}

