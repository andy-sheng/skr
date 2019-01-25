package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 其他人主场景收音机
 */
public class OthersSingCardView extends RelativeLayout {

    SVGAImageView mOtherBgSvga;

    ImageView mIvHStub;
    ImageView mIvTStub;
    ImageView mIvOStub;
    ImageView mIvS;

    TranslateAnimation mEnterAnimation;   // 进场动画
    TranslateAnimation mLeaveAnimation;   // 出场动画

    HandlerTaskTimer mCountDownTask;

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


        SVGAImageView mOtherBgSvga;
        mIvHStub = (ImageView)findViewById(R.id.iv_h_stub);
        mIvTStub = (ImageView) findViewById(R.id.iv_t_stub);
        mIvOStub = (ImageView) findViewById(R.id.iv_o_stub);
        mIvS = (ImageView) findViewById(R.id.iv_s);
    }

    public void bindData(String avatar, SongModel songModel) {
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
            parser.parse("grab_other_sing_bg.svga", new SVGAParser.ParseCompletion() {
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
            MyLog.e(e);
        }

        countDonw(songModel);
    }

    private void countDonw(SongModel songModel) {
        if (songModel == null) {
            return;
        }

        cancelCountDownTask();

        mCountDownTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(((songModel.getStandLrcEndT() - songModel.getStandLrcBeginT()) / 1000) + 1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        setNum(((songModel.getStandLrcEndT() - songModel.getStandLrcBeginT()) / 1000) - integer + 1);
                    }
                });
    }

    private void setNum(int num) {
        mIvOStub.setImageDrawable(null);
        mIvTStub.setImageDrawable(null);
        mIvHStub.setImageDrawable(null);

        String s = String.valueOf(num);
        int[] index_num = new int[s.length()];

        for (int i = 0; i < s.length(); i++) {
            index_num[i] = Integer.parseInt(getNum(num, i + 1));

            if (i == 0) {
                mIvOStub.setImageDrawable(getNumDrawable(index_num[0]));
            } else if (i == 1) {
                mIvTStub.setImageDrawable(getNumDrawable(index_num[1]));
            } else if (i == 2) {
                mIvHStub.setImageDrawable(getNumDrawable(index_num[2]));
            }
        }

        mIvS.setImageDrawable(U.getDrawable(R.drawable.daojishizi_s));
    }

    private void cancelCountDownTask() {
        if (mCountDownTask != null) {
            mCountDownTask.dispose();
        }
    }

    private Drawable getNumDrawable(int num) {
        Drawable drawable = null;
        switch (num) {
            case 0:
                drawable = U.getDrawable(R.drawable.daojishizi_0);
                break;
            case 1:
                drawable = U.getDrawable(R.drawable.daojishizi_1);
                break;
            case 2:
                drawable = U.getDrawable(R.drawable.daojishizi_2);
                break;
            case 3:
                drawable = U.getDrawable(R.drawable.daojishizi_3);
                break;
            case 4:
                drawable = U.getDrawable(R.drawable.daojishizi_4);
                break;
            case 5:
                drawable = U.getDrawable(R.drawable.daojishizi_5);
                break;
            case 6:
                drawable = U.getDrawable(R.drawable.daojishizi_6);
                break;
            case 7:
                drawable = U.getDrawable(R.drawable.daojishizi_7);
                break;
            case 8:
                drawable = U.getDrawable(R.drawable.daojishizi_8);
                break;
            case 9:
                drawable = U.getDrawable(R.drawable.daojishizi_9);
                break;
        }

        return drawable;
    }

    public String getNum(long num, int index) {
        String s = String.valueOf(num);
        if(index > s.length() || index < 0){
            return "";
        }
        String result = String.valueOf(s.charAt(s.length() - index));
        return result;
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

        cancelCountDownTask();
    }

    private SVGADynamicEntity requestDynamicItem(String avatar) {
        if (TextUtils.isEmpty(avatar)) {
            return null;
        }

        HttpImage image = AvatarUtils.getAvatarUrl(AvatarUtils.newParamsBuilder(avatar)
                .setWidth(U.getDisplayUtils().dip2px(90))
                .setHeight(U.getDisplayUtils().dip2px(90))
                .build());
        File file = FrescoWorker.getCacheFileFromFrescoDiskCache(image.getUrl());
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (file != null && file.exists()) {
            dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar");
        } else {
            dynamicEntity.setDynamicImage(image.getUrl(), "avatar");
        }
        return dynamicEntity;
    }
}
