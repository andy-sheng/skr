package com.module.playways.grab.room.top;

import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GrabRoomType;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;

public class GrabTopView extends RelativeLayout {

    ExTextView mTvChangeRoom;
    //    BitmapTextView mTvCoin;
//    ExTextView mTvCoinChange;
    ImageView mIvVoiceSetting;
    ImageView mCameraIv;
    ImageView mGameRuleIv;
    ImageView mFeedBackIv;
//    ExTextView mTvHzChange;
//    BitmapTextView mTvHz;

    ExTextView mExitTv;

    Listener mOnClickChangeRoomListener;
    GrabRoomData mGrabRoomData;

    ExImageView mIvHzIcon;

    AnimatorSet mAnimatorSet;  //金币加减的动画
    AnimatorSet mHzAnimatorSet;  //金币加减的动画

    public GrabTopView(Context context) {
        super(context);
        init();
    }

    public GrabTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabTopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(Listener onClickChangeRoomListener) {
        mOnClickChangeRoomListener = onClickChangeRoomListener;
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(GrabMyCoinChangeEvent event) {
//        mTvCoin.setText(event.coin + "");
//        mCoin = event.coin;
//        playCoinChangeAnimation(event.coinChange);
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(UpdateCoinEvent event) {
//        if (mCoin != event.getCoinBalance()) {
//            if (event.getTs() == 0) {
//                //从购买礼物来的
//                mCoin = event.getCoinBalance();
//                mTvCoin.setText(event.getCoinBalance() + "");
//            } else if (lastTs < event.getTs()) {
//                lastTs = event.getTs();
//                mCoin = event.getCoinBalance();
//                mTvCoin.setText(event.getCoinBalance() + "");
//            }
//        }
//
//        mGrabRoomData.setCoinNoEvent(mCoin);
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(UpdateHZEvent event) {
//        if (mHz != event.getHz()) {
//            if (lastHzTs < event.getTs()) {
//                float hzChange = event.getHz() - mHz;
//                lastHzTs = event.getTs();
//                mHz = event.getHz();
//                mTvHz.setText(String.format("%.1f", event.getHz()));
//                playHZChangeAnimation(hzChange);
//            }
//        }
//    }

//    private void playHZChangeAnimation(float hzChange) {
//        mTvHzChange.setText(String.format("+ %.1f", hzChange));
//
//        if (mHzAnimatorSet == null) {
//            mHzAnimatorSet = new AnimatorSet();
//            ObjectAnimator translateAnimation = ObjectAnimator.ofFloat(mTvHzChange, TRANSLATION_Y, 0f, -U.getDisplayUtils().dip2px(30));
//            ObjectAnimator alphAnimation = ObjectAnimator.ofFloat(mTvHzChange, View.ALPHA, 1f, 0f);
//
//            // 高度确定，直接写死中心点
//            mHzAnimatorSet.setDuration(1000);
//            mHzAnimatorSet.playTogether(translateAnimation, alphAnimation);
//        } else {
//            mHzAnimatorSet.cancel();
//        }
//
//        mTvHzChange.setVisibility(VISIBLE);
//        mHzAnimatorSet.removeAllListeners();
//        mHzAnimatorSet.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mTvHzChange.setVisibility(GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//                onAnimationEnd(animation);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        mHzAnimatorSet.start();
//    }

//    private void playCoinChangeAnimation(int coinChange) {
//        if (coinChange > 0) {
//            mTvCoinChange.setText("+ " + Math.abs(coinChange));
//        } else {
//            mTvCoinChange.setText("- " + Math.abs(coinChange));
//        }
//
//        if (mAnimatorSet == null) {
//            mAnimatorSet = new AnimatorSet();
//            ObjectAnimator translateAnimation = ObjectAnimator.ofFloat(mTvCoinChange, TRANSLATION_Y, 0f, -U.getDisplayUtils().dip2px(30));
//            ObjectAnimator alphAnimation = ObjectAnimator.ofFloat(mTvCoinChange, View.ALPHA, 1f, 0f);
//
//            // 高度确定，直接写死中心点
//            mAnimatorSet.setDuration(1000);
//            mAnimatorSet.playTogether(translateAnimation, alphAnimation);
//        } else {
//            mAnimatorSet.cancel();
//        }
//
//        mTvCoinChange.setVisibility(VISIBLE);
//        mAnimatorSet.removeAllListeners();
//        mAnimatorSet.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mTvCoinChange.setVisibility(GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//                onAnimationEnd(animation);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        mAnimatorSet.start();
//    }


    public void init() {
        inflate(getContext(), R.layout.grab_top_view, this);
        mTvChangeRoom = (ExTextView) findViewById(R.id.tv_change_room);
        mCameraIv = (ImageView) findViewById(R.id.camera_iv);
        mGameRuleIv = (ImageView) findViewById(R.id.game_rule_iv);
        mFeedBackIv = (ImageView) findViewById(R.id.feed_back_iv);
        mExitTv = (ExTextView) findViewById(R.id.exit_tv);
//        mTvCoin = (BitmapTextView) findViewById(R.id.tv_coin);
//        mTvCoinChange = (ExTextView) findViewById(R.id.tv_coin_change);
        mIvVoiceSetting = (ImageView) findViewById(R.id.iv_voice_setting);
        mIvHzIcon = (ExImageView) findViewById(R.id.iv_hz_icon);
//        mTvHzChange = (ExTextView) findViewById(R.id.tv_hz_change);
//        mTvHz = (BitmapTextView) findViewById(R.id.tv_hz);
//        mTvHz.setText("0");

        mTvChangeRoom.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.changeRoom();
//                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                            "game_changeroom", null);
                }
            }
        });

        mIvVoiceSetting.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.onClickVoiceAudition();
                }
            }
        });

        mCameraIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.onClickCamera();
                }
            }
        });

        mGameRuleIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.onClickGameRule();
                }
            }
        });

        mFeedBackIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.onClickFeedBack();
                }
            }
        });

        mExitTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.closeBtnClick();
                }
            }
        });
    }

    public void setRoomData(GrabRoomData modelBaseRoomData) {
        mGrabRoomData = modelBaseRoomData;

        if (mGrabRoomData.isOwner()) {
            // 是房主，肯定不能切换房间
            setChangeRoomBtnVisiable(false);
        } else {
            // 观众的话，私密房间也不能切
            if (mGrabRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_SECRET ||
                    mGrabRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_FRIEND) {
                setChangeRoomBtnVisiable(false);
            } else {
                setChangeRoomBtnVisiable(true);
            }
        }

        if (mGrabRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_GUIDE) {
            // 新手房
            setChangeRoomBtnVisiable(false);
            mIvVoiceSetting.setVisibility(GONE);
        }
    }

    /**
     * 切换房间按钮是否可见
     *
     * @param visiable
     */
    void setChangeRoomBtnVisiable(boolean visiable) {
        if (visiable) {
            mTvChangeRoom.setVisibility(VISIBLE);
//            LayoutParams lp = (LayoutParams) mTvCoin.getLayoutParams();
//            lp.addRule(RelativeLayout.RIGHT_OF, mTvChangeRoom.getId());
//            lp.leftMargin = U.getDisplayUtils().dip2px(10);
//            mTvCoin.setLayoutParams(lp);
        } else {
            mTvChangeRoom.setVisibility(GONE);
//            LayoutParams lp = (LayoutParams) mTvCoin.getLayoutParams();
//            lp.addRule(RelativeLayout.RIGHT_OF, -1);
//            lp.leftMargin = U.getDisplayUtils().dip2px(16);
//            mTvCoin.setLayoutParams(lp);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimatorSet != null) {
            mAnimatorSet.removeAllListeners();
            mAnimatorSet.cancel();
        }

        if (mHzAnimatorSet != null) {
            mHzAnimatorSet.removeAllListeners();
            mHzAnimatorSet.cancel();
        }
    }

    public interface Listener {
        void changeRoom();

        void closeBtnClick();

        void onVoiceChange(boolean voiceOpen);

        void onClickGameRule();

        void onClickFeedBack();

        void onClickVoiceAudition();

        void onClickCamera();
    }
}
