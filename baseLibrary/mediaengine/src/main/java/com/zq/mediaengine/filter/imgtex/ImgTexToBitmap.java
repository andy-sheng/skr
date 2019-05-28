package com.zq.mediaengine.filter.imgtex;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.util.gles.GLRender;

import java.nio.ByteBuffer;

/**
 * Created by sujia on 2017/1/6.
 *
 * @hide
 */

public class ImgTexToBitmap extends ImgTexToBuf {
    private static final String TAG = "ImgTexToBitmap";

    private boolean mIsRequestScreenShot = false;
    private float mScaleFactor = 1.0f;
    private GLRender.ScreenShotListener mScreenShotListener = null;
    private Thread mScreenShotThread = null;

    public ImgTexToBitmap(GLRender glRender) {
        super(glRender);
        setOutputColorFormat(AVConst.PIX_FMT_RGBA);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void doImageAvailable(final ImageReader reader) {
        final Image img = reader.acquireNextImage();

        if (mIsRequestScreenShot) {
            if (img != null) {
                Image.Plane[] planes = img.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();

                if (buffer != null) {
                    final int width = img.getWidth();
                    final int height = img.getHeight();
                    final int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    final int rowPadding = rowStride - pixelStride * width;

                    mScreenShotThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // create bitmap
                            Bitmap bitmap = Bitmap.createBitmap(/*width*/width + rowPadding / pixelStride,
                                    height, Bitmap.Config.ARGB_8888);
                            bitmap.copyPixelsFromBuffer(buffer);

                            if (mScaleFactor != 1.0) {
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                                        bitmap, (int) (width * mScaleFactor),
                                        (int) (height * mScaleFactor), true);
                                if (mScreenShotListener != null) {
                                    mScreenShotListener.onBitmapAvailable(scaledBitmap);
                                }
                                if (scaledBitmap != null) {
                                    scaledBitmap.recycle();
                                }
                            } else {
                                if (mScreenShotListener != null) {
                                    mScreenShotListener.onBitmapAvailable(bitmap);
                                }
                                if (bitmap != null) {
                                    bitmap.recycle();
                                }
                            }

                        }
                    });
                    mScreenShotThread.start();
                }
            }

            mIsRequestScreenShot = false;
            mStarted = false;
        }

        img.close();
    }

    public SinkPin<ImgTexFrame> getSinkPin() {
        return mSinkPin;
    }

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
        mStarted = true;
        mIsRequestScreenShot = true;
        mScreenShotListener = screenShotListener;
    }

    public void release() {
        if (mScreenShotThread != null && mScreenShotThread.isAlive()) {
            mScreenShotThread.interrupt();
            mScreenShotThread = null;
        }
    }
}
