package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.File;

/**
 * PK开始的板子
 */
public class PKSingBeginTipsCardView extends RelativeLayout {

    public final static String TAG = "PKSingBeginTipsCardView";

    SVGAImageView mPkSingBeginSvga;
    SVGAListener mSVGAListener;

    public PKSingBeginTipsCardView(Context context) {
        super(context);
        init();
    }

    public PKSingBeginTipsCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PKSingBeginTipsCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_pk_sing_begin_tips_card_layout, this);
        mPkSingBeginSvga = findViewById(R.id.pk_sing_begin_svga);
    }

    public void bindData(UserInfoModel left, UserInfoModel right, SVGAListener listener) {
        if (left == null || right == null) {
            MyLog.w(TAG, "bindData" + " left=" + left + " right=" + right + " listener=" + listener);
            return;
        }
        this.mSVGAListener = listener;
        setVisibility(VISIBLE);
        
        String assetsName = "grab_pk_sing_chance.svga";
        mPkSingBeginSvga.setVisibility(VISIBLE);
        try {
            SvgaParserAdapter.parse(assetsName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamic(left, right));
                    mPkSingBeginSvga.setImageDrawable(drawable);
                    mPkSingBeginSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }

        mPkSingBeginSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mPkSingBeginSvga != null) {
                    mPkSingBeginSvga.setCallback(null);
                    mPkSingBeginSvga.stopAnimation(true);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mPkSingBeginSvga != null && mPkSingBeginSvga.isAnimating()) {
                    mPkSingBeginSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    private SVGADynamicEntity requestDynamic(UserInfoModel left, UserInfoModel right) {
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
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
                dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar_1081");
            } else {
                dynamicEntity.setDynamicImage(image.getUrl(), "avatar_1081");
            }
        }

        if (!TextUtils.isEmpty(left.getNickname())) {
            TextPaint leftPaint = new TextPaint();
            leftPaint.setColor(U.getColor(R.color.black_trans_60));
            leftPaint.setAntiAlias(true);
            leftPaint.setTextSize(U.getDisplayUtils().dip2px(14));
            String text = left.getNickname();
            dynamicEntity.setDynamicText(text, leftPaint, "text_441");
        }

        if (!TextUtils.isEmpty(right.getAvatar())) {
            // 填入右边头像
            HttpImage image = ImageFactory.newPathImage(right.getAvatar())
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
                dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar_1082");
            } else {
                dynamicEntity.setDynamicImage(image.getUrl(), "avatar_1082");
            }
        }

        if (!TextUtils.isEmpty(right.getNickname())) {
            TextPaint rightPaint = new TextPaint();
            rightPaint.setColor(U.getColor(R.color.black_trans_60));
            rightPaint.setAntiAlias(true);
            rightPaint.setTextSize(U.getDisplayUtils().dip2px(14));
            String text = right.getNickname();
            dynamicEntity.setDynamicText(text, rightPaint, "text_442");
        }
        return dynamicEntity;
    }


    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            this.mSVGAListener = null;
            if (mPkSingBeginSvga != null) {
                mPkSingBeginSvga.setCallback(null);
                mPkSingBeginSvga.stopAnimation(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mSVGAListener = null;
        if (mPkSingBeginSvga != null) {
            mPkSingBeginSvga.setCallback(null);
            mPkSingBeginSvga.stopAnimation(true);
        }
    }

}
