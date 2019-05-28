package com.zq.mediaengine.filter.imgtex;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.gles.FboManager;
import com.zq.mediaengine.util.gles.GLProgramLoadException;
import com.zq.mediaengine.util.gles.GLRender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

/**
 * The base class of gpu filters.
 */
abstract public class ImgTexFilterBase extends ImgFilterBase {
    private static final String TAG = "ImgTexFilterBase";

    public static final int ERROR_LOAD_PROGRAM_FAILED = -1;
    public static final int ERROR_UNKNOWN = -2;

    /**
     * Input pins
     */
    private List<SinkPin<ImgTexFrame>> mSinkPins;
    /**
     * Output pin
     */
    private SrcPin<ImgTexFrame> mSrcPin;

    private ImgTexFrame[] mInputFrames;
    private boolean[] mUnRefFbos;
    private int[] mViewPort = new int[4];

    protected boolean mReuseFbo = true;
    protected GLRender mGLRender;
    protected boolean mInited;
    protected int mOutTexture = ImgTexFrame.NO_TEXTURE;
    private ImgTexFormat mLastOutFormat;

    protected Handler mMainHandler;

    //screenshot
    private boolean mIsRequestScreenShot;
    private float mScaleFactor;
    private GLRender.ScreenShotListener mScreenShotListener;
    private Thread mScreenShotThread = null;

    public ImgTexFilterBase(GLRender glRender) {
        mSinkPins = new LinkedList<>();
        for (int i = 0; i < getSinkPinNum(); i++) {
            mSinkPins.add(new ImgTexFilterSinkPin(i));
        }
        mSrcPin = new ImgTexFilterSrcPin();
        mInputFrames = new ImgTexFrame[getSinkPinNum()];
        mUnRefFbos = new boolean[getSinkPinNum()];
        mMainHandler = new Handler(Looper.getMainLooper());

        mGLRender = glRender;
        mGLRender.addListener(mGLReadyListener);
    }

    public void setGLRender(GLRender glRender) {
        if (mGLRender != null) {
            mGLRender.removeListener(mGLReadyListener);
        }
        mGLRender = glRender;
        mGLRender.addListener(mGLReadyListener);
    }

    /**
     * Get output pin format, child class must return valid value after
     * onFormatChanged(mMainSinkPinIndex, ...) called
     *
     * @return request output pin format or null
     */
    abstract protected ImgTexFormat getSrcPinFormat();

    /**
     * Get sink pin by index
     *
     * @param index index
     * @return SinPin object or null
     */
    @Override
    public SinkPin<ImgTexFrame> getSinkPin(int index) {
        return mSinkPins.get(index);
    }

    /**
     * Get source pin
     *
     * @return SrcPin object or null
     */
    @Override
    public SrcPin<ImgTexFrame> getSrcPin() {
        return mSrcPin;
    }

    protected boolean isReuseFbo() {
        return mReuseFbo;
    }

    public void setReuseFbo(boolean reuse) {
        mReuseFbo = reuse;
    }

