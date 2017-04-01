package com.wali.live.livesdk.live.liveshow;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.FragmentDataListener;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.base.GalileoConstants;
import com.mi.live.engine.streamer.GalileoStreamer;
import com.mi.live.engine.streamer.IStreamer;
import com.mi.live.engine.streamer.StreamerConfig;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.BaseSdkView;
import com.wali.live.livesdk.live.component.BaseLiveController;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.liveshow.data.MagicParamPresenter;
import com.wali.live.livesdk.live.liveshow.fragment.PrepareLiveFragment;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

import java.util.Arrays;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 组件控制器, 游戏直播
 */
public class LiveComponentController extends BaseLiveController {
    private static final String TAG = "LiveComponentController";

    public static final int[] VIDEO_RATE_360P = new int[]{400, 600, 800};

    @NonNull
    protected RoomBaseDataModel mMyRoomData; // 房间数据
    @NonNull
    protected LiveRoomChatMsgManager mRoomChatMsgManager; // 房间弹幕管理
    @NonNull
    protected StreamerPresenter mStreamerPresenter; // 推流器

    @NonNull
    protected MagicParamPresenter mMagicParamPresenter; // 美妆参数拉取

    @Nullable
    @Override
    protected String getTAG() {
        return TAG;
    }

    public LiveComponentController(
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull LiveRoomChatMsgManager roomChatMsgManager,
            @NonNull StreamerPresenter streamerPresenter) {
        mMyRoomData = myRoomData;
        mRoomChatMsgManager = roomChatMsgManager;
        mStreamerPresenter = streamerPresenter;

        mMagicParamPresenter = new MagicParamPresenter(this, GlobalData.app());
        mMagicParamPresenter.syncMagicParams();
    }

    @Override
    public void release() {
        super.release();
        mMagicParamPresenter.destroy();
    }

    @Override
    public void enterPreparePage(
            @NonNull BaseComponentSdkActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener) {
        MyLog.w(TAG, "prepareShowLive");
        PrepareLiveFragment.openFragment(fragmentActivity, requestCode, listener, mStreamerPresenter, mMyRoomData);
        mRoomChatMsgManager.setIsGameLiveMode(false);
    }

    @Override
    public void createStreamer(BaseSdkActivity activity, @NonNull View surfaceView, int clarity, Intent intent) {
        MyLog.w(TAG, "create streamer");
        StreamerConfig.Builder builder = new StreamerConfig.Builder();
        String videoRate = PreferenceUtils.getSettingString(
                GlobalData.app(), PreferenceKeys.PREF_KEY_VIDEO_RATE, null);
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
        mStreamerPresenter.setDisplayPreview(surfaceView);
        MyLog.w(TAG, "create streamer over");
    }

    @Override
    public BaseSdkView createSdkView(Activity activity) {
        return new LiveSdkView(activity, this);
    }

    @Override
    public void onResumeStream() {
    }

    @Override
    public void onPauseStream() {
    }
}
