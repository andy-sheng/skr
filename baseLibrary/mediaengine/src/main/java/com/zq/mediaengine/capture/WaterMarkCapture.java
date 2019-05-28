package com.zq.mediaengine.capture;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.zq.mediaengine.util.BitmapLoader;
import com.zq.mediaengine.util.TextGraphicUtils;
import com.zq.mediaengine.util.gles.GLRender;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Watermark capture module.
 */
public class WaterMarkCapture {
    private final static String TAG = "WaterMarkCapture";
    private final static int MAX_LOGO_LEN = 512;

    public ImgTexSrcPin mLogoTexSrcPin;
    public ImgBufSrcPin mLogoBufSrcPin;
    public ImgTexSrcPin mTimeTexSrcPin;
    public ImgBufSrcPin mTimeBufSrcPin;

    private Timer mWmiTimer;
    private GLRender mGLRender;

    // only used for BufSrcPin
    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;
    private int mTargetWidth = 0;
    private int mTargetHeight = 0;
    private Runnable mShowLogoRunnable;

    private int mTimeColor;
    private String mTimeDateFormat;
    private float mTimeWidth;
    private float mTimeHeight;

    private Bitmap mLogoBitmap;
    private final Object mLogoLock = new Object();

    public WaterMarkCapture(GLRender glRender) {
        mLogoTexSrcPin = new ImgTexSrcPin(glRender);
        mLogoBufSrcPin = new ImgBufSrcPin();
        mTimeTexSrcPin = new ImgTexSrcPin(glRender);
        mTimeBufSrcPin = new ImgBufSrcPin();

        // use ImgTexSrcPin in sync mode to avoid bitmap recycled issue.
        mLogoTexSrcPin.setUseSyncMode(true);
        mTimeTexSrcPin.setUseSyncMode(true);

        mGLRender = glRender;
        mGLRender.addListener(mGLReadyListener);
    }

    public ImgTexSrcPin getLogoTexSrcPin() {
        return mLogoTexSrcPin;
    }

    public ImgBufSrcPin getLogoBufSrcPin() {
        return mLogoBufSrcPin;
    }

    public ImgTexSrcPin getTimeTexSrcPin() {
        return mTimeTexSrcPin;
    }

    public ImgBufSrcPin getTimeBufSrcPin() {
        return mTimeBufSrcPin;
    }

    /**
     * Set the actual size of preview view, ie, the size of GLSurfaceView or TextureView
     * set to preview camera data.
     *
     * @param w width
     * @param h height
     */
    public void setPreviewSize(int w, int h) {
        mPreviewWidth = w;
        mPreviewHeight = h;
        if (isOutSizeReady() && mShowLogoRunnable != null) {
            mGLRender.queueEvent(mShowLogoRunnable);
            mShowLogoRunnable = null;
        }
    }

    /**
     * Set the target streaming resolution.
     *
     * @param w width
     * @param h height
     */
    public void setTargetSize(int w, int h) {
        mTargetWidth = w;
        mTargetHeight = h;
        if (isOutSizeReady() && mShowLogoRunnable != null) {
            mGLRender.queueEvent(mShowLogoRunnable);
            mShowLogoRunnable = null;
        }
    }

    /**
     * Decode logo image and transfer image frame to the connected module.
     *
     * @param context app context
     * @param uri     logo uri, support file:// and assets://
     * @param fw      relative width, should between 0~1,
     *                if set 0, width would be calculated by fh and image radio
     * @param fh      relative height, should between 0~1,
     *                if set 0, height would be calculated by fw and image radio
     */
    public void showLogo(final Context context, final String uri, final float fw, final float fh) {
        if (TextUtils.isEmpty(uri)) {
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                doShowLogo(context, uri, fw, fh);
            }
        };

