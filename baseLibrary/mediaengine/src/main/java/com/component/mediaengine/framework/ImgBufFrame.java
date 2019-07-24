package com.component.mediaengine.framework;

import com.component.mediaengine.util.FrameBufferCache;

import java.nio.ByteBuffer;

/**
 * Image buffer frame definition
 */
public class ImgBufFrame extends AVBufFrame {
    /**
     * Image frame format
     */
    public ImgBufFormat format;

    public ImgBufFrame(ImgBufFormat format, FrameBufferCache frameBufferCache,
                       ByteBuffer buf, long pts) {
        super(frameBufferCache);
        this.format = format;
        this.buf = buf;
        this.pts = pts;
        this.flags = 0;
    }

    public ImgBufFrame(ImgBufFormat format, ByteBuffer buf, long pts) {
        this.format = format;
        this.buf = buf;
        this.pts = pts;
        this.flags = 0;
    }

    public ImgBufFrame(ImgBufFrame frame) {
        super(frame);
        this.format = frame.format;
        this.buf = frame.buf;
        this.pts = frame.pts;
        this.flags = frame.flags;
    }

    private ImgBufFrame() {
        this.format = null;
        this.buf = null;
        this.pts = 0;
        this.flags = 0;
    }
}
