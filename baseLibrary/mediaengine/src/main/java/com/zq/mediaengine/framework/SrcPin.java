package com.zq.mediaengine.framework;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Source pin definition.
 */
public class SrcPin<T> {
    protected List<SinkPin<T>> sinkPins;
    protected Object format;

    public SrcPin() {
        sinkPins = new LinkedList<>();
    }

    synchronized public boolean isConnected() {
        return !sinkPins.isEmpty();
    }

    synchronized public void connect(@NonNull SinkPin<T> sinkPin) {
        if (sinkPins.contains(sinkPin)) {
            return;
        }
        sinkPins.add(sinkPin);
        sinkPin.onConnected();
        if (format != null) {
            sinkPin.onFormatChanged(format);
        }
    }

    synchronized public void onFormatChanged(Object format) {
        this.format = format;
        for (SinkPin<T> sinkPin : sinkPins) {
            sinkPin.onFormatChanged(format);
        }
    }

    synchronized public void onFrameAvailable(T frame) {
        for (SinkPin<T> sinkPin : sinkPins) {
            sinkPin.onFrameAvailable(frame);
        }

        if (frame == null) {
            return;
        }
        if (frame instanceof AVFrameBase) {
            if ((((AVFrameBase) frame).flags & AVConst.FLAG_END_OF_STREAM) != 0) {
                this.format = null;
            }
        } else if (frame instanceof AVPacketBase) {
            if ((((AVPacketBase) frame).flags & AVConst.FLAG_END_OF_STREAM) != 0) {
                this.format = null;
            }
        }
    }

    /**
     * disconnect all connected sinks
     *
     * @param recursive should the disconnect operation recursive passed
     */
    synchronized public void disconnect(boolean recursive) {
        disconnect(null, recursive);
    }

    /**
     * disconnect with dedicated sink
     *
     * @param sinkPin   dedicated sink to be disconnected, null means all connected sinks
     * @param recursive should the disconnect operation recursive passed
     */
    synchronized public void disconnect(@Nullable SinkPin<T> sinkPin, boolean recursive) {
        if (sinkPin != null) {
            sinkPin.onDisconnect(recursive);
            sinkPins.remove(sinkPin);
        } else {
            for (SinkPin<T> sink : sinkPins) {
                sink.onDisconnect(recursive);
            }
            sinkPins.clear();
        }
    }
}
