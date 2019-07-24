package com.component.mediaengine.framework;

import com.component.mediaengine.util.FrameBufferCache;

import java.nio.ByteBuffer;

/**
 * Base class of a/v buf frame.
 */
public class AVBufFrame extends AVFrameBase {
    /**
     * A/V Frame buffer, must be direct buffer
     */
    public ByteBuffer buf;

    /**
     * For ByteBuffer reuse.
     */
    private FrameBufferCache bufferCache;

    public AVBufFrame() {
        bufferCache = null;
    }

    public AVBufFrame(FrameBufferCache bufferCache) {
        this.bufferCache = bufferCache;
    }

    public AVBufFrame(AVBufFrame frame) {
        if (frame.isRefCounted()) {
            bufferCache = frame.bufferCache;
            bufferCache.ref(frame.buf);
        }
    }

    @Override
    public boolean isRefCounted() {
        return bufferCache != null && buf != null;
    }

    @Override
    synchronized public void ref() {
        if (isRefCounted()) {
            bufferCache.ref(buf);
        }
    }

    @Override
    synchronized public void unref() {
        if (isRefCounted()) {
            bufferCache.unref(buf);
        }
    }
}
