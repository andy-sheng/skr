package com.component.mediaengine.util;

import android.util.Log;
import android.util.SparseIntArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Frame ByteBuffer cache
 *
 * @hide
 */
public class FrameBufferCache {
    private static final String TAG = "FrameBufferCache";
    private static final boolean VERBOSE = false;

    private int mTotalCount;
    private int mItemSize;
    private int mCount;
    private BlockingQueue<ByteBuffer> mQueue;
    private final SparseIntArray mRefMap;

    /**
     * Create FrameBufferCache instance.
     *
     * @param num      ByteBuffer number, 0 means unlimited(Integer.MAX_VALUE)
     * @param itemSize size of each ByteBuffer item
     */
    public FrameBufferCache(int num, int itemSize) {
        mTotalCount = num;
        mItemSize = itemSize;
        mCount = 0;
        if (num == 0) {
            mQueue = new LinkedBlockingQueue<>();
        } else {
            mQueue = new ArrayBlockingQueue<>(num);
        }
        mRefMap = new SparseIntArray();
    }

    public ByteBuffer take(int size) {
        return poll(size, -1);
    }

    public ByteBuffer poll(int size) {
        return poll(size, 0);
    }

    public ByteBuffer poll(int size, long timeout) {
        ByteBuffer buffer = null;
        if (mQueue.isEmpty() && (mTotalCount == 0 || mCount < mTotalCount)) {
            int newSize = mItemSize;
            if(size > newSize) {
                newSize = mItemSize * 2;
                while (newSize < size) {
                    newSize *= 2;
                }
            }
            buffer = ByteBuffer.allocateDirect(newSize);
            buffer.order(ByteOrder.nativeOrder());
            mCount++;
            if (VERBOSE) {
                Log.d(TAG, "alloc " + newSize + " bytes ByteBuffer, current count " +
                        mCount + ", total limit " + mTotalCount);
            }
        } else {
            try {
                if (timeout == -1) {
                    buffer = mQueue.take();
                } else {
                    buffer = mQueue.poll(timeout, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                Log.d(TAG, "get cache buffer interrupted");
            }
        }

        if (buffer != null) {
            if (size > buffer.capacity()) {
                int newSize = buffer.capacity() * 2;
                while (newSize < size) {
                    newSize *= 2;
                }
                Log.d(TAG, "realloc buffer size from " + buffer.capacity() + " to " + newSize);
                buffer = ByteBuffer.allocateDirect(newSize);
                buffer.order(ByteOrder.nativeOrder());
            }
            buffer.clear();
        }
        synchronized (mRefMap) {
            mRefMap.put(getKey(buffer), 1);
        }
        return buffer;
    }

    public void add(ByteBuffer buffer) {
        // use add to throw exception for invalid use
        mQueue.add(buffer);
        synchronized (mRefMap) {
            mRefMap.delete(getKey(buffer));
        }
    }

    public boolean offer(ByteBuffer buffer) {
        boolean ret = mQueue.offer(buffer);
        synchronized (mRefMap) {
            mRefMap.delete(getKey(buffer));
        }
        if (!ret) {
            Log.e(TAG, "offered extra invalid buffer!");
        }
        return ret;
    }

    private int getKey(ByteBuffer buffer) {
        return System.identityHashCode(buffer);
    }

    public boolean ref(ByteBuffer buffer) {
        synchronized (mRefMap) {
            int key = getKey(buffer);
            if (mRefMap.get(key, -1) == -1) {
                Log.e(TAG, "try to ref unknown ByteBuffer " + getKey(buffer));
                return false;
            }
            int ref = mRefMap.get(key);
            ref++;
            mRefMap.put(key, ref);
            return true;
        }
    }

    public boolean unref(ByteBuffer buffer) {
        synchronized (mRefMap) {
            int key = getKey(buffer);
            if (mRefMap.get(key, -1) == -1) {
                Log.e(TAG, "try to unref unknown ByteBuffer " + getKey(buffer));
                return false;
            }
            int ref = mRefMap.get(key);
            ref--;
            if (ref == 0) {
                offer(buffer);
            } else {
                mRefMap.put(key, ref);
            }
            return true;
        }
    }

    public void clear() {
        mQueue.clear();
        synchronized (mRefMap) {
            mRefMap.clear();
        }
    }
}
