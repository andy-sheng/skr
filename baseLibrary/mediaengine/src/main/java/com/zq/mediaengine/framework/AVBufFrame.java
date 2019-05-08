package com.zq.mediaengine.framework;

import java.nio.ByteBuffer;

/**
 * Base class of a/v buf frame.
 */
public class AVBufFrame extends AVFrameBase {
    /**
     * A/V Frame buffer, must be direct buffer
     */
    public ByteBuffer buf;
}
