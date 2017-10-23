package com.wali.live.livesdk.live.view.camera;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.support.annotation.CallSuper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.base.log.MyLog;

import java.io.IOException;
import java.util.List;

/**
 * Created by lan on 2017/9/28.
 */
public abstract class BaseCameraView extends SurfaceView implements SurfaceHolder.Callback {
    protected final String TAG = getTAG();

    /*分辨率的误差*/
    protected static final double ASPECT_TOLERANCE = 0.1;

    protected SurfaceHolder mSurfaceHolder;
    protected Camera mCamera;

    protected boolean mIsStartCamera;

    public BaseCameraView(Context context) {
        this(context, null);
    }

    public BaseCameraView(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public BaseCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    @CallSuper
    protected void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    protected void startCamera() {
        mCamera = openFrontFacingCamera();
        if (mCamera == null) {
            MyLog.e(TAG, "openCamera is null");
            return;
        }
        mIsStartCamera = true;

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(supportedPreviewSizes, getViewWidth(), getViewHeight());

        MyLog.d(TAG, "startCamera=" + optimalSize.width + ":" + optimalSize.height);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);

        mCamera.setParameters(parameters);

        adjustCameraOrientation();
        adjustLayoutParam(optimalSize.width, optimalSize.height);

        startPreview();
    }

    protected abstract void adjustCameraOrientation();

    protected abstract void adjustLayoutParam(int width, int height);

    protected abstract int getViewWidth();

    protected abstract int getViewHeight();

    protected void startPreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                MyLog.e(TAG, "setPreviewDisplay", e);
                return;
            }
            mCamera.startPreview();
        }
    }

    protected void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    protected void stopCamera() {
        mIsStartCamera = false;
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        MyLog.v(TAG, "surfaceCreated=" + holder.hashCode());
        mSurfaceHolder = holder;
        if (mIsStartCamera) {
            startPreview();
        } else {
            startCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        MyLog.v(TAG, "surfaceChanged=" + holder.hashCode());
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        MyLog.v(TAG, "surfaceDestroyed=" + holder.hashCode());
        mSurfaceHolder = null;
        stopPreview();
    }

    protected Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    protected Camera openFrontFacingCamera() {
        return openDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    protected Camera openDefaultCamera(int position) {
        int numberOfCameras = Camera.getNumberOfCameras();

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == position) {
                return Camera.open(i);

            }
        }
        return null;
    }
}