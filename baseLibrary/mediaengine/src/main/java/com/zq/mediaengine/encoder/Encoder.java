package com.zq.mediaengine.encoder;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AVFrameBase;
import com.zq.mediaengine.framework.AVPacketBase;
import com.zq.mediaengine.framework.AudioCodecFormat;
import com.zq.mediaengine.framework.AudioPacket;
import com.zq.mediaengine.framework.ImgPacket;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.framework.VideoCodecFormat;
import com.zq.mediaengine.util.FpsLimiter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class of encoder modules.
 */
abstract public class Encoder<I extends AVFrameBase, O extends AVPacketBase> {
    private static final String TAG = "Encoder";
    private static final boolean VERBOSE = false;
    private static final boolean TRACE_FPS_LIMIT = false;

    public static final int ENCODER_STATE_IDLE = 0;
    public static final int ENCODER_STATE_INITIALIZING = 1;
    public static final int ENCODER_STATE_ENCODING = 2;
    public static final int ENCODER_STATE_STOPPING = 3;
    public static final int ENCODER_STATE_FLUSHING = 4;
    public static final int ENCODER_STATE_FLUSHED = 5;

    public static final int INFO_STARTED = 1;
    public static final int INFO_STOPPED = 2;

    public static final int ENCODER_ERROR_UNKNOWN = -1001;
    public static final int ENCODER_ERROR_UNSUPPORTED = -1002;

    private static final int CMD_START = 1;
    private static final int CMD_STOP = 2;
    private static final int CMD_RELEASE = 3;
    private static final int CMD_ADJUST_BITRATE = 4;
    private static final int CMD_FLUSH = 5;
    private static final int MSG_FORMAT_CHANGED = 10;
    private static final int MSG_FRAME_AVAILABLE = 11;
    private static final int MSG_REPEAT_LAST_FRAME = 12;

    private SinkPin<I> mSinkPin;
    private SrcPin<O> mSrcPin;

    protected int mType = AVConst.MEDIA_TYPE_UNKNOWN;
    protected Object mEncodeFormat;
    protected AtomicInteger mFrameEncoded;
    protected AtomicInteger mFrameDropped;
    protected boolean mMute;

    protected AtomicInteger mState;
    private final Handler mMainHandler;
    protected HandlerThread mEncodeThread;
    protected Handler mEncodeHandler;
    private EncoderInfoListener mInfoListener;
    private EncoderListener mListener;
    private FpsLimiter mFpsLimiter;
    protected ConditionVariable mSig = new ConditionVariable();

    private boolean mUseSyncMode = false;
    private boolean mAutoWork = false;

    protected volatile boolean mForceKeyFrame;
    private ByteBuffer mExtra;
    private AVPacketBase mExtraPacket;

    // repeat frame
    private Timer mTimer;
    private I mLastFrame;

    public interface EncoderInfoListener {
        void onInfo(Encoder encoder, int type, int extra);
    }

    public interface EncoderListener {
        void onError(Encoder encoder, int err);
    }

    public Encoder() {
        mSinkPin = new EncoderSinkPin();
        mSrcPin = new SrcPin<>();
        mState = new AtomicInteger(ENCODER_STATE_IDLE);
        mFrameDropped = new AtomicInteger(0);
        mFrameEncoded = new AtomicInteger(0);
        mMainHandler = new Handler(Looper.getMainLooper());
        mFpsLimiter = new FpsLimiter();
        mForceKeyFrame = false;
        initEncodeThread();
    }

    /**
     * Get sink pin.
     *
     * @return sink pin of this encoder.
     */
    public SinkPin<I> getSinkPin() {
        return mSinkPin;
    }

    /**
     * Get source pin.
     *
     * @return source pin of this encoder.
     */
    public SrcPin<O> getSrcPin() {
        return mSrcPin;
    }

    /**
     * Set encoder listener.
     *
     * @param listener listener to set
     */
    public void setEncoderListener(EncoderListener listener) {
        mListener = listener;
    }

    public void setEncoderInfoListener(EncoderInfoListener infoListener) {
        mInfoListener = infoListener;
    }

