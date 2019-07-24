package com.zq.mediaengine.util;

import android.util.Log;

/**
 * System Time Clock manager, time in milliseconds.
 */

public class StcMgt {
    private static final String TAG = "StcMgt";
    private static final boolean VERBOSE = true;

    private long mSystemTimeBase;
    private long mPtsBase;

    private boolean mPaused;
    private long mPausedSystemTime;

    public StcMgt() {
        reset();
    }

    /**
     * Reset stc to invalid state.
     */
    public void reset() {
        mSystemTimeBase = Long.MIN_VALUE;
        mPtsBase = Long.MIN_VALUE;
        mPaused = false;
        mPausedSystemTime = Long.MIN_VALUE;
    }

    /**
     * Start stc.
     */
    public void start() {
        if (!isValid() || !mPaused) {
            return;
        }
        mPaused = false;
        mSystemTimeBase += System.currentTimeMillis() - mPausedSystemTime;
    }

    /**
     * Pause stc.
     */
    public void pause() {
        if (!isValid() || mPaused) {
            return;
        }
        mPaused = true;
        mPausedSystemTime = System.currentTimeMillis();
    }

    /**
     * Update stc with current system time and current pts.
     *
     * @param pts stream pts(in ms) should be rendered currently
     */
    public void updateStc(long pts) {
        updateStc(System.currentTimeMillis(), pts, false);
    }

    /**
     * Update stc with current system time and current pts.
     *
     * @param pts   stream pts(in ms) should be rendered currently
     * @param start whether to start stc after updated if current in paused state
     */
    public void updateStc(long pts, boolean start) {
        updateStc(System.currentTimeMillis(), pts, start);
    }

    /**
     * Update stc with given system time and current pts.
     *
     * @param systemTime system time(in ms) corresponding to the pts
     * @param pts stream pts(in ms) corresponding to the system time
     */
    public void updateStc(long systemTime, long pts) {
        updateStc(systemTime, pts, false);
    }

    /**
     * Update stc with given system time and current pts.
     *
     * @param systemTime system time(in ms) corresponding to the pts
     * @param pts stream pts(in ms) corresponding to the system time
     * @param start whether to start stc after updated if current in paused state
     */
    public void updateStc(long systemTime, long pts, boolean start) {
        if (!isValid()) {
            long systemTimeBaseDiff = systemTime - mSystemTimeBase;
            long ptsBaseDiff = pts - mPtsBase;
            long stcOff = ptsBaseDiff - systemTimeBaseDiff;
            if (Math.abs(stcOff) > 5) {
                Log.d(TAG, "stc update with offset " + stcOff);
            } else if (VERBOSE) {
                Log.d(TAG, "stc update with offset " + stcOff);
            }
        }
        mSystemTimeBase = systemTime;
        mPtsBase = pts;
        mPausedSystemTime = systemTime;
        if (start) {
            mPaused = false;
        }
    }

    /**
     * Get current stc.
     *
     * @return stc in ms.
     */
    public long getCurrentStc() {
        if (!isValid()) {
            return 0;
        } else if (mPaused) {
            return mPausedSystemTime - mSystemTimeBase + mPtsBase;
        } else {
            return (System.currentTimeMillis() - mSystemTimeBase) + mPtsBase;
        }
    }

    /**
     * Is current StcMgt with valid data.
     *
     * @return true if valid, false if invalid.
     */
    public boolean isValid() {
        return mSystemTimeBase != Long.MIN_VALUE && mPtsBase != Long.MIN_VALUE;
    }
}
