package com.zq.mediaengine.framework;

/**
 * Sink pin definition
 */
public abstract class SinkPin<T> {
    private boolean mIsConnected = false;
    public synchronized void onConnected(){
        mIsConnected = true;
    }

    public boolean isConnected(){
        return mIsConnected;
    }

    public abstract void onFormatChanged(Object format);

    public abstract void onFrameAvailable(T frame);

    /**
     * this sink pin has been disconnect
     * @param recursive should release current sink module and
     *                  disconnect sinks connect to current module
     */
    public synchronized void onDisconnect(boolean recursive){
        mIsConnected = false;
    }
}
