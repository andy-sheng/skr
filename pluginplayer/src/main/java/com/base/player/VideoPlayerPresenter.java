package com.base.player;

import android.content.Context;
import android.view.TextureView;

import com.base.global.GlobalData;
import com.base.player.data.PullStreamerPresenter;
import com.base.player.engine.GalileoPlayer;
import com.mi.liveassistant.player.VideoPlayerWrapperView;

/**
 * Created by yangli on 2017/11/28.
 */
public class VideoPlayerPresenter extends BasePlayerPresenter<TextureView, PullStreamerPresenter>
        implements TextureView.SurfaceTextureListener {

    private VideoPlayerWrapperView.IOuterCallBack mOuterCallBack;

    private final PullStreamerPresenter.PlayerCallbackWrapper mPlayerCallback =
            new PullStreamerPresenter.PlayerCallbackWrapper() {
                @Override
                public void onError(int what, int extra) {
                    if (mOuterCallBack != null) {
                        mOuterCallBack.onError(what);
                    }
                }

                @Override
                public void onShowLoading() {
                    if (mOuterCallBack != null) {
                        mOuterCallBack.onBufferingStart();
                    }
                }

                @Override
                public void onHideLoading() {
                    if (mOuterCallBack != null) {
                        mOuterCallBack.onBufferingEnd();
                    }
                }
            };

    @Override
    protected final String getTAG() {
        return "VideoPlayerPresenter";
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

    public VideoPlayerPresenter(boolean isRealTime) {
        mStreamerPresenter = new PullStreamerPresenter(mPlayerCallback);
        mStreamerPresenter.setIsRealTime(isRealTime);
        GalileoPlayer player = new GalileoPlayer(GlobalData.app(), "", "");
        player.setCallback(mStreamerPresenter.getInnerPlayerCallback());
        mStreamerPresenter.setStreamer(player);
    }

    @Override
    protected void doStartPlay() {
    }

    @Override
    protected void updateShiftUp() {
        // nothing to do
    }

    public void setOriginalStreamUrl(String videoUrl) {
        mStreamerPresenter.setOriginalStreamUrl(videoUrl);
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

    @Override
    public void destroy() {
        mStreamerPresenter.stopWatch();
        mStreamerPresenter.destroy();
    }

    public void mute(boolean mute) {
        mStreamerPresenter.mute(mute);
    }

    public boolean isMute() {
        return mStreamerPresenter.isMute();
    }

    public final void shiftUp(float ratio) {
        mStreamerPresenter.shiftUp(ratio);
    }

    public void setOuterCallBack(VideoPlayerWrapperView.IOuterCallBack outerCallBack) {
        mOuterCallBack = outerCallBack;
    }

    public final void notifyOrientation(boolean isLandscape) {
        notifyOrientation(isLandscape);
    }
}
