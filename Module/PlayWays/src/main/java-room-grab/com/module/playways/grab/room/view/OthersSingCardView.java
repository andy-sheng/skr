package com.module.playways.grab.room.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

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

    private SVGADynamicEntity requestDynamicItem(String avatar) {
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (!TextUtils.isEmpty(avatar)) {
            dynamicEntity.setDynamicImage(avatar, "avatar");
        }
        return dynamicEntity;
    }
}
