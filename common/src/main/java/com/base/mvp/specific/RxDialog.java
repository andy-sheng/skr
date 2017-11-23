package com.base.mvp.specific;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.base.common.R;
import com.base.mvp.IRxView;
import com.base.utils.display.DisplayUtils;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by lan on 2017/11/16.
 */
public abstract class RxDialog extends Dialog implements IRxView {
    protected static final int DEFAULT_PADDING = DisplayUtils.dip2px(8);

    protected final String TAG = getTAG();

    protected Context mContext;
    protected BehaviorSubject<PresenterEvent> mBehaviorSubject;

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public RxDialog(@NonNull Context context) {
        this(context, R.style.MyAlertDialog);
    }

    public RxDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    protected abstract void init();

    protected void setWindow() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.windowAnimations = R.style.MyDialogAnimation;
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.getDecorView().setPadding(DEFAULT_PADDING, 0, DEFAULT_PADDING, DEFAULT_PADDING);
        window.setAttributes(lp);
    }

    public void show() {
        super.show();
        mBehaviorSubject = BehaviorSubject.create();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBehaviorSubject.onNext(PresenterEvent.DESTROY);
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return PresenterEvent.bindUntilEvent(mBehaviorSubject, PresenterEvent.DESTROY);
    }

    protected final <V extends View> V $(@IdRes int resId) {
        return (V) findViewById(resId);
    }

    protected final <V extends View> V $click(@IdRes int id, View.OnClickListener listener) {
        V v = $(id);
        if (v != null) {
            v.setOnClickListener(listener);
        }
        return v;
    }
}
