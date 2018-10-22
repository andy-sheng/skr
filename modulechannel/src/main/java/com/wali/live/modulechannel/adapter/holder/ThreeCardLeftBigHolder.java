package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;

/**
 * Created by zhaomin on 16-12-22.
 * @module 频道
 * @description 三个item 左边大 右边小 包括上下两个
 */
public class ThreeCardLeftBigHolder extends RepeatHolder {

    protected int mCountTvId;
    protected int mShadowId;

    protected TextView[] mCountTvs;
    protected View[] mShadowTvs;

    public ThreeCardLeftBigHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 3;
        mParentIds = new int[]{
                R.id.single_card_1,
                R.id.single_card_2,
                R.id.single_card_3,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.cover_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.name_tv);
        mCountTvId = R.id.count_tv;
        mShadowId = R.id.shadow_tv;
        mBottomContainerId = R.id.bottom_container;
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mCountTvs = new TextView[mViewSize];
        mShadowTvs = new View[mViewSize];
        mBottomContainers = new ViewGroup[mViewSize];

        for (int i = 0; i < mViewSize; i++) {
            mCountTvs[i] = $(mParentViews[i], mCountTvId);
            mShadowTvs[i] = $(mParentViews[i], mShadowId);
            mBottomContainers[i] = $(mParentViews[i], mBottomContainerId);
        }
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    protected void bindImage(ChannelLiveViewModel.BaseItem item, int i) {
        int height = 0;
        int width = 0;
        if (i < 2) {
            height = U.getDisplayUtils().dip2px(116);
            width = U.getDisplayUtils().dip2px(116);
        } else if (i == 2) {
            width = U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 3 - U.getDisplayUtils().dip2px(116);
            height = U.getDisplayUtils().dip2px(305.33f);
        }
        bindImageWithBorder(mImageViews[i], item.getImageUrl(), isCircle(), width, height, getScaleType());
    }

    @Override
    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item, int i) {
        mCountTvs[i].setText(String.valueOf(item.getViewerCnt()));
        mCountTvs[i].setVisibility(View.VISIBLE);
        mCountTvs[i].setText(item.getCountString());
        bindImage(item, i);
    }

    @Override
    protected void bindUserItem(ChannelLiveViewModel.UserItem item, int i) {
        mCountTvs[i].setVisibility(View.GONE);
    }

    @Override
    protected void bindVideoItem(ChannelLiveViewModel.VideoItem item, int index) {
        mCountTvs[index].setText(item.getCountString());
        mCountTvs[index].setVisibility(View.VISIBLE);
    }

    @Override
    protected void bindTVItem(ChannelLiveViewModel.TVItem item, int index) {
        mCountTvs[index].setVisibility(View.GONE);
    }

    @Override
    protected void bindSimpleItem(ChannelLiveViewModel.SimpleItem item, int index) {
        mCountTvs[index].setVisibility(View.GONE);
    }

}
