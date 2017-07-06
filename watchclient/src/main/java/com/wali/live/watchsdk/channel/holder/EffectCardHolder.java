package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoCallBackWrapper;
import com.base.image.fresco.view.MovableImageView;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.imagepipeline.image.ImageInfo;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 */
public class EffectCardHolder extends FixedHolder implements IEffectScrollHolder {
    protected static final float EFFECT_IMAGE_RATIO = 9f / 16;

    private MovableImageView mCoverIv;
    private TextView mTitleTv;

    private View mUserContainer;
    private BaseImageView mAvatarIv;
    private TextView mNameTv;

    private TextView mTypeTv;
    private TextView mCountTv;
    private TextView mMarkTv;

    private TextView mShadowTv;

    private int mHeight;
    private int mParentHeight;
    private boolean mEnableOffsetAnimation = false;//标记是否开启偏移的动画

    public EffectCardHolder(View itemView) {
        super(itemView);
        changeImageSize();
    }

    @Override
    protected void initContentView() {
        mCoverIv = $(R.id.cover_iv);
        mTitleTv = $(R.id.title_tv);

        mUserContainer = $(R.id.user_container);
        mAvatarIv = $(R.id.avatar_iv);
        mNameTv = $(R.id.name_tv);

        mTypeTv = $(R.id.type_tv);
        mCountTv = $(R.id.count_tv);
        mMarkTv = $(R.id.mark_tv);

        mShadowTv = $(R.id.shadow_tv);
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    protected void changeImageSize() {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mCoverIv.getLayoutParams();
        mlp.width = getImageWidth();
        mlp.height = mHeight = getImageHeight();
    }

    protected int getImageWidth() {
        return ViewGroup.MarginLayoutParams.MATCH_PARENT;
    }

    protected int getImageHeight() {
        // 比例按设计尺寸
        return (int) (GlobalData.screenWidth * EFFECT_IMAGE_RATIO);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel model) {
        final ChannelLiveViewModel.BaseItem item = model.getFirstItem();
        if (item == null) {
            return;
        }
        bindImageWithCallback(mCoverIv, item.getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_XLARGE), false, 640, 640, ScalingUtils.ScaleType.CENTER_CROP,
                new FrescoCallBackWrapper() {
                    @Override
                    public void processWithInfo(ImageInfo info) {
                        mViewModel.setImageSize(info.getWidth(), info.getHeight());
                        setFrameHeight();
                    }
                });
        mCoverIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpItem(item);
            }
        });

        if (!TextUtils.isEmpty(item.getLineOneText())) {
            mTitleTv.setVisibility(View.VISIBLE);
            mTitleTv.setText(ItemDataFormatUtils.getLiveTitle("", item.getLineOneText()));
            mTitleTv.setMovementMethod(new LinkMovementMethod());
        } else {
            mTitleTv.setVisibility(View.GONE);
        }

        if (item.getUser() != null) {
            mUserContainer.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //跳转到个人主页
                        }
                    }
            );
            mUserContainer.setVisibility(View.VISIBLE);
            AvatarUtils.loadAvatarByUidTs(mAvatarIv,
                    item.getUser().getUid(),
                    item.getUser().getAvatar(),
                    true);
            bindText(mNameTv, item.getUser().getNickname());
        } else {
            mUserContainer.setVisibility(View.INVISIBLE);
        }

        if (TextUtils.isEmpty(item.getUpRightText())) {
            mTypeTv.setVisibility(View.GONE);
        } else {
            mTypeTv.setVisibility(View.VISIBLE);
        }

        resetItem();
        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
        } else if (item instanceof ChannelLiveViewModel.VideoItem) {
            bindVideoItem((ChannelLiveViewModel.VideoItem) item);
        } else if (item instanceof ChannelLiveViewModel.SimpleItem) {
            bindSimpleItem((ChannelLiveViewModel.SimpleItem) item);
        }
    }

    private void setFrameHeight() {
        int frameHeight = mViewModel.getFrameHeight();
        mCoverIv.setFrameHeight(Math.max(frameHeight, mHeight));
    }

    private void resetItem() {
        mCountTv.setVisibility(View.GONE);
        mMarkTv.setVisibility(View.GONE);
        mShadowTv.setVisibility(View.VISIBLE);
    }

    private void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item) {
        if (item instanceof ChannelLiveViewModel.LiveItem) {
            if (((ChannelLiveViewModel.LiveItem) item).isTicket()) {
                mMarkTv.setVisibility(View.VISIBLE);
            }
        }
        mCountTv.setText(item.getCountString());
        mCountTv.setVisibility(View.VISIBLE);
    }

    private void bindVideoItem(ChannelLiveViewModel.VideoItem item) {
        mCountTv.setText(item.getCountString());
        mCountTv.setVisibility(View.VISIBLE);
    }

    private void bindSimpleItem(ChannelLiveViewModel.SimpleItem item) {
        mShadowTv.setVisibility(View.GONE);
    }

    @Override
    public void scrollEffect(int parentHeight) {
        if (mEnableOffsetAnimation) {
            mParentHeight = parentHeight;
            if (mParentHeight != 0) {
                setOffset();
            }
        }
    }

    private void setOffset() {
        mCoverIv.setHeight(mHeight, Math.max(mViewModel.getFrameHeight(), mHeight), mParentHeight);
    }

    /**
     * 设置是否开启偏移动画
     */
    public void enableOffsetAnimation(boolean enableOffsetAnimation) {
        mEnableOffsetAnimation = enableOffsetAnimation;
    }
}
