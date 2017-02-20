package com.wali.live.common.heartview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

import java.util.Random;

/**
 * Created by chengsimin on 16/8/12.
 */
public class HeartItem {

    public boolean isWorking = false;

    Random random = new Random();

    private Bitmap bitmap;

    private PointF P1;
    private PointF P2;
    private PointF P0;

    private float t;// 当前的进度 0 - 1
    private float speed;// 加进度的速度

    private int rotate;
    private float scale;

    private Paint paint;

    private int min_x;
    private int max_x;
    private int min_y;
    private int max_y;

    HeartItemManager manager;

    public HeartItem(HeartItemManager manager) {
        paint = new Paint();
        paint.setAntiAlias(true);
        this.manager = manager;
    }

    public void init(Bitmap bitmap,int width, int height) {
        if (width == 0 || height == 0 || bitmap==null) {
            isWorking = false;
            return;
        }
        isWorking = true;
        this.bitmap = bitmap;
        scale = manager.getRandomScale();
        this.speed = 0.007f + random.nextFloat() / 250f;

        min_x = (int) (0.42 * bitmap.getWidth() * scale);
        max_x = width - (int) (bitmap.getWidth() * 1.42 * scale);
        min_y = 0;
        max_y = height - (int) (bitmap.getHeight() * 1.42 * scale);

        rotate = (int) (Math.random() * 90) - 45;
        this.P0 = new PointF(min_x + (max_x - min_x) / 2, max_y);
        this.P2 = new PointF(2 * random.nextInt(min_x), min_y);
        this.P1 = new PointF(this.random.nextInt(max_x + 2 * min_x) - min_x, max_y / 2);
        this.t = 0f;
    }

    public void onDraw(Canvas canvas) {
        // 防止出现空指针，bug 4329
        // 问题原因：空指针外层捕获，导致画布锁未释放，从而在setVisibility的内部方法updateWindow等锁，出现anr的问题
        if (P0 == null || P1 == null || P2 == null || bitmap==null) {
            return;
        }
        float bt = 1 - t;
        float x = correctX(bt * bt * this.P0.x + t * (2.0F * bt) * this.P1.x + t * (2.0F * t) * this.P2.x);
        float y = correctY(bt * (max_y - min_y) + min_y);
        float s = correctScale((t / 0.1f + 0.2f) * this.scale);
        float alpha = correctAlpha(bt / 0.2f);
        paint.setAlpha((int) (alpha * 255));
        Matrix matrix = new Matrix();
        float cx = this.bitmap.getWidth() / 2, cy = this.bitmap.getHeight() / 2;
        matrix.setRotate(rotate, cx, cy);
        matrix.postScale(s, s, cx, cy);
        matrix.postTranslate(x, y);
        canvas.drawBitmap(bitmap, matrix, paint);
        t += speed;
        if (t > 1.0) {
            isWorking = false;
        }
    }

    private float correctX(float x) {
        if (x < min_x)
            return min_x;
        if (x > max_x)
            return max_x;
        return x;
    }

    private float correctY(float y) {
        if (y < min_y)
            return min_y;
        if (y > max_y)
            return max_y;
        return y;
    }

    private float correctScale(float s) {
        if (s > scale)
            return scale;
        if (s < 0)
            return 0;
        return s;
    }

    private float correctAlpha(float s) {
        if (s > manager.getStarAlpha())
            return manager.getStarAlpha();
        if (s < 0)
            return 0;
        return s;
    }
}