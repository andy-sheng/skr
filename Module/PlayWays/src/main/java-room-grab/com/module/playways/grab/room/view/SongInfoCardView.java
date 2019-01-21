package com.module.playways.grab.room.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;

import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;


/**
 * 转场时的歌曲信息页
 */
public class SongInfoCardView extends RelativeLayout {


    SVGAImageView mSongCover;
    ExTextView mSongNameTv;
    ExTextView mSongOwnerTv;
    ExImageView mBaibanIv;

    AlphaAnimation mAlphaAnimation; // 渐入的入场动画
    TranslateAnimation mTranslateAnimation; // 飞出的离场动画

    public SongInfoCardView(Context context) {
        super(context);
        init();
    }

    public SongInfoCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SongInfoCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_song_info_card_layout, this);
        mSongCover = (SVGAImageView) findViewById(R.id.song_cover);
        mSongNameTv = (ExTextView) findViewById(R.id.song_name_tv);
        mSongOwnerTv = (ExTextView) findViewById(R.id.song_owner_tv);
        mBaibanIv = (ExImageView) findViewById(R.id.baiban_iv);

    }

    // 该动画需要循环播放
    public void bindSongModel(SongModel songModel) {
        if (songModel == null || TextUtils.isEmpty(songModel.getCover())) {
            return;
        }

        setVisibility(VISIBLE);
        // 淡入效果
        if (mAlphaAnimation == null) {
            mAlphaAnimation = new AlphaAnimation(0f, 1f);
            mAlphaAnimation.setDuration(200);
        }
        this.startAnimation(mAlphaAnimation);

        if (songModel.isIsblank()) {
            mSongNameTv.setVisibility(GONE);
            mSongOwnerTv.setVisibility(GONE);
            mBaibanIv.setVisibility(VISIBLE);
        } else {
            mSongNameTv.setVisibility(VISIBLE);
            mSongOwnerTv.setVisibility(VISIBLE);
            mBaibanIv.setVisibility(GONE);
            mSongNameTv.setText(songModel.getItemName());
            mSongOwnerTv.setText(songModel.getOwner());
        }

        mSongCover.setVisibility(VISIBLE);
        mSongCover.setLoops(0);
        SVGAParser parser = new SVGAParser(getContext());
        try {
            parser.parse("record_player.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity, requestDynamicBitmapItem(songModel.getCover()));
                    mSongCover.setImageDrawable(drawable);
                    mSongCover.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }
    }

    private SVGADynamicEntity requestDynamicBitmapItem(String cover) {
        if (TextUtils.isEmpty(cover)) {
            return null;
        }
        HttpImage httpImage = ImageFactory.newHttpImage(cover)
                .addOssProcessors(OssImgFactory.newResizeBuilder()
                        .setW(ImageUtils.SIZE.SIZE_160.getW())
                        .build())
                .build();
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (!TextUtils.isEmpty(httpImage.getUrl())) {
            dynamicEntity.setDynamicImage(httpImage.getUrl(), "cover");
        }
        return dynamicEntity;
    }

    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mTranslateAnimation == null) {
                mTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mTranslateAnimation.setDuration(200);
            }

            this.startAnimation(mTranslateAnimation);
            mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mSongCover != null) {
                        mSongCover.stopAnimation(false);
                    }
                    clearAnimation();
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            if (mSongCover != null) {
                mSongCover.stopAnimation(false);
            }
            setVisibility(GONE);
            clearAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSongCover != null) {
            mSongCover.stopAnimation(true);
        }
    }
}

