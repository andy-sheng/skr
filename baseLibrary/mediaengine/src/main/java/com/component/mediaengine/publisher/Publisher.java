package com.component.mediaengine.publisher;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.component.mediaengine.framework.AVConst;
import com.component.mediaengine.framework.AVPacketBase;
import com.component.mediaengine.framework.AudioCodecFormat;
import com.component.mediaengine.framework.AudioPacket;
import com.component.mediaengine.framework.ImgPacket;
import com.component.mediaengine.framework.SinkPin;
import com.component.mediaengine.framework.VideoCodecFormat;
import com.component.mediaengine.util.FrameBufferCache;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for publishing streaming.
 */

public abstract class Publisher {
    private final static String TAG = "Publisher";
    private final static boolean VERBOSE = false;
    protected final static long INVALID_TS = Long.MIN_VALUE;

    public static final int INFO_STARTED = 1;
    public static final int INFO_AUDIO_HEADER_GOT = 2;
    public static final int INFO_VIDEO_HEADER_GOT = 3;
    public static final int INFO_STOPPED = 4;

    public static final int STATE_IDLE = 0;
    public static final int STATE_STARTING = 1;
    public static final int STATE_PUBLISHING = 2;
    public static final int STATE_STOPPING = 3;

    // keep max interleave delta sync with native implement
    private static final int MAX_INTERLEAVE_DELTA = 10 * 1000;
    public static final int ERROR_AV_ASYNC_ERROR = -2004;
    public static final int ERROR_INVALID_STATE = -2010;

    protected static final int CMD_START = 1;
    protected static final int CMD_STOP = 2;
    protected static final int CMD_WRITE_FRAME = 3;
    protected static final int CMD_RELEASE = 4;
    protected static final int CMD_ADD_AUDIO_TRACK = 5;
    protected static final int CMD_ADD_VIDEO_TRACK = 6;

    protected AtomicInteger mState;
    protected HandlerThread mPublishThread;
    protected Handler mPublishHandler;
    protected PubListener mPubListener;
    protected final Handler mMainHandler;
    protected volatile boolean mIsPublishing;

    protected String mUrl;
    protected int mVideoBitrate;
    protected int mAudioBitrate;
    protected float mFramerate;

    protected boolean mAudioHeaderGot;
    protected boolean mVideoHeaderGot;
    protected boolean mAudioTrackAdded;
    protected boolean mVideoTrackAdded;
    protected boolean mAudioFrameGot;
    protected boolean mVideoKeyFrameGot;

    private static final int VIDEO_CACHE_ITEM_SIZE = 200 * 1024;
    private static final int AUDIO_CACHE_ITEM_SIZE = 1 * 1024;
    private static final int MAX_PACKET_QUEUE_NUM = 1024;
    private FrameBufferCache mAudioCache;
    private FrameBufferCache mVideoCache;
    private BlockingQueue<AVPacketBase> mFrameQueue;
    private final Object mLock = new Object();

    protected long mInitDts;
    protected long mLastVideoDts;
    protected long mLastAudioDts;
    private ByteBuffer mVideoExtra;
    private ByteBuffer mAudioExtra;

    protected int mAFrameDropped;
    protected int mVFrameDroppedUpper;    // video frame dropped in java layer
    protected int mVFrameDroppedInner;    // video frame dropped in native layer

    protected boolean mIsAudioOnly;
    protected boolean mIsVideoOnly;
    protected boolean mForceVideoFrameFirst;
    protected boolean mCheckAVDiff = true;

    private final Map<String, String> mMetaOptions;

    // for auto work mode
    private boolean mAutoWork = false;
    private boolean mBlockingMode = false;
    private boolean mVideoEos;
    private boolean mAudioEos;
    private ConditionVariable mSig = new ConditionVariable();

    /**
     * Video encoded data input port
     */
    private SinkPin<ImgPacket> mVideoSink;
    /**
     * Audio encoded data input port
     */
    private SinkPin<AudioPacket> mAudioSink;

    public interface PubListener {
        void onInfo(int type, long msg);

        void onError(int err, long msg);
    }

    public Publisher(String name) {
        mVideoSink = new VideoSinkPin();
        mAudioSink = new AudioSinkPin();

        mMainHandler = new Handler(Looper.getMainLooper());
        mState = new AtomicInteger(STATE_IDLE);
        initPubThread(name);

        mMetaOptions = new LinkedHashMap<>();
    }

