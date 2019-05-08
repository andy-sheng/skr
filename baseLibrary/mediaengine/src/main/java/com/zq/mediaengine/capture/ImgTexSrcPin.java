package com.zq.mediaengine.capture;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.ConditionVariable;

import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;
import com.zq.mediaengine.util.gles.YUVLoader;

import java.nio.ByteBuffer;


/**
 * Convert bitmap image to ImgTexFrame.
 */
public class ImgTexSrcPin extends SrcPin<ImgTexFrame> {
    private static final String TAG = "ImgTexSrcPin";
    private ImgTexFormat mImgTexFormat;
    private int mTextureId = ImgTexFrame.NO_TEXTURE;
    private float[] mTexMatrix;
    private GLRender mGLRender;
    private boolean mSyncMode;
    private ConditionVariable mSig = new ConditionVariable();
    private YUVLoader mYUVLoader;

    public ImgTexSrcPin(GLRender glRender) {
        mTexMatrix = new float[16];

        // keep eye on if glcontext recreated
        mGLRender = glRender;
        mGLRender.addListener(mGLRenderListener);
        mSyncMode = false;
    }

    public boolean getUseSyncMode() {
        return mSyncMode;
    }

    public void setUseSyncMode(boolean syncMode) {
        mSyncMode = syncMode;
    }

    public void updateFrame(final Bitmap img, final boolean recycle) {
        updateFrame(img, 0, System.nanoTime() / 1000 / 1000, recycle);
    }

    public void updateFrame(final Bitmap img, final long pts, final boolean recycle) {
        updateFrame(img, 0, pts, recycle);
    }

    /**
     * Update image with Bitmap.
     *
     * @param img     source image
     * @param rotate  rotate degrees, only 0, 90, 180, 270 valid
     * @param pts     pts of current image frame
     * @param recycle should be recycled
     */
    public void updateFrame(final Bitmap img, final int rotate,
                            final long pts, final boolean recycle) {
        if (mGLRender.isGLRenderThread()) {
            doUpdateFrame(img, rotate, pts, recycle);
        } else {
            mGLRender.queueEvent(new Runnable() {
                @Override
                public void run() {
                    doUpdateFrame(img, rotate, pts, recycle);
                }
            });
        }
    }

    public void updateFrame(final ByteBuffer buf, final int stride,
                            final int width, final int height) {
        updateFrame(buf, stride, width, height, 0, System.nanoTime() / 1000 / 1000);
    }

    public void updateFrame(final ByteBuffer buf, final int stride,
                            final int width, final int height, final long pts) {
        updateFrame(buf, stride, width, height, 0, pts);
    }

    /**
     * Update image frame with RGBA ByteBuffer.
     *
     * @param buf    image ByteBuffer with RGBA data
     * @param stride stride of current frame, in bytes
     * @param width  width of frame
     * @param height height of frame
     * @param rotate rotate degrees, only 0, 90, 180, 270 valid
     * @param pts    pts of current frame
     */
    public void updateFrame(final ByteBuffer buf, final int stride, final int width,
                            final int height, final int rotate, final long pts) {
        if (mGLRender.isGLRenderThread()) {
            doUpdateFrame(buf, stride, width, height, rotate, pts);
        } else {
            if (mSyncMode) {
                mSig.close();
            }
            mGLRender.queueEvent(new Runnable() {
                @Override
                public void run() {
                    doUpdateFrame(buf, stride, width, height, rotate, pts);
                    if (mSyncMode) {
                        mSig.open();
                    }
                }
            });
            if (mSyncMode) {
                mSig.block();
            }
        }
    }

    /**
     * Update image frame with YUV420P ByteBuffer.
     *
     * @param buf     image ByteBuffer with I420 data
     * @param strides strides of each planar
     * @param width   width of frame
     * @param height  height of frame
     * @param rotate  rotate degrees, only 0, 90, 180, 270 valid
     * @param pts     pts of current frame
     */
    public void updateYUVFrame(final ByteBuffer buf, final int[] strides, final int width,
                               final int height, final int rotate, final long pts) {
        if (mGLRender.isGLRenderThread()) {
            doUpdateYUVFrame(buf, strides, width, height, rotate, pts);
        } else {
            if (mSyncMode) {
                mSig.close();
            }
            mGLRender.queueEvent(new Runnable() {
                @Override
                public void run() {
                    doUpdateYUVFrame(buf, strides, width, height, rotate, pts);
                    if (mSyncMode) {
                        mSig.open();
                    }
                }
            });
            if (mSyncMode) {
                mSig.block();
            }
        }
    }

