package com.zq.mediaengine.filter.imgtex;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.util.gles.GLRender;

import java.nio.ByteBuffer;

/**
 * RGBA buffer filter inserted in gpu pipe.
 * <p>
 * Use this filter would cause performance drop.
 */

abstract public class RGBABufFilter extends ImgTexBufFilter {
    private static final String TAG = "RGBABufFilter";

    public RGBABufFilter(GLRender glRender) {
        super(glRender, AVConst.PIX_FMT_RGBA);
    }

    @Override
    protected void onSizeChanged(int[] stride, int width, int height) {
        onSizeChanged(stride[0], width, height);
    }

    @Override
    protected ByteBuffer doFilter(ByteBuffer buffer, int[] stride, int width, int height) {
        return doFilter(buffer, stride[0], width, height);
    }

    /**
     * Notify image size changed.
     *
     * @param stride the row stride for this RGBA plane, in bytes
     * @param width  the image width
     * @param height the image height
     */
    abstract protected void onSizeChanged(int stride, int width, int height);

    /**
     * Do cpu filter here.
     *
     * @param buffer the image RGBA buffer
     * @param stride the row stride for this RGBA plane, in bytes
     * @param width  the image width
     * @param height the image height
     * @return ByteBuffer with filtered RGBA data in same size as input.
     */
    abstract protected ByteBuffer doFilter(ByteBuffer buffer, int stride, int width, int height);
}