    /**
     * Get if encoding frames in sync mode.
     *
     * @return sync mode
     */
    public boolean getUseSyncMode() {
        return mUseSyncMode;
    }

    /**
     * Set if encoding frames in sync mode.
     *
     * @param useSyncMode true in sync mode, false in async mode.
     */
    public void setUseSyncMode(boolean useSyncMode) {
        mUseSyncMode = useSyncMode;
    }

    /**
     * Get if encoder start/stop by input frames.
     *
     * @return is auto work
     */
    public boolean getAutoWork() {
        return mAutoWork;
    }

    /**
     * Set if encoder start/stop by input frames.
     * <p>
     * For example, onFormatChanged would start encoding if encoder not started,
     * and frame with {@link AVConst#FLAG_END_OF_STREAM} would stop encoding.
     *
     * @param autoWork true to enable, false to disable
     */
    public void setAutoWork(boolean autoWork) {
        mAutoWork = autoWork;
    }

    /**
     * Configure encoder with given format.<br/>
     * Should be set before start encoding.
     *
     * @param encodeFormat encode format to set
     */
    public void configure(Object encodeFormat) {
        Log.d(TAG, "config encoder width " + encodeFormat);

        mEncodeFormat = encodeFormat;
        if (mType == AVConst.MEDIA_TYPE_UNKNOWN) {
            if (encodeFormat instanceof AudioCodecFormat) {
                mType = AVConst.MEDIA_TYPE_AUDIO;
            } else if (encodeFormat instanceof VideoCodecFormat) {
                mType = AVConst.MEDIA_TYPE_VIDEO;
            }
        }
    }

    /**
     * Get encoder config format set by user.
     *
     * @return null or encode format set by user.
     */
    public Object getEncodeFormat() {
        return mEncodeFormat;
    }

    /**
     * Get current state.
     *
     * @return current state
     * @see #ENCODER_STATE_IDLE
     * @see #ENCODER_STATE_INITIALIZING
     * @see #ENCODER_STATE_ENCODING
     * @see #ENCODER_STATE_STOPPING
     */
    public int getState() {
        return mState.get();
    }

    /**
     * Get frame dropped number.<br/>
     * Should be call on {@link #ENCODER_STATE_ENCODING}
     *
     * @return dropped frame number on this session.
     */
    public int getFrameDropped() {
        return mFrameDropped.get();
    }

    /**
     * Get frame encoded number.<br/>
     * Should be call on {@link #ENCODER_STATE_ENCODING}.
     *
     * @return encoded frame number on this session.
     */
    public int getFrameEncoded() {
        return mFrameEncoded.get();
    }

    /**
     * Set if encode mute audio data.
     *
     * @param mute true to encode mute data, false otherwise
     */
    public void setMute(boolean mute) {
        mMute = mute;
    }

    /**
     * Adjust bitrate while encoding.<br/>
     * Should be call on {@link #ENCODER_STATE_ENCODING}.
     *
     * @param bitrate bitrate to adjust to, in bps
     */
    public void adjustBitrate(int bitrate) {
        if (mState.get() != ENCODER_STATE_ENCODING) {
            Log.e(TAG, "Call adjustBitrate on invalid state");
            return;
        }
        if (mEncodeThread != null) {
            Message msg = mEncodeHandler.obtainMessage(CMD_ADJUST_BITRATE, bitrate, 0);
            mEncodeHandler.sendMessage(msg);
        }
    }

    /**
     * flush current encoder data.<br/>
     * Should be call on {@link #ENCODER_STATE_ENCODING},
     * and this call should always followed by {@link #stop()}.
     */
    public void flush() {
        if (mState.get() != ENCODER_STATE_ENCODING) {
            Log.e(TAG, "Call flush on invalid state");
            return;
        }
        if (mEncodeThread != null) {
            mEncodeHandler.sendEmptyMessage(CMD_FLUSH);
        }
    }

