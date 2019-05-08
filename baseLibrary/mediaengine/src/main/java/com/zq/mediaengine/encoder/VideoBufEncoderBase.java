package com.zq.mediaengine.encoder;

import android.util.Log;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.ImgBufFormat;
import com.zq.mediaengine.framework.ImgBufFrame;
import com.zq.mediaengine.framework.ImgPacket;
import com.zq.mediaengine.framework.VideoEncodeConfig;
import com.zq.mediaengine.util.FrameBufferCache;

import java.nio.ByteBuffer;

/**
 * Base class of video encoder with ImgBufFrame as input.
 */

public abstract class VideoBufEncoderBase extends Encoder<ImgBufFrame, ImgPacket> {
    private final static String TAG = "VideoBufEncoderBase";
    private final static boolean VERBOSE = false;

    protected static final int VIDEO_CACHE_NUM = 1;
    protected static final int VIDEO_CACHE_ITEM_SIZE = 2*1024*1024;

    protected VideoEncodeConfig mVideoEncodeConfig;
    protected VideoEncodeConfig mOutConfig;
    protected boolean mFirstFrame;
    protected FrameBufferCache mFrameBufferCache;

    public VideoBufEncoderBase() {
        mFrameBufferCache = new FrameBufferCache(VIDEO_CACHE_NUM, VIDEO_CACHE_ITEM_SIZE);
    }

    @Override
    public int getEncoderType() {
        return AVConst.MEDIA_TYPE_VIDEO;
    }

    /**
     * Start video encoder in encoding thread.
     *
     * @param config video encode configuration.
     * @return 0 on success, or error code.
     */
    abstract protected int doStart(VideoEncodeConfig config);

    /**
     * Encode an video frame.
     *
     * @param frame video frame to be encoded.
     * @return 0 on success, or error code.
     */
    abstract protected int doEncode(ImgBufFrame frame);

    @Override
    protected int doStart(Object encodeConfig) {
        mFirstFrame = true;
        mVideoEncodeConfig = (VideoEncodeConfig) encodeConfig;
        mOutConfig = new VideoEncodeConfig(mVideoEncodeConfig);
        return doStart(mVideoEncodeConfig);
    }

    @Override
    protected boolean updateEncodeFormat(Object src, Object dst) {
        ImgBufFormat imgBufFormat = (ImgBufFormat) src;
        VideoEncodeConfig config = (VideoEncodeConfig) dst;
        config.width = imgBufFormat.width;
        config.height = imgBufFormat.height;
        config.pixFmt = imgBufFormat.pixFmt;
        return true;
    }

    @Override
    protected void doFormatChanged(Object format) {
        ImgBufFormat imgBufFormat = (ImgBufFormat) format;
        if (getState() == ENCODER_STATE_ENCODING) {
            if (mVideoEncodeConfig.width != imgBufFormat.width ||
                    mVideoEncodeConfig.height != imgBufFormat.height) {
                Log.d(TAG, "restart encoder");
                doFlush();
                doStop();
                mVideoEncodeConfig.width = imgBufFormat.width;
                mVideoEncodeConfig.height = imgBufFormat.height;
                doStart(mEncodeConfig);
            }
        }
    }

    @Override
    protected boolean onFrameAvailable(ImgBufFrame frame) {
        if (frame == null || frame.buf == null) {
            return false;
        }

        ByteBuffer buffer = mFrameBufferCache.poll(frame.buf.limit());
        if (buffer == null) {
            Log.w(TAG, "Video frame dropped, size=" + frame.buf.limit() + " pts=" + frame.pts);
            return true;
        }
        if (buffer != frame.buf) {
            buffer.put(frame.buf);
            buffer.flip();
            frame.buf.rewind();
            frame.buf = buffer;
        }
        return false;
    }

    @Override
    protected int doFrameAvailable(ImgBufFrame frame) {
        if (frame == null) {
            return 0;
        }
        if (VERBOSE) {
            int size = (frame.buf != null) ? frame.buf.limit() : 0;
            Log.d(TAG, "video frame in: size=" + size + " pts=" + frame.pts);
        }

        // gif trigger
        if (mVideoEncodeConfig.codecId == AVConst.CODEC_ID_GIF && mFirstFrame) {
            onEncodedFormatChanged(mOutConfig);
        }

        if (mFirstFrame || mForceKeyFrame) {
            ImgPacket outPacket = new ImgPacket(mOutConfig, null, frame.pts, frame.pts);
            outPacket.flags |= AVConst.FLAG_DUMMY_VIDEO_FRAME;
            onEncodedFrame(outPacket);
            if (mFirstFrame) {
                mFirstFrame = false;
            }
            if (mForceKeyFrame) {
                frame.flags |= AVConst.FLAG_KEY_FRAME;
                mForceKeyFrame = false;
            }
        }
        int ret = doEncode(frame);
        if (frame.buf != null) {
            mFrameBufferCache.offer(frame.buf);
        }
        return ret;
    }

    @Override
    protected void doFrameDropped(ImgBufFrame frame) {
        if (frame != null && frame.buf != null) {
            mFrameBufferCache.offer(frame.buf);
        }
    }

    protected void sendEncodedPacket(ByteBuffer data, long dts, long pts, int flags) {
        if (VERBOSE) {
            int size = (data != null) ? data.limit() : 0;
            Log.d(TAG, "encoded video frame: size=" + size + " dts=" + dts + " pts=" + pts);
        }
        if ((flags & AVConst.FLAG_CODEC_CONFIG) != 0) {
            // trigger onFormatChanged event to next module
            mOutConfig = new VideoEncodeConfig(mVideoEncodeConfig);
            onEncodedFormatChanged(mOutConfig);
        }
        // send frame to next module
        ImgPacket packet = new ImgPacket(mOutConfig, data, pts, dts);
        packet.flags = flags;
        onEncodedFrame(packet);
    }
}
