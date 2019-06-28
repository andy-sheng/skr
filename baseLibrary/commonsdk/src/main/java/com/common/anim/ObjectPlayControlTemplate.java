package com.common.anim;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.Pair;

import com.common.log.MyLog;
import com.common.utils.CustomHandlerThread;

import java.util.LinkedList;

/**
 * 循环播放动画时的控制模板类，封装点亮、背景、大礼物等播放的基本队列逻辑
 * Created by chengsimin on 16/6/17.
 */
public abstract class ObjectPlayControlTemplate<MODEL, CONSUMER> {
    public static final String MODELAG = "AnimationPlayControlMODELemplate";

    static final int MSG_START_ON_UI = 80;

    static final int MSG_END_ON_UI = 81;

    static final int MSG_ACCEPT_ON_UI = 82;

    public static final int SIZE = 100;//生产者池子里最多多少个

    /**
     * 播放动画队列
     */
    private LinkedList<MODEL> mQueue = new LinkedList<>();

    CustomHandlerThread mHandlerThread;// 保证++ --  都在后台线程操作

    Handler mUiHandler;


    public ObjectPlayControlTemplate() {
        mHandlerThread = new CustomHandlerThread("ObjectPlayControlTemplate") {
            @Override
            protected void processMessage(Message var1) {

            }
        };
        mUiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START_ON_UI:
                        Pair<MODEL, CONSUMER> pair = (Pair<MODEL, CONSUMER>) msg.obj;
                        onStart(pair.first, pair.second);
                        break;
                    case MSG_END_ON_UI:
                        onEnd((MODEL) msg.obj);
                        break;
                    case MSG_ACCEPT_ON_UI:
                        MODEL cur = (MODEL) msg.obj;
                        CONSUMER consumer = accept(cur);
                        mHandlerThread.post(new Runnable() {
                            @Override
                            public void run() {
                                if (consumer != null) {
                                    // 肯定有消费者，才会走到这
                                    MODEL cur = mQueue.poll();
                                    if (cur != null) {
                                        //取出来一个
                                        processInBackGround(cur);
                                        onStartInside(cur, consumer);
                                    }
                                }
                            }
                        });
                        break;
                }
            }
        };
    }

    public void add(MODEL model, boolean must) {
        mHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                if (mQueue.size() < SIZE || must) {
                    mQueue.offer(model);
                }
                play();
            }
        });
    }

    private void play() {
        MODEL cur = mQueue.peekFirst();
        if(cur!=null){
            Message msg = mUiHandler.obtainMessage(MSG_ACCEPT_ON_UI);
            msg.obj = cur;
            mUiHandler.sendMessage(msg);
//            CONSUMER consumer = accept(cur);
//            if (consumer != null) {
//                // 肯定有消费者，才会走到这
//                cur = mQueue.poll();
//                if (cur != null) {
//                    //取出来一个
//                    processInBackGround(cur);
//                    onStartInside(cur, consumer);
//                }
//            }
        }

    }

    /**
     *
     * @param model
     */
    private void onStartInside(MODEL model, CONSUMER consumer) {
        MyLog.d(MODELAG, "onStartInside model:" + model);
        Message msg = mUiHandler.obtainMessage(MSG_START_ON_UI);
        msg.obj = new Pair<>(model, consumer);
        mUiHandler.sendMessage(msg);
    }

    /**
     * 重要，每次消费完，请手动调用告知
     * 这样模型才继续前进 取 下一个消费对象
     *
     * @param model
     */
    public void endCurrent(MODEL model) {
        Message msg = mUiHandler.obtainMessage(MSG_END_ON_UI);
        msg.obj = model;
        mUiHandler.sendMessage(msg);

        mHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                onEndInSide(model);
            }
        });
    }

    private void onEndInSide(MODEL model) {
        MyLog.d(MODELAG, "onEndInSide model:" + model);
        play();
    }

    /**
     * 复位
     */
    public synchronized void reset() {
        mQueue.clear();
    }

    /**
     * 复位
     */
    public synchronized void destroy() {
        mQueue.clear();
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
        mHandlerThread.destroy();
    }


    /**
     * 是否接受这个播放对象
     * 如果不接受 不会从队列移除被消费
     * @param cur
     * @return
     */
    protected abstract CONSUMER accept(MODEL cur);

    /**
     * 某次动画开始时执行
     *
     * @param model
     */
    public abstract void onStart(MODEL model, CONSUMER consumer);

    /**
     * 某次动画结束了执行
     *
     * @param model
     */
    protected abstract void onEnd(MODEL model);

    protected void processInBackGround(MODEL model) {

    }

    public int getSize(){
        return mQueue.size();
    }
    /**
     * 队列这是否还有
     *
     * @return
     */
    public synchronized boolean hasMoreData() {
        return !mQueue.isEmpty();
    }



}
