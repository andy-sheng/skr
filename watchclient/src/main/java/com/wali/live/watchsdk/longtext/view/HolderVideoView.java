package com.wali.live.watchsdk.longtext.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.TextureView;

import com.base.activity.BaseSdkActivity;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.callback.ICommonCallBack;
import com.wali.live.component.BaseSdkController;
import com.wali.live.watchsdk.channel.view.presenter.HeaderVideoPresenter;

/**
 * Created by lan on 2017/9/21.
 *
 * @description 主要是用在recyclerView
 */
public class HolderVideoView extends TextureView {
    public final static int PLAYER_INIT = 0;
    public final static int PLAYER_PLAYING = 1;
    public final static int PLAYER_PAUSE = 2;
    public final static int PLAYER_STOP = 3;

    private int mPlayerState = PLAYER_INIT;

    private VideoPresenter mVideoPresenter;
    private HolderVideoCallback mCallback;
    private HeaderVideoPresenter mHeaderVideoPresenter;
    private BaseSdkController mController = new BaseSdkController() {
        @Override
        protected String getTAG() {
            return "HolderVideoView";
        }
    };

    public HolderVideoView(Context context) {
        this(context, null);
    }

    public HolderVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HolderVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mVideoPresenter = new VideoPresenter();
        if (getContext() instanceof BaseSdkActivity) {
            ((BaseSdkActivity) getContext()).addPresent(mVideoPresenter);
        }
        mHeaderVideoPresenter = new HeaderVideoPresenter(mController, false);
        mHeaderVideoPresenter.setView(this);
    }

    public void setHolderCallback(HolderVideoCallback callback) {
        mCallback = callback;
    }

    public void play(String videoUrl) {
        if (!TextUtils.isEmpty(videoUrl)) {
            mHeaderVideoPresenter.setOriginalStreamUrl(videoUrl);
            mHeaderVideoPresenter.startVideo();
            mPlayerState = PLAYER_PLAYING;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPlayerState != PLAYER_INIT) {
            mPlayerState = PLAYER_STOP;
            mHeaderVideoPresenter.releaseVideo();
            if (mCallback != null) {
                mCallback.process(mPlayerState);
            }
        }
    }

    public class VideoPresenter extends RxLifeCyclePresenter {
        @Override
        public void resume() {
            super.resume();
            if (mPlayerState == PLAYER_PAUSE) {
                mHeaderVideoPresenter.resumeVideo();
                mPlayerState = PLAYER_PLAYING;
            }
        }

        @Override
        public void pause() {
            super.pause();
            if (mPlayerState == PLAYER_PLAYING) {
                mHeaderVideoPresenter.pauseVideo();
                mPlayerState = PLAYER_PAUSE;
            }
        }

        @Override
        public void destroy() {
            super.destroy();
            if (mPlayerState != PLAYER_INIT) {
                mPlayerState = PLAYER_STOP;
                mHeaderVideoPresenter.releaseVideo();
            }
        }
    }

    public static class HolderVideoCallback implements ICommonCallBack<Integer> {
        @Override
        public void process(Integer state) {
        }
    }
}
