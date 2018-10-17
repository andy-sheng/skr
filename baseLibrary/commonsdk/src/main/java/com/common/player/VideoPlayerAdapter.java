package com.common.player;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.common.log.MyLog;
import com.common.player.exoplayer.ExoPlayer;
import com.common.utils.U;

/**
 * 职责：
 * 对各种播放器进行一个适配，在内部可以方便切换播放器
 * 并对 textureview 与 player 做关联
 * 不负责 加载 loading 视图 seekbar 等集成
 * 具体业务可基于此在编写具体的 播放器控件(比如带 loading view  seekbar等)
 */
public class VideoPlayerAdapter {

    public final static String TAG = "VideoPlayerAdapter";

    public static final int PLAY_TYPE_EXO = 1;

    private int mPlayerType = PLAY_TYPE_EXO;

    private IPlayer mPlayer;

    private TextureView mTextureView;

    private Surface mSurface;

    private String mVideoPath;

    private int mVideoWidth = 0, mVideoHeight = 0;
    private int mSurfaceWidth = 0, mSurfaceHeight = 0;

    TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mSurface == null) {
                mSurface = new Surface(surface);
            }
            onSurfaceTextureSizeChanged(surface, width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            MyLog.w(TAG, "onSurfaceTextureSizeChanged");
            if (mSurfaceWidth != width || mSurfaceHeight != height) {
                MyLog.w(TAG, "onSurfaceTextureSizeChanged width=" + width + ", height=" + height);
                mSurfaceWidth = width;
                mSurfaceHeight = height;
                mPlayer.setSurface(mSurface);
                updateGravity();
                updateShiftUp();
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
    };

    public VideoPlayerAdapter() {

    }

    public void setTextureView(TextureView textureView) {
        if (textureView != null && textureView != mTextureView) {
            mTextureView = textureView;
            textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void initPlayer() {
        if (mPlayer == null) {
            if (mPlayerType == PLAY_TYPE_EXO) {
                genExoplayer();
            }
        } else {
            if (mPlayerType == PLAY_TYPE_EXO) {
                if (mPlayer instanceof ExoPlayer) {
                    MyLog.d(TAG, "initPlayer mPlayer not null, right type");
                } else {
                    // 先销毁老的播放器
                    destroyPlayer();
                    genExoplayer();
                }
            }
        }
    }

    private void genExoplayer() {
        mPlayer = new ExoPlayer();
        mPlayer.setCallback(new IPlayerCallback() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onCompletion() {

            }

            @Override
            public void onSeekComplete() {

            }

            @Override
            public void onVideoSizeChanged(int width, int height) {

            }

            @Override
            public void onError(int what, int extra) {

            }

            @Override
            public void onInfo(int what, int extra) {

            }
        });
    }

    public void setPlayerType(int playerType) {
        if (playerType != mPlayerType) {
            this.mPlayerType = playerType;
            // 播放器类型发生变化
            if (mPlayer != null) {
                // 播放器已经初始化了
                initPlayer();
            }
        }
    }

    private void updateGravity() {
        if (mPlayer != null && mSurfaceWidth != 0 && mSurfaceHeight != 0) {
            if (U.getVideoUtils().isNeedFill(mSurfaceWidth, mSurfaceHeight, mVideoWidth, mVideoHeight)) {
                mPlayer.setGravity(mTextureView, IPlayer.GRAVITY_FIT_WITH_CROPPING, mSurfaceWidth, mSurfaceHeight);
            } else {
                mPlayer.setGravity(mTextureView, IPlayer.GRAVITY_FIT, mSurfaceWidth, mSurfaceHeight);
            }
        } else {
            if (mPlayer != null) {
                mPlayer.setGravity(mTextureView, IPlayer.GRAVITY_FIT,
                        mSurfaceWidth, mSurfaceHeight);
            }
        }
    }

    protected void updateShiftUp() {
        if (mPlayer == null) {
            return;
        }
        if (mSurfaceWidth == 0 || mSurfaceHeight == 0) {
            mPlayer.shiftUp(0, 0, 0, 0, 0);
        } else if (mVideoWidth == 0 || mVideoHeight == 0) {
            mPlayer.shiftUp(0, 0, 0, 0, 0);
        } else if (mVideoWidth * 16 > mVideoHeight * 9) {
            if (mTextureView instanceof View) {
                View view = (View) mTextureView;
                View parent = (View) view.getParent();
                if (parent.getHeight() != 0) {
                    float ratio = (parent.getHeight() - parent.getWidth() * 9 / 16) * 0.25f / parent.getHeight();
                    mPlayer.shiftUp(ratio, 0, 0, 0, 0);
                }
            } else {
                float ratio = (mSurfaceHeight - mSurfaceWidth * 9 / 16) * 0.25f / mSurfaceHeight;
                mPlayer.shiftUp(ratio, 0, 0, 0, 0);
            }
        } else {
            mPlayer.shiftUp(0, 0, 0, 0, 0);
        }
    }

    /**
     * 设定播放路径
     *
     * @param videoPath
     */
    public void setVideoPath(String videoPath) {
        this.mVideoPath = videoPath;
        this.mVideoPath = "http://playback.ks.zb.mi.com/record/live/101743_1531094545/hls/101743_1531094545.m3u8?playui=1";
    }

    /**
     * 开始播放
     * 跟resume 一个效果
     */
    public void play() {
        initPlayer();
        mPlayer.setVideoPath(mVideoPath);
        mPlayer.start();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    /**
     * 停止播放，不记录位置
     */
    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    public void destroy() {
        destroyPlayer();
        if (mSurface != null) {
            mSurfaceWidth = mSurfaceHeight = 0;
            mSurface.release();
            mSurface = null;
        }
    }

    /**
     * 只销毁播放器
     */
    private void destroyPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * @return 是否在播放
     */
    public boolean isPlaying() {
        if (mPlayer == null) {
            return false;
        }
        return mPlayer.isPlaying();
    }

    /**
     * @return 总的时间
     */
    public long getDuration() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getDuration();
    }

    /**
     * @return 当前播放的时间戳
     */
    public long getCurrentPosition() {
        if (mPlayer == null) {
            return 0;
        }
        return mPlayer.getCurrentPosition();
    }

}
