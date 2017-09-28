package com.wali.live.livesdk.live.view.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
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
    // 默认一样，否则转屏需要调整尺寸，比较麻烦，先简单处理，采用正方形
    public static final int VIEW_WIDTH = DisplayUtils.dip2px(80f);
    public static final int VIEW_HEIGHT = VIEW_WIDTH;

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

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onOrientation(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    public void onOrientation(boolean isLandscape) {
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        if (mIsLandscape) {
            mBoundRect.set(0, 0, mParentHeight, mParentWidth);
            mLayoutParams.x = mBoundRect.right - mViewWidth - 20;
            mLayoutParams.y = mBoundRect.top + 20 + BaseActivity.getStatusBarHeight();

            Display display = mWindowManager.getDefaultDisplay();
            MyLog.d(TAG, "onOrientation displayRotation=" + display.getRotation());
            switch (display.getRotation()) {
                case Surface.ROTATION_90:
                    mCamera.setDisplayOrientation(0);
                    break;
                case Surface.ROTATION_270:
                    mCamera.setDisplayOrientation(180);
                    break;
                default:
                    mCamera.setDisplayOrientation(180);
                    break;
            }

        } else {
            mBoundRect.set(0, 0, mParentWidth, mParentHeight);
            mLayoutParams.x = mBoundRect.right - mViewWidth - 20;
            mLayoutParams.y = mBoundRect.top + 20 + BaseActivity.getStatusBarHeight();

            mCamera.setDisplayOrientation(90);
        }
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
        onOrientation(false);
    }

    public void removeWindow() {
        MyLog.d(TAG, "removeWindow");
        if (!mIsWindowShow) {
            return;
        }
        mIsWindowShow = false;
        mWindowManager.removeViewImmediate(this);
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
