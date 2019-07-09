package com.module.playways.view;

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
     * 移动动画
     */
    private ValueAnimator moveAnim;
    /**
     * 放大动画
     */
    private ValueAnimator zoomAnim;
    /**
     * 透明度
     */
    public int alpha = 255;//
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
    public boolean isEnd = false;//是否结束


    public ZanBean(Context context, int resId, ZanView zanView) {
        random = new Random();
        bitmap = BitmapFactory.decodeResource(context.getResources(), resId);

        init(new Point(zanView.getWidth() / 2, zanView.getHeight() - bitmap.getHeight() / 2), new Point((random.nextInt(zanView.getWidth())), 0));
    }


    public ZanBean(Bitmap bitmap, ZanView zanView) {
        random = new Random();
        this.bitmap = bitmap;
        //为了让在起始坐标点时显示完整 需要减去bitmap.getHeight()/2

        init(new Point(zanView.getWidth() / 2, zanView.getHeight() - bitmap.getHeight() / 2), new Point((random.nextInt(zanView.getWidth())), 0));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void init(final Point startPoint, Point endPoint) {
        moveAnim = ValueAnimator.ofObject(new BezierEvaluator(new Point(random.nextInt(startPoint.x * 2), Math.abs(endPoint.y - startPoint.y) / 2)), startPoint, endPoint);
        moveAnim.setDuration(1500);
        moveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                point = (Point) animation.getAnimatedValue();
                alpha = (int) ((float) point.y / (float) startPoint.y * 255);
            }
        });
        moveAnim.start();
        zoomAnim = ValueAnimator.ofFloat(0, 1f).setDuration(700);
        zoomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float f = (Float) animation.getAnimatedValue();
                sf = f.floatValue();
            }
        });
        zoomAnim.start();
    }

//    public void pause(){
//        if(moveAnim !=null&& moveAnim.isRunning()){
//            moveAnim.pause();
//        }
//        if(zoomAnim !=null&& zoomAnim.isRunning()){
//            zoomAnim.pause();
//        }
//    }
//
//    public void resume(){
//        if(moveAnim !=null&& moveAnim.isPaused()){
//            moveAnim.resume();
//        }
//        if(zoomAnim !=null&& zoomAnim.isPaused()){
//            zoomAnim.resume();
//        }
//    }

    public void stop() {
        if (moveAnim != null) {
            moveAnim.cancel();
            moveAnim = null;
        }
        if (zoomAnim != null) {
            zoomAnim.cancel();
            zoomAnim = null;
        }
    }

    /**
     * 主要绘制函数
     */
    public void draw(Canvas canvas, Paint p) {
        MyLog.d(TAG, "draw" + " bitmap=" + bitmap + " alpha=" + alpha);
        if (bitmap != null && alpha > 0) {
            MyLog.d(TAG, "draw canvas 1" + " alpha=" + alpha);
            p.setAlpha(alpha);
            matrix.setScale(sf, sf, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            matrix.postTranslate(point.x - bitmap.getWidth() / 2, point.y - bitmap.getHeight() / 2);
            canvas.drawBitmap(bitmap, matrix, p);
            MyLog.d(TAG, "draw canvas 2" + " alpha=" + alpha);
        } else {
            MyLog.d(TAG, "draw canvas 3");
            isEnd = true;
        }
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
