package com.wali.live.livesdk.live.view.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
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

    public void showWindow() {
        MyLog.d(TAG, "showWindow");
        if (mIsWindowShow) {
            return;
        }
        mIsWindowShow = true;
        mWindowManager.addView(this, mLayoutParams);
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
}
