package com.wali.live.livesdk.live.window;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.engine.base.EngineEventClass;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.presenter.GameLivePresenter;
import com.xiaomi.rendermanager.videoRender.VideoStreamsView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.wali.live.livesdk.live.window.GameFloatIcon.MODE_DRAGGING;
import static com.wali.live.livesdk.live.window.GameFloatIcon.MODE_NORMAL;
import static com.wali.live.livesdk.live.window.GameFloatIcon.MOVE_THRESHOLD;
import static org.greenrobot.eventbus.ThreadMode.MAIN;

/**
 * Created by yangli on 2017/10/24.
 *
 * @module 悬浮窗相机画面
 */
public class GameFloatCameraView extends FrameLayout {
    private static final String TAG = "GameFloatCameraView";

    public static final int VIEW_MARGIN = DisplayUtils.dip2px(6.67f);
    public static final int VIEW_WIDTH = DisplayUtils.dip2px(90f);
    public static final int VIEW_HEIGHT = VIEW_WIDTH * 16 / 9;
    public static final int VIEWER_EXTRA_WIDTH = 0; // DisplayUtils.dip2px(6.67f);
    public static final int VIEWER_EXTRA_HEIGHT = 0; // VIEWER_EXTRA_WIDTH * 16 / 9;

    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;
    private final GameLivePresenter mGameLivePresenter;

    private int mViewWidth = VIEW_WIDTH;
    private int mViewHeight = VIEW_HEIGHT;

    private final int mParentWidth;
    private final int mParentHeight;
    private final Rect mBoundRect;

    private boolean mIsLandscape = false;
    private boolean mIsWindowShow = false;

    private int mMode = MODE_NORMAL;
    private final TouchEventHelper mTouchEventHelper;
    private float mLeftX;
    private float mLeftY;
    private float mScaleWidth;
    private float mScaleHeight;

    private final OrientationEventListener mOrientationEventListener;
    private int mDisplayRotation = -1;

    private VideoStreamsView mVideoStreamsView;

