package com.component.mediaengine.capture;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.ConditionVariable;

import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.framework.ImgTexFrame;
import com.component.mediaengine.framework.SrcPin;
import com.component.mediaengine.util.gles.GLRender;
import com.component.mediaengine.util.gles.GlUtil;
import com.component.mediaengine.util.gles.TexTransformUtil;
import com.component.mediaengine.util.gles.YUVLoader;

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
        if (glRender == null) {
            throw new IllegalArgumentException("glRender should not be null!");
        }
        mTexMatrix = new float[16];

        // keep eye on if glcontext recreated
        mGLRender = glRender;
        mGLRender.addListener(mOnReadyListener);
        mGLRender.addListener(mOnReleasedListener);
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
            if (mSyncMode) {
                mSig.close();
            }
            if (mGLRender.getState() != GLRender.STATE_RELEASED) {
                mGLRender.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        doUpdateFrame(img, rotate, pts, recycle);
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
            if (mGLRender.getState() != GLRender.STATE_RELEASED) {
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
            if (mGLRender.getState() != GLRender.STATE_RELEASED) {
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

            if (mGLRender.getState() != GLRender.STATE_RELEASED) {
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
        if (mYUVLoader == null) {
            if (mTextureId != ImgTexFrame.NO_TEXTURE) {
                GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
                mTextureId = ImgTexFrame.NO_TEXTURE;
            }
        } else {
            mYUVLoader.reset();
            mTextureId = ImgTexFrame.NO_TEXTURE;
        }
        mImgTexFormat = null;
    }

    public void release() {
        disconnect(true);
        mGLRender.removeListener(mOnReadyListener);
        mGLRender.removeListener(mOnReleasedListener);
        reset();
        mYUVLoader = null;
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

        TexTransformUtil.calTransformMatrix(mTexMatrix, 1, 1, rotate);
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
        TexTransformUtil.calTransformMatrix(mTexMatrix, fx, 1, rotate);
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
            mTextureId = ImgTexFrame.NO_TEXTURE;

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
            mTextureId = ImgTexFrame.NO_TEXTURE;
            formatChanged = true;
        }

        if (mYUVLoader == null) {
            mYUVLoader = new YUVLoader(mGLRender);
        }

        mTextureId = mYUVLoader.loadTexture(buf, srcWidth, srcHeight, strides);
        if (mTextureId == ImgTexFrame.NO_TEXTURE) {
            return;
        }

        if (formatChanged) {
            onFormatChanged(mImgTexFormat);
        }

        // update matrix
        TexTransformUtil.calTransformMatrix(mTexMatrix, 1, 1, rotate);
        ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, mTextureId, mTexMatrix, pts);
        onFrameAvailable(frame);
    }

    private void doRepeatFrame(long pts) {
        if (mTextureId == ImgTexFrame.NO_TEXTURE || mImgTexFormat == null) {
            return;
        }
        ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, mTextureId, mTexMatrix, pts);
        onFrameAvailable(frame);
    }

    private GLRender.OnReadyListener mOnReadyListener = new GLRender.OnReadyListener() {
        @Override
        public void onReady() {
            mImgTexFormat = null;
            mTextureId = ImgTexFrame.NO_TEXTURE;
            if (mYUVLoader != null) {
                mYUVLoader.reset();
            }
        }
    };

    private GLRender.OnReleasedListener mOnReleasedListener = new GLRender.OnReleasedListener() {
        @Override
        public void onReleased() {
            if (mSyncMode) {
                mSig.open();
            }
        }
    };
}
