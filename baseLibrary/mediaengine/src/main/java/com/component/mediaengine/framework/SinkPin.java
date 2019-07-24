package com.component.mediaengine.framework;

import java.util.LinkedList;
import java.util.List;

/**
 * Sink pin definition
 */
public abstract class SinkPin<T extends AVFrameBase> {
    protected List<SrcPin<T>> mSrcPins;
    protected boolean mIsConnected = false;

    public SinkPin() {
        mSrcPins = new LinkedList<>();
    }

    public synchronized void onConnected() {
        mIsConnected = true;
    }

    public synchronized void onConnected(SrcPin<T> srcPin) {
        if (mSrcPins.contains(srcPin)) {
            return;
        }
        mSrcPins.add(srcPin);
        onConnected();
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

    public synchronized void onDisconnect(SrcPin<T> srcPin, boolean recursive) {
        mSrcPins.remove(srcPin);
        if (mSrcPins.isEmpty()) {
            onDisconnect(recursive);
        }
    }
}
