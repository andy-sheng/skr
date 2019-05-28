package com.common.floatwindow;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 自定义 toast 方式，无需申请权限
 * 当前版本暂时用 TYPE_TOAST 代替，后续版本可能会再融入此方式
 */

class FloatToast extends FloatView {

    FloatWindow.B mB;
    private Toast toast;

    private Object mTN;
    private Method show;
    private Method hide;


    FloatToast(FloatWindow.B b) {
        this.mB = b;
        toast = new Toast(mB.mApplicationContext);
        toast.setView(mB.mView);
        toast.setGravity(mB.gravity, mB.xOffset, mB.yOffset);
        initTN();
    }

    @Override
    public void init() {
        try {
            show.invoke(mTN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        try {
            hide.invoke(mTN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initTN() {
        try {
            Field tnField = toast.getClass().getDeclaredField("mTN");
            tnField.setAccessible(true);
            mTN = tnField.get(toast);
            show = mTN.getClass().getMethod("show");
            hide = mTN.getClass().getMethod("hide");
            Field tnParamsField = mTN.getClass().getDeclaredField("mParams");
            tnParamsField.setAccessible(true);
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) tnParamsField.get(mTN);
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.width = mB.mWidth;
            params.height = mB.mHeight;
            params.windowAnimations = 0;
            Field tnNextViewField = mTN.getClass().getDeclaredField("mNextView");
            tnNextViewField.setAccessible(true);
            tnNextViewField.set(mTN, toast.getView());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
