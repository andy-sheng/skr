package com.zq.mediaengine.framework;

import java.nio.ByteBuffer;

/**
 * Audio packet definition.
 */

public class AudioPacket extends AVPacketBase {
    /**
     * Audio encoding configuration
     */
    public AudioEncodeConfig cfg;

    public AudioPacket(AudioEncodeConfig cfg, ByteBuffer buf, long pts) {
        this.cfg = cfg;
        this.buf = buf;
        this.pts = pts;
        this.dts = pts;
    }

    public AudioPacket(AudioPacket pkt) {
        this.cfg = pkt.cfg;
        this.buf = pkt.buf;
        this.pts = pkt.pts;
        this.dts = pkt.dts;
        this.flags = pkt.flags;
    }
}