    /**
     * Repeat to send current image frame with current system time as new pts.
     * Do nothing if no frame uploaded yet.
     */
    public void repeatFrame() {
        repeatFrame(System.nanoTime() / 1000 / 1000);
    }

    /**
     * Repeat to send current image frame, do nothing if no frame uploaded yet.
     *
     * @param pts new pts to set
     */
    public void repeatFrame(final long pts) {
        if (mGLRender.isGLRenderThread()) {
            doRepeatFrame(pts);
        } else {
            if (mSyncMode) {
                mSig.close();
            }
            mGLRender.queueEvent(new Runnable() {
                @Override
                public void run() {
                    doRepeatFrame(pts);
                    if (mSyncMode) {
                        mSig.open();
                    }
                }
            });
            if (mSyncMode) {
                mSig.block();
            }
        }
    }

    public void reset() {
        if (mGLRender.isGLRenderThread()) {
            doReset();
        } else {
            mGLRender.queueEvent(new Runnable() {
                @Override
                public void run() {
                    doReset();
                }
            });
        }
    }

    private void doReset() {
        GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        mTextureId = ImgTexFrame.NO_TEXTURE;
        if (mYUVLoader != null) {
            mYUVLoader.reset();
        }
        mImgTexFormat = null;
    }

    public void release() {
        disconnect(true);
        mGLRender.removeListener(mGLRenderListener);
        reset();
        mYUVLoader = null;
    }

    private void calRotateMatrix(float[] mat, float scaleX, int degrees) {
        degrees %= 360;
        if (degrees % 90 != 0) {
            return;
        }
        Matrix.setIdentityM(mat, 0);
        switch (degrees) {
            case 0:
                Matrix.translateM(mat, 0, 0, 1, 0);
                break;
            case 90:
                Matrix.translateM(mat, 0, 0, 0, 0);
                break;
            case 180:
                Matrix.translateM(mat, 0, 1, 0, 0);
                break;
            case 270:
                Matrix.translateM(mat, 0, 1, 1, 0);
                break;
        }
        Matrix.rotateM(mat, 0, degrees, 0, 0, 1);
        Matrix.scaleM(mat, 0, scaleX, -1, 1);
    }

    private void doUpdateFrame(Bitmap img, int rotate, long pts, boolean recycle) {
        if (img == null || img.isRecycled()) {
            if (mImgTexFormat == null) {
                return;
            }
            if (mTextureId != ImgTexFrame.NO_TEXTURE) {
                GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
                mTextureId = ImgTexFrame.NO_TEXTURE;
            }
            ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, ImgTexFrame.NO_TEXTURE, null, 0);
            onFrameAvailable(frame);
            return;
        }

        int width = img.getWidth();
        int height = img.getHeight();
        if (rotate % 180 != 0) {
            width = img.getHeight();
            height = img.getWidth();
        }

        boolean formatChanged = false;
        if (mImgTexFormat == null ||
                mImgTexFormat.width != width ||
                mImgTexFormat.height != height) {
            mImgTexFormat = new ImgTexFormat(ImgTexFormat.COLOR_RGBA, width, height);
            if (mTextureId != ImgTexFrame.NO_TEXTURE) {
                GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
                mTextureId = ImgTexFrame.NO_TEXTURE;
            }
            formatChanged = true;
        }

        mTextureId = GlUtil.loadTexture(img, mTextureId);
        if (recycle) {
            img.recycle();
        }
        if (mTextureId == ImgTexFrame.NO_TEXTURE) {
            return;
        }

        if (formatChanged) {
            onFormatChanged(mImgTexFormat);
        }

