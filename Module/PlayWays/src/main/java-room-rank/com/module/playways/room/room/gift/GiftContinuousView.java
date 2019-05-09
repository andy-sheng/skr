package com.module.playways.room.room.gift;

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

import com.common.callback.Callback;
import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.module.playways.room.gift.event.BigGiftMsgEvent;
import com.module.playways.room.gift.event.OverlayGiftBrushMsgEvent;
import com.module.playways.room.gift.view.ContinueTextView;
import com.module.playways.room.room.gift.model.GiftPlayModel;
import com.module.playways.R;

import org.greenrobot.eventbus.EventBus;

import static com.module.playways.room.room.gift.model.GiftPlayControlTemplate.MEDIUM_GIFT;

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
    ContinueTextView mGiftNumTv;
    ObjectAnimator mStep1Animator;
    AnimatorSet mStep2Animator;
    ExTextView mSenderNameTv;

    GiftContinueViewGroup.GiftProvider mGiftProvider;

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

    public void setGiftProvider(GiftContinueViewGroup.GiftProvider giftProvider) {
        mGiftProvider = giftProvider;
    }

    private void init() {
        inflate(getContext(), R.layout.gift_continue_view_layout, this);
        mInfoContainer = (ExRelativeLayout) this.findViewById(R.id.info_container);
        mSendAvatarIv = (BaseImageView) this.findViewById(R.id.send_avatar_iv);
        mDescTv = (ExTextView) this.findViewById(R.id.desc_tv);
        mGiftImgIv = (BaseImageView) this.findViewById(R.id.gift_img_iv);
        mGiftNumTv = (ContinueTextView) this.findViewById(R.id.gift_num_tv);
        mSenderNameTv = (ExTextView) this.findViewById(R.id.sender_name_tv);
    }

    //只有在IDLE情况下才去拉数据
    public void tryNotifyHasGiftCanPlay() {
        if (mCurStatus != STATUS_IDLE) {
            return;
        }

        mGiftProvider.tryGetGiftModel(null, 1, mId, new Callback<GiftPlayModel>() {
            @Override
            public void onCallback(int r, GiftPlayModel newGiftPlayModel) {
                /**
                 * 这边得在异步线程立即设值
                 */
                MyLog.d(TAG, "tryNotifyHasGiftCanPlay onCallback view is " + mId + " newGiftPlayModel=" + newGiftPlayModel);
                mCurGiftPlayModel = newGiftPlayModel;
            }
        }, new Callback<GiftPlayModel>() {
            @Override
            public void onCallback(int r, GiftPlayModel newGiftPlayModel) {
                MyLog.d(TAG, "tryNotifyHasGiftCanPlay newGiftPlayModel" + newGiftPlayModel);
                if (newGiftPlayModel != null) {
                    play(newGiftPlayModel, false);
                }
            }
        });
    }

    public boolean play(GiftPlayModel model, boolean force) {
        if (mCurStatus != STATUS_IDLE && !force) {
            return false;
        }
        mCurGiftPlayModel = model;
        AvatarUtils.loadAvatarByUrl(mSendAvatarIv, AvatarUtils.newParamsBuilder(model.getSender().getAvatar())
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(Color.WHITE)
                .build()
        );

        mSenderNameTv.setText(model.getSender().getNickname());
        mDescTv.setText(model.getAction());

        if (model.getEGiftType() == GiftPlayModel.EGiftType.EMOJI) {
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

            mSenderNameTv.setText(model.getSender().getNickname() + model.getAction());
            mDescTv.setVisibility(GONE);
        } else if (model.getEGiftType() == GiftPlayModel.EGiftType.GIFT) {
            FrescoWorker.loadImage(mGiftImgIv, ImageFactory.newPathImage(model.getGiftIconUrl())
                    .setLoadingDrawable(U.getDrawable(R.drawable.skrer_logo))
                    .setFailureDrawable(U.getDrawable(R.drawable.skrer_logo))
                    .setWidth(U.getDisplayUtils().dip2px(45))
                    .setHeight(U.getDisplayUtils().dip2px(45))
                    .build());

            mSenderNameTv.setText(model.getSender().getNickname());
            mDescTv.setText("送给 " + model.getReceiver().getNickname());
            mDescTv.setVisibility(VISIBLE);
        }

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
        mGiftNumTv.setText(String.valueOf(count));
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
//                    if (mCurNum >= mCurGiftPlayModel.getEndCount()) {
//                        mCurStatus = STATUS_WAIT_OVER;
//                        mUiHandler.removeMessages(MSG_DISPLAY_OVER);
//                        mUiHandler.sendEmptyMessageDelayed(MSG_DISPLAY_OVER, 1000);
//                    } else {
//                        step2(mCurNum + 1);
//                    }

                    // TODO: 2019-05-08 取数据
                    GiftPlayModel giftPlayModels[] = new GiftPlayModel[1];
                    mGiftProvider.tryGetGiftModel(mCurGiftPlayModel, mCurNum, mId, new Callback<GiftPlayModel>() {
                        @Override
                        public void onCallback(int r, GiftPlayModel newGiftPlayModel) {
                            if (newGiftPlayModel != null) {
                                MyLog.d(TAG, "step2 onCallback view is " + mId + " newGiftPlayModel=" + newGiftPlayModel);
                                giftPlayModels[0] = mCurGiftPlayModel;
                                mCurGiftPlayModel = newGiftPlayModel;
                            }
                        }
                    }, new Callback<GiftPlayModel>() {
                        @Override
                        public void onCallback(int r, GiftPlayModel newGiftPlayModel) {
                            MyLog.d(TAG, "step2 newGiftPlayModel" + newGiftPlayModel);
                            //TODO 这里有 bug 吧
                            if (newGiftPlayModel != null) {
                                if (newGiftPlayModel.getSender().getUserId() == giftPlayModels[0].getSender().getUserId()
                                        && newGiftPlayModel.getContinueId() == giftPlayModels[0].getContinueId()
                                        && newGiftPlayModel.getEndCount() > mCurNum) {
                                    step2(++mCurNum);
//                                    setData(newGiftPlayModel);
                                } else {
                                    play(newGiftPlayModel, true);
                                }
                            } else {
                                mCurStatus = STATUS_WAIT_OVER;
                                mUiHandler.removeMessages(MSG_DISPLAY_OVER);
                                mUiHandler.sendEmptyMessageDelayed(MSG_DISPLAY_OVER, 1000);
                            }
                        }
                    });

                }
            });
        }

        mStep2Animator.start();
        if (mCurGiftPlayModel.getGift().isPlay() && mCurGiftPlayModel.getGift().getDisplayType() == MEDIUM_GIFT) {
            EventBus.getDefault().post(new OverlayGiftBrushMsgEvent(mCurGiftPlayModel));
        }
    }

    public GiftPlayModel getCurGiftPlayModel() {
        return mCurGiftPlayModel;
    }

    private void onPlayOver() {
        GiftPlayModel giftPlayModels[] = new GiftPlayModel[1];
        mGiftProvider.tryGetGiftModel(mCurGiftPlayModel, mCurNum, mId, new Callback<GiftPlayModel>() {
            @Override
            public void onCallback(int r, GiftPlayModel newGiftPlayModel) {
                MyLog.d(TAG, "onPlayOver onCallback view is " + mId + " newGiftPlayModel=" + newGiftPlayModel);
                if (newGiftPlayModel != null) {
                    giftPlayModels[0] = mCurGiftPlayModel;
                    mCurGiftPlayModel = newGiftPlayModel;
                }
            }
        }, new Callback<GiftPlayModel>() {
            @Override
            public void onCallback(int r, GiftPlayModel newGiftPlayModel) {
                MyLog.d(TAG, "onPlayOver newGiftPlayModel" + newGiftPlayModel);
                if (newGiftPlayModel != null) {
                    if (newGiftPlayModel.getSender().getUserId() == giftPlayModels[0].getSender().getUserId()
                            && newGiftPlayModel.getContinueId() == giftPlayModels[0].getContinueId()
                            && newGiftPlayModel.getEndCount() > mCurNum) {
                        step2(++mCurNum);
//                        setData(newGiftPlayModel);
                    } else {
                        play(newGiftPlayModel, true);
                    }
                } else {
                    mCurStatus = STATUS_IDLE;
                    mCurGiftPlayModel = null;
                    GiftContinuousView.this.setVisibility(GONE);
                    if (mListener != null) {
                        mListener.onPlayOver(GiftContinuousView.this, mCurGiftPlayModel);
                    }
                    mUiHandler.removeCallbacksAndMessages(null);
                    //自己结束的时候也去查看礼物新数据
                    tryNotifyHasGiftCanPlay();
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
        TAG = "GiftContinuousView " + id;
    }

    public interface Listener {
        void onPlayOver(GiftContinuousView giftContinuousView, GiftPlayModel giftPlayModel);
    }
}
