package com.wali.live.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.live.module.common.R;


public class StrokeTextView extends TextView {
    private TextView mOutTv = null;

    public StrokeTextView(Context paramContext) {
        super(paramContext);
        this.mOutTv = new TextView(paramContext);
        init();
    }

    public StrokeTextView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        this.mOutTv = new TextView(paramContext, paramAttributeSet);
        init();
    }

    public StrokeTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        this.mOutTv = new TextView(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void init() {
        TextPaint localTextPaint = this.mOutTv.getPaint();
        localTextPaint.setStrokeWidth(5.0F);
        localTextPaint.setStyle(Paint.Style.STROKE);
        this.mOutTv.setTextColor(getResources().getColor(R.color.outer_stroketextview));
        this.mOutTv.setGravity(getGravity());
    }

    protected void onDraw(Canvas paramCanvas) {
        this.mOutTv.draw(paramCanvas);
        super.onDraw(paramCanvas);
    }

    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
        this.mOutTv.layout(paramInt1, paramInt2, paramInt3, paramInt4);
    }

    protected void onMeasure(int paramInt1, int paramInt2) {
        CharSequence localCharSequence = this.mOutTv.getText();
        if ((localCharSequence == null) || (!localCharSequence.equals(getText()))) {
            this.mOutTv.setText(getText());
            postInvalidate();
        }
        super.onMeasure(paramInt1, paramInt2);
        if (this.mOutTv != null)
            this.mOutTv.measure(paramInt1, paramInt2);
    }

    public void setLayoutParams(ViewGroup.LayoutParams paramLayoutParams) {
        super.setLayoutParams(paramLayoutParams);
        if (this.mOutTv != null)
            this.mOutTv.setLayoutParams(paramLayoutParams);
    }

    @Override
    public void setGravity(int gravity) {
        super.setGravity(gravity);
        if (this.mOutTv != null)
            this.mOutTv.setGravity(gravity);
    }

    @Override
    public void setMinWidth(int minpixels) {
        super.setMinWidth(minpixels);
        if (this.mOutTv != null)
            this.mOutTv.setMinWidth(minpixels);
    }

    public void setOutTextColor(int colorId){
        if(this.mOutTv != null){
            this.mOutTv.setTextColor(getResources().getColor(colorId));
            postInvalidate();
        }
    }

    public void setText(CharSequence paramCharSequence, BufferType paramBufferType) {
        super.setText(paramCharSequence, BufferType.NORMAL);
        if (this.mOutTv != null)
            this.mOutTv.setText(paramCharSequence);
    }

    public void setVisibility(int paramInt) {
        super.setVisibility(paramInt);
        if (this.mOutTv != null)
            this.mOutTv.setVisibility(paramInt);
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        if (this.mOutTv != null)
            this.mOutTv.setTextSize(size);
    }
}