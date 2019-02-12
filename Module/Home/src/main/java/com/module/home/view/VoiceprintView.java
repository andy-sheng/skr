package com.module.home.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.DisplayUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;

public class VoiceprintView extends View {
    //柱子最大高度
    int mPillarMaxHeight = U.getDisplayUtils().dip2px(10);
    //柱子最小
    int mPillarMinHeight = U.getDisplayUtils().dip2px(5);

    //柱子宽度
    int mPillarWidth = U.getDisplayUtils().dip2px(10);

    //两个柱子间距
    int mPillarSplit = U.getDisplayUtils().dip2px(20);

    int mOffsetX = 0;

    int mRadus = mPillarWidth / 2;

    //一秒钟走多少
    int mSpeed = U.getDisplayUtils().dip2px(36);

    //毫秒为单位
    int mIntervalTime = 10;

    //毫秒为单位
    int mIntervalSpeed = mSpeed * mIntervalTime / 1000;

    int mTotolWidth = 0;

    public VoiceprintView(Context context) {
        this(context, null);
    }

    public VoiceprintView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceprintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();

    }

    private void init() {
        HandlerTaskTimer.newBuilder()
                .interval(mIntervalTime)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        scrollTo(mIntervalSpeed * integer, 0);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint _paint = new Paint();
        _paint.setColor(Color.GREEN);

        mOffsetX = 100 + mPillarSplit;

        for (int i = 0; i < 1000; i++){
            RectF rectF = new RectF();
            rectF.left = (mPillarSplit + mPillarWidth) * i - mPillarWidth;
            rectF.right = (mPillarSplit + mPillarWidth) * i;
            rectF.top = 100;
            rectF.bottom = 150;
            canvas.drawRoundRect(rectF, mRadus, mRadus, _paint);
        }

    }
}
