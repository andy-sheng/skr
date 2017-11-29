package com.mi.liveassistant.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.player.VideoPlayerPresenter;

/**
 * Created by yangli on 2017/11/28.
 */
public class VideoPlayerWrapperView extends FrameLayout {
    private static final String TAG = "VideoPlayerWrapperView";

    private VideoPlayerPresenter mPlayerPresenter;

    private TextureView mVideoView;
    private TextView mWaterView;

    public VideoPlayerWrapperView(Context context) {
        this(context, null);
    }

    public VideoPlayerWrapperView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayerWrapperView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        GlobalData.setApplication(((Activity) context).getApplication());
        ((Activity) context).setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mVideoView = new TextureView(getContext());
        mWaterView = new TextView(getContext());

        final float density = getContext().getResources().getDisplayMetrics().density;

        // add video view
        addView(mVideoView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // add water mark view
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
        layoutParams.topMargin = layoutParams.rightMargin = (int) (20 * density);
        addView(mWaterView, layoutParams);
        mWaterView.setTextColor(0x99FFFFFF);
        mWaterView.setTextSize(13f);
        mWaterView.setShadowLayer(1, 1, 1, 0xFF000000);
        mWaterView.setText("MIBO");

        mPlayerPresenter = new VideoPlayerPresenter(true);
        mPlayerPresenter.setView(mVideoView);
        mPlayerPresenter.startPresenter();
    }

    public final void setOuterCallBack(IOuterCallBack callback) {
        mPlayerPresenter.setOuterCallBack(callback);
    }

    public boolean checkLibrary() {
        return GlobalData.isLoaded();
    }

    public void mute(boolean isMute) {
        mPlayerPresenter.mute(isMute);
    }

    public boolean isMute() {
        return mPlayerPresenter.isMute();
    }

    public void notifyOrientation(boolean isLandscape) {
        MyLog.w(TAG, "notifyOrientation isLandscape=" + isLandscape);
        mPlayerPresenter.notifyOrientation(isLandscape);
    }

    public void play(String videoUrl) throws LoadLibraryException {
        if (!checkLibrary()) {
            throw new LoadLibraryException("load library fail");
        }
        if (!TextUtils.isEmpty(videoUrl)) {
            mPlayerPresenter.setOriginalStreamUrl(videoUrl);
            mPlayerPresenter.startVideo();
        }
    }

    public void resume() {
        mPlayerPresenter.resumeVideo();
    }

    public void pause() {
        mPlayerPresenter.pauseVideo();
    }

    public void stop() {
        mPlayerPresenter.stopVideo();
    }

    public void release() {
        mPlayerPresenter.destroy();
    }

    public static class LoadLibraryException extends RuntimeException {
        public LoadLibraryException() {
        }

        public LoadLibraryException(String detailMessage) {
            super(detailMessage);
        }
    }

    @MainThread
    public interface IOuterCallBack {

        void onBufferingStart();

        void onBufferingEnd();

        void onError(int errCode);
    }
}
