package com.zq.mediaengine.capture.camera;

import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.util.Log;

import com.zq.mediaengine.capture.camera.CameraManager.CameraProxy;

import java.io.IOException;

/**
 * @hide
 */
public class CameraHolder {
    private static final String TAG = "CameraHolder";
    private CameraProxy mCameraDevice;
    private final int mNumberOfCameras;
    private int mCameraId = -1;  // current camera id
    private int mBackCameraId = -1;
    private int mFrontCameraId = -1;
    private final CameraInfo[] mInfo;

    private Parameters mParameters;

    // Use a singleton.
    private static CameraHolder sHolder;

    public static synchronized CameraHolder instance() {
        if (sHolder == null) {
            sHolder = new CameraHolder();
        }
        return sHolder;
    }

    private CameraHolder() {
        if (android.hardware.Camera.getNumberOfCameras() < 0) {
            mNumberOfCameras = 0;
        } else {
            mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
        }
        mInfo = new CameraInfo[mNumberOfCameras];
        for (int i = 0; i < mNumberOfCameras; i++) {
            mInfo[i] = new CameraInfo();
            try {
                android.hardware.Camera.getCameraInfo(i, mInfo[i]);
            } catch (Exception e) {
                Log.w(TAG, "Failed to getCameraInfo");
            }
        }

        // get the first (smallest) back and first front camera id
        for (int i = 0; i < mNumberOfCameras; i++) {
            if (mBackCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraId = i;
            } else if (mFrontCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
            }
        }
    }

    public synchronized CameraProxy open(int cameraId) throws CameraHardwareException, CameraDisabledException {
        if (mCameraDevice != null && mCameraId != cameraId) {
            mCameraDevice.release();
            mCameraDevice = null;
            mCameraId = -1;
        }
        if (mCameraDevice == null) {
            try {
                Log.v(TAG, "open camera " + cameraId);
                mCameraDevice = CameraManager.instance().cameraOpen(cameraId);
                mCameraId = cameraId;
            } catch (RuntimeException e) {
                Log.e(TAG, "fail to connect Camera", e);
                throw new CameraHardwareException(e);
            }
            if (mCameraDevice != null) {
                mParameters = mCameraDevice.getParameters();
            }

            if (mParameters == null) {
                throw new CameraDisabledException();
            }

        } else {
            try {
                mCameraDevice.reconnect();
            } catch (IOException e) {
                Log.e(TAG, "reconnect failed.");
                throw new CameraHardwareException(e);
            }
            mCameraDevice.setParameters(mParameters);
        }
        return mCameraDevice;
    }

    public synchronized void release() {

        if (mCameraDevice == null) {
            return;
        }

        mCameraDevice.release();
        mCameraDevice = null;
        mParameters = null;
        mCameraId = -1;
    }

    public int getBackCameraId() {
        return mBackCameraId;
    }

    public int getFrontCameraId() {
        return mFrontCameraId;
    }
}
