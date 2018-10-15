package com.wali.live.common.gift.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.wali.live.common.view.StrokeTextView;

import java.util.ArrayList;

/**
 * Created by chengsimin on 16/2/28.
 */
public class ContinueSendBtn extends RelativeLayout {

    private TextView mCountDownTv;
    private ViewGroup mContinueSendVp;

    private int[] mFlag = {
            19, 49, 98, 298, 519, 998, Integer.MAX_VALUE
    };

    private int[] mNumberColors = {
            R.color.gift_number_first,
            R.color.gift_number_second,
            R.color.gift_number_third,
            R.color.gift_number_forth,
            R.color.gift_number_fifth,
            R.color.gift_number_sixth,
            R.color.gift_number_seventh
    };

    private int[] mNumberOutTextColors = {
            R.color.gift_number_stroke_blue,
            R.color.gift_number_stroke_green,
            R.color.gift_number_stroke_orange,
            R.color.gift_number_stroke_red,
            R.color.gift_number_stroke_rosered,
            R.color.gift_number_stroke_purple,
            R.color.gift_number_stroke_darkblue,
    };

    private int mIndex = 0;// 当前索引级别

    public ContinueSendBtn(Context context) {
        super(context);
        init(context);
    }

    public ContinueSendBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContinueSendBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.gift_continue_send_btn_view, this);
        mCountDownTv = (TextView) findViewById(R.id.count_down_tv);
        mContinueSendVp = (ViewGroup) findViewById(R.id.continue_send_vp);
    }

    private int mNumber;

    @Override
    public void setOnClickListener(OnClickListener l) {
        mContinueSendVp.setOnClickListener(l);
    }

    public void setNumber(int number) {
        if (number > mNumber) {
//            mNumber++;
            mNumber = number;
        } else {
            mNumber = 1;
        }
        NumberViewWithExtraInfo viewWithExtraInfo = getFlyBarrageView();
        LayoutParams lp = (LayoutParams) viewWithExtraInfo.view.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        lp.addRule(RelativeLayout.ALIGN_RIGHT, R.id.continue_send_vp);
        lp.addRule(RelativeLayout.ALIGN_TOP, R.id.continue_send_vp);
        lp.leftMargin = DisplayUtils.dip2px(25);
        viewWithExtraInfo.view.setGravity(Gravity.CENTER);
        viewWithExtraInfo.view.setLayoutParams(lp);
        viewWithExtraInfo.view.setMinWidth(DisplayUtils.dip2px(75.0f));
        viewWithExtraInfo.view.setTextSize(getResources().getDimension(R.dimen.text_size_18));
        viewWithExtraInfo.view.setText("X" + mNumber);
        //viewWithExtraInfo.view.setTextColor(0xff7f62ff);
        setNumberColor(mNumber, viewWithExtraInfo.view);
        this.addView(viewWithExtraInfo.view);
        play(viewWithExtraInfo);
    }

    private void setNumberColor(int number, StrokeTextView view) {
        int index = 0;
        for (int i = 0; i < mFlag.length; i++) {
            if (number > mFlag[i]) {
                index++;
            } else {
                break;
            }
        }
        if (index >= mNumberColors.length) {
            index = mNumberColors.length - 1;
        } else if (index < 0) {
            index = 0;
        }
        view.setTextColor(getContext().getResources().getColorStateList(mNumberColors[index]));
        //字体描边
//        view.setOutTextColor(mNumberOutTextColors[index]);
        view.setOutTextColor(R.color.color_white);
    }

    private void play(NumberViewWithExtraInfo viewWithExtraInfo) {
        viewWithExtraInfo.animatorSet.start();
    }

    private static final int CACHE_NUMBER = 2; // 缓存数量

    private ArrayList<NumberViewWithExtraInfo> mNumberViewCache = new ArrayList(CACHE_NUMBER);

    private NumberViewWithExtraInfo getFlyBarrageView() {
        for (int i = 0; i < mNumberViewCache.size(); i++) {
            NumberViewWithExtraInfo info = mNumberViewCache.get(i);
            //缓存中有空闲的，返回缓存
            if (!info.isWorking) {
                return info;
            }
        }
        final NumberViewWithExtraInfo info = new NumberViewWithExtraInfo();
        info.view = new StrokeTextView(getContext());
        {
            float curTranslationY = info.view.getTranslationY();
            ObjectAnimator moveY = ObjectAnimator.ofFloat(info.view, "translationY", curTranslationY, curTranslationY - 150);
            ObjectAnimator fadeInOut = ObjectAnimator.ofFloat(info.view, "alpha", 1f, 0f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.play(moveY).with(fadeInOut);
            animSet.setInterpolator(new AccelerateInterpolator());
            animSet.setDuration(500);
            animSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    info.view.setLayerType(View.LAYER_TYPE_NONE, null);
                    info.isWorking = false;
                    removeView(info.view);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    info.view.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    info.isWorking = true;
                    info.view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            });
            info.animatorSet = animSet;
        }
        if (mNumberViewCache.size() < CACHE_NUMBER) {
            // 缓存未满，加入缓存
            mNumberViewCache.add(info);
        }
        return info;
    }

    public void setCountDown(int time) {
        mCountDownTv.setText(String.valueOf(time));
    }

    static class NumberViewWithExtraInfo {
        public StrokeTextView view;// view实体
        public AnimatorSet animatorSet;
        public boolean isWorking = false;// 是否正在被使用
    }
}
