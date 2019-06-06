package com.zq.mediaengine.capture.camera;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zq.mediaengine.capture.CameraCapture;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Handle touch events for camera view.
 */

public class CameraTouchHelper implements View.OnTouchListener {
    private static final String TAG = "CameraTouchHelper";
    private static final boolean VERBOSE = true;

    private CameraCapture mCameraCapture;
    private ICameraHintView mCameraHintView;
    private float mFocusAreaRadius = 1.f / 12.f;
    private boolean mEnableTouchFocus = true;
    private boolean mEnableZoom = true;
    private int mRefocusDelay = 5000;
    private float mMaxZoomRatio = 4.0f;
    private float mZoomSpeed = 1.0f;

    private List<OnTouchListener> mOnTouchListeners;

    private int mViewWidth;
    private int mViewHeight;

    private Handler mHandler;
    private Runnable mResetFocusMode;
    private Runnable mSetFocused;
    private boolean mFocused;

    private int mScaledWidth;
    private int mScaledHeight;
    private Rect mFocusRect = new Rect();
    private Rect mTouchRect = new Rect();

    private boolean mIsMutiTouch;
    private int mPointNum;
    private float mDistance;
    private long mLastZoomTime;
    private int mInitZoom;

    public CameraTouchHelper() {
        mHandler = new Handler();
        mResetFocusMode = new Runnable() {
            @Override
            public void run() {
                if (mCameraCapture == null) {
                    return;
                }
                if (VERBOSE) {
                    Log.d(TAG, "Reset focus mode");
                }
                Camera.Parameters params = mCameraCapture.getCameraParameters();
                if (params == null) {
                    return;
                }
                CameraUtil.setFocusModeForCamera(params);
                List<Camera.Area> focusList = new ArrayList<>();
                Camera.Area focusArea = new Camera.Area(new Rect(0, 0, 1000, 1000), 1000);
                focusList.add(focusArea);
                params.setMeteringAreas(focusList);
                mCameraCapture.setCameraParameters(params);
            }
        };
        mSetFocused = new Runnable() {
            @Override
            public void run() {
                mCameraHintView.setFocused(mFocused);
            }
        };

        mOnTouchListeners = new LinkedList<>();
    }

    /**
     * Set CameraCapture instance, without whom touch focus and zoom would not work.
     *
     * @param cameraCapture CameraCapture instance to set
     */
    public void setCameraCapture(CameraCapture cameraCapture) {
        mCameraCapture = cameraCapture;
    }

    /**
     * Set a CameraHintView to show touch focus status and zoom value.
     *
     * @param view view implements ICameraHintView
     */
    public void setCameraHintView(ICameraHintView view) {
        mCameraHintView = view;
    }

    /**
     * Set radius of focus square, relative to the short edge of preview view.<br/>
     * The default value is 0.083.
     *
     * @param radius must be 0-0.5, or exception occur.
     * @throws IllegalArgumentException
     */
    public void setFocusAreaRadius(float radius) {
        if (radius <= 0f || radius > 0.5f) {
            throw new IllegalArgumentException("radius must be > 0 && < 0.5");
        }
        mFocusAreaRadius = radius;
    }

    /**
     * Set delay time to reset to previous focus mode after touch focused.<br/>
     * The default value is 5000.
     *
     * @param delay delay time in milliseconds, 0 or negative value means never.
     */
    public void setRefocusDelay(int delay) {
        mRefocusDelay = delay;
    }

    /**
     * Enable touch focus or not. Default enabled.
     *
     * @param enableTouchFocus true to enable, false to disable
     */
    public void setEnableTouchFocus(boolean enableTouchFocus) {
        mEnableTouchFocus = enableTouchFocus;
    }

    /**
     * Enable zoom camera with gestures or not. Default enabled.
     *
     * @param enableZoom true to enable, false to disable
     */
    public void setEnableZoom(boolean enableZoom) {
        mEnableZoom = enableZoom;
    }

