package com.module.playways.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.TextureView;

import com.common.log.MyLog;
import com.module.playways.R;

import java.util.ArrayList;

public class ZanView extends TextureView implements TextureView.SurfaceTextureListener {
    public final static String TAG = "ZanView";
    public final static int ADD_XIN_MSG = 0;
    public final static int DRAW_XIN_MSG = 1;
//    private SurfaceHolder mSurfaceHolder;

    /**
     * 心的个数
     */
    private ArrayList<ZanBean> mBeanArrayList = new ArrayList<>();
    private Paint p;

    HandlerThread mDandlerThread;

    Handler mHandler;

    //TextureView是否销毁
    boolean mIsDestroy = false;

    public ZanView(Context context) {
        this(context, null);
    }

    public ZanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //设置透明
        setOpaque(false);
//        this.setZOrderOnTop(true);
//        /**设置画布  背景透明*/
//        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
//        mSurfaceHolder = getHolder();
//        mSurfaceHolder.addCallback(this);
        setSurfaceTextureListener(this);
        p = new Paint();
        p.setAntiAlias(true);

        mDandlerThread = new HandlerThread("ZanView HandlerThread");
        mDandlerThread.start();

        mHandler = new Handler(mDandlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == ADD_XIN_MSG) {
                    realAddZanXin();
                    if (!mHandler.hasMessages(DRAW_XIN_MSG)) {
                        mHandler.sendEmptyMessageDelayed(DRAW_XIN_MSG, 0);
                    }
                } else if (msg.what == DRAW_XIN_MSG) {
                    drawXin();
                }
                return true;
            }
        });
    }

    /**
     * 点赞动作  添加心的函数 控制画面最大心的个数
     */
    public void addZanXin(int count) {
        if (count < 1) {
            return;
        }
        int delay = 200 / count;
        for (int i = 0; i < count; i++) {
            mHandler.sendEmptyMessageDelayed(ADD_XIN_MSG, delay * i);
        }
    }

    private void realAddZanXin() {
        ZanBean zanBean = new ZanBean(ZanView.this.getContext(), R.drawable.srf_xin, ZanView.this);
        mBeanArrayList.add(zanBean);
        if (mBeanArrayList.size() > 80) {
            mBeanArrayList.remove(0);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mHandler.sendEmptyMessageDelayed(DRAW_XIN_MSG, 0);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mHandler.removeCallbacksAndMessages(null);
        mIsDestroy = true;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void drawXin() {
        Canvas canvas = null;
        try {
            canvas = lockCanvas();
            /**清除画面*/
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            /**对所有心进行遍历绘制*/
            java.util.Iterator<ZanBean> iterable = mBeanArrayList.iterator();
            while (iterable.hasNext()) {
                ZanBean bean = iterable.next();
                if (bean.isEnd) {
                    iterable.remove();
                    continue;
                }

                if (!mIsDestroy) {
                    bean.draw(canvas, p);
                }
            }
            /**这里做一个性能优化的动作，由于线程是死循环的 在没有心需要的绘制的时候会结束线程*/
            if (mBeanArrayList.size() == 0) {
                MyLog.d(TAG, "mBeanArrayList 为空，画完了");
            } else {
                mHandler.sendEmptyMessageDelayed(DRAW_XIN_MSG, ZanBean.DURATION_INTERVAL);
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
            mHandler.sendEmptyMessageDelayed(DRAW_XIN_MSG, ZanBean.DURATION_INTERVAL);
        } finally {
            if (canvas != null) {
                try {
                    unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }
            }
        }
    }

    public void destroy() {
        java.util.Iterator<ZanBean> iterable = mBeanArrayList.iterator();
        MyLog.d(TAG, "run DrawThread " + Thread.currentThread().hashCode());
        while (iterable.hasNext()) {
            ZanBean bean = iterable.next();
            bean.stop();
        }

        mHandler.removeCallbacksAndMessages(null);
        mDandlerThread.quit();

    }
}