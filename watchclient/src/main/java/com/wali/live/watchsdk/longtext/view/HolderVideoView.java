package com.wali.live.watchsdk.longtext.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.base.activity.BaseSdkActivity;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.callback.ICommonCallBack;
import com.wali.live.watchsdk.view.VideoPlayerWrapperView;

/**
 * Created by lan on 2017/9/21.
 *
 * @description 主要是用在recyclerView
 */
public class HolderVideoView extends VideoPlayerWrapperView {
    public final static int PLAYER_INIT = 0;
    public final static int PLAYER_PLAYING = 1;
    public final static int PLAYER_PAUSE = 2;
    public final static int PLAYER_STOP = 3;

    private int mPlayerState = PLAYER_INIT;

    private VideoPresenter mVideoPresenter;
    private HolderVideoCallback mCallback;

    public HolderVideoView(Context context) {
        super(context);
        init();
    }

    public HolderVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
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
    }

    public void setHolderCallback(HolderVideoCallback callback) {
        mCallback = callback;
    }

    @Override
    public void play(String videoUrl) throws LoadLibraryException {
        super.play(videoUrl);
        if (!TextUtils.isEmpty(videoUrl)) {
            mPlayerState = PLAYER_PLAYING;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPlayerState != PLAYER_INIT) {
            mPlayerState = PLAYER_STOP;
            release();

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
                HolderVideoView.this.resume();
                mPlayerState = PLAYER_PLAYING;
            }
        }

        @Override
        public void pause() {
            super.pause();
            if (mPlayerState == PLAYER_PLAYING) {
                HolderVideoView.this.pause();
                mPlayerState = PLAYER_PAUSE;
            }
        }

        @Override
        public void destroy() {
            super.destroy();
            if (mPlayerState != PLAYER_INIT) {
                mPlayerState = PLAYER_STOP;
                HolderVideoView.this.release();
            }
        }
    }

    public static class HolderVideoCallback implements ICommonCallBack<Integer> {
        @Override
        public void process(Integer state) {
        }
    }
}
