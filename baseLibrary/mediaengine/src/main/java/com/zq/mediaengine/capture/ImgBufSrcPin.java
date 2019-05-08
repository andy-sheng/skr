package com.zq.mediaengine.capture;

import android.graphics.Bitmap;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.ImgBufFormat;
import com.zq.mediaengine.framework.ImgBufFrame;
import com.zq.mediaengine.framework.SrcPin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Convert Bitmap to ImgBufFrame.
 */
public class ImgBufSrcPin extends SrcPin<ImgBufFrame> {
    private int[] mPixels;
    private ByteBuffer mByteBuffer;
    private ImgBufFrame mImgBufFrame;

    synchronized public void updateFrame(Bitmap img, boolean recycle) {
        if (img == null || img.isRecycled()) {
            if (mImgBufFrame == null) {
                return;
            }
            mImgBufFrame = null;
            if(this.isConnected()) {
                onFrameAvailable(null);
            }
            return;
        }

        boolean formatChanged = false;
        ImgBufFormat format;
        if (mImgBufFrame == null ||
                mImgBufFrame.format.width != img.getWidth() ||
                mImgBufFrame.format.height != img.getHeight()) {
            format = new ImgBufFormat(AVConst.PIX_FMT_ARGB,
                    img.getWidth(), img.getHeight(), 0);
            formatChanged = true;
        } else {
            format = mImgBufFrame.format;
        }

        int width = img.getWidth();
        int height = img.getHeight();
        int len = width * height;
        int size = len * 4;
        if (mPixels == null) {
            mPixels = new int[len];
        }
        if (mPixels.length < len) {
            mPixels = null;
            mPixels = new int[len];
        }
        img.getPixels(mPixels, 0, width, 0, 0, width, height);
        if (recycle) {
            img.recycle();
        }

        if (mByteBuffer == null) {
            mByteBuffer = ByteBuffer.allocateDirect(size);
            mByteBuffer.order(ByteOrder.nativeOrder());
        }
        if (mByteBuffer.capacity() < size) {
            mByteBuffer = null;
            mByteBuffer = ByteBuffer.allocateDirect(size);
            mByteBuffer.order(ByteOrder.nativeOrder());
        }
        mByteBuffer.clear();
        mByteBuffer.asIntBuffer().put(mPixels, 0, len);

        if (formatChanged) {
            onFormatChanged(format);
            mImgBufFrame = new ImgBufFrame(format, mByteBuffer, 0);
        }
        if(this.isConnected()) {
            onFrameAvailable(mImgBufFrame);
        }
    }

    synchronized public void release() {
        disconnect(true);
        mPixels = null;
        mByteBuffer = null;
    }
}
