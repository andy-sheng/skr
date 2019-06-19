package com.module.playways.grab.room.view.minigame;

import android.os.Handler;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.common.view.ExViewStub;

public class MiniGameRoundOverCardView extends ExViewStub {

    ImageView mMiniGameBgIv;
    SVGAListener mSVGAListener;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    Handler mUiHandler = new Handler();

    public MiniGameRoundOverCardView(ViewStub viewStub) {
        super(viewStub);
    }

    @Override
    protected void init(View parentView) {
        mMiniGameBgIv = (ImageView) parentView.findViewById(R.id.mini_game_bg_iv);
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_mini_game_over_card_stub_layout;
    }

    public void bindData(GrabRoundInfoModel lastRoundInfo, SVGAListener listener) {
        if (lastRoundInfo == null) {
            return;
        }
        tryInflate();
        this.mSVGAListener = listener;
        mParentView.setVisibility(View.VISIBLE);

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
        mParentView.startAnimation(mEnterTranslateAnimation);
    }

    /**
     * 离场动画
     */
    public void animationLeave() {
        if (mParentView != null) {
            if (mParentView.getVisibility() == View.VISIBLE) {
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
                        mParentView.clearAnimation();
                        setVisibility(View.GONE);
                        if (mSVGAListener != null) {
                            mSVGAListener.onFinished();
                        }

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mParentView.startAnimation(mLeaveTranslateAnimation);
            } else {
                mParentView.clearAnimation();
                setVisibility(View.GONE);
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
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
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
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
