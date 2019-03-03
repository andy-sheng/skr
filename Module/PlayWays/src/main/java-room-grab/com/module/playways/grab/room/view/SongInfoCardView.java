package com.module.playways.grab.room.view;


import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;


/**
 * 转场时的歌曲信息页
 */
public class SongInfoCardView extends RelativeLayout {

    public final static String TAG = "SongInfoCardView";

    SimpleDraweeView mSongCoverIv;
    ExTextView mSongNameTv;
    ExTextView mSongSingerTv;
    ExTextView mSongSeqTv;
    ExTextView mSongLyrics;
    ImageView mGrabCd;

    RotateAnimation mRotateAnimation;    // cd的旋转
    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

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
        mSongCoverIv = (SimpleDraweeView) findViewById(R.id.song_cover_iv);
        mSongNameTv = (ExTextView) findViewById(R.id.song_name_tv);
        mSongSingerTv = (ExTextView) findViewById(R.id.song_singer_tv);
        mSongSeqTv = (ExTextView) findViewById(R.id.song_seq_tv);
        mSongLyrics = (ExTextView) findViewById(R.id.song_lyrics);
        mGrabCd = (ImageView) findViewById(R.id.grab_cd);
    }

    // 该动画需要循环播放
    public void bindSongModel(SongModel songModel) {
        MyLog.d(TAG,"bindSongModel" + " songModel=" + songModel);
        if (songModel == null || TextUtils.isEmpty(songModel.getCover())) {
            return;
        }

        setVisibility(VISIBLE);
        if (!TextUtils.isEmpty(songModel.getCover())) {
            FrescoWorker.loadImage(mSongCoverIv,
                    ImageFactory.newHttpImage(songModel.getCover())
                            .setCornerRadius(U.getDisplayUtils().dip2px(6))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(Color.parseColor("#202239")).build());
        } else {
            FrescoWorker.loadImage(mSongCoverIv,
                    ImageFactory.newResImage(R.drawable.xuanzegequ_wufengmian)
                            .setCornerRadius(U.getDisplayUtils().dip2px(6))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(Color.parseColor("#202239")).build());
        }
        mSongNameTv.setText("《" + songModel.getItemName() + "》");
        mSongSingerTv.setText(songModel.getOwner());
        // 入场动画
        animationGo();
    }

    private void animationGo() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F, 0.0F);
            mEnterTranslateAnimation.setDuration(200);
        }
        this.startAnimation(mEnterTranslateAnimation);

        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateAnimation.setDuration(3000);
            mRotateAnimation.setRepeatCount(Animation.INFINITE);
            mRotateAnimation.setFillAfter(true);
            mRotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        mGrabCd.startAnimation(mRotateAnimation);
    }

    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mLeaveTranslateAnimation.setDuration(200);
            }

            this.startAnimation(mLeaveTranslateAnimation);
            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearAnimation();
                    if (mRotateAnimation != null) {
                        mRotateAnimation.cancel();
                    }
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            if (mRotateAnimation != null) {
                mRotateAnimation.cancel();
            }
            clearAnimation();
            setVisibility(GONE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }
        if (mRotateAnimation != null) {
            mRotateAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
    }
}

