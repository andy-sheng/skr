package com.mi.liveassistant.room.manager.live;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.camera.CameraView;
import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.preference.PreferenceKeys;
import com.mi.liveassistant.common.preference.PreferenceUtils;
import com.mi.liveassistant.data.Location;
import com.mi.liveassistant.engine.base.GalileoConstants;
import com.mi.liveassistant.engine.streamer.GalileoStreamer;
import com.mi.liveassistant.engine.streamer.IStreamer;
import com.mi.liveassistant.engine.streamer.StreamerConfig;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
import com.mi.liveassistant.room.presenter.live.NormalLivePresenter;

import java.util.Arrays;

/**
 * Created by lan on 17/4/20.
 */
public class NormalLiveManager extends BaseLiveManager {

    public static final int[] VIDEO_RATE_360P = new int[]{400, 600, 800};

    private CameraView mCameraView;

    public NormalLiveManager(@NonNull CameraView cameraView) {
        super();
        mIsGameLive = false;
        mLivePresenter = new NormalLivePresenter(this);
        mCameraView = cameraView;
        createStreamer();
    }

    @Override
    public void beginLive(Location location, String title, String coverUrl, ILiveCallback callback) {
        super.beginLive(location, title, coverUrl, callback);
        mLivePresenter.beginLive(location, title, coverUrl);
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
}
