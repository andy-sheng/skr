package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 游戏频道的item
 */
public class GameCardHolder extends RepeatHolder {
    protected View mMarginArea;
    protected View mArrayArea;

    protected int[] mTypeTvIds;
    protected int[] mDisplayTvIds;
    protected int[] mCountTvIds;
    protected int[] mTitleTvIds;
    protected int[] mShadowTvIds;
    protected int[] mAvatarIvIds;

    protected TextView[] mTypeTvs;
    protected TextView[] mDisplayTvs;
    protected TextView[] mCountTvs;
    protected TextView[] mTitleTvs;
    protected TextView[] mShadowTvs;
    protected BaseImageView[] mAvatarIvs;

    public GameCardHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 2;
        mParentIds = new int[]{
                R.id.single_card_1,
                R.id.single_card_2,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.cover_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.name_tv);

        mTypeTvIds = new int[mViewSize];
        Arrays.fill(mTypeTvIds, R.id.type_tv);
        mDisplayTvIds = new int[mViewSize];
        Arrays.fill(mDisplayTvIds, R.id.display_tv);
        mCountTvIds = new int[mViewSize];
        Arrays.fill(mCountTvIds, R.id.count_tv);
        mTitleTvIds = new int[mViewSize];
        Arrays.fill(mTitleTvIds, R.id.title_tv);
        mShadowTvIds = new int[mViewSize];
        Arrays.fill(mShadowTvIds, R.id.shadow_tv);
        mBottomContainerId = R.id.bottom_container;
        mAvatarIvIds = new int[mViewSize];
        Arrays.fill(mAvatarIvIds, R.id.avatar_iv);
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mTypeTvs = new TextView[mViewSize];
        mDisplayTvs = new TextView[mViewSize];
        mCountTvs = new TextView[mViewSize];
        mTitleTvs = new TextView[mViewSize];
        mShadowTvs = new TextView[mViewSize];
        mBottomContainers = new View[mViewSize];
        mAvatarIvs = new BaseImageView[mViewSize];
        for (int i = 0; i < mViewSize; i++) {
            mTypeTvs[i] = $(mParentViews[i], mTypeTvIds[i]);
            mDisplayTvs[i] = $(mParentViews[i], mDisplayTvIds[i]);
            mCountTvs[i] = $(mParentViews[i], mCountTvIds[i]);
            mTitleTvs[i] = $(mParentViews[i], mTitleTvIds[i]);
            mShadowTvs[i] = $(mParentViews[i], mShadowTvIds[i]);
            mBottomContainers[i] = $(mParentViews[i], mBottomContainerId);
            mAvatarIvs[i] = $(mParentViews[i], mAvatarIvIds[i]);
        }

        mMarginArea = $(R.id.margin_area);
        mArrayArea = $(R.id.array_area);
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    @Override
    protected boolean isChangeImageSize() {
        return false;
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    @Override
    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        super.bindLiveModel(viewModel);

        // 如果没有标题，而且是组内第一个的话，就显示margin区域
        if (!mViewModel.hasHead() && viewModel.isFirst() && mPosition != 0) {
            mMarginArea.setVisibility(View.VISIBLE);
        } else {
            mMarginArea.setVisibility(View.GONE);
        }

        int height;
        if (mViewModel.isFullColumn()) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
            lp.rightMargin = lp.leftMargin = 0;

            height = (U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(1f)) / 2;
        } else {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
            lp.rightMargin = lp.leftMargin = SIDE_MARGIN;

            height = (U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN_TWO) / 2;
        }
        for (int i = 0; i < mViewSize; i++) {
            mImageViews[i].getLayoutParams().height = height;
        }
    }

    @Override
    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item, int i) {
        super.bindItemOnLiveModel(item, i);
        bindText(mTypeTvs[i], item.getUpRightText());
        bindText(mTitleTvs[i], item.getUpLeftText());

        if (item.getUser() != null) {
            String avatarUrl = AvatarUtils.getAvatarUrlByCustom(item.getUser().getUserId(), ImageUtils.SIZE.SIZE_480, item.getUser().getAvatar(), false);
            FrescoWorker.loadImage(mAvatarIvs[i],
                    ImageFactory.newHttpImage(avatarUrl)
                            .setIsCircle(true)
                            .setLoadingDrawable(U.app().getResources().getDrawable(R.drawable.loading_empty))
                            .setFailureDrawable(U.app().getResources().getDrawable(R.drawable.loading_empty))
                            .build()
            );
            bindText(mDisplayTvs[i], item.getUser().getUserNickname());
        }
    }

    @Override
    protected void resetItem(int i) {
        mCountTvs[i].setVisibility(View.GONE);
        mShadowTvs[i].setVisibility(View.GONE);
    }

    @Override
    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item, int i) {
        // 重复做一步，因为其他四个类型没有title，这里给BaseLiveItem特殊处理
        mTextViews[i].setText(item.getTitleText());

        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
//        bindText(mDisplayTvs[i], item.getLocationText());
    }

    @Override
    protected void bindUserItem(ChannelLiveViewModel.UserItem item, int i) {
//        bindText(mDisplayTvs[i], item.getDisplayText());
    }

    @Override
    protected void bindVideoItem(ChannelLiveViewModel.VideoItem item, int i) {
        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
//        bindText(mDisplayTvs[i], item.getDisplayText());
    }

    @Override
    protected void bindTVItem(ChannelLiveViewModel.TVItem item, int i) {
//        bindText(mDisplayTvs[i], item.getDisplayText());
    }

    @Override
    protected void bindSimpleItem(ChannelLiveViewModel.SimpleItem item, int i) {
//        bindText(mDisplayTvs[i], item.getDisplayText());
    }
}
