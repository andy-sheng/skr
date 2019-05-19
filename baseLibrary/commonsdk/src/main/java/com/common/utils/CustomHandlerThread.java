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

    boolean hasDestroy = false;

    public CustomHandlerThread(String name) {
        this(name, 0);
    }

    public CustomHandlerThread(String name, int priority) {
        this.mHandlerThread = new HandlerThread(name, priority);
        this.mHandlerThread.start();
        hasDestroy = false;
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
        if(hasDestroy){
            return ;
        }
        this.mHandler.sendMessage(msg);
    }

    public void sendMessageDelayed(Message msg, long delayMillis) {
        if(hasDestroy){
            return ;
        }
        this.mHandler.sendMessageDelayed(msg, delayMillis);
    }

    public void removeMessage(int what) {
        this.mHandler.removeMessages(what);
    }

    public void removeMessage(int what, Object obj) {
        this.mHandler.removeMessages(what, obj);
    }

    public final boolean post(Runnable r) {
        if(hasDestroy){
            return false;
        }
        return this.mHandler.post(r);
    }

    public final boolean postDelayed(Runnable r, long delayMillis) {
        if(hasDestroy){
            return false;
        }
        return this.mHandler.postDelayed(r, delayMillis);
    }

    protected abstract void processMessage(Message var1);

    public void destroy() {
        hasDestroy = true;
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
        this.mHandlerThread.quit();
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
