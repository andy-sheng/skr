package com.wali.live.watchsdk.income.view;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;

/**
 * Created by feary on 17-8-23.
 */

public class NoLeakEditText extends android.support.v7.widget.AppCompatEditText {

    private TextWatcher mWatcher;

    public NoLeakEditText(Context context) {
        super(context);
    }

    public NoLeakEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoLeakEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
        mWatcher = watcher;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mWatcher != null) {
            addTextChangedListener(mWatcher);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mWatcher != null) {
            removeTextChangedListener(mWatcher);
        }
        super.onDetachedFromWindow();
    }
}
