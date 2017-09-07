package com.wali.live.livesdk.live.livegame;

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
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.base.GalileoConstants;
import com.mi.live.engine.streamer.GalileoStreamer;
import com.mi.live.engine.streamer.IStreamer;
import com.mi.live.engine.streamer.StreamerConfig;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.BaseSdkView;
import com.wali.live.livesdk.live.component.BaseLiveController;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.livegame.fragment.PrepareLiveFragment;
import com.wali.live.livesdk.live.presenter.GameLivePresenter;
import com.wali.live.livesdk.live.presenter.RoomInfoPresenter;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 组件控制器, 游戏直播
 */
public class GameLiveController extends BaseLiveController {
    private static final String TAG = "GameLiveController";

    @NonNull
    protected RoomBaseDataModel mMyRoomData; // 房间数据
    @NonNull
    protected LiveRoomChatMsgManager mRoomChatMsgManager; // 房间弹幕管理
    @NonNull
    protected StreamerPresenter mStreamerPresenter; // 推流器
    @NonNull
    protected GameLivePresenter mGameLivePresenter;
    @NonNull
    protected RoomInfoPresenter mRoomInfoPresenter;

    @Nullable
    @Override
    protected String getTAG() {
        return TAG;
    }

    public GameLiveController(
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull LiveRoomChatMsgManager roomChatMsgManager,
            @NonNull StreamerPresenter streamerPresenter) {
        mMyRoomData = myRoomData;
        mRoomChatMsgManager = roomChatMsgManager;
        mStreamerPresenter = streamerPresenter;
    }

    @Override
    public void release() {
        super.release();
        if (mGameLivePresenter != null) {
            mGameLivePresenter.destroy();
        }
        if (mRoomInfoPresenter != null) {
            mRoomInfoPresenter.destroy();
        }
    }

    @Override
    public void enterPreparePage(
            @NonNull BaseComponentSdkActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener) {
        MyLog.w(TAG, "prepareShowLive");
        PrepareLiveFragment.openFragment(fragmentActivity, requestCode, listener, mMyRoomData, mRoomChatMsgManager);
        mRoomChatMsgManager.setIsGameLiveMode(true);
    }

    @Override
    public void createStreamer(BaseSdkActivity activity, View surfaceView, int clarity, boolean isMute, Intent intent) {
        MyLog.w(TAG, "create streamer, clarity=" + clarity + ", isMute=" + isMute);
        StreamerConfig.Builder builder = new StreamerConfig.Builder();
        int width, height;
        switch (clarity) {
            case PrepareLiveFragment.LOW_CLARITY:
                width = GalileoConstants.GAME_LOW_RESOLUTION_WIDTH;
                height = GalileoConstants.GAME_LOW_RESOLUTION_HEIGHT;
                builder.setMinAverageVideoBitrate(500);
                builder.setMaxAverageVideoBitrate(500);
                break;
            case PrepareLiveFragment.MEDIUM_CLARITY:
                width = GalileoConstants.GAME_MEDIUM_RESOLUTION_WIDTH;
                height = GalileoConstants.GAME_MEDIUM_RESOLUTION_HEIGHT;
                builder.setMinAverageVideoBitrate(1000);
                builder.setMaxAverageVideoBitrate(1000);
                break;
            case PrepareLiveFragment.HIGH_CLARITY:
            default: // fall through
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
        mGameLivePresenter = new GameLivePresenter(streamer, mRoomChatMsgManager, mMyRoomData,
                width, height, intent, mRoomChatMsgManager.toString());
        mGameLivePresenter.muteMic(isMute);
        mRoomInfoPresenter = new RoomInfoPresenter(activity, mGameLivePresenter);
        MyLog.w(TAG, "create streamer over");
    }

    @Override
    public BaseSdkView createSdkView(Activity activity) {
        return new GameLiveSdkView(activity, this);
    }

    @Override
    public void onStartLive() {
        mGameLivePresenter.startGameLive();
        mRoomInfoPresenter.startLiveCover(mMyRoomData.getUid(), mMyRoomData.getRoomId());
    }

    @Override
    public void onStopLive(boolean wasKicked) {
        mGameLivePresenter.stopGameLive(wasKicked);
        mRoomInfoPresenter.destroy();
    }

    @Override
    public void onResumeStream() {
        if (mGameLivePresenter != null) {
            mGameLivePresenter.resumeStream();
        }
        if (mRoomInfoPresenter != null) {
            mRoomInfoPresenter.resumeTimer();
        }
    }

    @Override
    public void onPauseStream() {
        if (mGameLivePresenter != null) {
            mGameLivePresenter.pauseStream();
        }
        if (mRoomInfoPresenter != null) {
            mRoomInfoPresenter.pauseTimer();
        }
    }

    @Override
    public void onActivityResumed() {
        postEvent(MSG_ON_ACTIVITY_RESUMED);
        if (mGameLivePresenter != null) {
            mGameLivePresenter.resume();
        }
        if (mRoomInfoPresenter != null) {
            mRoomInfoPresenter.resume();
        }
    }

    @Override
    public void onActivityPaused() {
        if (mGameLivePresenter != null) {
            mGameLivePresenter.pause();
        }
        if (mRoomInfoPresenter != null) {
            mRoomInfoPresenter.pause();
        }
    }

    @Override
    public void onActivityStopped() {
        if (mGameLivePresenter != null) {
            mGameLivePresenter.stop();
        }
        if (mRoomInfoPresenter != null) {
            mRoomInfoPresenter.stop();
        }
    }

}
