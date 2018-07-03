package com.wali.live.watchsdk.fastsend.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.watchsdk.R;

/**
 * Created by zhujianning on 18-7-3.
 * 快捷送礼物按钮
 */

public class GiftFastSendView extends FrameLayout {
    private static final String TAG = "GiftFastSendView";
    private static final int TIME = 3;

    private Context mContext;
    private SimpleDraweeView mCoverIv;
    private CircleProgressBar mProgressBar;
    private ValueAnimator mAnimator;

    public GiftFastSendView(@NonNull Context context) {
        this(context, null);
    }

    public GiftFastSendView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GiftFastSendView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        inflate(context, R.layout.live_icon_fast_send_view, this);

        mCoverIv = (SimpleDraweeView) findViewById(R.id.live_icon_fast_send_img);
        mProgressBar = (CircleProgressBar) findViewById(R.id.live_icon_fast_send_circle);

        mProgressBar.setMax(360);
        mProgressBar.setProgress(360);
    }

    public void setImgPic(String url, boolean isSHow) {
        if (!TextUtils.isEmpty(url)) {
            FrescoWorker.loadImage(mCoverIv, ImageFactory.newHttpImage(url).build());
            if (isSHow) {
                setVisibility(VISIBLE);
            }
        } else {
            setVisibility(GONE);
        }
    }

    public void start() {
        if (mAnimator != null) {
            stop();
        }

        //这里用的是nineold的属性动画向下兼容包
        mAnimator = ValueAnimator.ofInt(0, 360);
        mAnimator.setDuration(TIME * 1000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (Integer) animation.getAnimatedValue();
                mProgressBar.setProgress(animatedValue);
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBar.setProgress(360);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mProgressBar.setProgress(360);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }


    public void stop() {
        if (mProgressBar != null) {
            mProgressBar.setProgress(0);
        }
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator.removeAllUpdateListeners();
            mAnimator = null;
        }
    }
}
