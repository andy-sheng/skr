package com.wali.live.common.gift.utils;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;

/**
 * Created by chengsimin on 16/5/19.
 */
public class MyAnimationUtils {
    public static ObjectAnimator shake(View view, float shakeFactor) {

        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
                Keyframe.ofFloat(0f, 1f),
                Keyframe.ofFloat(.1f, .9f),
                Keyframe.ofFloat(.2f, .9f),
                Keyframe.ofFloat(.3f, 1.1f),
                Keyframe.ofFloat(.4f, 1.1f),
                Keyframe.ofFloat(.5f, 1.1f),
                Keyframe.ofFloat(.6f, 1.1f),
                Keyframe.ofFloat(.7f, 1.1f),
                Keyframe.ofFloat(.8f, 1.1f),
                Keyframe.ofFloat(.9f, 1.1f),
                Keyframe.ofFloat(1f, 1f)
        );

        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
                Keyframe.ofFloat(0f, 1f),
                Keyframe.ofFloat(.1f, .9f),
                Keyframe.ofFloat(.2f, .9f),
                Keyframe.ofFloat(.3f, 1.1f),
                Keyframe.ofFloat(.4f, 1.1f),
                Keyframe.ofFloat(.5f, 1.1f),
                Keyframe.ofFloat(.6f, 1.1f),
                Keyframe.ofFloat(.7f, 1.1f),
                Keyframe.ofFloat(.8f, 1.1f),
                Keyframe.ofFloat(.9f, 1.1f),
                Keyframe.ofFloat(1f, 1f)
        );

        PropertyValuesHolder pvhRotate = PropertyValuesHolder.ofKeyframe(View.ROTATION,
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(.1f, -3f * shakeFactor),
                Keyframe.ofFloat(.2f, -3f * shakeFactor),
                Keyframe.ofFloat(.3f, 3f * shakeFactor),
                Keyframe.ofFloat(.4f, -3f * shakeFactor),
                Keyframe.ofFloat(.5f, 3f * shakeFactor),
                Keyframe.ofFloat(.6f, -3f * shakeFactor),
                Keyframe.ofFloat(.7f, 3f * shakeFactor),
                Keyframe.ofFloat(.8f, -3f * shakeFactor),
                Keyframe.ofFloat(.9f, 3f * shakeFactor),
                Keyframe.ofFloat(1f, 0)
        );

        return ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY, pvhRotate).
                setDuration(1000);
    }


    public static ObjectAnimator textScaleNormal(View view) {

        //float[] scale = new float[]{3f, 2f, 1.5f, 1f, .8f, 1f};
        float[] scale = new float[]{2.2f, 1.8f, 1.6f, 1.2f, .8f, 1f};
        float[] alpha = new float[]{0f, .6f, .7f, .8f, 1f, 1f};

        long duration = 300;
        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
                Keyframe.ofFloat(0f, scale[0]),
                Keyframe.ofFloat(.2f, scale[1]),
                Keyframe.ofFloat(.4f, scale[2]),
                Keyframe.ofFloat(.6f, scale[3]),
                Keyframe.ofFloat(.8f, scale[4]),
                Keyframe.ofFloat(1f, scale[5])
        );

        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
                Keyframe.ofFloat(0f, scale[0]),
                Keyframe.ofFloat(.2f, scale[1]),
                Keyframe.ofFloat(.4f, scale[2]),
                Keyframe.ofFloat(.6f, scale[3]),
                Keyframe.ofFloat(.8f, scale[4]),
                Keyframe.ofFloat(1f, scale[5])
        );

        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofKeyframe(View.ALPHA,
                Keyframe.ofFloat(0f, alpha[0]),
                Keyframe.ofFloat(.2f, alpha[1]),
                Keyframe.ofFloat(.4f, alpha[2]),
                Keyframe.ofFloat(.6f, alpha[3]),
                Keyframe.ofFloat(.8f, alpha[4]),
                Keyframe.ofFloat(1f, alpha[5])
        );
        return ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY, pvhAlpha).
                setDuration(duration);
    }

    public static ObjectAnimator textScaleFast(View view) {

        //float[] scale = new float[]{3f, 2.6f, 2.2f, 1.8f, 1.4f, 1f};
        float[] scale = new float[]{2.5f, 2.2f, 1.8f, 1.6f, 1.4f, 1f};
        float[] alpha = new float[]{0f, .2f, .4f, .6f, .8f, 1f};
        long duration = 250;
        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
                Keyframe.ofFloat(0f, scale[0]),
                Keyframe.ofFloat(.2f, scale[1]),
                Keyframe.ofFloat(.4f, scale[2]),
                Keyframe.ofFloat(.6f, scale[3]),
                Keyframe.ofFloat(.8f, scale[4]),
                Keyframe.ofFloat(1f, scale[5])
        );

        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
                Keyframe.ofFloat(0f, scale[0]),
                Keyframe.ofFloat(.2f, scale[1]),
                Keyframe.ofFloat(.4f, scale[2]),
                Keyframe.ofFloat(.6f, scale[3]),
                Keyframe.ofFloat(.8f, scale[4]),
                Keyframe.ofFloat(1f, scale[5])
        );

        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofKeyframe(View.ALPHA,
                Keyframe.ofFloat(0f, alpha[0]),
                Keyframe.ofFloat(.2f, alpha[1]),
                Keyframe.ofFloat(.4f, alpha[2]),
                Keyframe.ofFloat(.6f, alpha[3]),
                Keyframe.ofFloat(.8f, alpha[4]),
                Keyframe.ofFloat(1f, alpha[5])
        );
        return ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY, pvhAlpha).
                setDuration(duration);
    }
}
