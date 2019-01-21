package com.module.playways.grab.room.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

/**
 * 其他人主场景收音机
 */
public class OthersSingCardView extends RelativeLayout {

    SVGAImageView mOtherBgSvga;

    public OthersSingCardView(Context context) {
        super(context);
        init();
    }

    public OthersSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OthersSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_others_sing_card_layout, this);
        mOtherBgSvga = (SVGAImageView) findViewById(R.id.other_bg_svga);
    }

    public void bindData(String avatar) {
        setVisibility(VISIBLE);
        // 平移动画
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, -U.getDisplayUtils().getScreenWidth(), 0);
        animator.setDuration(200);
        animator.start();

        mOtherBgSvga.setVisibility(VISIBLE);
        mOtherBgSvga.setLoops(0);
        SVGAParser parser = new SVGAParser(getContext());
        try {
            parser.parse("other_sing_bg.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicItem(avatar));
                    mOtherBgSvga.setImageDrawable(drawable);
                    mOtherBgSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }
    }

    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0, U.getDisplayUtils().getScreenWidth());
            animator.setDuration(200);
            animator.start();

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (mOtherBgSvga != null) {
                        mOtherBgSvga.stopAnimation(false);
                    }
                    setVisibility(GONE);
                    setTranslationX(0);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    onAnimationEnd(animator);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else {
            if (mOtherBgSvga != null) {
                mOtherBgSvga.stopAnimation(false);
            }
            setVisibility(GONE);
            setTranslationX(0);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mOtherBgSvga != null) {
            mOtherBgSvga.stopAnimation(true);
        }
    }

    private SVGADynamicEntity requestDynamicItem(String avatar) {
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (!TextUtils.isEmpty(avatar)) {
            dynamicEntity.setDynamicImage(avatar, "avatar");
        }
        return dynamicEntity;
    }
}
