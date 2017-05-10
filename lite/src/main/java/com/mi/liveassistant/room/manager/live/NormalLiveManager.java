package com.mi.liveassistant.room.manager.live;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.camera.CameraView;
import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.preference.PreferenceKeys;
import com.mi.liveassistant.common.preference.PreferenceUtils;
import com.mi.liveassistant.engine.base.GalileoConstants;
import com.mi.liveassistant.engine.streamer.GalileoStreamer;
import com.mi.liveassistant.engine.streamer.IStreamer;
import com.mi.liveassistant.engine.streamer.StreamerConfig;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.room.manager.live.callback.ILiveListener;
import com.mi.liveassistant.room.presenter.live.NormalLivePresenter;

import java.util.Arrays;

/**
 * Created by lan on 17/4/20.
 */
public class NormalLiveManager extends BaseLiveManager {
    public static final int[] VIDEO_RATE_360P = new int[]{400, 600, 800};

    private CameraView mCameraView;

    /**
     * #API# 构造函数
     *
     * @param cameraView   相机画面View
     * @param liveListener 直播状态回调
     */
    public NormalLiveManager(@NonNull CameraView cameraView, ILiveListener liveListener) {
        super(liveListener);
        mIsGameLive = false;
        mLivePresenter = new NormalLivePresenter(mEventController, this);
        mCameraView = cameraView;
        createStreamer();
    }

    @Override
    protected void createStreamer() {
        MyLog.w(TAG, "create streamer");
        StreamerConfig.Builder builder = new StreamerConfig.Builder();
        String videoRate = PreferenceUtils.getSettingString(PreferenceKeys.PREF_KEY_VIDEO_RATE, null);
        if (!TextUtils.isEmpty(videoRate)) {
            String[] videoRates = videoRate.split(",");
            MyLog.w(TAG, Arrays.toString(videoRates));
            for (int i = 0; i < videoRates.length; i++) {
                VIDEO_RATE_360P[i] = Integer.valueOf(videoRates[i]);
            }
        }
        builder.setMinAverageVideoBitrate(VIDEO_RATE_360P[0]);
        builder.setMaxAverageVideoBitrate(VIDEO_RATE_360P[2]);
        builder.setAutoAdjustBitrate(true);
        builder.setFrameRate(15);
        builder.setSampleAudioRateInHz(44100);
        int width = GalileoConstants.LIVE_LOW_RESOLUTION_WIDTH, height = GalileoConstants.LIVE_LOW_RESOLUTION_HEIGHT;
        IStreamer streamer = new GalileoStreamer(GlobalData.app(),
                UserAccountManager.getInstance().getUuid(), width, height, true);
        streamer.setConfig(builder.build());
        String clientIp = MiLinkClientAdapter.getsInstance().getClientIp();
        if (!TextUtils.isEmpty(clientIp)) {
            streamer.setClientPublicIp(clientIp);
        }
        mStreamerPresenter.setStreamer(streamer);
        mStreamerPresenter.setDisplayPreview(mCameraView);
        MyLog.w(TAG, "create streamer over");
    }

    @Override
    public void resume() {
        super.resume();
        mStreamerPresenter.startPreview();
    }

    @Override
    public void destroy() {
        super.destroy();
        mStreamerPresenter.destroy();
    }

    @Override
    public void pause() {
        super.pause();
        mStreamerPresenter.stopPreview();
    }

    /*engine start*/

    /**
     * #API# 设置人声音量
     *
     * @param volume 音量
     */
    public void setVoiceVolume(@IntRange(from = 0, to = 100) int volume) {
        mStreamerPresenter.setVoiceVolume(volume);
    }

    /**
     * #API# 开启美颜
     *
     * @param enable 是否开启美颜, true为开启, false为关闭
     */
    public void enableVideoSmooth(boolean enable) {
        if (enable) {
            mStreamerPresenter.setBeautyLevel(GalileoConstants.BEAUTY_LEVEL_OFF);
        } else {
            mStreamerPresenter.setBeautyLevel(GalileoConstants.BEAUTY_LEVEL_HIGHEST);
        }
    }

    /**
     * #API# 开启闪光灯
     *
     * @param enable 是否开启闪光灯, true为开启, false为关闭
     */
    public void enableFlashLight(boolean enable) {
        mStreamerPresenter.enableFlashLight(enable);
    }

    /**
     * #API# 查询闪光灯是否开启
     */
    public boolean isFlashLight() {
        return mStreamerPresenter.isFlashLight();
    }

    /**
     * #API# 翻转前后置摄像头
     */
    public void switchCamera() {
        mStreamerPresenter.switchCamera();
    }

    /**
     * #API# 查询是否为后置摄像头
     */
    public boolean isBackCamera() {
        return mStreamerPresenter.isBackCamera();
    }

    /**
     * #API# 设置设备旋转角度
     *
     * @param angle 旋转角度
     */
    public void setAngle(int angle) {
        mStreamerPresenter.setAngle(angle);
    }

    /*engine end*/
}