    public GameFloatCameraView(
            @NonNull Context context,
            @NonNull WindowManager windowManager,
            @NonNull GameLivePresenter gameLivePresenter,
            int parentWidth,
            int parentHeight) {
        super(context);
        inflate(context, R.layout.live_display_view, this);
        mVideoStreamsView = (VideoStreamsView) findViewById(R.id.galileo_surface_view);
        mWindowManager = windowManager;
        mGameLivePresenter = gameLivePresenter;
        mParentWidth = parentWidth;
        mParentHeight = parentHeight;
        mBoundRect = new Rect(0, 0, mParentWidth, mParentHeight);
        mTouchEventHelper = new TouchEventHelper();
        mLayoutParams = new WindowManager.LayoutParams();
        setupLayoutParams();
        mOrientationEventListener = new OrientationEventListener(getContext(), SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                onDisplayRotation();
            }
        };
    }

    private void setupLayoutParams() {
        MyLog.d(TAG, "setupLayoutParams");
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.x = mBoundRect.right - mViewWidth - VIEW_MARGIN;
        mLayoutParams.y = mBoundRect.top + VIEW_MARGIN + BaseActivity.getStatusBarHeight();
        mLayoutParams.windowAnimations = 0;
        mLayoutParams.width = mViewWidth;
        mLayoutParams.height = mViewHeight;
        mLayoutParams.token = getWindowToken();
        mLayoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        calcScalePosition();
    }

    private final void calcScalePosition() {
        int scaledWidth, scaledHeight, extraWidth = 0, extraHeight = 0;
        if (mParentWidth * 16 >= mParentHeight * 9) {
            scaledWidth = mParentWidth;
            scaledHeight = mParentWidth * 16 / 9;
            extraHeight = (scaledHeight - mParentHeight) >> 1;
        } else {
            scaledHeight = mParentHeight;
            scaledWidth = mParentHeight * 9 / 16;
            extraWidth = (scaledWidth - mParentWidth) >> 1;
        }
        float parentWidth = scaledWidth, parentHeight = scaledHeight;
        int extraX = extraWidth, extraY = extraHeight;
        if (mIsLandscape) {
            parentWidth = scaledHeight;
            parentHeight = scaledWidth;
            extraX = extraHeight;
            extraY = extraWidth;
        }
        // 传递给观众端时，稍微放大一点VIEWER_EXTRA_WIDTH
        mLeftX = (extraX + mLayoutParams.x - VIEWER_EXTRA_WIDTH) / parentWidth;
        mLeftY = (extraY + mLayoutParams.y - VIEWER_EXTRA_HEIGHT) / parentHeight;
        mScaleWidth = (mLayoutParams.width + (VIEWER_EXTRA_WIDTH << 1)) / parentWidth;
        mScaleHeight = (mLayoutParams.height + (VIEWER_EXTRA_HEIGHT << 1)) / parentHeight;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onDisplayRotation();
    }

    private void onDisplayRotation() {
        Display display = mWindowManager.getDefaultDisplay();
        int rotation = display.getRotation();
        if (mDisplayRotation == rotation) {
            return;
        }
        MyLog.d(TAG, "onDisplayRotation displayRotation=" + rotation);
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

    private void adjustCameraOrientation() {
        if (mDisplayRotation == -1) {
            mDisplayRotation = Surface.ROTATION_0;
            mGameLivePresenter.setDisplayOrientation(0);
            return;
        }
        switch (mDisplayRotation) {
            case Surface.ROTATION_0:
                mGameLivePresenter.setDisplayOrientation(0);
                break;
            case Surface.ROTATION_90:
                mGameLivePresenter.setDisplayOrientation(270);
                break;
            case Surface.ROTATION_180:
                mGameLivePresenter.setDisplayOrientation(180);
                break;
            case Surface.ROTATION_270:
                mGameLivePresenter.setDisplayOrientation(90);
                break;
            default:
                MyLog.e(TAG, "cameraOrientation error happen");
                return;
        }
    }

    private void onOrientation(boolean isLandscape) {
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        if (mIsLandscape) {
            mBoundRect.set(0, 0, mParentHeight, mParentWidth);
            mLayoutParams.width = mViewHeight;
            mLayoutParams.height = mViewWidth;
            mLayoutParams.x = mBoundRect.right - mViewHeight - VIEW_MARGIN;
        } else {
            mBoundRect.set(0, 0, mParentWidth, mParentHeight);
            mLayoutParams.width = mViewWidth;
            mLayoutParams.height = mViewHeight;
            mLayoutParams.x = mBoundRect.right - mViewWidth - VIEW_MARGIN;
        }
        mLayoutParams.y = mBoundRect.top + VIEW_MARGIN + BaseActivity.getStatusBarHeight();
        mWindowManager.updateViewLayout(this, mLayoutParams);
        calcScalePosition();
        mGameLivePresenter.startMergeCameraPreview(mLeftX, mLeftY, mScaleWidth, mScaleHeight);
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
        EventBus.getDefault().register(this);
        mOrientationEventListener.enable();
        setVisibility(View.GONE);
        mGameLivePresenter.startCameraPreview(mVideoStreamsView);
        onDisplayRotation();
    }

    public void removeWindow() {
        MyLog.d(TAG, "removeWindow");
        if (!mIsWindowShow) {
            return;
        }
        mIsWindowShow = false;
        EventBus.getDefault().unregister(this);
        mOrientationEventListener.disable();
        mGameLivePresenter.stopMergeCameraPreview();
        mGameLivePresenter.stopCameraPreview();
        mWindowManager.removeViewImmediate(this);
    }

    private void onEnterDragMode() {
        if (mMode == MODE_DRAGGING) {
            return;
        }
        MyLog.w(TAG, "onEnterDragMode");
        mMode = MODE_DRAGGING;
        mGameLivePresenter.stopMergeCameraPreview();
    }

    private void onExitDragMode() {
        if (mMode != MODE_DRAGGING) {
            return;
        }
        MyLog.w(TAG, "onExitDragMode");
        mMode = MODE_NORMAL;
        calcScalePosition();
        mGameLivePresenter.startMergeCameraPreview(mLeftX, mLeftY, mScaleWidth, mScaleHeight);
    }

    @Subscribe(threadMode = MAIN)
    public void onEvent(EngineEventClass.CameraStartedEvent event) {
        if (event == null) {
            return;
        }
        setVisibility(View.VISIBLE);
        mGameLivePresenter.startMergeCameraPreview(mLeftX, mLeftY, mScaleWidth, mScaleHeight);
    }

    // 触摸移动辅助类
    private class TouchEventHelper {
        private float xInView;
        private float yInView;
        private float xInScreen;
        private float yInScreen;
        private float xDownInScreen;
        private float yDownInScreen;

        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    MyLog.i(TAG, "onTouchEvent ACTION_DOWN");
                    xInView = event.getX();
                    yInView = event.getY();
                    xInScreen = xDownInScreen = event.getRawX();
                    yInScreen = yDownInScreen = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    MyLog.i(TAG, "onTouchEvent ACTION_MOVE");
                    if (mMode == MODE_DRAGGING) {
                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY();
                        // 手指移动时更新小悬浮窗的位置
                        updateViewPosition((int) (xInScreen - xInView), (int) (yInScreen - yInView));
                    } else if (mMode == MODE_NORMAL) {
                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY();
                        if (Math.abs(xDownInScreen - xInScreen) > MOVE_THRESHOLD ||
                                Math.abs(yDownInScreen - yInScreen) > MOVE_THRESHOLD) {
                            onEnterDragMode();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    MyLog.i(TAG, "onTouchEvent ACTION_UP");
                    onExitDragMode();
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
            mWindowManager.updateViewLayout(GameFloatCameraView.this, mLayoutParams);
        }
    }
}
