package com.module.playways.grab.room.top;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GrabRoomType;
import com.component.busilib.view.BitmapTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabMyCoinChangeEvent;
import com.module.playways.R;
import com.module.playways.room.gift.event.UpdateCoinAndDiamondEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GrabTopView extends RelativeLayout {

    ExTextView mTvChangeRoom;
    BitmapTextView mTvCoin;
    ExImageView mConinChangeIv;
    ExTextView mTvCoinChange;
    ImageView mIvVoiceSetting;

    Listener mOnClickChangeRoomListener;
    GrabRoomData mGrabRoomData;

    int mCoin = 0;

    AnimatorSet mAnimatorSet;  //金币加减的动画

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabMyCoinChangeEvent event) {
        mTvCoin.setText(event.coin + "");
        mCoin = event.coin;
        playCoinChangeAnimation(event.coinChange);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateCoinAndDiamondEvent event) {
        if (mCoin != event.getCoinBalance()) {
            mCoin = event.getCoinBalance();
            mTvCoin.setText(event.getCoinBalance() + "");
//            playCoinChangeAnimation(event.getCoinBalance());
        }
    }

    private void playCoinChangeAnimation(int coinChange) {
        mTvCoinChange.setVisibility(VISIBLE);
        mConinChangeIv.setVisibility(VISIBLE);
        if (coinChange > 0) {
            mTvCoinChange.setText("+ " + Math.abs(coinChange));
        } else {
            mTvCoinChange.setText("- " + Math.abs(coinChange));
        }

        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            ObjectAnimator translateAnimation = ObjectAnimator.ofFloat(mTvCoinChange, TRANSLATION_Y, 0f, -U.getDisplayUtils().dip2px(30));
            ObjectAnimator alphAnimation = ObjectAnimator.ofFloat(mTvCoinChange, View.ALPHA, 1f, 0f);

            ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(mConinChangeIv, ROTATION, 0f, 360f);
            // 高度确定，直接写死中心点
            mConinChangeIv.setPivotX(U.getDisplayUtils().dip2px(19));
            mConinChangeIv.setPivotY(U.getDisplayUtils().dip2px(19));

            mAnimatorSet.setDuration(1000);
            mAnimatorSet.playTogether(translateAnimation, alphAnimation, rotateAnimation);
        }

        mAnimatorSet.removeAllListeners();
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mConinChangeIv.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimatorSet.start();

    }

    public void init() {
        inflate(getContext(), R.layout.grab_top_view, this);
        mTvChangeRoom = (ExTextView) findViewById(R.id.tv_change_room);
        mTvCoin = (BitmapTextView) findViewById(R.id.tv_coin);
        mConinChangeIv = (ExImageView) findViewById(R.id.conin_change_iv);
        mTvCoinChange = (ExTextView) findViewById(R.id.tv_coin_change);
        mIvVoiceSetting = (ImageView) findViewById(R.id.iv_voice_setting);

        mTvChangeRoom.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener.changeRoom();
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                            "game_changeroom", null);
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
    }

    public void setRoomData(GrabRoomData modelBaseRoomData) {
        mGrabRoomData = modelBaseRoomData;
        mTvCoin.setText(mGrabRoomData.getCoin() + "");
        mCoin = mGrabRoomData.getCoin();

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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        if (mAnimatorSet != null) {
            mAnimatorSet.removeAllListeners();
            mAnimatorSet.cancel();
        }
    }

    public interface Listener {
        void changeRoom();

        void addFirend();

        void onClickVoiceAudition();
    }
}
