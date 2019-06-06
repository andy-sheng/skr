package com.zq.mediaengine.capture.camera;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;

import com.zq.mediaengine.capture.CameraCapture;

import java.util.List;


/**
 * Collection of utility functions used in this package.
 *
 * @hide
 */
public class CameraUtil {
    private static final String TAG = "CameraUtil";

    private static boolean isSupported(String s, List list) {
        boolean flag = false;
        if (list != null && list.indexOf(s) >= 0) {
            flag = true;
        }
        return flag;
    }

    public static String setFocusModeForCamera(Camera.Parameters parameters) {
        if (parameters == null) {
            return null;
        }
        List<String> supportedFocus = parameters.getSupportedFocusModes();
        String focusMode = parameters.getFocusMode();
        if (isSupported(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, supportedFocus)) {
            focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;  //用于视频记录的连续自动对焦模式。
        } else if (isSupported(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, supportedFocus)) {
            focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;  //用于拍照的连续自动对焦模式
        } else if (isSupported(Camera.Parameters.FOCUS_MODE_AUTO, supportedFocus)) {
            focusMode = Camera.Parameters.FOCUS_MODE_AUTO; //自动对焦模式
        }
        parameters.setFocusMode(focusMode);
        return focusMode;
    }

    /**
     * 设置白平衡
     *
     * @param parameters
     */
    public static void setWhiteBalance(Camera.Parameters parameters) {
        if (null == parameters) {
            return;
        }
        if (isSupported(Camera.Parameters.WHITE_BALANCE_AUTO,
                parameters.getSupportedWhiteBalance())) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        } else {
            Log.e(TAG, "Auto white balance not found!");
        }
    }

    /**
     * 防抖
     *
     * @param parameters
     */
    public static void setVideoStabilization(Camera.Parameters parameters) {
        if (null == parameters) {
            return;
        }
        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
        }
    }

    /**
     * Antibanding function should decrease fluctuations in brightness of frames or images,
     * caused by a light source oscillations and exposure control algorithm.
     * If light source oscillates with 50 Hz, and exposure needed for the image or video is not fold (divisible)
     * of 10 then you should see brighter bands across the image.
     * They demonstrate frequency mismatch.
     *
     * @param parameters
     */
    public static void setAntibanding(Camera.Parameters parameters) {
        if (null == parameters) {
            return;
        }
        List<String> antibandings = parameters.getSupportedAntibanding();
        if (isSupported(Camera.Parameters.ANTIBANDING_AUTO, antibandings)) {
            parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
        }
    }

    private static void throwIfCameraDisabled(Context activity) throws CameraDisabledException {
        // Check if device policy has disabled the camera.
        DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        //某些机型(meizu)这个判断不生效,并且camera不会返回null,后续就需要对camera的相关调用做判空
        if (dpm.getCameraDisabled(null)) {
            throw new CameraDisabledException();
        }
    }

    public static CameraManager.CameraProxy openCamera(Context context, int cameraId)
            throws CameraHardwareException, CameraDisabledException {
        throwIfCameraDisabled(context);

        try {
            return CameraHolder.instance().open(cameraId);
        } catch (CameraHardwareException e) {
            // In eng build, we throw the exception so that test tool
            // can detect it and report it
            if ("eng".equals(Build.TYPE)) {
                throw new RuntimeException("openCamera failed", e);
            } else {
                throw e;
            }
        }
    }

    public static float setPreviewFps(Camera.Parameters parameters,
                                      float presetFps, boolean fixed) {
        if (parameters == null) {
            return 0;
        }

        int presetFrameRate = (int) (presetFps * 1000);
        int[] targetFps = new int[2];
        int min_diff = Integer.MAX_VALUE;
        List<int[]> fpsRanges = parameters.getSupportedPreviewFpsRange();
        for (int[] fps : fpsRanges) {
            Log.d(TAG, fps[0] + "-" + fps[1]);
        }
        for (int[] fps : fpsRanges) {
            if (presetFrameRate >= fps[0] && presetFrameRate <= fps[1]) {
                if (fixed || fps[0] != fps[1]) {
                    if (fixed) {
                        targetFps[0] = presetFrameRate;
                        targetFps[1] = presetFrameRate;
                    } else {
                        targetFps[0] = fps[0];
                        targetFps[1] = fps[1];
                    }
                    break;
                }
            }
            for (int fp : fps) {
                int diff = Math.abs(fp - presetFrameRate);
                if (diff <= min_diff) {
                    if (fixed) {
                        targetFps[0] = fp;
                        targetFps[1] = fp;
                    } else {
                        targetFps[0] = fps[0];
                        targetFps[1] = fps[1];
                    }
                    min_diff = diff;
                }
            }
        }

        parameters.setPreviewFpsRange(targetFps[0], targetFps[1]);
        return targetFps[1] / 1000.f;
    }

    public static CameraCapture.Size setPreviewResolution(Camera.Parameters parameters,
                                                          CameraCapture.Size presetSize) {
        if (parameters == null || presetSize == null) {
            return null;
        }

        int targetWidth = 0, targetHeight = 0;
        int secondWidth = 0, secondHeight = 0;
        int offset = Integer.MAX_VALUE;
        int secondOffset = Integer.MAX_VALUE;
        List<Size> sizes = parameters.getSupportedPreviewSizes();
        for (Size size : sizes) {
            Log.d(TAG, "==== Camera Support: " + size.width + "x" + size.height);
            int off = (size.width - presetSize.width) * (size.width - presetSize.width) +
                    (size.height - presetSize.height) * (size.height - presetSize.height);
            if (off < secondOffset) {
                secondWidth = size.width;
                secondHeight = size.height;
                secondOffset = off;
            }
            if (size.width >= presetSize.width && size.height >= presetSize.height) {
                if (off < offset) {
                    targetWidth = size.width;
                    targetHeight = size.height;
                    offset = off;
                }
            }
        }
        if (targetWidth == 0 || targetHeight == 0) {
            targetWidth = secondWidth;
            targetHeight = secondHeight;
        }
        parameters.setPreviewSize(targetWidth, targetHeight);
        return new CameraCapture.Size(targetWidth, targetHeight);
    }

    public static int getDisplayOrientation(int degrees, int cameraId) {
        // See android.hardware.Camera.setDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
}