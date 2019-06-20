package com.zq.mediaengine.kit;

import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.TextureView;
import android.util.Log;
import android.view.View;

import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.CustomHandlerThread;
import com.common.utils.DeviceUtils;
import com.common.utils.U;
import com.common.view.DebugLogView;
import com.engine.EngineEvent;
import com.engine.Params;
import com.engine.UserStatus;
import com.engine.agora.AgoraOutCallback;
import com.engine.agora.effect.EffectModel;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.score.Score2Callback;
import com.engine.token.AgoraTokenApi;
import com.zq.mediaengine.capture.AudioCapture;
import com.zq.mediaengine.capture.AudioPlayerCapture;
import com.zq.mediaengine.capture.CameraCapture;
import com.zq.mediaengine.encoder.MediaCodecAudioEncoder;
import com.zq.mediaengine.filter.audio.APMFilter;
import com.zq.mediaengine.filter.audio.AudioCopyFilter;
import com.zq.mediaengine.filter.audio.AudioFilterBase;
import com.zq.mediaengine.filter.audio.AudioFilterMgt;
import com.zq.mediaengine.filter.audio.AudioMixer;
import com.zq.mediaengine.filter.audio.AudioPreview;
import com.zq.mediaengine.filter.audio.AudioResampleFilter;
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
import com.zq.mediaengine.kit.filter.TbAudioEffectFilter;
import com.zq.mediaengine.publisher.MediaMuxerPublisher;
import com.zq.mediaengine.publisher.RawFrameWriter;
import com.zq.mediaengine.util.audio.AudioUtil;
import com.zq.mediaengine.util.gles.GLRender;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
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

    public final static String TAG = "ZqEngineKit";
    public static final String PREF_KEY_TOKEN_ENABLE = "key_agora_token_enable";

    public static final int VIDEO_RESOLUTION_360P = 0;
    public static final int VIDEO_RESOLUTION_480P = 1;
    public static final int VIDEO_RESOLUTION_540P = 2;
    public static final int VIDEO_RESOLUTION_720P = 3;
    public static final int VIDEO_RESOLUTION_1080P = 4;

    private static final int DEFAULT_PREVIEW_WIDTH = 720;
    private static final int DEFAULT_PREVIEW_HEIGHT = 1280;

    static final int STATUS_UNINIT = 0;
    static final int STATUS_INITING = 1;
    static final int STATUS_INITED = 2;
    static final int MSG_JOIN_ROOM_TIMEOUT = 11;
    static final int MSG_JOIN_ROOM_AGAIN = 12;

    private static final boolean SCORE_DEBUG = false;
    private static final String SCORE_DEBUG_PATH = "/sdcard/tongzhuodeni.pcm";

    private Params mConfig = new Params(); // 为了防止崩溃
    private final Object mLock = new Object();

    private int mStatus = STATUS_UNINIT;// 0未初始化 1 初始ing 2 初始化
    /**
     * 存储该房间所有用户在引擎中的状态的，
     * key为在引擎中的用户 id
     */
    private HashMap<Integer, UserStatus> mUserStatusMap = new HashMap<>();
    private HashSet<View> mRemoteViewCache = new HashSet<>();
    private Handler mUiHandler = new Handler();
    private Disposable mMusicTimePlayTimeListener;

    private String mInitFrom;

    private Handler mMainHandler;
    private CustomHandlerThread mCustomHandlerThread;
    // 用来做同步调用
    private ConditionVariable mCustomHandlerCondition = new ConditionVariable();

    private boolean mTokenEnable = false; // 是否开启token校验
    private String mLastJoinChannelToken; // 上一次加入房间用的token
    private String mRoomId = ""; // 房间id
    private boolean mInChannel = false; // 是否已经在频道中

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
    private CbAudioScorer mCbAudioScorer;
    private AcrRecognizer mAcrRecognizer;

    // 自采集相关
    private AudioCapture mAudioCapture;
    private AudioPlayerCapture mAudioPlayerCapture;
    private SrcPin<AudioBufFrame> mAudioRemoteSrcPin;
    private AudioPreview mAudioPreview;

    // AEC/NS/AGC等处理
    private APMFilter mAPMFilter;
    // 输出前的重采样模块
    private AudioResampleFilter mAudioResampleFilter;
    // 对bgm, remote进行混音，用于回声消除
    private AudioMixer mRemoteAudioMixer;
    // 对mic, bgm进行混音，用于远端发送
    private AudioMixer mLocalAudioMixer;
    // 对mic, bgm, remote进行混音，用于录制
    private AudioMixer mRecordAudioMixer;
    private MediaCodecAudioEncoder mAudioEncoder;
    private MediaMuxerPublisher mFilePublisher;
    private RawFrameWriter mRawFrameWriter;

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

    @Override
    public void onUserJoined(int uid, int elapsed) {
        // 用户加入了
        UserStatus userStatus = ensureJoin(uid);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_JOIN, userStatus));
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        // 用户离开
        UserStatus userStatus = mUserStatusMap.remove(uid);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_LEAVE, userStatus));
    }

    @Override
    public void onUserMuteVideo(int uid, boolean muted) {
        UserStatus status = ensureJoin(uid);
        status.setVideoMute(muted);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_MUTE_VIDEO, status));
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        UserStatus status = ensureJoin(uid);
        status.setAudioMute(muted);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_MUTE_AUDIO, status));
    }

    @Override
    public void onUserEnableVideo(int uid, boolean enabled) {
        UserStatus status = ensureJoin(uid);
        status.setEnableVideo(enabled);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_VIDEO_ENABLE, status));
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        UserStatus status = ensureJoin(uid);
        status.setEnableVideo(true);
        status.setFirstVideoDecoded(true);
        status.setFirstVideoWidth(width);
        status.setFirstVideoHeight(height);

        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_FIRST_REMOTE_VIDEO_DECODED, status));
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        UserStatus userStatus = ensureJoin(uid);
        userStatus.setIsSelf(true);
        mConfig.setSelfUid(uid);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_JOIN, userStatus));
        StatisticsAdapter.recordCalculateEvent("agora", "join_duration", System.currentTimeMillis() - mConfig.getJoinRoomBeginTs(), null);
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_TIMEOUT);
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_AGAIN);
        }
        mInChannel = true;
        onSelfJoinChannelSuccess();
    }

    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        UserStatus userStatus = ensureJoin(uid);
        userStatus.setIsSelf(true);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_REJOIN, userStatus));
        mInChannel = true;
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        mInChannel = false;
    }

    @Override
    public void onClientRoleChanged(int oldRole, int newRole) {
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
        mConfig.setMixMusicPlaying(false);
        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_FINISH, null);
        EventBus.getDefault().post(engineEvent);
    }

    @Override
    public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {
        List<EngineEvent.UserVolumeInfo> l = new ArrayList<>();
        for (IRtcEngineEventHandler.AudioVolumeInfo info : speakers) {
//            MyLog.d(TAG,"onAudioVolumeIndication" + " info=" + info.uid+" volume="+info.volume);
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
    public void onError(int error) {
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

    private UserStatus ensureJoin(int uid) {
        if (!mUserStatusMap.containsKey(uid)) {
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
        mMainHandler = new Handler(Looper.getMainLooper());

        mTokenEnable = U.getPreferenceUtils().getSettingBoolean(PREF_KEY_TOKEN_ENABLE, false);
    }

    public static ZqEngineKit getInstance() {
        return ZqEngineKitHolder.INSTANCE;
    }

    public void init(final String from, final Params params) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.destroy();
        }
        mCustomHandlerThread = new CustomHandlerThread(TAG) {
            @Override
            protected void processMessage(Message msg) {
                if (msg.what == MSG_JOIN_ROOM_AGAIN) {
                    MyLog.d(TAG, "processMessage MSG_JOIN_ROOM_AGAIN 再次加入房间");
                    JoinParams joinParams = (JoinParams) msg.obj;
                    joinRoomInner(joinParams.roomID, joinParams.userId, joinParams.token);
                } else if (msg.what == MSG_JOIN_ROOM_TIMEOUT) {
                    MyLog.d(TAG, "handleMessage 加入房间超时");
                    StatisticsAdapter.recordCountEvent("agora", "join_timeout", null);
                    JoinParams joinParams = (JoinParams) msg.obj;
                    joinRoomInner2(joinParams.roomID, joinParams.userId, joinParams.token);
                }
            }
        };
        final int oldStatus = mStatus;
        mStatus = STATUS_INITING;
        mCustomHandlerCondition.close();
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                MyLog.d(TAG, "init" + " from=" + from + " params=" + params);
                destroyInner(oldStatus);
                initInner(from, params);
                mCustomHandlerCondition.open();
            }
        });
        mCustomHandlerCondition.block();
    }

    private void initInner(String from, Params params) {
        mStatus = STATUS_INITED;
        mInitFrom = from;
        mConfig = params;
        synchronized (mLock) {
            mLock.notifyAll();
        }

        // TODO: engine代码合并后，采样率初始值在Params初始化时获取
        mConfig.setAudioSampleRate(AudioUtil.getNativeSampleRate(U.app().getApplicationContext()));

        initModules();
        mAgoraRTCAdapter.init(mConfig);
        mCbAudioScorer.init(mConfig);
        mAcrRecognizer.init(mConfig);
        if (SCORE_DEBUG) {
            mAudioDummyFilter.init(SCORE_DEBUG_PATH, mConfig);
        }
        setAudioEffectStyle(mConfig.getStyleEnum());
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void initModules() {
        MyLog.i(TAG, "isUseExternalAudio: " + mConfig.isUseExternalAudio() +
                " isUseExternalVideo: " + mConfig.isUseExternalVideo() +
                " isUseExternalRecord: " + mConfig.isUseExternalAudioRecord());

        if (mConfig.isEnableAudio()) {
            initAudioModules();
        }

        if (mConfig.isEnableVideo()) {
            initVideoModules();
        }
    }

    @SuppressWarnings("unchecked")
    private void initAudioModules() {
        mAudioFilterMgt = new AudioFilterMgt();
        mCbAudioScorer = new CbAudioScorer();
        // 单mic数据PCM录制
        mRawFrameWriter = new RawFrameWriter();

        SrcPin<AudioBufFrame> audioLocalSrcPin;
        if (mConfig.isUseExternalAudio()) {
            mAudioCapture = new AudioCapture(U.app().getApplicationContext());
            mAudioCapture.setSampleRate(mConfig.getAudioSampleRate());
            mAudioPlayerCapture = new AudioPlayerCapture(U.app().getApplicationContext());
            mAudioPreview = new AudioPreview(U.app().getApplicationContext());
            mAPMFilter = new APMFilter();

            mAudioCapture.getSrcPin().connect(mAPMFilter.getSinkPin());
            audioLocalSrcPin = mAPMFilter.getSrcPin();
            // 自采集模式下，练歌房不需要声网SDK
            if (mConfig.getScene() != Params.Scene.audiotest) {
                mRemoteAudioMixer = new AudioMixer();
                // 加入房间后，以声网远端数据作为主驱动
                mAgoraRTCAdapter.getRemoteAudioSrcPin().connect(mAudioPreview.getSinkPin());
                mAgoraRTCAdapter.getRemoteAudioSrcPin().connect(mRemoteAudioMixer.getSinkPin(0));
                mAudioPlayerCapture.getSrcPin().connect(mRemoteAudioMixer.getSinkPin(1));
                mRemoteAudioMixer.getSrcPin().connect(mAPMFilter.getReverseSinkPin());
                mAudioRemoteSrcPin = mRemoteAudioMixer.getSrcPin();
            } else {
                mAudioPlayerCapture.getSrcPin().connect(mAPMFilter.getReverseSinkPin());
                mAudioRemoteSrcPin = mAudioPlayerCapture.getSrcPin();
            }

            mAPMFilter.enableAEC(true);
            mAPMFilter.setRoutingMode(APMFilter.AEC_ROUTING_MODE_SPEAKER_PHONE);
            mAPMFilter.enableNs(true);
            mAPMFilter.setNsLevel(APMFilter.NS_LEVEL_1);

            mAudioPlayerCapture.setOnCompletionListener(new AudioPlayerCapture.OnCompletionListener() {
                @Override
                public void onCompletion(AudioPlayerCapture audioPlayerCapture) {
                    onAudioMixingFinished();
                }
            });
        } else {
            audioLocalSrcPin = mAgoraRTCAdapter.getLocalAudioSrcPin();
            mAudioRemoteSrcPin = mAgoraRTCAdapter.getRemoteAudioSrcPin();
        }

        if (SCORE_DEBUG) {
            mAudioDummyFilter = new AudioDummyFilter();
            audioLocalSrcPin.connect(mAudioDummyFilter.getSinkPin());
            mAudioDummyFilter.getSrcPin().connect(mCbAudioScorer.getSinkPin());
            mAudioDummyFilter.getSrcPin().connect(mAcrRecognizer.getSinkPin());
            mAudioDummyFilter.getSrcPin().connect(mAudioFilterMgt.getSinkPin());
        } else {
            audioLocalSrcPin.connect(mCbAudioScorer.getSinkPin());
            audioLocalSrcPin.connect(mAcrRecognizer.getSinkPin());
            audioLocalSrcPin.connect(mAudioFilterMgt.getSinkPin());
        }

        if (mConfig.isUseExternalAudio() || mConfig.isUseExternalAudioRecord()) {
            mAudioResampleFilter = new AudioResampleFilter();
            // 使用声网采集时，需要做buffer数据的隔离
            mAudioResampleFilter.setOutFormat(new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16,
                    mConfig.getAudioSampleRate(), mConfig.getAudioChannels()), !mConfig.isUseExternalAudio());
            mRecordAudioMixer = new AudioMixer();

            // PCM dump, 需要在最前面连接
            mAudioResampleFilter.getSrcPin().connect((SinkPin<AudioBufFrame>) mRawFrameWriter.getSinkPin());

            if (mConfig.isUseExternalAudio()) {
                // 自采集发送，需要做buffer数据的隔离
                AudioCopyFilter audioCopyFilter = new AudioCopyFilter();
                mLocalAudioMixer = new AudioMixer();
                mAudioResampleFilter.getSrcPin().connect(audioCopyFilter.getSinkPin());
                audioCopyFilter.getSrcPin().connect(mLocalAudioMixer.getSinkPin(0));
                mAudioPlayerCapture.getSrcPin().connect(mLocalAudioMixer.getSinkPin(1));
                mLocalAudioMixer.getSrcPin().connect(mAgoraRTCAdapter.getAudioSinkPin());

                // 用声网采集，需要录制的时候再连接
                connectRecord();
            }

            // 录制功能
            mAudioEncoder = new MediaCodecAudioEncoder();
            mFilePublisher = new MediaMuxerPublisher();
            mRecordAudioMixer.getSrcPin().connect(mAudioEncoder.getSinkPin());
            mAudioEncoder.getSrcPin().connect(mFilePublisher.getAudioSink());
        } else {
            mAudioFilterMgt.getSrcPin().connect((SinkPin<AudioBufFrame>) mRawFrameWriter.getSinkPin());
        }
    }

    private void connectRecord() {
        mAudioFilterMgt.getSrcPin().connect(mAudioResampleFilter.getSinkPin());
        mAudioResampleFilter.getSrcPin().connect(mRecordAudioMixer.getSinkPin(0));
        mAudioRemoteSrcPin.connect(mRecordAudioMixer.getSinkPin(1));
    }

    private void disconnectRecord() {
        mAudioFilterMgt.getSrcPin().disconnect(mAudioResampleFilter.getSinkPin(), false);
        mAudioResampleFilter.getSrcPin().disconnect(mRecordAudioMixer.getSinkPin(0), false);
        mAudioRemoteSrcPin.disconnect(mRecordAudioMixer.getSinkPin(1), false);
    }

    public boolean isInit() {
        return mStatus == STATUS_INITED;
    }

    public Params getParams() {
        if (mStatus == STATUS_INITED) {
            return mConfig;
        } else if (mStatus == STATUS_UNINIT) {
            return mConfig;
        } else if (mStatus == STATUS_INITING) {
            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                }
            }
            return mConfig;
        }
        return null;
    }

    /**
     * 离开房间
     */
    public void leaveChannel() {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                if (mConfig.isUseExternalAudio()) {
                    mAudioPreview.stop();
                    mAudioCapture.stop();
                    mAudioPlayerCapture.stop();
                }
                mAgoraRTCAdapter.leaveChannel();
            }
        });
    }

    /**
     * 销毁所有
     */
    public void destroy(final String from) {
        MyLog.d(TAG, "destroy" + " from=" + from + " status=" + mStatus);
        if (!"force".equals(from)) {
            if (mInitFrom != null && !mInitFrom.equals(from)) {
                return;
            }
        }
        if (mStatus == STATUS_INITED) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (from.equals(mInitFrom)) {
                        mConfig.setAnchor(false);
                        destroyInner(mStatus);
                        mCustomHandlerThread.destroy();
                        mStatus = STATUS_UNINIT;
                    }
                }
            });
        }
    }

    private void destroyInner(int status) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_AGAIN);
        }
        mUiHandler.removeMessages(MSG_JOIN_ROOM_TIMEOUT);
        if (status == STATUS_INITED) {
            if (mMusicTimePlayTimeListener != null && !mMusicTimePlayTimeListener.isDisposed()) {
                mMusicTimePlayTimeListener.dispose();
            }
            mInChannel = false;
            if (mConfig.isUseExternalAudio()) {
                mAudioCapture.release();
                mAudioPlayerCapture.release();
            }
            if (mConfig.isEnableVideo() && mConfig.isUseExternalVideo()) {
                mCameraCapture.release();
                mGLRender.release();
            }
            mAgoraRTCAdapter.destroy(true);
            mUserStatusMap.clear();
            mRemoteViewCache.clear();
            mRemoteUserPinMap.clear();
            mUiHandler.removeCallbacksAndMessages(null);
            mConfig = new Params();
            mPendingStartMixAudioParams = null;
            EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_ENGINE_DESTROY, null));
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
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
        MyLog.d(TAG, "getToken" + " roomId=" + roomId);
        AgoraTokenApi agoraTokenApi = ApiManager.getInstance().createService(AgoraTokenApi.class);
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
                                MyLog.d(TAG, "getToken 成功 token=" + token);
                                return token;
                            }
                        } else {
                            MyLog.w(TAG, "syncMyInfoFromServer obj==null");
                        }
                    }
                } catch (Exception e) {
                    MyLog.d(e);
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
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                MyLog.d(TAG, "joinRoom" + " roomid=" + roomid + " userId=" + userId + " isAnchor=" + isAnchor + " token=" + token);
                mConfig.setSelfUid(userId);
                if (mConfig.getChannelProfile() == Params.CHANNEL_TYPE_LIVE_BROADCASTING) {
                    mAgoraRTCAdapter.setClientRole(isAnchor);
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
        MyLog.w(TAG, "joinRoomInner" + " roomid=" + roomid + " userId=" + userId + " token=" + token);
        if (Looper.myLooper() != mCustomHandlerThread.getLooper()) {
            MyLog.d(TAG, "joinRoomInner not looper");
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    joinRoomInner(roomid, userId, token);
                }
            });
            return;
        }
        mRoomId = roomid;
        String token2 = token;
        if (mTokenEnable) {
            MyLog.d(TAG, "joinRoomInner 明确告知已经启用token了 token=" + token2);
            if (TextUtils.isEmpty(token2)) {
                // 但是token2还为空，短链接要个token
                token2 = getToken(roomid);
            } else {
                // token不为空，继续使用
            }
        } else {
            MyLog.d(TAG, "joinRoomInner 未启用token，一是真的未启用，二是启用了不知道");
            if (TextUtils.isEmpty(token)) {
                // 没有token
            } else {
                // 但是已经有token了
            }
        }
        joinRoomInner2(roomid, userId, token2);
    }

    // TODO: 自采集模式下，练歌房不需要加入声网房间
    private void joinRoomInner2(final String roomid, final int userId, final String token) {
        MyLog.d(TAG, "joinRoomInner2" + " roomid=" + roomid + " userId=" + userId + " token=" + token);
        mLastJoinChannelToken = token;
        mAgoraRTCAdapter.leaveChannel();
        int retCode = mAgoraRTCAdapter.joinChannel(token, roomid, "Extra Optional Data", userId);
        MyLog.d(TAG, "joinRoomInner2" + " retCode=" + retCode);
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
            // 成功后，自采集模式下开启采集
            if (mConfig.isUseExternalAudio()) {
                mAudioCapture.start();
                mAudioPreview.start();
            }

            //告诉我成功
            mConfig.setJoinRoomBeginTs(System.currentTimeMillis());
            Message msg = mUiHandler.obtainMessage();
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
            mConfig.setAnchor(isAnchor);
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    DebugLogView.println(TAG, "setClientRole" + " isAnchor=" + isAnchor);
                    mAgoraRTCAdapter.setClientRole(isAnchor);
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
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    DebugLogView.println(TAG, "muteLocalAudioStream muted=" + muted);
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
                    mConfig.setEnableInEarMonitoring(enable);
                    mAgoraRTCAdapter.enableInEarMonitoring(enable);
                }
            });
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
                    mAgoraRTCAdapter.setInEarMonitoringVolume(volume);
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
        MyLog.d(TAG, "adjustRecordingSignalVolume" + " volume=" + volume + " setConfig=" + setConfig);
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
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
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (setConfig) {
                        mConfig.setPlaybackSignalVolume(volume);
                    }
                    mAgoraRTCAdapter.adjustPlaybackSignalVolume(volume);
                }
            });
        }
    }
    /*音频基础结束*/

    /*音频高级扩展开始*/

    private void doSetAudioEffect(Params.AudioEffect styleEnum) {
        if (styleEnum == mConfig.getStyleEnum()) {
            return;
        }

        mConfig.setStyleEnum(styleEnum);
        List<AudioFilterBase> filters = new ArrayList<>(2);

        // 添加音效
        if (styleEnum == Params.AudioEffect.ktv) {
            filters.add(new TbAudioEffectFilter(2));
        } else if (styleEnum == Params.AudioEffect.rock) {
            filters.add(new TbAudioEffectFilter(1));
        } else if (styleEnum == Params.AudioEffect.dianyin) {
            filters.add(new CbAudioEffectFilter(8));
        } else if (styleEnum == Params.AudioEffect.kongling) {
            filters.add(new CbAudioEffectFilter(1));
        }

        // 针对不同场景，处理agc
        switch (mConfig.getScene()) {
            case grab:
                // 只有单人清唱才走天宝的agc
                if (mConfig.isGrabSingNoAcc()) {
                    filters.add(new TbAudioAgcFilter());
                }
                break;
            case voice:
                break;
            case rank:
            case audiotest:
                filters.add(new TbAudioAgcFilter());
                break;
        }

        mAudioFilterMgt.setFilter(filters);
    }

    public void setAudioEffectStyle(final Params.AudioEffect styleEnum) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    doSetAudioEffect(styleEnum);
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
        MyLog.d(TAG, "setLocalVoicePitch" + " pitch=" + pitch);
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

    public void startAudioMixing(String filePath, boolean loopback, boolean replace, int cycle) {
        startAudioMixing(filePath, null, 0, loopback, replace, cycle);
    }

    /**
     * 开始播放音乐文件及混音。
     * 播放伴奏结束后，会收到 onAudioMixingFinished 回调
     *
     * @param filePath 指定需要混音的本地或在线音频文件的绝对路径。支持d的音频格式包括：mp3、mp4、m4a、aac、3gp、mkv、wav 及 flac。详见 Supported Media Formats。
     *                 如果用户提供的目录以 /assets/ 开头，则去 assets 里面查找该文件
     *                 如果用户提供的目录不是以 /assets/ 开头，一律认为是在绝对路径里查找该文件
     * @param loopback true：只有本地可以听到混音或替换后的音频流
     *                 false：本地和对方都可以听到混音或替换后的音频流
     * @param replace  true：只推动设置的本地音频文件或者线上音频文件，不传输麦克风收录的音频
     *                 false：音频文件内容将会和麦克风采集的音频流进行混音
     * @param cycle    指定音频文件循环播放的次数：
     *                 正整数：循环的次数
     *                 -1：无限循环
     */
    public void startAudioMixing(final String filePath, final String midiPath, final long mixMusicBeginOffset, final boolean loopback, final boolean replace, final int cycle) {
        startAudioMixing(0, filePath, midiPath, mixMusicBeginOffset, loopback, replace, cycle);
    }

    public void startAudioMixing(final int uid, final String filePath, final String midiPath, final long mixMusicBeginOffset, final boolean loopback, final boolean replace, final int cycle) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    MyLog.w(TAG, "startAudioMixing" + " uid=" + uid + " filePath=" + filePath + " midiPath=" + midiPath + " mixMusicBeginOffset=" + mixMusicBeginOffset + " loopback=" + loopback + " replace=" + replace + " cycle=" + cycle);
                    if (TextUtils.isEmpty(filePath)) {
                        MyLog.d(TAG, "伴奏路径非法");
                        return;
                    }
                    boolean canGo = false;
                    if (uid <= 0) {
                        canGo = true;
                    } else {
                        UserStatus userStatus = mUserStatusMap.get(uid);
                        if (userStatus == null) {
                            MyLog.w(TAG, "该用户还未在频道中，播伴奏挂起");
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

                        if (mConfig.isUseExternalAudio()) {
                            mAudioPlayerCapture.start(filePath, cycle == -1);
                        } else {
                            mAgoraRTCAdapter.startAudioMixing(filePath, midiPath, loopback, replace, cycle);
                        }
                    } else {
                        mPendingStartMixAudioParams = new PendingStartMixAudioParams();
                        mPendingStartMixAudioParams.uid = uid;
                        mPendingStartMixAudioParams.filePath = filePath;
                        mPendingStartMixAudioParams.midiPath = midiPath;
                        mPendingStartMixAudioParams.mixMusicBeginOffset = mixMusicBeginOffset;
                        mPendingStartMixAudioParams.loopback = loopback;
                        mPendingStartMixAudioParams.replace = replace;
                        mPendingStartMixAudioParams.cycle = cycle;
                    }
                }
            });
        }
    }

    public static class PendingStartMixAudioParams {
        int uid;
        String filePath;
        String midiPath;
        long mixMusicBeginOffset;
        boolean loopback;
        boolean replace;
        int cycle;
    }

    private void onSelfJoinChannelSuccess() {
        if (mPendingStartMixAudioParams != null) {
            MyLog.w(TAG, "播放之前挂起的伴奏 uid=" + mPendingStartMixAudioParams.uid);
            startAudioMixing(mPendingStartMixAudioParams.uid,
                    mPendingStartMixAudioParams.filePath,
                    mPendingStartMixAudioParams.midiPath,
                    mPendingStartMixAudioParams.mixMusicBeginOffset,
                    mPendingStartMixAudioParams.loopback,
                    mPendingStartMixAudioParams.replace,
                    mPendingStartMixAudioParams.cycle);
        }
        mAgoraRTCAdapter.muteLocalAudioStream(mConfig.isLocalAudioStreamMute());
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_SELF_JOIN_SUCCESS));
    }

    /**
     * 停止播放音乐文件及混音。
     * 请在频道内调用该方法。
     */
    public void stopAudioMixing() {
        MyLog.d(TAG, "stopAudioMixing");
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(mConfig.getMixMusicFilePath())) {
                        mConfig.setMixMusicPlaying(false);
                        mConfig.setMixMusicFilePath(null);
                        mConfig.setMidiPath(null);
                        mConfig.setMixMusicBeginOffset(0);
                        stopMusicPlayTimeListener();
                        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_STOP);
                        EventBus.getDefault().post(engineEvent);

                        if (mConfig.isUseExternalAudio()) {
                            mAudioPlayerCapture.stop();
                        } else {
                            mAgoraRTCAdapter.stopAudioMixing();
                        }
                    }
                    mPendingStartMixAudioParams = null;
                    mConfig.setCurrentMusicTs(0);
                    mConfig.setRecordCurrentMusicTsTs(0);
                    mConfig.setLrcHasStart(false);
                }
            });
        }
    }

    /**
     * 继续播放混音
     */
    public void resumeAudioMixing() {
        MyLog.d(TAG, "resumeAudioMixing");
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
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
        MyLog.d(TAG, "pauseAudioMixing");
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
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
                        MyLog.d(TAG, "PlayTimeListener accept ts=" + aLong);
                        int currentPostion = getAudioMixingCurrentPosition();
                        mConfig.setCurrentMusicTs(currentPostion);
                        mConfig.setRecordCurrentMusicTsTs(System.currentTimeMillis());
                        if (duration < 0) {
                            duration = getAudioMixingDuration();
                        }
                        if (currentPostion < duration) {
                            EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER);
                            engineEvent.obj = new EngineEvent.MixMusicTimeInfo(currentPostion, duration);
                            EventBus.getDefault().post(engineEvent);
                        } else {
                            MyLog.d(TAG, "playtime不合法,currentPostion=" + currentPostion + " duration=" + duration);
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
     * 调节混音音量大小
     *
     * @param volume 1-100 默认100
     */
    public void adjustAudioMixingVolume(final int volume) {
        adjustAudioMixingVolume(volume, true);
    }

    public void adjustAudioMixingVolume(final int volume, final boolean setConfig) {
        MyLog.d(TAG, "adjustAudioMixingVolume" + " volume=" + volume + " setConfig=" + setConfig);
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (setConfig) {
                        mConfig.setAudioMixingVolume(volume);
                    }
                    if (mConfig.isUseExternalAudio()) {
                        mAudioPlayerCapture.setVolume(volume / 100.f);
                    } else {
                        mAgoraRTCAdapter.adjustAudioMixingVolume(volume);
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
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (mConfig.isUseExternalAudio()) {
                        mAudioPlayerCapture.seek(posMs);
                    } else {
                        mAgoraRTCAdapter.setAudioMixingPosition(posMs);
                    }
                }
            });
        }

    }


    /**
     * 开始客户端录音。
     * <p>
     * Agora SDK 支持通话过程中在客户端进行录音。该方法录制频道内所有用户的音频，并生成一个包含所有用户声音的录音文件，录音文件格式可以为：
     * <p>
     * .wav：文件大，音质保真度高
     * .aac：文件小，有一定的音质保真度损失
     * 请确保 App 里指定的目录存在且可写。该接口需在加入频道之后调用。如果调用 leaveChannel 时还在录音，录音会自动停止。
     */
    public void startAudioRecording(final String saveAudioForAiFilePath, final int audioRecordingQualityHigh, final boolean fromRecodFrameCallback) {
        MyLog.w(TAG, "startAudioRecording" + " saveAudioForAiFilePath=" + saveAudioForAiFilePath + " audioRecordingQualityHigh=" + audioRecordingQualityHigh);
        if (mCustomHandlerThread != null) {
            mConfig.setRecording(true);
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    File file = new File(saveAudioForAiFilePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    if (file.exists()) {
                        file.delete();
                    }

                    if (!mConfig.isUseExternalAudio()) {
                        // 用声网采集，需要录制的时候再连接
                        connectRecord();
                    }
                    if (fromRecodFrameCallback) {
                        mConfig.setRecordingFromCallbackSavePath(saveAudioForAiFilePath);
                        mRawFrameWriter.start(saveAudioForAiFilePath);
                    } else {
                        if (mConfig.isUseExternalAudioRecord()) {
                            AudioCodecFormat audioCodecFormat =
                                    new AudioCodecFormat(AVConst.CODEC_ID_AAC,
                                            AVConst.AV_SAMPLE_FMT_S16,
                                            mConfig.getAudioSampleRate(),
                                            mConfig.getAudioChannels(),
                                            mConfig.getAudioBitrate());
                            mAudioEncoder.configure(audioCodecFormat);
                            mFilePublisher.setAudioOnly(true);
                            mFilePublisher.start(saveAudioForAiFilePath);
                            mAudioEncoder.start();
                        } else {
                            mAgoraRTCAdapter.startAudioRecording(saveAudioForAiFilePath, audioRecordingQualityHigh);
                        }
                    }
                }
            });
        }
    }

    /**
     * 开始客户端录音。
     * <p>
     * Agora SDK 支持通话过程中在客户端进行录音。该方法录制频道内所有用户的音频，并生成一个包含所有用户声音的录音文件，录音文件格式可以为：
     * <p>
     * .wav：文件大，音质保真度高
     * .aac：文件小，有一定的音质保真度损失
     * 请确保 App 里指定的目录存在且可写。该接口需在加入频道之后调用。如果调用 leaveChannel 时还在录音，录音会自动停止。
     */
    public void startAudioRecording(final String saveAudioForAiFilePath, final int audioRecordingQualityHigh) {
        startAudioRecording(saveAudioForAiFilePath, audioRecordingQualityHigh, false);
    }

    /**
     * 停止客户端录音。
     * <p>
     * 该方法停止录音。该接口需要在 leaveChannel 之前调用，不然会在调用 leaveChannel 时自动停止。
     */
    public void stopAudioRecording() {
        MyLog.w(TAG, "stopAudioRecording");
        if (mCustomHandlerThread != null && mConfig.isRecording()) {
            mConfig.setRecording(false);
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(mConfig.getRecordingFromCallbackSavePath())) {
                        if (mConfig.isUseExternalAudioRecord()) {
                            mAudioEncoder.stop();
                        } else {
                            mAgoraRTCAdapter.stopAudioRecording();
                        }
                    } else {
                        mRawFrameWriter.stop();
                        mConfig.setRecordingFromCallbackSavePath(null);
                    }
                    if (!mConfig.isUseExternalAudio()) {
                        // 用声网采集，录制完成断开连接
                        disconnectRecord();
                    }
                }
            });
        }
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

    public void setRecognizeListener(ArcRecognizeListener recognizeConfig) {
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
        // Camera preview
        mCameraCapture = new CameraCapture(U.app().getApplicationContext(), mGLRender);
        mImgTexScaleFilter = new ImgTexScaleFilter(mGLRender);
        mBytedEffectFilter = new BytedEffectFilter(mGLRender);
        mImgTexMixer = new ImgTexMixer(mGLRender);
        mImgTexPreviewMixer = new ImgTexMixer(mGLRender);
        mImgTexPreviewMixer.setScalingMode(0, ImgTexMixer.SCALING_MODE_CENTER_CROP);
        mImgTexPreview = new ImgTexPreview();

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
        mGLRender.addListener(new GLRender.OnReadyListener() {
            @Override
            public void onReady() {
                mImgTexPreview.setEGL10Context(mGLRender.getEGL10Context());
            }
        });

        mCameraCapture.setOnCameraCaptureListener(new CameraCapture.OnCameraCaptureListener() {
            @Override
            public void onStarted() {
                Log.d(TAG, "CameraCapture ready");
                // TODO: notify to app
            }

            @Override
            public void onFacingChanged(int facing) {
                mCameraFacing = facing;
                updateFrontMirror();
                // TODO: notify to app
            }

            @Override
            public void onError(int err) {
                Log.e(TAG, "CameraCapture error: " + err);
                // TODO: notify to app
            }
        });

        // init with offscreen GLRender
        mGLRender.init(1, 1);
    }

    /**
     * Should be called on Activity.onResume or Fragment.onResume.
     */
    public void onResume() {
        Log.d(TAG, "onResume");
        mImgTexPreview.onResume();
    }

    /**
     * Should be called on Activity.onPause or Fragment.onPause.
     */
    public void onPause() {
        Log.d(TAG, "onPause");
        mImgTexPreview.onPause();
    }

    /**
     * Set GLSurfaceView as camera previewer.<br/>
     * Must set once before the GLSurfaceView created.
     *
     * @param surfaceView GLSurfaceView to be set.
     */
    public void setDisplayPreview(final GLSurfaceView surfaceView) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "setDisplayPreview");
                mImgTexPreview.setDisplayPreview(surfaceView);
                if (surfaceView != null) {
                    mImgTexPreview.getGLRender().addListener(mPreviewSizeChangedListener);
                }
            }
        });
    }

    /**
    /**
     * Set TextureView as camera previewer.<br/>
     * Must set once before the TextureView ready.
     *
     * @param textureView TextureView to be set.
     */
    public void setDisplayPreview(final TextureView textureView) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mImgTexPreview.setDisplayPreview(textureView);
                if(textureView!=null){
                    mImgTexPreview.getGLRender().addListener(mPreviewSizeChangedListener);
                }
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
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public void setRotateDegrees(final int rotate) throws IllegalArgumentException {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
     * @throws IllegalArgumentException
     */
    public void setCameraCaptureResolution(final int width, final int height) throws IllegalArgumentException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid resolution");
        }
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
     * @throws IllegalArgumentException
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
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
     * @throws IllegalArgumentException
     */
    public void setPreviewResolution(final int width, final int height) throws IllegalArgumentException {
        if (width < 0 || height < 0 || (width == 0 && height == 0)) {
            throw new IllegalArgumentException("Invalid resolution");
        }
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
     * @throws IllegalArgumentException
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
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
     * @throws IllegalArgumentException
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
     * @throws IllegalArgumentException
     */
    public void setTargetResolution(final int width, final int height) throws IllegalArgumentException {
        if (width < 0 || height < 0 || (width == 0 && height == 0)) {
            throw new IllegalArgumentException("Invalid resolution");
        }
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
     * @throws IllegalArgumentException
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
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
     * @throws IllegalArgumentException
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
        DebugLogView.println(TAG,"startCameraPreview" + " facing=" + facing);
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    /**
     * Stop camera preview.
     */
    public void stopCameraPreview() {
        DebugLogView.println(TAG,"stopCameraPreview");
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mIsCaptureStarted = false;
                mCameraCapture.stop();
                freeFboCacheIfNeeded();
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
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
        DebugLogView.println(TAG,"setLocalVideoRect" + " x=" + x + " y=" + y + " w=" + w + " h=" + h + " alpha=" + alpha);
        alpha = Math.max(0.0f, alpha);
        alpha = Math.min(alpha, 1.0f);
        final float a = alpha;
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mImgTexPreviewMixer.setRenderRect(0, x, y, w, h, a);
                // 重新计算预览尺寸
                setPreviewParams();
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
        DebugLogView.println(TAG,"bindRemoteVideoRect" + " userId=" + userId + " x=" + x + " y=" + y + " w=" + w + " h=" + h + " alpha=" + alpha);
        alpha = Math.max(0.0f, alpha);
        alpha = Math.min(alpha, 1.0f);
        final float a = alpha;

        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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

                // TODO: 仅在未开启本地视频，以及绑定了远端视图的情况下开启自动刷新
                mImgTexPreviewMixer.setEnableAutoRefresh(true, mPreviewFps);
            }
        });
    }

    /**
     * Unbind and remove remote video with user id.
     *
     * @param userId which user to unbind
     */
    public void unbindRemoteVideo(final int userId) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                for (int userId : mRemoteUserPinMap.keySet()) {
                    doUnbindRemoteVideo(userId);
                }
                mRemoteUserPinMap.clear();
                // 重置本地视频显示区域
                mImgTexPreviewMixer.setRenderRect(0, 0, 0, 1.0f, 1.0f, 1.0f);
                freeFboCacheIfNeeded();
            }
        });
    }

    private void doUnbindRemoteVideo(int userId) {
        DebugLogView.println(TAG,"doUnbindRemoteVideo userId=" + userId);
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
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                // 释放所有fbo缓存
                mGLRender.getFboManager().remove();
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
     * @param userId
     * @return
     */
    public boolean isFirstVideoDecoded(int userId) {
        boolean r = false;
        UserStatus userStatus = mUserStatusMap.get(userId);
        if (userStatus != null) {
            r = userStatus.isEnableVideo() && userStatus.isFirstVideoDecoded();
        }
        MyLog.d(TAG, "isFirstVideoDecoded" + " userId=" + userId + " r=" + r);
        return r;
    }

    private int getAvailableVideoMixerSink() {
        int idx = -1;
        for (int i = 1; i < mImgTexPreviewMixer.getSinkPinNum(); i++) {
            if (!mRemoteUserPinMap.containsValue(i)) {
                Log.d(TAG, "get available sink " + i);
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            Log.e(TAG, "unable to get available mixer sink!");
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
        mTargetWidth = align(mTargetWidth, 8);
        mTargetHeight = align(mTargetHeight, 8);

        Log.i(TAG, "calResolution: \n" +
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
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
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
                    Log.i(TAG, "onPreviewSizeChanged: " + width + "x" + height);
                    onPreviewSizeChanged(width, height);
                }
            };
}

