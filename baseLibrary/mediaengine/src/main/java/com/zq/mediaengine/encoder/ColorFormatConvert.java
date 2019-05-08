package com.zq.mediaengine.encoder;

import com.zq.mediaengine.util.LibraryLoader;

import java.nio.ByteBuffer;

/**
 * Convert colorFormat.
 *
 * @hide
 */
public class ColorFormatConvert {
    public static native int YUVAToI420(ByteBuffer src, int row_stride,
                                        int width, int height, ByteBuffer dest);

    public static native int RGBAToI420(ByteBuffer src, int row_stride,
                                        int width, int height, ByteBuffer dest);

    public static native int I420ToRGBA(ByteBuffer src, int row_stride, int width, int height,
                                        ByteBuffer dest);

    static {
        LibraryLoader.load();
    }
}
