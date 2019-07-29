package com.zq.mediaengine.capture;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zq.mediaengine.filter.audio.AudioFilterMgt;
import com.zq.mediaengine.filter.audio.AudioSLPlayer;
import com.zq.mediaengine.filter.audio.AudioTrackPlayer;
import com.zq.mediaengine.filter.audio.IPcmPlayer;
import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.StcMgt;
import com.zq.mediaengine.util.audio.AudioUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Audio player capture.
 */
public class AudioPlayerCapture {
    private final static String TAG = "AudioPlayerCapture";
    private final static boolean VERBOSE = true;

    /**
     * The constant AUDIO_PLAYER_TYPE_AUDIOTRACK.
     */
    public final static int AUDIO_PLAYER_TYPE_AUDIOTRACK = 0;
    /**
     * The constant AUDIO_PLAYER_TYPE_OPENSLES.
     */
    public final static int AUDIO_PLAYER_TYPE_OPENSLES = 1;

    /**
     * The constant STATE_IDLE.
     */
    public static final int STATE_IDLE = AudioFileCapture.STATE_IDLE;
    /**
     * The constant STATE_PREPARING.
     */
    public static final int STATE_PREPARING = AudioFileCapture.STATE_PREPARING;
    /**
     * The constant STATE_STARTED.
     */
    public static final int STATE_STARTED = AudioFileCapture.STATE_STARTED;
    /**
     * The constant STATE_STOPPING.
     */
    public static final int STATE_STOPPING = AudioFileCapture.STATE_STOPPING;

    /**
     * The constant ERROR_UNKNOWN.
     */
    public static final int ERROR_UNKNOWN = AudioFileCapture.ERROR_UNKNOWN;
    /**
     * The constant ERROR_IO.
     */
    public static final int ERROR_IO = AudioFileCapture.ERROR_IO;
    /**
     * The constant ERROR_UNSUPPORTED.
     */
    public static final int ERROR_UNSUPPORTED = AudioFileCapture.ERROR_UNSUPPORTED;

    private SrcPin<AudioBufFrame> mSrcPin;
    private AudioFilterMgt mAudioFilterMgt;
    private AudioFileCapture mAudioFileCapture;

    private Context mContext;
    private IPcmPlayer mPcmPlayer;
    private StcMgt mStcMgt;
    private AudioBufFormat mOutFormat;
    // TODO: OpenSLES模式下，getPosition不准确，需要改进，这里暂时只能用AudioTrack
    private int mAudioPlayerType = AUDIO_PLAYER_TYPE_AUDIOTRACK;
    private boolean mPlayerTypeChanged = false;
    private boolean mMute = false;
    private boolean mMuteChanged = false;

    private Handler mMainHandler;
    private volatile boolean mLoop;

    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;

    /**
     * The interface on media prepared listener.
     */
    public interface OnPreparedListener {
        /**
         * On prepared.
         *
         * @param audioPlayerCapture the AudioPlayerCapture instance
         */
        void onPrepared(AudioPlayerCapture audioPlayerCapture);
    }

    /**
     * The interface On completion listener.
     */
    public interface OnCompletionListener {
        /**
         * On completion.
         *
         * @param audioPlayerCapture the AudioPlayerCapture instance
         */
        void onCompletion(AudioPlayerCapture audioPlayerCapture);
    }

    /**
     * The interface on audio play error listener.
     */
    public interface OnErrorListener {
        /**
         * On error.
         *
         * @param audioPlayerCapture the AudioPlayerCapture intance
         * @param type               the error type
         * @param msg                the extra msg
         */
        void onError(AudioPlayerCapture audioPlayerCapture, int type, long msg);
    }

