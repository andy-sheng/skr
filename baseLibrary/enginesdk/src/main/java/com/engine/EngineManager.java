package com.engine;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.CustomHandlerThread;
import com.common.utils.DeviceUtils;
import com.common.utils.U;
import com.engine.agora.AgoraEngineAdapter;
import com.engine.agora.AgoraOutCallback;
import com.engine.agora.effect.EffectModel;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.score.Score2Callback;
import com.engine.token.AgoraTokenApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 关于音视频引擎的都放在这个类里
 */
public class EngineManager implements AgoraOutCallback {

    public final static String TAG = "EngineManager";
    public static final String PREF_KEY_TOKEN_ENABLE = "key_agora_token_enable";
    static final int STATUS_UNINIT = 0;
    static final int STATUS_INITING = 1;
    static final int STATUS_INITED = 2;
    static final int MSG_JOIN_ROOM_TIMEOUT = 11;
    static final int MSG_JOIN_ROOM_AGAIN = 12;

    private Params mConfig = new Params(); // 为了防止崩溃
    private Object mLock = new Object();

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

    private CustomHandlerThread mCustomHandlerThread;

    private boolean mTokenEnable = false; // 是否开启token校验
    private String mLastJoinChannelToken; // 上一次加入房间用的token
    private String mRoomId = ""; // 房间id
    private boolean mInChannel = false; // 是否已经在频道中

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
        if (l.size() == 1 && l.get(0).uid == 0 && l.get(0).volume == 0) {
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
    public void onRecordingBuffer(final byte[] samples) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    saveRecordingFrame(samples);
                }
            });
        }
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

    private static class EngineManagerHolder {
        private static final EngineManager INSTANCE = new EngineManager();
    }

    private EngineManager() {
        AgoraEngineAdapter.getInstance().setOutCallback(this);
        mTokenEnable = U.getPreferenceUtils().getSettingBoolean(PREF_KEY_TOKEN_ENABLE, false);
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
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.removeMessage(MSG_JOIN_ROOM_AGAIN);
        }
        mUiHandler.removeMessages(MSG_JOIN_ROOM_TIMEOUT);
        if (status == STATUS_INITED) {
            if (mMusicTimePlayTimeListener != null && !mMusicTimePlayTimeListener.isDisposed()) {
                mMusicTimePlayTimeListener.dispose();
            }
            mInChannel = false;
            AgoraEngineAdapter.getInstance().destroy(true);
//            CbEngineAdapter.getInstance().destroy();
            mUserStatusMap.clear();
            mRemoteViewCache.clear();
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
                    if (isAnchor) {
                        AgoraEngineAdapter.getInstance().setClientRole(isAnchor);
                    } else {
                        AgoraEngineAdapter.getInstance().setClientRole(isAnchor);
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

    private void joinRoomInner2(final String roomid, final int userId, final String token) {
        MyLog.d(TAG, "joinRoomInner2" + " roomid=" + roomid + " userId=" + userId + " token=" + token);
        mLastJoinChannelToken = token;
        AgoraEngineAdapter.getInstance().leaveChannel();
        int retCode = AgoraEngineAdapter.getInstance().joinChannel(token, roomid, "Extra Optional Data", userId);
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
                    MyLog.w(TAG, "setClientRole" + " isAnchor=" + isAnchor);
                    AgoraEngineAdapter.getInstance().setClientRole(isAnchor);
                }
            });
        }
    }


    /* 视频基础开始 */

    /**
     * 开启唱吧引擎的自采集视频预览
     * 这个view也是之后的本地view
     */
    public void startPreview(SurfaceView surfaceView) {
        if (mConfig.isUseExternalVideo()) {
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
        if (mConfig.isUseExternalVideo()) {
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
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    mConfig.setLocalVideoStreamMute(muted);
                    AgoraEngineAdapter.getInstance().muteLocalVideoStream(muted);
                }
            });
        }
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
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    mConfig.setAllRemoteVideoStreamsMute(muted);
                    AgoraEngineAdapter.getInstance().muteAllRemoteVideoStreams(muted);
                }
            });
        }
    }

    public void setEnableSpeakerphone(final boolean enableSpeakerphone) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    mConfig.setEnableSpeakerphone(enableSpeakerphone);
                    AgoraEngineAdapter.getInstance().setEnableSpeakerphone(enableSpeakerphone);
                }
            });
        }
    }

    /**
     * 监听耳机插拔
     *
     * @param event
     */
    @Subscribe
    public void onEvent(DeviceUtils.HeadsetPlugEvent event) {
//        if (event.on) {
//            setEnableSpeakerphone(false);
//            enableInEarMonitoring(false);
//        } else {
//            setEnableSpeakerphone(true);
//            enableInEarMonitoring(false);
//        }
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
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {

                    UserStatus status = new UserStatus(mConfig.getSelfUid());
                    status.setAudioMute(muted);
                    EventBus.getDefault().post(new EngineEvent(EngineEvent.TYPE_USER_MUTE_AUDIO, status));

                    mConfig.setLocalAudioStreamMute(muted);
                    AgoraEngineAdapter.getInstance().muteLocalAudioStream(muted);
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
                    AgoraEngineAdapter.getInstance().muteAllRemoteAudioStreams(muted);
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
                    AgoraEngineAdapter.getInstance().enableInEarMonitoring(enable);
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
                    AgoraEngineAdapter.getInstance().setInEarMonitoringVolume(volume);
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
        MyLog.d(TAG,"adjustRecordingSignalVolume" + " volume=" + volume + " setConfig=" + setConfig);
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (setConfig) {
                        mConfig.setRecordingSignalVolume(volume);
                    }
                    AgoraEngineAdapter.getInstance().adjustRecordingSignalVolume(volume);
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
        adjustPlaybackSignalVolume(volume,true);
    }

    public void adjustPlaybackSignalVolume(final int volume, final boolean setConfig) {
        if(mCustomHandlerThread != null){
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if(setConfig) {
                        mConfig.setPlaybackSignalVolume(volume);
                    }
                    AgoraEngineAdapter.getInstance().adjustPlaybackSignalVolume(volume);
                }
            });
        }
    }
    /*音频基础结束*/

    /*音频高级扩展开始*/

    public void setAudioEffectStyle(final Params.AudioEffect styleEnum) {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    mConfig.setStyleEnum(styleEnum);
//                CbEngineAdapter.getInstance().setIFAudioEffectEngine(styleEnum);
                    AgoraEngineAdapter.getInstance().setIFAudioEffectEngine(styleEnum);
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
                    AgoraEngineAdapter.getInstance().playEffects(effectModel);
                }
            });
        }
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
                        AgoraEngineAdapter.getInstance().startAudioMixing(filePath, midiPath, loopback, replace, cycle);
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
        EngineManager.getInstance().muteLocalAudioStream(mConfig.isLocalAudioStreamMute());
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
                        AgoraEngineAdapter.getInstance().stopAudioMixing();
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
                        AgoraEngineAdapter.getInstance().resumeAudioMixing();
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
                        AgoraEngineAdapter.getInstance().pauseAudioMixing();
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
                .interval(0, 1000, TimeUnit.MILLISECONDS)
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
        MyLog.d(TAG,"adjustAudioMixingVolume" + " volume=" + volume + " setConfig=" + setConfig);
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (setConfig) {
                        mConfig.setAudioMixingVolume(volume);
                    }
                    AgoraEngineAdapter.getInstance().adjustAudioMixingVolume(volume);
                }
            });
        }
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
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    AgoraEngineAdapter.getInstance().setAudioMixingPosition(posMs);
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
        MyLog.d(TAG, "startAudioRecording" + " saveAudioForAiFilePath=" + saveAudioForAiFilePath + " audioRecordingQualityHigh=" + audioRecordingQualityHigh);
        mConfig.setRecording(true);
        if (mCustomHandlerThread != null) {
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
                    if (fromRecodFrameCallback) {
                        mConfig.setRecordingFromCallbackSavePath(saveAudioForAiFilePath);
                    } else {
                        AgoraEngineAdapter.getInstance().startAudioRecording(saveAudioForAiFilePath, audioRecordingQualityHigh);
                    }
                }
            });
        }
    }

    private void saveRecordingFrame(byte[] newBuffer) {
        String path = mConfig.getRecordingFromCallbackSavePath();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        BufferedSink bufferedSink = null;
        try {
            Sink sink = Okio.appendingSink(file);
            bufferedSink = Okio.buffer(sink);
            bufferedSink.write(newBuffer);
            MyLog.d(TAG, "写入文件 path:" + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (null != bufferedSink) {
                bufferedSink.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        MyLog.d(TAG, "stopAudioRecording");
        if (mCustomHandlerThread != null && mConfig.isRecording()) {
            mConfig.setRecording(false);
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(mConfig.getRecordingFromCallbackSavePath())) {
                        AgoraEngineAdapter.getInstance().stopAudioRecording();
                    } else {
                        mConfig.setRecordingFromCallbackSavePath(null);
                    }
                }
            });
        }
    }

    public int getLineScore1() {
        return AgoraEngineAdapter.getInstance().getScoreV1();
    }

    public void getLineScore2(int lineNum, Score2Callback callback) {
        AgoraEngineAdapter.getInstance().getScoreV2(lineNum, callback);
    }

    /*音频高级扩展结束*/

    /*打分相关开始*/

    public void startRecognize(RecognizeConfig recognizeConfig) {
        AgoraEngineAdapter.getInstance().startRecognize(recognizeConfig);
    }

    public void setRecognizeListener(ArcRecognizeListener recognizeConfig) {
        AgoraEngineAdapter.getInstance().setRecognizeListener(recognizeConfig);
    }

    public void stopRecognize() {
        AgoraEngineAdapter.getInstance().stopRecognize();
    }

    public void recognizeInManualMode(int lineNo) {
        AgoraEngineAdapter.getInstance().recognizeInManualMode(lineNo);
    }

    /*打分相关结束*/

    public static class JoinParams {
        public int userId;
        public String roomID;
        public String token;
    }


}
