package com.module.playways.grab.room.view.normal;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.log.MyLog;
import com.common.utils.U;
import com.glidebitmappool.BitmapFactoryAdapter;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.room.song.model.SongModel;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.io.File;

/**
 * xxx获得演唱机会
 * 轮到你唱了
 * 演唱提示cardview
 */
public class NormalSingBeginTipsCardView {

    public final String TAG = "SingBeginTipsCardView";

    public void bindData(SVGAImageView svgaImageView, UserInfoModel info, SongModel songModel, SVGAListener listener, boolean isChallenge) {
        if (info == null || songModel == null) {
            MyLog.e(TAG, "bindData" + " info=" + info + " songModel=" + songModel + " listener=" + listener);
            return;
        }
        String assetsName = isChallenge ? "grab_challenge_sing_chance.svga" : "grab_sing_chance.svga";
        SvgaParserAdapter.parse(assetsName, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicBitmapItem(info, songModel));
                svgaImageView.setImageDrawable(drawable);
                svgaImageView.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        svgaImageView.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (svgaImageView != null) {
                    svgaImageView.setCallback(null);
                    svgaImageView.stopAnimation(true);
                }
                if (listener != null) {
                    listener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (svgaImageView != null && svgaImageView.isAnimating()) {
                    svgaImageView.stopAnimation(false);
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
            text1 = userInfoModel.getNicknameRemark();
            text2 = "获得演唱机会!";
        } else {
            text1 = "轮到你唱";
            text2 = "《" + songModel.getItemName() + "》";
        }
        dynamicEntity.setDynamicText(text1, textPaint1, "text_48");
        dynamicEntity.setDynamicText(text2, textPaint2, "text_32");

        if (!TextUtils.isEmpty(userInfoModel.getAvatar())) {
            // 填入头像
            HttpImage image = AvatarUtils.getAvatarUrl(AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                    .setCircle(true)
                    .build());
            File file = FrescoWorker.getCacheFileFromFrescoDiskCache(image.getUrl());
            if (file != null) {
                Bitmap bitmap = BitmapFactoryAdapter.decodeFile(file.getPath());
                //防止用户不给sd权限导致 bitmap为null
                if (bitmap != null) {
                    dynamicEntity.setDynamicImage(bitmap, "avatar_104");
                } else {
                    dynamicEntity.setDynamicImage(image.getUrl(), "avatar_104");
                }
            } else {
                dynamicEntity.setDynamicImage(image.getUrl(), "avatar_104");
            }

//            Bitmap bitmap = Bitmap.createBitmap(U.getDisplayUtils().dip2px(70), U.getDisplayUtils().dip2px(70), Bitmap.Config.ARGB_8888);
//            bitmap.eraseColor(userInfoModel.getSex() == ESex.SX_MALE.getValue() ? U.getColor(R.color.color_man_stroke_color) : U.getColor(R.color.color_woman_stroke_color));
//            dynamicEntity.setDynamicImage(bitmap, "border_140");
        }
        return dynamicEntity;
    }

}