    @Override
    public void release() {
        mSrcPin.disconnect(true);
        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                onRelease();
            }
        });
        mGLRender.removeListener(mGLReadyListener);
        if (mScreenShotThread != null && mScreenShotThread.isAlive()) {
            mScreenShotThread.interrupt();
            mScreenShotThread = null;
        }
    }

    /**
     * OpenGLES context ready, all gl related resource need to be reinitialized.
     */
    protected void onGLContextReady() {
    }

    /**
     * On input pin format changed
     *
     * @param inIdx  input pin index
     * @param format input pin format
     */
    abstract protected void onFormatChanged(final int inIdx, final ImgTexFormat format);

    protected void onDisconnect(boolean recursive){

    }

    /**
     * On draw
     *
     * @param frames input frames
     */
    abstract protected void onDraw(final ImgTexFrame[] frames);

    protected void onRelease() {
        // unref previous fbo if needed
        for (ImgTexFrame frame : mInputFrames) {
            if (frame != null && frame.isRefCounted()) {
                frame.unref();
            }
        }
        // unref current fbo
        if (!isReuseFbo()) {
            mGLRender.getFboManager().unlock(mOutTexture);
            mOutTexture = ImgTexFrame.NO_TEXTURE;
        }
    }

    protected void sendError(final int errno) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mErrorListener != null) {
                    mErrorListener.onError(ImgTexFilterBase.this, errno);
                }
            }
        });
    }

    private class ImgTexFilterSinkPin extends SinkPin<ImgTexFrame> {
        private int mIndex;

        public ImgTexFilterSinkPin(int index) {
            mIndex = index;
        }

        @Override
        public synchronized void onConnected(SrcPin<ImgTexFrame> srcPin) {
            super.onConnected(srcPin);
            mUnRefFbos[mIndex] = srcPin instanceof ImgTexFilterSrcPin;
        }

        @Override
        public void onFormatChanged(Object format) {
            ImgTexFilterBase.this.onFormatChanged(mIndex, (ImgTexFormat) format);
            if (mIndex == mMainSinkPinIndex) {
                final ImgTexFormat outFormat = ImgTexFilterBase.this.getSrcPinFormat();
                mSrcPin.onFormatChanged(outFormat);
                mLastOutFormat = outFormat;
            }
        }

        @Override
        public void onFrameAvailable(ImgTexFrame frame) {
            if (mIndex != mMainSinkPinIndex) {
                // unref previous fbo if needed
                if (mInputFrames[mIndex] != null && mInputFrames[mIndex].isRefCounted()) {
                    mInputFrames[mIndex].unref();
                }
                // ref current fbo if needed
                if (!mUnRefFbos[mIndex] && frame.isRefCounted()) {
                    frame.ref();
                }
                mInputFrames[mIndex] = frame;
            } else {
                mInputFrames[mIndex] = frame;
                render(frame);
                mInputFrames[mIndex] = null;
            }
        }

        @Override
        public synchronized void onDisconnect(boolean recursive) {
            if (mIndex == mMainSinkPinIndex) {
                if (recursive) {
                    release();
                } else {
                    ImgTexFilterBase.this.onDisconnect(recursive);
                }
            } else {
                mGLRender.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        if (mInputFrames[mIndex] != null && mInputFrames[mIndex].isRefCounted()) {
                            mInputFrames[mIndex].unref();
                        }
                        mInputFrames[mIndex] = null;
                    }
                });
            }
        }

        private void render(ImgTexFrame frame) {
            if (!mSrcPin.isConnected()) {
                return;
            }

            final ImgTexFormat outFormat = ImgTexFilterBase.this.getSrcPinFormat();
            if (outFormat == null) {
                return;
            }

            if (mLastOutFormat != null && (mLastOutFormat.width != outFormat.width ||
                    mLastOutFormat.height != outFormat.height)) {
                if (mIndex == mMainSinkPinIndex) {
                    mSrcPin.onFormatChanged(outFormat);
                }
            }
            mLastOutFormat = outFormat;

            if (mOutTexture == ImgTexFrame.NO_TEXTURE) {
                mOutTexture = mGLRender.getFboManager().getTextureAndLock(outFormat.width,
                        outFormat.height);
            }
            int outFrameBuffer = mGLRender.getFboManager().getFramebuffer(mOutTexture);

            GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, mViewPort, 0);
            GLES20.glViewport(0, 0, outFormat.width, outFormat.height);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outFrameBuffer);
            try {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                ImgTexFilterBase.this.onDraw(mInputFrames);

                if (mIsRequestScreenShot) {
                    saveFrame(outFormat.width, outFormat.height);
                    mIsRequestScreenShot = false;
                }
            } catch (Exception e) {
                if (isReuseFbo()) {
                    mGLRender.getFboManager().unlock(mOutTexture);
                    mOutTexture = ImgTexFrame.NO_TEXTURE;
                }
                if (e instanceof GLProgramLoadException) {
                    sendError(ERROR_LOAD_PROGRAM_FAILED);
                } else {
                    sendError(ERROR_UNKNOWN);
                }
                Log.e(TAG, "Draw frame error!");
                e.printStackTrace();
                return;
            } finally {
                // we can only unref fbo if SrcPin is implemented by ImgTexFilterBase
                if (mUnRefFbos[mIndex]) {
                    frame.unref();
                }
                // reset gl state
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                GLES20.glViewport(mViewPort[0], mViewPort[1], mViewPort[2], mViewPort[3]);
            }
            
            // send to the next module
            FboManager fboManager = isReuseFbo() ? mGLRender.getFboManager() : null;
            ImgTexFrame outFrame = new ImgTexFrame(outFormat, fboManager, mOutTexture,
                    null, frame.pts);
            outFrame.flags = frame.flags;
            mSrcPin.onFrameAvailable(outFrame);
            if (isReuseFbo()) {
                mOutTexture = ImgTexFrame.NO_TEXTURE;
            }
        }
    }

    private class ImgTexFilterSrcPin extends SrcPin<ImgTexFrame> {

        @Override
        public synchronized void onFrameAvailable(ImgTexFrame frame) {
            if (frame == null) {
                return;
            }

            if (isReuseFbo()) {
                // ref with sink pin numbers
                for (int i = 0; i < sinkPins.size(); i++) {
                    frame.ref();
                }
                // unref once here
                frame.unref();
            }

            for (SinkPin<ImgTexFrame> sinkPin : sinkPins) {
                try {
                    if (!isFormatChangedMap.get(sinkPin)) {
                        sinkPin.onFormatChanged(format);
                        isFormatChangedMap.put(sinkPin, true);
                    }
                    sinkPin.onFrameAvailable(frame);
                } catch (Exception e) {
                    Log.e(TAG, ImgTexFilterBase.this.getClass().getSimpleName());
                    e.printStackTrace();
                } finally {
                    if (isReuseFbo()) {
                        // if sink pin is not implement in ImgTexFilterBase, we must unref here
                        if (!(sinkPin instanceof ImgTexFilterSinkPin)) {
                            frame.unref();
                        }
                    }
                }
            }
        }
    }

    private GLRender.OnReadyListener mGLReadyListener = new GLRender.OnReadyListener() {
        @Override
        public void onReady() {
            mInited = false;
            mOutTexture = ImgTexFrame.NO_TEXTURE;
            for (int i = 0; i < mInputFrames.length; i++) {
                mInputFrames[i] = null;
            }
            ImgTexFilterBase.this.onGLContextReady();
        }
    };

    private void setScaleFactor(float scaleFactor) {
        scaleFactor = Math.max(0.0f, scaleFactor);
        scaleFactor = Math.min(scaleFactor, 1.0f);
        this.mScaleFactor = scaleFactor;
    }

    public void requestScreenShot(GLRender.ScreenShotListener screenShotListener) {
        requestScreenShot(1.0f, screenShotListener);
    }

    public void requestScreenShot(float scaleFactor, GLRender.ScreenShotListener screenShotListener) {
        setScaleFactor(scaleFactor);
        mIsRequestScreenShot = true;
        mScreenShotListener = screenShotListener;
    }

    private void saveFrame(final int width, final int height) {
        final long startTime = System.currentTimeMillis();

        final ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        buf.rewind();

        mScreenShotThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap fullBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                fullBitmap.copyPixelsFromBuffer(buf);
                Matrix m = new Matrix();
                m.preScale(1, -1);
                if (mScaleFactor != 1.0) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                            fullBitmap, (int) (width * mScaleFactor),
                            (int) (height * mScaleFactor), true);
                    Bitmap flippedScaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                            scaledBitmap.getWidth(), scaledBitmap.getHeight(), m, true);
                    if (mScreenShotListener != null)
                        mScreenShotListener.onBitmapAvailable(flippedScaledBitmap);
                    scaledBitmap.recycle();
                    flippedScaledBitmap.recycle();
                } else {
                    Bitmap flippedBitmap = Bitmap.createBitmap(fullBitmap, 0, 0,
                            width, height, m, true);
                    if (mScreenShotListener != null)
                        mScreenShotListener.onBitmapAvailable(flippedBitmap);
                    flippedBitmap.recycle();
                }
                fullBitmap.recycle();
                Log.d(TAG, "Saved " + width + "x" + height + " frame in "
                        + (System.currentTimeMillis() - startTime) + " ms");
            }
        });
        mScreenShotThread.start();
    }
}
