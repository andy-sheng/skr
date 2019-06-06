package com.zq.mediaengine.framework;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Source pin definition.
 */
public class SrcPin<T extends AVFrameBase> {
    protected List<SinkPin<T>> sinkPins;
    protected Map<SinkPin, Boolean> isFormatChangedMap;
    protected Object format;

    public SrcPin() {
        sinkPins = new LinkedList<>();
        isFormatChangedMap = new LinkedHashMap<>();
    }

    synchronized public boolean isConnected() {
        return !sinkPins.isEmpty();
    }

    synchronized public void connect(@NonNull SinkPin<T> sinkPin) {
        if (sinkPins.contains(sinkPin)) {
            return;
        }
        sinkPins.add(sinkPin);
        isFormatChangedMap.put(sinkPin, false);
        sinkPin.onConnected(this);
    }

    synchronized public void onFormatChanged(Object format) {
        this.format = format;
        for (SinkPin<T> sinkPin : sinkPins) {
            sinkPin.onFormatChanged(format);
            isFormatChangedMap.put(sinkPin, true);
        }
    }

    synchronized public void onFrameAvailable(T frame) {
        for (SinkPin<T> sinkPin : sinkPins) {
            if (!isFormatChangedMap.get(sinkPin)) {
                sinkPin.onFormatChanged(format);
                isFormatChangedMap.put(sinkPin, true);
            }
            sinkPin.onFrameAvailable(frame);
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
            sinkPin.onDisconnect(this, recursive);
            sinkPins.remove(sinkPin);
            isFormatChangedMap.remove(sinkPin);
        } else {
            for (SinkPin<T> sink : sinkPins) {
                sink.onDisconnect(this, recursive);
            }
            sinkPins.clear();
            isFormatChangedMap.clear();
        }
    }
}
