package com.module.playways.grab.room.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.log.MyLog;
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

    public final static String TAG = "SongInfoCardView";

    SVGAImageView mSongCover;
    RelativeLayout mSongInfoArea;

    ExTextView mSongNameTv;
    ExTextView mSongOwnerTv;
    ExImageView mBaibanIv;

    AnimatorSet mAnimatorSet;  // 入场动画
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
        mSongInfoArea = (RelativeLayout) findViewById(R.id.song_info_area);
        mSongNameTv = (ExTextView) findViewById(R.id.song_name_tv);
        mSongOwnerTv = (ExTextView) findViewById(R.id.song_owner_tv);
        mBaibanIv = (ExImageView) findViewById(R.id.baiban_iv);
    }

    // 该动画需要循环播放
    public void bindSongModel(SongModel songModel) {
        if (songModel == null || TextUtils.isEmpty(songModel.getCover())) {
            return;
        }

        // 入场动画
        animationGo();

        if (songModel.isIsblank()) {
            mSongNameTv.setVisibility(GONE);
            mSongOwnerTv.setVisibility(GONE);
            mBaibanIv.setVisibility(VISIBLE);
        } else {
            mSongNameTv.setVisibility(VISIBLE);
            mSongOwnerTv.setVisibility(VISIBLE);
            mBaibanIv.setVisibility(GONE);
            mSongNameTv.setText("《" + songModel.getItemName() + "》");
            mSongOwnerTv.setText(songModel.getOwner());
        }

        mSongCover.setVisibility(VISIBLE);
        mSongCover.setLoops(0);
        SVGAParser parser = new SVGAParser(getContext());
        try {
            parser.parse("grab_record_player.svga", new SVGAParser.ParseCompletion() {
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

    private void animationGo() {
        if (mAnimatorSet == null) {
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f);
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, View.SCALE_X, 0.8f, 1f);
            ObjectAnimator animator3 = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0.8f, 1f);
            ObjectAnimator animator4 = ObjectAnimator.ofFloat(mSongInfoArea, View.TRANSLATION_Y, -U.getDisplayUtils().dip2px(50), 0f);
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(animator1, animator2, animator3, animator4);
            mAnimatorSet.setDuration(200);
        }

        mAnimatorSet.start();
        mAnimatorSet.removeAllListeners();
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private SVGADynamicEntity requestDynamicBitmapItem(String cover) {
        if (TextUtils.isEmpty(cover)) {
            return null;
        }
//        cover = "http://song-static.inframe.mobi/cover/23bd61971ae39700b4a66a6b15bb3338.jpg";
        HttpImage httpImage = ImageFactory.newHttpImage(cover)
                .addOssProcessors(OssImgFactory.newResizeBuilder()
                                .setW(ImageUtils.SIZE.SIZE_160.getW())
                                .build()
                        , OssImgFactory.newCircleBuilder()
                                .setR(500)
                                .build()
                )
                .build();
        String url = httpImage.getUrl();
        MyLog.d(TAG, "requestDynamicBitmapItem" + " url=" + url);
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (!TextUtils.isEmpty(url)) {
            dynamicEntity.setDynamicImage(url, "cover");
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
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
    }
}