    /**
     * Instantiates a new Audio player capture.
     *
     * @param context the context
     */
    public AudioPlayerCapture(Context context) {
        mContext = context;
        mSrcPin = new SrcPin<>();
        mStcMgt = new StcMgt();
        mMainHandler = new Handler(Looper.getMainLooper());
        mAudioFileCapture = new AudioFileCapture(context);
        setupListeners();

        mAudioFilterMgt = new AudioFilterMgt();
        SinkPin<AudioBufFrame> playerSinkPin = new SinkPin<AudioBufFrame>() {
            AudioBufFormat mPcmOutFormat = null;

            @Override
            public void onFormatChanged(Object format) {
                mOutFormat = (AudioBufFormat) format;

                // 动态切换OpenSL/AudioTrack
                if (mPcmPlayer != null) {
                    mPcmPlayer.stop();
                    mPcmPlayer.release();
                    mPcmPlayer = null;
                }

                // open pcm player
                if (mAudioPlayerType == AUDIO_PLAYER_TYPE_OPENSLES) {
                    mPcmPlayer = new AudioSLPlayer();
                } else {
                    mPcmPlayer = new AudioTrackPlayer();
                }
                int atomSize = AudioUtil.getNativeBufferSize(mContext, mOutFormat.sampleRate);
                mPcmPlayer.config(mOutFormat.sampleFormat, mOutFormat.sampleRate, mOutFormat.channels, atomSize, 40);
                mPcmPlayer.setMute(mMute);
                mPcmPlayer.start();

                mPcmOutFormat = new AudioBufFormat(mOutFormat);
                mPcmOutFormat.nativeModule = mPcmPlayer.getNativeInstance();
                mSrcPin.onFormatChanged(mPcmOutFormat);
            }

            @Override
            public void onFrameAvailable(AudioBufFrame frame) {
                handlePlayerTypeChanged();
                if (mMuteChanged) {
                    mMuteChanged = false;
                    mPcmPlayer.setMute(mMute);
                }
                if (frame.buf != null && frame.buf.limit() > 0) {
                    // write audio data in blocking mode
                    mPcmPlayer.write(frame.buf);

                    // ignore flag frames which should not be delivered
                    AudioBufFrame outFrame = new AudioBufFrame(frame);
                    outFrame.format = mPcmOutFormat;
                    mSrcPin.onFrameAvailable(outFrame);

                    // 更新时钟
                    long position = mAudioFileCapture.getBasePosition() + mPcmPlayer.getPosition();
                    mStcMgt.updateStc(position, true);

                    if (VERBOSE) {
                        long pos = mAudioFileCapture.getPosition();
                        Log.i(TAG, "pos: " + pos + " position: " + position);
                    }
                }
            }
        };
        mAudioFileCapture.getSrcPin().connect(mAudioFilterMgt.getSinkPin());
        mAudioFilterMgt.getSrcPin().connect(playerSinkPin);
    }

