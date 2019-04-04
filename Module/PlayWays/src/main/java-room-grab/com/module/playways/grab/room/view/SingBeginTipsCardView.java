package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

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
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Common.ESex;

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

    public void bindData(UserInfoModel info, SongModel songModel, SVGAListener listener) {
        if (info == null || songModel == null) {
            MyLog.e(TAG, "bindData" + " info=" + info + " songModel=" + songModel + " listener=" + listener);
            return;
        }
        this.mSVGAListener = listener;
        setVisibility(VISIBLE);
        SVGAParser parser = new SVGAParser(U.app());
        String assetsName = "grab_sing_chance.svga";
        mSingBeginSvga.setVisibility(VISIBLE);
        try {
            parser.parse(assetsName, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicBitmapItem(info, songModel));
                    mSingBeginSvga.setImageDrawable(drawable);
                    mSingBeginSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG,e);
        }

        mSingBeginSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mSingBeginSvga != null) {
                    mSingBeginSvga.setCallback(null);
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

    private SVGADynamicEntity requestDynamicBitmapItem(UserInfoModel userInfoModel, SongModel songModel) {
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

        String text1 = "";
        String text2 = "";
        if (userInfoModel.getUserId() != MyUserInfoManager.getInstance().getUid()) {
            text1 = userInfoModel.getNickname();
            text2 = "获得演唱机会!";
        } else {
            text1 = "轮到你唱";
            text2 = "《" + songModel.getItemName() + "》";
        }
        dynamicEntity.setDynamicText(text1, textPaint1, "text_48");
        dynamicEntity.setDynamicText(text2, textPaint2, "text_32");

        if (!TextUtils.isEmpty(userInfoModel.getAvatar())) {
            // 填入头像和背景框
            HttpImage image = ImageFactory.newHttpImage(userInfoModel.getAvatar())
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
                dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar_128");
            } else {
                dynamicEntity.setDynamicImage(image.getUrl(), "avatar_128");
            }

            Bitmap bitmap = Bitmap.createBitmap(U.getDisplayUtils().dip2px(70), U.getDisplayUtils().dip2px(70), Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(userInfoModel.getSex() == ESex.SX_MALE.getValue() ? U.getColor(R.color.color_man_stroke_color) : U.getColor(R.color.color_woman_stroke_color));
            dynamicEntity.setDynamicImage(bitmap, "border_140");
        }
        return dynamicEntity;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            this.mSVGAListener = null;
            if (mSingBeginSvga != null) {
                mSingBeginSvga.setCallback(null);
                mSingBeginSvga.stopAnimation(false);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mSVGAListener = null;
        if (mSingBeginSvga != null) {
            mSingBeginSvga.setCallback(null);
            mSingBeginSvga.stopAnimation(true);
        }
    }
}
