package com.base.image.fresco.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;

import com.base.common.R;
import com.base.image.fresco.BaseImageView;

/**
 * Created by chengsimin on 16/5/23.
 */
public class TintableImageView extends BaseImageView {
    private ColorStateList tint;

    public TintableImageView(Context context) {
        super(context);
    }

    public TintableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TintableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TintableImageView);
        tint = a.getColorStateList(R.styleable.TintableImageView_tintColor);
        a.recycle();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (tint != null && tint.isStateful())
            updateTintColor();
    }

    public void setColorFilter(ColorStateList tint) {
        this.tint = tint;
        updateTintColor();
    }

    private void updateTintColor() {
        int color = tint.getColorForState(getDrawableState(), 0);
        getHierarchy().setActualImageColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
    }
}
