
package com.base.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;

public class MLTextView extends TextView {
    private MLTextPaint mPaint;

    public MLTextView(Context context) {
        super(context);
        setTextStyle();
    }

    public MLTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextStyle();
    }

    public MLTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTextStyle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (ArrayIndexOutOfBoundsException e) {
            MyLog.e(e);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(widthSize, heightSize);
        } catch (IndexOutOfBoundsException e) {
            MyLog.e(e);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (ArrayIndexOutOfBoundsException e) {
            MyLog.e(e);
        } catch (IndexOutOfBoundsException e) {
            MyLog.e(e);
        }
    }

    @Override
    public TextPaint getPaint() {
        if (mPaint == null) {
            mPaint = new MLTextPaint(this, super.getPaint());
        }
        return mPaint;
    }

    private void setTextStyle() {
        if (null != getTypeface() && getTypeface().isBold()) {
            if (CommonUtils.isMIUIRom()) {
                setTypeface(Typeface.DEFAULT_BOLD);
            }
        }
    }
}
