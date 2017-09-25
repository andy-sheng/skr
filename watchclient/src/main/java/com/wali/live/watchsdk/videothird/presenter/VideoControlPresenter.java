package com.wali.live.watchsdk.videothird.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.videothird.data.PullStreamerPresenter;
import com.wali.live.watchsdk.videothird.view.VideoControlView;

/**
 * Created by yangli on 2017/09/22.
 *
 * @module 播放控制表现
 */
public class VideoControlPresenter extends ComponentPresenter<VideoControlView.IView>
        implements VideoControlView.IPresenter {
    private static final String TAG = "VideoControlPresenter";

    private PullStreamerPresenter mStreamerPresenter;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public VideoControlPresenter(
            @NonNull IEventController controller,
            @NonNull PullStreamerPresenter streamerPresenter) {
        super(controller);
        mStreamerPresenter = streamerPresenter;
    }

    @Override
    public void startPlay() {
        if (mStreamerPresenter.isStarted()) {
            mStreamerPresenter.resumeWatch();
        } else {
            mStreamerPresenter.startWatch();
        }
    }

    @Override
    public void pausePlay() {
        mStreamerPresenter.pauseWatch();
    }

    @Override
    public void switchToFullScreen() {
        // TODO 加入切换全屏逻辑
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            default:
                break;
        }
        return false;
    }
}
