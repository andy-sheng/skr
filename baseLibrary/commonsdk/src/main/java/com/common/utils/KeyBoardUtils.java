package com.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyBoardUtils {

    private static final String SHARE_PREFERENCE_SOFT_INPUT_HEIGHT = "soft_keyboard_height";

    KeyBoardUtils() {

    }

    public void hideSoftInputKeyBoard(Context context, View focusView) {
        if (context == null) {
            return;
        }
        if (focusView != null) {
            IBinder binder = focusView.getWindowToken();
            if (binder != null) {
                InputMethodManager imd = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imd.hideSoftInputFromWindow(binder, 0);
            }
        }
    }

    public void hideSoftInputKeyBoard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        hideSoftInput(view);
    }

    /**
     * Hide the soft input.
     *
     * @param view The view.
     */
    public void hideSoftInput(final View view) {
        InputMethodManager imm =
                (InputMethodManager) U.app().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN
                        || resultCode == InputMethodManager.RESULT_SHOWN) {
                    toggleSoftInput();
                }
            }
        });
    }

    /**
     * Toggle the soft input display or not.
     */
    public void toggleSoftInput() {
        InputMethodManager imm =
                (InputMethodManager) U.app().getSystemService(Context.INPUT_METHOD_SERVICE);
        //noinspection ConstantConditions
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    public void showSoftInputKeyBoard(Context context, View focusView) {
        if (context == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(focusView, InputMethodManager.SHOW_FORCED);
    }

    public void showSoftInputKeyBoard(Activity activity) {
        showSoftInput(activity, InputMethodManager.SHOW_FORCED);
    }

    /**
     * Show the soft input.
     *
     * @param activity The activity.
     * @param flags    Provides additional operating flags.  Currently may be
     *                 0 or have the {@link InputMethodManager#SHOW_IMPLICIT} bit set.
     */
    public void showSoftInput(final Activity activity, final int flags) {
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        showSoftInput(view, flags);
    }

    public void showSoftInput(final View view, final int flags) {
        InputMethodManager imm =
                (InputMethodManager) U.app().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        imm.showSoftInput(view, flags, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN
                        || resultCode == InputMethodManager.RESULT_HIDDEN) {
                    toggleSoftInput();
                }
            }
        });
    }

    public boolean isSoftKeyboardShowing(Activity activity) {
        //获取当屏幕内容的高度
        int screenHeight = activity.getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        //DecorView即为activity的顶级view
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        //考虑到虚拟导航栏的情况（虚拟导航栏情况下：screenHeight = rect.bottom + 虚拟导航栏高度）
        //选取screenHeight*2/3进行判断
        return screenHeight * 2 / 3 > rect.bottom + U.getDeviceUtils().getVirtualNavBarHeight();
    }

    /**
     * 获取软键盘高度
     *
     * @return
     */
    public int getKeyBoardHeight() {
        return U.getPreferenceUtils().getSettingInt(SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, 858);
    }

    public void setKeyBoardHeight(int height) {
        U.getPreferenceUtils().setSettingInt(SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, height);
    }

    /**
     * 获取软件盘的高度,如果此时真的有软键盘在显示，否则返回null
     * 参考 {@link AndroidBug5497WorkaroundSupportingTranslucentStatus} 计算软键盘的方法
     *
     * @return
     */
    public int getKeyBoardHeightNow(Activity activity) {
        Rect r = new Rect();
        /**
         * decorView是window中的最顶层view，可以从window中通过getDecorView获取到decorView。
         * 通过decorView获取到程序显示的区域，包括标题栏，但不包括状态栏。
         *
         */
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);

        //获取手机屏幕的高度
        int screenHeight = activity.getWindow().getDecorView().getRootView().getHeight();
        //计算软件盘的高度
        int softInputHeight = screenHeight - r.bottom;

        /**
         * 某些Android版本下，没有显示软键盘时减出来的高度总是144，而不是零，
         * 这是因为高度是包括了虚拟按键栏的(例如华为系列)，所以在API Level高于20时，
         * 我们需要减去底部虚拟按键栏的高度（如果有的话）
         */
        if (Build.VERSION.SDK_INT >= 20 && U.getDeviceUtils().hasNavigationBar()) {
            // When SDK Level >= 20 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
            softInputHeight = softInputHeight - U.getDeviceUtils().getVirtualNavBarHeight();
        }
        if (softInputHeight < 0) {
            Log.w("LQR", "EmotionKeyboard--Warning: value of softInputHeight is below zero!");
            return 0;
        }
        //存一份到本地
        if (softInputHeight > 0) {
            U.getKeyBoardUtils().setKeyBoardHeight(softInputHeight);
        }
        return softInputHeight;
    }

}
