package com.common.utils;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.common.log.MyLog;

public abstract class CustomHandlerThread {
    protected HandlerThread mHandlerThread;
    protected Handler mHandler;

    public CustomHandlerThread(String name) {
        this(name, 0);
    }

    public CustomHandlerThread(String name, int priority) {
        this.mHandlerThread = new HandlerThread(name, priority);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                try {
                    CustomHandlerThread.this.processMessage(msg);
                } catch (Exception var3) {
                    MyLog.d(var3);
                }

            }
        };
    }

    public Message obtainMessage() {
        return this.mHandler.obtainMessage();
    }

    public void sendMessage(Message msg) {
        this.mHandler.sendMessage(msg);
    }

    public void sendMessageDelayed(Message msg, long delayMillis) {
        this.mHandler.sendMessageDelayed(msg, delayMillis);
    }

    public void removeMessage(int what) {
        this.mHandler.removeMessages(what);
    }

    public void removeMessage(int what, Object obj) {
        this.mHandler.removeMessages(what, obj);
    }

    public void removeCallbacksAndMessages(Object token) {
        this.mHandler.removeCallbacksAndMessages(token);
    }

    public final boolean post(Runnable r) {
        return this.mHandler.post(r);
    }

    public final boolean postDelayed(Runnable r, long delayMillis) {
        return this.mHandler.postDelayed(r, delayMillis);
    }

    protected abstract void processMessage(Message var1);

    public void destroy() {
        this.mHandlerThread.quitSafely();
    }

    public Looper getLooper() {
        return this.mHandlerThread.getLooper();
    }

    public void setHandler(Handler h) {
        if (h != null) {
            if (h.getLooper() != this.getLooper()) {
                throw new IllegalArgumentException("Looper对象不一致，请使用CustomHandlerThread.getLooper()构造Handler对象");
            }

            this.mHandler = h;
        }

    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public HandlerThread getHandlerThread() {
        return this.mHandlerThread;
    }
}
