package com.zq.mediaengine.framework;

/**
 * Image format for ImgBufFrame.
 */

public class ImgBufFormat {
    public int pixFmt;
    public int orientation;
    public int width;
    public int height;
    public int[] stride;
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
