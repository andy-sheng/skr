package com.wali.live.watchsdk.contest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.base.log.MyLog;

/**
 * Created by liuyanyan on 2018/1/23.
 * @module 友乐
 */

public class CustomTextView1 extends android.support.v7.widget.AppCompatTextView {

    public CustomTextView1(Context context) {
        super(context);
        setTextStyle();
    }

    public CustomTextView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextStyle();
    }

    public CustomTextView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTextStyle();
    }

    private void setTextStyle() {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf"));
    }
}