    public void setPubListener(PubListener listener) {
        this.mPubListener = listener;
    }

    public PubListener getPubListener() {
        return mPubListener;
    }

    public boolean isPublishing() {
        return mIsPublishing;
    }

    public SinkPin<ImgPacket> getVideoSink() {
        return mVideoSink;
    }

    public SinkPin<AudioPacket> getAudioSink() {
        return mAudioSink;
    }

    /**
     * Set encoded video bitrate
     *
     * @param videoBitrate video bitrate in bps
     */
    public void setVideoBitrate(int videoBitrate) {
        mVideoBitrate = videoBitrate;
    }

    /**
     * Set encoded audio bitrate
     *
     * @param audioBitrate audio bitrate in bps
     */
    public void setAudioBitrate(int audioBitrate) {
        mAudioBitrate = audioBitrate;
    }

    /**
     * Set video fps
     *
     * @param framerate video fps
     */
    public void setFramerate(float framerate) {
        mFramerate = framerate;
    }


    /**
     * Set if this stream is audio only.
     *
     * @param audioOnly true if stream is audio only
     */
    public void setAudioOnly(boolean audioOnly) {
        if (mIsVideoOnly && audioOnly) {
            throw new IllegalArgumentException("audioOnly and videoOnly both be true");
        }
        mIsAudioOnly = audioOnly;
    }

    public boolean isAudioOnly() {
        return mIsAudioOnly;
    }

    /**
     * Set if this stream is video only.
     *
     * @param videoOnly true if stream is video only
     */
    public void setVideoOnly(boolean videoOnly) {
        if (mIsAudioOnly && videoOnly) {
            throw new IllegalArgumentException("audioOnly and videoOnly both be true");
        }
        mIsVideoOnly = videoOnly;
    }

    public boolean isVideoOnly() {
        return mIsVideoOnly;
    }

    /**
     * Force first frame is video.
     *
     * @param forceVideoFrameFirst true to enable, false to disable.
     */
    public void setForceVideoFrameFirst(boolean forceVideoFrameFirst) {
        mForceVideoFrameFirst = forceVideoFrameFirst;
    }

    /**
     * Get if force video frame first feature enabled.
     *
     * @return true for enabled, false for disabled
     */
    public boolean getForceVideoFrameFirst() {
        return mForceVideoFrameFirst;
    }

    /**
     * Get if writing frames in blocking mode.
     *
     * @return blocking mode
     */
    public boolean getBlockingMode() {
        return mBlockingMode;
    }

    /**
     * Set if writing frames in blocking mode.
     *
     * @param blockingMode true in blocking mode, false in non-blocking mode.
     */
    public void setBlockingMode(boolean blockingMode) {
        mBlockingMode = blockingMode;
    }

    /**
     * Get if publisher start/stop by input frames.
     *
     * @return is auto work
     */
    public boolean getAutoWork() {
        return mAutoWork;
    }

    /**
     * Set if publisher start/stop by input frames.
     * <p>
     * For example, onFormatChanged would start publishing if publisher not started,
     * and frame with {@link AVConst#FLAG_END_OF_STREAM} would stop publishing.
     *
     * @param autoWork true to enable, false to disable
     */
    public void setAutoWork(boolean autoWork) {
        mAutoWork = autoWork;
    }

    public boolean isAudioExtraGot() {
        return mAudioExtra != null;
    }

    public boolean isVideoExtraGot() {
        return mVideoExtra != null;
    }

    public void setAudioExtra(AVPacketBase audioExtra) {
        sendExtraFrame(audioExtra);
    }

    public void setVideoExtra(AVPacketBase videoExtra) {
        sendExtraFrame(videoExtra);
    }

    public void checkAVDiff(boolean check) {
        mCheckAVDiff = check;
    }

