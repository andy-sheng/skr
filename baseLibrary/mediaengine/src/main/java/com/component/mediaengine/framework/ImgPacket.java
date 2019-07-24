package com.component.mediaengine.framework;

import java.nio.ByteBuffer;

/**
 * Encoded video packet definition.
 */

public class ImgPacket extends AVPacketBase {
    /**
     * Video encoding configuration
     */
    public VideoCodecFormat format;

    public ImgPacket(VideoCodecFormat format, ByteBuffer buf, long pts, long dts,
                     long avPacketOpaque) {
        super(avPacketOpaque);
        this.format = format;
        this.buf = buf;
        this.pts = pts;
        this.dts = dts;
    }

    public ImgPacket(VideoCodecFormat format, ByteBuffer buf, long pts, long dts) {
        this.format = format;
        this.buf = buf;
        this.pts = pts;
        this.dts = dts;
    }

    public ImgPacket(ImgPacket pkt) {
        super(pkt);
        this.format = pkt.format;
        this.buf = pkt.buf;
        this.pts = pkt.pts;
        this.dts = pkt.dts;
        this.flags = pkt.flags;
    }
}
