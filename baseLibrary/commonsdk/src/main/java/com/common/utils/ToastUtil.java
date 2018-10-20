package com.common.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.common.base.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 通过U.getToastUtils 获得
 */
public class ToastUtil {

    ToastUtil() {
    }

    public void showToast(int resId) {
        if (null == U.app()) {
            return;
        }
        final String tips = U.app().getString(resId);
        showToast(tips);
    }

    public void showToast(int resId, Object... formatArgs) {
        if (null == U.app()) {
            return;
        }
        final String tips = U.app().getString(resId, formatArgs);
        showToast(tips);
    }

    public void showToast(final String tips) {
        if (U.getCommonUtils().isMainThread()) {
            Toast.makeText(U.app(), tips, Toast.LENGTH_SHORT).show();
        } else {
            U.getCommonUtils().getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(U.app(), tips, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
