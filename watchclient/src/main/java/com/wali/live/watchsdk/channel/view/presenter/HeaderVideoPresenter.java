package com.wali.live.watchsdk.channel.view.presenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.TextureView;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.engine.player.engine.GalileoPlayer;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.presenter.BasePlayerPresenter;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;

/**
 * Created by zyh on 2017/10/23.
 */
public class HeaderVideoPresenter extends BasePlayerPresenter<TextureView, PullStreamerPresenter>
        implements TextureView.SurfaceTextureListener {

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

    public HeaderVideoPresenter(@NonNull IEventController controller, boolean isRealTime) {
        super(controller);
        mStreamerPresenter = new PullStreamerPresenter(new WatchComponentController.EventPlayerCallback(controller));
        mStreamerPresenter.setIsRealTime(isRealTime);
        GalileoPlayer player = new GalileoPlayer(GlobalData.app(), UserAccountManager.getInstance().getUuid(),
                MiLinkClientAdapter.getsInstance().getClientIp());
        player.setCallback(mStreamerPresenter.getInnerPlayerCallback());
        mStreamerPresenter.setStreamer(player);
    }

    public void setOriginalStreamUrl(String videoUrl) {
        mStreamerPresenter.setOriginalStreamUrl(videoUrl);
    }

    @Override
    protected void doStartPlay() {
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
        return false;
    }
}
