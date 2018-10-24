package com.imagebrowse;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.common.utils.DisplayUtils;
import com.common.utils.U;
import com.facebook.drawee.drawable.ProgressBarDrawable;

public class ImageBrowseProgressBar extends ProgressBarDrawable {

    float level;

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    int color = Color.WHITE;

    final RectF oval = new RectF();

    int radius = U.getDisplayUtils().dip2px(30);

    public ImageBrowseProgressBar(){
        paint.setStrokeWidth(U.getDisplayUtils().dip2px(1));
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected boolean onLevelChange(int level) {
        this.level = level;
        invalidateSelf();
        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        oval.set(canvas.getWidth() / 2 - radius, canvas.getHeight() / 2 - radius,
                canvas.getWidth() / 2 + radius, canvas.getHeight() / 2 + radius);

        drawCircle(canvas, level, color);
    }


    private void drawCircle(Canvas canvas, float level, int color) {
        paint.setColor(color);
        float angle;
        angle = 360 / 1f;
        angle = level * angle;
        canvas.drawArc(oval, 0, Math.round(angle), false, paint);
    }
}
