package com.common.player;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.common.log.MyLog;

/**
 * 职责：
 * 对各种播放器进行一个适配，在内部可以方便切换播放器
 * 并对 textureview 与 player 做关联
 * 不负责 加载 loading 视图 seekbar 等集成
 * 具体业务可基于此再编写具体的 播放器控件(比如带 loading view  seekbar等)
 * <p>
 * 同时支持 SurfaceView 和 TextureView
 * <p>
 * <p>
 * {@code
 * 基础代码使用样例
 * // mSurfaceView 要包裹一个父布局
 * mVideoPlayerAdapter.setSurfaceView(mSurfaceView); // 或者 mVideoPlayerAdapter.setTextureView(mTexture);
 * mVideoPlayerAdapter.setVideoPath("http://playback.ks.zb.mi.com/record/live/101743_1531094545/hls/101743_1531094545.m3u8?playui=1");
 * mVideoPlayerAdapter.play();
 * }
 */
public class VideoPlayerAdapter {

    public final static String TAG = "VideoPlayerAdapter";

    public static final int PLAY_TYPE_EXO = 1;

    private int mPlayerType = PLAY_TYPE_EXO;

    private boolean mLoop = false;

    private static IPlayer sPreStartPlayer;

    private IPlayer mPlayer;

    private PlayerCallbackAdapter mOutPlayerCallback;

    private TextureView mTextureView;

    private SurfaceView mSurfaceView;

    private Surface mSurface;

    private String mVideoPath;

    /**
     * 这是视频本身的宽高
     */
    private int mVideoWidth = 0, mVideoHeight = 0;

    /**
     * 这是 surfaceview  view 的宽高
     */
    private int mSurfaceWidth = 0, mSurfaceHeight = 0;

