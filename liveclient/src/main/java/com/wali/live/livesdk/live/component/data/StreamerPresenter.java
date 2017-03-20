package com.wali.live.livesdk.live.component.data;


import android.content.Context;
import android.media.AudioManager;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.base.EngineEventClass;
import com.mi.live.engine.base.GalileoConstants;
import com.mi.live.engine.streamer.IStreamer;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.dns.IDnsStatusListener;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.utils.MagicParamUtils;
import com.wali.live.livesdk.live.dns.MultiCdnIpSelectionHelper;
import com.wali.live.livesdk.live.livegame.LiveComponentController;
import com.wali.live.proto.LiveCommonProto;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by yangli on 2017/03/08.
 * <p>
 * Generated using create_component_data.py
 *
 * @module 推流器数据
 */
public class StreamerPresenter extends BaseStreamerPresenter<StreamerPresenter.ReconnectHelper,
        MultiCdnIpSelectionHelper, IStreamer> {
    private static final String TAG = "StreamerPresenter";

    @NonNull
    private ComponentPresenter.IComponentController mComponentController;
    @NonNull
    protected RoomBaseDataModel mMyRoomData; // 房间数据

    private int mMusicVolume = 50; // 音乐音量

    // 通过设置面板调节的参数
    private int mVoiceVolume = 50; // 人声音量
    private boolean mMirrorImage = true; // 是否开启镜像
    private boolean mBackCamera = false; // 是否为后置摄像头
    private boolean mFlashLight = false; // 是否开启闪光灯
    private boolean mHifi = false; // 是否开启高保真
    private int mReverb = GalileoConstants.TYPE_ORIGINAL; // 混响类型

    // 通过美妆面板调节的参数
    private int mBeautyLevel = MagicParamUtils.getBeautyLevel();
    private int mFilterIntensity = MagicParamUtils.getFilterIntensity();
    private String mFilter = "";

    @Override
    protected String getTAG() {
        return TAG;
    }

    public void setComponentController(
            @NonNull ComponentPresenter.IComponentController componentController) {
        mComponentController = componentController;
    }

    public StreamerPresenter(
            @NonNull RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
        mUIHandler = new MyUIHandler(this);
        mReconnectHelper = new ReconnectHelper();
        mIpSelectionHelper = new MultiCdnIpSelectionHelper(GlobalData.app(), mReconnectHelper);
        EventBus.getDefault().register(this);
    }

    @Override
    public void destroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mStreamer != null) {
            mStreamer.destroy();
            mStreamer = null;
        }
        mUIHandler.removeCallbacksAndMessages(null);
    }

    // 设置人声音量
    public void setVoiceVolume(int volume) {
        if (mStreamer != null && mVoiceVolume != volume) {
            mVoiceVolume = volume;
            mStreamer.setVoiceVolume(mVoiceVolume / 50.0f);
        }
    }

    public int getVoiceVolume() {
        return mVoiceVolume;
    }

    // 设置混响
    public void setReverb(int type) {
        if (mStreamer != null && mReverb != type) {
            mReverb = type;
            mStreamer.setReverbLevel(mReverb);
        }
    }

    public int getReverb() {
        return mReverb;
    }

    // 开启高保真
    public void enableHifi(boolean enable) {
        if (mStreamer != null && mHifi != enable) {
            mHifi = enable;
            mStreamer.setAudioType(mHifi ? 1 : 0);
        }
    }

    public boolean isHifi() {
        return false;
    }

    // 切换前后置摄像头
    public void switchCamera() {
        if (mStreamer != null) {
            MyLog.w(TAG, "switchCamera");
            mBackCamera = !mBackCamera;
            mStreamer.switchCamera();
            mStreamer.setMirrorMode(mBackCamera ? false : mMirrorImage);
        }
    }

    // 当前是否为后置摄像头
    public boolean isBackCamera() {
        return mBackCamera;
    }

    // 开启镜像
    public void enableMirrorImage(boolean enable) {
        if (mStreamer != null && mMirrorImage != enable) {
            mMirrorImage = enable;
            if (!mBackCamera) {
                mStreamer.setMirrorMode(mMirrorImage);
            }
        }
    }

    public boolean isMirrorImage() {
        return mMirrorImage;
    }

    // 开启闪光灯
    public void enableFlashLight(boolean enable) {
        if (mStreamer != null && mFlashLight != enable) {
            mFlashLight = enable;
            mStreamer.toggleTorch(mFlashLight);
        }
    }

    public boolean isFlashLight() {
        return mFlashLight;
    }

    // 播放音效
    public void playAtmosphere(String path) {
        if (mStreamer != null) {
            mStreamer.playAtmosphereMusic(path);
        }
    }

    // 设置美颜
    public void setBeautyLevel(int beautyLevel) {
        if (mStreamer != null && mBeautyLevel != beautyLevel) {
            mBeautyLevel = beautyLevel;
            mStreamer.setBeautyLevel(beautyLevel);
        }
    }

    public int getBeautyLevel() {
        return mBeautyLevel;
    }

    // 设置滤镜
    public void setFilter(@NonNull String filter) {
        if (mStreamer != null && mFilter.equals(filter)) {
            mFilter = filter;
            mStreamer.setVideoFilter(filter);
        }
    }

    public String getFilter() {
        return mFilter;
    }

    // 设置滤镜强度
    public void setFilterIntensity(int intensity) {
        if (mStreamer != null && mFilterIntensity != intensity) {
            mFilterIntensity = intensity;
            mStreamer.setVideoFilterIntensity(intensity / 100f);
            MagicParamUtils.saveFilterIntensity(intensity);
        }
    }

    public int getFilterIntensity() {
        return mFilterIntensity;
    }

    public void setAngle(int angle) {
        if (mStreamer != null) {
            mStreamer.setAngle(angle, 0);
        }
    }

    public void setDisplayPreview(View surfaceView) {
        if (mStreamer != null) {
            mStreamer.setDisplayPreview(surfaceView);
            if (surfaceView != null) {
                mStreamer.setVideoFilterIntensity(mFilterIntensity);
                mStreamer.setVideoFilter(mFilter);
                mStreamer.setBeautyLevel(mBeautyLevel);
            }
        }
    }

    public void setOriginalStreamUrl(List<LiveCommonProto.UpStreamUrl> originalStreamUrlList, String originalUdpStreamUrl) {
        mIpSelectionHelper.setOriginalStreamUrl(originalStreamUrlList, originalUdpStreamUrl);
    }

    private JSONObject getOptimalKaraOkParams() {
        JSONObject object = new JSONObject();
        try {
            object.put(IStreamer.OPTIMAL_MUSIC_IN_SPEAKER_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_MUSIC_IN_SPEAKER_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_VOICE_IN_SPEAKER_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_VOICE_IN_SPEAKER_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_ONLY_VOICE_IN_SPEAKER_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_ONLY_VOICE_IN_SPEAKER_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_MUSIC_IN_HEADSET_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_MUSIC_IN_HEADSET_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_VOICE_IN_HEADSET_MODE,
                    PreferenceUtils.getSettingFloat(GlobalData.app(), IStreamer.OPTIMAL_VOICE_IN_HEADSET_MODE, 1.0f));
            object.put(IStreamer.OPTIMAL_INTRINSIC_MUSIC_VOICE_DELAY,
                    PreferenceUtils.getSettingInt(GlobalData.app(), IStreamer.OPTIMAL_INTRINSIC_MUSIC_VOICE_DELAY, 0));
        } catch (JSONException e) {
            MyLog.e(TAG, "getOptimalKaraOkParams failed, exception=" + e);
        }
        return object;
    }

    // 直播开始
    public void startLive() {
        if (mStreamer == null || mLiveStarted) {
            return;
        }
        mLiveStarted = true;
        mReconnectHelper.startStream();
        mStreamer.setOptimalDefaultParams(getOptimalKaraOkParams());
        mStreamer.setVoiceVolume(mVoiceVolume / 50.0f);
        AudioManager audioManager = (AudioManager)
                GlobalData.app().getSystemService(Context.AUDIO_SERVICE);
        mStreamer.setHeadsetPlugged(audioManager != null &&
                (audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn()));
    }

    // 直播结束
    public void stopLive() {
        if (mStreamer == null || !mLiveStarted) {
            return;
        }
        mLiveStarted = false;
        mReconnectHelper.stopStream();
    }

    // 直播暂停
    public void stopPreview() {
        if (mStreamer == null) {
            return;
        }
        mStreamer.pause();
    }

    // 直播恢复
    public void startPreview() {
        if (mStreamer == null) {
            return;
        }
        mStreamer.resume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EngineEventClass.StreamerEvent event) {
        MyLog.w(TAG, "onEventMainThread");
        if (event != null) {
            switch (event.type) {
                case EngineEventClass.StreamerEvent.EVENT_TYPE_OPEN_STREAM_SUCC:
                    // 推流成功
                    if (!mStreamStarted) { // 防止重连过程中，错误收到上次的成功事件
                        break;
                    }
                    MyLog.w(TAG, "EVENT_TYPE_OPEN_STREAM_SUCC");
                    mUIHandler.removeMessages(MSG_START_STREAM_TIMEOUT);
                    mIpSelectionHelper.onPushStreamSuccess();
                    mIpSelectionHelper.updateStutterStatus(false);
                    mComponentController.onEvent(ComponentController.MSG_ON_STREAM_SUCCESS);
                    break;
                case EngineEventClass.StreamerEvent.EVENT_TYPE_OPEN_CAMERA_FAILED:
                    MyLog.e(TAG, "EVENT_TYPE_OPEN_CAMERA_FAILED");
                    mComponentController.onEvent(LiveComponentController.MSG_OPEN_CAMERA_FAILED);
                    break;
                case EngineEventClass.StreamerEvent.EVENT_TYPE_OPEN_MIC_FAILED:
                    MyLog.e(TAG, "EVENT_TYPE_OPEN_MIC_FAILED");
                    mComponentController.onEvent(LiveComponentController.MSG_OPEN_MIC_FAILED);
                    break;
                case EngineEventClass.StreamerEvent.EVENT_TYPE_ERROR:
                case EngineEventClass.StreamerEvent.EVENT_TYPE_NEED_RECONNECT: // fall through
                    MyLog.e(TAG, (event.type == EngineEventClass.StreamerEvent.EVENT_TYPE_ERROR
                            ? "EVENT_TYPE_ERROR" : "EVENT_TYPE_NEED_RECONNECT"));
                    mReconnectHelper.startReconnect(event.type);
                    break;
                case EngineEventClass.StreamerEvent.EVENT_TYPE_ON_STREAM_CLOSED:
                    MyLog.w(TAG, "EVENT_TYPE_ON_STREAM_CLOSED");
                    break;
                default:
                    break;
            }
        }
    }

    // 域名解析、重连相关
    protected class ReconnectHelper extends BaseStreamerPresenter.ReconnectHelper
            implements IDnsStatusListener {

        @Override
        public void onDnsReady() {
            MyLog.w(TAG, "onDnsReady");
            if (mLiveStarted) {
                mUIHandler.removeMessages(MSG_START_STREAM);
                mReconnectHelper.doStartStream();
            }
        }

        private void doStartStream() {
            if (mStreamer != null && !mStreamStarted) {
                MyLog.w(TAG, "doStartStream");
                mStreamStarted = true;
                mIpSelectionHelper.ipSelect();
                mStreamer.startStreamEx(mIpSelectionHelper.getRtmpServerInfos());
                mUIHandler.sendEmptyMessageDelayed(MSG_START_STREAM_TIMEOUT, START_STREAM_TIMEOUT);
            } else {
                MyLog.w(TAG, "doStartStream is ignored, mStreamStarted=" + mStreamStarted);
            }
        }

        @Override
        protected void startStream() {
            if (mIpSelectionHelper.isDnsReady()) {
                MyLog.w(TAG, "startStream");
                doStartStream();
            } else {
                MyLog.w(TAG, "startStream but dns not ready");
                // 等待onDnsReady事件5秒之后，强制启动
                mUIHandler.sendEmptyMessageDelayed(MSG_START_STREAM, RECONNECT_TIMEOUT);
            }
        }

        @Override
        protected void stopStream() {
            if (mStreamStarted) {
                MyLog.w(TAG, "stopStream");
                mStreamStarted = false;
                mStreamer.stopMusic();
                mStreamer.toggleTorch(false);
                mStreamer.stopStream();
                mUIHandler.removeMessages(MSG_START_STREAM);
                mUIHandler.removeMessages(MSG_START_STREAM_TIMEOUT);
            } else {
                MyLog.w(TAG, "stopStream is ignored, mStreamStarted=" + mStreamStarted);
            }
        }

        @Override
        protected void startReconnect(int code) {
            if (mStreamer != null && mStreamStarted) {
                MyLog.w(TAG, "startReconnect, code = " + code);
                mStreamStarted = false;
                if (!mIpSelectionHelper.isStuttering()) {
                    mComponentController.onEvent(ComponentController.MSG_ON_STREAM_RECONNECT);
                }
                mIpSelectionHelper.updateStutterStatus(true);
                mStreamer.stopStream();
                mUIHandler.removeMessages(MSG_START_STREAM_TIMEOUT);
                if (code == EngineEventClass.StreamerEvent.EVENT_TYPE_ERROR) { // 延迟启动
                    mUIHandler.sendEmptyMessageDelayed(MSG_START_STREAM, RECONNECT_TIMEOUT);
                } else {
                    mUIHandler.sendEmptyMessage(MSG_START_STREAM);
                }
            } else {
                MyLog.w(TAG, "startReconnect is ignored, mStreamStarted=" + mStreamStarted);
            }
        }
    }

    protected static class MyUIHandler extends BaseStreamerPresenter.MyUIHandler<StreamerPresenter> {

        public MyUIHandler(
                @NonNull StreamerPresenter presenter) {
            super(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            final StreamerPresenter presenter = deRef(mPresenterRef);
            if (presenter == null || !presenter.mLiveStarted) {
                return;
            }
            switch (msg.what) {
                case MSG_START_STREAM:
                    MyLog.v(TAG, "MSG_START_STREAM");
                    presenter.mReconnectHelper.doStartStream();
                    break;
                case MSG_START_STREAM_TIMEOUT:
                    MyLog.w(TAG, "MSG_START_STREAM_TIMEOUT");
                    presenter.mReconnectHelper.startReconnect(0);
                    break;
                case MSG_START_STREAM_FAILED:
                    MyLog.w(TAG, "MSG_START_STREAM_FAILED");
                    presenter.mComponentController.onEvent(LiveComponentController.MSG_END_LIVE_UNEXPECTED,
                            new ComponentPresenter.Params().putItem(R.string.start_stream_failure));
                    break;
                default:
                    break;
            }
        }
    }
}
