package com.zq.mediaengine.framework;

/**
 * Base class of all a/v frame.
 */
public class AVFrameBase {
    /**
     * Frame presentation timestamp
     */
    public long pts;
    /**
     * Frame flags, AVConst.FLAG_XXX
     */
    public int flags;
}
