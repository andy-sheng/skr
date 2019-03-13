package com.module.playways.rank.room.gift;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.module.playways.rank.room.gift.model.GiftDataUtils;
import com.module.playways.rank.room.gift.model.GiftPlayModel;
import com.module.rank.R;

/**
 * Created by yangjiawei on 2017/8/7.
 */

public class GiftContinuousView extends RelativeLayout {
    private String TAG = "GiftContinuousView";
    static final int STATUS_IDLE = 1;
    static final int STATUS_STEP1 = 2;
    static final int STATUS_STEP2 = 3;
    static final int STATUS_WAIT_OVER = 4;

    static final int MSG_DISPLAY_OVER = 10;

    static final int MSG_DISPLAY_ENSUSE_OVER = 11;// 无法结束的容错逻辑
    int mId;

    ExRelativeLayout mInfoContainer;
    BaseImageView mSendAvatarIv;
    ExTextView mDescTv;
    BaseImageView mGiftImgIv;
    ExTextView mGiftNumTv;
    ObjectAnimator mStep1Animator;
    AnimatorSet mStep2Animator;
    ExTextView mSenderNameTv;

    int mCurNum = 1;

    GiftPlayModel mCurGiftPlayModel;

    int mCurStatus = STATUS_IDLE;

    Listener mListener;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISPLAY_OVER:
                    onPlayOver();
                    break;
                case MSG_DISPLAY_ENSUSE_OVER:
                    onPlayOver();
                    break;
            }
        }
    };

    public GiftContinuousView(Context context) {
        super(context);
        init();
    }

    public GiftContinuousView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GiftContinuousView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.gift_continue_view_layout, this);
        mInfoContainer = (ExRelativeLayout) this.findViewById(R.id.info_container);
        mSendAvatarIv = (BaseImageView) this.findViewById(R.id.send_avatar_iv);
        mDescTv = (ExTextView) this.findViewById(R.id.desc_tv);
        mGiftImgIv = (BaseImageView) this.findViewById(R.id.gift_img_iv);
        mGiftNumTv = (ExTextView) this.findViewById(R.id.gift_num_tv);
        mSenderNameTv = (ExTextView) this.findViewById(R.id.sender_name_tv);
    }

    public boolean play(GiftPlayModel model) {
        if (mCurStatus != STATUS_IDLE) {
            return false;
        }
        mCurGiftPlayModel = model;
        AvatarUtils.loadAvatarByUrl(mSendAvatarIv, AvatarUtils.newParamsBuilder(model.getSender().getAvatar())
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColorBySex(model.getSender().getIsMale())
                .build()
        );
        mSenderNameTv.setText(model.getSender().getNickname());
        mDescTv.setText(model.getAction());
        int resId = 0;
        switch (model.getEmojiType()) {
            case SP_EMOJI_TYPE_LIKE:
                resId = R.drawable.yanchangjiemian_xin;
                break;
            case SP_EMOJI_TYPE_UNLIKE:
                resId = R.drawable.yanchangjiemian_dabian;
                break;
        }
        FrescoWorker.loadImage(mGiftImgIv, ImageFactory.newResImage(resId)
                .build());

//        mCurNum = model.getBeginCount();
//        mGiftNumTv.setText("X" + mCurNum);
        step1();
        return true;
    }

    private void step1() {
        mUiHandler.removeMessages(MSG_DISPLAY_ENSUSE_OVER);
        mUiHandler.sendEmptyMessageDelayed(MSG_DISPLAY_ENSUSE_OVER, 5000);

        mCurStatus = STATUS_STEP1;
        this.setVisibility(VISIBLE);
        mGiftNumTv.setVisibility(GONE);
        if (mStep1Animator == null) {
            mStep1Animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, -U.getDisplayUtils().dip2px(150), 0);
            mStep1Animator.setDuration(300);
            mStep1Animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    step2(mCurGiftPlayModel.getBeginCount());
                }
            });
        }
        mStep1Animator.start();
    }

    private void step2(int count) {
        mUiHandler.removeMessages(MSG_DISPLAY_ENSUSE_OVER);
        mUiHandler.sendEmptyMessageDelayed(MSG_DISPLAY_ENSUSE_OVER, 5000);
        //目前没有
        mCurStatus = STATUS_STEP2;
        mCurNum = count;
        mGiftNumTv.setVisibility(VISIBLE);
        mGiftNumTv.setText("X" + count);
        if (mStep2Animator == null) {
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mGiftNumTv, View.SCALE_X, 1.2f, 0.9f, 1);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mGiftNumTv, View.SCALE_Y, 1.2f, 0.9f, 1);
            mStep2Animator = new AnimatorSet();
            mStep2Animator.playTogether(objectAnimator1, objectAnimator2);
            mStep2Animator.setDuration(300);
            mStep2Animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCurNum >= mCurGiftPlayModel.getEndCount()) {
                        mCurStatus = STATUS_WAIT_OVER;
                        mUiHandler.removeMessages(MSG_DISPLAY_OVER);
                        mUiHandler.sendEmptyMessageDelayed(MSG_DISPLAY_OVER, 1000);
                    } else {
                        step2(mCurNum + 1);
                    }
                }
            });
        }
        mStep2Animator.start();
    }

    private void onPlayOver() {
        mCurStatus = STATUS_IDLE;
        this.setVisibility(GONE);
        if (mListener != null) {
            mListener.onPlayOver(this, mCurGiftPlayModel);
        }
        mUiHandler.removeCallbacksAndMessages(null);
    }

    public boolean isIdle() {
        MyLog.d(TAG + hashCode(), "isIdle mCurStatus=" + mCurStatus);
        return mCurStatus == STATUS_IDLE;
    }

    public boolean accept(GiftPlayModel playModel) {
        if (GiftDataUtils.sameContinueId(mCurGiftPlayModel, playModel)) {
            int endCount = playModel.getEndCount();
            if (endCount > mCurGiftPlayModel.getEndCount()) {
                mCurGiftPlayModel.setEndCount(endCount);
                return true;
            }
        }
        return false;
    }

    public void tryTriggerAnimation() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCurStatus == STATUS_WAIT_OVER) {
                    //等待结束
                    if (mCurNum < mCurGiftPlayModel.getEndCount()) {
                        mUiHandler.removeMessages(MSG_DISPLAY_OVER);
                        step2(mCurNum + 1);
                    }
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mStep1Animator != null) {
            mStep1Animator.cancel();
        }
        if (mStep2Animator != null) {
            mStep2Animator.cancel();
        }
        mCurStatus = STATUS_IDLE;
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setMyId(int id) {
        mId = id;
    }

    public interface Listener {
        void onPlayOver(GiftContinuousView giftContinuousView, GiftPlayModel giftPlayModel);
    }
}
