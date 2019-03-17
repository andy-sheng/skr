package com.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyBoardUtils {

    private static final String SHARE_PREFERENCE_SOFT_INPUT_HEIGHT = "soft_keyboard_height";

    KeyBoardUtils() {

    }

    public void hideSoftInputKeyBoard(Context context, View focusView) {
        if(context == null){
            return;
        }
        if (focusView != null) {
            IBinder binder = focusView.getWindowToken();
            if (binder != null) {
                InputMethodManager imd = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imd.hideSoftInputFromWindow(binder, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        }
    }

    public void hideSoftInputKeyBoard(Activity context) {
        if(context == null){
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        if (context.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showSoftInputKeyBoard(Context context, View focusView) {
        if(context == null){
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(focusView, InputMethodManager.SHOW_FORCED);
    }

    public void showSoftInputKeyBoard(Context context) {
        if(context == null){
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.SHOW_FORCED);
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
        return screenHeight*2/3 > rect.bottom+U.getDeviceUtils().getVirtualNavBarHeight();
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
