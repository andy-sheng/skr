package com.wali.live.common.gift.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.live.module.common.R;
import com.wali.live.common.view.StrokeTextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 *
 * @Module 连送的数字区域动画
 * Created by chengsimin on 16/6/20.
 */
public class GiftNumberAnimationView extends RelativeLayout {
    public static String TAG = GiftNumberAnimationView.class.getSimpleName();

    public static final int SPEED_FAST = 2;
    public static final int SPEED_NORMAL = 1;
    private int mSpeedMode = 1;

    private ImageView mNumberBgIv;

    private StrokeTextView mNumberTv;

    private int[] mResourceId = {
            R.drawable.blue,
            R.drawable.green,
            R.drawable.yellow,
            R.drawable.orange,
            R.drawable.red,
            R.drawable.redrose,
            R.drawable.purple,
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

    private int[] mFlag = {
            19, 49, 98, 298, 519, 998, Integer.MAX_VALUE
//            9, 19, 29, 39, 49, 59, Integer.MAX_VALUE
    };

    private int mIndex = 0;// 当前索引级别

    boolean mIsMySendFastMode = false;//表示是否是自己快速连送模式

    public GiftNumberAnimationView(Context context) {
        super(context);
        init(context);
    }

    public GiftNumberAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftNumberAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.gift_number_animation_layout, this);
        bindView();
        mNumberBgIv.setImageResource(mResourceId[mIndex]);
        mNumberTv.setTextColor(GlobalData.app().getResources().getColorStateList(mNumberColors[0]));
        mNumberTv.setOutTextColor(R.color.color_white);
    }

    private void bindView() {
        mNumberBgIv = (ImageView) findViewById(R.id.number_bg_iv);
        mNumberTv = (StrokeTextView) findViewById(R.id.number_tv);
    }

    private boolean mIsSpecialNumFlag = false;
    private boolean mCurrentNumFlag = false;

    private boolean isSpecialNum(int num) {
        for (int i = 0; i < mFlag.length; i++) {
            if (num < mFlag[i] + 1) {
                return false;
            } else if (num == mFlag[i] + 1) {
                return true;
            }
        }

        return false;
    }

    public void setPlayNumber(int num) {
        if (num > 0) {
            mIsSpecialNumFlag = isSpecialNum(num);
            mNumberTv.setVisibility(INVISIBLE);
            mNumberTv.setText("x" + num);
            selectPic(num);
        }
    }

    public void setSpeedMode(int speedMode) {

        if (mSpeedMode != speedMode) {
            mSpeedMode = speedMode;
            mPlayTextScaleOnlyNumAnimSet = null;
            mPlayTextScaleWithBackGroupAnimSet = null;
        }
        mStartBackGroupAnimatorSet = null;
    }

    /**
     * 设置是否处于自己的快速连送模式
     *
     * @param isMySendFastMode
     */
    public void setIsMySendFastMode(boolean isMySendFastMode) {
        if(mIsMySendFastMode != isMySendFastMode) {
            mPlayTextScaleOnlyNumAnimSet = null;
            mPlayTextScaleWithBackGroupAnimSet = null;
            this.mIsMySendFastMode = isMySendFastMode;
        }
    }

    private int mGiftNumValue = 1;

    public void play(AnimatorListenerAdapter l, int giftNumValue, boolean onlyPlayNumFlay) {
        playTextScale(l, onlyPlayNumFlay);
        mGiftNumValue = giftNumValue;
    }

    private void selectPic(int number) {
        int index = 0;
        for (int i = 0; i < mFlag.length; i++) {
            if (number > mFlag[i]) {
                index++;
            } else {
                break;
            }
        }
        mNumberBgIv.setVisibility(VISIBLE);
        if (index >= mResourceId.length) {
            index = mResourceId.length - 1;
        }
        if (index != mIndex) {
            mIndex = index;
            mNumberBgIv.setImageResource(mResourceId[mIndex]);
            mNumberTv.setTextColor(GlobalData.app().getResources().getColorStateList(mNumberColors[mIndex]));
            //字体描边
//            mNumberTv.setOutTextColor(mNumberOutTextColors[mIndex]);
            mNumberTv.setOutTextColor(R.color.color_white);
        }
    }

    AnimatorSet mStartBackGroupAnimatorSet;

    private void startBackGroupAnimator() {
        if (mStartBackGroupAnimatorSet == null) {

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mNumberBgIv, "scaleX", 0f, 1.2f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mNumberBgIv, "scaleY", 0f, 1.2f);
            scaleX.setDuration(167);
            scaleY.setDuration(167);

            ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(mNumberBgIv, "scaleX", 1.2f, 1.5f);
            ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(mNumberBgIv, "scaleY", 1.2f, 1.5f);
            scaleX1.setDuration(708);
            scaleY1.setDuration(708);

            ObjectAnimator rotation = ObjectAnimator.ofFloat(mNumberBgIv, "rotation", 0f, 100f);
            rotation.setDuration(650);

            ObjectAnimator alpha = ObjectAnimator.ofFloat(mNumberBgIv, "alpha", 1f, 0f);
            alpha.setDuration(583);

            mStartBackGroupAnimatorSet = new AnimatorSet();
            mStartBackGroupAnimatorSet.play(scaleX).with(scaleY).with(rotation).with(alpha);
            mStartBackGroupAnimatorSet.play(scaleX1).with(scaleY1).after(scaleX);

            mStartBackGroupAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mNumberBgIv.setLayerType(View.LAYER_TYPE_NONE, null);
                    mNumberBgIv.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mNumberBgIv.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    mNumberBgIv.setVisibility(View.VISIBLE);
                    mNumberBgIv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            });
        }
        mStartBackGroupAnimatorSet.start();
    }

    private void stopBackGroupAnimator() {
        //onAnimationEnd未执行完不算结束
        if (mStartBackGroupAnimatorSet != null && mStartBackGroupAnimatorSet.isRunning()) {
            mStartBackGroupAnimatorSet.end();
        }
    }

    AnimatorSet mPlayTextScaleOnlyNumAnimSet;
    AnimatorSet mPlayTextScaleWithBackGroupAnimSet;
    private Interpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private Interpolator mDecelerateInterpolator = new DecelerateInterpolator();

    private void setAnimatorActions(long accelerateScaleAnimatorTime, long decelerateScaleAnimatorTime, long aphlaAnimatorTime, final AnimatorListenerAdapter l) {
        if (mCurrentNumFlag != mIsSpecialNumFlag) {
            mPlayTextScaleWithBackGroupAnimSet = null;
            mPlayTextScaleOnlyNumAnimSet = null;
            mCurrentNumFlag = mIsSpecialNumFlag;
        }
        if (mPlayTextScaleWithBackGroupAnimSet == null) {
            ObjectAnimator scaleX;
            ObjectAnimator scaleY;
            if (mCurrentNumFlag) {
                scaleX = ObjectAnimator.ofFloat(mNumberTv, "scaleX", 4.0f, .3f);
                scaleY = ObjectAnimator.ofFloat(mNumberTv, "scaleY", 4.0f, .3f);
            } else {
                scaleX = ObjectAnimator.ofFloat(mNumberTv, "scaleX", 2.0f, .3f);
                scaleY = ObjectAnimator.ofFloat(mNumberTv, "scaleY", 2.0f, .3f);
            }
            scaleX.setInterpolator(mAccelerateInterpolator);
            scaleY.setInterpolator(mAccelerateInterpolator);
            scaleX.setDuration(accelerateScaleAnimatorTime);
            scaleY.setDuration(accelerateScaleAnimatorTime);
            ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(mNumberTv, "scaleX", .3f, 1f);
            ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(mNumberTv, "scaleY", .3f, 1f);
            scaleX1.setInterpolator(mDecelerateInterpolator);
            scaleY1.setInterpolator(mDecelerateInterpolator);
            scaleX1.setDuration(decelerateScaleAnimatorTime);
            scaleY1.setDuration(decelerateScaleAnimatorTime);

            ObjectAnimator alpha = ObjectAnimator.ofFloat(mNumberTv, "alpha", .4f, 1.0f);
            alpha.setDuration(aphlaAnimatorTime);

            mPlayTextScaleWithBackGroupAnimSet = new AnimatorSet();
            mPlayTextScaleWithBackGroupAnimSet.play(scaleX).with(scaleY).with(alpha);
            mPlayTextScaleWithBackGroupAnimSet.play(scaleX1).with(scaleY1).after(scaleX);

            scaleX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    startBackGroupAnimator();
                }
            });

            mPlayTextScaleWithBackGroupAnimSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mNumberTv.setLayerType(View.LAYER_TYPE_NONE, null);
                    l.onAnimationEnd(animation);
                    stopBackGroupAnimator();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mNumberTv.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    l.onAnimationStart(animation);   // 改一下伴随开始和结束
                    mNumberTv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            });
        }

        if (!mPlayTextScaleWithBackGroupAnimSet.isRunning()) {
            mPlayTextScaleWithBackGroupAnimSet.start();
        }
    }

    private void setOnlyNumAnimatorActions(long accelerateScaleAnimatorTime, long decelerateScaleAnimatorTime, long aphleAnimatorTime, final AnimatorListenerAdapter l) {
        if (mCurrentNumFlag != mIsSpecialNumFlag) {
            mPlayTextScaleOnlyNumAnimSet = null;
            mPlayTextScaleWithBackGroupAnimSet = null;
            mCurrentNumFlag = mIsSpecialNumFlag;
        }
        if (mPlayTextScaleOnlyNumAnimSet == null) {
            ObjectAnimator scaleX;
            ObjectAnimator scaleY;
            if (mCurrentNumFlag) {
                scaleX = ObjectAnimator.ofFloat(mNumberTv, "scaleX", 4.0f, .3f);
                scaleY = ObjectAnimator.ofFloat(mNumberTv, "scaleY", 4.0f, .3f);
            } else {
                scaleX = ObjectAnimator.ofFloat(mNumberTv, "scaleX", 2.0f, .3f);
                scaleY = ObjectAnimator.ofFloat(mNumberTv, "scaleY", 2.0f, .3f);
            }

            scaleX.setInterpolator(mAccelerateInterpolator);
            scaleY.setInterpolator(mAccelerateInterpolator);
            scaleX.setDuration(accelerateScaleAnimatorTime);
            scaleY.setDuration(accelerateScaleAnimatorTime);
            ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(mNumberTv, "scaleX", 0f, 1f);
            ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(mNumberTv, "scaleY", 0f, 1f);
            scaleX1.setInterpolator(mDecelerateInterpolator);
            scaleY1.setInterpolator(mDecelerateInterpolator);
            scaleX1.setDuration(decelerateScaleAnimatorTime);
            scaleY1.setDuration(decelerateScaleAnimatorTime);

            ObjectAnimator alpha = ObjectAnimator.ofFloat(mNumberTv, "alpha", .4f, 1.0f);
            alpha.setDuration(aphleAnimatorTime);

            mPlayTextScaleOnlyNumAnimSet = new AnimatorSet();
            mPlayTextScaleOnlyNumAnimSet.play(scaleX).with(scaleY).with(alpha);
            mPlayTextScaleOnlyNumAnimSet.play(scaleX1).with(scaleY1).after(scaleX);
            mPlayTextScaleOnlyNumAnimSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mNumberTv.setLayerType(View.LAYER_TYPE_NONE, null);
                    l.onAnimationEnd(animation);
                    stopBackGroupAnimator();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mNumberTv.setLayerType(View.LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    l.onAnimationStart(animation);   // 改一下伴随开始和结束
                    mNumberTv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            });
        }

        if (!mPlayTextScaleOnlyNumAnimSet.isRunning()) {
            mPlayTextScaleOnlyNumAnimSet.start();
        }
    }

    /**
     * 字体放大
     */
    private void playTextScale(AnimatorListenerAdapter l, boolean onlyPlayNumFlag) {

        mNumberTv.setVisibility(VISIBLE);

        switch (mSpeedMode) {
            case SPEED_FAST: {
                mNumberBgIv.setVisibility(View.INVISIBLE);
                setOnlyNumAnimatorActions(80, 80, 150, l);
            }
            break;

            case SPEED_NORMAL: {
                if (mIsMySendFastMode) {
                    if (onlyPlayNumFlag && mGiftNumValue > 1) {
                        mGiftNumValue--;

                        setOnlyNumAnimatorActions(50, 50, 100, l);
                    } else {

                        setAnimatorActions(70, 70, 200, l);
                    }
                } else {
                    if (onlyPlayNumFlag && mGiftNumValue > 1) {
                        mGiftNumValue--;

                        setOnlyNumAnimatorActions(100, 100, 200, l);
                    } else {
                        setAnimatorActions(125, 125, 767, l);
                    }
                }
            }
            break;

            default: {
                if (onlyPlayNumFlag && mGiftNumValue > 1) {
                    mGiftNumValue--;
                    setOnlyNumAnimatorActions(100, 100, 200, l);
                } else {
                    setAnimatorActions(125, 125, 767, l);
                }
            }
            break;
        }
    }
}
