package com.mi.live.engine.streamer;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.thread.ThreadPool;
import com.mi.live.engine.base.EngineEventClass;
import com.mi.live.engine.base.GalileoConstants;
import com.mi.live.engine.base.GalileoDeviceManager;
import com.mi.live.engine.base.GalileoRenderManager;
import com.xiaomi.broadcaster.BroadCaster;
import com.xiaomi.broadcaster.callback.BroadcastCallback;
import com.xiaomi.broadcaster.dataStruct.ConnectedServerInfo;
import com.xiaomi.broadcaster.dataStruct.RtmpServerInfo;
import com.xiaomi.broadcaster.enums.VCNetworkQuality;
import com.xiaomi.broadcaster.enums.VCSessionErrType;
import com.xiaomi.broadcaster.enums.VCSessionState;
import com.xiaomi.devicemanager.DeviceManager;
import com.xiaomi.rendermanager.RenderManager;
import com.xiaomi.rendermanager.videoRender.VideoStreamsView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by chenyong on 16/4/9.
 *
 * @module 推流模块，实现引擎推流功能
 */
public class GalileoStreamer implements IStreamer {

    private static final String TAG = GalileoStreamer.class.getSimpleName();

    private static final String DEFAULT_PORT = "1935";
    private static final int DEFAULT_FRAME_RATE = 15;
    private static final int DEFAULT_BIT_RATE = 4 * 100 * 1024;

    private boolean mHeadsetPlugged = false;
    private boolean mIsPlayingMusic = false;

    private float mMusicVolume = 1.0f;
    private float mVoiceVolume = 1.0f;
    private float mExtraVideoVolume = 1.0f;
    private float mMusicVoiceDeviate = 0.0f;

    private static final int MAX_MUSIC_AMPLIFICATION = 1;
    private static final int MAX_VOICE_AMPLIFICATION = 1;
    private static final int MAX_MUSIC_VOICE_DEVIATION = 500;
    private float mOptimalMusicInSpeakerMode = 0.8f;
    private float mOptimalVoiceInSpeakerMode = 0.8f;
    private float mOptimalOnlyVoiceInSpeakerMode = 0.8f;
    private float mOptimalMusicInHeadSetMode = 0.8f;
    private float mOptimalVoiceInHeadSetMode = 0.8f;
    private float mOptimalFeedbackInHeadSetMode = 0.8f;
    private int mOptimalIntrinsicMusicVoiceDelay = 0;

    private VideoStreamsView mLocalPreview;
    private StreamerConfig mConfig;
    private String mUpStreamUrl;
    private volatile boolean mIsPreviewStarted = false;
    private volatile boolean mIsRtmpServerStarted = false;

    private volatile OnProgressListener mOnProgressListener;

    private BroadCaster mBroadCaster;
    private DeviceManager mDeviceManager;
    private RenderManager mRenderManager;

    private long mPlayerId = 0;
    private long mLiveLineId = 0;
    private boolean mIsSelfLarge = true;

    private MixVideoModel mSelfMixVideoModel = new MixVideoModel();
    private MixVideoModel mRemoteMixVideoModel = new MixVideoModel();
    private int mCurDeviceAngle = 0;

    private boolean mHasMicSource;
    private boolean mIsDestroyed;
    private Lock mLock = new ReentrantLock();

    private class MixVideoModel {
        long streamId = 0;
        float locationX = 0;
        float locationY = 0;
        float scaledWidth = 1;
        float scaledHeight = 1;
        float displayWidth = 1;
        float displayHeight = 1;
        int layer = 0;
    }

    private BroadcastCallback mBroadcastCallback = new BroadcastCallback() {
        @Override
        public void onConnectionStatusChanged(VCSessionState vcSessionState) {
            MyLog.w(TAG, "connection status changed to " + vcSessionState.toString());
            switch (vcSessionState) {
                case VCSessionStateStarted:
                    EventBus.getDefault().post(new EngineEventClass.StreamerEvent(EngineEventClass.StreamerEvent.EVENT_TYPE_OPEN_STREAM_SUCC));
                    break;
            }
        }

        @Override
        public void onNetworkQualityStatus(VCNetworkQuality vcNetworkQuality) {

        }

        @Override
        public void onDetectedThroughput(float v, int i) {

        }

        @Override
        public void onAudioDataProcess(byte[] bytes, int i) {

        }

        @Override
        public void onAudioMixedMusicFinished() {
            OnProgressListener listener = mOnProgressListener;
            if (listener != null) {
                listener.onMusicCompleted();
            }
        }

        @Override
        public void onAudioMixedMusicProgress(int progress) {
            OnProgressListener listener = mOnProgressListener;
            if (listener != null) {
                listener.onMusicProgress(progress);
            }
        }

        @Override
        public void onTakingPicOk() {
            MyLog.d(TAG, "onTakingPic OK");
        }

        @Override
        public void onTakingPicFailed() {
            MyLog.d(TAG, "onTakingPic error");
        }

        @Override
        public void onVCSessionErr(VCSessionErrType vcSessionErrType) {
            MyLog.w(TAG, "VCSessionErrType=" + vcSessionErrType);
            switch (vcSessionErrType) {
                case kVCSessionErrRTMPBadName:
                    EventBus.getDefault().post(new EngineEventClass.StreamerEvent(EngineEventClass.StreamerEvent.EVENT_TYPE_ERROR, vcSessionErrType));
                    break;
                default:
                    EventBus.getDefault().post(new EngineEventClass.StreamerEvent(EngineEventClass.StreamerEvent.EVENT_TYPE_NEED_RECONNECT, vcSessionErrType));
                    break;
            }
        }

        @Override
        public void onStreamPublished(String s) {
            MyLog.w(TAG, "EVENT_TYPE_ON_STREAM_PUBLISHED");
            EventBus.getDefault().post(new EngineEventClass.StreamerEvent(EngineEventClass.StreamerEvent.EVENT_TYPE_ON_STREAM_PUBLISHED, s));
        }

        @Override
        public void onStreamClosed(String s) {
            MyLog.w(TAG, "EVENT_TYPE_ON_STREAM_CLOSED");
            EventBus.getDefault().post(new EngineEventClass.StreamerEvent(EngineEventClass.StreamerEvent.EVENT_TYPE_ON_STREAM_CLOSED));
        }

        @Override
        public void onAvgBiteRate(int i) {
            // TODO chenyong1 增加平均码率上报逻辑
        }
    };

