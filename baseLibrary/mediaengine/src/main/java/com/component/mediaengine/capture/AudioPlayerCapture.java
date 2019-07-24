package com.component.mediaengine.capture;

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

import com.component.mediaengine.filter.audio.AudioFilterMgt;
import com.component.mediaengine.filter.audio.AudioSLPlayer;
import com.component.mediaengine.filter.audio.AudioTrackPlayer;
import com.component.mediaengine.filter.audio.IPcmPlayer;
import com.component.mediaengine.framework.AVConst;
import com.component.mediaengine.framework.AudioBufFormat;
import com.component.mediaengine.framework.AudioBufFrame;
import com.component.mediaengine.framework.SinkPin;
import com.component.mediaengine.framework.SrcPin;
import com.component.mediaengine.util.StcMgt;
import com.component.mediaengine.util.audio.AudioUtil;

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

    private static final long TIMEOUT_US = 10000;
    private static final int MAX_EOS_SPINS = 10;
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
    public static final int STATE_IDLE = 0;
    /**
     * The constant STATE_PREPARING.
     */
    public static final int STATE_PREPARING = 1;
    /**
     * The constant STATE_STARTED.
     */
    public static final int STATE_STARTED = 2;
    /**
     * The constant STATE_STOPPING.
     */
    public static final int STATE_STOPPING = 3;

    /**
     * The constant ERROR_UNKNOWN.
     */
    public static final int ERROR_UNKNOWN = -1;
    /**
     * The constant ERROR_IO.
     */
    public static final int ERROR_IO = -2;
    /**
     * The constant ERROR_UNSUPPORTED.
     */
    public static final int ERROR_UNSUPPORTED = -3;

    private final static int CMD_START = 1;
    private final static int CMD_STOP = 2;
    private final static int CMD_SEEK = 3;
    private final static int CMD_RELEASE = 4;
    private final static int CMD_LOOP = 5;
    private final static int CMD_PAUSE = 6;
    private final static int CMD_WAIT = 7;

    private SrcPin<AudioBufFrame> mSrcPin;
    private AudioFilterMgt mAudioFilterMgt;

    private Context mContext;
    private IPcmPlayer mPcmPlayer;
    private StcMgt mStcMgt;
    private AudioBufFormat mOutFormat;
    private ByteBuffer mOutBuffer;
    private int mAudioPlayerType = AUDIO_PLAYER_TYPE_AUDIOTRACK;
    private boolean mPlayerTypeChanged = false;
    private boolean mMute = false;
    private float mVolume = 1.0f;
    private String mUrl;

    private MediaExtractor mMediaExtractor;
    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo mBufferInfo;
    private int mEosSpinCount = 0;
    private HandlerThread mDecodeThread;
    private Handler mDecodeHandler;
    private Handler mMainHandler;
    private volatile int mState;
    private volatile boolean mPaused;
    private volatile boolean mLoop;
    private volatile boolean mIsSeeking;
    private long mFirstPts;
    private long mDuration;
    private long mBasePosition;
    private long mSamplesWritten;

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
        mState = STATE_IDLE;
        mContext = context;
        mSrcPin = new SrcPin<>();
        mMainHandler = new Handler(Looper.getMainLooper());
        mStcMgt = new StcMgt();
        initDecodeThread();

        mAudioFilterMgt = new AudioFilterMgt();
        SinkPin<AudioBufFrame> playerSinkPin = new SinkPin<AudioBufFrame>() {
            AudioBufFormat mPcmOutFormat = null;

            @Override
            public void onFormatChanged(Object format) {
                AudioBufFormat inFormat = (AudioBufFormat) format;

                // open pcm player
                if (mAudioPlayerType == AUDIO_PLAYER_TYPE_OPENSLES) {
                    mPcmPlayer = new AudioSLPlayer();
                } else {
                    mPcmPlayer = new AudioTrackPlayer();
                }
                int atomSize = AudioUtil.getNativeBufferSize(mContext, inFormat.sampleRate);
                mPcmPlayer.config(inFormat.sampleFormat, inFormat.sampleRate, inFormat.channels, atomSize, 40);
                mPcmPlayer.setMute(mMute);
                mPcmPlayer.start();

                mPcmOutFormat = new AudioBufFormat(inFormat);
                mPcmOutFormat.nativeModule = mPcmPlayer.getNativeInstance();
                mSrcPin.onFormatChanged(mPcmOutFormat);
            }

            @Override
            public void onFrameAvailable(AudioBufFrame frame) {
                if (frame.buf != null && frame.buf.limit() > 0) {
                    // write audio data in blocking mode
                    mPcmPlayer.write(frame.buf);

                    // ignore flag frames which should not be delivered
                    AudioBufFrame outFrame = new AudioBufFrame(frame);
                    outFrame.format = mPcmOutFormat;
                    mSrcPin.onFrameAvailable(outFrame);
                }
            }
        };
        mAudioFilterMgt.getSrcPin().connect(playerSinkPin);
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
        mVolume = volume;
    }

    /**
     * Gets volume.
     *
     * @return the volume, should in 0.0f-1.0f
     */
    public float getVolume() {
        return mVolume;
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
        mUrl = url;
        mLoop = loop;
        mDecodeHandler.sendEmptyMessage(CMD_START);
    }

    /**
     * Stop.
     */
    public void stop() {
        mDecodeHandler.sendEmptyMessage(CMD_STOP);
    }

    /**
     * Pause.
     */
    public void pause() {
        Message msg = mDecodeHandler.obtainMessage(CMD_PAUSE, 1, 0);
        mDecodeHandler.sendMessage(msg);
    }

    /**
     * Resume.
     */
    public void resume() {
        Message msg = mDecodeHandler.obtainMessage(CMD_PAUSE, 0, 0);
        mDecodeHandler.sendMessage(msg);
    }

    /**
     * Seek.
     *
     * @param ms the time seek to, in miliseconds
     */
    public void seek(long ms) {
        Message msg = mDecodeHandler.obtainMessage(CMD_SEEK, (int) ms, 0);
        mDecodeHandler.sendMessage(msg);
    }

    /**
     * Release.
     */
    public void release() {
        stop();
        mDecodeHandler.sendEmptyMessage(CMD_RELEASE);
        mAudioFilterMgt.getSinkPin().onDisconnect(true);
    }

    /**
     * Gets duration.
     *
     * @return the duration in milisenconds
     */
    public long getDuration() {
        return mDuration;
    }

    /**
     * Gets position.
     *
     * @return current position in miliseconds
     */
    public long getPosition() {
        return mStcMgt.getCurrentStc();
    }

    private void initDecodeThread() {
        mDecodeThread = new HandlerThread("AudioDecode");
        mDecodeThread.start();
        mDecodeHandler = new Handler(mDecodeThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                int err;
                switch (msg.what) {
                    case CMD_START:
                        if (mState != STATE_IDLE) {
                            break;
                        }
                        mState = STATE_PREPARING;
                        err = doStart();
                        if (err != 0) {
                            mState = STATE_IDLE;
                            postError(err, 0);
                        } else {
                            mState = STATE_STARTED;
                            postOnPrepared();
                            mDecodeHandler.sendEmptyMessage(CMD_LOOP);
                        }
                        break;
                    case CMD_LOOP:
                        if (mState != STATE_STARTED) {
                            break;
                        }
                        mPcmPlayer.setMute(mMute);
                        if (!doLoop()) {
                            if (!mPaused) {
                                mDecodeHandler.sendEmptyMessage(CMD_LOOP);
                            }
                        } else {
                            mDecodeHandler.sendEmptyMessage(CMD_WAIT);
                        }
                        break;
                    case CMD_PAUSE:
                        if (mState != STATE_STARTED) {
                            break;
                        }
                        if (msg.arg1 != 0 && !mPaused) {
                            mPaused = true;
                            mPcmPlayer.pause();
                            mStcMgt.pause();
                        } else if (msg.arg1 == 0 && mPaused) {
                            mPaused = false;
                            mPcmPlayer.resume();
                            mStcMgt.start();
                            mDecodeHandler.sendEmptyMessage(CMD_LOOP);
                        }
                        break;
                    case CMD_WAIT:
                        if (mState != STATE_STARTED) {
                            break;
                        }
                        long delay = doWait();
                        if (delay < 10) {
                            if (mLoop) {
                                doRestart();
                                mDecodeHandler.sendEmptyMessage(CMD_LOOP);
                            } else {
                                postOnCompletion();
                            }
                        } else {
                            mDecodeHandler.sendEmptyMessageDelayed(CMD_WAIT, delay);
                        }
                        break;
                    case CMD_STOP:
                        if (mState != STATE_STARTED) {
                            break;
                        }
                        mState = STATE_STOPPING;
                        doStop();
                        mState = STATE_IDLE;
                        break;
                    case CMD_SEEK:
                        if (mState != STATE_STARTED) {
                            break;
                        }
                        doSeek(msg.arg1);
                        mDecodeHandler.sendEmptyMessage(CMD_LOOP);
                        break;
                    case CMD_RELEASE:
                        mDecodeThread.quit();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void postOnPrepared() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnPreparedListener != null) {
                    mOnPreparedListener.onPrepared(AudioPlayerCapture.this);
                }
            }
        });
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

    private void setDataSource(String url) throws IOException {
        final String filePrefix = "file://";
        final String assetsPrefix = "assets://";

        if (url == null || url.isEmpty()) {
            throw new IOException("url is empty!");
        }
        Log.d(TAG, "Try to open url " + mUrl);
        if (url.startsWith(filePrefix)) {
            String path = url.substring(filePrefix.length());
            mMediaExtractor.setDataSource(path);
        } else if (url.startsWith(assetsPrefix)) {
            String path = url.substring(assetsPrefix.length());
            final AssetFileDescriptor afd = mContext.getAssets().openFd(path);
            mMediaExtractor.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
        } else {
            mMediaExtractor.setDataSource(mUrl);
        }
    }

    private int doStart() {
        mFirstPts = Long.MIN_VALUE;
        mIsSeeking = false;
        mDuration = 0;
        mBasePosition = 0;
        mSamplesWritten = 0;
        mStcMgt.reset();
        mMediaExtractor = new MediaExtractor();
        try {
            setDataSource(mUrl);
        } catch (IOException e) {
            Log.e(TAG, "Open " + mUrl + " failed");
            e.printStackTrace();
            return ERROR_IO;
        }

        int numTracks = mMediaExtractor.getTrackCount();
        for (int i = 0; i< numTracks; i++) {
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                mMediaExtractor.selectTrack(i);
                mDuration = mediaFormat.getLong(MediaFormat.KEY_DURATION);

                // audio format
                int sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                mOutFormat = new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16, sampleRate, channels);

                // open decoder
                try {
                    mMediaCodec = MediaCodec.createDecoderByType(mime);
                    mMediaCodec.configure(mediaFormat, null, null, 0);
                } catch (Exception e) {
                    Log.e(TAG, "init decoder " + mime + " failed!");
                    e.printStackTrace();
                    return ERROR_UNSUPPORTED;
                }
                mMediaCodec.start();
                mBufferInfo = new MediaCodec.BufferInfo();

                // trigger format changed
                mAudioFilterMgt.getSinkPin().onFormatChanged(mOutFormat);
                return 0;
            }
        }

        return ERROR_UNSUPPORTED;
    }

    private void doStop() {
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaExtractor.release();
        mPcmPlayer.stop();
        mPcmPlayer.release();
    }

    private boolean doLoop() {
        boolean eos = fillDecoder();
        drainDecoder(eos);
        return eos;
    }

    private long doWait() {
        long dur = mSamplesWritten * 1000 / mOutFormat.sampleRate - mPcmPlayer.getPosition();
        Log.d(TAG, "do Wait " + dur + "ms");
        return dur;
    }

    private void doRestart() {
        mMediaCodec.flush();
        mMediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        mPcmPlayer.stop();
        mPcmPlayer.start();
        mBasePosition = 0;
        mSamplesWritten = 0;
    }

    private void doSeek(long ms) {
        mMediaCodec.flush();
        mPcmPlayer.flush();
        mSamplesWritten = 0;
        mIsSeeking = true;
        mStcMgt.pause();
        mStcMgt.updateStc(ms);
        mMediaExtractor.seekTo(ms * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
    }

    private boolean fillDecoder() {
        boolean eos = false;
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_US);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            int sampleSize = mMediaExtractor.readSampleData(inputBuffer, 0);
            if (sampleSize >= 0) {
                long pts = mMediaExtractor.getSampleTime();
                if (mFirstPts == Long.MIN_VALUE) {
                    mFirstPts = pts;
                }
                if (mIsSeeking) {
                    mBasePosition = (pts - mFirstPts) / 1000;
                    mIsSeeking = false;
                }
                if (VERBOSE) Log.d(TAG, "fill decoder " + sampleSize + " pts: " + pts / 1000);
                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, pts, 0);
                mMediaExtractor.advance();
            } else {
                Log.d(TAG, "EOS got, flush decoder");
                eos = true;
                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }
        }
        return eos;
    }

    private short clip_short(int val) {
        if (((val + 0x8000) & ~0xFFFF) != 0) {
            return (short) ((val >> 31) ^ 0x7FFF);
        } else {
            return (short) val;
        }
    }

    private ByteBuffer applyVolume(ByteBuffer inBuffer) {
        if (mOutBuffer != null && mOutBuffer.capacity() < inBuffer.limit()) {
            mOutBuffer = null;
        }
        if (mOutBuffer == null) {
            int size = 8 * 1024;
            while (size < inBuffer.limit()) {
                size *= 2;
            }
            mOutBuffer = ByteBuffer.allocateDirect(size);
            mOutBuffer.order(ByteOrder.nativeOrder());
        }
        mOutBuffer.clear();
        mOutBuffer.put(inBuffer);
        mOutBuffer.flip();
        inBuffer.rewind();
        if (mVolume != 1.0f) {
            ShortBuffer shortBuffer = mOutBuffer.asShortBuffer();
            int shortSize = mOutBuffer.limit() / 2;
            for (int i = 0; i < shortSize; i++) {
                shortBuffer.put(i, clip_short((int) (shortBuffer.get(i) * mVolume)));
            }
        }
        return mOutBuffer;
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
            }

            IPcmPlayer player = mPcmPlayer;
            mPcmPlayer = null;
            player.stop();
            player.release();
            if (mAudioPlayerType == AUDIO_PLAYER_TYPE_OPENSLES) {
                mPcmPlayer = new AudioSLPlayer();
            } else {
                mPcmPlayer = new AudioTrackPlayer();
            }
            mPcmPlayer.setMute(mMute);
            int atomSize = AudioUtil.getNativeBufferSize(mContext, mOutFormat.sampleRate);
            mPcmPlayer.config(mOutFormat.sampleFormat, mOutFormat.sampleRate, mOutFormat.channels, atomSize, 40);
            mPcmPlayer.start();

            mAudioFilterMgt.getSinkPin().onFormatChanged(mOutFormat);
        }
    }

    private void drainDecoder(boolean eos) {
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
        mEosSpinCount = 0;
        long timeoutUs = TIMEOUT_US;
        while (true) {
            int outputBufferIndex;
            try {
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, timeoutUs);
            } catch (Exception e) {
                // Exynos socs may report invalid state exception even on a valid state,
                // when no input frame filled but eos signaled.
                Log.e(TAG, "dequeueOutputBuffer failed");
                break;
            }
            if (outputBufferIndex >= 0) {
                if (VERBOSE) Log.d(TAG, "drain decoder " + mBufferInfo.size);

                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                outputBuffer.position(mBufferInfo.offset);
                outputBuffer.limit(mBufferInfo.size + mBufferInfo.offset);

                handlePlayerTypeChanged();
                ByteBuffer buffer = applyVolume(outputBuffer);
                AudioBufFrame frame = new AudioBufFrame(mOutFormat, buffer,
                        mBufferInfo.presentationTimeUs / 1000);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    frame.flags |= AVConst.FLAG_END_OF_STREAM;
                    Log.d(TAG, "eos frame got, size = " + mBufferInfo.size);
                }
                mAudioFilterMgt.getSinkPin().onFrameAvailable(frame);
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);

                mSamplesWritten += frame.buf.limit() / 2 / mOutFormat.channels;
                long position = mBasePosition + mPcmPlayer.getPosition();
                mStcMgt.updateStc(position, true);
                if (VERBOSE) {
                    long pos = (mBufferInfo.presentationTimeUs - mFirstPts) / 1000;
                    Log.i(TAG, "pos: " + pos + " position: " + position);
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
                timeoutUs = 0;
            } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!eos) {
                    break;
                } else {
                    mEosSpinCount++;
                    if (mEosSpinCount > MAX_EOS_SPINS) {
                        Log.i(TAG, "Force shutting down decoder for MAX_EOS_SPINS reached");
                        AudioBufFrame frame = new AudioBufFrame(mOutFormat, null, 0);
                        frame.flags |= AVConst.FLAG_END_OF_STREAM;
                        mAudioFilterMgt.getSinkPin().onFrameAvailable(frame);
                        break;
                    }
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mMediaCodec.getOutputBuffers();
                Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
            } else {
                Log.w(TAG, "unexpected result to dequeueOutputBuffer: " + outputBufferIndex);
                break;
            }
        }
    }
}