        calRotateMatrix(mTexMatrix, 1, rotate);
        ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, mTextureId, mTexMatrix, pts);
        onFrameAvailable(frame);
    }

    private void doUpdateFrame(ByteBuffer buf, int stride, int srcWidth, int srcHeight,
                               int rotate, long pts) {
        if (buf == null || buf.limit() == 0) {
            if (mImgTexFormat == null) {
                return;
            }
            if (mTextureId != ImgTexFrame.NO_TEXTURE) {
                GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
                mTextureId = ImgTexFrame.NO_TEXTURE;
            }
            ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, ImgTexFrame.NO_TEXTURE, null, 0);
            onFrameAvailable(frame);
            return;
        }

        int width = srcWidth;
        int height = srcHeight;
        if (rotate % 180 != 0) {
            width = srcHeight;
            height = srcWidth;
        }

        boolean formatChanged = false;
        if (mImgTexFormat == null ||
                mImgTexFormat.width != width ||
                mImgTexFormat.height != height) {
            mImgTexFormat = new ImgTexFormat(ImgTexFormat.COLOR_RGBA, width, height);
            if (mTextureId != ImgTexFrame.NO_TEXTURE) {
                GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
                mTextureId = ImgTexFrame.NO_TEXTURE;
            }
            formatChanged = true;
        }

        int sw = stride / 4;
        mTextureId = GlUtil.loadTexture(buf, sw, srcHeight, mTextureId);
        if (mTextureId == ImgTexFrame.NO_TEXTURE) {
            return;
        }

        if (formatChanged) {
            onFormatChanged(mImgTexFormat);
        }

        // update matrix
        float fx = (float) srcWidth / (float) sw;
        calRotateMatrix(mTexMatrix, fx, rotate);
        ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, mTextureId, mTexMatrix, pts);
        onFrameAvailable(frame);
    }

    private void doUpdateYUVFrame(ByteBuffer buf, int[] strides, int srcWidth, int srcHeight,
                                  int rotate, long pts) {
        if (buf == null || buf.limit() == 0) {
            if (mImgTexFormat == null) {
                return;
            }
            if (mYUVLoader != null) {
                mYUVLoader.reset();
            }
            ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, ImgTexFrame.NO_TEXTURE, null, 0);
            onFrameAvailable(frame);
            return;
        }

        int width = srcWidth;
        int height = srcHeight;
        if (rotate % 180 != 0) {
            width = srcHeight;
            height = srcWidth;
        }

        boolean formatChanged = false;
        if (mImgTexFormat == null ||
                mImgTexFormat.width != width ||
                mImgTexFormat.height != height) {
            mImgTexFormat = new ImgTexFormat(ImgTexFormat.COLOR_RGBA, width, height);
            if (mYUVLoader != null) {
                mYUVLoader.reset();
            }
            formatChanged = true;
        }

        if (mYUVLoader == null) {
            mYUVLoader = new YUVLoader(mGLRender);
        }

        int textureId = mYUVLoader.loadTexture(buf, srcWidth, srcHeight, strides);
        if (textureId == ImgTexFrame.NO_TEXTURE) {
            return;
        }

        if (formatChanged) {
            onFormatChanged(mImgTexFormat);
        }

        // update matrix
        calRotateMatrix(mTexMatrix, 1, rotate);
        ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, textureId, mTexMatrix, pts);
        onFrameAvailable(frame);
    }

    private void doRepeatFrame(long pts) {
        if (mTextureId == ImgTexFrame.NO_TEXTURE || mImgTexFormat == null) {
            return;
        }
        ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, mTextureId, mTexMatrix, pts);
        onFrameAvailable(frame);
    }

    private GLRender.GLRenderListener mGLRenderListener = new GLRender.GLRenderListener() {
        @Override
        public void onReady() {
            mImgTexFormat = null;
            mTextureId = ImgTexFrame.NO_TEXTURE;
            if (mYUVLoader != null) {
                mYUVLoader.reset();
            }
        }

        @Override
        public void onSizeChanged(int width, int height) {
        }

        @Override
        public void onDrawFrame() {
        }

        @Override
        public void onReleased() {
            if (mSyncMode) {
                mSig.open();
            }
        }
    };
}
