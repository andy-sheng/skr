package com.component.mediaengine.util;

/**
 * Fps limiter.
 *
 * @hide
 */
public class FpsLimiter {
    private static final String TAG = "FpsLimiter";
    private static final int TOLERANCE = 15;

    private float mFps;
    private long mInitPts;
    private int mFrameNum;

    public void init(float fps, long initPts) {
        mFps = fps;
        mInitPts = initPts;
        mFrameNum = 0;
    }

    public boolean needDrop(long pts) {
        long limit = (long) (1000 * mFrameNum / mFps);
        if ((pts - mInitPts) > (limit + 1000 / mFps)) {
            mInitPts = pts;
            mFrameNum = 0;
            limit = 0;
        }
        pts -= mInitPts;
        limit -= TOLERANCE;
        if (pts < limit) {
            return true;
        } else {
            mFrameNum++;
            return false;
        }
    }
}
