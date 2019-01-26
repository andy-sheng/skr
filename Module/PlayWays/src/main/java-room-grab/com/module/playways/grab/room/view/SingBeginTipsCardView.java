package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.utils.U;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * xxx获得演唱机会
 * 轮到你唱了
 * 演唱提示cardview
 */
public class SingBeginTipsCardView extends RelativeLayout {

    public final static String TAG = "SingBeginTipsCardView";

    SVGAImageView mSingBeginSvga;
    SVGAListener mSVGAListener;

    public SingBeginTipsCardView(Context context) {
        super(context);
        init();
    }

    public SingBeginTipsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SingBeginTipsCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_sing_begin_tips_card_layout, this);
        mSingBeginSvga = (SVGAImageView) findViewById(R.id.sing_begin_svga);
    }

    public void bindData(UserInfoModel info, SVGAListener listener) {
        this.mSVGAListener = listener;
        setVisibility(VISIBLE);
        SVGAParser parser = new SVGAParser(getContext());
        String assetsName = "grab_sing_self_chance.svga";
        if (info.getUserId() != MyUserInfoManager.getInstance().getUid()) {
            assetsName = "grab_sing_other_chance.svga";
        }
        mSingBeginSvga.setVisibility(VISIBLE);
        try {
            parser.parse(assetsName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicBitmapItem(info));
                    mSingBeginSvga.setImageDrawable(drawable);
                    mSingBeginSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }

        mSingBeginSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mSingBeginSvga != null) {
                    mSingBeginSvga.stopAnimation(true);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mSingBeginSvga != null && mSingBeginSvga.isAnimating()) {
                    mSingBeginSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    private SVGADynamicEntity requestDynamicBitmapItem(UserInfoModel userInfoModel) {
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (userInfoModel.getUserId() != MyUserInfoManager.getInstance().getUid()) {
            // 填入名字和头像
            TextPaint textPaint = new TextPaint();
            textPaint.setColor(Color.parseColor("#0C2275"));
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(U.getDisplayUtils().dip2px(13));
            dynamicEntity.setDynamicImage(userInfoModel.getAvatar(), "avatar");
            dynamicEntity.setDynamicText(userInfoModel.getNickname(), textPaint, "name");
        }

        if (!TextUtils.isEmpty(userInfoModel.getAvatar())) {
            HttpImage image = AvatarUtils.getAvatarUrl(AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                    .setWidth(U.getDisplayUtils().dip2px(90))
                    .setHeight(U.getDisplayUtils().dip2px(90))
                    .build());
            File file = FrescoWorker.getCacheFileFromFrescoDiskCache(image.getUrl());
            if (file != null) {
                dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar");
            } else {
                dynamicEntity.setDynamicImage(image.getUrl(), "avatar");
            }
        }
        return dynamicEntity;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            if (mSingBeginSvga != null) {
                mSingBeginSvga.stopAnimation(false);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSingBeginSvga != null) {
            mSingBeginSvga.stopAnimation(true);
        }
        this.mSVGAListener = null;
    }
}
