package com.component.mediaengine.framework;

import android.media.MediaCodec;
import android.os.Build;

import com.component.mediaengine.util.AVPacketUtil;

import java.nio.ByteBuffer;

/**
 * Base class of encoded a/v data.
 */

public class AVPacketBase extends AVFrameBase {
    /**
     * A/V Packet buffer, must be direct buffer
     */
    public ByteBuffer buf;
    /**
     * Frame decode timestamp
     */
    public long dts;

    /**
     * Native pointer to native av_packet,
     * could be pass through from encoder to publisher.
     */
    private long avPacketOpaque = 0;
    private int refCount = 0;

    public AVPacketBase(long avPacketOpaque) {
        if (avPacketOpaque != 0) {
            this.avPacketOpaque = avPacketOpaque;
            refCount = 1;
        }
    }

    public AVPacketBase(AVPacketBase pkt) {
        if (pkt.avPacketOpaque != 0) {
            avPacketOpaque = AVPacketUtil.clone(pkt.avPacketOpaque);
            refCount = 1;
        }
    }

    public AVPacketBase() {
    }

    public long getAvPacketOpaque() {
        return avPacketOpaque;
    }

    @Override
    public boolean isRefCounted() {
        return avPacketOpaque != 0;
    }

    @Override
    public void ref() {
        if (avPacketOpaque != 0) {
            refCount++;
        }
    }

    @Override
    public void unref() {
        if (avPacketOpaque != 0 && refCount > 0) {
            refCount--;
            if (refCount == 0) {
                AVPacketUtil.free(avPacketOpaque);
                avPacketOpaque = 0;
            }
        }
    }

    public MediaCodec.BufferInfo toBufferInfo() {
        int flags = 0;
        if ((this.flags & AVConst.FLAG_END_OF_STREAM) != 0) {
            flags |= MediaCodec.BUFFER_FLAG_END_OF_STREAM;
        }
        if ((this.flags & AVConst.FLAG_KEY_FRAME) != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                flags |= MediaCodec.BUFFER_FLAG_KEY_FRAME;
            } else {
                flags |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            }
        }
        if ((this.flags & AVConst.FLAG_CODEC_CONFIG) != 0) {
            flags |= MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.set(0, this.buf.limit(), this.pts * 1000, flags);
        return bufferInfo;
    }
}
