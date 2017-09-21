package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 带1个方形图的item
 */
public class OneCardHolder extends FixedHolder {
    private BaseImageView mCoverIv;
    private TextView mNameTv;
    private TextView mTypeTv;
    private TextView mDisplayTv;
    private TextView mCountTv;
    private TextView mTitleTv;
    private TextView mShadowTv;
    private View mBottomContainer;
    private BaseImageView mAvatarIv;

    public OneCardHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mCoverIv = $(R.id.cover_iv);
        mNameTv = $(R.id.name_tv);
        mTypeTv = $(R.id.type_tv);

        mDisplayTv = $(R.id.display_tv);
        mCountTv = $(R.id.count_tv);
        mTitleTv = $(R.id.title_tv);
        mShadowTv = $(R.id.shadow_tv);
        mBottomContainer = $(R.id.bottom_container);
        mAvatarIv = $(R.id.avatar_iv);
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    protected boolean isCircle() {
        return false;
    }

    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel model) {
        final ChannelLiveViewModel.BaseItem item = model.getFirstItem();
        if (item == null) {
            return;
        }
        exposureItem(item);

        MyLog.d(TAG, "bindLiveModel imageUrl : " + item.getImageUrl());
        if (mViewModel.isFullColumn()) {
            bindImage(mCoverIv, item.getImageUrl(), isCircle(), 320, 320, getScaleType());
        } else {
            bindImageWithBorder(mCoverIv, item.getImageUrl(), isCircle(), 320, 320, getScaleType());
        }
        mCoverIv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                    }
                });
        mNameTv.setText(item.getNameText());

        bindItemOnLiveModel(item);

        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
        }
    }

    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item) {
        bindText(mTypeTv, item.getUpRightText());
        bindText(mTitleTv, item.getUpLeftText());

        if (item.getUser() != null) {
            mBottomContainer.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //跳转个人资料页
                        }
                    }
            );
            String avatarUrl = AvatarUtils.getAvatarUrlByUidTs(item.getUser().getUid(),
                    AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, item.getUser().getAvatar());
            FrescoWorker.loadImage(mAvatarIv,
                    ImageFactory.newHttpImage(avatarUrl)
                            .setIsCircle(true)
                            .build()
            );

            bindText(mDisplayTv, item.getUser().getNickname());
        } else {
            mBottomContainer.setOnClickListener(null);
        }

        resetItem();
    }

    private void resetItem() {
        mCountTv.setVisibility(View.GONE);
        mShadowTv.setVisibility(View.GONE);
    }

    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item) {
        // 重复做一步，因为其他四个类型没有title，这里给BaseLiveItem特殊处理
        mNameTv.setText(item.getTitleText());

        mCountTv.setText(item.getCountString());
        mCountTv.setVisibility(View.VISIBLE);
        mShadowTv.setVisibility(View.VISIBLE);
//        bindText(mDisplayTvs[i], item.getLocationText());
    }

    protected void bindUserItem(ChannelLiveViewModel.UserItem item, int i) {
//        bindText(mDisplayTvs[i], item.getDisplayText());
    }

    protected void bindVideoItem(ChannelLiveViewModel.VideoItem item, int i) {
        mCountTv.setText(item.getCountString());
        mCountTv.setVisibility(View.VISIBLE);
        mShadowTv.setVisibility(View.VISIBLE);
//        bindText(mDisplayTvs[i], item.getDisplayText());
    }

    protected void bindTVItem(ChannelLiveViewModel.TVItem item, int i) {
//        bindText(mDisplayTvs[i], item.getDisplayText());
    }

    protected void bindSimpleItem(ChannelLiveViewModel.SimpleItem item, int i) {
//        bindText(mDisplayTvs[i], item.getDisplayText());
    }
}
