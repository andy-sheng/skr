package com.base.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.WebView;

import com.base.global.GlobalData;
import com.base.log.MyLog;

import java.lang.reflect.Field;

public class NonLeakingWebView extends WebView {
    public final static String TAG = NonLeakingWebView.class.getSimpleName();
    private static Field sConfigCallback;

    static {
        try {
            sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
            sConfigCallback.setAccessible(true);
        } catch (Exception e) {
            // ignored
        }
    }

    public NonLeakingWebView(Context context) {
        super(context);
        init();
    }

    public NonLeakingWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NonLeakingWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setConfigCallback((WindowManager) GlobalData.app().getSystemService(Context.WINDOW_SERVICE));
    }

    @Override
    public void destroy() {
        MyLog.d(TAG, "destroy");
        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
        // destory()
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }

        stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        getSettings().setJavaScriptEnabled(false);
        clearHistory();
        clearView();
        removeAllViews();

        try {
            super.destroy();
        } catch (Throwable ex) {
            MyLog.d(TAG, ex);
        }
        setConfigCallback(null);
        try {
            if (sConfigCallback != null)
                sConfigCallback.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        MyLog.d(TAG, "destroy over");
    }

    public void setConfigCallback(WindowManager windowManager) {
        try {
            Field field = WebView.class.getDeclaredField("mWebViewCore");
            field = field.getType().getDeclaredField("mBrowserFrame");
            field = field.getType().getDeclaredField("sConfigCallback");
            field.setAccessible(true);
            Object configCallback = field.get(null);

            if (null == configCallback) {
                return;
            }

            field = field.getType().getDeclaredField("mWindowManager");
            field.setAccessible(true);
            field.set(configCallback, windowManager);
        } catch (Exception e) {
        }
    }
}
