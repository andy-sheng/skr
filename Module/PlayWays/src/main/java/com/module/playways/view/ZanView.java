package com.module.playways.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.common.log.MyLog;

import java.util.ArrayList;
import java.util.HashSet;

public class ZanView extends SurfaceView implements SurfaceHolder.Callback {
    public final static String TAG = "ZanView";
    private SurfaceHolder surfaceHolder;

    /**
     * 心的个数
     */
    private ArrayList<ZanBean> mBeanArrayList = new ArrayList<>();
    private Paint p;
    /**
     * 负责绘制的工作线程
     */
    private DrawThread drawThread;

    public ZanView(Context context) {
        this(context, null);
    }

    public ZanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setZOrderOnTop(true);
        /**设置画布  背景透明*/
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        p = new Paint();
        p.setAntiAlias(true);
        drawThread = new DrawThread();
    }

    /**
     * todo 这里优化
     * 点赞动作  添加心的函数 控制画面最大心的个数
     */
    public void addZanXin(ZanBean zanBean) {
        synchronized (surfaceHolder) {
            mBeanArrayList.add(zanBean);
            if (mBeanArrayList.size() > 40) {
                mBeanArrayList.remove(0);
            }
            start();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (drawThread == null) {
            drawThread = new DrawThread();
        }
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (drawThread != null) {
            drawThread.isRun = false;
            drawThread = null;
        }
    }

    class DrawThread extends Thread {
        boolean isRun = true;

        @Override
        public void run() {
            super.run();
            /**绘制的线程 死循环 不断的跑动*/
            while (isRun) {
                Canvas canvas = null;
                try {
                    synchronized (surfaceHolder) {
                        canvas = surfaceHolder.lockCanvas();
                        /**清除画面*/
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                        /**对所有心进行遍历绘制*/
                        java.util.Iterator<ZanBean> iterable = mBeanArrayList.iterator();
                        MyLog.d(TAG, "run DrawThread " + Thread.currentThread().hashCode());
                        while (iterable.hasNext()) {
                            ZanBean bean = iterable.next();
                            if (bean.isEnd) {
                                iterable.remove();
                                continue;
                            }

                            bean.draw(canvas, p);
                        }

                        MyLog.d(TAG, "run + mBeanArrayList size is " + mBeanArrayList.size());
                        /**这里做一个性能优化的动作，由于线程是死循环的 在没有心需要的绘制的时候会结束线程*/
                        if (mBeanArrayList.size() == 0) {
                            isRun = false;
                            drawThread = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
                try {
                    /**用于控制绘制帧率*/
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        if (drawThread != null) {

//            for (int i = 0; i < mBeanArrayList.size(); i++) {
//                mBeanArrayList.get(i).pause();
//            }
//            for (int i = 0; i < mBeanArrayList.size(); i++) {
//                mBeanArrayList.get(i).stop();
//            }

            java.util.Iterator<ZanBean> iterable = mBeanArrayList.iterator();
            MyLog.d(TAG, "run DrawThread " + Thread.currentThread().hashCode());
            while (iterable.hasNext()) {
                ZanBean bean = iterable.next();
                bean.stop();
            }

            drawThread.isRun = false;
            drawThread = null;
        }

    }

    public void start() {
        if (drawThread == null) {
//            for (int i = 0; i < mBeanArrayList.size(); i++) {
//                mBeanArrayList.get(i).resume();
//            }
            drawThread = new DrawThread();
            drawThread.start();
        }
    }
}