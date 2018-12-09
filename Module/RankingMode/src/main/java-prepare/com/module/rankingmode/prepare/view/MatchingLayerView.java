package com.module.rankingmode.prepare.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.common.core.userinfo.UserInfo;
import com.common.utils.U;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.rankingmode.R;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class MatchingLayerView extends Layer {

    int midLineColor = Color.RED;

    int oneCircleTiem = 10000;

    boolean aroundOrientation = true;

    int rotationDegree = 0;

    int randomInterval = 0;

    float iconWidth = U.getDisplayUtils().dip2px(30);

    float midLineWidth = U.getDisplayUtils().dip2px(2);

    int iconCount = 4;

    int circleLineRadio;

    int layerViewWidth;

    ArrayList<MatchingUserIconView> matchingUserIconViewArrayList = new ArrayList<>();

    View circleView;

    int[] rotateLocationList;

    public MatchingLayerView(Context context) {
        this(context, null);
    }

    public MatchingLayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatchingLayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.MatchingLayerView);

        if(attributes.hasValue(R.styleable.MatchingLayerView_midLine_Color)){
            midLineColor = attributes.getColor(R.styleable.MatchingLayerView_midLine_Color, 0);
        }

        if(attributes.hasValue(R.styleable.MatchingLayerView_one_circle_time)){
            oneCircleTiem = attributes.getInteger(R.styleable.MatchingLayerView_one_circle_time, 10000);
        }

        if(attributes.hasValue(R.styleable.MatchingLayerView_around_orientation)){
            aroundOrientation = attributes.getBoolean(R.styleable.MatchingLayerView_around_orientation, true);
        }

        if(attributes.hasValue(R.styleable.MatchingLayerView_rotation_degree)){
            rotationDegree = attributes.getInteger(R.styleable.MatchingLayerView_rotation_degree, 0);
        }

        if(attributes.hasValue(R.styleable.MatchingLayerView_random_interval)){
            randomInterval = attributes.getInteger(R.styleable.MatchingLayerView_random_interval, 0);
        }

        if(attributes.hasValue(R.styleable.MatchingLayerView_midLine_width)){
            midLineWidth = attributes.getDimension(R.styleable.MatchingLayerView_midLine_width, 0);
        }

        if(attributes.hasValue(R.styleable.MatchingLayerView_user_icon_width)){
            iconWidth = attributes.getDimension(R.styleable.MatchingLayerView_user_icon_width, 0);
        }

        if(attributes.hasValue(R.styleable.MatchingLayerView_icon_count)){
            iconCount = attributes.getInteger(R.styleable.MatchingLayerView_icon_count, 4);
        }

        rotateLocationList = new int[iconCount];

        int eachRatation = 360 / iconCount;

        Observable.range(0, iconCount).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                rotateLocationList[integer] = integer * eachRatation;
            }
        });

        getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(this);
                        layerViewWidth = getWidth(); // 获取宽度
                        circleLineRadio = layerViewWidth / 2 - U.getDisplayUtils().dip2px(iconWidth) / 2;
                        for (int i = 0; i < iconCount; i++) {
                            addMatchingUserIcon(null, i);
                        }
                        startCircleAnimation();
                        return true;
                    }
                });
    }


    /**
     * 添加圆环
     */
    private void addCircleLine() {
        if (circleView != null && circleView.getParent() != null) {
            ((ViewGroup) circleView.getParent()).removeView(circleView);
        }

        circleView = new View(getContext());

        Drawable drawable = new DrawableCreator.Builder()
                .setCornersRadius(10000)
                .setStrokeColor(midLineColor)
                .setStrokeWidth(U.getDisplayUtils().dip2px(midLineWidth))
                .build();
        circleView.setBackground(drawable);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(circleLineRadio * 2, circleLineRadio * 2);
        layoutParams.addRule(CENTER_IN_PARENT);
        circleView.setLayoutParams(layoutParams);
        addView(circleView, 0);
    }

    public void startCircleAnimation() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "rotation", 0f, aroundOrientation ? 360f : -360f);

        // 动画的持续时间，执行多久？
        anim.setDuration(oneCircleTiem);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        anim.start();

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (MatchingUserIconView matchingUserIconView : matchingUserIconViewArrayList) {
                    matchingUserIconView.onChangeRotation((Float) animation.getAnimatedValue());
                }
            }
        });

        addCircleLine();
    }

    public void addMatchingUserIcon(UserInfo userInfo, int index) {

        int randomRotation = (int) ((Math.random()) * randomInterval);
        MatchingUserIconView matchingUserIconView = new MatchingUserIconView(getContext());
        matchingUserIconView.setUserInfo(userInfo);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(iconWidth), U.getDisplayUtils().dip2px(iconWidth));
        //这里传的角度是加上偏移量和一个随机的角度
        matchingUserIconView.setIconLocation(rotateLocationList[index] + randomRotation + rotationDegree, circleLineRadio);
        layoutParams.setMargins(matchingUserIconView.getIconLocation()[0] - U.getDisplayUtils().dip2px(iconWidth) /2
                , matchingUserIconView.getIconLocation()[1] - U.getDisplayUtils().dip2px(iconWidth)/2, 0, 0);
        matchingUserIconView.setLayoutParams(layoutParams);
        addSprite(matchingUserIconView);
        matchingUserIconViewArrayList.add(matchingUserIconView);
    }

    public void addMatchingUserIconList(ArrayList<UserInfo> iconUrlList) {
        if (iconUrlList == null) {
            return;
        }

        for (int i = 0; i < iconUrlList.size(); i++) {
            addMatchingUserIcon(iconUrlList.get(i), i);
        }
    }

    public void removeUserIcon(UserInfo userInfo) {
        if (getMatchingViewById(userInfo.getId()) != null) {
            removeSprite(getMatchingViewById(userInfo.getId()));
        }
    }

    private MatchingUserIconView getMatchingViewById(long userid) {
        for (MatchingUserIconView matchingUserIconView : matchingUserIconViewArrayList) {
            if (userid == matchingUserIconView.getUserInfo().getId()) {
                return matchingUserIconView;
            }
        }

        return null;
    }

    public void toScale(UserInfo userInfo) {
        if (getMatchingViewById(userInfo.getId()) != null) {
            getMatchingViewById(userInfo.getId()).toScaleAnimation(1.6f);
        }
    }

    /**
     * 万众归一
     */
    public void toCenter() {

    }
}
