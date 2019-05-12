package com.module.playways.room.room.gift;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.module.playways.R;
import com.module.playways.room.room.gift.model.GiftPlayModel;

public class GiftBigContinuousView extends RelativeLayout {
    public final static String TAG = "GiftBigContinuousView";

    ExRelativeLayout mInfoContainer;
    BaseImageView mSendAvatarIv;
    ExTextView mDescTv;
    BaseImageView mGiftImgIv;
    ObjectAnimator mStep1Animator;
    ExTextView mSenderNameTv;

    GiftContinueViewGroup.GiftProvider mGiftProvider;

    GiftPlayModel mCurGiftPlayModel;

    public GiftBigContinuousView(Context context) {
        super(context);
        init();
    }

    public GiftBigContinuousView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GiftBigContinuousView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setGiftProvider(GiftContinueViewGroup.GiftProvider giftProvider) {
        mGiftProvider = giftProvider;
    }

    private void init() {
        inflate(getContext(), R.layout.gift_big_continue_view_layout, this);
        mInfoContainer = (ExRelativeLayout) this.findViewById(R.id.info_container);
        mSendAvatarIv = (BaseImageView) this.findViewById(R.id.send_avatar_iv);
        mDescTv = (ExTextView) this.findViewById(R.id.desc_tv);
        mGiftImgIv = (BaseImageView) this.findViewById(R.id.gift_img_iv);
        mSenderNameTv = (ExTextView) this.findViewById(R.id.sender_name_tv);
    }

    public boolean play(GiftPlayModel model) {
        mCurGiftPlayModel = model;
        AvatarUtils.loadAvatarByUrl(mSendAvatarIv, AvatarUtils.newParamsBuilder(model.getSender().getAvatar())
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(Color.WHITE)
                .build()
        );

        mSenderNameTv.setText(model.getSender().getNickname());
        mDescTv.setText(model.getAction());

        if (model.getEGiftType() == GiftPlayModel.EGiftType.GIFT) {
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

        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mStep1Animator != null) {
            mStep1Animator.cancel();
        }
    }

}
