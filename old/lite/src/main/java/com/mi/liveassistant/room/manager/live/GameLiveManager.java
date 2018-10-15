package com.mi.liveassistant.room.manager.live;

import android.content.Intent;
import android.text.TextUtils;

import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.engine.base.GalileoConstants;
import com.mi.liveassistant.engine.streamer.GalileoStreamer;
import com.mi.liveassistant.engine.streamer.IStreamer;
import com.mi.liveassistant.engine.streamer.StreamerConfig;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.room.manager.live.callback.ILiveListener;
import com.mi.liveassistant.room.presenter.live.GameLivePresenter;

/**
 * Created by lan on 17/4/20.
 */
public class GameLiveManager extends BaseLiveManager<GameLivePresenter> {
    public static final int LOW_CLARITY = 0;
    public static final int MEDIUM_CLARITY = 1;
    public static final int HIGH_CLARITY = 2;

    private int mClarity = MEDIUM_CLARITY;

    private Intent mCaptureIntent;

    /**
     * #API# 构造函数
     *
     * @param liveListener 直播状态回调
     */
    public GameLiveManager(ILiveListener liveListener) {
        super(liveListener);
        mIsGameLive = true;
        mLivePresenter = new GameLivePresenter(mEventController, this);
    }

    /**
     * #API# 设置录屏Intent
     *
     * @param intent 录屏Intent
     * @see {@link android.media.projection.MediaProjectionManager MediaProjectionManager}
     */
    public void setCaptureIntent(Intent intent) {
        mCaptureIntent = intent;
    }

    /**
     * #API# 设置分辨率级别，默认值MEDIUM_CLARITY
     *
     * @param clarity 分辨率级别
     */
    public void setClarity(int clarity) {
        mClarity = clarity;
    }

    /**
     * #API# 设置静音
     *
     * @param isMute 是否静音, true为静音, false为取消静音
     */
    public void muteMic(boolean isMute) {
        if (mLivePresenter != null) {
            mLivePresenter.muteMic(isMute);
        }
    }

    protected void createStreamer() {
        MyLog.w(TAG, "create streamer, clarity=" + mClarity);
        StreamerConfig.Builder builder = new StreamerConfig.Builder();
        int width, height;
        switch (mClarity) {
            case LOW_CLARITY:
                width = GalileoConstants.GAME_LOW_RESOLUTION_WIDTH;
                height = GalileoConstants.GAME_LOW_RESOLUTION_HEIGHT;
                builder.setMinAverageVideoBitrate(500);
                builder.setMaxAverageVideoBitrate(500);
                break;
            case MEDIUM_CLARITY:
                width = GalileoConstants.GAME_MEDIUM_RESOLUTION_WIDTH;
                height = GalileoConstants.GAME_MEDIUM_RESOLUTION_HEIGHT;
                builder.setMinAverageVideoBitrate(1000);
                builder.setMaxAverageVideoBitrate(1000);
                break;
            case HIGH_CLARITY:
            default:
                width = GalileoConstants.GAME_HIGH_RESOLUTION_WIDTH;
                height = GalileoConstants.GAME_HIGH_RESOLUTION_HEIGHT;
                builder.setMinAverageVideoBitrate(2000);
                builder.setMaxAverageVideoBitrate(2000);
                break;
        }
        builder.setAutoAdjustBitrate(true);
        builder.setFrameRate(15);
        builder.setSampleAudioRateInHz(44100);

        IStreamer streamer = new GalileoStreamer(GlobalData.app(),
                UserAccountManager.getInstance().getUuid(), width, height, false);
        streamer.setConfig(builder.build());

        String clientIp = MiLinkClientAdapter.getsInstance().getClientIp();
        if (!TextUtils.isEmpty(clientIp)) {
            streamer.setClientPublicIp(clientIp);
        }

        mStreamerPresenter.setStreamer(streamer);
        mLivePresenter.initStreamer(streamer, width, height, mCaptureIntent);
    }

    @Override
    protected void startLive() {
        super.startLive();
        mLivePresenter.startLive();
    }

    @Override
    protected void stopLive() {
        super.stopLive();
        mLivePresenter.stopLive();
    }
}
