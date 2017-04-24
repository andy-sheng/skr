package com.wali.live.sdk.litedemo.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.mi.liveassistant.common.global.GlobalData;

/**
 * @author linjinbin
 * @description Toast工具类，根据resId或者String提示
 */
public class ToastUtils {
    private static Handler sMainHanlder = new Handler(Looper.getMainLooper());

    public static void showToast(int resId) {
        if (null == GlobalData.app()) {
            return;
        }
        final String tips = GlobalData.app().getString(resId);
        showToast(tips);
    }

    public static void showToast(int resId, Object... formatArgs) {
        if (null == GlobalData.app()) {
            return;
        }
        final String tips = GlobalData.app().getString(resId, formatArgs);
        showToast(tips);
    }

    public static void showToast(final String tips) {
        sMainHanlder.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(GlobalData.app(), tips, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
