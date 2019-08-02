package com.zq.mediaengine.framework;

import com.zq.mediaengine.util.FrameBufferCache;

import java.nio.ByteBuffer;

/**
 * Audio buffer frame definition
 */
public class AudioBufFrame extends AVBufFrame {
    /**
     * Audio frame format
     */
    public AudioBufFormat format;

    public AudioBufFrame(AudioBufFormat format, FrameBufferCache bufferCache,
                         ByteBuffer buf, long pts) {
        super(bufferCache);
        this.format = format;
        this.buf = buf;
        this.pts = pts;
        this.flags = 0;
    }

    public AudioBufFrame(AudioBufFormat format, ByteBuffer buf, long pts) {
        this.format = format;
        this.buf = buf;
        this.pts = pts;
        this.flags = 0;
    }

    public AudioBufFrame(AudioBufFormat format, ByteBuffer buf, long pts, int flags) {
        this.format = format;
        this.buf = buf;
        this.pts = pts;
        this.flags = flags;
    }

    public AudioBufFrame(AudioBufFrame frame) {
        super(frame);
        this.format = frame.format;
        this.buf = frame.buf;
        this.pts = frame.pts;
        this.flags = frame.flags;
    }
}