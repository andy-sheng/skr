package com.module.playways.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;

import com.common.log.MyLog;

import java.util.Random;

public class ZanBean {
    public final static String TAG = "ZanBean";
    /**
     * 心的当前坐标
     */
    public Point point;

    /**
     * 心图
     */
    private Bitmap bitmap;
    /**
     * 绘制bitmap的矩阵  用来做缩放和移动的
     */
    private Matrix matrix = new Matrix();
    /**
     * 缩放系数
     */
    private float sf = 0;
    /**
     * 产生随机数
     */
    private Random random;

    /**
     * 透明度
     */
    public int alpha = 255;

    public boolean isEnd = false;//是否结束

    int duration = 3000, nowDuration = 0;

    Point startPoint;

    Point endPoint;

    BezierEvaluator mBezierEvaluator;

    public static final int DURATION_INTERVAL = 10;

    public ZanBean(Context context, int resId, ZanView zanView) {
        random = new Random();
        bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        int sx = 50+random.nextInt(zanView.getWidth()-50);
        startPoint = new Point(sx, zanView.getHeight() - bitmap.getHeight() / 2);

        int ex = sx + 50 - random.nextInt(100);
        endPoint = new Point(ex, random.nextInt(zanView.getHeight() / 5));
        int cx = random.nextInt(startPoint.x * 2 + 1);
        int cy = Math.abs(endPoint.y + startPoint.y) / 2;
        Point centerPoint = new Point(cx, cy);
        mBezierEvaluator = new BezierEvaluator(centerPoint);
    }

    /**
     * 主要绘制函数
     */
    public void draw(Canvas canvas, Paint p) {
        nowDuration += DURATION_INTERVAL;
        float t = nowDuration / (duration * 1.0f);
        point = mBezierEvaluator.evaluate(t, startPoint, endPoint);
        alpha = (int) ((1 - t) * 255);
        sf = 0.5f + t * 3.0f;
        if (bitmap != null && t <= 1) {
            p.setAlpha(alpha);
            matrix.setScale(sf, sf, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            matrix.postTranslate(point.x - bitmap.getWidth() / 2, point.y - bitmap.getHeight() / 2);
            canvas.drawBitmap(bitmap, matrix, p);
        } else {
            isEnd = true;
        }
    }

    public void stop() {

    }

    /**
     * 二次贝塞尔曲线
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class BezierEvaluator implements TypeEvaluator<Point> {

        private Point centerPoint;

        public BezierEvaluator(Point centerPoint) {
            this.centerPoint = centerPoint;
        }

        @Override
        public Point evaluate(float t, Point startValue, Point endValue) {
            int x = (int) ((1 - t) * (1 - t) * startValue.x + 2 * t * (1 - t) * centerPoint.x + t * t * endValue.x);
            int y = (int) ((1 - t) * (1 - t) * startValue.y + 2 * t * (1 - t) * centerPoint.y + t * t * endValue.y);
            return new Point(x, y);
        }

    }
}
