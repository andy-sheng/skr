package com.changba.songstudio.recording.camera.preview;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.common.log.MyLog;

public class ChangbaRecordingPreviewView implements Callback {
    private static final String TAG = "ChangbaRecordingPreviewView";

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    public ChangbaRecordingPreviewView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        MyLog.d(TAG, "surfaceCreated" + " holder=" + holder);

        Surface surface = holder.getSurface();
        int width = mSurfaceView.getWidth();
        int height = mSurfaceView.getHeight();
        if (null != mCallback) {
            mCallback.createSurface(surface, width, height);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        MyLog.d(TAG, "surfaceChanged" + " holder=" + holder + " format=" + format + " width=" + width + " height=" + height);
        if (null != mCallback) {
            mCallback.resetRenderSize(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        MyLog.d(TAG, "surfaceDestroyed" + " holder=" + holder);
        if (null != mCallback) {
            mCallback.destroySurface();
        }
    }

    private ChangbaRecordingPreviewViewCallback mCallback;

    public void setCallback(ChangbaRecordingPreviewViewCallback callback) {
        this.mCallback = callback;
    }

    public SurfaceHolder getHolder() {
        return mSurfaceView.getHolder();
    }

    public int getWidth() {
        return mSurfaceView.getWidth();
    }

    public int getHeight() {
        return mSurfaceView.getHeight();
    }

    public interface ChangbaRecordingPreviewViewCallback {
        void createSurface(Surface surface, int width, int height);

        void resetRenderSize(int width, int height);

        void destroySurface();
    }
}
