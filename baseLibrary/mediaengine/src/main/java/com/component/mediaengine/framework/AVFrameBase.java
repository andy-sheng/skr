package com.component.mediaengine.framework;

/**
 * Base class of all a/v frame/packet.
 */
public class AVFrameBase {
    public static final int FLAG_KEY_FRAME = AVConst.FLAG_KEY_FRAME;
    public static final int FLAG_CODEC_CONFIG = AVConst.FLAG_CODEC_CONFIG;
    public static final int FLAG_END_OF_STREAM = AVConst.FLAG_END_OF_STREAM;
    public static final int FLAG_DETACH_NATIVE_MODULE = AVConst.FLAG_DETACH_NATIVE_MODULE;
    /**
     * Frame presentation timestamp
     */
    public long pts;
    /**
     * Frame flags, AVConst.FLAG_XXX
     */
    public int flags;

    /**
     * Is this frame ref counted.
     *
     * @return true if ref counted false if not.
     */
    public boolean isRefCounted() {
        return false;
    }

    /**
     * Increase ref count if this frame is ref counted.
     */
    public void ref() {
    }

    /**
     * Decrease ref count if this frame is ref counted.
     * When ref count decrease to zero, buffer in this frame would be freed.
     */
    public void unref() {
    }
}
