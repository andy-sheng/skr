package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.helper.ConcernHelper;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelRankingViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 并列3个方形图片，带关注操作按钮的item
 */
public class ThreeConcernCardHolder extends RepeatHolder {
    protected int[] mDisplayTvIds;
    protected int[] mConcernBtnIds;
    protected TextView[] mDisplayTvs;
    protected TextView[] mConcernBtns;
    private ConcernHelper mConcernHelper;

    public ThreeConcernCardHolder(View itemView) {
        super(itemView);
        mConcernHelper = new ConcernHelper(itemView.getContext());
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
        Arrays.fill(mIvIds, R.id.avatar_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.name_tv);

        mDisplayTvIds = new int[mViewSize];
        Arrays.fill(mDisplayTvIds, R.id.display_tv);
        mConcernBtnIds = new int[mViewSize];
        Arrays.fill(mConcernBtnIds, R.id.concern_btn);
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mDisplayTvs = new TextView[mViewSize];
        mConcernBtns = new TextView[mViewSize];
        for (int i = 0; i < mViewSize; i++) {
            mDisplayTvs[i] = $(mParentViews[i], mDisplayTvIds[i]);
            mConcernBtns[i] = $(mParentViews[i], mConcernBtnIds[i]);
        }

        for (int i = 0; i < mViewSize; i++) {
            mImageViews[i].getLayoutParams().height = (DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN_THREE * 2) / 3;
        }
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

    protected void bindItemOnUserModel(ChannelRankingViewModel.UserItemData item, int i) {
        mDisplayTvs[i].setText(GlobalData.app().getString(R.string.michannel_fans_cnt, item.getUser().getFansNum()));
        mConcernHelper.initFocusData(item.getUser(), mConcernBtns[i]);
    }

    @Override
    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item, int i) {

    }
}
