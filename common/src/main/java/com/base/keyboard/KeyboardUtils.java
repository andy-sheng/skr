package com.base.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.base.activity.BaseActivity;
import com.base.common.R;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;

public class KeyboardUtils {
    private static final String TAG = KeyboardUtils.class.getSimpleName();
    private static final String PREF_KEY_KEYBOARD_HEIGHT = "pref_key_keyboard_height";

    private static int sDefaultPickerHeight = 0;

    public static int getDefaultPickerHeight() {
        return sDefaultPickerHeight;
    }

    public static void showKeyboard(final Context context, EditText editText) {
        editText.requestFocus();
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard(context);
            }
        }, 200);
    }

    public static void showKeyboardWithDelay(final Context context, EditText editText, long miniSecond) {
        editText.requestFocus();
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard(context);
            }
        }, miniSecond);
    }

    /**
     * EditText没有获取焦点的情况下有可能无法弹起键盘,请使用上面的方法
     */
    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void hideKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * 隐藏键盘并返回隐藏的结果
     *
     * @param activity
     * @return
     */
    public static boolean hideKeyboardThenReturnResult(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            boolean isHide = imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getApplicationWindowToken(), 0);
            MyLog.w(TAG, "isHide=" + isHide);
            return isHide;
        }
        return false;
    }

    public static void hideKeyboard(final Activity activity) {
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //华为手机键盘隐藏需要postDelay才work.  LIVEAND-4963
        activity.getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "imm.isActive() =" + imm.isActive());
                if (imm.isActive()) {
                    boolean isHide = imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
                    MyLog.w(TAG, "isHide=" + isHide);
                }
            }
        }, 1000);
    }

    //注释：这个函数是 如果有软键盘，那么隐藏它；反之，把它显示出来。 不是强制隐藏键盘的api.
    public static void toggleKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void hideKeyboardImmediately(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            boolean isHide = imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getApplicationWindowToken(), 0);
            MyLog.w(TAG, "isHide=" + isHide);
        }
    }


    public static boolean isKeyboardShown(Context context, EditText editText) {
        return isKeyboardShown(context, editText);
    }

    public static boolean isKeyboardShown(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        return imm.isActive(view);
    }

    public static int getKeyboardHeight(Activity activity) {
        if (sDefaultPickerHeight == 0) {
            sDefaultPickerHeight = (int) activity.getResources().getDimension(R.dimen.smileypicker_default_height);
        }

        if (activity == null) {
            return sDefaultPickerHeight;
        }

        int savedHeight = PreferenceUtils.getSettingInt(activity, PREF_KEY_KEYBOARD_HEIGHT, 0);
        Rect r = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(r);
        final int statusBarHeight = r.top;
        int keyboardHeight = getScreenHeight(activity) - statusBarHeight - r.height()
                - getBottomBarHeight(activity);

        // 保存的高度与键盘高度不等，更新保存的值
        if (keyboardHeight != 0) {
            keyboardHeight = Math.max(keyboardHeight, sDefaultPickerHeight);
            PreferenceUtils.setSettingInt(activity, PREF_KEY_KEYBOARD_HEIGHT, keyboardHeight);
            return keyboardHeight;
        }

        // 键盘没有弹出 返回保存的值
        if (keyboardHeight == 0 && savedHeight != 0) {
            savedHeight = Math.max(savedHeight, sDefaultPickerHeight);
            return savedHeight;
        }
        return sDefaultPickerHeight;
    }

    public static int getAboveLayoutHeight(Activity activity) {
        MyLog.d(TAG, "getScreenHeight = " + getScreenHeight(activity) + " getStatusBarHeight = " + getStatusBarHeight(activity)
                + " getKeyboardHeight = " + getKeyboardHeight(activity));
        return getScreenHeight(activity) - getStatusBarHeight(activity) - getKeyboardHeight(activity)
                - getBottomBarHeight(activity);
    }

    public static int getScreenHeight(Activity activity) {
        Window window = activity.getWindow();
        final int screenHeight = window.getWindowManager().getDefaultDisplay().getHeight();
        return screenHeight;
    }

    public static int getBottomBarHeight(Activity activity) {
        Window window = activity.getWindow();
        return getScreenHeight(activity) - window.getDecorView().getHeight();
    }

    //沉浸式不需要减去状态栏高度 因此需要根据版本区分对待
    public static int getStatusBarHeight(Activity activity) {
        if (!BaseActivity.isProfileMode()) {
            Rect r = new Rect();
            Window window = activity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(r);
            final int statusBarHeight = r.top;
            return statusBarHeight;
        }
        return 0;
    }
}
