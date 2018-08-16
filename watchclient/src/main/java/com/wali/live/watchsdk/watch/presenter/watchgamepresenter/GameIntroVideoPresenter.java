package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.TextureView;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.engine.player.ExoPlayer;
import com.mi.live.engine.player.GalileoPlayer;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.presenter.BasePlayerPresenter;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;
import com.wali.live.watchsdk.watch.view.watchgameview.VideoPluginView;

/**
 * Created by zyh on 2017/10/23.
 */
public class GameIntroVideoPresenter extends BasePlayerPresenter<TextureView, PullStreamerPresenter>
        implements TextureView.SurfaceTextureListener {

    String mOriginUrl;

    IView mVideoPluginView;

    @Override
    protected final String getTAG() {
        return "HeaderVideoPresenter";
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

    public void setIView(IView mVideoPluginView) {
        this.mVideoPluginView = mVideoPluginView;
    }

    public GameIntroVideoPresenter(@NonNull IEventController controller) {
        super(controller);
//        mStreamerPresenter = new PullStreamerPresenter(new WatchComponentController.EventPlayerCallback(controller));

        mStreamerPresenter = new PullStreamerPresenter(new PullStreamerPresenter.PlayerCallbackWrapper() {
            @Override
            public void onPrepared() {
                if (mVideoPluginView != null) {
                    mVideoPluginView.onPrepared();
                }
            }

            @Override
            public void onCompletion() {
                if (mVideoPluginView != null) {
                    mVideoPluginView.onCompleted();
                }
            }
        });
        mStreamerPresenter.setIsRealTime(true);

    }

    public void setOriginalStreamUrl(String videoUrl) {
        this.mOriginUrl = videoUrl;
        mStreamerPresenter.setOriginalStreamUrl(videoUrl);
    }

    @Override
    protected void doStartPlay() {
    }

    public boolean isStarted() {
        return mStreamerPresenter.isStarted();
    }

    public boolean isPause() {
        return mStreamerPresenter.isPaused();
    }

    public String getOriginalStreamUrl() {
        return mOriginUrl;
    }

    public void startVideo() {
        mStreamerPresenter.startWatch();
    }

    public void pauseVideo() {
        mStreamerPresenter.pauseWatch();
    }

    public void resumeVideo() {
        mStreamerPresenter.resumeWatch();
    }

    public void stopVideo() {
        mStreamerPresenter.stopWatch();
    }

    public void releaseVideo() {
        mStreamerPresenter.stopWatch();
        mStreamerPresenter.destroy();
    }

    public void mute(boolean mute) {
        mStreamerPresenter.mute(mute);
    }

    public void seekTo(long position) {
        mStreamerPresenter.seekTo(position);
    }

    public  long getCurrentPosition(){
        return mStreamerPresenter.getCurrentPosition();
    }

    public  long getDuration(){
        return mStreamerPresenter.getDuration();
    }

    @Override
    protected void updateShiftUp() {
        // nothing to do
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        ExoPlayer player = new ExoPlayer();
        player.setCallback(mStreamerPresenter.getInnerPlayerCallback());
        mStreamerPresenter.setStreamer(player);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        releaseVideo();
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }

    public  interface IView {
        void onCompleted();

        void onPrepared();
    }
}
