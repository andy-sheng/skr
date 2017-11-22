package com.wali.live.watchsdk.fans.pay.interpolator;

import android.view.animation.BounceInterpolator;

/**
 * Created by lan on 2017/11/22.
 */
public class YearPayInterpolator extends BounceInterpolator {
    private float mMaxHigh = 1.049f;
    private float mMaxTime = 0.33f;
    private float mTime = 0.66f;

    @Override
    public float getInterpolation(float t) {
        t *= mTime;
        if (t < mMaxTime) {
            return (float) (Math.sin(t * (3.14 / 2) / mMaxTime) * mMaxHigh);
        } else {
            return (float) (2.0309f * Math.pow(t, 2) - 2.15914f * t + 1.5403455f);
        }
    }
}
