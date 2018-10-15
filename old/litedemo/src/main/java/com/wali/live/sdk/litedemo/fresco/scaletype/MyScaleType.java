package com.wali.live.sdk.litedemo.fresco.scaletype;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import com.facebook.drawee.drawable.ScalingUtils;

public class MyScaleType implements ScalingUtils.ScaleType {
    private static final String TAG = MyScaleType.class.getSimpleName();

    @Override
    public Matrix getTransform(Matrix outTransform, Rect parentRect, int childWidth, int childHeight, float focusX, float focusY) {
//      // 取宽度和高度需要缩放的倍数中最大的一个
        final float sX = (float) parentRect.width() / (float) childWidth;
        final float sY = (float) parentRect.height() / (float) childHeight;
        float scale = Math.max(1 / sX, 1 / sY);
//
//      // 计算为了均分空白区域，需要偏移的x、y方向的距离
//      float dx = parentRect.left + (parentRect.width() - childWidth * scale) * 0.5f;
//      float dy = parentRect.top + (parentRect.height() - childHeight * scale) * 0.5f;
//
//      // 最后我们应用它
        outTransform.setScale(scale, scale);
//      outTransform.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));

        Log.d(TAG, parentRect + ":" + childWidth + ":" + childHeight + ":" + focusX + ":" + focusY);
        Log.d(TAG, outTransform.toString());
        outTransform.postTranslate(0, 50);
        return outTransform;
    }
}