    /**
     * Set max zoom value, will use the smaller one between the set value
     * and max zoom value that hardware supported.<br/>
     * The default value is 4.0f.
     *
     * @param maxZoomRatio value that bigger than 1.0f, 1.0 means no zoom.
     */
    public void setMaxZoomRatio(float maxZoomRatio) {
        if (maxZoomRatio < 1) {
            maxZoomRatio = 1;
        }
        mMaxZoomRatio = maxZoomRatio;
    }

    /**
     * Set zoom in/out speed while using scale gestures.
     *
     * @param zoomSpeed valid in 0.1f~10.f, default 1.0f.
     */
    public void setZoomSpeed(float zoomSpeed) {
        if (zoomSpeed < 0.1f) {
            zoomSpeed = 0.1f;
        } else if (zoomSpeed > 10.f) {
            zoomSpeed = 10.f;
        }
        mZoomSpeed = zoomSpeed;
    }

    synchronized public void addTouchListener(OnTouchListener touchListener) {
        if (!mOnTouchListeners.contains(touchListener)) {
            mOnTouchListeners.add(touchListener);
        }
    }

    synchronized public void removeTouchListener(OnTouchListener touchListener) {
        if (mOnTouchListeners.contains(touchListener)) {
            mOnTouchListeners.remove(touchListener);
        }
    }

    synchronized public void removeAllTouchListener() {
        mOnTouchListeners.clear();
    }


    public interface OnTouchListener {
        boolean onTouch(View view, MotionEvent event);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mViewWidth = view.getWidth();
        mViewHeight = view.getHeight();
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            mIsMutiTouch = false;
            mPointNum = 1;
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            mIsMutiTouch = true;
            mPointNum++;
            if (mPointNum == 2) {
                mDistance = spacing(event);
                doZoom(0, true);
            }
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            mPointNum--;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mPointNum >= 2) {
                float distance = spacing(event);
                int delta = (int) (distance - mDistance);
                doZoom(delta, false);
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (!mIsMutiTouch) {
                doFocus(event.getX(), event.getY());
            }
            mIsMutiTouch = false;
            mPointNum = 0;
        }

        notifyTouch(view, event);
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private boolean doZoom(int delta, boolean init) {
        if (!mEnableZoom || mCameraCapture == null) {
            return false;
        }

        Camera.Parameters parameters = mCameraCapture.getCameraParameters();
        if (parameters == null) {
            return false;
        }
        if (!parameters.isZoomSupported()) {
            return false;
        }

        if (init) {
            mInitZoom = parameters.getZoom();
            return false;
        }

        // limit interval
        long time = System.currentTimeMillis();
        if (time - mLastZoomTime < 40) {
            return false;
        }
        mLastZoomTime = time;

        int maxTrip = (mViewWidth < mViewHeight) ? mViewWidth : mViewHeight;
        maxTrip = maxTrip / 2;
        int maxZoom = parameters.getMaxZoom();
        List<Integer> zoomRatios = parameters.getZoomRatios();

        int i;
        int zoomRatio = (int) (mMaxZoomRatio * 100);
        for (i = maxZoom; i >= 0; i--) {
            if (zoomRatio >= zoomRatios.get(i)) {
                maxZoom = i;
                break;
            }
        }
        if (i < 0) {
            maxZoom = 0;
        }

        int zoom = mInitZoom + (int) (mZoomSpeed * delta * maxZoom / maxTrip);
        if (zoom < 0) {
            zoom = 0;
        } else if (zoom > maxZoom) {
            zoom = maxZoom;
        }
        if (zoom != mInitZoom) {
            parameters.setZoom(zoom);
            mCameraCapture.setCameraParameters(parameters);
        }

        if (mCameraHintView != null) {
            float zoomValue = zoomRatios.get(zoom) / 100.f;
            mCameraHintView.updateZoomRatio(zoomValue);
        }

        return true;
    }

