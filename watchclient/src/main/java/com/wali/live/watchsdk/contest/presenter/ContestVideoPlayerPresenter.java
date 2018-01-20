package com.wali.live.watchsdk.contest.presenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.TextureView;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.engine.player.GalileoPlayer;
import com.mi.live.engine.player.IPlayer;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.presenter.BasePlayerPresenter;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;

import static com.wali.live.component.BaseSdkController.MSG_ON_STREAM_RECONNECT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_COMPLETED;

/**
 * Created by zyh on 2017/10/23.
 *
 * @module 冲顶大会的播放器
 */
public class ContestVideoPlayerPresenter extends BasePlayerPresenter<TextureView, PullStreamerPresenter>
        implements TextureView.SurfaceTextureListener {
    private static final int PLAYER_TIME_THRESHOLD = 3000;

    private IPlayer mPlayer;

    @Override
    protected final String getTAG() {
        return "ContestVideoPlayerPresenter";
    }

    @Override
    protected final Context getContext() {
        return mView.getContext();
    }

    @Override
    public void setView(TextureView textureView) {
        super.setView(textureView);
        if (mView != null) {
            mView.setSurfaceTextureListener(this);
        }
    }

    public ContestVideoPlayerPresenter(@NonNull IEventController controller, boolean isRealTime) {
        super(controller);
        mStreamerPresenter = new PullStreamerPresenter(new WatchComponentController.EventPlayerCallback(controller));
        mStreamerPresenter.setIsRealTime(isRealTime);
        mPlayer = new GalileoPlayer(GlobalData.app(), UserAccountManager.getInstance().getUuid(),
                MiLinkClientAdapter.getsInstance().getClientIp());
        mPlayer.setCallback(mStreamerPresenter.getInnerPlayerCallback());
        mStreamerPresenter.setStreamer(mPlayer);
    }

    public void setOriginalStreamUrl(String videoUrl) {
        mStreamerPresenter.setOriginalStreamUrl(videoUrl);
    }

    public long getCurrentAudioTimestamp() {
        return mPlayer.getCurrentAudioTimestamp();
    }

    @Override
    protected void doStartPlay() {
    }

    public void startVideo() {
        mStreamerPresenter.startWatch();
        mPlayer.setSpeedUpThreshold(PLAYER_TIME_THRESHOLD);
    }

    public void pauseVideo() {
        mStreamerPresenter.pauseWatch();
    }

    public void resumeVideo() {
        mStreamerPresenter.resumeWatch();
    }

    public void releaseVideo() {
        mStreamerPresenter.stopWatch();
        mStreamerPresenter.destroy();
    }

    public void mute(boolean mute) {
        mStreamerPresenter.mute(mute);
    }

    @Override
    protected void updateShiftUp() {
        // nothing to do
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
            case MSG_PLAYER_COMPLETED:
                break;
            case MSG_ON_STREAM_RECONNECT:
                break;
        }
        return false;
    }
}
