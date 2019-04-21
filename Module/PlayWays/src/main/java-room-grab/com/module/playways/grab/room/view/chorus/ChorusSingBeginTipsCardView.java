package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.File;

/**
 * 合唱开始的板子
 */
public class ChorusSingBeginTipsCardView extends RelativeLayout {

    public final static String TAG = "ChorusSingBeginTipsCardView";

    SVGAImageView mChorusSingBeginSvga;
    SVGAListener mSVGAListener;

    public ChorusSingBeginTipsCardView(Context context) {
        super(context);
        init();
    }

    public ChorusSingBeginTipsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChorusSingBeginTipsCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_chorus_sing_begin_tips_card_layout, this);
        mChorusSingBeginSvga = findViewById(R.id.chorus_sing_begin_svga);
    }

    public void bindData(UserInfoModel left, UserInfoModel right, SVGAListener listener) {
        if (left == null || right == null) {
            MyLog.w(TAG, "bindData" + " left=" + left + " right=" + right + " listener=" + listener);
            return;
        }

        setVisibility(VISIBLE);
        // TODO: 2019/4/21 等新的svga再替换
        String assetsName = "grab_sing_chance.svga";
        mChorusSingBeginSvga.setVisibility(VISIBLE);
        try {
            SvgaParserAdapter.parse(assetsName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamic(left, right));
                    mChorusSingBeginSvga.setImageDrawable(drawable);
                    mChorusSingBeginSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }

        mChorusSingBeginSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mChorusSingBeginSvga != null) {
                    mChorusSingBeginSvga.setCallback(null);
                    mChorusSingBeginSvga.stopAnimation(true);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mChorusSingBeginSvga != null && mChorusSingBeginSvga.isAnimating()) {
                    mChorusSingBeginSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });

    }

    private SVGADynamicEntity requestDynamic(UserInfoModel left, UserInfoModel right) {
        // TODO: 2019/4/21 等动效替换
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        TextPaint textPaint1 = new TextPaint();
        textPaint1.setColor(Color.parseColor("#1A1B28"));
        textPaint1.setAntiAlias(true);
        textPaint1.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint1.setTextSize(U.getDisplayUtils().dip2px(20));

        TextPaint textPaint2 = new TextPaint();
        textPaint2.setColor(Color.parseColor("#2E3041"));
        textPaint2.setAntiAlias(true);
        textPaint2.setTextSize(U.getDisplayUtils().dip2px(14));

        String text1 = left.getNickname() + "左边的人合唱";
        String text2 = right.getNickname() + "右边的人合唱";
        dynamicEntity.setDynamicText(text1, textPaint1, "text_48");
        dynamicEntity.setDynamicText(text2, textPaint2, "text_32");

        if (!TextUtils.isEmpty(left.getAvatar())) {
            // 填入左边头像
            HttpImage image = ImageFactory.newPathImage(left.getAvatar())
                    .addOssProcessors(OssImgFactory.newResizeBuilder()
                                    .setW(ImageUtils.SIZE.SIZE_160.getW())
                                    .build()
                            , OssImgFactory.newCircleBuilder()
                                    .setR(500)
                                    .build()
                    )
                    .build();
            File file = FrescoWorker.getCacheFileFromFrescoDiskCache(image.getUrl());
            if (file != null) {
                dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar_104");
            } else {
                dynamicEntity.setDynamicImage(image.getUrl(), "avatar_104");
            }
        }
//        if (!TextUtils.isEmpty(right.getAvatar())) {
//            // 填入右边头像
//            HttpImage image = ImageFactory.newPathImage(right.getAvatar())
//                    .addOssProcessors(OssImgFactory.newResizeBuilder()
//                                    .setW(ImageUtils.SIZE.SIZE_160.getW())
//                                    .build()
//                            , OssImgFactory.newCircleBuilder()
//                                    .setR(500)
//                                    .build()
//                    )
//                    .build();
//            File file = FrescoWorker.getCacheFileFromFrescoDiskCache(image.getUrl());
//            if (file != null) {
//                dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar_104");
//            } else {
//                dynamicEntity.setDynamicImage(image.getUrl(), "avatar_104");
//            }
//        }
        return dynamicEntity;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            this.mSVGAListener = null;
            if (mChorusSingBeginSvga != null) {
                mChorusSingBeginSvga.setCallback(null);
                mChorusSingBeginSvga.stopAnimation(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mSVGAListener = null;
        if (mChorusSingBeginSvga != null) {
            mChorusSingBeginSvga.setCallback(null);
            mChorusSingBeginSvga.stopAnimation(true);
        }
    }
}
