package com.wali.live.watchsdk.channel.view.presenter;

import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.TextureView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;
import com.mi.live.engine.player.engine.GalileoPlayer;
import com.xiaomi.player.Player;

/**
 * Created by zyh on 2017/10/23.
 */

public class HeaderVideoPresenter extends ComponentPresenter<TextureView>
        implements TextureView.SurfaceTextureListener {
    private PullStreamerPresenter mStreamerPresenter;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private Surface mSurface;

    @Override
    public void setView(TextureView textureView) {
        super.setView(textureView);
        mView.setSurfaceTextureListener(this);
    }

    public HeaderVideoPresenter(@NonNull IEventController controller, boolean isRealTime) {
        super(controller);
        mStreamerPresenter = new PullStreamerPresenter(controller);
        mStreamerPresenter.setIsRealTime(isRealTime);

        GalileoPlayer player = new GalileoPlayer(GlobalData.app(), UserAccountManager.getInstance().getUuid(),
                MiLinkClientAdapter.getsInstance().getClientIp());
        player.setCallback(mStreamerPresenter.getPlayerCallback());
        mStreamerPresenter.setStreamer(player);
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

    public void destroyVideo() {
        mStreamerPresenter.stopWatch();
        mStreamerPresenter.destroy();
    }

    public void mute(boolean mute) {
        mStreamerPresenter.mute(mute);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        MyLog.w(TAG, "onSurfaceTextureAvailable");
        if (mSurface == null) {
            mSurface = new Surface(surface);
        }
        onSurfaceTextureSizeChanged(surface, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        MyLog.w(TAG, "onSurfaceTextureSizeChanged");
        if (mSurfaceWidth != width || mSurfaceHeight != height) {
            MyLog.w(TAG, "onSurfaceTextureSizeChanged width=" + width + ", height=" + width);
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            mStreamerPresenter.setSurface(mSurface);
            mStreamerPresenter.setGravity(Player.SurfaceGravity.SurfaceGravityResizeAspect,
                    mSurfaceWidth, mSurfaceHeight);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        MyLog.w(TAG, "onSurfaceTextureDestroyed");
        if (mSurface != null) {
            mSurfaceWidth = mSurfaceHeight = 0;
            mSurface.release();
            mSurface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    protected String getTAG() {
        return "HeaderVideoPresenter";
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        return false;
    }
}