    private Runnable mCheckCompletionRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAudioFileCapture.getState() != AudioFileCapture.STATE_STARTED) {
                return;
            }
            long tm = mAudioFileCapture.getPosition() - mAudioFileCapture.getBasePosition();
            long pos = mPcmPlayer.getPosition();
            long delay = tm - pos;
            if (VERBOSE) {
                Log.d(TAG, "check completion: " + tm + " - " + pos + " = " + delay);
            }
            if (delay < 10) {
                if (mLoop) {
                    seek(0);
                } else {
                    postOnCompletion();
                }
            } else {
                mAudioFileCapture.getWorkHandler().postDelayed(mCheckCompletionRunnable, delay);
            }
        }
    };

    private void setupListeners() {
        // onPrepared
        mAudioFileCapture.setOnPreparedListener(new AudioFileCapture.OnPreparedListener() {
            @Override
            public void onPrepared(AudioFileCapture audioFileCapture) {
                if (mOnPreparedListener != null) {
                    mOnPreparedListener.onPrepared(AudioPlayerCapture.this);
                }
            }
        });
        // onCompletion
        mAudioFileCapture.setOnCompletionListener(new AudioFileCapture.OnCompletionListener() {
            @Override
            public void onCompletion(AudioFileCapture audioFileCapture) {
                mAudioFileCapture.getWorkHandler().post(mCheckCompletionRunnable);
            }
        });
        // onError
        mAudioFileCapture.setOnErrorListener(new AudioFileCapture.OnErrorListener() {
            @Override
            public void onError(AudioFileCapture audioFileCapture, int type, long msg) {
                if (mOnErrorListener != null) {
                    mOnErrorListener.onError(AudioPlayerCapture.this, type, msg);
                }
            }
        });
    }

    /**
     * Gets src pin.
     *
     * @return the src pin
     */
    public SrcPin<AudioBufFrame> getSrcPin() {
        return mSrcPin;
    }

    /**
     * Get current state.
     *
     * @return current state
     */
    public int getState() {
        return mAudioFileCapture.getState();
    }

    /**
     * Gets audio filter mgt.
     *
     * @return the audio filter mgt
     */
    public AudioFilterMgt getAudioFilterMgt() {
        return mAudioFilterMgt;
    }

    /**
     * Set audio player type.
     *
     * @param type type in AUDIO_PLAYER_TYPE_AUDIOTRACK or AUDIO_PLAYER_TYPE_OPENSLES.
     */
    public void setAudioPlayerType(int type) {
        mPlayerTypeChanged = (mAudioPlayerType != type);
        mAudioPlayerType = type;
    }

    /**
     * Sets mute.
     *
     * @param mute true to mute, false to unmute
     */
    public void setMute(boolean mute) {
        mMuteChanged = (mMute != mute);
        mMute = mute;
    }

    /**
     * Gets mute.
     *
     * @return the mute state
     */
    public boolean getMute() {
        return mMute;
    }

    /**
     * Sets volume.
     *
     * @param volume the volume, should be 0.0f-1.0f
     */
    public void setVolume(float volume) {
        mAudioFileCapture.setVolume(volume);
    }

    /**
     * Gets volume.
     *
     * @return the volume, should in 0.0f-1.0f
     */
    public float getVolume() {
        return mAudioFileCapture.getVolume();
    }

    /**
     * Sets on prepared listener.
     *
     * @param listener the listener
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    /**
     * Sets on completion listener.
     *
     * @param listener the listener
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    /**
     * Sets on error listener.
     *
     * @param listener the listener
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    /**
     * Start audio player.
     *
     * @param url the url.
     *            prefix "file://" for absolute path,
     *            and prefix "assets://" for resource in assets folder,
     *            also prefix "http://", "https://"  supported.
     */
    public void start(String url) {
        start(url, false);
    }

    /**
     * Start audio player.
     *
     * @param url  the url.
     *             prefix "file://" for absolute path,
     *             and prefix "assets://" for resource in assets folder,
     *             also prefix "http://", "https://"  supported.
     * @param loop set if in loop play mode
     */
    public void start(String url, boolean loop) {
        mLoop = loop;
        mAudioFileCapture.start(url);
    }

    /**
     * Stop.
     */
    public void stop() {
        mAudioFileCapture.stop(new Runnable() {
            @Override
            public void run() {
                mPcmPlayer.stop();
                mPcmPlayer.release();
                mPcmPlayer = null;
                mStcMgt.reset();
            }
        });
    }

    /**
     * Pause.
     */
    public void pause() {
        mAudioFileCapture.pause(new Runnable() {
            @Override
            public void run() {
                mPcmPlayer.pause();
                mStcMgt.pause();
            }
        });
    }

    /**
     * Resume.
     */
    public void resume() {
        mAudioFileCapture.resume(new Runnable() {
            @Override
            public void run() {
                mPcmPlayer.resume();
                mStcMgt.start();
            }
        });
    }

    /**
     * Seek.
     *
     * @param ms the time seek to, in miliseconds
     */
    public void seek(final long ms) {
        mAudioFileCapture.seek(ms, new Runnable() {
            @Override
            public void run() {
                mPcmPlayer.flush();
                mStcMgt.pause();
                mStcMgt.updateStc(ms);
            }
        });
    }

    /**
     * Release.
     */
    public void release() {
        stop();
        mAudioFileCapture.release();
    }

    /**
     * Gets duration.
     *
     * @return the duration in milisenconds
     */
    public long getDuration() {
        return mAudioFileCapture.getDuration();
    }

    /**
     * Gets position.
     *
     * @return current position in miliseconds
     */
    public long getPosition() {
        return mStcMgt.getCurrentStc();
    }

    private void postOnCompletion() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnCompletionListener != null) {
                    mOnCompletionListener.onCompletion(AudioPlayerCapture.this);
                }
            }
        });
    }

    private void postError(final int err, final long msg) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnErrorListener != null) {
                    mOnErrorListener.onError(AudioPlayerCapture.this, err, msg);
                }
            }
        });
    }

    private void handlePlayerTypeChanged() {
        if (!mPlayerTypeChanged) {
            return;
        }
        // TODO: 支持动态切换audiotrack和opensles播放
        mPlayerTypeChanged = false;
        if ((mAudioPlayerType == AUDIO_PLAYER_TYPE_OPENSLES &&
                mPcmPlayer instanceof AudioTrackPlayer) ||
                (mAudioPlayerType == AUDIO_PLAYER_TYPE_AUDIOTRACK &&
                        mPcmPlayer instanceof AudioSLPlayer)) {
            if (mOutFormat != null) {
                // send detach event
                AudioBufFrame aFrame = new AudioBufFrame(mOutFormat, null, 0);
                aFrame.flags |= AVConst.FLAG_DETACH_NATIVE_MODULE;
                mSrcPin.onFrameAvailable(aFrame);
                mAudioFilterMgt.getSinkPin().onFormatChanged(mOutFormat);
            }
        }
    }
}
