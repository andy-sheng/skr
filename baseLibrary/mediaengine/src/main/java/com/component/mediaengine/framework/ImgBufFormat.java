package com.component.mediaengine.framework;

/**
 * Image format for ImgBufFrame.
 */

public class ImgBufFormat {
    public static final int FMT_OPAQUE = AVConst.PIX_FMT_NONE;
    public static final int FMT_NV21 = AVConst.PIX_FMT_NV21;
    public static final int FMT_YV12 = AVConst.PIX_FMT_YV12;
    public static final int FMT_I420 = AVConst.PIX_FMT_I420;
    public static final int FMT_ARGB = AVConst.PIX_FMT_ARGB;
    public static final int FMT_RGBA = AVConst.PIX_FMT_RGBA;
    public static final int FMT_BGR8 = AVConst.PIX_FMT_BGR8;

    /**
     * pixel format of current frame, AVConst.PIX_FMT_XXX
     */
    public int pixFmt;
    /**
     * orientation of current frame, 0/90/180/270 is valid value
     */
    public int orientation;
    /**
     * image width
     */
    public int width;
    /**
     * image height
     */
    public int height;
    /**
     * stride of current image buffer
     */
    public int[] stride;
    /**
     * stride number
     */
    public int strideNum;

    public ImgBufFormat(int pixFmt, int width, int height, int orientation) {
        this.pixFmt = pixFmt;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        this.stride = null;
        this.strideNum = 0;
    }

    public ImgBufFormat(int pixFmt, int width, int height, int orientation, int[] stride) {
        this.pixFmt = pixFmt;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        this.stride = stride;
        if (stride != null) {
            this.strideNum = stride.length;
        }
    }

    public ImgBufFormat() {
        this.pixFmt = 0;
        this.width = 0;
        this.height = 0;
        this.orientation = 0;
        this.stride = null;
        this.strideNum = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ImgBufFormat)) {
            return false;
        }
        ImgBufFormat format = (ImgBufFormat) obj;
        return this.pixFmt == format.pixFmt &&
                this.width == format.width &&
                this.height == format.height &&
                this.orientation == format.orientation;
    }
}
