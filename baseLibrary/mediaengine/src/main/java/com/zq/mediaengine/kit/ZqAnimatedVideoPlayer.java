package com.zq.mediaengine.kit;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.zq.mediaengine.filter.imgtex.ImgTexScaleFilter;
import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.kit.filter.ImgTexAlphaFrameFilter;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;

import java.io.IOException;

/**
 * 视频格式动画播放器，支持左右分区，左半透明度的动画视频
 */
public class ZqAnimatedVideoPlayer implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "ZqAnimatedVideoPlayer";
    private final static boolean VERBOSE = true;

    private GLRender mGLRender;
    private MediaPlayer mMediaPlayer;
    private boolean mEnableLoop;
    private volatile boolean mStopped = true;

    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private ImgTexFormat mImgTexFormat;
    private SrcPin<ImgTexFrame> mImgTexSrcPin;
    private ImgTexAlphaFrameFilter mImgTexAlphaFrameFilter;
    private ImgTexScaleFilter mImgTexScaleFilter;

    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;


    public interface OnCompletionListener {
        void onCompletion(ZqAnimatedVideoPlayer player);
    }

    public interface OnErrorListener {
        boolean onError(MediaPlayer mp, int what, int extra);
    }

    public ZqAnimatedVideoPlayer() {
        mGLRender = new GLRender();
        mMediaPlayer = new MediaPlayer();
        mImgTexSrcPin = new SrcPin<>();
        mImgTexAlphaFrameFilter = new ImgTexAlphaFrameFilter(mGLRender);
        mImgTexScaleFilter = new ImgTexScaleFilter(mGLRender);

        mGLRender.addListener(mOnGLReadyListener);
        mGLRender.addListener(mOnGLSizeChangedListener);
        mGLRender.addListener(mOnGLReleasedListener);
        mMediaPlayer.setOnPreparedListener(mOnMediaPlayerPreparedListener);
        mMediaPlayer.setOnCompletionListener(mOnMediaPlayerCompletionListener);
        mMediaPlayer.setOnSeekCompleteListener(mOnMediaPlayerSeekCompleteListener);
        mMediaPlayer.setOnErrorListener(mOnMediaPlayerErrorListener);

        mImgTexSrcPin.connect(mImgTexAlphaFrameFilter.getSinkPin());
        mImgTexAlphaFrameFilter.getSrcPin().connect(mImgTexScaleFilter.getSinkPin());
        mImgTexScaleFilter.setScalingMode(ImgTexScaleFilter.SCALING_MODE_BEST_FIT);
        mImgTexScaleFilter.setIsRender(true);
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        mOnCompletionListener = onCompletionListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener){
        mOnErrorListener = onErrorListener;
    }

    public void setDisplay(TextureView textureView) {
        mGLRender.init(textureView);
    }

    public void setEnableLoop(boolean enableLoop) {
        mEnableLoop = enableLoop;
    }

    public void start(AssetFileDescriptor afd) {
        Log.d(TAG, "Try to open afd " + afd);
        if (afd == null) {
            return;
        }

        try {
            mMediaPlayer.reset();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaPlayer.setDataSource(afd);
            } else {
                mMediaPlayer.setDataSource(afd.getFileDescriptor());
            }
            mMediaPlayer.prepareAsync();
            mStopped = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(String url) {
        Log.d(TAG, "Try to open url " + url);
        if (TextUtils.isEmpty(url)) {
            return;
        }

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
            mStopped = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        mStopped = true;
        mMediaPlayer.setSurface(null);
        mMediaPlayer.release();
        mMediaPlayer = null;
        mGLRender.release();
    }

    private MediaPlayer.OnPreparedListener mOnMediaPlayerPreparedListener = mp -> {
        if (mSurface != null) {
            mMediaPlayer.setSurface(mSurface);
        }

        // trig onFormatChanged event
        int w = mMediaPlayer.getVideoWidth();
        int h = mMediaPlayer.getVideoHeight();
        Log.d(TAG, "video prepared, " + w + "x" + h);
        if (mSurfaceTexture != null) {
            mSurfaceTexture.setDefaultBufferSize(w, h);
            mSurfaceTexture.setOnFrameAvailableListener(ZqAnimatedVideoPlayer.this);
        }
        mImgTexFormat = new ImgTexFormat(ImgTexFormat.COLOR_EXTERNAL_OES, w, h);
        mImgTexSrcPin.onFormatChanged(mImgTexFormat);

        mMediaPlayer.start();
    };

    private MediaPlayer.OnErrorListener mOnMediaPlayerErrorListener = (mp, what, extra) -> {
        Log.d(TAG, "mOnErrorListener: " + what + " " + extra);
        return mOnErrorListener != null && mOnErrorListener.onError(mp, what, extra);
    };

    private MediaPlayer.OnCompletionListener mOnMediaPlayerCompletionListener = mp -> {
        Log.d(TAG, "onMediaPlayerCompletion");
        if (mEnableLoop) {
            mMediaPlayer.seekTo(0);
        } else if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(ZqAnimatedVideoPlayer.this);
        }
    };

    private MediaPlayer.OnSeekCompleteListener mOnMediaPlayerSeekCompleteListener = mp -> {
        Log.d(TAG, "onMediaPlayerSeekCompletion");
        mMediaPlayer.start();
    };

    private GLRender.OnReadyListener mOnGLReadyListener = () -> {
        Log.d(TAG, "onGLContext ready");
        try {
            mTextureId = GlUtil.createOESTextureObject();
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            if (mSurface != null) {
                mSurface.release();
            }
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            mSurfaceTexture.setOnFrameAvailableListener(ZqAnimatedVideoPlayer.this);
            mSurface = new Surface(mSurfaceTexture);
            mMediaPlayer.setSurface(mSurface);
            if (mMediaPlayer.isPlaying()) {
                int w = mMediaPlayer.getVideoWidth();
                int h = mMediaPlayer.getVideoHeight();
                Log.d(TAG, "onReady " + w + "x" + h);
                mSurfaceTexture.setDefaultBufferSize(w, h);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private GLRender.OnSizeChangedListener mOnGLSizeChangedListener = (width, height) -> {
        Log.d(TAG, "onGLSizeChanged: " + width + "x" + height);
        mImgTexScaleFilter.setTargetSize(width, height);
    };

    private GLRender.OnReleasedListener mOnGLReleasedListener = () -> {
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(null);
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    };

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        final long pts = System.nanoTime() / 1000 / 1000;
        mGLRender.queueEvent(() -> {
            mSurfaceTexture.updateTexImage();
            if (mStopped) {
                return;
            }
            float[] texMatrix = new float[16];
            mSurfaceTexture.getTransformMatrix(texMatrix);
            ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, mTextureId, texMatrix, pts);
            try {
                mImgTexSrcPin.onFrameAvailable(frame);
                mGLRender.requestRender();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Draw player frame failed, ignore");
            }
        });
    }
}