    public GalileoStreamer(final Context context, final String userId, final int width, final int height, final boolean hasMicSource) {
        MyLog.w(TAG, "GalileoStreamer()");
        mHasMicSource = hasMicSource;
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setSpeaker enable=" + !mHeadsetPlugged);
                GalileoDeviceManager.INSTANCE.init(context);
                mDeviceManager = GalileoDeviceManager.INSTANCE.getDeviceManger();
                GalileoRenderManager.INSTANCE.init(context);
                mRenderManager = GalileoRenderManager.INSTANCE.getRenderManager();
                if (mDeviceManager != null && mRenderManager != null) {
                    mBroadCaster = new BroadCaster();
                    mBroadCaster.constructSession(context, mBroadcastCallback, GalileoConstants.LIVE_LOW_RESOLUTION_HEIGHT, GalileoConstants.LIVE_LOW_RESOLUTION_WIDTH, height, width, DEFAULT_FRAME_RATE, DEFAULT_BIT_RATE, mDeviceManager.getInstance(), hasMicSource);
                    mDeviceManager.setSpeaker(!mHeadsetPlugged);
                    mDeviceManager.enableRotation(true);
                    mDeviceManager.SetOrientation(0, 0);
                    if (!hasMicSource) {
                        mBroadCaster.forceToUseHardWareCodec(true);
                        mBroadCaster.useVbrMode(true);
                    }
                }
            }
        }, "GalileoStreamer");
    }

    @Override
    public void setConfig(final StreamerConfig config) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mConfig = config;
                    mBroadCaster.setFps(config.getFrameRate());
                    mBroadCaster.setVideoMinBitrate(config.getMinAverageVideoBitrate() * 1024);
                    mBroadCaster.setVideoMaxBitrate(config.getMaxAverageVideoBitrate() * 1024);
                    mBroadCaster.setAudioSampleRate(config.getSampleAudioRateInHz());
                    mBroadCaster.setUseAdaptiveBitrate(config.isAutoAdjustBitrate());
                    mBroadCaster.setMirrorMode(true);
                    enableVideoPreprocess(GalileoConstants.BEAUTY_LEVEL_HIGHEST);
                }
            }
        }, "setConfig");
    }

    @Override
    public void setDisplayPreview(View surfaceView) {
        mLocalPreview = (VideoStreamsView) surfaceView;
    }

    @Override
    @Deprecated
    public void updateUrl(String url) {
//        url = "rtmp://218.92.226.43/live/stream?wsHost=r2.zb.mi.com";
        mUpStreamUrl = url;
        MyLog.w(TAG, "mUpStreamUrl =" + mUpStreamUrl);
    }

    @Override
    @Deprecated
    public void startStream(final List<String> ipList) {
        MyLog.w(TAG, "startStream ipList:" + ipList);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null && !TextUtils.isEmpty(mUpStreamUrl)) {
                    if (ipList != null && ipList.size() > 0) {
                        ArrayList<String> ipResultList = new ArrayList<>();
                        for (String s : ipList) {
                            if (Collections.frequency(ipResultList, s) < 1) {
                                ipResultList.add(s);
                            }
                        }
                        int size = ipResultList.size();
                        String[] ipPortList = new String[size];
                        for (int i = 0; i < size; i++) {
                            String ip = ipResultList.get(i);
                            if (ip.contains(":")) {
                                ipPortList[i] = ip;
                            } else {
                                ipPortList[i] = ip + ":" + DEFAULT_PORT;
                            }
                        }
                        mBroadCaster.startRtmpSessionWithURL(mUpStreamUrl, ipPortList);
                    } else {
                        mBroadCaster.startRtmpSessionWithURL(mUpStreamUrl, new String[0]);
                    }
                }
            }
        }, "startStream");
    }

    @Override
    public void startStreamEx(@NonNull final RtmpServerInfo[] rtmpServerInfos) {
        MyLog.w(TAG, "startStreamEx");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.startRtmpSessionWithURLEx(rtmpServerInfos);
                }
            }
        }, "startStreamEx");
    }

    @Override
    public void stopStream() {
        MyLog.w(TAG, "stopStream");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.endRtmpSession();
                }
            }
        }, "stopStream");
    }

    @Override
    public void switchCamera() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.switchCamera();
                }
            }
        }, "switchCamera");
    }

    @Override
    public void resume() {
        MyLog.w(TAG, "startPreview");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (!mIsPreviewStarted && mBroadCaster != null) {
                    mBroadCaster.startPreview();
                    mIsPreviewStarted = true;
                }
            }
        }, "resume");
    }

    private void bindLocalRender() {
        MyLog.w(TAG, "bindLocalRender");
        if (mLocalPreview != null && mBroadCaster != null && mRenderManager.getRender("") == null) {
            MyLog.w(TAG, "bindLocalRendering");
            mRenderManager.bindRenderWithStream(mLocalPreview, "", false);
            if (16 * GlobalData.screenWidth != 9 * GlobalData.screenHeight) {
                MyLog.w(TAG, "setRenderModel for screen is not 16:9");
                mRenderManager.setRenderModel(mRenderManager.getRender(""), VideoStreamsView.RenderModel.RENDER_MODEL_CUT);
            }
            EventBus.getDefault().post(new EngineEventClass.LocalBindEvent());
        }
    }

    @Override
    public void pause() {
        MyLog.w(TAG, "stopPreview");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mIsPreviewStarted && mBroadCaster != null) {
                    mBroadCaster.stopPreview();
                    mIsPreviewStarted = false;
                }
            }
        }, "pause");
    }

    @Override
    public void pauseImmediately() {
        MyLog.w(TAG, "stopPreview");
        if (mIsPreviewStarted && mBroadCaster != null) {
            mBroadCaster.stopPreview();
            mIsPreviewStarted = false;
        }
    }

    @Override
    public void destroy() {
        MyLog.w(TAG, "destroy");
        mOnProgressListener = null;
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                mLock.lock();
                mIsDestroyed = true;
                mLock.unlock();
                if (mBroadCaster != null) {
                    VideoStreamsView render = mRenderManager.getRender("");
                    if (render != null) {
                        MyLog.w(TAG, "destroy unBindLocalRendering");
                        mRenderManager.unbindRenderWithStream(render);
                        mLocalPreview.destoryNativeRender();
                    } else {
                        MyLog.w(TAG, "destroy unBindLocalRendering render==null");
                    }
                    mLocalPreview = null;
                    mBroadCaster.destructSession();
                    mBroadCaster = null;
                    mDeviceManager = null;
                    mRenderManager = null;
                    GalileoDeviceManager.INSTANCE.destroy();
                    GalileoRenderManager.INSTANCE.destroy();
                }
            }
        }, "destroy");
    }

    @Override
    public boolean toggleTorch(final boolean open) {
        MyLog.w(TAG, "toggleTorch");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.setFlashState(open);
                }
            }
        }, "toggleTorch");
        return true;
    }

    @Override
    public boolean startMusic(final String path, OnProgressListener listener) {
        MyLog.w(TAG, "set startMixMusic");
        mOnProgressListener = listener;
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mIsPlayingMusic = true;
                    mDeviceManager.startMixMusic(path, false);
                    int voiceVolume = (int) (100 * getVoiceVolume(mVoiceVolume));
                    MyLog.w(TAG, "startMusic mVoiceVolume=" + mVoiceVolume + ", formatted volume=" + voiceVolume);
                    mDeviceManager.setForegroundVolume(voiceVolume);
                }
            }
        }, "startMusic");
        return true;
    }

    @Override
    public boolean pauseMusic() {
        MyLog.w(TAG, "set pauseMixMusic");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.pauseMixMusic();
                }
            }
        }, "pauseMusic");
        return true;
    }

    @Override
    public boolean resumeMusic() {
        MyLog.w(TAG, "set resumeMixMusic");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.resumeMixMusic();
                }
            }
        }, "resumeMusic");
        return true;
    }

    @Override
    public boolean stopMusic() {
        MyLog.w(TAG, "set stopMixMusic");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mIsPlayingMusic = false;
                    mDeviceManager.stopMixMusic();
                    int voiceVolume = (int) (100 * getVoiceVolume(mVoiceVolume));
                    MyLog.w(TAG, "stopMusic mVoiceVolume=" + mVoiceVolume + ", formatted volume=" + voiceVolume);
                    mDeviceManager.setForegroundVolume(voiceVolume);
                }
            }
        }, "stopMusic");
        return true;
    }

    @Override
    public boolean playAtmosphereMusic(final String path) {
        MyLog.w(TAG, "playAtmosphereMusic path=" + path);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.playEffective(path);
                }
            }
        }, "playAtmosphereMusic");
        return true;
    }

    @Override
    public void setOptimalDefaultParams(JSONObject params) {
        if (params == null) {
            MyLog.e(TAG, "setOptimalDefaultParams params is null");
            return;
        }
        if (PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceUtils.KEY_DEBUG_MEDIA_INFO, false)) {
            return;
        }
        try {
            if (params.has(OPTIMAL_MUSIC_IN_SPEAKER_MODE)) {
                mOptimalMusicInSpeakerMode = (float) params.getDouble(OPTIMAL_MUSIC_IN_SPEAKER_MODE);
            }
            if (params.has(OPTIMAL_VOICE_IN_SPEAKER_MODE)) {
                // mOptimalVoiceInSpeakerMode = (float) params.getDouble(OPTIMAL_VOICE_IN_SPEAKER_MODE);
            }
            if (params.has(OPTIMAL_ONLY_VOICE_IN_SPEAKER_MODE)) {
                // mOptimalOnlyVoiceInSpeakerMode = (float) params.getDouble(OPTIMAL_ONLY_VOICE_IN_SPEAKER_MODE);
            }
            if (params.has(OPTIMAL_MUSIC_IN_HEADSET_MODE)) {
                mOptimalMusicInHeadSetMode = (float) params.getDouble(OPTIMAL_MUSIC_IN_HEADSET_MODE);
            }
            if (params.has(OPTIMAL_VOICE_IN_HEADSET_MODE)) {
                // mOptimalVoiceInHeadSetMode = (float) params.getDouble(OPTIMAL_VOICE_IN_HEADSET_MODE);
            }
            if (params.has(OPTIMAL_FEEDBACK_IN_HEADSET_MODE)) {
                mOptimalFeedbackInHeadSetMode = (float) params.getDouble(OPTIMAL_FEEDBACK_IN_HEADSET_MODE);
            }
            if (params.has(OPTIMAL_INTRINSIC_MUSIC_VOICE_DELAY)) {
                mOptimalIntrinsicMusicVoiceDelay = params.getInt(OPTIMAL_INTRINSIC_MUSIC_VOICE_DELAY);
            }
            MyLog.d(TAG, "setOptimalDefaultParams "
                    + "SpeakerMusic=" + mOptimalMusicInSpeakerMode + ", "
                    + "SpeakerVoice=" + mOptimalVoiceInSpeakerMode + ", "
                    + "SpeakerOnlyVoice=" + mOptimalOnlyVoiceInSpeakerMode + ", "
                    + "HeadSetMusic=" + mOptimalMusicInHeadSetMode + ", "
                    + "HeadSetVoice=" + mOptimalVoiceInHeadSetMode + ", "
                    + "IntrinsicDelay=" + mOptimalIntrinsicMusicVoiceDelay);
        } catch (JSONException e) {
            MyLog.e(TAG, "setOptimalDefaultParams failed, just ignore");
        }
    }

    @Override
    public void setHeadsetPlugged(final boolean isPlugged) {
        MyLog.w(TAG, "setHeadsetPlugged isPlugged=" + isPlugged);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mHeadsetPlugged = isPlugged;
                    MyLog.w(TAG, "setSpeaker enable=" + !mHeadsetPlugged);
                    mDeviceManager.setSpeaker(!mHeadsetPlugged);
                    mDeviceManager.enableMicMixMusic(isPlugged);
                    int musicVolume = (int) (100 * getMusicVolume(mMusicVolume));
                    int voiceVolume = (int) (100 * getVoiceVolume(mVoiceVolume));
                    MyLog.w(TAG, "setHeadsetPlugged musicVolume=" + musicVolume + ", voiceVolume=" + voiceVolume);
                    mDeviceManager.setLoopbackBackgroundVolume(musicVolume);
                    mDeviceManager.setBackgroundVolume(musicVolume);
                    mDeviceManager.setForegroundVolume(voiceVolume);
                    if (mHasMicSource) {
                        if (mHeadsetPlugged) {
                            mBroadCaster.unmuteExtraAppAudioStream();
                        } else {
                            mBroadCaster.muteExtraAppAudioStream();
                        }
                    }
                    updateIPAudioSource();
                }
            }
        }, "setHeadsetPlugged");
    }

    private void updateIPAudioSource() {
        if (mPlayerId != 0 && mBroadCaster != null) {
            if (mHeadsetPlugged) {
                mBroadCaster.unmuteIPAudioSource(mPlayerId, true);
                mBroadCaster.unmuteIPAudioSource(mPlayerId, false);
            } else {
                mBroadCaster.unmuteIPAudioSource(mPlayerId, true);
                mBroadCaster.muteIPAudioSource(mPlayerId, false);
            }
            float extraVideoVolume = getMusicVolume(mExtraVideoVolume);
            mBroadCaster.setIPAudioSourceVolume(mPlayerId, extraVideoVolume, true);
            mBroadCaster.setIPAudioSourceVolume(mPlayerId, extraVideoVolume, false);
        }
    }

    @Override
    public void setBeautyLevel(final int beautyLevel) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    enableVideoPreprocess(beautyLevel);
                }
            }
        }, "setBeautyLevel");
    }

    private void enableVideoPreprocess(int beautyLevel) {
        MyLog.w(TAG, "enableVideoPreprocess beautyLevel=" + beautyLevel);
        switch (beautyLevel) {
            case GalileoConstants.BEAUTY_LEVEL_OFF:
                mDeviceManager.enableVideoPreprocess(false);
                break;
            case GalileoConstants.BEAUTY_LEVEL_LOWEST:
                mDeviceManager.enableVideoPreprocess(true);
                mDeviceManager.setSmoothLevel(1);
                break;
            case GalileoConstants.BEAUTY_LEVEL_LOW:
                mDeviceManager.enableVideoPreprocess(true);
                mDeviceManager.setSmoothLevel(2);
                break;
            case GalileoConstants.BEAUTY_LEVEL_MIDDLE:
                mDeviceManager.enableVideoPreprocess(true);
                mDeviceManager.setSmoothLevel(3);
                break;
            case GalileoConstants.BEAUTY_LEVEL_HIGH:
                mDeviceManager.enableVideoPreprocess(true);
                mDeviceManager.setSmoothLevel(4);
                break;
            case GalileoConstants.BEAUTY_LEVEL_HIGHEST:
                mDeviceManager.enableVideoPreprocess(true);
                mDeviceManager.setSmoothLevel(5);
                break;
            default:
                break;
        }
    }

    @Override
    public void setMuteAudio(final boolean isMute) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    if (isMute) {
                        mBroadCaster.muteBroadcastMicrophone();
                    } else {
                        mBroadCaster.unMuteBroadcastMicrophone();
                    }
                }
            }
        }, "setMuteAudio");

    }

    @Override
    public void setMusicVolume(final float volume) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mMusicVolume = volume;
                    int musicVolume = (int) (100 * getMusicVolume(mMusicVolume));
                    MyLog.w(TAG, "setMusicVolume mMusicVolume=" + mMusicVolume + ", formatted volume=" + musicVolume);
                    mDeviceManager.setLoopbackBackgroundVolume(musicVolume);
                    mDeviceManager.setBackgroundVolume(musicVolume);
                }
            }
        }, "setMusicVolume");
    }

    @Override
    public void setVoiceVolume(final float volume) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mVoiceVolume = volume;
                    int voiceVolume = (int) (100 * getVoiceVolume(mVoiceVolume));
                    MyLog.w(TAG, "setVoiceVolume mVoiceVolume=" + mVoiceVolume + ", formatted volume=" + voiceVolume);
                    mDeviceManager.setForegroundVolume(voiceVolume);
                }
            }
        }, "setVoiceVolume");
    }

    @Override
    public void setMusicVoiceDeviate(float deviate) {
        mMusicVoiceDeviate = deviate;
        MyLog.w(TAG, "setMusicVoiceDeviate deviate=" + getMusicVoiceDeviate());
    }

    @Override
    public void setReverbLevel(final int level) {
        MyLog.v(TAG, "setReverbLevel level=" + level);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    switch (level) {
                        case GalileoConstants.TYPE_ORIGINAL:
                            mDeviceManager.stopReverberation();
                            break;
                        case GalileoConstants.TYPE_RECORDING_STUDIO:
                            mDeviceManager.startReverberation(1);
                            break;
                        case GalileoConstants.TYPE_KTV:
                            mDeviceManager.startReverberation(2);
                            break;
                        case GalileoConstants.TYPE_CONCERT:
                            mDeviceManager.startReverberation(3);
                            break;
                        default:
                            break;
                    }
                }
            }
        }, "setReverbLevel");
    }

    @Override
    public void setMirrorMode(final boolean isMirrorMode) {
        MyLog.v(TAG, "setMirrorMode " + isMirrorMode);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.setMirrorMode(isMirrorMode);
                }
            }
        }, "setMirrorMode");
    }

    @Override
    public String getRtmpHostIP() {
        MyLog.v(TAG, "getRtmpHostIP ");
        BroadCaster broadCaster = mBroadCaster;
        if (broadCaster != null) {
            return broadCaster.getRemoteIP();
        } else {
            return null;
        }
    }

    @Override
    public ConnectedServerInfo getConnectedServerInfo() {
        MyLog.v(TAG, "ConnectedServerInfo ");
        BroadCaster broadCaster = mBroadCaster;
        if (broadCaster != null) {
            return broadCaster.getConnectedServerInfo();
        } else {
            return null;
        }
    }

    @Override
    public StreamerConfig getConfig() {
        return mConfig;
    }

    @Override
    public boolean setFocus(final float x, final float y, final float w, final float h) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    switch (mCurDeviceAngle) {
                        case GalileoConstants.ORIENTATION_PORTRAIT_NORMAL:
                            mDeviceManager.setFocusPoint(x / w - 0.5f, 0.5f - y / h);
                            break;
                        case GalileoConstants.ORIENTATION_LANDSCAPE_REVERSED:
                            mDeviceManager.setFocusPoint(0.5f - y / h, 0.5f - x / w);
                            break;
                        case GalileoConstants.ORIENTATION_PORTRAIT_REVERSED:
                            mDeviceManager.setFocusPoint(0.5f - x / w, y / h - 0.5f);
                            break;
                        case GalileoConstants.ORIENTATION_LANDSCAPE_NORMAL:
                            mDeviceManager.setFocusPoint(y / h - 0.5f, x / w - 0.5f);
                            break;
                    }
                }
            }
        }, "setFocus");
        return true;
    }

    @Override
    public void startMixVideo(final long liveLineId, final float x, final float y, final float width, final float height, final boolean isSelfLarge) {
        MyLog.w(TAG, "startMixVideo liveLineId=" + liveLineId + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", isSelfLarge=" + isSelfLarge);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    if (isSelfLarge) {
                        updateMixVideoModel(mSelfMixVideoModel, 0, 0, 0, 1, 1, 1, 1, 0);
                        updateMixVideoModel(mRemoteMixVideoModel, liveLineId, x, y, width, height, width, height, 1);
                        mixVideoStream();
                    } else {
                        updateMixVideoModel(mRemoteMixVideoModel, liveLineId, 0, 0, 1, 1, 1, 1, 0);
                        updateMixVideoModel(mSelfMixVideoModel, 0, x, y, width, height, width, height, 1);
                        mixVideoStream();
                    }
                    mLiveLineId = liveLineId;
                    mIsSelfLarge = isSelfLarge;
                }
            }
        }, "startMixVideo");
    }

    private void updateMixVideoModel(MixVideoModel model, long id, float locationX, float locationY, float scaledWidth, float scaledHeight,
                                     float displayWidth, float displayHeight, int layer) {
        model.streamId = id;
        model.locationX = locationX;
        model.locationY = locationY;
        model.scaledWidth = scaledWidth;
        model.scaledHeight = scaledHeight;
        model.displayWidth = displayWidth;
        model.displayHeight = displayHeight;
        model.layer = layer;
    }

    private void mixVideoStream() {
        mBroadCaster.addExternalVideoStream(mSelfMixVideoModel.streamId, mSelfMixVideoModel.locationX, mSelfMixVideoModel.locationY, mSelfMixVideoModel.scaledWidth, mSelfMixVideoModel.scaledHeight,
                mSelfMixVideoModel.displayWidth, mSelfMixVideoModel.displayHeight, mSelfMixVideoModel.layer);
        if (mRemoteMixVideoModel.streamId > 0) {
            mBroadCaster.addExternalVideoStream(mRemoteMixVideoModel.streamId, mRemoteMixVideoModel.locationX, mRemoteMixVideoModel.locationY, mRemoteMixVideoModel.scaledWidth, mRemoteMixVideoModel.scaledHeight,
                    mRemoteMixVideoModel.displayWidth, mRemoteMixVideoModel.displayHeight, mRemoteMixVideoModel.layer);
        }
    }

    @Override
    public void stopMixVideo() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    if (!mIsSelfLarge) {
                        updateMixVideoModel(mSelfMixVideoModel, 0, 0, 0, 1, 1, 1, 1, 0);
                        mBroadCaster.addExternalVideoStream(0, 0, 0, 1, 1, 1, 1, 0);
                    }
                    mBroadCaster.removeExternalVideoStream(mLiveLineId);
                    updateMixVideoModel(mRemoteMixVideoModel, 0, 0, 0, 1, 1, 1, 1, 0);
                }
            }
        }, "stopMixVideo");
    }

    @Override
    public void setAudioType(final int type) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.setAudioType(type);
                }
            }
        }, "setAudioType");
    }

    private float getMusicVolume(float musicVolume) {
        float optimal = mHeadsetPlugged ? mOptimalMusicInHeadSetMode : mOptimalMusicInSpeakerMode;
        if (musicVolume <= 1.0f) {
            return optimal * musicVolume;
        } else {
            return (MAX_MUSIC_AMPLIFICATION - optimal) * musicVolume + 2 * optimal - MAX_MUSIC_AMPLIFICATION;
        }
    }

    private float getVoiceVolume(float voiceVolume) {
        // 外放模式下，需要考虑是否在播放音乐
        float optimal = mHeadsetPlugged ? mOptimalVoiceInHeadSetMode : (mIsPlayingMusic ? mOptimalVoiceInSpeakerMode : mOptimalOnlyVoiceInSpeakerMode);
        if (voiceVolume <= 1.0f) {
            return optimal * voiceVolume;
        } else {
            return (MAX_VOICE_AMPLIFICATION - optimal) * voiceVolume + 2 * optimal - MAX_VOICE_AMPLIFICATION;
        }
    }

    private int getMusicVoiceDeviate() {
        float musicDeviate = mOptimalIntrinsicMusicVoiceDelay + mMusicVoiceDeviate * MAX_MUSIC_VOICE_DEVIATION;
        return Math.max(-MAX_MUSIC_VOICE_DEVIATION, Math.min((int) musicDeviate, MAX_MUSIC_VOICE_DEVIATION));
    }

    /*
    * 主播端开始添加照片
    * */
    @Override
    public void startAddExtra(final long streamId, final float leftX, final float leftY, final float scaleWidth, final float scaleHeight, final float displayWidth, final float displayHeight, final int layer) {
        MyLog.w(TAG, "startAddExtra, streamId=" + streamId + ", leftX=" + leftX + ", leftY=" + leftY + ", scaleWidth=" + scaleWidth + ", scaleHeight=" + scaleHeight
                + ", displayWidth" + displayWidth + ", displayHeight" + displayHeight + ", layer" + layer);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.addExternalVideoStream(streamId, leftX, leftY, scaleWidth,
                            scaleHeight, displayWidth, displayHeight, layer);
                }
            }
        }, "startAddExtra");
    }

    /*
    * 主播删除照片信息
    * */
    @Override
    public void stopAddExtra(final long streamId) {
        MyLog.w(TAG, "stopAddExtra");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.addExternalVideoStream(0, 0, 0, 1, 1, 1, 1, 0);
                    mBroadCaster.removeExternalVideoStream(streamId);
                }
            }
        }, "stopAddExtra");
    }

    /*
    * 添加的照片的具体信息
    * videoType, 0 for YUV420 , 1 for ARGB
    * */
    @Override
    public void putExtraDetailInfo(final int width, final int height, final byte[] data, final int stride, final int videoType, final int frameType, final long streamId) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.pushExtraYUVFrame(width, height, data, width, videoType, frameType,
                            streamId);
                }
            }
        }, "putExtraDetailInfo");
    }

    @Override
    public void putExtraDetailInfoWithTimestamp(int width, int height, byte[] data, int stride, int videoType, int frameType, long streamId, long timestamp) {
        if (mBroadCaster != null) {
            mBroadCaster.pushExtraYUVFrameWithTimestamp(width, height, data, stride, videoType, frameType,
                    streamId, timestamp);
        }
    }

    @Override
    public void startExtraVideo(final long streamId, final long audioSource) {
        MyLog.w(TAG, "startExtraVideo");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.addExternalAudioSourceOberver(streamId, audioSource);
                    mPlayerId = streamId;
                    updateIPAudioSource();
                }
            }
        }, "startExtraVideo");
    }

    @Override
    public void stopExtraVideo(final long streamId) {
        MyLog.w(TAG, "stopExtraVideo");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mPlayerId = 0;
                    mBroadCaster.addExternalVideoStream(0, 0, 0, 1, 1, 1, 1, 0);
                    mBroadCaster.removeExternalAudioSourceOberver(streamId);
                    mBroadCaster.removeExternalVideoStream(streamId);
                }
            }
        }, "stopExtraVideo");
    }

    @Override
    public void setAngle(final int deviceAngle, final int uiAngle) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    MyLog.w(TAG, "setAngle, deviceAngle=" + deviceAngle + ", uiAngle=" + uiAngle);
                    mCurDeviceAngle = deviceAngle;
                    mDeviceManager.SetOrientation(deviceAngle, uiAngle);
                    mBroadCaster.setAppRotation(deviceAngle);
                    mixVideoStream();
                }
            }
        }, "setAngle");
    }

    /**
     * mute麦克风
     */
    @Override
    public void muteMic() {
        MyLog.d(TAG, "muteMic");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.muteBroadcastMicrophone();
                }
            }
        }, "muteMic");
    }

    /**
     * mute麦克风
     */
    @Override
    public void unMuteMic() {
        MyLog.d(TAG, "unMuteMic");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.unMuteBroadcastMicrophone();
                }
            }
        }, "unMuteMic");
    }

    @Override
    public boolean startRtmpServerImmediately(int port, String name) {
        if (!mIsRtmpServerStarted && mBroadCaster != null) {
            MyLog.w(TAG, "startRtmpServer");
            mIsRtmpServerStarted = true;
            return mBroadCaster.startRtmpServer(port, name);
        }
        return false;
    }

    @Override
    public void stopRtmpServer() {
        MyLog.w(TAG, "stopRtmpServer");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mIsRtmpServerStarted && mBroadCaster != null) {
                    mBroadCaster.stopRtmpServer();
                    mIsRtmpServerStarted = false;
                }
            }
        }, "stopRtmpServer");
    }

    @Override
    public void removeVideoView(RelativeLayout layout, final String uid) {
        MyLog.w(TAG, "removeVideoView: uid=" + uid);
        if (null != layout) {
            ViewGroup.LayoutParams lp = layout.getLayoutParams();
            MyLog.w(TAG, "layout size: width = " + lp.width + ",height = " + lp.height);
            layout.removeAllViews();
            layout.setVisibility(View.INVISIBLE);
        }
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    final VideoStreamsView render = mRenderManager.getRender(uid);
                    if (render != null) {
                        MyLog.w(TAG, "unbindRenderWithStream");
                        mRenderManager.unbindRenderWithStream(render);
                        mRenderManager.destroyRender(render);
                    }
                }
            }
        }, "removeVideoView");
    }

    @Override
    public void showVideoView(RelativeLayout layout, final String uid, boolean isTop, boolean isForce) {
        MyLog.w(TAG, "showVideoView: uid=" + uid);
        if (layout != null && mBroadCaster != null) {
            ViewGroup.LayoutParams lp = layout.getLayoutParams();
            int width = lp.width;
            int height = lp.height;
            layout.removeAllViews();
            layout.setVisibility(View.VISIBLE);
            Point size = new Point(width, height);
            final VideoStreamsView createdRender = mRenderManager.createRender(size);
            if (null != createdRender) {
                createdRender.setVisibility(View.VISIBLE);
                layout.addView(createdRender, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                if (isTop) {
                    createdRender.setZOrderMediaOverlay(true);
                } else {
                    createdRender.setZOrderMediaOverlay(false);
                }
                ThreadPool.runOnEngine(new Runnable() {
                    @Override
                    public void run() {
                        if (mBroadCaster != null) {
                            MyLog.i(TAG, "showVideoOfUid bindRenderWithStream");
                            mRenderManager.bindRenderWithStream(createdRender, uid, !TextUtils.isEmpty(uid));
                        }
                    }
                }, "showVideoView");
            }
        }
    }

    @Override
    public void setIPAudioSource(final float volume) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mPlayerId != 0 && mBroadCaster != null) {
                    mExtraVideoVolume = volume;
                    float extraVideoVolume = getMusicVolume(mExtraVideoVolume);
                    MyLog.w(TAG, "setIPAudioSource volume=" + mExtraVideoVolume + ", formatted volume=" + extraVideoVolume);
                    mBroadCaster.setIPAudioSourceVolume(mPlayerId, extraVideoVolume, true);
                    mBroadCaster.setIPAudioSourceVolume(mPlayerId, extraVideoVolume, false);
                }
            }
        }, "setIPAudioSource");
    }

    @Override
    public void handleZoom(final double zoomFactor) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null && mDeviceManager.isVideoZoomSupported()) {
                    int maxZoomFactor = mDeviceManager.getSupportedVideoZoomMaxFactor();
                    double currentVideoZoomFactor = mDeviceManager.getCurrentVideoZoomFactor();
                    MyLog.w(TAG, " currentVideoZoomFactor" + currentVideoZoomFactor + "(zoomFactor * 40)" + (zoomFactor * 40));
                    currentVideoZoomFactor += (zoomFactor * 40);
                    if (currentVideoZoomFactor < 0) {
                        currentVideoZoomFactor = 0;
                    } else if (currentVideoZoomFactor > maxZoomFactor) {
                        currentVideoZoomFactor = maxZoomFactor;
                    }
                    mDeviceManager.setVideoZoomFactor((int) currentVideoZoomFactor);
                } else {
                    MyLog.w(TAG, "zoom is not supported");
                }

            }
        }, "handleZoom");
    }

    @Override
    public void switchRenderWithUid(final String uid1, final String uid2) {
        MyLog.w(TAG, "switchRenderWithUid");
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    final VideoStreamsView render1 = mRenderManager.getRender(uid1);
                    final VideoStreamsView render2 = mRenderManager.getRender(uid2);
                    if (render1 != null && render2 != null) {
                        mRenderManager.unbindRenderWithStream(render1);
                        mRenderManager.unbindRenderWithStream(render2);
                        mRenderManager.bindRenderWithStream(render1, uid2, !TextUtils.isEmpty(uid2));
                        mRenderManager.bindRenderWithStream(render2, uid1, !TextUtils.isEmpty(uid1));
                    }
                }
            }
        }, "switchRenderWithUid");
    }

    @Override
    public void putExtraAudioFrame(final int nSamples, final int nBytesPerSample, final int nChannels, final int samplesPerSec, final byte[] data) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.pushExtraAudioFrame(nSamples, nBytesPerSample, nChannels, samplesPerSec, data);
                }
            }
        }, "putExtraAudioFrame");
    }

    @Override
    public void putExtraAudioFrameWithTimestamp(int nSamples, int nBytesPerSample, int nChannels, int samplesPerSec, byte[] data, long timestamp) {
        mLock.lock();
        if (!mIsDestroyed && mBroadCaster != null) {
            mBroadCaster.pushExtraAudioFrameWithTimestamp(nSamples, nBytesPerSample, nChannels, samplesPerSec, data, timestamp);
        }
        mLock.unlock();
    }

    @Override
    public void loopbackAudio(final boolean enable) {
        MyLog.w(TAG, "loopbackAudio enable =" + enable);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.loopbackAudio(enable);
                }
            }
        }, "loopbackAudio");
    }

    //滤镜
    @Override
    public void setVideoFilterIntensity(final float filterIntensity) {
        MyLog.w(TAG, "setVideoFilterIntensity filterIntensity=" + filterIntensity);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.setVideoFilterIntensity(filterIntensity);
                }
            }
        }, "setVideoFilterIntensity");
    }

    @Override
    public void setVideoFilter(final String filter) {
        MyLog.w(TAG, "setVideoFilter filter=" + filter);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.setVideoFilter(filter);
                }
            }
        }, "setVideoFilter");
    }

    @Override
    public void muteIPAudioSource(final long streamId) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (streamId != 0 && mBroadCaster != null) {
                    mBroadCaster.muteIPAudioSource(streamId, true);
                    mBroadCaster.muteIPAudioSource(streamId, false);
                }
            }
        }, "muteIPAudioSource");
    }

    @Override
    public void hidePreview() {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.addExternalVideoStream(0, -100, -100, 1, 1, 1, 1, 0);
                }
            }
        }, "hidePreview");
    }

    @Override
    public void setUpOutputFrameResolution(final int width, final int height) {
        MyLog.w(TAG, "setUpOutputFrameResolution width=" + width + ", height=" + height);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.setUpOutputFrameResolution(width, height);
                }
            }
        }, "setUpOutputFrameResolution");
    }

    @Override
    public void setVideoMainStream(final long streamId, final boolean isMainStream) {
        MyLog.w(TAG, "setVideoMainStream streamId=" + streamId + ", isMainStream=" + isMainStream);
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mBroadCaster.setVideoMainStream(streamId, isMainStream);
                }
            }
        }, "setVideoMainStream");
    }

    @Override
    public void addVideoPreprocessFilter(final long filter) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                // type 0 普通filter 1 face filter
                if (mBroadCaster != null) {
                    mDeviceManager.addVideoPreprocessFilter(filter, 1);
                }
            }
        }, "addVideoPreprocessFilter");
    }

    @Override
    public void removeVideoPreprocessFilter(final long filter) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                // type 0 普通filter 1 face filter
                if (mBroadCaster != null) {
                    mDeviceManager.removeVideoPreprocessFilter(filter, 1);
                }
            }
        }, "removeVideoPreprocessFilter");
    }

    @Override
    public void setStickerPath(final String path) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                if (mBroadCaster != null) {
                    mDeviceManager.setStickerPath(path);
                }
            }
        }, "setStickerPath");
    }

    @Override
    public void setClientPublicIp(final String clientPublicIp) {
        ThreadPool.runOnEngine(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "setClientPublicIp=" + clientPublicIp);
                mBroadCaster.setClientPublicIp(clientPublicIp);
            }
        }, "setClientPublicIp");
    }

    @Override
    public long getIPCameraVideoSourceOberver() {
        if (mBroadCaster != null) {
            return mBroadCaster.getIPCameraVideoSourceOberver();
        } else {
            return 0;
        }
    }
}
