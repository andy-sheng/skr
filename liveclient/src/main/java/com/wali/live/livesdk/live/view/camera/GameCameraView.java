package com.wali.live.livesdk.live.view.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;

/**
 * Created by lan on 16/1/17.
 */
public class GameCameraView extends BaseCameraView {
    public static final int VIEW_WIDTH = DisplayUtils.dip2px(80f);
    public static final int VIEW_HEIGHT = VIEW_WIDTH * 4 / 3;

    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;

    private final int mParentWidth;
    private final int mParentHeight;
    private final Rect mBoundRect;

    private TouchEventHelper mTouchEventHelper;

    private int mViewWidth;
    private int mViewHeight;

    private boolean mIsLandscape;
    private boolean mIsWindowShow;

    private OrientationEventListener mOrientationEventListener;
    private int mDisplayRotation = -1;

    public GameCameraView(Context context, WindowManager windowManager, int parentWidth, int parentHeight) {
        super(context);
        mWindowManager = windowManager;
        mLayoutParams = new WindowManager.LayoutParams();
        mParentWidth = parentWidth;
        mParentHeight = parentHeight;
        mBoundRect = new Rect(0, 0, mParentWidth, mParentHeight);

        init();
    }

    @Override
    protected void init() {
        super.init();
        setupViewSize();
        setupLayoutParams();

        mTouchEventHelper = new TouchEventHelper();

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void adjustCameraOrientation() {
        if (mCamera == null) {
            return;
        }
        if (mDisplayRotation == -1) {
            mCamera.setDisplayOrientation(90);
            return;
        }
        switch (mDisplayRotation) {
            case Surface.ROTATION_0:
                mCamera.setDisplayOrientation(90);
                break;
            case Surface.ROTATION_90:
                mCamera.setDisplayOrientation(0);
                break;
            case Surface.ROTATION_180:
                mCamera.setDisplayOrientation(270);
                break;
            case Surface.ROTATION_270:
                mCamera.setDisplayOrientation(180);
                break;
            default:
                MyLog.e(TAG, "cameraOrientation error happen");
                return;
        }
    }

    @Override
    protected void adjustLayoutParam(int width, int height) {
        if (height == 0) {
            return;
        }
        mViewHeight = mViewWidth * width / height;

        if (mViewWidth != mViewHeight) {
            if (mIsLandscape) {
                mLayoutParams.width = mViewHeight;
                mLayoutParams.height = mViewWidth;
            } else {
                mLayoutParams.width = mViewWidth;
                mLayoutParams.height = mViewHeight;
            }
            mWindowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    @Override
    protected int getViewWidth() {
        return mViewWidth;
    }

    @Override
    protected int getViewHeight() {
        return mViewHeight;
    }

    private void setupViewSize() {
        mViewWidth = Math.min(mParentWidth, VIEW_WIDTH);
        mViewHeight = Math.min(mParentHeight, VIEW_HEIGHT);
    }

    private void setupLayoutParams() {
        MyLog.d(TAG, "setupLayoutParams");

        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.x = mBoundRect.right - mViewWidth - 20;
        mLayoutParams.y = mBoundRect.top + 20 + BaseActivity.getStatusBarHeight();
        mLayoutParams.windowAnimations = 0;
        mLayoutParams.width = mViewWidth;
        mLayoutParams.height = mViewHeight;
        mLayoutParams.token = getWindowToken();
        mLayoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
    }

    private void initOrientationEventListener() {
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(getContext(), SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int orientation) {
                    onDisplayRotation();
                }
            };
        }
    }

    public void onDisplayRotation() {
        Display display = mWindowManager.getDefaultDisplay();
        int rotation = display.getRotation();
        if (mDisplayRotation == rotation) {
            return;
        }
        MyLog.d(TAG, "onOrientation displayRotation=" + rotation);
        mDisplayRotation = rotation;

        adjustCameraOrientation();

        boolean isLandscape;
        switch (mDisplayRotation) {
            case Surface.ROTATION_0:
                isLandscape = false;
                break;
            case Surface.ROTATION_90:
                isLandscape = true;
                break;
            case Surface.ROTATION_180:
                isLandscape = false;
                break;
            case Surface.ROTATION_270:
                isLandscape = true;
                break;
            default:
                MyLog.e(TAG, "displayRotation error happen");
                return;
        }
        onOrientation(isLandscape);
    }

    private void onOrientation(boolean isLandscape) {
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        if (mIsLandscape) {
            mBoundRect.set(0, 0, mParentHeight, mParentWidth);
        } else {
            mBoundRect.set(0, 0, mParentWidth, mParentHeight);
        }
        if (mIsLandscape) {
            mLayoutParams.width = mViewHeight;
            mLayoutParams.height = mViewWidth;

            mLayoutParams.x = mBoundRect.right - mViewHeight - 20;
        } else {
            mLayoutParams.width = mViewWidth;
            mLayoutParams.height = mViewHeight;

            mLayoutParams.x = mBoundRect.right - mViewWidth - 20;
        }
        mLayoutParams.y = mBoundRect.top + 20 + BaseActivity.getStatusBarHeight();
        mWindowManager.updateViewLayout(this, mLayoutParams);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mTouchEventHelper.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void showWindow() {
        MyLog.d(TAG, "showWindow");
        if (mIsWindowShow) {
            return;
        }
        mIsWindowShow = true;
        mWindowManager.addView(this, mLayoutParams);

        initOrientationEventListener();
        enableOrientationEventListener();
    }

    public void removeWindow() {
        MyLog.d(TAG, "removeWindow");
        if (!mIsWindowShow) {
            return;
        }
        mIsWindowShow = false;
        mWindowManager.removeViewImmediate(this);

        disableOrientationEventListener();
    }

    private void enableOrientationEventListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.enable();
        }
    }

    private void disableOrientationEventListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    public void destroy() {
        MyLog.d(TAG, "destroy");
        stopCamera();
    }

    // 触摸移动辅助类
    private class TouchEventHelper {
        private float xInView;
        private float yInView;
        private float xInScreen;
        private float yInScreen;

        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    xInView = event.getX();
                    yInView = event.getY();
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY();
                    // 手指移动时更新小悬浮窗的位置
                    updateViewPosition((int) (xInScreen - xInView), (int) (yInScreen - yInView));
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            return true;
        }

        private void updateViewPosition(int x, int y) {
            if (x < mBoundRect.left) {
                x = mBoundRect.left;
            } else if (x > mBoundRect.right - getWidth()) {
                x = mBoundRect.right - getWidth();
            }
            if (y < mBoundRect.top) {
                y = mBoundRect.top;
            } else if (y > mBoundRect.bottom - getHeight()) {
                y = mBoundRect.bottom - getHeight();
            }
            mLayoutParams.x = x;
            mLayoutParams.y = y;
            mWindowManager.updateViewLayout(GameCameraView.this, mLayoutParams);
        }
    }
}
