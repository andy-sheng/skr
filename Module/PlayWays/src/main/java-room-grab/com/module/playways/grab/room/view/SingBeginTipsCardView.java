package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.PlayerInfo;

import org.jetbrains.annotations.NotNull;

/**
 * xxx获得演唱机会
 * 轮到你唱了
 * 演唱提示cardview
 */
public class SingBeginTipsCardView extends RelativeLayout {

    SVGAImageView mSingBeginSvga;

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
        SVGAParser parser = new SVGAParser(getContext());
        String assetsName = "sing_yourself.svga";
        if (info.getUserId() != MyUserInfoManager.getInstance().getUid()) {
            assetsName = "sing_yourself.svga";
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
                if (listener != null) {
                    listener.onFinished();
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
        if (userInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
            dynamicEntity.setDynamicImage(userInfoModel.getAvatar(), "avatar");
        } else {
            // 填入名字和头像
            dynamicEntity.setDynamicImage(userInfoModel.getAvatar(), "avatar");
        }
        return dynamicEntity;
    }
}
