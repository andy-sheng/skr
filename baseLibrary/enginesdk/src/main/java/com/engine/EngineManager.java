package com.engine;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.changba.songstudio.CbEngineAdapter;
import com.changba.songstudio.audioeffect.AudioEffectStyleEnum;
import com.common.log.MyLog;
import com.common.utils.CustomHandlerThread;
import com.common.utils.DeviceUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.engine.agora.AgoraEngineAdapter;
import com.engine.agora.AgoraOutCallback;
import com.engine.agora.effect.EffectModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 关于音视频引擎的都放在这个类里
 */
public class EngineManager implements AgoraOutCallback {

    public final static String TAG = "EngineManager";
    static final int STATUS_UNINIT = 0;
    static final int STATUS_INITING = 1;
    static final int STATUS_INITED = 2;
    private Params mConfig = null;
    private Object mLock = new Object();

    private int mStatus = STATUS_UNINIT;// 0未初始化 1 初始ing 2 初始化
    /**
     * 存储该房间所有用户在引擎中的状态的，
     * key为在引擎中的用户 id
     */
    private HashMap<Integer, UserStatus> mUserStatusMap = new HashMap<>();
    private HashSet<View> mRemoteViewCache = new HashSet<>();
    private Handler mUiHandler = new Handler();
    private HandlerTaskTimer mMusicTimePlayTimeListener;

    private String mInitFrom;

