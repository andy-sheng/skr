package com.imagebrowse;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.common.log.MyLog;
import com.common.utils.DisplayUtils;
import com.common.utils.U;
import com.facebook.drawee.drawable.ProgressBarDrawable;

public class ImageBrowseProgressBar extends ProgressBarDrawable {

    float level;

    Paint paint = new com.common.view.ExPaint(Paint.ANTI_ALIAS_FLAG);

    int color = Color.WHITE;

    final RectF oval = new RectF();

    int radius = U.getDisplayUtils().dip2px(30);

    public ImageBrowseProgressBar() {
        paint.setStrokeWidth(U.getDisplayUtils().dip2px(1));
        paint.setStyle(Paint.Style.FILL);
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

    /**
     * level=9900.0 color=-1 angle=3564000.0
     *
     * @param canvas
     * @param level
     * @param color
     */
    private void drawCircle(Canvas canvas, float level, int color) {
        paint.setColor(color);
        float angle = 360 * level / 10000.0f;
        MyLog.d("ImageBrowseProgressBar", "drawCircle canvas=" + canvas + " level=" + level + " color=" + color + " angle=" + angle);
        canvas.drawArc(oval, 0, Math.round(angle), true, paint);
    }
}