        if (isOutSizeReady()) {
            mGLRender.queueEvent(runnable);
        } else {
            mShowLogoRunnable = runnable;
        }
    }

    /**
     * Transfer logo image frame to the connected module.
     *
     * @param bitmap logo to show, must not be recycled by caller
     * @param fw     relative width, should between 0~1,
     *               if set 0, width would be calculated by fh and image radio
     * @param fh     relative height, should between 0~1,
     *               if set 0, height would be calculated by fw and image radio
     */
    public void showLogo(final Bitmap bitmap, final float fw, final float fh) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        synchronized (mLogoLock) {
            if (mLogoBitmap != null && mLogoBitmap != bitmap) {
                mLogoBitmap.recycle();
            }
            mLogoBitmap = bitmap;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                doShowLogo(fw, fh);
            }
        };

        if (isOutSizeReady()) {
            mGLRender.queueEvent(runnable);
        } else {
            mShowLogoRunnable = runnable;
        }
    }

    /**
     * Clear logo image.
     */
    public void hideLogo() {
        synchronized (mLogoLock) {
            if (mLogoBitmap != null) {
                // clear logo params
                mLogoBitmap.recycle();
                mLogoBitmap = null;
            }
        }

        mGLRender.queueEvent(new Runnable() {
            @Override
            public void run() {
                mLogoBufSrcPin.updateFrame(null, false);
                mLogoTexSrcPin.updateFrame(null, false);
            }
        });

        if (mShowLogoRunnable != null) {
            mShowLogoRunnable = null;
        }
    }

    /**
     * Start to generate time watermark every seconds, and transfer image to the connected module.
     *
     * @param color      color in ARGB
     * @param dateFormat time format to show, default "yyyy-MM-dd HH:mm:ss"
     * @param fw         relative width, should between 0~1,
     *                   if set 0, width would be calculated by fh and image radio
     * @param fh         relative height, should between 0~1,
     *                   if set 0, height would be calculated by fw and image radio
     */
    public void showTime(final int color, final String dateFormat, final float fw, final float fh) {
        if (mWmiTimer != null) {
            return;
        }

        // init time params
        mTimeColor = color;
        mTimeDateFormat = dateFormat;
        mTimeWidth = fw;
        mTimeHeight = fh;

        mWmiTimer = new Timer();
        mWmiTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                doShowTime(color, dateFormat, fw, fh);
            }
        }, 0, 1000);
    }

    /**
     * Clear time image.
     */
    public void hideTime() {
        if (mWmiTimer != null) {
            mWmiTimer.cancel();
            mWmiTimer = null;
        }
        mTimeBufSrcPin.updateFrame(null, false);
        mTimeTexSrcPin.updateFrame(null, false);
    }

    public void release() {
        if (mWmiTimer != null) {
            mWmiTimer.cancel();
            mWmiTimer = null;
        }
        synchronized (mLogoLock) {
            if (mLogoBitmap != null) {
                mLogoBitmap.recycle();
                mLogoBitmap = null;
            }
        }
        mLogoTexSrcPin.release();
        mLogoBufSrcPin.release();
        mTimeTexSrcPin.release();
        mTimeBufSrcPin.release();
        mGLRender.removeListener(mGLReadyListener);
    }

    private boolean isOutSizeReady() {
        return mPreviewWidth != 0 && mPreviewHeight != 0 &&
                mTargetWidth != 0 && mTargetHeight != 0;
    }

    private void doShowLogo(Context context, String uri, float fw, float fh) {
        int maxWidth = (int) (fw * mPreviewWidth) / 2 * 2;
        int maxHeight = (int) (fh * mPreviewHeight) / 2 * 2;
        if (maxWidth == 0 && maxHeight == 0) {
            maxWidth = MAX_LOGO_LEN;
            maxHeight = MAX_LOGO_LEN;
        }
        synchronized (mLogoLock) {
            if (mLogoBitmap != null) {
                mLogoBitmap.recycle();
            }
            mLogoBitmap = BitmapLoader.loadBitmap(context, uri, maxWidth, maxHeight);
        }
        doShowLogo(fw, fh);
    }

    private void doShowLogo(float fw, float fh) {
        synchronized (mLogoLock) {
            if (mLogoBitmap != null) {
                updateImgBufFrame(mLogoBitmap, mLogoBufSrcPin, fw, fh);
                updateImgTexFrame(mLogoBitmap, mLogoTexSrcPin, false);
            }
        }
    }

    private void doShowTime(int color, String dateFormat, float fw, float fh) {
        if (TextUtils.isEmpty(dateFormat)) {
            dateFormat = "yyyy-MM-dd HH:mm:ss";
        }
        DateFormat df = new SimpleDateFormat(dateFormat, Locale.getDefault());
        String tm = df.format(new Date());
        Bitmap timeBitmap = TextGraphicUtils.getTextBitmap(tm, color, 32);
        updateImgBufFrame(timeBitmap, mTimeBufSrcPin, fw, fh);
        updateImgTexFrame(timeBitmap, mTimeTexSrcPin, true);
    }

    private void updateImgBufFrame(Bitmap img, ImgBufSrcPin pin, float fw, float fh) {
        if (pin.isConnected() && img != null) {
            int width = (int) (fw * mTargetWidth) / 2 * 2;
            int height = (int) (fh * mTargetHeight) / 2 * 2;
            if (width == 0 && height == 0) {
                return;
            }
            if (width == 0) {
                width = height * img.getWidth() / img.getHeight();
                width = width / 2 * 2;
            } else if (height == 0) {
                height = width * img.getHeight() / img.getWidth();
                height = height / 2 * 2;
            }
            Bitmap b = img;
            boolean recycle = false;
            if (width != img.getWidth() || height != img.getHeight()) {
                b = Bitmap.createScaledBitmap(img, width, height, true);
                recycle = true;
            }
            pin.updateFrame(b, recycle);
        }
    }

    private void updateImgTexFrame(Bitmap img, ImgTexSrcPin pin, boolean recycle) {
        if (pin.isConnected()) {
            pin.updateFrame(img, recycle);
        } else if (recycle) {
            img.recycle();
        }
    }

    private GLRender.OnReadyListener mGLReadyListener = new GLRender.OnReadyListener() {
        @Override
        public void onReady() {
            mGLRender.queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mLogoLock) {
                        if (mLogoBitmap != null) {
                            updateImgTexFrame(mLogoBitmap, mLogoTexSrcPin, false);
                        }
                    }
                    if (mWmiTimer != null) {
                        doShowTime(mTimeColor, mTimeDateFormat, mTimeWidth, mTimeHeight);
                    }
                }
            });
        }
    };
}
