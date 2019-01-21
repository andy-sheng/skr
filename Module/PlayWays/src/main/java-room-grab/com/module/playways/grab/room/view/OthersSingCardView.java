package com.module.playways.grab.room.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.utils.U;
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

    TranslateAnimation mEnterAnimation;   // 进场动画
    TranslateAnimation mLeaveAnimation;   // 出场动画

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
        if (mEnterAnimation == null) {
            mEnterAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0F, 0F, 0F);
            mEnterAnimation.setDuration(200);
        }
        this.startAnimation(mEnterAnimation);

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
            if (mLeaveAnimation == null) {
                mLeaveAnimation = new TranslateAnimation(0F, U.getDisplayUtils().getScreenWidth(), 0F, 0F);
                mLeaveAnimation.setDuration(200);
            }
            this.startAnimation(mLeaveAnimation);
            mLeaveAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mOtherBgSvga != null) {
                        mOtherBgSvga.stopAnimation(false);
                    }
                    setVisibility(GONE);
                    clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            if (mOtherBgSvga != null) {
                mOtherBgSvga.stopAnimation(false);
            }
            setVisibility(GONE);
            clearAnimation();
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
