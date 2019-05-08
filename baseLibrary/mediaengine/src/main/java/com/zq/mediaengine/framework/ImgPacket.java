package com.zq.mediaengine.framework;

import java.nio.ByteBuffer;

/**
 * Encoded video packet definition.
 */

public class ImgPacket extends AVPacketBase {
    /**
     * Video encoding configuration
     */
    public VideoEncodeConfig cfg;

    public ImgPacket(VideoEncodeConfig cfg, ByteBuffer buf, long pts, long dts) {
        this.cfg = cfg;
        this.buf = buf;
        this.pts = pts;
        this.dts = dts;
    }

    public ImgPacket(ImgPacket pkt) {
        this.cfg = pkt.cfg;
        this.buf = pkt.buf;
        this.pts = pkt.pts;
        this.dts = pkt.dts;
        this.flags = pkt.flags;
    }
}
