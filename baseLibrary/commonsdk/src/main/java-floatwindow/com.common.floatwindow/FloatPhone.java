package com.common.floatwindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import com.common.log.MyLog;
import com.common.permission.FloatWindowPermissionActivity;
import com.common.permission.PermissionUtils;
import com.common.utils.U;

import java.util.List;

/**
 * Created by yhao on 17-11-14.
 * https://github.com/yhaolpz
 */

class FloatPhone extends FloatView {
    public final static String TAG = "FloatPhone";
    private FloatWindow.B mB;
    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;
    private boolean isRemove = true;

    FloatPhone(FloatWindow.B b) {
        mB = b;

        mWindowManager = (WindowManager) mB.mApplicationContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.windowAnimations = 0;

        mLayoutParams.gravity = mB.gravity;
        mLayoutParams.x = mB.xOffset;
        mLayoutParams.y = mB.yOffset;
        mLayoutParams.width = mB.mWidth;
        mLayoutParams.height = mB.mHeight;
    }

    @Override
    public void init() {
        boolean hasFloatWindowPermission = U.getPermissionUtils().checkFloatWindow(mB.mApplicationContext);
        MyLog.d(TAG, "init hasFloatWindowPermission=" + hasFloatWindowPermission);
        if (hasFloatWindowPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            addView();
        } else {
            if (mB.reqPermissionIfNeed) {
                U.getPermissionUtils().requestFloatWindow(new PermissionUtils.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        MyLog.d(TAG, "onRequestPermissionSuccess");
                    }

                    @Override
                    public void onRequestPermissionFailure(List<String> permissions) {
                        MyLog.d(TAG, "onRequestPermissionFailure" + " permissions=" + permissions);
                    }

                    @Override
                    public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                    }
                }, U.getActivityUtils().getTopActivity());
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                addView();
            }
        }
    }

    public void addView() {
        if (isRemove) {
            MyLog.d(TAG, "addView type" + mLayoutParams.type);
            mWindowManager.addView(mB.mView, mLayoutParams);
            isRemove = false;
        }
    }

    @Override
    public void dismiss() {
        MyLog.d(TAG, "dismiss isRemove=" + isRemove);
        if (!isRemove) {
            isRemove = true;
            mWindowManager.removeView(mB.mView);
        }
    }

    @Override
    public void updateXY(int x, int y) {
        if (isRemove) return;
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        mWindowManager.updateViewLayout(mB.mView, mLayoutParams);
    }

    @Override
    void updateX(int x) {
        if (isRemove) return;
        mB.xOffset = x;
        mLayoutParams.x = x;
        mWindowManager.updateViewLayout(mB.mView, mLayoutParams);
    }

    @Override
    void updateY(int y) {
        if (isRemove) return;
        mB.yOffset = y;
        mLayoutParams.y = y;
        mWindowManager.updateViewLayout(mB.mView, mLayoutParams);
    }

    @Override
    int getX() {
        return mB.xOffset;
    }

    @Override
    int getY() {
        return mB.yOffset;
    }


}