    private CustomHandlerThread mCustomHandlerThread;

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
        status.setFirstVideoDecoded(true);
        status.setFirstVideoWidth(width);
        status.setFirstVideoHeight(height);
        tryBindRemoteViewAutoOnMainThread("onFirstRemoteVideoDecoded");
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_FIRST_VIDEO_DECODED, status));
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        UserStatus userStatus = ensureJoin(uid);
        userStatus.setIsSelf(true);
        mConfig.setSelfUid(uid);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_JOIN, userStatus));
    }

    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        UserStatus userStatus = ensureJoin(uid);
        userStatus.setIsSelf(true);
        EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_REJOIN, userStatus));
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onClientRoleChanged(int oldRole, int newRole) {
        // 只有切换时才会触发
        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_USER_ROLE_CHANGE);
        EngineEvent.RoleChangeInfo roleChangeInfo = new EngineEvent.RoleChangeInfo(oldRole,newRole);
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
        if (l.size() == 1 && l.get(0).uid == 0 && l.get(0).volume == 0) {
            return;
        }
        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION, null);
        engineEvent.obj = l;
        EventBus.getDefault().post(engineEvent);
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

    private static class EngineManagerHolder {
        private static final EngineManager INSTANCE = new EngineManager();
    }

    private EngineManager() {
        AgoraEngineAdapter.getInstance().setOutCallback(this);
    }

    public static final EngineManager getInstance() {
        return EngineManagerHolder.INSTANCE;
    }

    public void init(final String from, final Params params) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.destroy();
        }
        mCustomHandlerThread = new CustomHandlerThread(TAG) {
            @Override
            protected void processMessage(Message var1) {

            }
        };
        final int oldStatus = mStatus;
        mStatus = STATUS_INITING;
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                MyLog.d(TAG, "init" + " from=" + from + " params=" + params);
                destroyInner(oldStatus);
                mStatus = STATUS_INITED;
                mInitFrom = from;
                mConfig = params;
                synchronized (mLock) {
                    mLock.notifyAll();
                }
                AgoraEngineAdapter.getInstance().init(mConfig);
                CbEngineAdapter.getInstance().init(mConfig);
                setAudioEffectStyle(mConfig.getStyleEnum());
                if (!EventBus.getDefault().isRegistered(EngineManager.this)) {
                    EventBus.getDefault().register(EngineManager.this);
                }
            }
        });

    }

    public boolean isInit() {
        return mStatus == STATUS_INITED;
    }

    public Params getParams() {
        if (mStatus == STATUS_INITED) {
            return mConfig;
        } else if (mStatus == STATUS_UNINIT) {
            return null;
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
                AgoraEngineAdapter.getInstance().leaveChannel();
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
                        destroyInner(mStatus);
                        mCustomHandlerThread.destroy();
                        mStatus = STATUS_UNINIT;
                    }
                }
            });
        }
    }

    private void destroyInner(int status) {
        if (status == STATUS_INITED) {
            if (mMusicTimePlayTimeListener != null) {
                mMusicTimePlayTimeListener.dispose();
            }
            AgoraEngineAdapter.getInstance().destroy(true);
            CbEngineAdapter.getInstance().destroy();
            mUserStatusMap.clear();
            mRemoteViewCache.clear();
            mUiHandler.removeCallbacksAndMessages(null);
            mConfig = null;
            EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_ENGINE_DESTROY, null));
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        }
    }

    public void startRecord() {
        if (mConfig.isUseCbEngine()) {
//            CbEngineAdapter.getInstance().startRecord();
        } else {
            U.getToastUtil().showShort("mConfig.isUseCbEngine is false ，cancel");
        }
    }

    /**
     * 加入agora的房间
     *
     * @param roomid
     * @param userId
     * @param isAnchor 是否以主播的身份
     *                 不是主播只看不能说
     */
    public void joinRoom(final String roomid, final int userId, final boolean isAnchor) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "joinRoom" + " roomid=" + roomid + " userId=" + userId + " isAnchor=" + isAnchor);
                if (mConfig.getChannelProfile() == Params.CHANNEL_TYPE_LIVE_BROADCASTING) {
                    if (isAnchor) {
                        AgoraEngineAdapter.getInstance().setClientRole(isAnchor);
                    } else {
                        AgoraEngineAdapter.getInstance().setClientRole(isAnchor);
                    }
                }
                AgoraEngineAdapter.getInstance().joinChannel(null, roomid, "Extra Optional Data", userId);
                //TODO 临时关闭耳返
                if (U.getDeviceUtils().getHeadsetPlugOn()) {
                    setEnableSpeakerphone(false);
                    enableInEarMonitoring(false);
                } else {
                    setEnableSpeakerphone(true);
                    enableInEarMonitoring(false);
                }
            }
        });

    }

    public void setClientRole(final boolean isAnchor) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                AgoraEngineAdapter.getInstance().setClientRole(isAnchor);
            }
        });
    }


    /* 视频基础开始 */

    /**
     * 开启唱吧引擎的自采集视频预览
     * 这个view也是之后的本地view
     */
    public void startPreview(SurfaceView surfaceView) {
        if (mConfig.isUseCbEngine()) {
//            CbEngineAdapter.getInstance().startPreview(surfaceView);
        } else {
            // agora引擎好像加入房间后，预览才有效果
            AgoraEngineAdapter.getInstance().setLocalVideoRenderer(surfaceView);
            AgoraEngineAdapter.getInstance().startPreview();
        }
    }

    /**
     * 开启唱吧引擎的自采集视频预览
     */
    public void stopPreview() {
        if (mConfig.isUseCbEngine()) {
//            CbEngineAdapter.getInstance().stopPreview();
        } else {
            AgoraEngineAdapter.getInstance().stopPreview();
        }
    }

    /**
     * 绑定远端用户的视频view
     * 如果uid传的是0，会自动绑定一个当前没有绑定视图的用户
     * 如果当前都绑定视图，等下一个 onFirstRemoteVideoDecoded 就会绑定消费掉该视图
     *
     * @param uid
     * @param view
     */
    public void bindRemoteView(final int uid, final TextureView view) {
        MyLog.d(TAG, "bindRemoteView" + " uid=" + uid + " view=" + view);
        if (uid != 0) {
            final UserStatus userStatus = mUserStatusMap.get(uid);
            if (userStatus != null) {
                adjustViewWH2VideoWH(view, userStatus.getFirstVideoWidth(), userStatus.getFirstVideoHeight());
            }
            if (Looper.getMainLooper() == Looper.myLooper()) {
                userStatus.setView(view);
                AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        userStatus.setView(view);
                        AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
                    }
                });
            }
        } else {
            mRemoteViewCache.add(view);
            tryBindRemoteViewAutoOnMainThread("bindRemoteView");
        }
    }

    public void bindRemoteView(final int uid, final SurfaceView view) {
        MyLog.d(TAG, "bindRemoteView" + " uid=" + uid + " view=" + view);
        if (uid != 0) {
            final UserStatus userStatus = mUserStatusMap.get(uid);
            if (userStatus != null) {
                adjustViewWH2VideoWH(view, userStatus.getFirstVideoWidth(), userStatus.getFirstVideoHeight());
            }
            if (Looper.getMainLooper() == Looper.myLooper()) {
                userStatus.setView(view);
                AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        userStatus.setView(view);
                        AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(uid, view);
                    }
                });
            }
        } else {
            mRemoteViewCache.add(view);
            tryBindRemoteViewAutoOnMainThread("bindRemoteView");
        }
    }

    /**
     * 尝试自动绑定视图
     */
    private void tryBindRemoteViewAutoOnMainThread(String from) {
        MyLog.d(TAG, "tryBindRemoteViewAutoOnMainThread" + " from=" + from);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            tryBindRemoteViewAuto();
        } else {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    tryBindRemoteViewAuto();
                }
            });
        }

        return;
    }

    private void tryBindRemoteViewAuto() {
        // 判断当前有没有未绑定的
        List<View> canRemoveViews = new ArrayList<>();
        for (View view : mRemoteViewCache) {
            for (int key : mUserStatusMap.keySet()) {
                UserStatus userStatus = mUserStatusMap.get(key);
                if (!userStatus.isSelf()
                        && !userStatus.hasBindView()
                        && !userStatus.isVideoMute()
                        && userStatus.isFirstVideoDecoded()
                        ) {
                    // 这个用户有资格消费一个 surfaceview
                    if (view instanceof TextureView) {
                        canRemoveViews.add(view);
                        userStatus.setView(view);
                        adjustViewWH2VideoWH(view, userStatus.getFirstVideoWidth(), userStatus.getFirstVideoHeight());
                        AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(userStatus.getUserId(), (TextureView) view);
                        break;
                    } else if (view instanceof SurfaceView) {
                        canRemoveViews.add(view);
                        userStatus.setView(view);
                        adjustViewWH2VideoWH(view, userStatus.getFirstVideoWidth(), userStatus.getFirstVideoHeight());
                        AgoraEngineAdapter.getInstance().setRemoteVideoRenderer(userStatus.getUserId(), (SurfaceView) view);
                        break;
                    }
                }
            }
        }
        for (View view : canRemoveViews) {
            mRemoteViewCache.remove(view);
        }
    }

    /**
     * 矫正view的宽高和视频一致
     *
     * @param view
     * @param width
     * @param height
     */
    private void adjustViewWH2VideoWH(View view, int width, int height) {
        MyLog.d(TAG, "adjustViewWH2VideoWH" + " view=" + view + " width=" + width + " height=" + height);
        if (width != 0 && height != 0) {
            // 适应一下视频流的宽和高
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = width;
            lp.height = height;
        }
    }

    /**
     * 切换前/后摄像头
     */
    public void switchCamera() {
        AgoraEngineAdapter.getInstance().switchCamera();
    }

    /**
     * 是否打开闪光灯
     *
     * @param on true：打开
     *           false：关闭
     */
    public void setCameraTorchOn(boolean on) {
        mConfig.setCameraTorchOn(on);
        AgoraEngineAdapter.getInstance().setCameraTorchOn(on);
    }


    /**
     * 还有两个方法
     * isCameraFocusSupported 是否支持对焦
     * isCameraAutoFocusFaceModeSupported 是否支持手动对焦
     * 手动对焦
     *
     * @param x
     * @param y
     */
    public void setCameraFocusPositionInPreview(float x, float y) {
        AgoraEngineAdapter.getInstance().setCameraFocusPositionInPreview(x, y);
    }

    /**
     * 该方法设置本地视频镜像，须在开启本地预览前设置。如果在开启预览后设置，需要重新开启预览才能生效
     *
     * @param mode 0：默认镜像模式，即由 SDK 决定镜像模式
     *             1：启用镜像模式
     *             2：关闭镜像模式
     */
    public void setLocalVideoMirrorMode(int mode) {
        AgoraEngineAdapter.getInstance().setLocalVideoMirrorMode(mode);
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
                mConfig.setLocalVideoStreamMute(muted);
                AgoraEngineAdapter.getInstance().muteLocalVideoStream(muted);
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
    public void muteRemoteVideoStream(int uid, boolean muted) {
        AgoraEngineAdapter.getInstance().muteRemoteVideoStream(uid, muted);
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
                mConfig.setAllRemoteVideoStreamsMute(muted);
                AgoraEngineAdapter.getInstance().muteAllRemoteVideoStreams(muted);
            }
        });
    }

    public void setEnableSpeakerphone(final boolean enableSpeakerphone) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mConfig.setEnableSpeakerphone(enableSpeakerphone);
                AgoraEngineAdapter.getInstance().setEnableSpeakerphone(enableSpeakerphone);
            }
        });
    }

    /**
     * 监听耳机插拔
     *
     * @param event
     */
    @Subscribe
    public void onEvent(DeviceUtils.HeadsetPlugEvent event) {
        if (event.on) {
            setEnableSpeakerphone(false);
            enableInEarMonitoring(false);
        } else {
            setEnableSpeakerphone(true);
            enableInEarMonitoring(false);
        }
    }
    /*视频基础结束*/

    /*音频基础开始*/

    /**
     * 两个方法的区别是
     * enableLocalAudio：开启或关闭本地语音采集及处理
     * muteLocalAudioStream：停止或继续发送本地音频流
     *
     * @param muted
     */
    public void muteLocalAudioStream(final boolean muted) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mConfig.setLocalAudioStreamMute(muted);
                AgoraEngineAdapter.getInstance().muteLocalAudioStream(muted);
            }
        });
    }

    /**
     * 接收/停止接收所有音频流。
     * 适用于 A 在唱歌，B C 能互相聊天，但不能打扰到 A 的场景
     */
    public void muteAllRemoteAudioStreams(final boolean muted) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mConfig.setAllRemoteAudioStreamsMute(muted);
                AgoraEngineAdapter.getInstance().muteAllRemoteAudioStreams(muted);
            }
        });

    }

    /**
     * 开启或者关闭🎧耳返
     * 默认关闭
     */
    public void enableInEarMonitoring(final boolean enable) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mConfig.setEnableInEarMonitoring(enable);
                AgoraEngineAdapter.getInstance().enableInEarMonitoring(enable);
            }
        });
    }

    /**
     * 设定耳返音量
     *
     * @param volume 默认100
     */
    public void setInEarMonitoringVolume(final int volume) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mConfig.setInEarMonitoringVolume(volume);
                AgoraEngineAdapter.getInstance().setInEarMonitoringVolume(volume);
            }
        });
    }

    /**
     * 录音音量，可在 0~400 范围内进行调节 默认100
     *
     * @param volume
     */
    public void adjustRecordingSignalVolume(final int volume) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mConfig.setRecordingSignalVolume(volume);
                AgoraEngineAdapter.getInstance().adjustRecordingSignalVolume(volume);
            }
        });
    }

    /**
     * 播放音量，可在 0~400 范围内进行调节 默认100
     *
     * @param volume
     */
    public void adjustPlaybackSignalVolume(final int volume) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mConfig.setPlaybackSignalVolume(volume);
                AgoraEngineAdapter.getInstance().adjustPlaybackSignalVolume(volume);
            }
        });
    }

    /*音频基础结束*/

    /*音频高级扩展开始*/

    public void setAudioEffectStyle(final AudioEffectStyleEnum styleEnum) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mConfig.setStyleEnum(styleEnum);
                CbEngineAdapter.getInstance().setIFAudioEffectEngine(styleEnum);
                AgoraEngineAdapter.getInstance().setIFAudioEffectEngine(styleEnum);
            }
        });

    }

    /**
     * 播放音效
     */
    public void playEffects(final EffectModel effectModel) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                AgoraEngineAdapter.getInstance().playEffects(effectModel);
            }
        });

    }

    public List<EffectModel> getAllEffects() {
        return AgoraEngineAdapter.getInstance().getAllEffects();
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
        AgoraEngineAdapter.getInstance().setLocalVoicePitch(pitch);
    }

    /**
     * 设置本地语音音效均衡
     */
    public void setLocalVoiceEqualization() {
        AgoraEngineAdapter.getInstance().setLocalVoiceEqualization(mConfig.getBandFrequency(), mConfig.getBandGain());
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
        AgoraEngineAdapter.getInstance().setLocalVoiceReverb(reverbKey, value);
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
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                MyLog.d(TAG, "startAudioMixing" + " filePath=" + filePath + " midiPath=" + midiPath + " mixMusicBeginOffset=" + mixMusicBeginOffset + " loopback=" + loopback + " replace=" + replace + " cycle=" + cycle);
                mConfig.setMixMusicPlaying(true);
                mConfig.setMixMusicFilePath(filePath);
                mConfig.setMidiPath(midiPath);
                mConfig.setMixMusicBeginOffset(mixMusicBeginOffset);

                startMusicPlayTimeListener();
                EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_START);
                EventBus.getDefault().post(engineEvent);
                AgoraEngineAdapter.getInstance().startAudioMixing(filePath, loopback, replace, cycle);
            }
        });
    }

    public void startAudioMixing(String filePath, boolean loopback, boolean replace, int cycle) {
        startAudioMixing(filePath, null, 0, loopback, replace, cycle);
    }

    /**
     * 停止播放音乐文件及混音。
     * 请在频道内调用该方法。
     */
    public void stopAudioMixing() {
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
                    AgoraEngineAdapter.getInstance().stopAudioMixing();
                }
            }
        });

    }

    /**
     * 继续播放混音
     */
    public void resumeAudioMixing() {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(mConfig.getMixMusicFilePath())) {
                    mConfig.setMixMusicPlaying(true);
                    startMusicPlayTimeListener();
                    EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_START);
                    EventBus.getDefault().post(engineEvent);
                    AgoraEngineAdapter.getInstance().resumeAudioMixing();
                }
            }
        });
    }

    /**
     * 暂停播放音乐文件及混音
     */
    public void pauseAudioMixing() {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(mConfig.getMixMusicFilePath())) {
                    mConfig.setMixMusicPlaying(false);
                    stopMusicPlayTimeListener();
                    EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_PAUSE);
                    EventBus.getDefault().post(engineEvent);
                    AgoraEngineAdapter.getInstance().pauseAudioMixing();
                }
            }
        });

    }

    private void startMusicPlayTimeListener() {
        if (mMusicTimePlayTimeListener != null) {
            mMusicTimePlayTimeListener.dispose();
        }
        mMusicTimePlayTimeListener = HandlerTaskTimer.newBuilder().interval(1000)
                .start(new Observer<Integer>() {
                    int duration = -1;

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        int currentPostion = getAudioMixingCurrentPosition();
                        if (duration < 0) {
                            duration = getAudioMixingDuration();
                        }
                        EngineEvent engineEvent = new EngineEvent(EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER);
                        engineEvent.obj = new EngineEvent.MixMusicTimeInfo(currentPostion, duration);
                        EventBus.getDefault().post(engineEvent);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void stopMusicPlayTimeListener() {
        if (mMusicTimePlayTimeListener != null) {
            mMusicTimePlayTimeListener.dispose();
        }
    }

    /**
     * 调节混音音量大小
     *
     * @param volume 1-100 默认100
     */
    public void adjustAudioMixingVolume(final int volume) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mConfig.setAudioMixingVolume(volume);
                AgoraEngineAdapter.getInstance().adjustAudioMixingVolume(volume);
            }
        });

    }

    /**
     * @return 获取伴奏时长，单位ms
     */
    public int getAudioMixingDuration() {
        return AgoraEngineAdapter.getInstance().getAudioMixingDuration();
    }

    /**
     * @return 获取混音当前播放位置 ms
     */
    public int getAudioMixingCurrentPosition() {
        return AgoraEngineAdapter.getInstance().getAudioMixingCurrentPosition();
    }

    /**
     * 拖动混音进度条
     *
     * @param posMs
     */
    public void setAudioMixingPosition(final int posMs) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                AgoraEngineAdapter.getInstance().setAudioMixingPosition(posMs);
            }
        });

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
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                File file = new File(saveAudioForAiFilePath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                    }
                }
                AgoraEngineAdapter.getInstance().startAudioRecording(saveAudioForAiFilePath, audioRecordingQualityHigh);
            }
        });
    }

    /**
     * 停止客户端录音。
     * <p>
     * 该方法停止录音。该接口需要在 leaveChannel 之前调用，不然会在调用 leaveChannel 时自动停止。
     */
    public void stopAudioRecording() {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                AgoraEngineAdapter.getInstance().stopAudioRecording();
            }
        });
    }

    public int getLineScore() {
        int score = CbEngineAdapter.getInstance().getLineScore();
        return score;
    }
    /*音频高级扩展结束*/
}