    private boolean doFocus(float x, float y) {
        if (!mEnableTouchFocus || mCameraCapture == null) {
            return false;
        }
        Camera.Parameters parameters = mCameraCapture.getCameraParameters();
        if (parameters == null) {
            return false;
        }
        List<String> supportedFocus = parameters.getSupportedFocusModes();
        if (supportedFocus == null ||
                !supportedFocus.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            return false;
        }

        calculateTouchRect(x, y);
        Camera.Size size = parameters.getPreviewSize();
        int rotateAngle = mCameraCapture.getCameraDisplayOrientation();
        calculateScaleSize(size, rotateAngle);
        calculateFocusArea(rotateAngle);
        if (VERBOSE) {
            Log.d(TAG, "touchRect: " + mTouchRect.toString() +
                    " focusRect: " + mFocusRect.toString());
        }

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        List<Camera.Area> focusList = new ArrayList<>();
        Camera.Area focusArea = new Camera.Area(mFocusRect, 1000);
        focusList.add(focusArea);
        parameters.setFocusAreas(focusList);
        parameters.setMeteringAreas(focusList);
        mCameraCapture.setCameraParameters(parameters);
        mCameraCapture.cancelAutoFocus();
        mCameraCapture.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(final boolean success, Camera camera) {
                mCameraCapture.cancelAutoFocus();
                if (mRefocusDelay > 0) {
                    mHandler.postDelayed(mResetFocusMode, mRefocusDelay);
                }

                if (mCameraHintView != null) {
                    mFocused = success;
                    mHandler.post(mSetFocused);
                }
            }
        });

        if (mCameraHintView != null) {
            mHandler.removeCallbacks(mSetFocused);
            mHandler.removeCallbacks(mResetFocusMode);
            mCameraHintView.startFocus(mTouchRect);
        }
        return true;
    }

    private void calculateTouchRect(float x, float y) {
        int touchR = (mViewWidth < mViewHeight) ?
                (int) (mViewWidth * mFocusAreaRadius) :
                (int) (mViewHeight * mFocusAreaRadius);
        int touchX = clamp((int) x, 0, mViewWidth, touchR);
        int touchY = clamp((int) y, 0, mViewHeight, touchR);
        mTouchRect.set(touchX - touchR, touchY - touchR, touchX + touchR, touchY + touchR);
    }

    private void calculateScaleSize(Camera.Size previewSize, int rotateAngle) {
        int viewHeight = mViewHeight;
        int viewWidth = mViewWidth;
        float viewWH = (float) viewWidth / viewHeight;
        if (rotateAngle % 180 == 0) {
            float previewWH = (float) previewSize.width / previewSize.height;
            if (viewWH > previewWH) {
                mScaledHeight = (int) (1000 * (previewSize.width / viewWH / previewSize.height));
                mScaledWidth = 1000;
            } else {
                mScaledHeight = 1000;
                mScaledWidth = (int) (1000 * (previewSize.height * viewWH / previewSize.width));
            }
        } else {
            //need switch width and height
            float previewWH = (float) previewSize.height / previewSize.width;
            if (viewWH > previewWH) {
                mScaledHeight = (int) (1000 * (previewSize.height / viewWH / previewSize.width));
                mScaledWidth = 1000;
            } else {
                mScaledHeight = 1000;
                mScaledWidth = (int) (1000 * (previewSize.width * viewWH / previewSize.height));
            }
        }
    }

    private void calculateFocusArea(int rotateAngle) {
        RectF areaF = new RectF(
                (float) mTouchRect.left / mViewWidth * mScaledWidth * 2 - mScaledWidth,
                (float) mTouchRect.top / mViewHeight * mScaledHeight * 2 - mScaledHeight,
                (float) mTouchRect.right / mViewWidth * mScaledWidth * 2 - mScaledWidth,
                (float) mTouchRect.bottom / mViewHeight * mScaledHeight * 2 - mScaledHeight);
        Matrix matrix = new Matrix();
        matrix.postRotate(360 - rotateAngle, 0, 0);
        matrix.mapRect(areaF);
        areaF.round(mFocusRect);
    }

    private int clamp(int coord, int min, int max, int r) {
        if (coord < (min + r)) {
            return (min + r);
        }
        if (coord > (max - r)) {
            return (max - r);
        }
        return coord;
    }

    private synchronized void notifyTouch(View view, MotionEvent event) {
        if (mOnTouchListeners != null) {
            for (int i = 0; i < mOnTouchListeners.size(); i++) {
                mOnTouchListeners.get(i).onTouch(view, event);
            }
        }
    }
}
