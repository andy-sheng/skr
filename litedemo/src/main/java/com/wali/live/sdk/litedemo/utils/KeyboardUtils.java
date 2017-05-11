package com.wali.live.sdk.litedemo.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

/**
 * Created by chenyong on 2017/5/8.
 */

public class KeyboardUtils {

    private View mChildOfContent;
    private int usableHeightPrevious;
    private boolean isTranslucentEnabled = false;

    public static void showKeyboard(final Context context, EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void hideKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static void assistActivity(Activity activity, OnKeyboardChangedListener listener) {
        new KeyboardUtils(activity, listener);
    }

    private KeyboardUtils(Activity activity, final OnKeyboardChangedListener listener) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        final WindowManager.LayoutParams attrs = activity.getWindow()
                .getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isTranslucentEnabled = (attrs.flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0;
        }
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent(listener);
            }
        });
    }


    private void possiblyResizeChildOfContent(OnKeyboardChangedListener listener) {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                listener.onKeyboardShow();
            } else {
                listener.onKeyboardHide();
            }
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        if (isTranslucentEnabled) {
            r.top = 0;
        }
        return (r.bottom - r.top);
    }

    public interface OnKeyboardChangedListener {
        void onKeyboardShow();

        void onKeyboardHide();
    }
}