    /**
     * Start encoding.<br/>
     * Should be called on {@link #ENCODER_STATE_IDLE}.
     */
    public void start() {
        if (mState.get() != ENCODER_STATE_IDLE && mState.get() != ENCODER_STATE_STOPPING) {
            Log.i(TAG, "Call start on invalid state");
            return;
        }
        if (mEncodeThread != null) {
            Message msg = mEncodeHandler.obtainMessage(CMD_START, mEncodeFormat);
            mEncodeHandler.sendMessage(msg);
        }
    }

    public void forceKeyFrame() {
        mForceKeyFrame = true;
    }

    /**
     * Stop encoding.<br/>
     * Should be called on {@link #ENCODER_STATE_ENCODING}.
     */
    public void stop() {
        if (mState.get() == ENCODER_STATE_IDLE || mState.get() == ENCODER_STATE_STOPPING) {
            return;
        }
        stopRepeatLastFrame();
        if (getUseSyncMode()) {
            mSig.open();
        }
        if (mEncodeThread != null) {
            mEncodeHandler.sendEmptyMessage(CMD_STOP);
        }
    }

    public void release() {
        // call stop first
        stop();

        // disconnect all connected pins
        mSrcPin.disconnect(true);

        if (mEncodeThread != null) {
            mEncodeHandler.sendEmptyMessage(CMD_RELEASE);
            try {
                mEncodeThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "Encode Thread Interrupted!");
            }
            mEncodeThread = null;
        }
    }

    /**
     * Is it in {@link #ENCODER_STATE_ENCODING} state now.
     *
     * @return true while encoding, false otherwise
     */
    public boolean isEncoding() {
        return mState.get() == ENCODER_STATE_ENCODING;
    }

    /**
     * Repeating to send last video frame.
     */
    public void startRepeatLastFrame() {
        if (mState.get() != ENCODER_STATE_ENCODING ||
                mType != AVConst.MEDIA_TYPE_VIDEO || mTimer != null || mLastFrame == null) {
            Log.e(TAG, "Call startRepeatLastFrame on invalid state");
            return;
        }

        float fps = ((VideoCodecFormat) mEncodeFormat).frameRate;
        int delay = (int) (1000.f / fps);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mEncodeHandler.hasMessages(MSG_REPEAT_LAST_FRAME)) {
                    mFrameDropped.incrementAndGet();
                    if (VERBOSE) {
                        Log.d(TAG, "total dropped: " + mFrameDropped.get() +
                                " total encoded: " + mFrameEncoded.get());
                    }
                } else if (mLastFrame != null && !onFrameAvailable(mLastFrame)) {
                    mLastFrame.pts = System.nanoTime() / 1000 / 1000;
                    Message msg = mEncodeHandler.obtainMessage(MSG_REPEAT_LAST_FRAME, mLastFrame);
                    mEncodeHandler.sendMessage(msg);
                }
            }
        }, delay, delay);
    }

    /**
     * Stop repeating last video frame.
     */
    public void stopRepeatLastFrame() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * Update encode format with input frame format.
     *
     * @param src input frame format
     * @param dst encode format to be updated
     * @return true if success, false otherwise
     */
    protected boolean updateEncodeFormat(Object src, Object dst) {
        return false;
    }

    /**
     * Give child class a chance to handle onFrameAvailable
     *
     * @param frame frame
     * @return true if handled by child, and Encoder would ignore this frame, false otherwise.
     */
    protected boolean onFrameAvailable(I frame) {
        return false;
    }

    /**
     * should be called by child class after output format changed.
     *
     * @param encodedFormat output format
     */
    protected void onEncodedFormatChanged(Object encodedFormat) {
        mSrcPin.onFormatChanged(encodedFormat);
    }

    public AVPacketBase getExtra() {
        if (mExtraPacket != null) {
            if (mExtraPacket instanceof ImgPacket) {
                return new ImgPacket((ImgPacket) mExtraPacket);
            } else if (mExtraPacket instanceof AudioPacket) {
                return new AudioPacket((AudioPacket) mExtraPacket);
            }
        }
        return null;
    }

    private void cacheExtra(O packet) {
        //cache the frame with FLAG_CODEC_CONFIG flag
        if ((packet.flags & AVConst.FLAG_CODEC_CONFIG) != 0 && packet.buf != null) {
            Log.d(TAG, "Cache Extra: " + packet.buf.limit() + " bytes");
            if (mExtra == null || mExtra.capacity() < packet.buf.limit()) {
                mExtra = ByteBuffer.allocateDirect(packet.buf.limit());
                mExtra.order(ByteOrder.nativeOrder());
            }
            mExtra.clear();
            mExtra.put(packet.buf);
            mExtra.flip();
            packet.buf.rewind();

            if (packet instanceof ImgPacket) {
                mExtraPacket = new ImgPacket((ImgPacket) packet);
                mExtraPacket.buf = mExtra;
            } else if (packet instanceof AudioPacket) {
                mExtraPacket = new AudioPacket((AudioPacket) packet);
                mExtraPacket.buf = mExtra;
            }
        }
    }

    /**
     * should be called by child class after a video frame encoded.
     *
     * @param encodedFrame encoded frame
     */
    protected void onEncodedFrame(O encodedFrame) {
        cacheExtra(encodedFrame);
        mSrcPin.onFrameAvailable(encodedFrame);
    }

    abstract protected int doStart(Object encodeFormat);

    abstract protected void doStop();

    protected void doFormatChanged(Object format) {
    }

    abstract protected int doFrameAvailable(I frame);

    abstract protected void doAdjustBitrate(int bitrate);

    protected void doFlush() {
    }

    protected void doFrameDropped(I frame) {
    }

    protected void sendInfo(final int type, final int extra) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mInfoListener != null) {
                    mInfoListener.onInfo(Encoder.this, type, extra);
                }
            }
        });
    }

    protected void sendError(final int err) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onError(Encoder.this, err);
                }
            }
        });
    }

    private void initEncodeThread() {
        mEncodeThread = new HandlerThread("EncodeThread");
        mEncodeThread.start();
        mEncodeHandler = new Handler(mEncodeThread.getLooper()) {
            @Override
            @SuppressWarnings("unchecked")
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CMD_START: {
                        if (mState.get() == ENCODER_STATE_IDLE) {
                            mState.set(ENCODER_STATE_INITIALIZING);
                            mFrameEncoded.set(0);
                            mFrameDropped.set(0);
                            mLastFrame = null;
                            mExtra = null;
                            mExtraPacket = null;
                            int err = doStart(msg.obj);
                            if (err == 0) {
                                mState.set(ENCODER_STATE_ENCODING);
                                sendInfo(INFO_STARTED, 0);
                            } else {
                                mState.set(ENCODER_STATE_IDLE);
                                sendError(err);
                            }
                        }
                        if (getAutoWork() && getUseSyncMode()) {
                            mSig.open();
                        }
                        break;
                    }
                    case CMD_STOP: {
                        if (mState.get() == ENCODER_STATE_ENCODING ||
                                mState.get() == ENCODER_STATE_FLUSHED) {
                            mState.set(ENCODER_STATE_STOPPING);
                            doStop();
                            mState.set(ENCODER_STATE_IDLE);
                            sendInfo(INFO_STOPPED, 0);
                        }
                        mExtra = null;
                        mExtraPacket = null;
                        break;
                    }
                    case CMD_RELEASE: {
                        mEncodeThread.quit();
                        break;
                    }
                    case CMD_ADJUST_BITRATE: {
                        if (mState.get() == ENCODER_STATE_ENCODING) {
                            doAdjustBitrate(msg.arg1);
                        }
                        break;
                    }
                    case CMD_FLUSH: {
                        if (mState.get() == ENCODER_STATE_ENCODING) {
                            mState.set(ENCODER_STATE_FLUSHING);
                            doFlush();
                            mState.set(ENCODER_STATE_FLUSHED);
                        }
                        break;
                    }
                    case MSG_FORMAT_CHANGED: {
                        doFormatChanged(msg.obj);
                        break;
                    }
                    case MSG_FRAME_AVAILABLE: {
                        handleFrameAvailable((I) msg.obj);
                        if (getUseSyncMode()) {
                            mSig.open();
                        }
                        break;
                    }
                    case MSG_REPEAT_LAST_FRAME: {
                        handleFrameAvailable((I) msg.obj);
                        break;
                    }
                }
            }

            private void handleFrameAvailable(I frame) {
                if (mState.get() == ENCODER_STATE_ENCODING) {
                    int err = 0;
                    long encodeStartTime = System.currentTimeMillis();
                    if ((frame.flags & AVConst.FLAG_END_OF_STREAM) != 0) {
                        // flush and stop encoder
                        if (getAutoWork()) {
                            Log.d(TAG, "end of stream, flushing...");
                            doFlush();
                            stop();
                            return;
                        }
                    } else {
                        // lock this frame for repeat use
                        if (frame.isRefCounted()) {
                            frame.ref();
                        }
                        err = doFrameAvailable(frame);
                    }

                    if (err != 0) {
                        if (frame.isRefCounted()) {
                            frame.unref();
                        }
                        sendError(err);
                    } else {
                        // keep last frame for repeat use, and free previous one.
                        if (mLastFrame != null && mLastFrame.isRefCounted()) {
                            mLastFrame.unref();
                        }
                        mLastFrame = frame;
                        mFrameEncoded.incrementAndGet();
                    }
                } else {
                    doFrameDropped(frame);
                    mFrameDropped.incrementAndGet();
                    if (VERBOSE) {
                        Log.d(TAG, "total dropped: " + mFrameDropped.get() +
                                " total encoded: " + mFrameEncoded.get());
                    }
                }
            }
        };
    }

    private class EncoderSinkPin extends SinkPin<I> {
        @Override
        public void onFormatChanged(Object format) {
            if (getState() == ENCODER_STATE_IDLE && getAutoWork()) {
                if (updateEncodeFormat(format, mEncodeFormat)) {
                    if (getUseSyncMode()) {
                        mSig.close();
                    }
                    start();
                    if (getUseSyncMode()) {
                        mSig.block();
                    }
                }
            } else if (mEncodeThread != null) {
                Message msg = mEncodeHandler.obtainMessage(MSG_FORMAT_CHANGED, format);
                mEncodeHandler.sendMessage(msg);
            }
        }

        @Override
        public void onFrameAvailable(I frame) {
            if (!isEncoding()) {
                return;
            }

            //check pts depend fps except eos
            if (mType == AVConst.MEDIA_TYPE_VIDEO && (frame.flags & AVConst.FLAG_END_OF_STREAM) == 0) {
                float fps = ((VideoCodecFormat) mEncodeFormat).frameRate;
                if (fps > 0) {
                    // drop frame due fps set
                    long pts = frame.pts;
                    if (mFrameEncoded.get() == 0) {
                        mFpsLimiter.init(fps, pts);
                    }
                    if (mFpsLimiter.needDrop(pts)) {
                        if (TRACE_FPS_LIMIT) {
                            Log.d(TAG, "--- " + pts);
                        }
                        return;
                    } else if (TRACE_FPS_LIMIT) {
                        Log.d(TAG, "*** " + pts);
                    }
                }
            }

            boolean isDropped = false;
            if (mType == AVConst.MEDIA_TYPE_VIDEO &&
                    mEncodeHandler.hasMessages(MSG_FRAME_AVAILABLE)) {
                // do not cache video raw frame
                isDropped = true;
            } else {
                if (!Encoder.this.onFrameAvailable(frame)) {
                    boolean toWait = getUseSyncMode() &&
                            (frame.flags & AVConst.FLAG_END_OF_STREAM) == 0;
                    if (toWait) {
                        mSig.close();
                    }
                    Message msg = mEncodeHandler.obtainMessage(MSG_FRAME_AVAILABLE, frame);
                    mEncodeHandler.sendMessage(msg);
                    if (toWait) {
                        mSig.block();
                    }
                } else {
                    isDropped = true;
                }
            }

            if (isDropped) {
                mFrameDropped.incrementAndGet();
                if (VERBOSE) {
                    Log.d(TAG, "total dropped: " + mFrameDropped.get() +
                            " total encoded: " + mFrameEncoded.get());
                }
            }
        }

        @Override
        public void onDisconnect(boolean recursive) {
            if (recursive) {
                release();
            }
        }
    }
}
