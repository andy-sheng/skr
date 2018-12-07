package com.module.rankingmode.prepare.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rankingmode.R;

import java.util.ArrayList;
import java.util.List;

public class RingCircleView extends RelativeLayout {

    List<View> cildViews = new ArrayList<>();

    private int middleLineColor = Color.GRAY; //环中心线颜色
    private float middleLineWidth = -1; //环中心线的宽度

    private float ringRadius; //环的半径

    public RingCircleView(Context context) {
        super(context);
    }

    public RingCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAtts(context, attrs);
    }

    public RingCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAtts(context, attrs);
    }

    private void initAtts(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingCircleView);

        middleLineColor = typedArray.getColor(R.styleable.RingCircleView_midLine_Color, Color.GRAY);
//        middleLineWidth = typedArray.getFloat(R.styleable.ringView_midLine_Width, -1);

        ringRadius = 100;

        typedArray.recycle();
    }

    public void addIconView(SimpleDraweeView view1) {
        cildViews.add(view1);

        view1.setBackgroundColor(getResources().getColor(R.color.blue));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(100,100);
        layoutParams.setMargins(0,0,0,0);
        view1.setLayoutParams(layoutParams);
        addView(view1);
    }


    public void startCircleAnimator() {
        // todo 开始旋转
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f);

        // 动画的持续时间，执行多久？
        anim.setDuration(5000);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                for (View view : cildViews) {
                    view.setRotation(-value);
                }
            }
        });

    }
}