    /**
     * 一定要在activity 中开启硬件啊加速，不然 onSurfaceTextureAvailable 不执行
     */
    TextureView.SurfaceTextureListener mTextureViewListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            MyLog.d(TAG, "onSurfaceTextureAvailable" + " surface=" + surface + " width=" + width + " height=" + height);
            if (mSurface == null) {
                mSurface = new Surface(surface);
            }
            onSurfaceTextureSizeChanged(surface, width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            MyLog.d(TAG, "onSurfaceTextureSizeChanged" + " surface=" + surface + " width=" + width + " height=" + height);
            onSurfceChange(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            MyLog.w(TAG, "onSurfaceTextureDestroyed");
            destroySurface();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    private SurfaceHolder.Callback mSurfaceViewListener = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            MyLog.d(TAG, "surfaceCreated" + " holder=" + holder);
            if (mSurface == null) {
                mSurface = holder.getSurface();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            MyLog.w(TAG, "onSurfaceTextureSizeChanged width=" + width + ", height=" + height);
            onSurfceChange(width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            MyLog.d(TAG, "surfaceDestroyed" + " holder=" + holder);
            destroySurface();
        }
    };

    public VideoPlayerAdapter() {

    }

    private void onSurfceChange(int width, int height) {
        if (mSurfaceWidth != width || mSurfaceHeight != height) {
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            initPlayerIfNeed();
            mPlayer.setSurface(mSurface);
            updateGravity();
            updateShiftUp();
        }
    }

    private void onVideoChange(int videoWidth, int videoHeight) {
        if (mVideoWidth != videoWidth || mVideoHeight != videoHeight) {
            mVideoWidth = videoWidth;
            mVideoHeight = videoHeight;
            updateGravity();
            updateShiftUp();
        }
    }

    /**
     * setSurfaceView setTextureView 只能执行一个
     *
     * @param textureView
     */
    public void setTextureView(TextureView textureView) {
        if (mSurfaceView != null) {
            throw new IllegalStateException("mTextureView mSurfaceView can only set one ");
        }
        if (textureView != null && textureView != mTextureView) {
            mTextureView = textureView;
            mTextureView.setSurfaceTextureListener(mTextureViewListener);
        }
    }

    /**
     * setSurfaceView setTextureView 只能执行一个
     *
     * @param surfaceView
     */
    public void setSurfaceView(SurfaceView surfaceView) {
        if (mTextureView != null) {
            throw new IllegalStateException("mTextureView mSurfaceView can only set one ");
        }
        if (surfaceView != null && surfaceView != mSurfaceView) {
            mSurfaceView = surfaceView;
            mSurfaceView.getHolder().addCallback(mSurfaceViewListener);
        }
    }

    private void initPlayerIfNeed() {
        if (mPlayer == null) {
            if (mPlayerType == PLAY_TYPE_EXO) {
                if (sPreStartPlayer != null && sPreStartPlayer instanceof ExoPlayer) {
                    mPlayer = sPreStartPlayer;
                    mPlayer.setCallback(mIPlayerCallback);
                    sPreStartPlayer = null;
                    return;
                }
                mPlayer = new ExoPlayer();
                mPlayer.setCallback(mIPlayerCallback);
            }
        } else {
            if (mPlayerType == PLAY_TYPE_EXO) {
                if (mPlayer instanceof ExoPlayer) {
                    MyLog.d(TAG, "initPlayer mPlayer not null, right type");
                } else {
                    // 先销毁老的播放器
                    destroyPlayer();
                    mPlayer = new ExoPlayer();
                    mPlayer.setCallback(mIPlayerCallback);
                }
            }
        }
    }

    private IPlayerCallback mIPlayerCallback = new IPlayerCallback() {
        /**
         * 如果是prestart的话 播放器中会缓存这个标记，等到 setCallback 时会调用
         */
        @Override
        public void onPrepared() {
            MyLog.d(TAG, "onPrepared");
            if (mOutPlayerCallback != null) {
                mOutPlayerCallback.onPrepared();
            }
        }

        @Override
        public void onCompletion() {
            MyLog.d(TAG, "onCompletion");
            if (mLoop) {
                mPlayer.seekTo(0);
                play();
            }
            if (mOutPlayerCallback != null) {
                mOutPlayerCallback.onCompletion();
            }
        }

        @Override
        public void onSeekComplete() {
            MyLog.d(TAG, "onSeekComplete");
            if (mOutPlayerCallback != null) {
                mOutPlayerCallback.onSeekComplete();
            }
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            MyLog.d(TAG, "onVideoSizeChanged" + " width=" + width + " height=" + height);
            onVideoChange(width, height);
            if (mOutPlayerCallback != null) {
                mOutPlayerCallback.onVideoSizeChanged(width, height);
            }
        }

        @Override
        public void onError(int what, int extra) {
            MyLog.d(TAG, "onError" + " what=" + what + " extra=" + extra);
            if (mOutPlayerCallback != null) {
                mOutPlayerCallback.onError(what, extra);
            }
        }

        @Override
        public void onInfo(int what, int extra) {
            MyLog.d(TAG, "onInfo" + " what=" + what + " extra=" + extra);
            if (mOutPlayerCallback != null) {
                mOutPlayerCallback.onInfo(what, extra);
            }
        }

    };

    /**
     * 播放器类型
     *
     * @param playerType
     */
    public void setPlayerType(int playerType) {
        if (playerType != mPlayerType) {
            this.mPlayerType = playerType;
            // 播放器类型发生变化
            if (mPlayer != null) {
                // 播放器已经初始化了
                initPlayerIfNeed();
            }
        }
    }

    /**
     * 是否需要循环播放
     *
     * @param loop
     */
    public void setLoop(boolean loop) {
        this.mLoop = loop;
    }

    /**
     * 获取视频view
     *
     * @return
     */
    private View getVideoView() {
        if (mTextureView != null) {
            return mTextureView;
        }
        return mSurfaceView;
    }

    private void updateGravity() {
        if (mPlayer != null) {
            mPlayer.setGravity(getVideoView(), IPlayer.GRAVITY_FIT,
                    mSurfaceWidth, mSurfaceHeight);
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
            View videoView = getVideoView();
            if (videoView instanceof View) {
                View parent = (View) videoView.getParent();
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

    public void setOutCallback(PlayerCallbackAdapter outPlayerCallback) {
        this.mOutPlayerCallback = outPlayerCallback;
    }

    /**
     * 设定播放路径
     *
     * @param videoPath
     */
    public void setVideoPath(String videoPath) {
        this.mVideoPath = videoPath;
    }

    /**
     * 开始播放
     * 跟resume 一个效果
     */
    public void play() {
        initPlayerIfNeed();
        mPlayer.startPlay(mVideoPath);
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

    /**
     * 销毁方法，退出时一定要调用
     */
    public void destroy() {
        destroyPlayer();
        destroySurface();
        mTextureView = null;
        mSurfaceView = null;
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
     * 只销毁播放器
     */
    private void destroySurface() {
        if (mSurface != null) {
            mSurfaceWidth = mSurfaceHeight = 0;
            mSurface.release();
            mSurface = null;
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

    public void seekTo(long mSec) {
        if (mPlayer == null) {
            return;
        }
        mPlayer.seekTo(mSec);
    }

    public static void preStartPlayer(String url) {
        preStartPlayer(url, PLAY_TYPE_EXO);
    }

    /**
     * 提前加载视频url
     * 为了提高视频秒开体验
     * 一般在启动某个观看Activity前先load url
     * 等到activity启动后再将 player 和 view 绑定
     *
     * @param url
     * @param playerType
     */
    public static void preStartPlayer(String url, int playerType) {
        MyLog.d(TAG, "preStartPlayer" + " url=" + url + " playerType=" + playerType);
        if (sPreStartPlayer == null) {
            if (playerType == PLAY_TYPE_EXO) {
                sPreStartPlayer = new ExoPlayer();
            }
        }
        sPreStartPlayer.startPlay(url);
    }


    public static class PlayerCallbackAdapter implements IPlayerCallback {

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

    }
}
