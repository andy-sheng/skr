package com.common.view.ex;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * Created by feary on 17-8-23.
 */

public class NoLeakEditText extends AppCompatEditText {
    public final static String TAG = "NoLeakEditText";
    private ArrayList<TextWatcher> mAddedList = new ArrayList<>();//已经加入的
    private ArrayList<TextWatcher> mRemovedList = new ArrayList<>();//被remove，待恢复的

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
       com.common.log.MyLog.d(TAG, "addTextChangedListener" + " watcher=" + watcher);
        if (!mAddedList.contains(watcher)) {
            mAddedList.add(watcher);
            super.addTextChangedListener(watcher);
        }
        if (mRemovedList.contains(watcher)) {
            mRemovedList.remove(watcher);
        }
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
       com.common.log.MyLog.d(TAG, "removeTextChangedListener" + " watcher=" + watcher);
        if (mRemovedList.contains(watcher)) {
            mRemovedList.remove(watcher);
        } else {
            if (mAddedList.contains(watcher)) {
                mAddedList.remove(watcher);
                super.removeTextChangedListener(watcher);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
       com.common.log.MyLog.d(TAG, "onAttachedToWindow mAddedList.size:" + mAddedList.size() + ",mRemovedList.size:" + mRemovedList.size());
        super.onAttachedToWindow();
        for (TextWatcher watcher : mRemovedList) {
            super.addTextChangedListener(watcher);
            mAddedList.add(watcher);
        }
        mRemovedList.clear();
        setCursorVisible(true);
    }


    @Override
    protected void onDetachedFromWindow() {
       com.common.log.MyLog.d(TAG, "onDetachedFromWindow mAddedList.size:" + mAddedList.size() + ",mRemovedList.size:" + mRemovedList.size());
        for (TextWatcher watcher : mAddedList) {
            super.removeTextChangedListener(watcher);
            mRemovedList.add(watcher);
        }
        mAddedList.clear();
        clearFocus();
        /**
         * 防止 blink 内存泄漏
         */
        setCursorVisible(false);
        getHandler().removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }
}
