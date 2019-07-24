package com.component.mediaengine.capture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.View;

import com.component.mediaengine.framework.ImgTexFrame;
import com.component.mediaengine.framework.SrcPin;
import com.component.mediaengine.util.gles.GLRender;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Capture texture from picture.
 */

public class ViewCapture {
    public final String TAG = "ViewCapture";

    private ImgTexSrcPin mImgTexSrcPin;
    private float mUpdateFps = 15.0f;
    private int mTargetWidth;
    private int mTargetHeight;
    private Timer mTimer;
    private View mView;

    private Bitmap mBitmap;
    private Canvas mCanvas;

    // cache buffer to avoid video splash
    private Object BUFFER_LOCK = new Object();
    private ByteBuffer mByteBuffer;

    // view draw/update in sync mode
    private ConditionVariable mSig = new ConditionVariable();
    private Bitmap mOutBitmap;

    public ViewCapture(GLRender glRender) {
        mImgTexSrcPin = new ImgTexSrcPin(glRender);
        mImgTexSrcPin.setUseSyncMode(true);
    }

    public SrcPin<ImgTexFrame> getSrcPin() {
        return mImgTexSrcPin;
    }

    public void setTargetResolution(int width, int height) {
        mTargetWidth = width;
        mTargetHeight = height;
    }

    public int getTargetWidth() {
        return mTargetWidth;
    }

    public int getTargetHeight() {
        return mTargetHeight;
    }

    public float getUpdateFps() {
        return mUpdateFps;
    }

    public void setUpdateFps(float fps) {
        mUpdateFps = fps;
    }

    public void start(View view) {
        if (view == null) {
            return;
        }
        mView = view;
        if (mUpdateFps > 0) {
            long period = (long) (1000.f / mUpdateFps);
            mTimer = new Timer("ViewRepeat");
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mSig.close();
                    mOutBitmap = null;
                    mView.post(new Runnable() {
                        @Override
                        public void run() {
                            mOutBitmap = getBitmapFromView(mView);
                            mSig.open();
                        }
                    });
                    mSig.block();
                    updateFrame(mOutBitmap);
                }
            }, 40, period);
        }
    }

    public void stop() {
        mSig.open();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mImgTexSrcPin.updateFrame(null, false);

        // clean
        synchronized (BUFFER_LOCK) {
            mByteBuffer = null;
        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    public void release() {
        stop();
        mImgTexSrcPin.release();
    }

    private Bitmap getBitmapFromView(View view) {
        if (view == null || view.getWidth() == 0 || view.getHeight() == 0) {
            return null;
        }
        if (mBitmap == null || mBitmap.isRecycled()) {
            int w = view.getWidth();
            int h = view.getHeight();
            if (mTargetWidth > 0 || mTargetHeight > 0) {
                if (mTargetWidth == 0) {
                    mTargetWidth = mTargetHeight * w / h;
                }
                if (mTargetHeight == 0) {
                    mTargetHeight = mTargetWidth * h / w;
                }
                w = mTargetWidth;
                h = mTargetHeight;
            }
            float scaleX = ((float) w) / view.getWidth();
            float scaleY = ((float) h) / view.getHeight();
            Log.d(TAG, "init bitmap " + w + "x" + h + " scale: " + scaleX + "x" + scaleY);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mCanvas.scale(scaleX, scaleY);
        }
        mBitmap.eraseColor(Color.TRANSPARENT);
        view.draw(mCanvas);
        return mBitmap;
    }

    private void updateFrame(Bitmap bitmap) {
        if (bitmap != null) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int stride = w * 4;
            synchronized (BUFFER_LOCK) {
                if (mByteBuffer == null) {
                    mByteBuffer = ByteBuffer.allocate(stride * h);
                }
                mByteBuffer.clear();
                bitmap.copyPixelsToBuffer(mByteBuffer);
                mByteBuffer.flip();
                mImgTexSrcPin.updateFrame(mByteBuffer, stride, w, h);
            }
        } else {
            mImgTexSrcPin.updateFrame(null, false);
        }
    }
}
