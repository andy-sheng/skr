package com.module.playways.grab.room.top;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabMyCoinChangeEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GrabTopView extends RelativeLayout {

    ExTextView mTvChangeRoom;
    ExTextView mTvCoin;
    ExTextView mTvCoinChange;
    ExTextView mTvAcc;
    ImageView mIvAccDisable;

    Listener mOnClickChangeRoomListener;
    GrabRoomData mBaseRoomData;

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
        playCoinChangeAnimation(event.coinChange);
    }

    private void playCoinChangeAnimation(int coinChange) {
        mTvCoinChange.setVisibility(VISIBLE);
        if (coinChange > 0) {
            mTvCoinChange.setText("+ " + Math.abs(coinChange));
        } else {
            mTvCoinChange.setText("- " + Math.abs(coinChange));
        }

        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            ObjectAnimator translateAnimation = ObjectAnimator.ofFloat(mTvCoinChange, TRANSLATION_Y, 0f, -U.getDisplayUtils().dip2px(30));
            ObjectAnimator alphAnimation = ObjectAnimator.ofFloat(mTvCoinChange, View.ALPHA, 1f, 0f);

            mAnimatorSet.setDuration(1000);
            mAnimatorSet.playTogether(translateAnimation, alphAnimation);
        }
        mAnimatorSet.start();
    }

    public void init() {
        inflate(getContext(), R.layout.grab_top_view, this);
        mTvChangeRoom = (ExTextView) findViewById(R.id.tv_change_room);
        mTvCoin = (ExTextView) findViewById(R.id.tv_coin);
        mTvCoinChange = (ExTextView) findViewById(R.id.tv_coin_change);
        mTvAcc = (ExTextView) findViewById(R.id.tv_acc);
        mIvAccDisable = (ImageView) findViewById(R.id.iv_acc_disable);

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

        mTvAcc.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIvAccDisable.getVisibility() == VISIBLE) {
                    mIvAccDisable.setVisibility(GONE);
                    mBaseRoomData.setAccEnable(true);
                } else {
                    mIvAccDisable.setVisibility(VISIBLE);
                    mBaseRoomData.setAccEnable(false);
                }
            }
        });
    }

    public void onSing(){
        mTvAcc.setVisibility(GONE);
        mIvAccDisable.setVisibility(GONE);
    }

    public void onGrab(){
//        mTvAcc.setVisibility(VISIBLE);
//        mIvAccDisable.setVisibility(VISIBLE);
        mTvAcc.setVisibility(GONE);
        mIvAccDisable.setVisibility(GONE);
    }

    public void setRoomData(GrabRoomData modelBaseRoomData) {
        mBaseRoomData = modelBaseRoomData;
        mTvCoin.setText(mBaseRoomData.getCoin() + "");
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
    }
}
