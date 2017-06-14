package com.wali.live.watchsdk.videodetail.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by yangli on 2017/6/14.
 */
public class SpanClickView extends android.support.v7.widget.AppCompatTextView {
    private static final String TAG = "SpanClickView";

    private boolean mIsClickSpan = false;
    private boolean mHasCreateContextMenu = false;

    public SpanClickView(Context context) {
        super(context);
    }

    public SpanClickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SpanClickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean superResult = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mIsClickSpan = getSelectionStart() != -1;
            mHasCreateContextMenu = false;
        }
        return superResult;
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        ((View) getParent()).setBackgroundColor(pressed && !mIsClickSpan ? 0x19000000 : 0); // color_black_trans_10 : transparent
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        mHasCreateContextMenu = true;
    }

    @Override
    public boolean performClick() {
        if (mIsClickSpan || mHasCreateContextMenu) {
            return true;
        }
        return super.performClick();
    }
}
