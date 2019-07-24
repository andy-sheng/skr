package com.zq.mediaengine.capture;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.BitmapLoader;
import com.zq.mediaengine.util.gles.GLRender;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Capture texture from picture.
 */

public class ImageCapture {
    public final String TAG = "ImageCapture";

    private ImgTexSrcPin mImgTexSrcPin;
    private float mRepeatFps = 0.0f;
    private Timer mTimer;

    public ImageCapture(GLRender glRender) {
        mImgTexSrcPin = new ImgTexSrcPin(glRender);
    }

    public SrcPin<ImgTexFrame> getSrcPin() {
        return mImgTexSrcPin;
    }

    public float getRepeatFps() {
        return mRepeatFps;
    }

    public void setRepeatFps(float fps) {
        mRepeatFps = fps;
    }

    public void start(Context context, String uri) {
        Bitmap bitmap = BitmapLoader.loadBitmap(context, uri);
        start(bitmap, true);
    }

    public void start(Bitmap bitmap, boolean recycle) {
        if (bitmap == null || bitmap.isRecycled()) {
            Log.e(TAG, "invalid bitmap, start failed!");
            return;
        }
        mImgTexSrcPin.updateFrame(bitmap, recycle);
        if (mRepeatFps > 0) {
            long period = (long) (1000.f/mRepeatFps);
            mTimer = new Timer("ImageRepeat");
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mImgTexSrcPin.repeatFrame();
                }
            }, period, period);
        }
    }

    public void stop() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mImgTexSrcPin.updateFrame(null, false);
    }

    public void release() {
        stop();
    }
}
