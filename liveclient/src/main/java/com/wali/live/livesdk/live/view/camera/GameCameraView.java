package com.wali.live.livesdk.live.view.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;

/**
 * Created by lan on 16/1/17.
 */
public class GameCameraView extends BaseCameraView {
    protected final WindowManager mWindowManager;
    protected final WindowManager.LayoutParams mLayoutParams;

    protected boolean mIsWindowShow;

    public GameCameraView(Context context, WindowManager windowManager) {
        super(context);
        mWindowManager = windowManager;
        mLayoutParams = new WindowManager.LayoutParams();

        init();
    }

    @Override
    protected void init() {
        super.init();
        setupLayoutParams();

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void setupLayoutParams() {
        MyLog.d(TAG, "setupLayoutParams");

        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.x = 100;
        mLayoutParams.y = 100;
        mLayoutParams.windowAnimations = 0;
        mLayoutParams.width = DisplayUtils.dip2px(80f);
        mLayoutParams.height = DisplayUtils.dip2px(80f);
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
}
