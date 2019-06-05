package com.module.playways.room.gift.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.gift.event.ShowHalfRechargeFragmentEvent;
import com.module.playways.room.gift.inter.IContinueSendView;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.presenter.BuyGiftPresenter;

import org.greenrobot.eventbus.EventBus;

import java.util.Observer;

import static com.module.playways.room.gift.presenter.BuyGiftPresenter.ErrCoinNotEnough;
import static com.module.playways.room.gift.presenter.BuyGiftPresenter.ErrPresentObjLeave;
import static com.module.playways.room.gift.presenter.BuyGiftPresenter.ErrSystem;
import static com.module.playways.room.gift.presenter.BuyGiftPresenter.ErrZSNotEnough;

public class ContinueSendView extends FrameLayout implements IContinueSendView {
    public final static String TAG = "ContinueSendView";

    public static final int MSG_HIDE = 101;
    public static final int MSG_SHOW_RECHARGE = 102;

    ImageView mIvBg;
    ImageView mIvContinueText;
    ContinueTextView mTvContinueNum;

    BaseGift mBaseGift;

    GrabRoomData mBaseRoomData;

    BuyGiftPresenter mBuyGiftPresenter;

    AnimatorSet mScaleAnimatorSet;

    AnimatorSet mJumpAnimatorSet;

    UserInfoModel mReceiver;

    OnVisibleStateListener mOnVisibleStateListener;

    private long mCanContinueDuration = 3000;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_HIDE) {
                setVisibility(GONE);
            } else if (msg.what == MSG_SHOW_RECHARGE) {
                EventBus.getDefault().post(new ShowHalfRechargeFragmentEvent());
            }
        }
    };

    public ContinueSendView(Context context) {
        super(context);
        init();
    }

    public ContinueSendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContinueSendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setBaseRoomData(GrabRoomData baseRoomData) {
        mBaseRoomData = baseRoomData;
    }

    public void setObserver(OnVisibleStateListener observer) {
        mOnVisibleStateListener = observer;
    }

    public void startBuy(BaseGift baseGift, UserInfoModel receiver) {
        mBaseGift = baseGift;
        mReceiver = receiver;
        GrabRoundInfoModel infoModel = mBaseRoomData.getRealRoundInfo();
        if (infoModel != null) {
            if (baseGift.isCanContinue()) {
                mBuyGiftPresenter.buyGift(baseGift, mBaseRoomData.getGameId(), mBaseRoomData.getRealRoundSeq(), infoModel.isSingStatus(), receiver);
                setVisibility(VISIBLE);

                mHandler.removeMessages(MSG_HIDE);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE), mCanContinueDuration);
            } else {
                mBuyGiftPresenter.buyGift(baseGift, mBaseRoomData.getGameId(), mBaseRoomData.getRealRoundSeq(), infoModel.isSingStatus(), receiver);
            }
        } else {
            MyLog.w(TAG, "startBuy" + " baseGift=" + baseGift + " receiver=" + receiver);
        }
    }

    private void init() {
        inflate(getContext(), R.layout.continue_send_view_layout, this);
//        EventBus.getDefault().register(this);
        mIvBg = (ImageView) findViewById(R.id.iv_bg);
        mIvContinueText = (ImageView) findViewById(R.id.iv_continue_text);
        mTvContinueNum = (ContinueTextView) findViewById(R.id.tv_continue_num);

        mBuyGiftPresenter = new BuyGiftPresenter(this);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GrabRoundInfoModel grabRoundInfoModel = mBaseRoomData.getRealRoundInfo();
                if (grabRoundInfoModel != null) {
                    mBuyGiftPresenter.buyGift(mBaseGift, mBaseRoomData.getGameId(), mBaseRoomData.getRealRoundSeq(), grabRoundInfoModel.isSingStatus(), mReceiver);

                    if (mScaleAnimatorSet != null) {
                        mScaleAnimatorSet.cancel();
                    }

                    ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(ContinueSendView.this, "scaleX", 1.0f, 0.8f, 1.0f);
                    ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(ContinueSendView.this, "scaleY", 1.0f, 0.8f, 1.0f);
                    mScaleAnimatorSet = new AnimatorSet();
                    mScaleAnimatorSet.play(objectAnimator1).with(objectAnimator2);
                    mScaleAnimatorSet.setDuration(500);
                    mScaleAnimatorSet.start();

                    mHandler.removeMessages(MSG_HIDE);
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE), mCanContinueDuration);
                } else {
                    MyLog.w(TAG, "ContinueSendView" + " mBaseRoomData.getRealRoundInfo()=null");
                }
            }
        });
    }

    @Override
    public void buySuccess(BaseGift baseGift, int continueCount) {
        mTvContinueNum.setText(String.valueOf(continueCount));
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mTvContinueNum, "translationY", 0.0f, -40);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mTvContinueNum, "alpha", 1.0f, 0.7f, 0.0f);

        if (mJumpAnimatorSet != null) {
            mJumpAnimatorSet.cancel();
        }

        mJumpAnimatorSet = new AnimatorSet();
        mJumpAnimatorSet.play(objectAnimator1).with(objectAnimator2);
        mJumpAnimatorSet.setDuration(500);
        mJumpAnimatorSet.start();
    }

    @Override
    public void buyFaild(int erroCode, String errorMsg) {
        switch (erroCode) {
            case ErrZSNotEnough:
                ToastUtils.showShort("钻石余额不足，充值后就可以送礼啦");
                mHandler.removeMessages(MSG_SHOW_RECHARGE);
                mHandler.sendEmptyMessageDelayed(MSG_SHOW_RECHARGE, 1000);
                mHandler.removeMessages(MSG_HIDE);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE), 0);
                break;
            case ErrPresentObjLeave:
                ToastUtils.showShort("送礼对象已离开，请重新选择");
                mHandler.removeMessages(MSG_HIDE);
                mHandler.sendEmptyMessageDelayed(MSG_HIDE, 0);
                break;
            case ErrCoinNotEnough:
                ToastUtils.showShort("金币余额不足");
                mHandler.removeMessages(MSG_HIDE);
                mHandler.sendEmptyMessageDelayed(MSG_HIDE, 0);
                break;
            case ErrSystem:
                ToastUtils.showShort(errorMsg);
                break;
            default:
                ToastUtils.showShort(errorMsg);
                break;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            if (mScaleAnimatorSet != null) {
                mScaleAnimatorSet.cancel();
            }
            if (mJumpAnimatorSet != null) {
                mJumpAnimatorSet.cancel();
            }

            mIvBg.clearAnimation();

            if (mOnVisibleStateListener != null) {
                mOnVisibleStateListener.onVisible(false);
            }
        } else {
            RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator lin = new LinearInterpolator();
            rotate.setInterpolator(lin);
            rotate.setDuration(1000);
            rotate.setRepeatCount(-1);
            rotate.setFillAfter(true);
            rotate.setStartOffset(10);
            mIvBg.setAnimation(rotate);

            if (mOnVisibleStateListener != null) {
                mOnVisibleStateListener.onVisible(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mScaleAnimatorSet != null) {
            mScaleAnimatorSet.cancel();
        }
        if (mJumpAnimatorSet != null) {
            mJumpAnimatorSet.cancel();
        }
        mIvBg.clearAnimation();
    }

    public void destroy() {
        mBuyGiftPresenter.destroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    public interface OnVisibleStateListener {
        void onVisible(boolean isVisible);
    }
}
