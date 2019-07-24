package com.component.mediaengine.framework;

import java.nio.ByteBuffer;

/**
 * Audio packet definition.
 */

public class AudioPacket extends AVPacketBase {
    /**
     * Audio encoding configuration
     */
    public AudioCodecFormat format;

    public AudioPacket(AudioCodecFormat format, ByteBuffer buf, long pts, long avPacketOpaque) {
        super(avPacketOpaque);
        this.format = format;
        this.buf = buf;
        this.pts = pts;
        this.dts = pts;
    }

    public AudioPacket(AudioCodecFormat format, ByteBuffer buf, long pts) {
        this.format = format;
        this.buf = buf;
        this.pts = pts;
        this.dts = pts;
    }

    public AudioPacket(AudioPacket pkt) {
        super(pkt);
        this.format = pkt.format;
        this.buf = pkt.buf;
        this.pts = pkt.pts;
        this.dts = pkt.dts;
        this.flags = pkt.flags;
    }
}
