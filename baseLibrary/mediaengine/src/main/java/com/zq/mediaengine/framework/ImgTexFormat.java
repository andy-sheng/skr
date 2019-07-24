package com.zq.mediaengine.framework;

/**
 * Image texture format definition.
 */
public class ImgTexFormat {
    public static final int COLOR_RGBA = 1;
    public static final int COLOR_YUVA = 2;
    public static final int COLOR_EXTERNAL_OES = 3;

    public int colorFormat;
    public final int width;
    public final int height;

    public ImgTexFormat(int colorFormat, int width, int height) {
        this.colorFormat = colorFormat;
        this.width = width;
        this.height = height;
    }

    public ImgTexFormat(ImgTexFormat format) {
        this.colorFormat = format.colorFormat;
        this.width = format.width;
        this.height = format.height;
    }
}
