package com.zq.mediaengine.framework;

import java.nio.ByteBuffer;

/**
 * Base class of encoded a/v data.
 */

public class AVPacketBase {
    /**
     * A/V Packet buffer, must be direct buffer
     */
    public ByteBuffer buf;
    /**
     * Frame decode timestamp
     */
    public long dts;
    /**
     * Frame presentation timestamp
     */
    public long pts;
    /**
     * Frame flags, AVConst.FLAG_XXX
     */
    public int flags;
}
