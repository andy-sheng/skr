package com.module.playways.grab.room.bottom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.common.log.MyLog;
import com.common.utils.U;

/**
 * 房主说话时底部 点点点 动画view
 */
public class SpeakingDotAnimationView extends View {
    public final static String TAG = "SpeakingDotAnimationView";

    int mDotNum = 5;
    Paint mPaint;
    int mR;
    int mIndex = Integer.MAX_VALUE;

    public SpeakingDotAnimationView(Context context) {
        super(context);
        init();
    }

    public SpeakingDotAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mR = U.getDisplayUtils().dip2px(3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int h = getHeight();
        int w = getWidth();
        for (int i = 0; i < mDotNum; i++) {
            int x = w * (i + 1) / (mDotNum + 1);
//            int alpha = 255 - 255 * ((mIndex + i) % mDotNum) / mDotNum;
            int alpha =  255 * ((mIndex + i) % mDotNum+1) / mDotNum;
            MyLog.d(TAG,"onDraw mIndex=" + mIndex+" i="+i+" alpha="+alpha);

            mPaint.setAlpha(alpha);
            canvas.drawCircle(x, h / 2, mR, mPaint);
        }
        mIndex--;
        postInvalidateDelayed(200);
    }
}
