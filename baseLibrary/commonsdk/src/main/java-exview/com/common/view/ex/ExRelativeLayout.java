package com.common.view.ex;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.view.ex.shadow.Config;
import com.common.view.ex.shadow.ShadowHelper;

public class ExRelativeLayout extends RelativeLayout {

    public ExRelativeLayout(Context context) {
        super(context);
    }

    public ExRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context, attrs);
    }

    public ExRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context, attrs);
    }

    private void loadAttributes(Context context, AttributeSet attrs) {
        AttributeInject.injectBackground(this, context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.background);
        int shadowColor = typedArray.getInt(R.styleable.background_bl_shadow_Color, Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //实现阴影
        ShadowHelper.draw(canvas, this,
                Config.obtain()
                        .color(Color.RED)
                        .leftTopCorner(20)
                        .rightTopCorner(30)
                        .leftBottomCorner(40)
                        .rightBottomCorner(50)
                        .radius(60)
                        .xOffset(30)
                        .yOffset(30)
        );
    }
}