    private void sendExtraFrame(AVPacketBase packet) {
        if (packet == null || packet.buf == null) {
            return;
        }
        handleAVFrame(packet);
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    /**
     * Set metadata for rtmp stream.
     *
     * @param key   meta key
     * @param value meta value
     */
    public void addMetaOption(String key, String value) {
        synchronized (mMetaOptions) {
            mMetaOptions.put(key, value);
        }
    }

    public int getVideoCacheLength() {
        return 0;
    }

    public boolean start(String uri) {
        if (mState.get() != STATE_IDLE && mState.get() != STATE_STOPPING) {
            Log.e(TAG, "startRecording on invalid state");
            return false;
        }
        if (TextUtils.isEmpty(uri)) {
            Log.e(TAG, "uri is empty");
            return false;
        }
        mIsPublishing = true;
        mInitDts = INVALID_TS;
        mLastVideoDts = INVALID_TS;
        mLastAudioDts = INVALID_TS;

        mAFrameDropped = 0;
        mVFrameDroppedUpper = 0;
        mVFrameDroppedInner = 0;

        mUrl = uri;
        if (mPublishThread != null) {
            Message msg = mPublishHandler.obtainMessage(CMD_START, uri);
            mPublishHandler.sendMessage(msg);
            return true;
        }
        return false;
    }

    abstract protected int doStart(String uri);

    abstract protected int doAddAudioTrack(AudioPacket packet);

    abstract protected int doAddVideoTrack(ImgPacket packet);

    abstract protected int doWriteAudioPacket(AudioPacket packet);

    abstract protected int doWriteVideoPacket(ImgPacket packet);

    protected int doWriteFrame(boolean drop) {
        int ret = 0;
        AVPacketBase packet;
        while ((packet = mFrameQueue.poll()) != null) {
            packet.dts -= mInitDts;
            packet.pts -= mInitDts;
            if (ret == 0 && !drop) {
                if (packet instanceof AudioPacket) {
                    if (packet.pts < 0) {
                        // ensure force video frame first could take effect
                        if (VERBOSE) {
                            Log.d(TAG, "drop audio " + packet.buf.limit() +
                                    " bytes pts=" + packet.pts + " dts=" + packet.dts);
                        }
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, "sent audio " + packet.buf.limit() +
                                    " bytes pts=" + packet.pts + " dts=" + packet.dts);
                        }
                        ret = doWriteAudioPacket((AudioPacket) packet);
                    }
                } else if (packet instanceof ImgPacket) {
                    if (VERBOSE) {
                        Log.d(TAG, "sent video " + packet.buf.limit() +
                                " bytes pts=" + packet.pts + " dts=" + packet.dts);
                    }
                    ret = doWriteVideoPacket((ImgPacket) packet);
                }
            }
            if (packet.isRefCounted()) {
                packet.unref();
            } else {
                synchronized (mLock) {
                    if (packet instanceof AudioPacket && packet.buf != null) {
                        mAudioCache.offer(packet.buf);
                    } else if (packet instanceof ImgPacket && packet.buf != null) {
                        mVideoCache.offer(packet.buf);
                    }
                }
            }
        }
        return ret;
    }

    public void stop() {
        if (mState.get() == STATE_IDLE || mState.get() == STATE_STOPPING) {
            return;
        }
        mIsPublishing = false;
        if (mState.get() == STATE_STARTING) {
            Log.d(TAG, "abort connecting...");
            // abort previous network I/O
            doAbort();
        }
        if (mPublishThread != null) {
            // avoid to remove unwritten CMD_WRITE_FRAME to avoid AVPacket leak.
            mPublishHandler.sendEmptyMessage(CMD_STOP);
        }
        synchronized (mMetaOptions) {
            mMetaOptions.clear();
        }
    }

    abstract protected void doAbort();

    abstract protected void doStop();

    public void release() {
        if (mPublishThread != null) {
            // It seems no need to wait for publish thread to quit
            Message msg = mPublishHandler.obtainMessage(CMD_RELEASE, mPublishThread);
            mPublishHandler.sendMessage(msg);
            mPublishThread = null;
        }
    }

    abstract protected void doRelease();

    protected void postInfo(int type) {
        postInfo(type, 0);
    }

    protected void postInfo(final int type, final long msg) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (getPubListener() != null) {
                    getPubListener().onInfo(type, msg);
                }
            }
        });
    }

    protected void postError(final int err) {
        postError(err, 0);
    }

    protected void postError(final int err, final long msg) {
        if (isPublishing()) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (getPubListener() != null) {
                        getPubListener().onError(err, msg);
                    }
                }
            });
        }
    }

    private void initPubThread(String name) {
        mPublishThread = new HandlerThread(name + "thread");
        mPublishThread.start();
        mPublishHandler = new Handler(mPublishThread.getLooper()) {
            @Override
            @SuppressWarnings("unchecked")
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CMD_START: {
                        if (mState.get() == STATE_IDLE) {
                            mState.set(STATE_STARTING);

                            mAudioHeaderGot = false;
                            mVideoHeaderGot = false;
                            mAudioTrackAdded = false;
                            mVideoTrackAdded = false;
                            mAudioFrameGot = false;
                            mVideoKeyFrameGot = false;
                            mAudioEos = false;
                            mVideoEos = false;

                            int err = doStart((String) msg.obj);
                            mState.set(err == 0 ? STATE_PUBLISHING : STATE_IDLE);
                            if (mAutoWork) {
                                mSig.open();
                            }
                            if (err == 0) {
                                postInfo(INFO_STARTED);
                            } else {
                                postError(err);
                            }
                        }
                        break;
                    }
                    case CMD_STOP: {
                        if (mState.get() == STATE_PUBLISHING) {
                            mState.set(STATE_STOPPING);

                            synchronized (mLock) {
                                mAudioExtra = null;
                                mVideoExtra = null;
                                if (mAudioCache != null) {
                                    mAudioCache.clear();
                                    mAudioCache = null;
                                }
                                if (mVideoCache != null) {
                                    mVideoCache.clear();
                                    mVideoCache = null;
                                }
                                if (mFrameQueue != null) {
                                    mFrameQueue.clear();
                                }
                            }
                            doStop();

                            mState.set(STATE_IDLE);

                            // post stopped info
                            postInfo(INFO_STOPPED);
                        }
                        break;
                    }
                    case CMD_WRITE_FRAME: {
                        if (mState.get() == STATE_PUBLISHING) {
                            int err = doWriteFrame(false);
                            if (err != 0) {
                                postError(err);
                            }
                        } else {
                            Log.e(TAG, "Please start publisher before encoder, " +
                                    "or enable auto work mode!");
                            doWriteFrame(true);
                        }
                        break;
                    }
                    case CMD_ADD_AUDIO_TRACK: {
                        if (mState.get() == STATE_PUBLISHING) {
                            int err = doAddAudioTrack((AudioPacket) msg.obj);
                            if (err == 0) {
                                mAudioTrackAdded = true;
                            } else {
                                postError(err);
                            }
                        }
                        break;
                    }
                    case CMD_ADD_VIDEO_TRACK: {
                        if (mState.get() == STATE_PUBLISHING) {
                            int err = doAddVideoTrack((ImgPacket) msg.obj);
                            if (err == 0) {
                                mVideoTrackAdded = true;
                            } else {
                                postError(err);
                            }
                        }
                        break;
                    }
                    case CMD_RELEASE: {
                        doRelease();
                        ((HandlerThread) msg.obj).quit();
                        break;
                    }
                }
            }
        };
    }

    /**
     * Does current publisher using ffmpeg.
     *
     * @return true if used, false otherwise.
     */
    protected boolean isUseFFmpeg() {
        return true;
    }

    /**
     * Should this publisher add SPS/PPS data to video key frame.
     *
     * @return Publisher would add SPS/PPS to video key frame if returned true,
     * keep key frame as is if returned false.
     */
    protected abstract boolean isAddExtraForVideoKeyFrame();

    protected void handleAVFrame(AVPacketBase inPacket) {
        if (inPacket == null) {
            return;
        }

        if (mAutoWork && mBlockingMode) {
            // wait for publisher started
            mSig.block();
        }

        // ignore input frame before publisher started
        if (mState.get() != STATE_PUBLISHING) {
            return;
        }

        String TYPE;
        boolean isVideo;
        boolean isVideoKeyFrame = false;
        AVPacketBase packet;
        if (inPacket instanceof AudioPacket) {
            packet = new AudioPacket((AudioPacket) inPacket);
            isVideo = false;
            TYPE = "audio";
        } else if (inPacket instanceof ImgPacket) {
            packet = new ImgPacket((ImgPacket) inPacket);
            isVideo = true;
            TYPE = "video";
            isVideoKeyFrame = ((packet.flags & AVConst.FLAG_KEY_FRAME) != 0);
        } else {
            Log.i(TAG, "got unknown type frame, dropped");
            return;
        }

        do {
            // drop video frame in audio only mode/drop audio frame in video only mode
            if (isAudioOnly() && isVideo || isVideoOnly() && !isVideo) {
                Log.w(TAG, "get video in audio only mode or get audio in video only mode!");
                break;
            }

            // handle config frame
            if ((packet.flags & AVConst.FLAG_CODEC_CONFIG) != 0) {
                if (isVideo) {
                    mVideoHeaderGot = true;
                } else {
                    mAudioHeaderGot = true;
                }
                synchronized (mLock) {
                    Log.d(TAG, TYPE + " header got");
                    if (packet.buf != null) {
                        if (!isVideo && mAudioExtra == null) {
                            mAudioExtra = ByteBuffer.allocateDirect(packet.buf.limit());
                        } else if (isVideo && mVideoExtra == null) {
                            mVideoExtra = ByteBuffer.allocateDirect(packet.buf.limit());
                        }

                        ByteBuffer extra = isVideo ? mVideoExtra : mAudioExtra;
                        if (extra.capacity() < packet.buf.limit()) {
                            extra = ByteBuffer.allocateDirect(packet.buf.limit());
                            if (isVideo) {
                                mVideoExtra = extra;
                            } else {
                                mAudioExtra = extra;
                            }
                        }
                        extra.clear();
                        extra.put(packet.buf);
                        extra.flip();
                        packet.buf.rewind();
                        packet.buf = extra;
                    }
                }
                if (mPublishThread != null) {
                    int cmd = isVideo ? CMD_ADD_VIDEO_TRACK : CMD_ADD_AUDIO_TRACK;
                    Message msg = mPublishHandler.obtainMessage(cmd, packet);
                    mPublishHandler.sendMessage(msg);
                }
                break;
            }

            // handle eos frame
            if ((packet.flags & AVConst.FLAG_END_OF_STREAM) != 0) {
                if (mAutoWork) {
                    if (isVideo) {
                        mVideoEos = true;
                    } else {
                        mAudioEos = true;
                    }
                    Log.d(TAG, TYPE + " EOS got");
                    if ((mIsVideoOnly && mVideoEos) ||
                            (mIsAudioOnly && mAudioEos) ||
                            (mVideoEos && mAudioEos)) {
                        // TODO: call flush first
                        stop();
                    }
                    break;
                }
            }

            // drop a/v frame before config frame came
            if (!mAudioHeaderGot && !isVideo) {
                Log.d(TAG, "drop audio frames before audio header");
                break;
            }
            if (!mVideoHeaderGot && isVideo) {
                Log.d(TAG, "drop video frames before video header");
                break;
            }

            // drop non-key frame before first key frame came
            if (isVideo && !mVideoKeyFrameGot && !isVideoKeyFrame) {
                Log.d(TAG, "drop non-key frame");
                break;
            }

            // drop frame without payload
            if (packet.buf == null) {
                break;
            }

            // print origin frame info
            if (VERBOSE) {
                Log.d(TAG, "send " + TYPE + " size=" + packet.buf.limit() +
                        " pts=" + packet.pts + " dts=" + packet.dts);
            }

            // check a/v diff
            if (mCheckAVDiff && (!mIsAudioOnly) && (!mIsVideoOnly)) {
                if (!isVideo && mLastVideoDts != INVALID_TS) {
                    long tsDiff = Math.abs(packet.dts - mLastVideoDts);
                    if (tsDiff > MAX_INTERLEAVE_DELTA) {
                        Log.w(TAG, "the audio and video capture tsDiff above :" + tsDiff);
                        postError(ERROR_AV_ASYNC_ERROR, tsDiff);
                    }
                }
            }

            // check dts diff with last frame
            long lastDts = isVideo ? mLastVideoDts : mLastAudioDts;
            if (packet.dts <= lastDts) {
                Log.i(TAG, "non increase " + TYPE + " timestamp, diff=" + (packet.dts - lastDts));
                packet.pts += lastDts - packet.dts + 10;
                packet.dts = lastDts + 10;
            }
            if (isVideo) {
                mLastVideoDts = packet.dts;
            } else {
                mLastAudioDts = packet.dts;
            }

            // cal base dts value
            if (mInitDts == INVALID_TS) {
                // keep negative dts/pts negative
                mInitDts = (packet.pts > 0) ? packet.pts : 0;
            }
            if (isVideoKeyFrame && !mVideoKeyFrameGot) {
                mVideoKeyFrameGot = true;
                if (mForceVideoFrameFirst || packet.pts < mInitDts) {
                    mInitDts = (packet.pts > 0) ? packet.pts : 0;
                }
            }
            if (!isVideo) {
                mAudioFrameGot = true;
            }

            // create frame cache if needed
            FrameBufferCache cache;
            synchronized (mLock) {
                if (!isVideo && mAudioCache == null) {
                    // unlimit frame buffer cache
                    mAudioCache = new FrameBufferCache(0, AUDIO_CACHE_ITEM_SIZE);
                } else if (isVideo && mVideoCache == null) {
                    // unlimit frame buffer cache
                    mVideoCache = new FrameBufferCache(0, VIDEO_CACHE_ITEM_SIZE);
                }
                if (mFrameQueue == null) {
                    mFrameQueue = new LinkedBlockingQueue<>(MAX_PACKET_QUEUE_NUM);
                }
                cache = isVideo ? mVideoCache : mAudioCache;
            }

            boolean needExtra = isVideoKeyFrame && isAddExtraForVideoKeyFrame() && mVideoExtra != null;
            if (!packet.isRefCounted() || needExtra) {
                // copy frame buffer
                int size = packet.buf.limit();
                synchronized (mLock) {
                    if (needExtra) {
                        size += mVideoExtra.limit();
                    }
                }
                ByteBuffer buffer = cache.poll(size);
                if (buffer == null) {
                    Log.wtf(TAG, "cache.poll failed!!!");
                    break;
                }
                if (isVideoKeyFrame) {
                    synchronized (mLock) {
                        if (isAddExtraForVideoKeyFrame() && mVideoExtra != null) {
                            buffer.put(mVideoExtra);
                            mVideoExtra.rewind();
                        }
                    }
                }
                buffer.put(packet.buf);
                buffer.flip();
                packet.buf.rewind();
                packet.buf = buffer;
                // after buf replaced, we make packet un-refcounted
                if (packet.isRefCounted()) {
                    packet.unref();
                }
            } else {
                // ref packet
                packet.ref();
            }

            boolean dropped = false;
            if (mBlockingMode) {
                try {
                    mFrameQueue.put(packet);
                } catch (InterruptedException e) {
                    Log.e(TAG, "put packet to queue interrupted!");
                    dropped = true;
                }
            } else {
                dropped = !mFrameQueue.offer(packet);
            }
            if (dropped) {
                if (isVideo) {
                    mVFrameDroppedUpper++;
                } else {
                    mAFrameDropped++;
                }
                Log.w(TAG, "dropped " + mVFrameDroppedUpper + " " + TYPE +
                        " frames in java layer, this size:" + packet.buf.limit());
                break;
            }

            // determine whether to send frame
            if (mIsAudioOnly || mIsVideoOnly || (mVideoKeyFrameGot && mAudioFrameGot)) {
                if (mPublishHandler != null) {
                    mPublishHandler.sendEmptyMessage(CMD_WRITE_FRAME);
                }
            }
        } while (false);

        // unref ref counted packet
        if (packet.isRefCounted()) {
            packet.unref();
        }
    }

    private void handleFormatChanged(Object format) {
        if (!mAutoWork) {
            return;
        }

        boolean isVideo;
        if (format instanceof VideoCodecFormat) {
            isVideo = true;
        } else if (format instanceof AudioCodecFormat) {
            isVideo = false;
        } else {
            return;
        }

        if (isAudioOnly() && isVideo || isVideoOnly() && !isVideo) {
            return;
        }

        if (!mIsPublishing && mState.get() == STATE_IDLE) {
            mSig.close();
            if (start(mUrl)) {
                mSig.block();
            } else {
                mSig.open();
                postError(ERROR_INVALID_STATE);
            }
        }
    }

    private class VideoSinkPin extends SinkPin<ImgPacket> {

        @Override
        public void onFormatChanged(Object format) {
            handleFormatChanged(format);
        }

        @Override
        public void onFrameAvailable(ImgPacket frame) {
            handleAVFrame(frame);
        }

        @Override
        public void onDisconnect(boolean recursive) {
            if (recursive && mIsVideoOnly) {
                release();
            }
        }
    }

    // as the main input pin
    private class AudioSinkPin extends SinkPin<AudioPacket> {

        @Override
        public void onFormatChanged(Object format) {
            handleFormatChanged(format);
        }

        @Override
        public void onFrameAvailable(AudioPacket frame) {
            handleAVFrame(frame);
        }

        @Override
        public void onDisconnect(boolean recursive) {
            if (recursive && !mIsVideoOnly) {
                release();
            }
        }
    }
}
