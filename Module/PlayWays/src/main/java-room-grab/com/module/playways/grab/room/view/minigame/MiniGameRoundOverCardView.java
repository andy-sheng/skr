package com.module.playways.grab.room.view.minigame;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.zq.live.proto.Room.EQRoundOverReason;

public class MiniGameRoundOverCardView extends RelativeLayout {

    ImageView mMiniGameBgIv;
    SVGAListener mSVGAListener;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    Handler mUiHandler = new Handler();

    public MiniGameRoundOverCardView(Context context) {
        super(context);
        init();
    }

    public MiniGameRoundOverCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiniGameRoundOverCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_mini_game_over_card_layout, this);
        mMiniGameBgIv = (ImageView) findViewById(R.id.mini_game_bg_iv);
    }

    public void bindData(GrabRoundInfoModel lastRoundInfo, SVGAListener listener) {
        if (lastRoundInfo == null) {
            return;
        }
        int songId = 0;
        if (lastRoundInfo.getMusic() != null) {
            songId = lastRoundInfo.getMusic().getItemID();
        }
        int reason = lastRoundInfo.getOverReason();
        int resultType = lastRoundInfo.getResultType();
        this.mSVGAListener = listener;
        setVisibility(VISIBLE);

//        if (reason == EQRoundOverReason.ROR_MIN_GAME_NOT_ENOUTH_PLAYER.getValue()) {
//            // 连麦小游戏人数不够
//        } else if (reason == EQRoundOverReason.ROR_MIN_GAME_OWNER_END_ROUND.getValue()) {
//            // 连麦小游戏房主结束轮次
//        } else if (reason == EQRoundOverReason.ROR_MIN_GAME_NOT_PLAY.getValue()) {
//            // 用户不玩了
//        } else {
//            // 都算正常结束
//        }

        animationGo();
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationLeave();
            }
        }, 1500);
    }

    /**
     * 入场动画
     */
    private void animationGo() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F, 0.0F);
            mEnterTranslateAnimation.setDuration(200);
        }
        this.startAnimation(mEnterTranslateAnimation);
    }

    /**
     * 离场动画
     */
    public void animationLeave() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mLeaveTranslateAnimation.setDuration(200);
            }
            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearAnimation();
                    setVisibility(GONE);
                    if (mSVGAListener != null) {
                        mSVGAListener.onFinished();
                    }

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.startAnimation(mLeaveTranslateAnimation);
        } else {
            clearAnimation();
            setVisibility(GONE);
            if (mSVGAListener != null) {
                mSVGAListener.onFinished();
            }
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mUiHandler.removeCallbacksAndMessages(null);
            if (mEnterTranslateAnimation != null) {
                mEnterTranslateAnimation.setAnimationListener(null);
                mEnterTranslateAnimation.cancel();
            }
            if (mLeaveTranslateAnimation != null) {
                mLeaveTranslateAnimation.setAnimationListener(null);
                mLeaveTranslateAnimation.cancel();
            }

        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHandler.removeCallbacksAndMessages(null);
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
    }
}
