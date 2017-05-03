package com.mi.liveassistant.room.manager.watch;

import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mi.liveassistant.engine.player.widget.VideoPlayerPresenter;
import com.mi.liveassistant.engine.player.widget.VideoPlayerView;
import com.mi.liveassistant.room.RoomConstant;
import com.mi.liveassistant.room.manager.watch.callback.IWatchCallback;
import com.mi.liveassistant.room.presenter.streamer.PullStreamerPresenter;
import com.mi.liveassistant.room.presenter.watch.WatchPresenter;
import com.mi.liveassistant.room.view.IWatchView;

/**
 * Created by lan on 17/4/20.
 */
public class WatchManager implements IWatchView {
    private static final String TAG = RoomConstant.LOG_PREFIX + WatchManager.class.getSimpleName();

    private VideoPlayerView mSurfaceView;

    private WatchPresenter mWatchPresenter;

    private VideoPlayerPresenter mVideoPlayerPresenter;
    private PullStreamerPresenter mStreamerPresenter;

    /*外部接口的回调*/
    protected IWatchCallback mOutEnterCallback;

    private long mPlayerId;
    private String mLiveId;

    private String mDownStreamUrl;

    public WatchManager() {
        mWatchPresenter = new WatchPresenter(this);
        mStreamerPresenter = new PullStreamerPresenter();
    }

    public void setContainerView(@NonNull ViewGroup containerView) {
        if (mSurfaceView == null) {
            mSurfaceView = new VideoPlayerView(containerView.getContext());
        }
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        containerView.addView(mSurfaceView, lp);
    }

    @Override
    public void enterLive(long playerId, String liveId, IWatchCallback callback) {
        mPlayerId = playerId;
        mLiveId = liveId;
        mOutEnterCallback = callback;
        mWatchPresenter.enterLive(playerId, liveId);
    }

    @Override
    public void notifyEnterLiveFail(int errCode) {
        if (mOutEnterCallback != null) {
            mOutEnterCallback.notifyFail(errCode);
        }
    }

    @Override
    public void notifyEnterLiveSuccess(String downStreamUrl) {
        mDownStreamUrl = downStreamUrl;

        if (mOutEnterCallback != null) {
            mOutEnterCallback.notifySuccess();
        }

        // TODO 其实拉流和进房间没有必然关系
        mVideoPlayerPresenter = mSurfaceView.getVideoPlayerPresenter();
        mVideoPlayerPresenter.setRealTime(true);
        mVideoPlayerPresenter.setVideoStreamBufferTime(2);

        mStreamerPresenter.setStreamer(mVideoPlayerPresenter);
        mStreamerPresenter.setOriginalStreamUrl(mDownStreamUrl);
        mStreamerPresenter.startLive();
    }

    @Override
    public void leaveLive() {
        mWatchPresenter.leaveLive(mPlayerId, mLiveId);

        mStreamerPresenter.stopLive();
        mStreamerPresenter.destroy();
    }
}
