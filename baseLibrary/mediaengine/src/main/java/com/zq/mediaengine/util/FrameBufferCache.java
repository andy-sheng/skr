package com.zq.mediaengine.util;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Frame ByteBuffer cache
 *
 * @hide
 */
public class FrameBufferCache {
    private static final String TAG = "FrameBufferCache";

    private BlockingQueue<ByteBuffer> cache;

    public FrameBufferCache(int num, int itemSize) {
        cache = new ArrayBlockingQueue<>(num);
        for (int i=0; i<num; i++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(itemSize);
            cache.add(buffer);
        }
    }

    public ByteBuffer poll(int size) {
        return poll(size, -1);
    }

    public ByteBuffer poll(int size, long timeout) {
        ByteBuffer buffer = null;
        try {
            if (timeout == -1) {
                buffer = cache.poll();
            } else {
                buffer = cache.poll(timeout, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            Log.d(TAG, "get cache buffer interrupted");
        }

        if (buffer != null) {
            if (size > buffer.capacity()) {
                int newSize = buffer.capacity() * 2;
                while (newSize < size) {
                    newSize *= 2;
                }
                Log.d(TAG, "realloc buffer size from " + buffer.capacity() + " to " + newSize);
                buffer = ByteBuffer.allocateDirect(newSize);
            }
            buffer.clear();
        }
        return buffer;
    }

    public void add(ByteBuffer buffer) {
        // use add to throw exception for invalid use
        cache.add(buffer);
    }

    public boolean offer(ByteBuffer buffer) {
        boolean ret = cache.offer(buffer);
        if (!ret) {
            Log.e(TAG, "offered extra invalid buffer!");
        }
        return ret;
    }

    public void clear() {
        cache.clear();
    }
}
